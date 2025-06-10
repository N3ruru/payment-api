package com.destaxa.payment.payment_api.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authorization")
public class PaymentController {

    @PostMapping
    public String processPayment(@RequestBody String requestBody) {
        return "Processando pagamento: " + requestBody;
    }
}
