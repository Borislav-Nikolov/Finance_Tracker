package finalproject.financetracker.utils.pdfWriter;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Cell;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.pojos.User;
import lombok.AllArgsConstructor;


import java.io.ByteArrayOutputStream;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
                "\nFirst name: " +  user.getFirstName() +
                "\nLast name: " + user.getLastName() +
                "\nEmail: " + user.getEmail()));
        StringBuilder accountsText = new StringBuilder();
        // Creating a table
        float [] pointColumnWidths = {150F, 150F,150F,150F,};
        Table tableAccount = new Table(7);
        NumberFormat formatter = new DecimalFormat("#0.00");
        // Adding cells to the table
        if (accounts.size()>0) {
            tableAccount.addCell(new Cell().add("Accounts").setBold());
            tableAccount.addCell(new Cell().add("balance").setBold());
            tableAccount.addCell(new Cell().add("total transactions").setBold());
            tableAccount.addCell(new Cell().add("in/out last week").setBold());
            tableAccount.addCell(new Cell().add("in/out last month").setBold());
            tableAccount.addCell(new Cell().add("in/out last year").setBold());
            tableAccount.addCell(new Cell().add("in/out total").setBold());

            for (ReturnAccountDTO account: accounts){

                //----------------------- Account name -----------------------//
                tableAccount.addCell(new Cell().add(account.getAccountName()));

                //----------------------- Account amount -----------------------//
                tableAccount.addCell(new Cell().add(formatter.format(account.getAmount())));

                //----------------------- Total transactions -----------------------//
                tableAccount.addCell(new Cell().add(Integer.toString(transactions.size())));

                //----------------------- In/Out last week -----------------------//
                int incomeTransactionsLastWeek = transactions
                        .stream()
                        .filter(returnTransactionDTO -> returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                        .minusDays(DAYS_IN_WEEK))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .collect(Collectors.toList())
                        .size();
                int expenseTransactionsLastWeek = transactions
                        .stream()
                        .filter(returnTransactionDTO -> !returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                         .isAfter(LocalDateTime.now()
                                         .minusWeeks(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .collect(Collectors.toList())
                        .size();
                tableAccount.addCell(new Cell().add(incomeTransactionsLastWeek+" / "+expenseTransactionsLastWeek));

                //----------------------- In/Out last month -----------------------//
                int incomeTransactionsLastMonth = transactions
                        .stream()
                        .filter(returnTransactionDTO -> returnTransactionDTO.getIsIncome()
                                        &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                        .minusMonths(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .collect(Collectors.toList())
                        .size();
                int expenseTransactionsLastMonth = transactions
                        .stream()
                        .filter(returnTransactionDTO -> !returnTransactionDTO.getIsIncome()
                                        &&
                                returnTransactionDTO.getExecutionDate()
                                                .isAfter(LocalDateTime.now()
                                                .minusMonths(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .collect(Collectors.toList())
                        .size();
                tableAccount.addCell(new Cell().add(incomeTransactionsLastMonth+" / "+expenseTransactionsLastMonth));

                //----------------------- In/Out last year -----------------------//
                int incomeTransactionsLastYear = transactions
                        .stream()
                        .filter(returnTransactionDTO -> returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                                .minusYears(1))
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .collect(Collectors.toList())
                        .size();
                int expenseTransactionsLastYear = transactions
                        .stream()
                        .filter(returnTransactionDTO -> !returnTransactionDTO.getIsIncome()
                                &&
                                returnTransactionDTO.getExecutionDate()
                                        .isAfter(LocalDateTime.now()
                                                .minusYears(1))
                        && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .collect(Collectors.toList())
                        .size();
                tableAccount.addCell(new Cell().add(incomeTransactionsLastYear+" / "+expenseTransactionsLastYear));

                //----------------------- In/Out total -----------------------//
                int incomeTransactionsTotal = transactions
                        .stream()
                        .filter(returnTransactionDTO -> returnTransactionDTO.getIsIncome()
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .collect(Collectors.toList())
                        .size();
                int expenseTransactionsTotal = transactions
                        .stream()
                        .filter(returnTransactionDTO -> !returnTransactionDTO.getIsIncome()
                                && returnTransactionDTO.getAccountId() == account.getAccountId())
                        .collect(Collectors.toList())
                        .size();
                tableAccount.addCell(new Cell().add(incomeTransactionsTotal+" / "+expenseTransactionsTotal));
            }
        }
        // Adding Table to document
        document.add(tableAccount);

        /*Table tableTransactions = new Table(pointColumnWidths);
        if (transactions.size()>0) {
            tableTransactions.addCell(new Cell().add("Transaction name").setBold());
            tableTransactions.addCell(new Cell().add("balance").setBold());
            for (ReturnAccountDTO account: accounts){
                tableTransactions.addCell(new Cell().add(account.getAccountName()));
                tableTransactions.addCell(new Cell().add(formatter.format(account.getAmount())));
            }
        }
        // Adding Table to document
        document.add(tableTransactions);

        Table tablePlannedTransactions = new Table(pointColumnWidths);
        if (plannedTransactions.size()>0) {
            tablePlannedTransactions.addCell(new Cell().add("Accounts").setBold());
            tablePlannedTransactions.addCell(new Cell().add("balance").setBold());
            for (ReturnPlannedTransactionDTO plannedTransactionDTO: plannedTransactions){
                tablePlannedTransactions.addCell(new Cell().add(plannedTransactionDTO.getAccountName()));
                tablePlannedTransactions.addCell(new Cell().add(formatter.format(plannedTransactionDTO.getAmount())));
            }
        }*/


        document.close();
        return cachedContent.toByteArray();
    }
}
