/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.eoa.dao;

import com.zuzzu.eoa.util.DBConnection;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author tirupathi
 */

@Component
public class ZuzzuEoaDao {
    
    /* @Autowired
    public DBConnection dbconnection;
    */ 
    
    @Autowired
    DataSource dataSource,dataSource1;
    
   
    
    public boolean zuzzuCheck(String zuzzuid){
        
        
        boolean zuzzuCheck=false;
        String zuzzuItemCheckQuery="select itemid from zuzzu.offer where itemid like '"+zuzzuid+"'";
        
        try{
            Connection zuzzuConnection=dataSource.getConnection();
            Statement stmt =zuzzuConnection.createStatement();
                ResultSet result = stmt.executeQuery(zuzzuItemCheckQuery);
            while(result.next()){
                zuzzuCheck=true;
                System.out.println("Zuzzu ItemId is existing");
            }
            
            //zuzzuConnection.close();
        }catch(Exception e){
            zuzzuCheck=false;
            e.printStackTrace();
            System.out.println("Exception occured in Zuzzu Id Checking:===>"+e.getMessage());
            
        }
        
        return zuzzuCheck;
    }   
    
    public boolean ebayItemIdCheck(BigInteger ebayid){
    
    boolean ebayCheck=false;
    String ebayidCheckQuery="select oxe.itemid from zuzzu.offer_x_ebayitemid oxe where oxe.ebayitemid="+ebayid;
    try{
        Connection zuzzuConnection=dataSource.getConnection();
    
            Statement stmt =zuzzuConnection.createStatement();
                ResultSet result = stmt.executeQuery(ebayidCheckQuery);
            while(result.next()){
                ebayCheck=true;
                System.out.println("Zuzzu ItemId is existing");
            }
            
    zuzzuConnection.close();
    }catch(Exception e){
    ebayCheck=false;
    e.printStackTrace();
    System.out.println("Exception occured in eBay Id Checking:===>"+e.getMessage());
    
    }
    
    return ebayCheck;
    
    }
    
    
    public boolean zuzzuEbayCheck(String ebayid,String zuzzuid){
        boolean zuzzuebayCheck=false;
        
        String SQL_Query="select oxe.itemid from zuzzu.offer_x_ebayitemid oxe where oxe.ebayitemid="+ebayid+" and oxe.itemid like '"+zuzzuid+"' limit 1";
        try{
            Connection zuzzuConnection=dataSource.getConnection();
            Statement stmt =zuzzuConnection.createStatement();
                ResultSet result = stmt.executeQuery(SQL_Query);
            while(result.next()){
                zuzzuebayCheck=true;
                System.out.println("This zuzzu and ebayitemid is exist");
            }
            
            //zuzzuConnection.close();
        }catch(Exception e){
            zuzzuebayCheck=false;
            e.printStackTrace();
            System.out.println("Exception occured in zuzzueBay Id Checking:===>"+e.getMessage());
            
        }
        
        return zuzzuebayCheck;
    }
    
    
    public String updateZuzzuEoa(String zuzzuId){
        String zuzzuEOATableUpdate="";
        String startDate="";
        String endDate="";
        String duration="";
       // Connection zuzzuConnection=dbconnection.getZuzzuConnection();
        System.out.println("Update method");
        String SQL_Query1="select o.itemid,o.starttime from zuzzu.offer o where o.itemid like '"+zuzzuId+"' limit 1";
        try{
            Connection zuzzuConnection=dataSource.getConnection();
            Statement stmt1=zuzzuConnection.createStatement();
             ResultSet result1 = stmt1.executeQuery(SQL_Query1);
             while(result1.next())
                startDate=result1.getString("starttime");
              
             java.util.Date date= new java.util.Date();
                endDate=new Timestamp(date.getTime()).toString();
                
                if(!startDate.isEmpty() && !endDate.isEmpty()){
               Statement stmt2=zuzzuConnection.createStatement();
               ResultSet result2 = stmt2.executeQuery("SELECT ceil((UNIX_TIMESTAMP('"+endDate+"') - UNIX_TIMESTAMP('"+startDate+"'))/(60*60*24)) as dauer_diff");
               while(result2.next())
                 duration = result2.getString("dauer_diff");
                }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Exception occured in getting item enddate and startdate");
        }
        
        if(!duration.isEmpty() && !endDate.isEmpty()){
            
        String SQL_Query="update zuzzu.offer set offer.endtime='"+endDate+"', offer.duration='"+duration+"' where offer.itemid like '"+zuzzuId+"'";
        try{
            Connection zuzzuConnection=dataSource.getConnection();
            Statement stmt =zuzzuConnection.createStatement();
                int result = stmt.executeUpdate(SQL_Query);
           if(result>0)
               zuzzuEOATableUpdate="success";
            else
               zuzzuEOATableUpdate="fail";
            
            //zuzzuConnection.close();
        }catch(Exception e){
            zuzzuEOATableUpdate="fail";
            e.printStackTrace();
            System.out.println("Exception occured in update table:===>"+e.getMessage());
            
        }
        
        }else
            zuzzuEOATableUpdate="fail";
        
        return zuzzuEOATableUpdate; 
    }
    
    
    public void test(){
        
        try{
            Connection con=dataSource1.getConnection();
            Statement stmt1=con.createStatement();
            ResultSet result1 = stmt1.executeQuery("select * from auktion limit 1");
             while(result1.next())
                System.out.println("new connection"+result1.getString("id"));
            
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
