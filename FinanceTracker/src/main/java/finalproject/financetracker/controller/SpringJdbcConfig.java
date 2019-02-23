package finalproject.financetracker.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@ComponentScan("finalproject.financetracker")
public class SpringJdbcConfig {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String BASE_URL = "jdbc:mysql://";
    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String DATABASE_NAME = "final_project";
    // "jdbc:mysql://127.0.0.1:3306/final_project"
    private static final String FULL_URL = BASE_URL + HOST + ":" + PORT + "/" + DATABASE_NAME;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Cql100gLeshnik+";
    @Bean
    public static DataSource mysqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER);
        dataSource.setUrl(FULL_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        return dataSource;
    }
}