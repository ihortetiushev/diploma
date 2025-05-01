package ua.nure.finance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Immutable
@Table(name = "transaction_view")
@IdClass(TransactionViewId.class)
public class TransactionView {

    @Id
    private Long id;
    @Id
    private String type; // "INCOME" or "EXPENSE"

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "operation_date")
    private LocalDate operationDate;

    private String category;

    private BigDecimal amount;

    private BigDecimal amountMainCurrency;

    private String currency;

    @ManyToOne
    @JoinColumn(name = "asset_id", referencedColumnName = "id")
    private Asset asset;

    private String description;

}
