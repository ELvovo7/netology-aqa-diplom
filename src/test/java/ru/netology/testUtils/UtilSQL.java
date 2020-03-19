package ru.netology.testUtils;

import lombok.Getter;
import java.sql.*;

public class UtilSQL {

    private static String dbUrl = System.getProperty("db.url"); //для запуска из консоли
    private static String dbUser = System.getProperty("db.user");
    private static String dbPass = System.getProperty("db.pass");

    //private static final String dbUrl = "jdbc:postgresql://127.0.0.1:5432/app"; //для запуска из Idea
    //private static final String dbUrl = "jdbc:mysql://127.0.0.1:3306/app"; //для запуска из Idea
    //private static final String dbUser = "app";
    //private static final String dbPass = "pass";

    @Getter private static final String payTable = "payment_entity";
    @Getter private static final String creditTable = "credit_request_entity";

    public static void cleanTables() throws SQLException {
        String cleanCreditTables = "DELETE FROM credit_request_entity;";
        String cleanOrderTable = "DELETE FROM order_entity;";
        String cleanPaymentTable = "DELETE FROM payment_entity;";
        try (Connection conDataBase = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            Statement baseStatement = conDataBase.createStatement();
            baseStatement.executeUpdate(cleanCreditTables);
            baseStatement.executeUpdate(cleanOrderTable);
            baseStatement.executeUpdate(cleanPaymentTable);
        }
    }

    public static String getOperationStatus(String table) {
        String status = "";
        try (Connection c = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             Statement s = c.createStatement()) {
            try (ResultSet resultSet = s.executeQuery("SELECT * FROM " + table + " ORDER BY id DESC LIMIT 1;")) {
                while (resultSet.next()) status = resultSet.getString("status");
            }
        } catch (SQLException e) {
            e.getErrorCode();
        }
        return status;
    }
}