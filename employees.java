import java.sql.*;
import java.util.*;
import java.time.LocalDate;

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
            Scanner sc = new Scanner(System.in);
            int employeeID;
            int deptCode = 0;

            System.out.println("Enter Employee ID:");
            employeeID = Integer.parseInt(sc.nextLine());

            System.out.println("Enter Type: \n  [1] - Reclassify to Inventory Manager \n  [2] - Reclassify to Sales Manager \n  [3] - Reclassify to Sales Representative \n");

            int tempChoice = sc.nextInt();
            sc.nextLine();

            try {
                Connection conn = DriverManager.getConnection(url, username, password);
                System.out.println("Connection Successful");
                conn.setAutoCommit(false);

                String sql = null;
                PreparedStatement pstmt = null;

                switch (tempChoice) {
                    case 1:
                        System.out.println("Enter Department Code:");
                        deptCode = Integer.parseInt(sc.nextLine());

                        sql = "CALL employeeTypeToInventoryManagers (?, ?)";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, employeeID);
                        pstmt.setInt(2, deptCode);
                        break;
                    case 2:
                        System.out.println("Enter Department Code:");
                        deptCode = Integer.parseInt(sc.nextLine());

                        sql = "CALL employeeTypeToSalesManager (?, ?)";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, employeeID);
                        pstmt.setInt(2, deptCode);
                        break;
                    case 3:
                        sql = "CALL employeeTypeToSalesRepresentative (?)";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, employeeID);
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        return 0;
                }

                if (pstmt != null) {
                    System.out.println("Press Enter to Start Reclassifying Employee");
                    sc.nextLine();

                    pstmt.execute();

                    conn.commit();
                    pstmt.close();
                    conn.close();

                    System.out.println("Employee reclassified successfully.");
                    return 1;
                } else {
                    System.out.println("Fail to reclassify Employee");
                    return 0;
                }
            } catch (SQLException e) {
                System.out.println("SQL Error: " + e.getMessage());
                return 0;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return 0;
            } finally {
                sc.close();
            }
        }

    public int resignEmployee()     {
        return 0;
    }

    public int createSalesRepAssign()     {
        Scanner sc = new Scanner(System.in);
        int employeeID;
        int officeCode;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate;
        String reason;
        int quota;
        int salesManagerNumber;
        String end_username = "DBADMIN205@S17";
        String end_userreason = "Test";

        System.out.println("Enter Employee ID:");
        employeeID = Integer.parseInt(sc.nextLine());

        System.out.println("Enter Office Code:");
        officeCode = Integer.parseInt(sc.nextLine());

        System.out.println("Enter End Date Assignment [YYYY-MM-DD]:");
        endDate = LocalDate.parse(sc.nextLine());

        System.out.println("Enter reason:");
        reason = sc.nextLine();

        System.out.println("Enter quota amount:");
        quota = Integer.parseInt(sc.nextLine());

        System.out.println("Enter Sales Manager Number:");
        salesManagerNumber = Integer.parseInt(sc.nextLine());

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);

            String checkSql = "SELECT COUNT(*) FROM employees WHERE employeeNumber = ? FOR UPDATE";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, employeeID);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                String sql = "INSERT INTO salesRepAssignments (employeeNumber, officeCode, startDate, endDate, reason, quota, salesManagerNumber, end_username, end_userreason) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt1 = conn.prepareStatement(sql);
                pstmt1.setInt(1, employeeID);
                pstmt1.setInt(2, officeCode);
                pstmt1.setDate(3, java.sql.Date.valueOf(startDate));
                pstmt1.setDate(4, java.sql.Date.valueOf(endDate));
                pstmt1.setString(5, reason);
                pstmt1.setInt(6, quota);
                pstmt1.setInt(7, salesManagerNumber);
                pstmt1.setString(8, end_username);
                pstmt1.setString(9, end_userreason);

                pstmt1.executeUpdate();

                String insertSalesRepresentativesSql = "INSERT INTO salesRepresentatives (employeeNumber, end_username, end_userreason) "
                        + "VALUES (?, ?, ?)";
                PreparedStatement pstmt2 = conn.prepareStatement(insertSalesRepresentativesSql);
                pstmt2.setInt(1, employeeID);
                pstmt2.setString(2, end_username);
                pstmt2.setString(3, end_userreason);

                pstmt2.executeUpdate();

                pstmt1.close();
                pstmt2.close();
                conn.commit();
                conn.close();

                System.out.println("SalesRepAssignment Inserted Successfully");
                return 1;
            } else {
                System.out.println("Employee does not exist.");
                conn.rollback();
                return 0;
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            try {
                Connection conn = DriverManager.getConnection(url, username, password);
                conn.rollback(); // Rollback in case of SQL error
            } catch (SQLException rollbackEx) {
                System.out.println("Rollback Error: " + rollbackEx.getMessage());
            }
            return 0;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return 0;
        } finally {
            sc.close();
        }
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
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Employee ID:");
        employeeID = Integer.parseInt(sc.nextLine());

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql = "SELECT * FROM employees WHERE employeeNumber = ? LOCK IN SHARE MODE";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, employeeID);

            System.out.println("Press Enter to Start Viewing the Employee");
            sc.nextLine();

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                employeeID = rs.getInt("employeeNumber");
                lastName = rs.getString("lastName");
                firstName = rs.getString("firstName");
                extension = rs.getString("extension");
                email = rs.getString("email");
                jobTitle = rs.getString("jobTitle");
                employee_type = rs.getString("employee_type");
                is_deactivated = rs.getInt("is_deactivated");

                System.out.println("Employee ID: " + employeeID);
                System.out.println("Last Name: " + lastName);
                System.out.println("First Name: " + firstName);
                System.out.println("Extension: " + extension);
                System.out.println("Email: " + email);
                System.out.println("jobTitle: " + jobTitle);
                System.out.println("employee_type: " + employee_type);
                System.out.println("is_deactivated: " + is_deactivated);
            } else {
                System.out.println("Employee does not exist.");
            }

            rs.close();
            pstmt.close();
            conn.commit();
            conn.close();

            return 1;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }

    }

    public static void main(String[] args) {
        Scanner sc     = new Scanner (System.in);
        int     choice;
        // Letting the user choose between the functions
        // kenneth 0, 2, 4 
        // laiven 1, 3, 5
        System.out.println("Enter Type: \n  [0] - CREATE AN EMPLOYEE \n  [1] - RECLASSIFY EMPLOYEE \n  [2] - CREATE AN EMPLOYEE \n  [3] - CREATE NEW SALES REP ASSIGNMENT OR SALES REP ONLY \n  [4] - VIEW ALL DETAILS OF SALES REP (PREVIOUS ASSIGN) \n  [5] - VIEW BASIC EMPLOYEE RECORD ");

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
