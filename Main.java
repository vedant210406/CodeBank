import java.sql.*;
import java.util.Scanner;

public class Main {
    static final String DB_URL = "jdbc:mysql://localhost:3306/CodeBank";
static final String DB_USER = "root";
static final String DB_PASS = "1234";


    static Scanner scanner = new Scanner(System.in);
    static String currentUser = null;

    public static void main(String[] args) {
        int choice;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            do {
                System.out.println("\n--- Welcome to Java Bank ---");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        register(conn);
                        break;
                    case 2:
                        if (login(conn)) {
                            dashboard(conn);
                        }
                        break;
                    case 3:
                        System.out.println("Thank you! Visit again.");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } while (choice != 3);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void register(Connection conn) throws SQLException {
        System.out.print("Enter new username: ");
        String username = scanner.next();
        System.out.print("Enter new password: ");
        String password = scanner.next();

        String checkQuery = "SELECT username FROM users WHERE username = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, username);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            System.out.println("Username already exists.");
        } else {
            String insertQuery = "INSERT INTO users (username, password, balance) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setDouble(3, 0.0);
            insertStmt.executeUpdate();
            System.out.println("Registered successfully!");
        }
    }

    private static boolean login(Connection conn) throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();

        String loginQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(loginQuery);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            currentUser = username;
            System.out.println("Login successful. Welcome, " + currentUser + "!");
            return true;
        } else {
            System.out.println("Invalid credentials.");
            return false;
        }
    }

    private static void dashboard(Connection conn) throws SQLException {
        int choice;
        do {
            System.out.println("\n--- Bank Dashboard ---");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    checkBalance(conn);
                    break;
                case 2:
                    deposit(conn);
                    break;
                case 3:
                    withdraw(conn);
                    break;
                case 4:
                    System.out.println("Logged out.");
                    currentUser = null;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (choice != 4);
    }

    private static void checkBalance(Connection conn) throws SQLException {
        String query = "SELECT balance FROM users WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, currentUser);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            double balance = rs.getDouble("balance");
            System.out.printf("Current Balance: ₹%.2f\n", balance);
        }
    }

    private static void deposit(Connection conn) throws SQLException {
        System.out.print("Enter amount to deposit: ₹");
        double amount = scanner.nextDouble();

        if (amount > 0) {
            String update = "UPDATE users SET balance = balance + ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(update);
            stmt.setDouble(1, amount);
            stmt.setString(2, currentUser);
            stmt.executeUpdate();
            System.out.println("Amount deposited successfully.");
        } else {
            System.out.println("Invalid amount.");
        }
    }

    private static void withdraw(Connection conn) throws SQLException {
        System.out.print("Enter amount to withdraw: ₹");
        double amount = scanner.nextDouble();

        String query = "SELECT balance FROM users WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, currentUser);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            double currentBalance = rs.getDouble("balance");
            if (amount > 0 && amount <= currentBalance) {
                String update = "UPDATE users SET balance = balance - ? WHERE username = ?";
                PreparedStatement updateStmt = conn.prepareStatement(update);
                updateStmt.setDouble(1, amount);
                updateStmt.setString(2, currentUser);
                updateStmt.executeUpdate();
                System.out.println("Amount withdrawn successfully.");
            } else if (amount > currentBalance) {
                System.out.println("Insufficient balance.");
            } else {
                System.out.println("Invalid amount.");
            }
        }
    }
}
