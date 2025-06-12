package com.destaxa.payment.payment_api.config;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArraySet;

public class PaymentWebSocketHandler extends TextWebSocketHandler {
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Mensagem recebida: " + payload);

        // Simulação de fluxo de pagamento com diferentes etapas
        session.sendMessage(new TextMessage("🔄 Iniciando processamento do pagamento..."));
        Thread.sleep(2000); // Simulando tempo de processamento

        session.sendMessage(new TextMessage("✅ Pagamento autorizado pelo banco."));
        Thread.sleep(2000); // Simulando tempo de resposta do banco

        session.sendMessage(new TextMessage("🚀 Pagamento concluído!"));
    }


    public static void sendMessageToClients(String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    System.err.println("Erro ao enviar mensagem WebSocket: " + e.getMessage());
                }
            }
        }
    }
}
