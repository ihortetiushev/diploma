package ua.nure.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CategorizedAmount {
    String getCategoryName();

    BigDecimal getAmountMainCurrency();

    LocalDate getOperationDate();

}
