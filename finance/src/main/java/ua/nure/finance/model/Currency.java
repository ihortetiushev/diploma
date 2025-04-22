package ua.nure.finance.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.Valid;


@Entity
@Valid
public class Currency {
    @Id
    private String currencyCode;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
