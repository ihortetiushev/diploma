package ua.nure.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "optimization_categories")
@Getter
@Setter
public class OptimizationCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "spending_limit", precision = 19, scale = 2)
    private BigDecimal spendingLimit;

    private Integer priority;

    @Column(precision = 19, scale = 2)
    private BigDecimal desired;

    @Column(precision = 19, scale = 2)
    private BigDecimal optimal;

    @Column(length = 255)
    private String result;

    @Column(precision = 19, scale = 2)
    private BigDecimal deviation;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
