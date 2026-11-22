package ua.nure.finance.controller;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nure.finance.model.CategorizedAmount;
import ua.nure.finance.repository.ExpenseRepository;
import ua.nure.finance.repository.IncomeRepository;
import ua.nure.finance.service.AiAnalysisService;
import ua.nure.finance.service.AiAnalysisService.AiAnalysisException;
import ua.nure.finance.service.AiAnalysisService.MonthlySummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class MonthlyBudgetController {

    private final Parser markdownParser = Parser.builder()
            .extensions(List.<Extension>of(TablesExtension.create()))
            .build();
    private final HtmlRenderer markdownRenderer = HtmlRenderer.builder()
            .extensions(List.<Extension>of(TablesExtension.create()))
            .escapeHtml(true)
            .build();

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AiAnalysisService aiAnalysisService;

    public MonthlyBudgetController(IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
                                   AiAnalysisService aiAnalysisService) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/planning")
    public String getPlanning(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String month,
            Model model) {
        LocalDate periodStart = from == null ? LocalDate.now().withDayOfYear(1) : from;
        LocalDate periodEnd = to == null ? LocalDate.now() : to;
        model.addAttribute("periodFrom", periodStart);
        model.addAttribute("periodTo", periodEnd);
        model.addAttribute("selectedMonth", month == null ? "" : month);
        addPeriodData(model, loadPeriod(periodStart, periodEnd));
        return "planning";
    }

    @PostMapping("/planning/ai")
    public String analyzePeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String month,
            Model model) {
        model.addAttribute("periodFrom", from);
        model.addAttribute("periodTo", to);
        model.addAttribute("selectedMonth", month == null ? "" : month);

        PeriodReport report = loadPeriod(from, to);
        addPeriodData(model, report);
        if (report.error() != null) {
            model.addAttribute("periodError", report.error());
        } else {
            try {
                List<MonthlySummary> summaries = report.months().stream()
                        .map(monthSummary -> new MonthlySummary(monthSummary.getMonth(),
                                monthSummary.getIncome(), monthSummary.getExpenses()))
                        .toList();
                MonthlySummary selectedMonthSummary = findSelectedMonth(report, month);
                String aiResult = aiAnalysisService.analyze(from, to, summaries, selectedMonthSummary,
                        report.totalIncome(), report.totalExpenses());
                model.addAttribute("aiResultHtml", markdownRenderer.render(markdownParser.parse(aiResult)));
            } catch (AiAnalysisException exception) {
                model.addAttribute("aiError", exception.getMessage());
            }
        }
        return "planning";
    }

    private PeriodReport loadPeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart.isAfter(periodEnd)) {
            return new PeriodReport(List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                    "Дата початку не може бути пізніше дати завершення.");
        }

        Map<YearMonth, BigDecimal> incomeByMonth = sumByMonth(
                incomeRepository.findByOperationDateBetween(periodStart, periodEnd));
        Map<YearMonth, BigDecimal> expensesByMonth = sumByMonth(
                expenseRepository.findByOperationDateBetween(periodStart, periodEnd));
        List<PeriodMonth> months = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        YearMonth firstMonth = YearMonth.from(periodStart);
        YearMonth lastMonth = YearMonth.from(periodEnd);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("uk"));

        for (YearMonth month = firstMonth; !month.isAfter(lastMonth); month = month.plusMonths(1)) {
            BigDecimal income = incomeByMonth.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal expenses = expensesByMonth.getOrDefault(month, BigDecimal.ZERO);
            months.add(new PeriodMonth(month.toString(), month.format(monthFormatter), income, expenses));
            totalIncome = totalIncome.add(income);
            totalExpenses = totalExpenses.add(expenses);
        }

        return new PeriodReport(months, months, totalIncome, totalExpenses, null);
    }

    private MonthlySummary findSelectedMonth(PeriodReport report, String selectedMonth) {
        if (selectedMonth == null || selectedMonth.isBlank()) {
            return null;
        }

        return report.availableMonths().stream()
                .filter(month -> month.getMonthValue().equals(selectedMonth))
                .filter(month -> month.getIncome().signum() != 0 || month.getExpenses().signum() != 0)
                .findFirst()
                .map(month -> new MonthlySummary(month.getMonth(), month.getIncome(), month.getExpenses()))
                .orElse(null);
    }

    private void addPeriodData(Model model, PeriodReport report) {
        model.addAttribute("monthOptions", report.availableMonths());
        model.addAttribute("actualMonths", report.months());
        model.addAttribute("actualPeriodIncome", report.totalIncome());
        model.addAttribute("actualPeriodExpenses", report.totalExpenses());
        if (report.error() != null) {
            model.addAttribute("periodError", report.error());
        }
    }

    private Map<YearMonth, BigDecimal> sumByMonth(List<? extends CategorizedAmount> amounts) {
        Map<YearMonth, BigDecimal> totals = new HashMap<>();
        for (CategorizedAmount amount : amounts) {
            YearMonth month = YearMonth.from(amount.getOperationDate());
            totals.merge(month, amount.getAmountMainCurrency(), BigDecimal::add);
        }
        return totals;
    }

    public static class PeriodMonth {
        private final String monthValue;
        private final String month;
        private final BigDecimal income;
        private final BigDecimal expenses;

        public PeriodMonth(String monthValue, String month, BigDecimal income, BigDecimal expenses) {
            this.monthValue = monthValue;
            this.month = month;
            this.income = income;
            this.expenses = expenses;
        }

        public String getMonthValue() {
            return monthValue;
        }

        public String getMonth() {
            return month;
        }

        public BigDecimal getIncome() {
            return income;
        }

        public BigDecimal getExpenses() {
            return expenses;
        }
    }

    private record PeriodReport(List<PeriodMonth> availableMonths, List<PeriodMonth> months, BigDecimal totalIncome,
                                BigDecimal totalExpenses, String error) {
    }
}
