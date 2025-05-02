package ua.nure.finance.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.BankStatementImportDTO;
import ua.nure.finance.model.TransactionType;
import ua.nure.finance.model.TransactionView;
import ua.nure.finance.repository.AssetRepository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrivatbankDataImport {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private StatementImportService statementImportService;

    private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final int HEADER_ROWS = 2;
    private final int DATE_COLUMN_INDEX = 0;
    private final int DESCRIPTION_COLUMN_INDEX = 3;
    private final int AMOUNT_COLUMN_INDEX = 4;
    private final int CURRENCY_COLUMN_INDEX = 5;

    public BankStatementImportDTO importData(InputStream inputStream, Long assetId) throws IOException {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid asset id Id:" + assetId));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        List<TransactionView> operations = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() < HEADER_ROWS) {
                continue; // skip header
            }
            TransactionView dto = rowToDto(row);
            dto.setAsset(asset);
            operations.add(dto);
        }
        BankStatementImportDTO bankStatementImportDTO = new BankStatementImportDTO();
        bankStatementImportDTO.setOperations(operations);
        return bankStatementImportDTO;
    }

    private TransactionView rowToDto(Row row) {
        TransactionView dto = new TransactionView();
        LocalDate date = LocalDateTime.parse(row.getCell(DATE_COLUMN_INDEX).getStringCellValue(), DATE_FORMATTER).toLocalDate();
        dto.setOperationDate(date);
        BigDecimal amount = BigDecimal.valueOf(row.getCell(AMOUNT_COLUMN_INDEX).getNumericCellValue());
        dto.setAmount(amount.abs());
        dto.setCurrency(row.getCell(CURRENCY_COLUMN_INDEX).getStringCellValue());
        dto.setDescription(row.getCell(DESCRIPTION_COLUMN_INDEX).getStringCellValue());
        TransactionType type = amount.signum() > 0 ? TransactionType.INCOME : TransactionType.EXPENSE;
        dto.setType(type.name());
        Long categoryId = statementImportService.assignCategory(dto.getDescription(), type);
        dto.setCategory(categoryId != null ? categoryId.toString() : null);
        return dto;
    }

}
