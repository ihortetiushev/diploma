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
    @NotBlank(message = "Description is mandatory")
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "Operation date is mandatory")
    private LocalDate operationDate;
    @NotBlank(message = "Category is mandatory")
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private ExpenseCategory category;

    @ManyToOne
    @JoinColumn(name = "asset_id", referencedColumnName = "id")
    private Asset asset;

    @NotNull(message = "Currency is mandatory")
    @ManyToOne
    @JoinColumn(name = "currency_code", referencedColumnName = "currencyCode")
    private Currency currency;

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
