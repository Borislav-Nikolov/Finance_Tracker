package finalproject.financetracker.model.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public abstract class AbstractDao {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected enum SQLCompareOperator {

        SMALLER_OR_EQUAL("<="), EQUALS("="), BIGGER_OR_EQUAL(">=");

        private String value;

        SQLCompareOperator(String value) {
            this.value = value;
        }

        protected String getValue() {
            return this.value;
        }
    }

    protected enum SQLColumnName {
        ACCOUNT_ID, ACCOUNT_NAME, AMOUNT, USER_ID
    }

    protected enum SQLOderBy {
        ASC, DESC
    }

    protected void closeStatement(Statement s) throws SQLException {
        if (s!=null){
            s.close();
        }
    }

    protected void closeResultSet(ResultSet rs) throws SQLException{
        if (rs!= null){
            rs.close();
        }
    }
}
