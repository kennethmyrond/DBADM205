import java.sql.*;
import java.util.*;

public class employees {

    public int      employeeID;

    // public String   productName;
    // public String   productLine;
    // public int      quantityInStock;
    // public float    buyPrice;
    // public float    MSRP;

    public employees(){}

    public int addEmployee()        {
        // Database URL
        String url = "jdbc:mysql://localhost:3306/your_database";
        // Database credentials
        String username = "your_username";
        String password = "your_password";

        try {
            Connection conn;
            CallableStatement callableStmt;
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql = "{CALL add_employee(?, ?, ?, ?, ?, ?, ?, ?, ?)}"; 
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
            return 1;
        } catch (Exception e) {
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
        float   incr;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Sales Representative ID:");
        employeeID = sc.nextInt();

        try {
            Connection conn; 
            conn = DriverManager.getConnection("jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/dbsalesV2.5G205?useTimezone=true&serverTimezone=UTC&user=DBADM_205&password=DLSU1234!");
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);
            
            // PreparedStatement pstmt = conn.prepareStatement("SELECT productName, productLine, quantityInStock, buyPrice, MSRP FROM products WHERE productCode=? LOCK IN SHARE MODE");
            // pstmt.setString(1, productCode);
            
            // System.out.println("Press enter key to start retrieving the data");
            // sc.nextLine();
            
            // ResultSet rs = pstmt.executeQuery();   
            
            // while (rs.next()) {
            //     productName     = rs.getString("productName");
            //     productLine     = rs.getString("productLine");
            //     quantityInStock = rs.getInt("quantityInStock");
            //     buyPrice        = rs.getFloat("buyPrice");
            //     MSRP            = rs.getFloat("MSRP");
            // }
            
            // rs.close();
            
            // System.out.println("Product Name: " + productName);
            // System.out.println("Product Line: " + productLine);
            // System.out.println("Quantity:     " + quantityInStock);
            // System.out.println("Buy Price:    " + buyPrice);
            // System.out.println("MSRP:         " + MSRP);
            
            // System.out.println("Press enter key to end transaction");
            // sc.nextLine();

            // pstmt.close();
            // conn.commit();
            // conn.close();
            return 1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    public int viewEmployee()     {
        return 0;
    }

    public static void main(String[] args) {
        Scanner sc     = new Scanner (System.in);
        int     choice;
        // Letting the user choose between the functions
        // kenneth 0, 2, 4 
        // laiven 1, 3, 5
        // System.out.println("Enter Type of Task");
        // System.out.println("Enter Type: [0] - CREATE AN EMPLOYEE");
        // System.out.println("Enter Type: [1] - RECLASSIFY EMPLOYEE TYPE");
        // System.out.println("Enter Type: [2] - CREATE AN EMPLOYEE");
        // System.out.println("Enter Type: [3] - CREATE AN EMPLOYEE");
        // System.out.println("Enter Type: [4] - CREATE AN EMPLOYEE");
        // System.out.println("Enter Type: [5] - CREATE AN EMPLOYEE");

        System.out.println("Enter Type: \n  [0] - CREATE AN EMPLOYEE \n  [1] - RESIGN EMPLOYEE \n  [2] - CREATE AN EMPLOYEE \n  [3] - CREATE NEW SALES REP ASSIGNMENT OR SALES REP ONLY \n  [4] - VIEW ALL DETAILS OF SALES REP (PREVIOUS ASSIGN) \n  [5] - VIEW BASIC EMPLOYEE RECORD ");
        //"1 -  /n 2 -  /n "
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
