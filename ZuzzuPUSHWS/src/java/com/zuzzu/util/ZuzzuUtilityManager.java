/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.util;

import static comm.ZuzzuSqlQueries.insert_query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used for logging into the log table
 * @author sowmya
 */
@Component
public class ZuzzuUtilityManager {
    
      @Autowired
    DataSource zuzzudb;

    PreparedStatement statement;

    Connection con;
    
    Logger logger=Logger.getLogger(ZuzzuUtilityManager.class);
    
     public void insertIntoZuzzuPushLog(String zuzzuitemId, String ebayItemId, String request, String response,int type, int status, long perfTime) {
        java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
         logger.info("This is log classs");
         if(ebayItemId.equals(""))
             ebayItemId="0";
             
         
        try {
            con=zuzzudb.getConnection();
            statement = con.prepareStatement(insert_query);
            statement.setString(1, zuzzuitemId);
            statement.setString(2, ebayItemId);
            statement.setString(3, request);
            statement.setString(4, response);
            statement.setInt(5, type);
            statement.setInt(6, status);
            statement.setLong(7, perfTime);
            int records=statement.executeUpdate();
            
            logger.info("inserted"+records);
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.error(ex);
            }
    }

}
