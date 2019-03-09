package finalproject.financetracker.utils.pdfWriter;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import lombok.AllArgsConstructor;



import java.io.ByteArrayOutputStream;

import java.util.List;

@AllArgsConstructor
public class MyPdfWriter {

    private User user;
    private List<ReturnAccountDTO> accounts;
    private List<ReturnTransactionDTO> transactions;
    private List<ReturnPlannedTransactionDTO> plannedTransactions;

    public byte[] create() throws DocumentException {
        ByteArrayOutputStream cachedContent = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, cachedContent);
        document.open();
        document.add(new Paragraph("Username: " + user.getUsername() +
                "\nFirst name: " +  user.getFirstName() +
                "\nLast name: " + user.getLastName() +
                "\nEmail: " + user.getEmail()));
        StringBuilder accountsText = new StringBuilder();
        for (ReturnTransactionDTO transactionDTO: transactions){

        }

        document.close();
        return cachedContent.toByteArray();
    }
}
