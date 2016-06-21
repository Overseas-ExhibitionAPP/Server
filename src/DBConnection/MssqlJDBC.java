/*
 *  File name : MssqlJDBC.java
 *  Author : Nai-Jia Chen
 *  Since : 2016/06/08
 *  
 *  Finished:
 *  1.Microsoft SQL Server Connection (forschool)
 *  2.Microsoft SQL Server Connection (Overseas)
 */
package DBConnection;

import java.sql.*;

public class MssqlJDBC {
    String connectionUrl_forschool = "jdbc:sqlserver://your_database_ip:1433;"
            + "databaseName= forschool;"
            + "user= your_db_user;"
            + "password= your_password;";
    String connectionUrl_oversea = "jdbc:sqlserver://your_database_ip:1433;"
            + "databaseName=oversea"
            + "user= your_db_user;"
            + "password= your_password;";
    public Connection con = null;
    public Statement stmt = null;
    public ResultSet rs = null;
    public void connectionServer(String type) throws Exception {
        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //分別有forschool與overseas兩個資料庫的連線設定
            if(type.equals("forschool")) {
                con = DriverManager.getConnection(connectionUrl_forschool);
            } else {
                con = DriverManager.getConnection(connectionUrl_oversea);
            }
            stmt = con.createStatement();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void executeQueryCommand(String sql) throws Exception{
        try{
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() throws Exception{
        try{
            if(rs != null) {
                rs.close();
            }
            if(stmt != null) {
                stmt.close();
            }
            if(con != null) {
                con.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
