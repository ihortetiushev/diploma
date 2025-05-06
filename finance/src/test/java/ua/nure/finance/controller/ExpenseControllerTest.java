package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Currency;
import ua.nure.finance.model.Expense;
import ua.nure.finance.model.ExpenseCategory;
import ua.nure.finance.repository.*;
import ua.nure.finance.service.ExpenseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseRepository expenseRepository;

    @MockBean
    private IncomeRepository incomeRepository;

    @MockBean
    private ExpenseCategoryRepository expenseCategoryRepository;

    @MockBean
    private CurrencyRepository currencyRepository;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private AssetRepository assetRepository;

    @Test
    void testShowAddForm() throws Exception {
        mockMvc.perform(get("/add-expense"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-expense"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("currencies"))
                .andExpect(model().attributeExists("assets"));
    }

    @Test
    void testShowUpdateForm() throws Exception {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(BigDecimal.TEN);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        mockMvc.perform(get("/expense/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-expense"))
                .andExpect(model().attributeExists("expense"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("currencies"))
                .andExpect(model().attributeExists("assets"));
    }

    @Test
    void testDeleteExpenses() throws Exception {
        Expense expense = new Expense();
        expense.setId(1L);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        mockMvc.perform(get("/delete-expenses/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));
    }

    @Test
    void testExpensesDefaultRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));
    }

    @Test
    void testGetExpensesWithNoParams() throws Exception {
        when(expenseRepository.findByOperationDateBetween(any(), any()))
                .thenReturn(Collections.emptyList());
        when(incomeRepository.findByOperationDateBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(view().name("expenses"))
                .andExpect(model().attributeExists("totalByCategory"))
                .andExpect(model().attributeExists("chartData"))
                .andExpect(model().attributeExists("totalAmount"))
                .andExpect(model().attributeExists("totalIncome"))
                .andExpect(model().attributeExists("totalExpenses"))
                .andExpect(model().attributeExists("diff"))
                .andExpect(model().attributeExists("startDate"))
                .andExpect(model().attributeExists("endDate"));
    }

    @Test
    void testAddExpense_Valid() throws Exception {
        mockMvc.perform(post("/add-expense")
                        .param("amount", "100")
                        .param("amountMainCurrency", "100")
                        .param("currency.currencyCode", "UAH")
                        .param("operationDate", LocalDate.now().toString())
                        .param("category.id", "1")
                        .param("asset.id", "2")
                        .param("description", "some expense"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));

        Mockito.verify(expenseService).saveExpense(any());
    }

    @Test
    void testAddExpense_Invalid() throws Exception {
        mockMvc.perform(post("/add-expense")
                        .param("amount", "")  // Missing required field
                )
                .andExpect(status().isOk())
                .andExpect(view().name("add-expense"));
    }

    @Test
    void testAddExpenseCategory_Valid() throws Exception {
        mockMvc.perform(post("/add-expenses-category")
                        .param("name", "Food"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expense-categories"));

        Mockito.verify(expenseCategoryRepository).save(any());
    }

    @Test
    void testAddExpenseCategory_Invalid() throws Exception {
        mockMvc.perform(post("/add-expenses-category")
                        .param("name", "")) // invalid
                .andExpect(status().isOk())
                .andExpect(view().name("expense-categories"));
    }

    @Test
    void testUpdateExpenseCategory_Valid() throws Exception {
        mockMvc.perform(post("/update-expenses-category")
                        .param("id", "1")
                        .param("name", "Updated Category"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expense-categories"));

        Mockito.verify(expenseCategoryRepository).save(any());
    }

    @Test
    void testUpdateExpenseCategory_Invalid() throws Exception {
        mockMvc.perform(post("/update-expenses-category")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("name", "") // empty triggers @NotBlank
                        .param("keywords", "some,keywords"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("expenseCategory", "name"))
                .andExpect(view().name("expense-categories"));
    }

    @Test
    void testExpenseCategories() throws Exception {
        // Add some categories to your mock repository, if needed
        ExpenseCategory category1 = new ExpenseCategory();
        category1.setName("Name1");
        ExpenseCategory category2 = new ExpenseCategory();
        category2.setName("Name2");
        when(expenseCategoryRepository.findAll()).thenReturn(List.of(category1, category2));

        mockMvc.perform(get("/expense-categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("expense-categories"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("categories", hasSize(2)));  // Checking the size of the list
    }


    @Test
    void testDeleteExpenseCategory() throws Exception {
        // Set up mock data
        ExpenseCategory category = new ExpenseCategory();
        category.setName("Groceries");

        // Mock the repository behavior
        when(expenseCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        doNothing().when(expenseCategoryRepository).delete(any(ExpenseCategory.class));
        when(expenseCategoryRepository.count()).thenReturn(1L);  // Mock the count to be 1

        // Perform delete request
        mockMvc.perform(get("/delete-expense-category/{id}", category.getId()))
                .andExpect(status().is3xxRedirection())  // Expecting a redirect after deletion
                .andExpect(view().name("redirect:/expense-categories"));

        // Verify the interactions
        verify(expenseCategoryRepository, times(1)).delete(category);
    }

    private Expense createExpense() {
        ExpenseCategory category = new ExpenseCategory();
        category.setId(1L);  // Use the valid ID for the category
        category.setName("Groceries");

        // Create a valid Asset (mocking it as an example)
        Asset asset = new Asset();
        asset.setId(1L);  // Assume it already exists
        asset.setName("Cash");

        // Create a valid Currency (mocking it as an example)
        Currency currency = new Currency();
        currency.setCurrencyCode("USD");  // Assuming USD is valid

        // Create Expense object with valid values
        Expense expense = new Expense();
        expense.setId(1L);  // ID of the expense to update
        expense.setAmount(new BigDecimal("100.00"));  // Example amount
        expense.setAmountMainCurrency(new BigDecimal("100.00"));  // Main currency amount
        expense.setDescription("Monthly groceries");  // Valid description
        expense.setOperationDate(LocalDate.of(2025, 5, 4));  // Example date
        expense.setCategory(category);  // Linking valid category
        expense.setAsset(asset);  // Linking valid asset
        expense.setCurrency(currency);  // Linking valid currency

        return expense;
    }

    @Test
    void testUpdateExpenseValid() throws Exception {
        // Prepare an expense to update
        Expense expense = createExpense();

        // Perform the POST request to update the expense
        mockMvc.perform(post("/expense/edit/{id}", expense.getId())  // Pass the expense ID
                        .param("amount", expense.getAmount().toString())  // Pass the amount
                        .param("amountMainCurrency", expense.getAmountMainCurrency().toString())  // Pass the main currency amount
                        .param("description", expense.getDescription())  // Pass the description
                        .param("operationDate", expense.getOperationDate().toString())  // Pass the operation date
                        .param("category.id", String.valueOf(expense.getCategory().getId()))  // Pass the category ID (nested object)
                        .param("asset.id", String.valueOf(expense.getAsset().getId()))  // Pass the asset ID (nested object)
                        .param("currency.currencyCode", expense.getCurrency().getCurrencyCode())  // Pass the currency code (nested object)
                )
                .andExpect(status().is3xxRedirection())  // Expecting a redirect after successful update
                .andExpect(view().name("redirect:/transactions"));  // Expecting redirection to transactions page


        // Verify that the save method was called once on the expenseRepository (mocked behavior)
        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository, times(1)).save(expenseCaptor.capture());

        // Get the captured expense
        Expense capturedExpense = expenseCaptor.getValue();

        // Verify the data of the captured expense
        assertEquals(expense.getAmount(), capturedExpense.getAmount());
        assertEquals(expense.getAmountMainCurrency(), capturedExpense.getAmountMainCurrency());
        assertEquals(expense.getDescription(), capturedExpense.getDescription());
        assertEquals(expense.getOperationDate(), capturedExpense.getOperationDate());
        assertEquals(expense.getCategory().getId(), capturedExpense.getCategory().getId());
        assertEquals(expense.getAsset().getId(), capturedExpense.getAsset().getId());
        assertEquals(expense.getCurrency().getCurrencyCode(), capturedExpense.getCurrency().getCurrencyCode());
    }

    @Test
    void testUpdateExpenseInvalid() throws Exception {
        Expense expense = new Expense();
        expense.setId(2);

        // Attempt to update with invalid data (e.g., missing amount)
        mockMvc.perform(post("/expense/edit/{id}", expense.getId())
                        .param("amount", "")  // Invalid input
                        .param("category", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("update-expense"))
                .andExpect(model().attributeHasFieldErrors("expense", "amount", "category"));  // Verify validation errors
    }

}
