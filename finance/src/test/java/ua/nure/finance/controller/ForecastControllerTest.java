package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.finance.model.Expense;
import ua.nure.finance.model.Income;
import ua.nure.finance.repository.ExpenseRepository;
import ua.nure.finance.repository.IncomeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ForecastController.class)
class ForecastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncomeRepository incomeRepository;

    @MockBean
    private ExpenseRepository expenseRepository;

    @Test
    void getForecastDisplaysSixMonthlyRowsWithIncomeAndExpenseTotals() throws Exception {
        LocalDate today = LocalDate.now();
        YearMonth lastCompletedMonth = YearMonth.from(today).minusMonths(1);
        LocalDate startDate = lastCompletedMonth.minusMonths(5).atDay(1);
        LocalDate endDate = lastCompletedMonth.atEndOfMonth();
        Income firstIncome = new Income();
        firstIncome.setOperationDate(lastCompletedMonth.atDay(1));
        firstIncome.setAmountMainCurrency(new BigDecimal("100.25"));
        Income secondIncome = new Income();
        secondIncome.setOperationDate(endDate);
        secondIncome.setAmountMainCurrency(new BigDecimal("25.25"));
        Expense expense = new Expense();
        expense.setOperationDate(endDate);
        expense.setAmountMainCurrency(new BigDecimal("40.00"));
        when(incomeRepository.findByAsset_PrimaryAccountTrueAndOperationDateBetween(
                startDate, endDate)).thenReturn(List.of(firstIncome, secondIncome));
        when(expenseRepository.findByAsset_PrimaryAccountTrueAndOperationDateBetween(
                startDate, endDate)).thenReturn(List.of(expense));

        mockMvc.perform(get("/forecast"))
                .andExpect(status().isOk())
                .andExpect(view().name("forecast"))
                .andExpect(model().attribute("months", hasSize(6)))
                .andExpect(content().string(containsString("125.50")))
                .andExpect(content().string(containsString("40.00")));
    }
}
