package finalproject.financetracker.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

@Configuration
@ComponentScan("finalproject.financetracker")
public class SpringJdbcConfig {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String BASE_URL = "jdbc:mysql://";
    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String DATABASE_NAME = "hr?allowMultiQueries=true&useUnicode=yes&characterEncoding=UTF-8";
    private static final String FULL_URL = BASE_URL + HOST + ":" + PORT + "/" + DATABASE_NAME;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234";

//    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
//    private static final String BASE_URL = "jdbc:mysql://";
//    private static final String HOST = "127.0.0.1";
//    private static final String PORT = "3306";
//    private static final String DATABASE_NAME = "final_project";
//    // "jdbc:mysql://127.0.0.1:3306/final_project"
//    private static final String FULL_URL = BASE_URL + HOST + ":" + PORT + "/" + DATABASE_NAME;
//    private static final String USERNAME = "root";
//    private static final String PASSWORD = "Cql100gLeshnik+";

    private volatile static Object obj;

    SpringJdbcConfig(){
            SpringJdbcConfig.obj = new Object();
        try {
            createSchemaIfNotExists();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Bean
    public static DataSource mysqlDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER);
        dataSource.setUrl(FULL_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        if (SpringJdbcConfig.obj == null){
                new SpringJdbcConfig();
        }
        return dataSource;
    }

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


    public static void createSchemaIfNotExists() throws SQLException{
        StringBuilder sql = new StringBuilder();
        File file = new File("schemaSQL.txt");

        if (file.exists()) {
            System.out.println("SQL schema query file - found!");
            Scanner sc = null;
            try {
                sc = new Scanner(file);

            } catch (FileNotFoundException e) {
                System.out.println("Error creating DB schema! File " + file.getName() + " read error! " + e.getMessage());
                return;
            }

            while (sc.hasNextLine()) {
                sql.append(sc.nextLine());
            }
            String sqlString = sql.toString();
            PreparedStatement ps =SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sqlString);
            ps.executeUpdate();
            if (ps!=null){
                ps.close();
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("File with schema create query not found!");
        }
    }

}
