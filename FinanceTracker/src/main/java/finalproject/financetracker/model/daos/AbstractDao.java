package finalproject.financetracker.model.daos;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public abstract class AbstractDao {

    protected Logger logger = LogManager.getLogger(Logger.class);

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    public enum SQLCompareOperator {

        SMALLER_OR_EQUAL("<="), EQUALS("="), BIGGER_OR_EQUAL(">=");

        private String value;

        SQLCompareOperator(String value) {
            this.value = value;
        }

        protected String getValue() {
            return this.value;
        }
    }

    public enum SQLColumnName {
        ACCOUNT_ID, ACCOUNT_NAME, AMOUNT, USER_ID
    }

    public enum SQLOderBy {
        ASC, DESC
    }

    public void closeStatement(Statement s) throws SQLException {
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
