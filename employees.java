import java.sql.*;
import java.util.*;

public class employees {

    public String   productCode;
    public String   productName;
    public String   productLine;
    public int      quantityInStock;
    public float    buyPrice;
    public float    MSRP;

    public employees(){}

    public int addEmployee()        {
        // Database URL
        String url = "jdbc:mysql://localhost:3306/your_database";
        // Database credentials
        String username = "your_username";
        String password = "your_password";

        // Connection and CallableStatement objects
        Connection conn = null;
        CallableStatement callableStmt = null;

        try {
            // Step 1: Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Establish the connection to the database
            conn = DriverManager.getConnection(url, username, password);

            // Step 3: Prepare the CallableStatement
            String sql = "{CALL your_stored_procedure(?, ?)}"; // Adjust according to your stored procedure
            callableStmt = conn.prepareCall(sql);

            // Step 4: Set input parameters if any
            callableStmt.setInt(1, 123); // Example parameter
            callableStmt.setString(2, "example"); // Example parameter

            // Step 5: Execute the stored procedure
            boolean hasResults = callableStmt.execute();

            // Step 6: Process the result set if there are any results
            if (hasResults) {
                ResultSet rs = callableStmt.getResultSet();
                while (rs.next()) {
                    // Retrieve data from the result set
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    System.out.println("ID: " + id + ", Name: " + name);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return 0;
        } 

    }

    public int reclassifyEmployee()     {
        return 0;
    }

    public int resignEmployee()     {
        return 0;
    }

    public int createSalesRepAssign()     {
        return 0;
    }

    public int viewSalesRepDetails()     {
        return 0;
    }
    
    public int viewEmployee()     {
        return 0;
    }

    public static void main(String[] args) {
        Scanner sc     = new Scanner (System.in);
        int     choice = 0;
        // Letting the user choose between the functions
        // kenneth 0, 2, 4 
        // laiven 1, 3, 5
        System.out.println("Enter 0 - CREATE AN EMPLOYEE /n 1 - RECLASSIFY EMPLOYEE TYPE /n 2 - RESIGN EMPLOYEE /n 3 - CREATE NEW SALES REP ASSIGNMENT OR SALES REP ONLY /n 4 - VIEW ALL DETAILS OF SALES REP (PREVIOUS ASSIGN) /n 5 - VIEW BASIC EMPLOYEE RECORD ");
        choice = sc.nextInt();
        employees e = new employees();
        if (choice==0) e.addEmployee(); 
        if (choice==1) e.reclassifyEmployee();
        if (choice==2) e.resignEmployee();
        if (choice==3) e.createSalesRepAssign();
        if (choice==4) e.viewSalesRepDetails();
        if (choice==5) e.viewEmployee();
        
        System.out.println("Press enter key to continue....");
        sc.nextLine();
    }
}
