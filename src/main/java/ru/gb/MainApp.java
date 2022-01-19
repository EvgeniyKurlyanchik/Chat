package ru.gb;

import java.sql.*;

public class MainApp {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;


    public static void main(String[] args) {
        try {
            connect();
            clearTable();
//            prepareAllStatements();
            exRollback();
            System.out.println(" Connect.......");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public static void exRollback() throws SQLException {
        stmt.executeUpdate("INSERT INTO students (name, score) VALUES ('Bob1', 75);");

        Savepoint sp1 = connection.setSavepoint();
        stmt.executeUpdate("INSERT INTO students (name, score) VALUES ('Bob2', 55);");
        connection.rollback(sp1);

        stmt.executeUpdate("INSERT INTO students (name, score) VALUES ('Bob3', 80);");
        connection.setAutoCommit(true);
    }

    public static void batchFillTable() throws SQLException {
        long begin = System.currentTimeMillis();
        connection.setAutoCommit(false);
        for (int i = 0; i < 1000; i++) {
            psInsert.setString(1, "Bob" + i);
            psInsert.setInt(2, i * 15 % 100);
            psInsert.addBatch();
        }
        psInsert.executeBatch();
        connection.commit();
        long end = System.currentTimeMillis();
        System.out.printf("Time: %d ms\n", end - begin);
    }

    public static void fillTable() throws SQLException {
        long begin = System.currentTimeMillis();
        connection.setAutoCommit(false);
        for (int i = 0; i < 1000; i++) {
            psInsert.setString(1, "Bob" + i);
            psInsert.setInt(2, i * 15 % 100);
            psInsert.executeUpdate();
        }
        connection.commit();
        long end = System.currentTimeMillis();
        System.out.printf("Time: %d ms\n", end - begin);
    }

    public static void prepareAllStatements() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO students (name, score) VALUES ( ? , ? );");
    }

    //CRUD create read update delete
    public static void exSelect() throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT name, score FROM students WHERE score >= 45 AND score <=75;");

        while (rs.next()) {
            System.out.println(rs.getString("name") + " " + rs.getInt("score"));
        }

        rs.close();
    }

    public static void clearTable() throws SQLException {
        stmt.executeUpdate("DELETE FROM students;");
    }

    public static void exDelete() throws SQLException {
        stmt.executeUpdate("DELETE FROM students WHERE score = 50;");
    }

    public static void exUpdate() throws SQLException {
        stmt.executeUpdate("UPDATE students SET score = 50 WHERE score < 50;");
        stmt.executeUpdate("UPDATE students SET score = 50 WHERE score >= 70 and score <=80;");
    }

    public static void exInsert() throws SQLException {
        stmt.executeUpdate("INSERT INTO students (name, score) VALUES ('Bob4', 75);");
        stmt.executeUpdate("INSERT INTO students (name, score) " +
                "VALUES ('Bob6', 85),('Bob7', 15),('Bob8', 45),('Bob9', 25);");
    }

    public static void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    public static void disconnect() {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
