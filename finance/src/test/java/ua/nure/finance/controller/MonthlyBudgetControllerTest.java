package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.finance.model.Expense;
import ua.nure.finance.model.Income;
import ua.nure.finance.repository.ExpenseRepository;
import ua.nure.finance.repository.IncomeRepository;
import ua.nure.finance.service.AiAnalysisService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MonthlyBudgetController.class)
class MonthlyBudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncomeRepository incomeRepository;

    @MockitoBean
    private ExpenseRepository expenseRepository;

    @MockitoBean
    private AiAnalysisService aiAnalysisService;

    @Test
    void selectedDateRangeLoadsTransactionsAndDisplaysMonthlyTotals() throws Exception {
        LocalDate from = LocalDate.of(2026, 1, 15);
        LocalDate to = LocalDate.of(2026, 2, 15);
        Income januaryIncome = income(LocalDate.of(2026, 1, 20), "120.50");
        Income februaryIncome = income(LocalDate.of(2026, 2, 10), "300");
        Expense januaryExpense = expense(LocalDate.of(2026, 1, 25), "45.25");
        when(incomeRepository.findByOperationDateBetween(from, to))
                .thenReturn(List.of(januaryIncome, februaryIncome));
        when(expenseRepository.findByOperationDateBetween(from, to)).thenReturn(List.of(januaryExpense));

        mockMvc.perform(get("/planning").param("from", "2026-01-15").param("to", "2026-02-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("planning"))
                .andExpect(content().string(containsString("Доходи та витрати за період")))
                .andExpect(content().string(containsString("січень 2026")))
                .andExpect(content().string(containsString("лютий 2026")))
                .andExpect(content().string(containsString("420.50")))
                .andExpect(content().string(containsString("45.25")))
                .andExpect(model().attribute("actualPeriodIncome", new BigDecimal("420.50")))
                .andExpect(model().attribute("actualPeriodExpenses", new BigDecimal("45.25")));

        verify(incomeRepository).findByOperationDateBetween(from, to);
        verify(expenseRepository).findByOperationDateBetween(from, to);
    }

    @Test
    void invalidDateRangeDisplaysErrorWithoutQueryingDatabase() throws Exception {
        mockMvc.perform(get("/planning").param("from", "2026-02-15").param("to", "2026-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("planning"))
                .andExpect(content().string(containsString("Дата початку не може бути пізніше дати завершення.")));
    }

    @Test
    void promptUsesSelectedPeriodAndDisplaysAiAnswer() throws Exception {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);
        when(incomeRepository.findByOperationDateBetween(from, to))
                .thenReturn(List.of(income(LocalDate.of(2026, 3, 10), "1200")));
        when(expenseRepository.findByOperationDateBetween(from, to))
                .thenReturn(List.of(expense(LocalDate.of(2026, 3, 15), "500")));
        when(aiAnalysisService.analyze(eq("Проаналізуй бюджет"), eq(from), eq(to),
                anyList(), eq(new BigDecimal("1200")), eq(new BigDecimal("500"))))
                .thenReturn("### Підсумок\n\n| Показник | Сума |\n| --- | ---: |\n| Доходи | 1200 |\n\n<script>alert(1)</script>");

        mockMvc.perform(post("/planning/ai")
                        .param("from", "2026-03-01")
                        .param("to", "2026-03-31")
                        .param("prompt", "Проаналізуй бюджет"))
                .andExpect(status().isOk())
                .andExpect(view().name("planning"))
                .andExpect(content().string(containsString("<h3>Підсумок</h3>")))
                .andExpect(content().string(containsString("<table>")))
                .andExpect(content().string(containsString("<th>Показник</th>")))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));

        verify(aiAnalysisService).analyze(eq("Проаналізуй бюджет"), eq(from), eq(to),
                anyList(), eq(new BigDecimal("1200")), eq(new BigDecimal("500")));
    }

    private Income income(LocalDate date, String amount) {
        Income income = new Income();
        income.setOperationDate(date);
        income.setAmountMainCurrency(new BigDecimal(amount));
        return income;
    }

    private Expense expense(LocalDate date, String amount) {
        Expense expense = new Expense();
        expense.setOperationDate(date);
        expense.setAmountMainCurrency(new BigDecimal(amount));
        return expense;
    }
}
