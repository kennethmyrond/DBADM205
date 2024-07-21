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
        String sql = null;

        // \n [1] - All Current Sales Representatives \n [2] - All Previous Sales Representatives
        System.out.println("Enter Type of Record to View: \n [0] - All Sales Representatives record  \n [1] - Record of Specific Sales Rep");
        int recordType = Integer.parseInt(sc.nextLine());

        if(recordType == 0 ){
            System.out.println("Enter Range: \n [0] - Current Sales Representatives \n [1] - Previous Sales Representatives \n [2] - All Records");
            int rangeSales = Integer.parseInt(sc.nextLine());

            String baseSql =    "SELECT sra.employeeNumber, \r\n" + 
                                "    sra.officeCode, \r\n" + 
                                "    em.firstName, em.lastName, \r\n" + 
                                "    sra.startDate, \r\n" + 
                                "    sra.endDate, \r\n" + 
                                "    sra.reason, \r\n" + 
                                "    sra.quota, \r\n" + 
                                "    sra.salesManagerNumber,\r\n" + 
                                "    e.firstName AS salesManagerFirstName,\r\n" + 
                                "    e.lastName AS salesManagerLastName\r\n" + 
                                "FROM salesRepAssignments sra\r\n" + 
                                "JOIN employees e ON e.employeeNumber = sra.salesManagerNumber\r\n" + 
                                "JOIN employees em ON em.employeeNumber = sra.employeeNumber    \r\n";

            String condition = "";
            switch (rangeSales) {
                case 0:
                    condition = "WHERE (sra.endDate IS NULL OR sra.endDate > CURDATE()) \r\n";
                    break;
                case 1:
                    condition = "WHERE sra.endDate <= CURDATE() \r\n";
                    break;
                case 2:
                    // No additional condition needed for case 2
                    break;
            }
            sql = baseSql + condition + "LOCK IN SHARE MODE;";
        }
        else if(recordType == 1){
            System.out.println("Enter Employee ID:");
            employeeID = Integer.parseInt(sc.nextLine());

            System.out.println("Enter Range: \n [0] - Current \n [1] - Previous \n [2] - All Assignments");
            int range = Integer.parseInt(sc.nextLine());

            String baseSql =    "SELECT \r\n" + 
                                "    sra.officeCode, \r\n" + 
                                "    sra.startDate, \r\n" + 
                                "    sra.endDate, \r\n" + 
                                "    sra.reason, \r\n" + 
                                "    sra.quota, \r\n" + 
                                "    sra.salesManagerNumber,\r\n" + 
                                "    e.firstName AS salesManagerFirstName,\r\n" + 
                                "    e.lastName AS salesManagerLastName\r\n" + 
                                "FROM salesRepAssignments sra\r\n" + 
                                "JOIN employees e ON e.employeeNumber = sra.salesManagerNumber\r\n" + 
                                "WHERE sra.employeeNumber = ?\r\n";

            String condition = "";
            switch (range) {
                case 0:
                    condition = "AND (sra.endDate IS NULL OR sra.endDate > CURDATE()) \r\n";
                    break;
                case 1:
                    condition = "AND sra.endDate <= CURDATE() \r\n";
                    break;
                case 2:
                    // No additional condition needed for case 2
                    break;
            }
            sql = baseSql + condition + "LOCK IN SHARE MODE;";
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql);
            if(recordType==1) pstmt.setInt(1, employeeID);

            System.out.println("Press Enter to Start Viewing Sales Rep Assignment");
            sc.nextLine();

            rs = pstmt.executeQuery();
            boolean assignmentsFound = false;
            
            while (rs.next()) {
            	assignmentsFound = true;
                String SalesRepName = null;
                if(recordType==0){
                    employeeID      = rs.getInt("employeeNumber");
                    SalesRepName = rs.getString("firstName") + ' ' + rs.getString("lastName");
                }
            	officeCode     		= rs.getString("officeCode");
            	startDate     		= rs.getString("startDate");
            	endDate 			= rs.getString("endDate");
            	reason        		= rs.getString("reason");
            	quota            	= rs.getDouble("quota");
            	salesManagerNumber	= rs.getInt("salesManagerNumber");
            	String salesManagerName	= rs.getString("salesManagerFirstName") + ' ' + rs.getString("salesManagerLastName");
            	
                if(recordType==0){
                    System.out.println("Employee Number: " + employeeID);
                    System.out.println("Employee Name: " + SalesRepName);
                }
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
                System.out.println("No records found");
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

    public void modifyEmployee() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Employee ID:");
        int employeeID = Integer.parseInt(sc.nextLine());

        Connection conn = null;
        PreparedStatement fetchPstmt = null;
        PreparedStatement updatePstmt = null;
        PreparedStatement jobTitlesPstmt = null;
        ResultSet rs = null;
        ResultSet jobTitlesRs = null;


        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String fetchSql = "SELECT extension, email, jobTitle FROM employees WHERE employeeNumber = ? FOR UPDATE";
            fetchPstmt = conn.prepareStatement(fetchSql);
            fetchPstmt.setInt(1, employeeID);

            System.out.println("Press enter key to start retrieving the data");
            sc.nextLine();

            rs = fetchPstmt.executeQuery();
            if (rs.next()) {
                String currentExtension = rs.getString("extension");
                String currentEmail = rs.getString("email");
                String currentJobTitle = rs.getString("jobTitle");

                System.out.println("Current Details:");
                System.out.println("Extension: " + currentExtension);
                System.out.println("Email: " + currentEmail);
                System.out.println("Job Title: " + currentJobTitle);
            } else {
                System.out.println("No employee found with the given ID.");
                return;
            }
            rs.close();
            fetchPstmt.close();

            int empDetail;
            // do {
                // Displaying the menu options to the user
                System.out.println("\nSelect Employee Detail To Edit: \n" +
                        "  [1] - Extension \n" +
                        "  [2] - Email \n" +
                        "  [3] - Job Title \n" +
                        "  [9] - EXIT");

                empDetail = sc.nextInt();
                sc.nextLine(); // Consume the newline character

                String updateSql = null;
                switch (empDetail) {
                    case 1:
                        System.out.println("Enter New Extension:");
                        String newExtension = sc.nextLine();

                        updateSql = "UPDATE employees SET extension = ? WHERE employeeNumber = ?";
                        updatePstmt = conn.prepareStatement(updateSql);
                        updatePstmt.setString(1, newExtension);
                        updatePstmt.setInt(2, employeeID);
                        break;
                    case 2:
                        System.out.println("Enter New Email:");
                        String newEmail = sc.nextLine();

                        updateSql = "UPDATE employees SET email = ? WHERE employeeNumber = ?";
                        updatePstmt = conn.prepareStatement(updateSql);
                        updatePstmt.setString(1, newEmail);
                        updatePstmt.setInt(2, employeeID);
                        break;
                    case 3:
                        System.out.println("Available Job Titles:");
                        String jobTitlesSql = "SELECT jobTitle FROM employees_jobTitles LOCK IN SHARE MODE;";
                        jobTitlesPstmt = conn.prepareStatement(jobTitlesSql);
                        jobTitlesRs = jobTitlesPstmt.executeQuery();
                        
                        while (jobTitlesRs.next()) {
                            System.out.println("- " + jobTitlesRs.getString("jobTitle"));
                        }
                        
                        jobTitlesRs.close();
                        jobTitlesPstmt.close();

                        System.out.println("\nEnter New Job Title:");
                        String newJobTitle = sc.nextLine();

                        updateSql = "UPDATE employees SET jobTitle  = ? WHERE employeeNumber = ?";
                        updatePstmt = conn.prepareStatement(updateSql);
                        updatePstmt.setString(1, newJobTitle);
                        updatePstmt.setInt(2, employeeID);
                        break;
                    case 9:
                        System.out.println("Exiting...");
                        //continue;
                    default:
                        System.out.println("Invalid choice, please try again.");
                        //continue;
                }

                int rowsUpdated = updatePstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Employee details updated successfully.");
                } else {
                    System.out.println("No employee found with the given ID.");
                }
                updatePstmt.close();
            // } while (empDetail != 9);

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error updating employee details: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back.");
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (fetchPstmt != null) fetchPstmt.close();
                if (updatePstmt != null) updatePstmt.close();
                if (jobTitlesRs != null) jobTitlesRs.close();
                if (jobTitlesPstmt != null) jobTitlesPstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        employees e = new employees();

        do {
            // Displaying the menu options to the user
            System.out.println( "Enter Type: \n" +
                                "  [0] - CREATE AN EMPLOYEE \n" +
                                "  [1] - RECLASSIFY EMPLOYEE \n" +
                                "  [2] - RESIGN EMPLOYEE \n" +
                                "  [3] - CREATE NEW SALES REP ASSIGNMENT FOR SALES REP ONLY \n" +
                                "  [4] - VIEW SALES REPRESENTATIVE DETAILS \n" +
                                "  [5] - VIEW BASIC EMPLOYEE RECORD \n" +
                                "  [6] - MODIFY EMPLOYEE RECORD \n" +
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
                case 6:
                    e.modifyEmployee();
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
