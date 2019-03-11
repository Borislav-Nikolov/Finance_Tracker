package finalproject.financetracker.utils.pdfWriter;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.pojos.User;
import lombok.AllArgsConstructor;
import org.springframework.web.servlet.mvc.AbstractController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static finalproject.financetracker.controller.AbstractController.MILLIS_FOR_MONTH;

@AllArgsConstructor
public class MyPdfWriter {

    public static final int DAYS_IN_WEEK = 7;
    private User user;
    private List<ReturnAccountDTO> accounts;
    private List<ReturnTransactionDTO> transactions;
    private List<ReturnPlannedTransactionDTO> plannedTransactions;

    public byte[] create() {
        ByteArrayOutputStream cachedContent = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(cachedContent);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);
        document.add(new Paragraph("Finance Tracker").setBold().setFontSize(18));
        document.add(new Paragraph("Username: " + user.getUsername() +
                "\nFirst name: " + user.getFirstName() +
                "\nLast name: " + user.getLastName() +
                "\nEmail: " + user.getEmail()));

        // ------------Creating a tables---------------

        Table tableAccount = new Table(7);
        NumberFormat formatter = new DecimalFormat("#0.00");
        // Adding cells to the table
        if (accounts.size() > 0) {
            document.add(new Paragraph("\nAccounts").setBold().setFontSize(13));
            tableAccount.addCell(new Cell().add("account name").setBold());
            tableAccount.addCell(new Cell().add("balance").setBold());
            tableAccount.addCell(new Cell().add("total \ntransactions").setBold());
            tableAccount.addCell(new Cell().add("in/out \nlast \nweek").setBold());
            tableAccount.addCell(new Cell().add("in/out \nlast \nmonth").setBold());
            tableAccount.addCell(new Cell().add("in/out \nlast \nyear").setBold());
            tableAccount.addCell(new Cell().add("in/out \ntotal").setBold());


            for (ReturnAccountDTO account : accounts) {

                //----------------------- Account name -----------------------//
                tableAccount.addCell(new Cell().add(account.getAccountName()));

                //----------------------- Account amount -----------------------//
                tableAccount.addCell(new Cell().add(formatter.format(account.getAmount())));

                //----------------------- Total transactions -----------------------//
                tableAccount.addCell(new Cell().add(Integer.toString(transactions.stream()
                        .filter(returnTransactionDTO -> (returnTransactionDTO.getAccountId() == account.getAccountId()))
                        .mapToInt(returnTransactionDTO -> 1).reduce(0, (a1, a2) -> a1 + a2))));

                //----------------------- In/Out last week -----------------------//
                double incomeTransactionsLastWeek = transactions
                        .stream()
                        .filter(returnTransactionDTO -> returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                                .minusDays(DAYS_IN_WEEK))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .mapToDouble(ReturnTransactionDTO::getAmount)
                        .reduce(0, (a1, a2) -> a1 + a2);
                double expenseTransactionsLastWeek = transactions
                        .stream()
                        .filter(returnTransactionDTO -> !returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                                .minusWeeks(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .mapToDouble(ReturnTransactionDTO::getAmount)
                        .reduce(0, (a1, a2) -> a1 + a2);
                tableAccount.addCell(new Cell().add(formatter.format(incomeTransactionsLastWeek)
                        + " / "
                        + formatter.format(expenseTransactionsLastWeek)));

                //----------------------- In/Out last month -----------------------//
                double incomeTransactionsLastMonth = transactions
                        .stream()
                        .filter(returnTransactionDTO -> returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                                .minusMonths(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .mapToDouble(ReturnTransactionDTO::getAmount)
                        .reduce(0, (a1, a2) -> a1 + a2);
                double expenseTransactionsLastMonth = transactions
                        .stream()
                        .filter(returnTransactionDTO -> !returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                                .minusMonths(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .mapToDouble(ReturnTransactionDTO::getAmount)
                        .reduce(0, (a1, a2) -> a1 + a2);
                tableAccount.addCell(new Cell().add(formatter.format(incomeTransactionsLastMonth)
                        + " / "
                        + formatter.format(expenseTransactionsLastMonth)));

                //----------------------- In/Out last year -----------------------//
                double incomeTransactionsLastYear = transactions
                        .stream()
                        .filter(returnTransactionDTO -> returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                                .minusYears(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .mapToDouble(ReturnTransactionDTO::getAmount)
                        .reduce(0, (a1, a2) -> a1 + a2);
                double expenseTransactionsLastYear = transactions
                        .stream()
                        .filter(returnTransactionDTO -> !returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                                .minusYears(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .mapToDouble(ReturnTransactionDTO::getAmount)
                        .reduce(0, (a1, a2) -> a1 + a2);
                tableAccount.addCell(new Cell().add(formatter.format(incomeTransactionsLastYear)
                        + " / "
                        + formatter.format(expenseTransactionsLastYear)));

                //----------------------- In/Out total -----------------------//
                double incomeTransactionsTotal = transactions
                        .stream()
                        .filter(returnTransactionDTO -> returnTransactionDTO.getIsIncome()
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .mapToDouble(ReturnTransactionDTO::getAmount)
                        .reduce(0, (a1, a2) -> a1 + a2);
                double expenseTransactionsTotal = transactions
                        .stream()
                        .filter(returnTransactionDTO -> !returnTransactionDTO.getIsIncome()
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .mapToDouble(ReturnTransactionDTO::getAmount)
                        .reduce(0, (a1, a2) -> a1 + a2);
                tableAccount.addCell(new Cell().add(formatter.format(incomeTransactionsTotal)
                        + " / "
                        + formatter.format(expenseTransactionsTotal)));
            }
            tableAccount.addCell(new Cell().add("TOTAL transactions").setBold());
            tableAccount.addCell(new Cell().add(Integer.toString(transactions.size())).setBold());
        }
        // Adding Table to document
        document.add(tableAccount);

        Table tableTransactions = new Table(6);
        if (transactions.size() > 0) {
            document.add(new Paragraph("\nTransactions").setBold().setFontSize(13));
            tableTransactions.addCell(new Cell().add("transaction name").setBold());
            tableTransactions.addCell(new Cell().add("amount").setBold());
            tableTransactions.addCell(new Cell().add("execution date").setBold());
            tableTransactions.addCell(new Cell().add("category name").setBold());
            tableTransactions.addCell(new Cell().add("account name").setBold());
            tableTransactions.addCell(new Cell().add("income").setBold());

            for (ReturnTransactionDTO transactionDTO : transactions) {
                tableTransactions.addCell(new Cell().add(transactionDTO.getTransactionName()));
                tableTransactions.addCell(new Cell().add(formatter.format(transactionDTO.getAmount())));
                tableTransactions.addCell(new Cell().add(transactionDTO.getExecutionDate()
                        .format(DateTimeFormatter.ofPattern("dd.MM.YY HH:mm"))));
                tableTransactions.addCell(new Cell().add(transactionDTO.getCategoryName()));
                tableTransactions.addCell(new Cell().add(transactionDTO.getAccountName()));
                tableTransactions.addCell(new Cell().add(transactionDTO.getIsIncome().toString()));
            }
        }
        // Adding Table to document
        document.add(tableTransactions);

        Table tablePlannedTransactions = new Table(7);
        if (plannedTransactions.size() > 0) {
            document.add(new Paragraph("\nPlanned Transactions").setBold().setFontSize(13));
            tablePlannedTransactions.addCell(new Cell().add("transaction name").setBold());
            tablePlannedTransactions.addCell(new Cell().add("amount").setBold());
            tablePlannedTransactions.addCell(new Cell().add("next execution date").setBold());
            tablePlannedTransactions.addCell(new Cell().add("repeat period").setBold());
            tablePlannedTransactions.addCell(new Cell().add("category name").setBold());
            tablePlannedTransactions.addCell(new Cell().add("account name").setBold());
            tablePlannedTransactions.addCell(new Cell().add("income").setBold());

            for (ReturnPlannedTransactionDTO transactionDTO : plannedTransactions) {
                tablePlannedTransactions.addCell(new Cell().add(transactionDTO.getTransactionName()));
                tablePlannedTransactions.addCell(new Cell().add(formatter.format(transactionDTO.getAmount())));
                tablePlannedTransactions.addCell(new Cell().add(transactionDTO.getNextExecutionDate()
                        .format(DateTimeFormatter.ofPattern("dd.MM.YY HH:mm"))));
                String repeatPeriod;
                if (transactionDTO.getRepeatPeriod()% MILLIS_FOR_MONTH == 0) {
                    long months = transactionDTO.getRepeatPeriod()/ MILLIS_FOR_MONTH;
                    repeatPeriod = (months==1) ? "every month":"every "+months+" months";
                }else {
                    long days = (transactionDTO.getRepeatPeriod() / (1000 * 60 * 60 * 24));
                    long hours = ((transactionDTO.getRepeatPeriod() % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
                    long minutes = ((transactionDTO.getRepeatPeriod() % (1000 * 60 * 60 * 24)) %
                                                                                 (1000 * 60 * 60)) / (1000 * 60);
                    repeatPeriod = ((days == 0) ? "" : (days + " " + ((days > 1) ? "days" : "day"))) +
                            ((days > 0 && hours > 0) ? ",\n" : "") +
                            ((hours == 0) ? "" : (hours + " " + ((hours > 1) ? "hours" : "hour"))) +
                            ((hours > 0 && minutes > 0) ? ",\n" : "") +
                            ((minutes == 0) ? "" : (minutes + " " + ((minutes > 1) ? "minutes" : "minute")));
                };
                tablePlannedTransactions.addCell(repeatPeriod);
                tablePlannedTransactions.addCell(new Cell().add(transactionDTO.getCategoryName()));
                tablePlannedTransactions.addCell(new Cell().add(transactionDTO.getAccountName()));
                tablePlannedTransactions.addCell(new Cell().add(transactionDTO.getIsIncome().toString()));
            }
        }
        // Adding Table to document
        document.add(tablePlannedTransactions);
        document.close();
        return cachedContent.toByteArray();
    }
}
