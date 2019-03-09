package finalproject.financetracker.controller;

import com.itextpdf.text.DocumentException;
import finalproject.financetracker.exceptions.MyException;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.utils.pdfWriter.MyPdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.HttpHeadersReturnValueHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Scanner;

@RestController
@RequestMapping(produces = "application/PDF")
public class ExportController {
    @Autowired
    private UserController userController;
    @Autowired
    private AccountController accountController;
    @Autowired
    private TransactionController transactionController;
    @Autowired
    private PlannedTransactionController plannedTransactionController;

    @RequestMapping(value = "/export/pdf",produces = "application/PDF", method = RequestMethod.GET)
    public byte[] exportToPdf(HttpSession session, HttpServletRequest req, HttpServletResponse response) throws MyException, IOException, SQLException, DocumentException {
                return new MyPdfWriter(userController.getLoggedValidUserFromSession(session,req),
                        accountController.allAccOrdered(false,session,req),
                        transactionController.getAllWhere(null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          session,
                                                          req),
                        plannedTransactionController.getAllPlannedTransaction(null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          session,
                                                          req)
                        ).create();
    }
}
