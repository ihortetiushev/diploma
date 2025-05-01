package ua.nure.finance.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BankStatementImportDTO {
    private List<TransactionView> operations = new ArrayList<>();
}
