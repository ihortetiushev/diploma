package ua.nure.finance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "transaction_view") // Name of your view
@Getter
@Setter
public class TransactionView {

    @Id
    private Long id;

    private String type; // "INCOME" or "EXPENSE"

    @Column(name = "operation_date")
    private LocalDate operationDate;

    private String category;

    private BigDecimal amount;

    private String currency;

    @Column(name = "asset_id")
    private Long assetId;

    private String description;

}
