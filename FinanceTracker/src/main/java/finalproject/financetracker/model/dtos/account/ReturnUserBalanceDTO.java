package finalproject.financetracker.model.dtos.account;


import finalproject.financetracker.model.pojos.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnUserBalanceDTO {
    private long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private double balance;

    public ReturnUserBalanceDTO(User u){
        this.userId = u.getUserId();
        this.username = u.getUsername();
        this.firstName = u. getFirstName();
        this.lastName = u.getLastName();
        this.email = u.getEmail();
    }

    public ReturnUserBalanceDTO withBalance(double balance){
        this.balance = balance;
        return this;
    }

}
