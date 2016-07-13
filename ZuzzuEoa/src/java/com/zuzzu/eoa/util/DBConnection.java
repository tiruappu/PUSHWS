/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.eoa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.xml.bind.JAXBContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author tirupathi
 */
@Component
public class DBConnection {
    
    private Connection zuzzuConnection;

    
    public Connection getZuzzuConnection() {
        if(zuzzuConnection==null){
            
            try{
                Class.forName("com.mysql.jdbc.Driver");
                zuzzuConnection = DriverManager.getConnection("jdbc:mysql://91.203.200.116:8080/zuzzu","accounting","accounting23!");
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("Zuzzu Connection");
            }
            
        }
        return zuzzuConnection;
        
    }
}
