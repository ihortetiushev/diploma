package ua.nure.finance.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.BankStatementImportDTO;
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

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public BankStatementImportDTO importData(InputStream inputStream, Long assetId) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid asset id Id:" + assetId));
        List<TransactionView> operations = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() < 2) continue; // skip header

            TransactionView dto = new TransactionView();
            dto.setAsset(asset);
            LocalDate date = LocalDateTime.parse(row.getCell(0).getStringCellValue(), DATE_FORMATTER).toLocalDate();
            dto.setOperationDate(date);
            BigDecimal amount = BigDecimal.valueOf(row.getCell(4).getNumericCellValue());
            dto.setAmount(amount.abs());
            dto.setCurrency(row.getCell(5).getStringCellValue());
            dto.setDescription(row.getCell(3).getStringCellValue());
            dto.setType(amount.signum() > 0 ? "INCOME" : "EXPENSE");
            //TODO
            dto.setCategory("Category");
            operations.add(dto);
        }

/*        TransactionView item1 = new TransactionView();
        item1.setAmount(new BigDecimal(1));
        item1.setCurrency("UAH");
        item1.setAsset(asset);
        item1.setType("INCOME");
        item1.setCategory("Category");
        item1.setDescription("Description");
        item1.setOperationDate(LocalDate.now());
        TransactionView item2 = new TransactionView();
        item2.setAmount(new BigDecimal(2));
        item2.setCurrency("UAH");
        item2.setAsset(asset);
        item2.setType("EXPENSE");
        item2.setCategory("Category1");
        item2.setDescription("Description2");
        item2.setOperationDate(LocalDate.now());
        dataList.add(item1);
        dataList.add(item2);*/
        BankStatementImportDTO bankStatementImportDTO = new BankStatementImportDTO();
        bankStatementImportDTO.setOperations(operations);
        return bankStatementImportDTO;
    }

}
