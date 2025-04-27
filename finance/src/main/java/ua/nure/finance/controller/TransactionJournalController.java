package ua.nure.finance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nure.finance.model.TransactionView;
import ua.nure.finance.repository.TransactionViewRepository;

import java.time.LocalDate;
import java.util.List;

@Controller
public class TransactionJournalController {

    @Autowired
    private TransactionViewRepository transactionViewRepository;

    @GetMapping("/transactions")
    public String listTransactions(
            @RequestParam(required = false) String descriptionFilter,
            @RequestParam(required = false) String typeFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        List<TransactionView> transactions = transactionViewRepository.searchTransactions(
                descriptionFilter,
                typeFilter,
                startDate,
                endDate
        );

        model.addAttribute("transactions", transactions);
        model.addAttribute("descriptionFilter", descriptionFilter);
        model.addAttribute("typeFilter", typeFilter);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "transactions";
    }

}