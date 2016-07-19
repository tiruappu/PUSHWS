/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.dao;

import com.revise.ZuzzuReviseItemRQ;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author rohith
 */
@Component
public class ZuzzuReviseOfferDAO {

    Connection conn = null;
    int value = 0;
    BigInteger ebayItemId;
    int quantity = 0;
    double price = 0;
    String updateSql = null;

    @Autowired
    DataSource zuzzudb;
    
    Logger logger=Logger.getLogger(ZuzzuReviseOfferDAO.class);

    public int zuzzuUpdateQuantity(ZuzzuReviseItemRQ zuzzuReviseOffer) {

        int value = 0;
        try {
            if(conn==null)
            conn = zuzzudb.getConnection();

            ebayItemId = zuzzuReviseOffer.getEbayItemId();

            if (zuzzuReviseOffer.isSetQuantity()) {
                quantity = zuzzuReviseOffer.getQuantity();
                updateSql = "update  zuzzu.offer o join zuzzu.offer_x_ebayitemid oe on o.itemid=oe.itemid set quantity = ?" + " where ebayitemid = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, quantity);
                pstmt.setLong(2, ebayItemId.longValue());
//                logger.info("After setting the values,query is::" + pstmt);
//                logger.info("Query executed Successfully..");
                  value = pstmt.executeUpdate();
             //   logger.info("Value in zuzzuExecuteQuery::" + value);
                //logger.info("Query Updated Successfully After executeUpdate FOR PRICE..");
            }
        } catch (SQLException ex) {
            logger.error("Exception found in SQLException block of ZuzzuReviseOfferDAO class" + ex);
           
        }

        return value;
    }

    public int zuzzuUpdatePrice(ZuzzuReviseItemRQ zuzzuReviseOffer) {

        int value = 0;
        ebayItemId = zuzzuReviseOffer.getEbayItemId();
        try {
              if(conn==null) 
              conn = zuzzudb.getConnection();

            if (zuzzuReviseOffer.isSetQuantity()) {
                price = zuzzuReviseOffer.getPrice();
                updateSql = "update zuzzu.offer_x_price op join zuzzu.offer_x_ebayitemid oe on op.offerId=oe.id set price = ?" + " where ebayitemid= ?";
                PreparedStatement pstmt = conn.prepareStatement(updateSql);
                pstmt.setDouble(1, price);
                pstmt.setLong(2, ebayItemId.longValue());
//                logger.info("After setting the values,query is::" + pstmt);
//                logger.info("Query executed Successfully..");
                value = pstmt.executeUpdate();
//                logger.info("Value in zuzzuExecuteQuery::" + value);
                //logger.info("Query Updated Successfully After executeUpdate FOR PRICE..");
            }
        } catch (SQLException ex) {
            logger.error("Exception found in SQLException block of ZuzzuReviseOfferDAO class" + ex);
            
        }

        return value;

    }

}
