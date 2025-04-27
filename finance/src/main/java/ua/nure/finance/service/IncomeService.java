package ua.nure.finance.service;

import org.springframework.stereotype.Service;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Income;
import ua.nure.finance.repository.AssetRepository;
import ua.nure.finance.repository.IncomeRepository;

@Service
public class IncomeService {
    private final IncomeRepository incomeRepository;
    private final AssetRepository assetRepository;

    public IncomeService(IncomeRepository incomeRepository, AssetRepository assetRepository) {
        this.incomeRepository = incomeRepository;
        this.assetRepository = assetRepository;
    }

    public void saveIncome(Income income) {
        incomeRepository.save(income);

        if (income.getAsset() != null) {
            Asset asset = income.getAsset();
            asset.setCurrentValue(asset.getCurrentValue().add(income.getAmount()));
            assetRepository.save(asset);
        }
    }
}
