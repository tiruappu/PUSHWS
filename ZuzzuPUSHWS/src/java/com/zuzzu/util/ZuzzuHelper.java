/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.util;


import com.additem.ZuzzuAddItemRQ;
import com.additem.ZuzzuAddItemRS;
import com.currentbid.ZuzzuCurrentBidRS;
import com.currentbid.ZuzzuCurrentPriceBidCount;
import com.revise.ZuzzuReviseItemRQ;
import com.revise.ZuzzuReviseItemRS;
import com.zuzzu.ZuzzuEndItemRQ;
import com.zuzzu.ZuzzuEndItemRS;
import com.zuzzu.dao.ZuzzuEoaDao;
import com.zuzzu.dao.ZuzuAddItemDAO;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import static java.lang.System.out;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author tirupathi
 */
@Component
public class ZuzzuHelper {

    @Autowired
    public ZuzzuEoaDao zuzzuEoaDao;
    
    @Autowired
    ZuzuAddItemDAO addItemDB;
    
   Logger logger = Logger.getLogger(ZuzzuHelper.class);

    public boolean validateEoaRequest(ZuzzuEndItemRQ zuzzuEndItemRQ) {
        boolean validation = false;
        boolean zuzzuChannelIdCheck = false;
        boolean zuzzueBayCheck = false;
        boolean zuzzuIdChecking = false;
        boolean ebayIdChecking = false;
        logger.info(zuzzuEndItemRQ.getChannelId() + "===" + zuzzuEndItemRQ.getZuzzuItemId() + "===" + zuzzuEndItemRQ.getEbayItemId());

        //ChannelId is mandatory
        if (zuzzuEndItemRQ.isSetChannelId() && zuzzuEndItemRQ.getChannelId() == 2) {
            zuzzuChannelIdCheck = true;

            if (zuzzuEndItemRQ.isSetEbayItemId() && zuzzuEndItemRQ.isSetZuzzuItemId() && zuzzuEndItemRQ.getZuzzuItemId() != null && !zuzzuEndItemRQ.getZuzzuItemId().isEmpty() && zuzzuEndItemRQ.getEbayItemId() != null && !zuzzuEndItemRQ.getEbayItemId().equals("")) {

                zuzzueBayCheck = zuzzuEoaDao.zuzzuEbayCheck(String.valueOf(zuzzuEndItemRQ.getEbayItemId()), zuzzuEndItemRQ.getZuzzuItemId());

                logger.info("tiru");

            } else if (zuzzuEndItemRQ.getZuzzuItemId() != null && zuzzuEndItemRQ.isSetZuzzuItemId()) {
                zuzzuIdChecking = zuzzuEoaDao.zuzzuCheck(zuzzuEndItemRQ.getZuzzuItemId());

            } else if (zuzzuEndItemRQ.getEbayItemId() != null && zuzzuEndItemRQ.isSetEbayItemId()) {
                ebayIdChecking = zuzzuEoaDao.ebayItemIdCheck(zuzzuEndItemRQ.getEbayItemId());
            }

        } else {
            zuzzuChannelIdCheck = false;
        }

        logger.info("this is result");
        logger.info(zuzzuChannelIdCheck + "===" + zuzzueBayCheck + "===" + zuzzuIdChecking);
        if (zuzzuChannelIdCheck && (zuzzueBayCheck || zuzzuIdChecking || ebayIdChecking)) {
            validation = true;
        } else {
            validation = false;
        }

        logger.info(validation);
        return validation;
    }

    public ZuzzuEndItemRS updateEOARequest(String zuzzuId) {

        ZuzzuEndItemRS zuzzurs = new ZuzzuEndItemRS();
        logger.info("before update");
        String updateStatus = zuzzuEoaDao.updateZuzzuEoa(zuzzuId);
        if (updateStatus.equals("success")) {
            zuzzurs.setAck("success");
        } else {
            zuzzurs.setAck("fail");

        }

        logger.info(zuzzurs.getAck());
        return zuzzurs;
    }
    
    
    public String getZuzzuid(String ebayid){
        return zuzzuEoaDao.getZuzzuId(ebayid);
    }

    public String endItemMarshaller(ZuzzuEndItemRQ zuzzuEndItemRQ) {

        StringWriter sw = new StringWriter();
        try {
            JAXBContext endJaxbContext = null;

            if (endJaxbContext == null) {
                endJaxbContext = JAXBContext.newInstance("com.zuzzu");
            }

            Marshaller jaxbMarshaller = endJaxbContext.createMarshaller();
            jaxbMarshaller.marshal(zuzzuEndItemRQ, sw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Request Data" + sw.toString());
        return sw.toString();

    }

    public String reviseItemMarshaller(ZuzzuReviseItemRQ zuzzuReviseItemRQ) {

        StringWriter sw = new StringWriter();
        try {
            JAXBContext reviseJaxbContext = null;

            if (reviseJaxbContext == null) {
                reviseJaxbContext = JAXBContext.newInstance("com.revise");
            }

            Marshaller jaxbMarshaller = reviseJaxbContext.createMarshaller();
            jaxbMarshaller.marshal(zuzzuReviseItemRQ, sw);
            logger.info("Revise Data:"+sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in ReviseItem Marshaller==>"+e.getMessage());
        }
        return sw.toString();

    }

     public String addItemMarshaller(ZuzzuAddItemRQ addItemRQ){
         
         
          StringWriter sw = new StringWriter();
        try {
            JAXBContext addItemJaxbContext = null;

            if (addItemJaxbContext == null) {
                addItemJaxbContext = JAXBContext.newInstance("com.additem");
            }

            Marshaller jaxbMarshaller = addItemJaxbContext.createMarshaller();
            jaxbMarshaller.marshal(addItemRQ, sw);
            logger.info("Add Data:"+sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception occured in addItemMarshaller"+e.getMessage());
        }
        return sw.toString();
    
    }
   
    public String currentBidPriceMarshaller(ZuzzuCurrentPriceBidCount currentBidPriceNotification){
        
        StringWriter sw = new StringWriter();
        try {
            JAXBContext addItemJaxbContext = null;

            if (addItemJaxbContext == null) {
                addItemJaxbContext = JAXBContext.newInstance("com.currentbid");
            }

            Marshaller jaxbMarshaller = addItemJaxbContext.createMarshaller();
            jaxbMarshaller.marshal(currentBidPriceNotification, sw);
            logger.info("CurrentBid Data:"+sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception occured in CurrentBidPriceMarshaller"+e.getMessage());
        }
        return sw.toString();
    
    }
    
     public int endItemUnMarshaller(String clientResponse) {

         int status=0;
          
        try {
            JAXBContext addItemJaxbContext = null;

            if (addItemJaxbContext == null) {
                addItemJaxbContext = JAXBContext.newInstance("com.zuzzu");
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(clientResponse.getBytes("UTF-8"));
            Unmarshaller jaxbUnmarshaller = addItemJaxbContext.createUnmarshaller();  
            ZuzzuEndItemRS itemRs= (ZuzzuEndItemRS) jaxbUnmarshaller.unmarshal(bis); 
            
            if(itemRs.getAck().equals("success"))
                status=1;
            else
                status=-1;
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception occured in endItemUnMarshaller"+e.getMessage());
        }
        return status;
       

    }

    public int reviseItemUnMarshaller(String clientResponse) {

       int status=0;
          
        try {
            JAXBContext addItemJaxbContext = null;

            if (addItemJaxbContext == null) {
                addItemJaxbContext = JAXBContext.newInstance("com.revise");
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(clientResponse.getBytes("UTF-8"));
            Unmarshaller jaxbUnmarshaller = addItemJaxbContext.createUnmarshaller();  
            
            ZuzzuReviseItemRS itemRs= (ZuzzuReviseItemRS) jaxbUnmarshaller.unmarshal(bis); 
            
            if(itemRs.getAck().equals("success"))
                status=1;
            else
                status=-1;
           
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception occured in reviseItemUnMarshaller"+e.getMessage());
        }
        return status;
    }

     public int addItemUnMarshaller(String clientResponse){
         
         int status=0;
          
        try {
            JAXBContext addItemJaxbContext = null;

            if (addItemJaxbContext == null) {
                addItemJaxbContext = JAXBContext.newInstance("com.additem");
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(clientResponse.getBytes("UTF-8"));
            Unmarshaller jaxbUnmarshaller = addItemJaxbContext.createUnmarshaller();  
            ZuzzuAddItemRS itemRs= (ZuzzuAddItemRS) jaxbUnmarshaller.unmarshal(bis); 
            
            if(itemRs.getAck().equals("success"))
                status=1;
            else
                status=-1;
           
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception occured in AddItemUnMarshaller"+e.getMessage());
        }
        return status;
    
    }
   
    public int currentBidPriceUnMarshaller(String clientResponse){
        
       int status=0;
          
        try {
            JAXBContext addItemJaxbContext = null;

            if (addItemJaxbContext == null) {
                addItemJaxbContext = JAXBContext.newInstance("com.currentbid");
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(clientResponse.getBytes("UTF-8"));
            Unmarshaller jaxbUnmarshaller = addItemJaxbContext.createUnmarshaller();  
            ZuzzuCurrentBidRS itemRs= (ZuzzuCurrentBidRS) jaxbUnmarshaller.unmarshal(bis); 
            
            if(itemRs.getAck().equals("success"))
                status=1;
            else
                status=-1;
           
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception occured in currentBidItemUnMarshaller"+e.getMessage());
        }
        return status;
    
    }
    
    
    
    public String sendRQToClient(String requestXml) {

        String clientResponse ="";

        try {

            long startTime = System.currentTimeMillis();
            URL url = new URL("http://localhost:8080/TestUrl/TestServlet");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("charset", "utf-8");
            // conn.setRequestProperty("Content-Length", sw.toString());
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(requestXml.getBytes());

            conn.connect();
            conn.getResponseCode();
            logger.info("Response Code " + conn.getResponseCode());

            StringBuffer response = null;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;

                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
               clientResponse=response.toString();
            }
            long endTime = System.currentTimeMillis();
            long perfTime = endTime-startTime;

            logger.info("This is Client Request Total time"+perfTime);
        } catch (Exception e) {
           
            e.printStackTrace();
            logger.error("Exception occured in Send request to client");
        }

        return clientResponse;
    }
    
     /**
     * This method is used to get the offer details
     *
     * @param ebayItemid
     * @param addItemRQ
     * @return
     */
    public ZuzzuAddItemRQ getAddItemDetails(String ebayItemid, ZuzzuAddItemRQ addItemRQ) throws SQLException {
        addItemRQ = addItemDB.getOfferDetails(ebayItemid, addItemRQ);
        return addItemRQ;
    }
    
    
     /**
     * This method id used to get the currentBid information
     * @param ebayItemid
     * @param bidCount
     * @return
     */
    public ZuzzuCurrentPriceBidCount getCurrentBid(String ebayItemid, ZuzzuCurrentPriceBidCount bidCount) throws SQLException {

        bidCount = addItemDB.getCurrentBidDetails(ebayItemid, bidCount);

        return bidCount;
    }
    

}
