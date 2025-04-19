package ua.nure.finance.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Valid
public class Expenses implements CategorizedAmount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull(message = "Amount is mandatory")
    private BigDecimal amount;
    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotNull(message = "Operation date is mandatory")
    private LocalDate operationDate;

    @NotBlank(message = "Category is mandatory")
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private ExpensesCategory category;


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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(LocalDate operationDate) {
        this.operationDate = operationDate;
    }

    public ExpensesCategory getCategory() {
        return category;
    }

    public void setCategory(ExpensesCategory category) {
        this.category = category;
    }
}
