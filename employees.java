import java.sql.*;
import java.util.*;

public class employees {
    // Database URL
    public String url = "jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/dbsalesV2.5G205";
    // Database credentials
    public String username = "DBADM_205";
    public String password = "DLSU1234!";

    public int      employeeID;
    
    public String   lastName;
    public String   firstName;
    public String   extension;
    public String   email; 
    public String   jobTitle;
    public String   employee_type;
    public int      deptCode;
    public String   endUsername = null;
    public String   endUserReason = null;
    

    public employees(){}

    public int addEmployee()        {
    	Scanner sc = new Scanner(System.in);
    	
    	System.out.println("Enter Last Name:");
        lastName = sc.nextLine();

        System.out.println("Enter First Name:");
        firstName = sc.nextLine();

        System.out.println("Enter Extension:");
        extension = sc.nextLine();

        System.out.println("Enter Email:");
        email = sc.nextLine();

        System.out.println("Enter Job Title:");
        jobTitle = sc.nextLine();

        System.out.println("Enter Employee Type (Sales Representatives, Inventory Manager, Sales Manager):");
        employee_type = sc.nextLine();

        System.out.println("Enter Department Code (Enter 0 if not applicable):");
        deptCode = sc.nextInt();
        sc.nextLine(); // Consume newline
        
        try {
            Connection conn;
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql = "{CALL add_employee(?, ?, ?, ?, ?, ?, ?, ?, ?)}"; 
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, extension);
            pstmt.setString(4, email);
            pstmt.setString(5, jobTitle);
            pstmt.setString(6, employee_type);
            pstmt.setInt(7, deptCode);
            pstmt.setString(8, endUsername);
            pstmt.setString(9, endUserReason);
            
            System.out.println("Press Enter to Start Adding Employee");
            sc.nextLine();

            pstmt.executeUpdate();
            pstmt.close();
            conn.commit();
            conn.close();
            System.out.println("Employee was added!");
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
        System.out.println("Enter Type: \n  [0] - CREATE AN EMPLOYEE \n  [1] - RESIGN EMPLOYEE \n  [2] - CREATE AN EMPLOYEE \n  [3] - CREATE NEW SALES REP ASSIGNMENT OR SALES REP ONLY \n  [4] - VIEW ALL DETAILS OF SALES REP (PREVIOUS ASSIGN) \n  [5] - VIEW BASIC EMPLOYEE RECORD ");

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
        sc.close();
    }
}
