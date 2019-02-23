package finalproject.financetracker.controller;


import finalproject.financetracker.model.Account;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpSession;
import java.sql.SQLException;



@RestController
public class AccountController {
    private final AccountDao dao;

     AccountController(AccountDao dao){
        this.dao = dao;
    }

    @PostMapping(value = "/profile/account/add")
    public Account addAcc(@RequestBody Account a, HttpSession session) throws SQLException, InvalidRequestDataException {

         //TODO to validate if account_name exists

        if (a == null ||
            a.getAccountName()==null||
            a.getAccountName().isEmpty()||
            a.getAmount()<= 0 ||
            a.getUserId()<=0||
            !UserController.checkIfLoggedIn(session)){

            throw new InvalidRequestDataException();
        }
        dao.addAcc(a);
        System.out.println(a);
        return a;
     }

    //TODO add more web services and create Exception Handlers

}
