import java.sql.*;
import java.time.*;
import java.util.Scanner;

public class products {
    // Database URL
    public String url = "jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/DBSALES26_G205";

    public String username = "DBADM_205";
    public String password = "DLSU1234!";

    public String productCode;
    public String productName;
    public String productScale;
    public String productVendor;
    public String productDescription;
    public Double buyPrice;
    public char product_type;
    public int product_quantityInStock;
    public Double MSRP;
    public String productLine;
    public String end_userreason;

    public int newproducts() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Product Code: ");
        productCode = sc.nextLine();

        System.out.println("Enter Product Name: ");
        productName = sc.nextLine();

        System.out.println("Enter Product Scale: ");
        productScale = sc.nextLine();

        System.out.println("Enter Product Vendor name: ");
        productVendor = sc.nextLine();

        System.out.println("Enter Product Description: ");
        productDescription = sc.nextLine();

        System.out.println("Enter Buy Price: ");
        buyPrice = sc.nextDouble();
        sc.nextLine();

        System.out.println("Enter Product Type: ");
        product_type = Character.toUpperCase(sc.next().charAt(0));
        sc.nextLine();

        System.out.println("Enter the quantity of the product: ");
        product_quantityInStock = sc.nextInt();
        sc.nextLine();

        System.out.println("Enter the MSRP: ");
        MSRP = sc.nextDouble();
        sc.nextLine();

        System.out.println("Enter Product Line: ");
        productLine = sc.nextLine();

        System.out.println("Enter Reason for adding: ");
        end_userreason = sc.nextLine();

        Connection conn = null;
        CallableStatement cstmt = null;

        try{
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);

            String sql = "{CALL add_product(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            cstmt = conn.prepareCall(sql);

            cstmt.setString(1, productCode);
            cstmt.setString(2, productName);
            cstmt.setString(3, productScale);
            cstmt.setString(4, productVendor);
            cstmt.setString(5, productDescription);
            cstmt.setDouble(6, buyPrice);
            cstmt.setString(7, String.valueOf(product_type));
            cstmt.setInt(8, product_quantityInStock);
            cstmt.setDouble(9, MSRP);
            cstmt.setString(10, productLine);
            cstmt.setString(11, username);
            cstmt.setString(12, end_userreason);
            
            System.out.println("Press Enter to Start Adding Product");
            sc.nextLine();

            cstmt.executeUpdate();
            System.out.println("Product was added!");

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            try {
                if (cstmt != null) cstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            sc.close();
        }
    }


    public int updateproduct(){
        Scanner sc = new Scanner(System.in);
        int choice;

        System.out.println("Type: \n [0] - Update an Existing Product Entry \n [1] - Discontinue a Product \n [2] - Reintroduce a Product \n");
        choice = sc.nextInt();
        sc.nextLine();

        if(choice == 0) {
            System.out.println("Enter Product Code: ");
            productCode = sc.nextLine();
    
            System.out.println("Enter Product Name: ");
            productName = sc.nextLine();
    
            System.out.println("Enter Product Scale: ");
            productScale = sc.nextLine();
    
            System.out.println("Enter Product Vendor name: ");
            productVendor = sc.nextLine();
    
            System.out.println("Enter Product Description: ");
            productDescription = sc.nextLine();
    
            System.out.println("Enter Buy Price: ");
            buyPrice = sc.nextDouble();
            sc.nextLine();
    
            System.out.println("Enter the quantity of the product: ");
            product_quantityInStock = sc.nextInt();
            sc.nextLine();
    
            System.out.println("Enter the MSRP: ");
            MSRP = sc.nextDouble();
            sc.nextLine();
    
            System.out.println("Enter Reason for updating product: ");
            end_userreason = sc.nextLine();

            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                conn = DriverManager.getConnection(url, username, password);
                System.out.println("Connection successful");
                conn.setAutoCommit(false);

                // Lock products
                String lockQuery = "SELECT * FROM  products WHERE productCode = ? FOR UPDATE";
                pstmt = conn.prepareStatement(lockQuery);
                pstmt.setString(1, productCode);
                rs = pstmt.executeQuery();

                if(!rs.next()) {
                    System.out.println("Product not found");
                    return 0;
                }
                // Lock current_products
                String LockCurrentProductsQuery = "SELECT * FROM current_products WHERE productCode = ? FOR UPDATE";
                pstmt = conn.prepareStatement(LockCurrentProductsQuery);
                pstmt.setString(1, productCode);
                pstmt.executeQuery();

                System.out.println("Press enter key to update");
                sc.nextLine();

                String updateProductSQL = "UPDATE products set productName=?, productScale=?, productVendor=?, productDescription=?, buyPrice=?, end_username=?, end_userreason=? WHERE productCode = ?";
                pstmt = conn.prepareStatement(updateProductSQL);
                pstmt.setString(1, productName);
                pstmt.setString(2, productScale);
                pstmt.setString(3, productVendor);
                pstmt.setString(4, productDescription);
                pstmt.setDouble(5, buyPrice);
                pstmt.setString(6, username);
                pstmt.setString(7, end_userreason);
                pstmt.setString(8, productCode);
                pstmt.executeUpdate();
                
                String updateCurrentProductsSQL = "UPDATE current_products SET quantityInStock = ?, end_username = ?, end_userreason = ? WHERE productCode = ?";
                pstmt = conn.prepareStatement(updateCurrentProductsSQL);
                pstmt.setInt(1, product_quantityInStock);
                pstmt.setString(2, username);
                pstmt.setString(3, end_userreason);
                pstmt.setString(4, productCode);
                pstmt.executeUpdate();

                // Update product_pricing or product_wholesale based on product_type
                String productTypeQuery = "SELECT product_type FROM current_products WHERE productCode = ?";
                pstmt = conn.prepareStatement(productTypeQuery);
                pstmt.setString(1, productCode);
                rs = pstmt.executeQuery();

                // Adding sleep to capture conflicts
                pstmt = conn.prepareStatement("SELECT SLEEP (5)");
                pstmt.executeQuery();                

                if(rs.next()) {
                    char currentType = rs.getString("product_type").charAt(0);

                    if(currentType == 'R') {
                        // Getting current date and end date for product_pricing table
                        LocalDateTime now = LocalDateTime.now();
                        Timestamp startdate = Timestamp.valueOf(now);
                        Timestamp enddate = Timestamp.valueOf(now.plusDays(7));

                        // Lock product_pricing table
                        String lockSQL = "SELECT * FROM product_pricing WHERE productCode = ? FOR UPDATE";
                        pstmt = conn.prepareStatement(lockSQL);
                        pstmt.setString(1, productCode);
                        rs = pstmt.executeQuery();

                        // Query the maximum enddate for the given productCode
                        String maxEndDateQuery = "SELECT MAX(enddate) AS max_enddate FROM product_pricing WHERE productCode = ?";
                        pstmt = conn.prepareStatement(maxEndDateQuery);
                        pstmt.setString(1, productCode);
                        rs = pstmt.executeQuery();

                        Date maxEndDate = null;

                        if (rs.next()) {
                            maxEndDate = rs.getDate("max_enddate");
                        }
                        // Compare startdate with maxEndDate
                        if (maxEndDate != null && startdate.compareTo(maxEndDate) <= 0) {
                            throw new Exception("Too early to update record.");
                        } else {
                            String updatePricingSQL = "INSERT INTO product_pricing (productCode, startdate, enddate, MSRP, end_username, end_userreason) VALUES (?, ?, ?, ?, ?, ?)";
                            pstmt = conn.prepareStatement(updatePricingSQL);
                            pstmt.setString(1, productCode);
                            pstmt.setTimestamp(2, startdate);
                            pstmt.setTimestamp(3, enddate);
                            pstmt.setDouble(4, MSRP);
                            pstmt.setString(5, username);
                            pstmt.setString(6, end_userreason);
                            
                            pstmt.executeUpdate();
                        }
                    } // End of if (product_type == 'R')
                    else if(currentType == 'W') {
                        // Lock product_wholesale
                        String lockSQL = "SELECT * FROM product_wholesale WHERE productCode = ? FOR UPDATE";
                        pstmt = conn.prepareStatement(lockSQL);
                        pstmt.setString(1, productCode);
                        rs = pstmt.executeQuery();                        

                        String updateWholesaleSQL = "UPDATE product_wholesale SET MSRP = ?, end_username = ?, end_userreason = ? WHERE productCode = ?";
                        pstmt = conn.prepareStatement(updateWholesaleSQL);
                        pstmt.setDouble(1, MSRP);
                        pstmt.setString(2, username);
                        pstmt.setString(3, end_userreason);
                        pstmt.setString(4, productCode);
                        pstmt.executeUpdate();
                    } // End of if (product_type == 'W')
                } // End of if(rs.next())
                System.out.println("Product updated succesfully for Product Code: " + productCode);
                conn.commit();
                return 1;
            } catch (Exception e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException se) {
                        se.printStackTrace();
                    }
                }
                e.printStackTrace();
                return 0;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
                sc.close();
            }
        } // End of if(choice == 0)
        else if(choice == 1) {
            int inventoryManagerId;
            String reason;

            System.out.println("Enter a Product Code: ");
            productCode = sc.nextLine();

            System.out.println("Enter Inventory Manager ID: ");
            inventoryManagerId = sc.nextInt();
            sc.nextLine();

            System.out.println("Enter reason for discontinuing: ");
            reason = sc.nextLine();
            
            Connection connection = null;
            CallableStatement cstmt = null;

            try {
                connection = DriverManager.getConnection(url, username, password);

                String sql = "{CALL discontinue_product(?, ?, ?, ?, ?)}";

                cstmt = connection.prepareCall(sql);

                cstmt.setString(1, productCode);
                cstmt.setInt(2, inventoryManagerId);
                cstmt.setString(3, reason);
                cstmt.setString(4, username);
                cstmt.setString(5, reason);
                cstmt.executeUpdate();
                System.out.println("Product code: " + productCode + " has been discontinued.");
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            } finally {
                try {
                if (cstmt != null) cstmt.close();
                if (connection != null) connection.close();
                } catch (Exception se) {
                    se.printStackTrace();
                }
                sc.close();
            }
        } // End of else if (choice == 1)
        else if(choice == 2) {
            System.out.println("Enter a Product Code: ");
            productCode = sc.nextLine();

            System.out.println("Enter a reason for reinstating: ");
            end_userreason = sc.nextLine();
            
            Connection connection = null;
            CallableStatement cstmt = null;

            try {
                connection = DriverManager.getConnection(url, username, password);

                String sql = "{CALL reintroduce_product(?, ?, ?)}";

                cstmt = connection.prepareCall(sql);

                cstmt.setString(1, productCode);
                cstmt.setString(2, username);
                cstmt.setString(3, end_userreason);
                cstmt.executeUpdate();
                System.out.println("Product code: " + productCode + " has been reinstated.");

            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            } finally {
                try {
                if (cstmt != null) cstmt.close();
                if (connection != null) connection.close();
                } catch (Exception se) {
                    se.printStackTrace();
                }
                sc.close();
            }
        }
        return 1;
    }

    public int classifyproductline(){
        Scanner sc = new Scanner(System.in);
        int choice;

        System.out.println("Type: \n [0] - Classify a Product to a Productline \n [1] - Remove a productline from a product \n [2] - Add a Productline \n");
        choice = sc.nextInt();
        sc.nextLine();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        if (choice == 0) {
            System.out.println("Enter Product Code: ");
            productCode = sc.nextLine();

            System.out.println("Enter a Productline to be added: ");
            productLine = sc.nextLine();

            System.out.println("Enter reason");
            end_userreason = sc.nextLine();
            
            try {
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);

            // Lock product_productlines table
            String lockSQL = "SELECT * FROM product_productlines WHERE productCode = ? FOR UPDATE";
            pstmt = conn.prepareStatement(lockSQL);
            pstmt.setString(1, productCode);
            rs = pstmt.executeQuery();

            System.out.println("Press Enter to Start Adding a Productline to an existing product.");
            sc.nextLine();
            
            pstmt = conn.prepareStatement("SELECT SLEEP (5)");
            pstmt.executeQuery();

            String sql = "INSERT INTO product_productlines (productCode, productLine, end_username, end_userreason) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, productCode);
            pstmt.setString(2, productLine);
            pstmt.setString(3, username);
            pstmt.setString(4, end_userreason);
            
            pstmt.executeUpdate();
            conn.commit();
            System.out.println("Productline was added to productCode: " + productCode);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (SQLException se) {
                    se.printStackTrace();
                }
                return 0;
            } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            sc.close();
            }
        } // End of if(choice == 0)
        else if(choice == 1) {
            System.out.println("Enter Product Code: ");
            productCode = sc.nextLine();

            System.out.println("Enter Productline to be removed: ");
            productLine = sc.nextLine();

            try {
                conn = DriverManager.getConnection(url, username, password);
                conn.setAutoCommit(false);
    
                // Lock product_productlines table
                String lockSQL = "SELECT * FROM product_productlines WHERE productCode = ? FOR UPDATE";
                pstmt = conn.prepareStatement(lockSQL);
                pstmt.setString(1, productCode);
                rs = pstmt.executeQuery();
    
                String sql = "DELETE FROM product_productlines WHERE productCode = ? AND productLine = ?";
                pstmt = conn.prepareStatement(sql);
    
                pstmt.setString(1, productCode);
                pstmt.setString(2, productLine);
                
                System.out.println("Press Enter to remove a Productline from an existing product.");
                sc.nextLine();

                pstmt = conn.prepareStatement("SELECT SLEEP (5)");
                pstmt.executeQuery();

                pstmt.executeUpdate();
                conn.commit();
                System.out.println("Productline was added to productCode: " + productCode);                
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (SQLException se) {
                    se.printStackTrace();
                }
                return 0;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
                sc.close();
            }
        } // End of else if(choice == 1)
        else if(choice == 2) {
            System.out.println("Type a Productline: ");
            productLine = sc.nextLine();

            System.out.println("Type in a description: ");
            String description = sc.nextLine();

            System.out.println("Enter a HTML: ");
            String html = sc.nextLine();

            System.out.println("Enter a reason for adding Productline: ");
            end_userreason = sc.nextLine();

            try {
                conn = DriverManager.getConnection(url, username, password);
                conn.setAutoCommit(false);
                
                // Lock productlines table
                String lockSQL = "SELECT * FROM productlines FOR UPDATE";
                pstmt = conn.prepareStatement(lockSQL);
                rs = pstmt.executeQuery();

                System.out.println("Press Enter to add a Productline.");
                sc.nextLine();

                pstmt = conn.prepareStatement("SELECT SLEEP (5)");
                pstmt.executeQuery();                

                String sql = "INSERT INTO productlines (productLine, textDescription, htmlDescription, end_username, end_userreason) VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, productLine);
                pstmt.setString(2, description);
                pstmt.setString(3, html);
                pstmt.setString(4, username);
                pstmt.setString(5, end_userreason);

                pstmt.executeUpdate();
                conn.commit();

                System.out.println("Productline added successfully.");
            } catch (Exception e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                }
                e.printStackTrace();
                return 0;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
                sc.close();
            }
        } // End of else if (choice == 2)
        else {
            System.out.println("Invalid Input");
        }
        return 1;
    }

    public int viewproduct(){
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter a Product Code: ");
        productCode = sc.nextLine();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);

            String sql = "SELECT p.productCode, p.productName, p.productScale, p.productVendor, p.productDescription, p.buyPrice, p.product_category, cp.product_type, cp.quantityInStock, getMSRP(?) as MSRP, getPrice(?, 'MIN') as Minimum_Price, getPrice(?, 'MAX') as Maximum_Price, GROUP_CONCAT(pp.productLine SEPARATOR ', ') AS productLines, p.end_username, p.end_userreason " +
            "FROM products p " +
            "JOIN current_products cp ON p.productCode = cp.productCode " +
            "JOIN product_productlines pp ON p.productCode = pp.productCode " +
            "WHERE p.productCode = ? " +
            "GROUP BY p.productCode, p.productName, p.productScale, p.productVendor, p.productDescription, p.buyPrice, p.product_category, cp.product_type, p.end_username, p.end_userreason " +
            "FOR SHARE";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);
            pstmt.setString(2, productCode);
            pstmt.setString(3, productCode);
            pstmt.setString(4, productCode);

            System.out.println("Press Enter to Start Viewing the Product");
            sc.nextLine();

            rs = pstmt.executeQuery();

            pstmt = conn.prepareStatement("SELECT SLEEP (5)");
            pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Product Code: " + rs.getString("productCode"));
                System.out.println("Product Name: " + rs.getString("productName"));
                System.out.println("Product Scale: " + rs.getString("productScale"));
                System.out.println("Product Vendor: " + rs.getString("productVendor"));
                System.out.println("Product Description: " + rs.getString("productDescription"));
                System.out.println("Buy Price: " + rs.getDouble("buyPrice"));
                System.out.println("Product Category: " + rs.getString("product_category"));
                System.out.println("Product Type: " + rs.getString("product_type"));
                System.out.println("Quantity: " + rs.getString("quantityInStock"));
                System.out.println("MSRP: " + rs.getString("MSRP"));                
                System.out.println("Minimum Price: " + rs.getDouble("Minimum_Price"));
                System.out.println("Maximum Price: " + rs.getDouble("Maximum_Price"));
                System.out.println("Product Lines: " + rs.getString("productLines"));
                System.out.println("End User Name: " + rs.getString("end_username"));
                System.out.println("End User Reason: " + rs.getString("end_userreason"));
                System.out.println("----------------------------------------");
            }
            System.out.println("Press enter key to end....");
            sc.nextLine();
            conn.commit();           
            return 1;
        } catch (Exception e){
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            sc.close();
        }
    }

    public static void main (String args[]) {
        Scanner sc = new Scanner(System.in);
        products p = new products();
        int choice;

        System.out.println("Enter Type: \n  [0] - CREATE A NEW PRODUCT \n  [1] - UPDATE A PRODUCT \n  [2] - CLASSIFY A PRODUCT INTO A PRODUCTLINE \n  [3] - VIEW A PRODUCT \n");
        
        choice = sc.nextInt();
        
        if(choice == 0)
            p.newproducts();
        else if(choice == 1)
            p.updateproduct();
        else if(choice == 2)
            p.classifyproductline();
        else if(choice == 3)
            p.viewproduct();
        else
            System.out.println("Invalid Input");
        sc.close();
    }
}