package ua.nure.finance.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Valid
public class Currency {
    @Id
    private String currencyCode;

}
