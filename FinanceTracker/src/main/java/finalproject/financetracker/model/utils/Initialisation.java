package finalproject.financetracker.model.utils;

import finalproject.financetracker.controller.SpringJdbcConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Initialisation {
    public static void createSchemaIfNotExists() throws SQLException {
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
            PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sqlString);
            ps.executeUpdate();
            if (ps != null) {
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
