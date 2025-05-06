package ua.nure.finance.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Income;
import ua.nure.finance.repository.AssetRepository;
import ua.nure.finance.repository.IncomeRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private IncomeService incomeService;

    @Test
    void saveIncome_WithAsset_ShouldSaveIncomeAndUpdateAssetValue() {
        // Arrange
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setCurrentValue(new BigDecimal("1000.00"));

        Income income = new Income();
        income.setId(1L);
        income.setAmount(new BigDecimal("200.00"));
        income.setAsset(asset);

        // Act
        incomeService.saveIncome(income);

        // Assert
        verify(incomeRepository).save(income);
        assertEquals(new BigDecimal("1200.00"), asset.getCurrentValue());
        verify(assetRepository).save(asset);
    }

    @Test
    void saveIncome_WithoutAsset_ShouldOnlySaveIncome() {
        // Arrange
        Income income = new Income();
        income.setId(2L);
        income.setAmount(new BigDecimal("99.99"));
        income.setAsset(null);

        // Act
        incomeService.saveIncome(income);

        // Assert
        verify(incomeRepository).save(income);
        verify(assetRepository, never()).save(any());
    }
}
