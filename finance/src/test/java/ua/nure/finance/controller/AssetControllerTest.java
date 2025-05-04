
package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.AssetCategory;
import ua.nure.finance.model.BankStatementImportDTO;
import ua.nure.finance.model.Currency;
import ua.nure.finance.repository.*;
import ua.nure.finance.service.ExpenseService;
import ua.nure.finance.service.IncomeService;
import ua.nure.finance.service.PrivatbankDataImport;
import ua.nure.finance.service.StockPriceService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
public class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetCategoryRepository categoryRepository;
    @MockBean
    private AssetRepository assetRepository;
    @MockBean
    private CurrencyRepository currencyRepository;
    @MockBean
    private PrivatbankDataImport privatbankDataImport;
    @MockBean
    private IncomeCategoryRepository incomeCategoryRepository;
    @MockBean
    private ExpenseCategoryRepository expenseCategoryRepository;
    @MockBean
    IncomeService incomeService;
    @MockBean
    private ExpenseService expenseService;
    @MockBean
    private StockPriceService stockPriceService;

    @Test
    void testListAssets() throws Exception {
        AssetCategory category = new AssetCategory();
        category.setName("TestCat");
        Currency currency = new Currency();
        currency.setCurrencyCode("UAH");
        Asset asset = new Asset();
        asset.setName("Stock");
        asset.setStatus(Asset.Status.active);
        asset.setCategory(category);
        asset.setCurrency(currency);
        asset.setQuantity(1);
        asset.setInitialPricePerShare(BigDecimal.TEN);

        when(assetRepository.findByStatus(eq(Asset.Status.active), any())).thenReturn(Collections.singletonList(asset));
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(stockPriceService.fetchCurrentPrice("Stock")).thenReturn(BigDecimal.valueOf(11));

        mockMvc.perform(get("/assets"))
                .andExpect(status().isOk())
                .andExpect(view().name("assets"))
                .andExpect(model().attributeExists("assets"));
    }

    @Test
    void testAddAssetForm() throws Exception {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/assets/add-asset"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-asset"));
    }

    @Test
    void testAddAsset_Valid() throws Exception {
        AssetCategory category = new AssetCategory();
        category.setId(1L);
        category.setName("Stocks");

        Currency currency = new Currency();
        currency.setCurrencyCode("USD");

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(currencyRepository.findAll()).thenReturn(List.of(currency));

        mockMvc.perform(post("/assets/add-asset")
                        .param("initialValue", "1000.00")
                        .param("currency.currencyCode", "USD")
                        .param("name", "Apple Inc.")
                        .param("description", "Apple shares")
                        .param("category.id", "1")
                        .param("startDate", "2024-01-01")
                        .param("status", "active")
                        .param("initialPricePerShare", "150.00")
                        .param("quantity", "10")
                        .param("stockExchange", "NASDAQ"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assets"));

        verify(assetRepository).save(any(Asset.class));
    }


    @Test
    void testUpdateAssetForm() throws Exception {
        Asset asset = new Asset();
        asset.setId(1L);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/assets/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-asset"));
    }

    @Test
    void testUpdateAsset_Valid() throws Exception {
        mockMvc.perform(post("/assets/edit/1")
                        .param("name", "Updated Asset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assets"));

        verify(assetRepository).save(any());
    }

    @Test
    void testCloseAsset() throws Exception {
        Asset asset = new Asset();
        asset.setId(1L);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        mockMvc.perform(post("/assets/close/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assets"));

        verify(assetRepository).save(any());
    }

    @Test
    void testImportPrivat() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.xls", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());
        BankStatementImportDTO dto = new BankStatementImportDTO();
        dto.setOperations(Collections.emptyList());

        when(privatbankDataImport.importData(any(InputStream.class), eq(1L))).thenReturn(dto);
        when(incomeCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(expenseCategoryRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(multipart("/assets/import")
                        .file(file)
                        .param("fileType", "privatbank")
                        .param("assetId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("import-bank-statement"))
                .andExpect(model().attributeExists("importDTO"));
    }

    @Test
    void testConfirmImport() throws Exception {
        mockMvc.perform(post("/assets/import/confirm"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));
    }
}
