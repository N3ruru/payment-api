package com.destaxa.payment.payment_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class PaymentRequest {
    @JsonProperty("external_id")
    @NotBlank(message = "O ID externo não pode ser vazio.")
    private String externalId;

    @JsonProperty("value")
    @Positive(message = "O valor do pagamento deve ser maior que zero.")
    private double value;

    @JsonProperty("card_number")
    @NotBlank(message = "O número do cartão não pode ser vazio.")
    @Size(min = 13, max = 19, message = "O número do cartão deve ter entre 13 e 19 dígitos.")
    private String cardNumber;

    @JsonProperty("installments")
    @Min(value = 1, message = "O número mínimo de parcelas é 1.")
    private int installments;

    @JsonProperty("cvv")
    @NotBlank(message = "O CVV não pode ser vazio.")
    @Size(min = 3, max = 4, message = "O CVV deve ter 3 ou 4 dígitos.")
    private String cvv;

    @JsonProperty("exp_month")
    @Min(value = 1, message = "Mês de expiração inválido.")
    @Max(value = 12, message = "Mês de expiração inválido.")
    private int expMonth;

    @JsonProperty("exp_year")
    @Min(value = 2024, message = "Ano de expiração inválido.")
    private int expYear;

    @JsonProperty("holder_name")
    @NotBlank(message = "O nome do titular não pode ser vazio.")
    private String holderName;

    // Getters e Setters
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public int getInstallments() { return installments; }
    public void setInstallments(int installments) { this.installments = installments; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public int getExpMonth() { return expMonth; }
    public void setExpMonth(int expMonth) { this.expMonth = expMonth; }

    public int getExpYear() { return expYear; }
    public void setExpYear(int expYear) { this.expYear = expYear; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
}
