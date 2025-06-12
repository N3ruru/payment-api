package com.destaxa.payment.payment_api.controller;

import com.destaxa.payment.payment_api.model.PaymentRequest;
import com.destaxa.payment.payment_api.util.ISO8583Util;
import jakarta.validation.Valid;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/authorization")
public class PaymentController {

    @PostMapping
    public ResponseEntity<Map<String, String>> processPayment(@Valid @RequestBody PaymentRequest request) throws Exception {
        System.out.println("🔍 Número do cartão recebido: " + request.getCardNumber());
        System.out.println("🔍 NSU recebido: " + request.getExternalId());

        ISOMsg isoRequest = ISO8583Util.createPaymentRequest(request.getValue(), request.getCardNumber(), request.getExternalId());

        System.out.println("📢 ISO8583 gerado: " + isoRequest);

        String serverResponse = sendToAuthorizer(isoRequest.pack());

        System.out.println("📢 ISO8583 enviado ao servidor! Aguardando resposta...");

        ISOMsg isoResponse = new ISOMsg();
        isoResponse.unpack(serverResponse.getBytes());

        System.out.println("✅ ISO8583 resposta decodificada:");
        for (int i = 0; i <= 127; i++) {
            if (isoResponse.hasField(i)) {
                System.out.println("Campo " + i + ": " + isoResponse.getString(i));
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("payment_id", request.getExternalId());
        response.put("value", String.valueOf(request.getValue()));
        response.put("response_code", isoResponse.getString(39));
        response.put("authorization_code", isoResponse.getString(38));
        response.put("transaction_date", "24-11-18");

        return ResponseEntity.ok(response);
    }


    private String sendToAuthorizer(byte[] packedMessage) throws IOException {
        Socket socket = new Socket();
        // Set connection timeout (5 seconds)
        socket.connect(new InetSocketAddress("127.0.0.1", 9090), 5000);
        // Set read timeout (10 seconds) - CRUCIAL to prevent indefinite hangs
        socket.setSoTimeout(10000); // 10 seconds for read timeout

        System.out.println("📢 Enviando mensagem ao servidor ISO8583...");
        System.out.println("📤 Dados enviados (ISO Message Payload): " + Arrays.toString(packedMessage));

        OutputStream out = socket.getOutputStream();

        // --- NEW CODE: Add the 2-byte length header ---
        int messageLength = packedMessage.length;
        // Convert integer length to a 2-byte binary representation
        byte[] lengthHeader = new byte[2];
        lengthHeader[0] = (byte) (messageLength >> 8); // High byte
        lengthHeader[1] = (byte) (messageLength & 0xFF); // Low byte

        System.out.println("📦 Length Header (Bytes): " + Arrays.toString(lengthHeader) + " (for length " + messageLength + " bytes)");
        System.out.println("📦 Length Header (Hex): " + ISOUtil.hexString(lengthHeader));


        // Write the length header first
        out.write(lengthHeader);
        // Then write the actual ISO message payload
        out.write(packedMessage);
        out.flush();

        InputStream in = socket.getInputStream();
        byte[] responseHeader = new byte[2]; // Expecting a 2-byte length header for the response too

        int bytesReadHeader = in.read(responseHeader);
        if (bytesReadHeader != 2) {
            throw new IOException("❌ Erro: Não foi possível ler o cabeçalho de comprimento da resposta ISO8583!");
        }

        int responseLength = (responseHeader[0] & 0xFF) << 8 | (responseHeader[1] & 0xFF);
        System.out.println("✅ Cabeçalho de Resposta (Bytes): " + Arrays.toString(responseHeader) + " (comprimento esperado: " + responseLength + " bytes)");

        byte[] responsePayload = new byte[responseLength];
        int bytesReadPayload = 0;
        int totalBytesRead = 0;
        while (totalBytesRead < responseLength && (bytesReadPayload = in.read(responsePayload, totalBytesRead, responseLength - totalBytesRead)) != -1) {
            totalBytesRead += bytesReadPayload;
        }

        System.out.println("📢 Bytes lidos do servidor (total): " + totalBytesRead);
        if (totalBytesRead > 0) {
            System.out.println("✅ Resposta ISO8583 recebida do servidor (payload): " + Arrays.toString(Arrays.copyOf(responsePayload, totalBytesRead)));
            System.out.println("✅ Resposta ISO8583 recebida do servidor (payload Hex): " + ISOUtil.hexString(Arrays.copyOf(responsePayload, totalBytesRead)));
        }

        socket.close();

        if (totalBytesRead > 0) {
            String serverResponse = new String(Arrays.copyOf(responsePayload, totalBytesRead)); // Assuming ASCII response
            System.out.println("✅ Resposta decodificada do servidor ISO8583: " + serverResponse);
            return serverResponse;
        } else {
            throw new IOException("❌ Erro: Nenhuma resposta válida do servidor ISO8583!");
        }
    }
}
