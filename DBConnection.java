package event; // Unga folder name 'event' nura nala intha line mukkiyam

import java.sql.*;

public class DBConnection {
    // 1. Database name 'event' nu irukanum
    // 2. XAMPP default user 'root', password empty ""
    private static String url = "jdbc:mysql://localhost:3306/event"; 
    private static String user = "root"; 
    private static String pass = "dbms"; 

    public static Connection getConnection() {
        Connection con = null;
        try {
            // New MySQL driver (Connector/J 8.0+)
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);
            System.out.println("Database Connected Successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver Not Found! (Library add aagala): " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Connection Error! (XAMPP-la MySQL Start pannunga): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Other Error: " + e.getMessage());
        }
        return con;
    }
}