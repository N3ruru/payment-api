package com.destaxa.payment.payment_api.controller;

import com.destaxa.payment.payment_api.model.PaymentRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/authorization")
public class PaymentController {

    @PostMapping
    public ResponseEntity<Map<String, String>> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        paymentRequest.validateExpiration(); // Validação de expiração
        paymentRequest.validateCardNumber(); // Validação do número do cartão

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Pagamento válido! Processando R$ " + paymentRequest.getValue() + " para " + paymentRequest.getHolderName());
        return ResponseEntity.ok(successResponse);
    }

}