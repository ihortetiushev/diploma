package ua.nure.finance.model;

import java.math.BigDecimal;

public interface CategorizedAmount {
    String getCategoryName();

    BigDecimal getAmount();

}
