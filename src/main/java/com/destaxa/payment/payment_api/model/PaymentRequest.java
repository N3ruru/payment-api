package com.destaxa.payment.payment_api.model;

import jakarta.validation.constraints.*;

import com.destaxa.payment.payment_api.exception.Exception;

public class PaymentRequest {
    @NotBlank(message = "Número do cartão é obrigatório")
    @Size(min = 16, max = 16, message = "Número do cartão deve ter 16 dígitos")
    private String cardNumber;

    @Positive(message = "O valor do pagamento deve ser positivo")
    private double value;

    @Min(value = 1, message = "Mês de expiração inválido")
    @Max(value = 12, message = "Mês de expiração inválido")
    private int expMonth;

    @Min(value = 1, message = "Ano de expiração inválido")
    @Max(value = 99, message = "Ano de expiração inválido")
    private int expYear;

    @NotBlank(message = "Nome do titular é obrigatório")
    private String holderName;

    @NotBlank(message = "CVV é obrigatório")
    @Size(min = 3, max = 3, message = "CVV deve ter 3 dígitos")
    private String cvv;

    public void validateExpiration() {
        int currentYear = java.time.Year.now().getValue() % 100;
        int currentMonth = java.time.Month.from(java.time.LocalDate.now()).getValue();

        if (expYear < currentYear || (expYear == currentYear && expMonth < currentMonth)) {
            throw new Exception("O cartão expirou. Mês ou ano inválido.");
        }
    }

    public void validateCardNumber() {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        if (sum % 10 != 0) {
            throw new Exception("Número do cartão inválido.");
        }
    }


    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public int getExpMonth() { return expMonth; }
    public void setExpMonth(int expMonth) { this.expMonth = expMonth; }

    public int getExpYear() { return expYear; }
    public void setExpYear(int expYear) { this.expYear = expYear; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}