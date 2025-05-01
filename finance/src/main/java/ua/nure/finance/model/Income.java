package ua.nure.finance.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Valid
@Getter
@Setter
public class Income implements CategorizedAmount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Amount is mandatory")
    private BigDecimal amount;

    @NotNull(message = "Amount in main currency is mandatory")
    @Column(name = "amount_main_currency", nullable = false)
    private BigDecimal amountMainCurrency;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "Operation date is mandatory")
    private LocalDate operationDate;

    @NotNull(message = "Category is mandatory")
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private IncomeCategory category;

    @NotNull(message = "Currency is mandatory")
    @ManyToOne
    @JoinColumn(name = "currency_code", referencedColumnName = "currencyCode")
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "asset_id", referencedColumnName = "id")
    private Asset asset;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getCategoryName() {
        return getCategory().getName();
    }

}
