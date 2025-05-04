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
@Table(name = "expenses")
public class Expense implements CategorizedAmount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Amount is mandatory")
    private BigDecimal amount;

    @NotNull(message = "Amount in main currency is mandatory")
    @Column(name = "amount_main_currency", nullable = false)
    private BigDecimal amountMainCurrency;

    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "Operation date is mandatory")
    private LocalDate operationDate;

    @NotNull(message = "Category is mandatory")
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private ExpenseCategory category;

    @ManyToOne
    @NotNull(message = "Asset is mandatory")
    @JoinColumn(name = "asset_id", referencedColumnName = "id")
    private Asset asset;

    @NotNull(message = "Currency is mandatory")
    @ManyToOne
    @JoinColumn(name = "currency_code", referencedColumnName = "currencyCode")
    private Currency currency;

    @Override
    public String getCategoryName() {
        return getCategory().getName();
    }

}
