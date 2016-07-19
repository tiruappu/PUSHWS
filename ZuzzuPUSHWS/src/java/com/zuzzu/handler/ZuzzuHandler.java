/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.handler;


import com.additem.ZuzuAddRequest;
import com.additem.ZuzzuAddItemRQ;
import com.currentbid.ZuzuCurrentBidRequest;
import com.currentbid.ZuzzuCurrentPriceBidCount;
import com.revise.ZuzzuReviseItemRQ;
import com.revise.ZuzzuReviseItemRS;
import com.zuzzu.ZuzzuEndItemRQ;
import com.zuzzu.ZuzzuEndItemRS;
import com.zuzzu.util.ZuzzuHelper;
import com.zuzzu.dao.ZuzzuReviseOfferDAO;
import com.zuzzu.util.ZuzzuUtilityManager;
import java.io.IOException;
import java.sql.SQLException;
import javax.xml.bind.JAXBException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author tirupathi
 */
@RestController
@RequestMapping(value = "/zuzzu")
public class ZuzzuHandler {

    @Autowired
    ZuzzuHelper zuzzuHelper;

    ZuzzuReviseItemRQ zuzzuReviseItemRQ = null;
    ZuzzuReviseItemRS zuzzuReviseItemRS = null;

    @Autowired
    ZuzzuReviseOfferDAO zuzzuReviseOfferDAO;
    
     @Autowired
    ZuzzuUtilityManager saveLog;
     
      private static final Logger logger = Logger.getLogger(ZuzzuHandler.class);

    //JAXBContext jaxbContext=null;
    @RequestMapping(value = "/zuzzuEOA", method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public void eoaReqHandler(@RequestBody ZuzzuEndItemRQ zuzzuEndItemRq) {
         long startTime = System.currentTimeMillis();
        ZuzzuEndItemRS zuzzuRes = null;
        String zuzzuid=null;
        logger.debug("its controller");
        logger.debug("This is EOA Handler");

        if (zuzzuHelper.validateEoaRequest(zuzzuEndItemRq)) {
            
            if(zuzzuEndItemRq.getZuzzuItemId()==null || !zuzzuEndItemRq.isSetZuzzuItemId()){
                  zuzzuid  = zuzzuHelper.getZuzzuid(zuzzuEndItemRq.getEbayItemId().toString());
                    logger.debug("Getting zuzzuId"+zuzzuid);
                  zuzzuEndItemRq.setZuzzuItemId(zuzzuid);
            
            }
            
            logger.debug("Zuzzu Id==>"+zuzzuEndItemRq.getZuzzuItemId()+"==EbayId=="+zuzzuEndItemRq.getEbayItemId());
            
            zuzzuRes = zuzzuHelper.updateEOARequest(zuzzuEndItemRq.getZuzzuItemId());
        } else {
            logger.debug("Invalid XML");
        }

        if (zuzzuRes.getAck().equals("success")) {
            //Client Url
            logger.debug("Enter into end item checking");
            String rqXML = zuzzuHelper.endItemMarshaller(zuzzuEndItemRq);
            String clientRS=zuzzuHelper.sendRQToClient(rqXML);
            logger.debug("ClientResponse"+clientRS);
            int endItemStatus=zuzzuHelper.endItemUnMarshaller(clientRS);
            long endTime = System.currentTimeMillis();
            long perfTime = endTime-startTime;
            String ebayid="";
            if(zuzzuEndItemRq.getEbayItemId()!=null && zuzzuEndItemRq.isSetEbayItemId())
               ebayid= zuzzuEndItemRq.getEbayItemId().toString();
            
                
                
            
            saveLog.insertIntoZuzzuPushLog(zuzzuEndItemRq.getZuzzuItemId(), ebayid, rqXML, clientRS, 4, endItemStatus, perfTime);
            /* if (clientSendStatus) {
            logger.debug("zuzzu updated");
            } else {
            logger.debug("problem occured at sendToClient");
            }*/

        }

        
    }

    //Revice
    //private UnMarshalling unmarshalling;
    @RequestMapping(value = "/zuzzuRevise", method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public void ZuzzuReviseItemRQHandler(@RequestBody ZuzzuReviseItemRQ zuzzuReviseItemRQ) {
        logger.debug("inside Service...");
        String zuzzuid="";
        String ebayid="";
        long startTime = System.currentTimeMillis();
        //String xmlRes = "";

//        logger.debug("After Unmarshalling...");
//        //zuzzuReviseItemRQ =(ZuzzuReviseItemRQ)unmarshalling.convertToObject(xml,xmlRes);
//        logger.debug("After Convert To Object...");
//
//        logger.debug("setChannelId" + zuzzuReviseItemRQ.getChannelId());
//        logger.debug("Price::" + zuzzuReviseItemRQ.getPrice());
//        logger.debug("" + zuzzuReviseItemRQ.getZuzzuItemId());
//        logger.debug("" + zuzzuReviseItemRQ.getChannelId());
//        logger.debug("" + zuzzuReviseItemRQ.getQuantity());
//        logger.debug("" + zuzzuReviseItemRQ.getQuantityRestriction());

        int i = zuzzuReviseOfferDAO.zuzzuUpdateQuantity(zuzzuReviseItemRQ);
        logger.debug("value of i is:" + i);

        int j = zuzzuReviseOfferDAO.zuzzuUpdatePrice(zuzzuReviseItemRQ);
        logger.debug("value of j is:" + j);
        
        if(i>0 || j>0){
            logger.debug("Enter to revise if");
            String rqXML=zuzzuHelper.reviseItemMarshaller(zuzzuReviseItemRQ);
            String clientRS=zuzzuHelper.sendRQToClient(rqXML);
            int reviseItemStatus=zuzzuHelper.reviseItemUnMarshaller(clientRS);
            long endTime = System.currentTimeMillis();
            long perfTime =  endTime-startTime;
            
            if(zuzzuReviseItemRQ.getZuzzuItemId()!=null)
                 zuzzuid=zuzzuReviseItemRQ.getZuzzuItemId();
            
            if(zuzzuReviseItemRQ.getEbayItemId()!=null)
           ebayid=zuzzuReviseItemRQ.getEbayItemId().toString();
                
            
            saveLog.insertIntoZuzzuPushLog(zuzzuid, ebayid, rqXML, clientRS, 3, reviseItemStatus, perfTime);
        }
        
           
       

    }
    
    
    //AddItem
    
    
    //AddItem
    /**
     * This method is used to produce the zuzzu add item xml
     *
     * @param addRequest it is the intermediate XML between PHP
     * item_einstellfehler cron and AddItem Restfull
     * @return
     */
    @RequestMapping(value = "/zuzzuAdd", method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)

    private void ZuzzuAddItemRQHandler(@RequestBody ZuzuAddRequest addRequest) {
        long startTime = System.currentTimeMillis();
        ZuzzuAddItemRQ addItemRQ = new ZuzzuAddItemRQ();
        try {
            String ebayItemId = addRequest.getItemid();
            String zuzzuitemId = addRequest.getZuzuitemid();
            addItemRQ = zuzzuHelper.getAddItemDetails(ebayItemId, addItemRQ);
            addItemRQ.setZuzzuItemId(zuzzuitemId);
            String marshallString = zuzzuHelper.addItemMarshaller(addItemRQ);
            logger.info("Request" + marshallString);
            String clientResponse = null;
            int addItemStatus = 0;
            if (zuzzuitemId != null && ebayItemId != null) {
                clientResponse = zuzzuHelper.sendRQToClient(marshallString);
                logger.info("Response" + clientResponse);
                addItemStatus = zuzzuHelper.addItemUnMarshaller(clientResponse);
            } else {
                addItemStatus = -1;
            }
            long endTime = System.currentTimeMillis();
            long perfTime = endTime - startTime;
            saveLog.insertIntoZuzzuPushLog(zuzzuitemId, ebayItemId, marshallString, clientResponse, 1, addItemStatus, perfTime);
        } catch (SQLException ex) {
            addItemRQ = null;
            logger.error(ex);

        } catch (Exception e) {
            addItemRQ = null;
            logger.error(e);
        }

    }

    
    //Current Bid Price
    
    /**
     * This is required to handle the current bid notifications for particular
     * ebay item id
     *
     * @param bidRequest
     * @return
     */
    @RequestMapping(value = "/zuzzuCurrentBidPrice", method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    private void handleZuzzuCurrentBid(@RequestBody ZuzuCurrentBidRequest bidRequest) {
         long startTime = System.currentTimeMillis();
        ZuzzuCurrentPriceBidCount bidNotification = new ZuzzuCurrentPriceBidCount();
        try {
            String ebayItemid = bidRequest.getItemid();
            bidNotification = zuzzuHelper.getCurrentBid(ebayItemid, bidNotification);
            String currentBidPriceRQXML=zuzzuHelper.currentBidPriceMarshaller(bidNotification);
            
            String clientResponse = zuzzuHelper.sendRQToClient(currentBidPriceRQXML);
            logger.debug("Client Response"+clientResponse);
            int bidPriceStatus=zuzzuHelper.currentBidPriceUnMarshaller(clientResponse);
            
            long endTime = System.currentTimeMillis();
            long perfTime =   endTime-startTime;
            
            
            saveLog.insertIntoZuzzuPushLog("", ebayItemid, currentBidPriceRQXML, clientResponse, 2, bidPriceStatus, perfTime);
            
        } catch (SQLException ex) {
            bidNotification = null;
            logger.error(ex);
           
        } catch (Exception e) {
            bidNotification = null;
            logger.error(e);
        }
       // return bidNotification;
    }

    /* private void generateReviseResponse(HttpURLConnection conn, long startTime, ZuzzuReviseItemRQ zuzzuReviseItemRQ) throws IOException {
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
    logger.debug(response.toString());
    }
    long endTime = System.currentTimeMillis();
    long perfTime = startTime - endTime;
    
    }*/
    /*public void setZuzzuReviseOfferDAO(ZuzzuReviseOfferDAO zuzzuReviseOfferDAO) {
    this.zuzzuReviseOfferDAO = zuzzuReviseOfferDAO;
    }
    
    public ZuzzuReviseOfferDAO getZuzzuReviseOfferDAO() {
    return zuzzuReviseOfferDAO;
    }*/

}
