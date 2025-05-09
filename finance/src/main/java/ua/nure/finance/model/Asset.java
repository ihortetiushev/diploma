package ua.nure.finance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
@Getter
@Setter
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "initial_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialValue;

    @Column(name = "current_value", precision = 19, scale = 2)
    private BigDecimal currentValue;

    @NotNull(message = "Currency is mandatory")
    @ManyToOne
    @JoinColumn(name = "currency_code", referencedColumnName = "currencyCode")
    private Currency currency;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "last_valuated_date")
    private LocalDateTime lastValuatedDate;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private AssetCategory category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.active;

    @Column(name = "initial_price_per_share", precision = 19, scale = 4)
    private BigDecimal initialPricePerShare;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "stock_exchange", length = 50)
    private String stockExchange;

    public enum Status {
        active, closed
    }
}
