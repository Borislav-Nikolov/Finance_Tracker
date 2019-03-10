package finalproject.financetracker.model.daos;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public abstract class AbstractDao {
    public static final int QUERY_RETURN_MAX_LIMIT = 1000;
    public static final int QUERY_RETURN_LIMIT_DEFAULT = 100;
    public static final int QUERY_RETURN_OFFSET_DEFAULT = 0;
    Logger logger = LogManager.getLogger(Logger.class);

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

    public enum SQLOrderBy {
        USER_ID,
        EXECUTION_DATE,
        AMOUNT,
        TRANSACTION_NAME,
        ACCOUNT_NAME,
        PT_NAME,
        NEXT_EXECUTION_DATE, CATEGORY_NAME, PT_AMOUNT;

        SQLOrderBy() {
        }
    }

    public enum SQLOrder {
        ASC, DESC
    }

    void closeStatement(Statement s) throws SQLException {
        if (s != null) {
            s.close();
        }
    }

    void closeResultSet(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }
}
