# Destaxa Payment API
### 🚀 API de Autorização de Pagamentos usando **Spring Boot 3.5.0** e **Java 21**

Este projeto implementa uma API REST para autorização de pagamentos via protocolo **ISO8583**, utilizando **Spring Boot** para o cliente e **ServerSocket** para o servidor.

## 📌 Objetivo
Criar um sistema de **cliente-servidor** que:
- Recebe **requisições de pagamento** via API REST.
- Envia solicitações para um **servidor de autorização** via **Socket Server**.
- Processa a resposta simulando um ambiente **ISO8583**.

## 🔹 Funcionalidades
✔️ Endpoint `POST /authorization` para recebimento de pagamentos  
✔️ Envio e recebimento de mensagens via **Socket Server**  
✔️ Validação de transações com simulação de regras de aprovação/rejeição  
✔️ Suporte a concorrência para múltiplos clientes  
✔️ Baseado em **Spring Boot 3.5.0** e **Java 21**

## 🛠 Tecnologias Utilizadas
- **Java 21**
- **Spring Boot 3.5.0**
- **Maven**
- **ServerSocket**
- **Postman** (para testes)

## ⚙ Como Rodar o Projeto
1️⃣ Clone o repositório:
```bash
git clone https://github.com/N3ruru/payment-api.git
cd payment-api
```

2️⃣ Compile e execute a aplicação:

```bash
mvn clean install
mvn spring-boot:run
```
3️⃣ Teste a API com Postman ou cURL:

```bash
curl -X POST http://localhost:8080/authorization \
-H "Content-Type: application/json" \
-d '{"value": 100, "cardNumber": "1234567890123456"}'
```
📜 Licença
Este projeto está sob a licença MIT. Sinta-se à vontade para usá-lo e contribuir!