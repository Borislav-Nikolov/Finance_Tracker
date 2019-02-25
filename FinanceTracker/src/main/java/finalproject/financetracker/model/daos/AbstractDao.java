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

    public void closeStatement(Statement s) throws SQLException {
        if (s!=null){
            s.close();
        }
    }

    public void closeResultSet(ResultSet rs) throws SQLException{
        if (rs!= null){
            rs.close();
        }
    }
}
