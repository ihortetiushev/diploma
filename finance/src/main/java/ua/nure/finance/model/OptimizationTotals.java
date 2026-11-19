package ua.nure.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "optimization_totals")
@Getter
@Setter
public class OptimizationTotals {

    @Id
    private Long id;

    @Column(precision = 19, scale = 2)
    private BigDecimal optimal;

    @Column(precision = 19, scale = 2)
    private BigDecimal deviation;

    @Column(precision = 19, scale = 2)
    private BigDecimal income;

    @Column(name = "mandatory_expenses", precision = 19, scale = 2)
    private BigDecimal mandatoryExpenses;
}
