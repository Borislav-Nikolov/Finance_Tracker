package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.MyException;
import finalproject.financetracker.model.daos.AbstractDao;
import finalproject.financetracker.model.daos.PlannedTransactionDao;
import finalproject.financetracker.model.daos.TransactionDao;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.utils.pdfWriter.MyPdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@RestController
@RequestMapping(produces = "application/PDF")
public class ExportController {
    @Autowired
    private UserController userController;
    @Autowired
    private AccountController accountController;
    @Autowired
    private TransactionDao transactionDao;
    @Autowired
    private PlannedTransactionDao plannedTransactionDao;

    @RequestMapping(value = "/export/pdf",produces = "application/PDF", method = RequestMethod.GET)
    public byte[] exportToPdf(HttpSession session, HttpServletRequest req, HttpServletResponse response) throws MyException, IOException, SQLException {
        User u = userController.getLoggedValidUserFromSession(session,req);
        return new MyPdfWriter(u,
                        accountController.allAccOrdered(false,session,req),
                        transactionDao.getAllByAccIdStartDateEndDateIsIncome(
                                                           u.getUserId(),
                                                          null,
                                                          null,
                                                          0L,
                                                          null,
                                                          null,
                                                          AbstractDao.SQLOrderBy.EXECUTION_DATE,
                                                          AbstractDao.SQLOrder.DESC,
                                                          null,
                                                          null),
                        plannedTransactionDao.getAllByAccIdIsIncomeOrder(u.getUserId(),
                                                          null,
                                                          null,
                                                          null,
                                                          AbstractDao.SQLOrderBy.NEXT_EXECUTION_DATE,
                                                          AbstractDao.SQLOrder.ASC)
                        ).create();
    }
}
