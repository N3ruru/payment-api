package com.destaxa.payment.payment_api.util;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

import java.io.InputStream;

public class ISO8583Util {

    public static ISOMsg createPaymentRequest(double value, String cardNumber, String externalId) throws ISOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("iso8583.xml")) {
            if (inputStream == null) {
                throw new ISOException("❌ Erro: Arquivo iso8583.xml não encontrado!");
            }

            System.out.println("✅ iso8583.xml carregado com sucesso!");
            GenericPackager packager = new GenericPackager(inputStream);

            ISOMsg isoMsg = new ISOMsg();

            for (int i = 0; i <= 127; i++) { // ISO8583 tem campos até 127
                if (packager.getFieldPackager(i) == null) {
                    System.out.println("❌ Campo não inicializado: " + i);
                } else {
                    System.out.println("✅ Campo " + i + " inicializado corretamente!");
                }
            }


            isoMsg.setPackager(packager);

// **Definir MTI corretamente antes dos outros campos**
            isoMsg.setMTI("0200");

            isoMsg.set(2, cardNumber);
            isoMsg.set(4, String.format("%.2f", value));
            isoMsg.set(11, externalId);
            isoMsg.set(12, "123456"); // Exemplo: Tempo da transação
            isoMsg.set(37, "ABC123456789"); // Exemplo: Número de referência
            isoMsg.set(38, "AUTH12"); // Exemplo: Código de autorização
            isoMsg.set(39, "00"); // Exemplo: Código de resposta

            System.out.println("📢 Campos ISO8583 configurados:");
            System.out.println("MTI: " + isoMsg.getMTI());
            System.out.println("Campo 2 (Número do Cartão): " + isoMsg.getString(2));
            System.out.println("Campo 4 (Valor da Transação): " + isoMsg.getString(4));
            System.out.println("Campo 11 (NSU): " + isoMsg.getString(11));

            return isoMsg;
        } catch (Exception e) {
            throw new ISOException("Erro ao criar requisição ISO8583", e);
        }
    }
}
