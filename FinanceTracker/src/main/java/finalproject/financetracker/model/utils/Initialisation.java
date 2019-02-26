package finalproject.financetracker.model.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import finalproject.financetracker.model.daos.CategoryDao;
import finalproject.financetracker.model.daos.ImageDao;
import finalproject.financetracker.model.daos.UserDao;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Initialisation implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ImageDao imageDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private UserDao userDao;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            createSchemaIfNotExists();
            userDao.insertDefaultUserIfNotExists();
            if (imageDao.getImageById(1) == null) {
                imageDao.addAllIcons();
            }
            if (categoryDao.getCategoryById(1) == null) {
                categoryDao.addAllPredefinedCategories();
            }
        } catch (SQLException | IOException ex) {
            System.out.println("Couldn't initialize DB: " + ex.getMessage());
        }
    }
    private void createSchemaIfNotExists() throws SQLException, IOException {
        StringBuilder oneQuerySB = new StringBuilder();
        List<StringBuilder> allQueriesSB = new ArrayList<>();
        File file = new File("schemaSQL.txt");
        if (file.exists()) {
            System.out.println("SQL schema query file - found!");
            try (FileInputStream fis = new FileInputStream(file)) {
                int symbol = fis.read();
                while (symbol != -1) {
                    char currentChar = (char) symbol;
                    oneQuerySB.append(currentChar);
                    if (currentChar == ';') {
                        allQueriesSB.add(oneQuerySB);
                        oneQuerySB = new StringBuilder();
                    }
                    symbol = fis.read();
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error creating DB schema! File " + file.getName() + " read error! " + e.getMessage());
                return;
            }
            for (StringBuilder sql : allQueriesSB) {
                if (sql.toString().contains("USE")) {
                    continue;
                }
                String sqlString = sql.toString();
                jdbcTemplate.execute(sqlString);
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
