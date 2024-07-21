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
    public int      is_deactivated;
    
    public String officeCode;
    public String startDate;
    public String endDate;
    public String reason;
    public double quota;
    public int salesManagerNumber;
    
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
    	Scanner sc = new Scanner(System.in);
    	
        System.out.println("Enter Employee Number To Resign:");
        employeeID = sc.nextInt();
        sc.nextLine(); // Consume newline
        
        try {
            Connection conn;
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql = "{CALL deactivateEmployee(?, ?, ?)}"; 
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, employeeID);
            pstmt.setString(2, endUsername);
            pstmt.setString(3, endUserReason);
            
            System.out.println("Press Enter to Resign Employee");
            sc.nextLine();

            pstmt.executeUpdate();
            pstmt.close();
            conn.commit();
            conn.close();
            System.out.println("Employee has Resigned!");
            return 1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        } 
    }

    public int createSalesRepAssign()     {
        return 0;
    }

    public int viewSalesRepDetails()     {
    	Scanner sc = new Scanner(System.in);

        System.out.println("Enter Employee ID:");
        employeeID = Integer.parseInt(sc.nextLine());

        System.out.println("Enter Range: \n [0] - Current \n [1] - Previous \n [2] - All Assignments");
        int range = Integer.parseInt(sc.nextLine());
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String baseSql = "SELECT \r\n" + 
                    "    sra.officeCode, \r\n" + 
                    "    sra.startDate, \r\n" + 
                    "    sra.endDate, \r\n" + 
                    "    sra.reason, \r\n" + 
                    "    sra.quota, \r\n" + 
                    "    sra.salesManagerNumber,\r\n" + 
                    "    e.firstName AS salesManagerFirstName,\r\n" + 
                    "    e.lastName AS salesManagerLastName\r\n" + 
                    "FROM \r\n" + 
                    "    salesRepAssignments sra\r\n" + 
                    "LEFT JOIN \r\n" + 
                    "    employees e \r\n" + 
                    "ON \r\n" + 
                    "    e.employeeNumber = sra.salesManagerNumber\r\n" + 
                    "WHERE \r\n" + 
                    "    sra.employeeNumber = ?\r\n";

            String condition = "";
            switch (range) {
                case 0:
                    condition = "AND (sra.endDate IS NULL OR sra.endDate > CURDATE()) AND sra.startDate \r\n";
                    break;
                case 1:
                    condition = "AND sra.endDate <= CURDATE() \r\n";
                    break;
                case 2:
                    // No additional condition needed for case 2
                    break;
            }
        
            String sql = baseSql + condition + "LOCK IN SHARE MODE;";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, employeeID);

            System.out.println("Press Enter to Start Viewing Sales Rep Assignment");
            sc.nextLine();

            rs = pstmt.executeQuery();
            boolean assignmentsFound = false;
            while (rs.next()) {
            	assignmentsFound = true;
            	officeCode     		= rs.getString("officeCode");
            	startDate     		= rs.getString("startDate");
            	endDate 			= rs.getString("endDate");
            	reason        		= rs.getString("reason");
            	quota            	= rs.getDouble("quota");
            	salesManagerNumber	= rs.getInt("salesManagerNumber");
            	String salesManagerName	= rs.getString("salesManagerFirstName") + ' ' + rs.getString("salesManagerLastName");
            	
            	System.out.println("Office Code: " + officeCode);
                System.out.println("Start Date: " + startDate);
                System.out.println("End Date: " + endDate);
                System.out.println("Reason: " + reason);
                System.out.println("Quota: " + quota);
                System.out.println("Sales Manager Number: " + salesManagerNumber);
                System.out.println("Sales Manager Name: " + salesManagerName);
                System.out.println("-----------------------------------");
            }

            if (!assignmentsFound) {
                System.out.println("No record found for employee ID: " + employeeID);
            }

            rs.close();
            pstmt.close();
            conn.commit();
            conn.close();

            return 1;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    public int viewEmployee()     {
        return 0;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        employees e = new employees();

        do {
            // Displaying the menu options to the user
            System.out.println("Enter Type: \n" +
                    "  [0] - CREATE AN EMPLOYEE \n" +
                    "  [1] - RECLASSIFY EMPLOYEE \n" +
                    "  [2] - RESIGN EMPLOYEE \n" +
                    "  [3] - CREATE NEW SALES REP ASSIGNMENT OR SALES REP ONLY \n" +
                    "  [4] - VIEW ALL DETAILS OF SALES REP (PREVIOUS ASSIGN) \n" +
                    "  [5] - VIEW BASIC EMPLOYEE RECORD \n" +
                    "  [9] - EXIT");

            choice = sc.nextInt();
            sc.nextLine(); // Consume the newline character

            // Handling user choices
            switch (choice) {
                case 0:
                    e.addEmployee();
                    break;
                case 1:
                    e.reclassifyEmployee();
                    break;
                case 2:
                    e.resignEmployee();
                    break;
                case 3:
                    e.createSalesRepAssign();
                    break;
                case 4:
                    e.viewSalesRepDetails();
                    break;
                case 5:
                    e.viewEmployee();
                    break;
                case 9:
                    System.out.println("Exiting the program...");
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }

            if (choice != 9) {
                System.out.println("Press enter key to continue....");
                sc.nextLine();
            }

        } while (choice != 9);

        sc.close();
    }
}
