package com.destaxa.payment.payment_api.server;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil; // Import ISOUtil for hexString conversion
import org.jpos.iso.packager.GenericPackager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException; // Specific exception for read timeouts
import java.util.Arrays; // For Array.copyOfRange for better byte array handling

public class PaymentAuthorizerServer {
    private static final int PORT = 9090; // Define port as a constant
    private static final int READ_TIMEOUT_MS = 15000; // Server read timeout (e.g., 15 seconds)

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 Servidor Autorizador iniciado na porta " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Blocks until a client connects
                System.out.println("✅ Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                // Process each client in a new thread for concurrent handling
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("❌ Erro ao iniciar ou aceitar conexão no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {

            // Set a read timeout for the client socket to prevent indefinite blocking
            clientSocket.setSoTimeout(READ_TIMEOUT_MS);

            // 1. Read the 2-byte length header
            byte[] lengthHeader = new byte[2];
            int bytesReadHeader = inputStream.read(lengthHeader);

            if (bytesReadHeader == -1) {
                System.out.println("🚫 Cliente fechou a conexão antes de enviar o cabeçalho de comprimento.");
                return;
            }
            if (bytesReadHeader != 2) {
                System.err.println("❌ Erro: Cabeçalho de comprimento incompleto recebido (" + bytesReadHeader + " bytes). Esperado 2 bytes. Hex: " + ISOUtil.hexString(Arrays.copyOf(lengthHeader, bytesReadHeader)));
                return; // Exit if header is not fully received
            }

            int messageLength = (lengthHeader[0] & 0xFF) << 8 | (lengthHeader[1] & 0xFF);
            System.out.println("📥 Recebido Cabeçalho de Comprimento (Hex): " + ISOUtil.hexString(lengthHeader) + " (Comprimento da Mensagem Esperado: " + messageLength + " bytes)");

            // 2. Read the actual ISO 8583 message payload based on the length header
            byte[] requestBytes = new byte[messageLength];
            int totalBytesRead = 0;
            int bytesReadCurrent;
            // Loop to ensure all bytes are read, as InputStream.read might not fill the buffer in one go
            while (totalBytesRead < messageLength && (bytesReadCurrent = inputStream.read(requestBytes, totalBytesRead, messageLength - totalBytesRead)) != -1) {
                totalBytesRead += bytesReadCurrent;
            }

            if (totalBytesRead == -1) { // End of stream reached unexpectedly
                System.err.println("❌ Erro: Conexão fechada inesperadamente pelo cliente durante a leitura da mensagem.");
                return;
            }
            if (totalBytesRead != messageLength) {
                System.err.println("❌ Erro: Comprimento da mensagem recebida (" + totalBytesRead + " bytes) não corresponde ao esperado (" + messageLength + " bytes).");
                // Log partial message for debugging if needed
                System.err.println("Mensagem Parcial (Hex): " + ISOUtil.hexString(Arrays.copyOf(requestBytes, totalBytesRead)));
                return;
            }

            System.out.println("📥 Recebido Payload da Mensagem ISO8583 (Hex): " + ISOUtil.hexString(requestBytes));

            // 3. Process the ISO 8583 message and get a response
            byte[] responseBytes = processISO8583(requestBytes);

            if (responseBytes != null) {
                // 4. Prepend a 2-byte length header to the response
                byte[] responseLengthHeader = new byte[2];
                responseLengthHeader[0] = (byte) (responseBytes.length >> 8); // High byte
                responseLengthHeader[1] = (byte) (responseBytes.length & 0xFF); // Low byte

                System.out.println("📦 Enviando Cabeçalho de Resposta (Hex): " + ISOUtil.hexString(responseLengthHeader) + " (Comprimento do Payload: " + responseBytes.length + " bytes)");
                System.out.println("📤 Enviando Payload de Resposta (Hex): " + ISOUtil.hexString(responseBytes));

                outputStream.write(responseLengthHeader); // Write length header first
                outputStream.write(responseBytes);         // Then write the actual response payload
                outputStream.flush();
                System.out.println("✅ Resposta enviada ao cliente.");
            } else {
                System.err.println("❌ Não foi possível gerar uma resposta para a mensagem ISO8583.");
            }

        } catch (SocketTimeoutException e) {
            System.err.println("⏰ Erro de Tempo Limite de Leitura no Cliente: " + e.getMessage());
            e.printStackTrace();
        } catch (ISOException e) {
            System.err.println("❌ Erro ISOException ao processar mensagem do cliente: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("❌ Erro de I/O na comunicação com o cliente: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao lidar com o cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                    System.out.println("🔌 Cliente desconectado: " + clientSocket.getInetAddress().getHostAddress());
                }
            } catch (IOException e) {
                System.err.println("❌ Erro ao fechar o socket do cliente: " + e.getMessage());
            }
        }
    }

    private static byte[] processISO8583(byte[] requestData) throws ISOException {
        GenericPackager packager = null;
        try (InputStream inputStream = PaymentAuthorizerServer.class.getResourceAsStream("/iso8583.xml")) {
            if (inputStream == null) {
                throw new ISOException("❌ Erro: Arquivo iso8583.xml não encontrado no servidor!");
            }
            System.out.println("✅ iso8583.xml carregado com sucesso no servidor!");
            packager = new GenericPackager(inputStream);
        } catch (IOException e) {
            throw new ISOException("Erro ao carregar packager do XML no servidor", e);
        }

        ISOMsg isoRequest = new ISOMsg();
        isoRequest.setPackager(packager);

        // Ensure the input data is valid before unpacking
        if (requestData == null || requestData.length == 0) {
            throw new ISOException("Dados de requisição nulos ou vazios no servidor.");
        }

        isoRequest.unpack(requestData);

        // Debugging received fields on the server side
        System.out.println("Server: Unpacked MTI: " + isoRequest.getMTI());
        System.out.println("Server: Unpacked Field 2 (Card Number): " + isoRequest.getString(2));
        System.out.println("Server: Unpacked Field 4 (Transaction Value): " + isoRequest.getString(4));
        System.out.println("Server: Unpacked Field 11 (NSU): " + isoRequest.getString(11));
        System.out.println("Server: Unpacked Field 12 (Transaction Time): " + isoRequest.getString(12));
        System.out.println("Server: Unpacked Field 37 (Retrieval Reference Number): " + isoRequest.getString(37));
        System.out.println("Server: Unpacked Field 38 (Authorization Code): " + isoRequest.getString(38));


        String valueStr = isoRequest.getString(4);
        double value;
        try {
            // Replace comma with dot for parsing if the locale expects dot for decimals
            value = Double.parseDouble(valueStr.replace(',', '.'));
        } catch (NumberFormatException e) {
            System.err.println("❌ Erro de formato de número no Campo 4: " + valueStr);
            throw new ISOException("Formato inválido para valor da transação (Campo 4)", e);
        }


        ISOMsg isoResponse = new ISOMsg();
        isoResponse.setPackager(packager);
        isoResponse.setMTI("0210"); // Response MTI for a 0200 request
        isoResponse.set(4, String.format("%.2f", value)); // Return the value, formatted
        isoResponse.set(11, isoRequest.getString(11)); // Echo NSU
        isoResponse.set(12, isoRequest.getString(12)); // Echo Transaction Time
        isoResponse.set(37, isoRequest.getString(37)); // Echo Retrieval Reference Number

        // Simple authorization logic: "00" for even value, "05" for odd value (or "051" as in your original)
        // Ensure the length matches your XML definition for Field 39 (length="2")
        isoResponse.set(39, value % 2 == 0 ? "00" : "05"); // "00" for success, "05" for denied

        return isoResponse.pack();
    }
}