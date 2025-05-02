package ua.nure.finance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.nure.finance.model.ExpenseCategory;
import ua.nure.finance.model.IncomeCategory;
import ua.nure.finance.model.TransactionType;
import ua.nure.finance.repository.ExpenseCategoryRepository;
import ua.nure.finance.repository.IncomeCategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementImportService {
    private final IncomeCategoryRepository incomeCategoryRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public Long assignCategory(String description, TransactionType type) {
        String desc = description.toLowerCase();

        if (type == TransactionType.INCOME) {
            return incomeCategoryRepository.findAll().stream()
                    .filter(cat -> matchesKeyword(desc, cat.getKeywords()))
                    .map(IncomeCategory::getId)
                    .findFirst()
                    .orElse(null);
        } else {
            return expenseCategoryRepository.findAll().stream()
                    .filter(cat -> matchesKeyword(desc, cat.getKeywords()))
                    .map(ExpenseCategory::getId)
                    .findFirst()
                    .orElse(null);
        }
    }

    private boolean matchesKeyword(String text, String keywordCsv) {
        if (keywordCsv == null || keywordCsv.isBlank()) return false;
        return List.of(keywordCsv.split(","))
                .stream()
                .map(String::trim)
                .anyMatch(text::contains);
    }

}
