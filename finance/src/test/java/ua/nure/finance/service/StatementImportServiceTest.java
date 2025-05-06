package ua.nure.finance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.nure.finance.model.ExpenseCategory;
import ua.nure.finance.model.IncomeCategory;
import ua.nure.finance.model.TransactionType;
import ua.nure.finance.repository.ExpenseCategoryRepository;
import ua.nure.finance.repository.IncomeCategoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatementImportServiceTest {

    private IncomeCategoryRepository incomeCategoryRepository;
    private ExpenseCategoryRepository expenseCategoryRepository;
    private StatementImportService statementImportService;

    @BeforeEach
    void setUp() {
        incomeCategoryRepository = mock(IncomeCategoryRepository.class);
        expenseCategoryRepository = mock(ExpenseCategoryRepository.class);
        statementImportService = new StatementImportService(incomeCategoryRepository, expenseCategoryRepository);
    }

    @Test
    void assignCategory_shouldReturnIncomeCategoryId_whenKeywordMatches() {
        IncomeCategory salary = new IncomeCategory();
        salary.setId(1L);
        salary.setKeywords("salary,bonus");

        when(incomeCategoryRepository.findAll()).thenReturn(List.of(salary));

        Long result = statementImportService.assignCategory("Monthly Salary from work", TransactionType.INCOME);

        assertEquals(1L, result);
    }

    @Test
    void assignCategory_shouldReturnExpenseCategoryId_whenKeywordMatches() {
        ExpenseCategory groceries = new ExpenseCategory();
        groceries.setId(2L);
        groceries.setKeywords("groceries,supermarket");

        when(expenseCategoryRepository.findAll()).thenReturn(List.of(groceries));

        Long result = statementImportService.assignCategory("Bought groceries at local supermarket", TransactionType.EXPENSE);

        assertEquals(2L, result);
    }

    @Test
    void assignCategory_shouldReturnNull_whenNoMatchFound() {
        when(incomeCategoryRepository.findAll()).thenReturn(List.of());

        Long result = statementImportService.assignCategory("Some unrelated description", TransactionType.INCOME);

        assertNull(result);
    }
}
