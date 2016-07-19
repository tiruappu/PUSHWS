/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.dao;

import com.additem.GeoPosition;
import com.additem.GeoTag;
import com.additem.PictureURLs;
import com.additem.ZuzzuAddItemRQ;
import com.currentbid.ItemStatus;
import com.currentbid.ZuzzuCurrentPriceBidCount;
import com.zuzzu.util.ZuzzuUtilityManager;
import comm.ZuzzuSqlQueries;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used to interact with ebay and cusebeda databses for auction
 * information.
 *
 * @author sowmya
 */
@Component
public class ZuzuAddItemDAO implements ZuzzuSqlQueries {

    @Autowired
    DataSource zuzzudb;
    
    @Autowired
     ZuzzuUtilityManager manager;

    PreparedStatement statement;

    ResultSet rs;

    Connection con;
    
    Logger logger=Logger.getLogger(ZuzuAddItemDAO.class);
    

    /**
     * This method is responsible to fill the data points while construction the
     * AddItem xml
     *
     * @param ebayItemid
     * @param addItemRQ
     * @return
     */
    public ZuzzuAddItemRQ getOfferDetails(String ebayItemid, ZuzzuAddItemRQ addItemRQ) throws SQLException {

        if (con == null) {
            con = zuzzudb.getConnection();
        }
        statement = con.prepareStatement(offer_query);
        statement.setString(1, ebayItemid);
        rs = statement.executeQuery();
        logger.info("Result size" + rs.getRow());
        logger.info("Statement" + statement.toString());
        long ebayL=Long.valueOf(ebayItemid);
        if (rs != null && rs.next()) {
            int objectId = rs.getInt("objectID");
            int auctionId = rs.getInt("id");
            String buyNowPrice = rs.getString("ebaysofortkauf");
            int quantity = rs.getInt("quantity");
            String siteId = rs.getString("ebaysiteid");
            int vorlageId = rs.getInt("vorlage_id");
            addItemRQ.setObjectID(objectId);
             addItemRQ.setEbayItemId(BigInteger.valueOf(ebayL));
            if (buyNowPrice != null && buyNowPrice.length() != 0 && rs.getInt("currentbid") == 0) {
                addItemRQ.setBuyNowPrice(buyNowPrice);
            }
//            if (rs.getInt("status") != 0) {
//                
//                addItemRQ.setStatus(rs.getInt("status"));
//            }
//            
            if (rs.getInt("AuctionMasterTypeID") == 4) {
                addItemRQ.setBuyNowPrice(rs.getString("startpreis"));
            }

            addItemRQ.setStartBidPrice(rs.getString("startpreis"));
            addItemRQ.setQuantity(quantity);
            addItemRQ.setEndDate(rs.getString("endDate"));
           // addItemRQ.setItemId(auctionId);

            if (rs.getString("ebaydaten.ebayname") != null) {
                addItemRQ.setSellerId(rs.getString("ebayname"));
            } else {
                addItemRQ.setSellerId("");
            }
            logger.info("Duration  :: "+rs.getInt("dauer"));
              logger.info("Start Date  :: "+rs.getString("startdatum"));
               logger.info("Subtitle  :: "+rs.getString("untertitel"));
                logger.info("Title  :: "+rs.getString("ebayueberschrift"));
            addItemRQ.setDurationDays(rs.getInt("dauer"));
            addItemRQ.setStartDate(rs.getString("startdatum"));
            addItemRQ.setSubTitle(rs.getString("untertitel"));
            addItemRQ.setTitle(rs.getString("ebayueberschrift"));
            if (rs.getString("ebaycountry") != null) {
                addItemRQ.setCountry(rs.getString("ebaycountry"));
            } else {
                addItemRQ.setCountry("");
            }
            getMarketPlace(addItemRQ, siteId);
            getAuctionURL(siteId, addItemRQ);
            getSiteCurrency(siteId, addItemRQ);
            getGeoTagInformation(addItemRQ, objectId);
            getAddressInformation(addItemRQ, objectId);
            getImageURl(addItemRQ, auctionId);
            getCalendarInformation(addItemRQ, objectId);
            //getSoldQuantity(addItemRQ, ebayItemid);
            String[] priceRangeS = new String[2];
            priceRangeS = getAuctionPriceRange(String.valueOf(auctionId));
            addItemRQ.setPriceRange(priceRangeS[0] + " " + priceRangeS[1]);
            getProductDetails(addItemRQ, vorlageId, ebayItemid);
        }
        return addItemRQ;
    }

    /**
     * This method is used to get details of geoTag
     *
     * @param addItemRQ
     * @param ojectId
     */
    private void getGeoTagInformation(ZuzzuAddItemRQ addItemRQ, int objectId) throws SQLException {
        GeoTag geoTag = new GeoTag();
        addItemRQ.setGeoTag(geoTag);
        addItemRQ.getGeoTag().setGeoPosition(getgeoPosition(objectId));
    }

    /**
     * This method is used to get details of GeoPosition
     *
     * @param ojectId
     * @return
     */
    private GeoPosition getgeoPosition(int objectId) throws SQLException {
        GeoPosition geoPosition = new GeoPosition();
        geoPosition.setLatitude(BigDecimal.ZERO);
        geoPosition.setLongitude(BigDecimal.ZERO);
        statement = con.prepareStatement(geo_query);
        rs = statement.executeQuery(geo_query + objectId);
        if (rs != null && rs.next()) {
            logger.info("Latitude  :: "+rs.getBigDecimal("latitude"));
             logger.info("Longitude  :: "+rs.getBigDecimal("longitude"));
            geoPosition.setLatitude(rs.getBigDecimal("latitude"));
            geoPosition.setLongitude(rs.getBigDecimal("longitude"));
        }
        return geoPosition;
    }

    /**
     * This method is required to get the address information
     *
     * @param addItemRQ
     * @param objectId
     */
    private void getAddressInformation(ZuzzuAddItemRQ addItemRQ, int objectId) throws SQLException {
        String[] objectData = getObjectData(objectId);
         logger.info("Country :: "+objectData[1]);
        logger.info("Place :: " +objectData[2]);
        logger.info("Region :: "+objectData[3]);
        logger.info("HotelName :: "+objectData[0]);
        if (null != objectData[0] && null != objectData[1] && null != objectData[2] && null != objectData[3]) {
            addItemRQ.setHotelName(objectData[0]);
            addItemRQ.setPlace(objectData[2]);
            addItemRQ.setRegion(objectData[3]);
        } else {
            addItemRQ.setHotelName("");
            addItemRQ.setPlace("");
            addItemRQ.setRegion("");
        }
        addItemRQ.setAddress(objectData[1] + "+" + objectData[3] + "+" + objectData[2] + "+" + objectData[4]);
    }

    /**
     * The method is used to query for the address information
     *
     * @param objectId
     * @return
     */
    private String[] getObjectData(int objectId) throws SQLException {
        String[] objectData = new String[5];
        statement = con.prepareStatement(address_query, objectId);
        statement.setInt(1, objectId);
        rs = statement.executeQuery();
        if (rs != null && rs.next()) {

            objectData[0] = rs.getString("bezeichnung");
            objectData[1] = rs.getString("land");
            objectData[2] = rs.getString("city");
            objectData[3] = rs.getString("region");
            objectData[4] = rs.getString("street");
        }
        return objectData;

    }

    /**
     * This method is used to get image,Picture Url and Description from
     * apiCallRequest Table for particular auctionId
     *
     * @param auctionId
     */
    private void getImageURl(ZuzzuAddItemRQ addItemRQ, int auctionId) throws SQLException {
        String imageQuery = "SELECT request "
                + "FROM ebay3.apiCallRequest" + " WHERE uuid like '"
                + auctionId + "%'";
        PictureURLs pictureURLs = new PictureURLs();

        statement = con.prepareStatement(imageQuery);
        rs = statement.executeQuery();
        if (rs != null && rs.next()) {

            String apiCallRequest = rs.getString("request");
            if (apiCallRequest.contains("<GalleryURL>")) {
                int indexbeg = apiCallRequest.indexOf("<GalleryURL>") + 12;
                int indexend = apiCallRequest.lastIndexOf("</GalleryURL>");
                String img = apiCallRequest.substring(indexbeg, indexend);

                addItemRQ.setHotelPicture("<![CDATA[" + img + "]]>");

            }
            if (apiCallRequest.contains("<Description>")) {
                int indexbeg = apiCallRequest.indexOf("<Description>") + 13;
                int indexend = apiCallRequest.indexOf("</Description>");
                String desc = apiCallRequest.substring(indexbeg, indexend);
                String output = desc.replace("´", "'");
                output = desc.replace("�", "'");

                addItemRQ.setHtmlTemplate("<![CDATA[" + output + "]]>");

            }
            if (apiCallRequest.contains("<PictureURL>")) {
                int indexbeg = apiCallRequest.indexOf("<PictureURL>") + 12;
                int indexend = apiCallRequest.lastIndexOf("</PictureURL>");
                List pictureUrl = null;
                logger.info("Begin index======" + indexbeg);
                if (indexbeg != 11) {
                    String desc = apiCallRequest.substring(indexbeg, indexend);
                    String output = desc.replace("´", "'");

                    pictureUrl = new ArrayList();
                    String urls[] = output.split("<PictureURL>");
                    for (int i = 0; i < urls.length; i++) {
                        logger.info("" + urls[i].replace("</PictureURL>", ""));
                        urls[i] = urls[i].replace("</PictureURL>", "");
                        pictureUrl.add(urls[i].replace("</PictureURL>", ""));
                    }
                }
                pictureURLs.setPictureURL("<![CDATA[" + pictureUrl.toString() + "]]>");

                addItemRQ.setPictureURLs(pictureURLs);

            }
        } else {
            addItemRQ.setHotelPicture("");
            addItemRQ.setHtmlTemplate("");
            addItemRQ.setPictureURLs(pictureURLs);
        }

    }

//    /**
//     * This method is used to get the sold quantity for particular auctionId
//     *
//     * @param addItemRQ
//     * @param auctionId
//     */
//    private void getSoldQuantity(ZuzzuAddItemRQ addItemRQ, String auctionId) throws SQLException {
//        con = dBConn.getEbayDBConn();
//        
//        statement = con.prepareStatement(sold_quantity_query);
//        statement.setString(1, auctionId);
//        rs = statement.executeQuery();
//        if (rs.getRow() > 0) {
//            while (rs.next()) {
//                addItemRQ.setSoldnumbers(rs.getInt("soldnumbers"));
//            }
//        } else {
//            addItemRQ.setSoldnumbers(0);
//        }
//        
//    }
    /**
     * This method is used to get the URL base don the siteId
     *
     * @param siteID
     * @param addItemRQ
     */
    public void getAuctionURL(String siteID, ZuzzuAddItemRQ addItemRQ) {
        String URL = null;

        if (siteID.equals("0")) {
            URL = "http://cgi.ebay.com/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("3")) {
            URL = "http://cgi.ebay.co.uk/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("77")) {
            URL = "http://cgi.ebay.de/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("15")) {
            URL = "http://cgi.ebay.com.au/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("71")) {
            URL = "http://cgi.ebay.fr/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("101")) {
            URL = "http://cgi.ebay.it/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("16")) {
            URL = "http://cgi.ebay.at/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("193")) {
            URL = "http://cgi.ebay.ch/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("146")) {
            URL = "http://cgi.ebay.nl/ws/eBayISAPI.dll?ViewItem&item=";
        } else {
            URL = "http://cgi.ebay.de/ws/eBayISAPI.dll?ViewItem&item=";
        }

        addItemRQ.setUrl(URL);
    }

    /**
     * This method is used to fetch the currency based on siteId
     *
     * @param siteId
     * @param addItemRQ
     */
    private void getSiteCurrency(String siteId, ZuzzuAddItemRQ addItemRQ) throws SQLException {

        statement = con.prepareStatement(site_currency);
        statement.setString(1, siteId);
        rs = statement.executeQuery();
        if (rs != null && rs.next()) {
             logger.info("Currency :: "+rs.getString(2));
            addItemRQ.setCurrency(rs.getString(2));
        } else {
            addItemRQ.setCurrency("");
        }

    }

    /**
     * This method is used to get the Price Range Information
     *
     * @param auctionID
     * @return
     */
    public String[] getAuctionPriceRange(String auctionID) throws SQLException {
        String[] priceRange = new String[2];
        priceRange[0] = "";
        priceRange[1] = "";
        String act_table = null;

        Calendar cal = new GregorianCalendar();
        // Get the components of the time
        int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23
        // if it is an even hour
        if (hour24 % 2 == 1) {
            act_table = "1";
        } else {
            act_table = "2";
        }

        // Period Query
        String SQL_Query = " SELECT cultbaydata.auktionen_" + act_table
                + ".auktion_id," + "cultbaydata.vorlagen_" + act_table
                + ".sigma," + "cultbaydata.vorlagen_" + act_table
                + ".ebaymittel" + " FROM cultbaydata.auktionen_"
                + act_table + ",cultbaydata.vorlagen_" + act_table
                + " WHERE cultbaydata.auktionen_" + act_table
                + ".auktion_id ='" + auctionID + "'"
                + " AND cultbaydata.vorlagen_" + act_table
                + ".vorlage_id = cultbaydata.auktionen_" + act_table
                + ".vorlage_id  ";
        // Query
        //logger.info("Price Range Query:" + SQL_Query);

        statement = con.prepareStatement(SQL_Query);
        rs = statement.executeQuery();
        while (rs.next()) {
            Double rangeMin;
            Double rangeMax;
            Double sigma = rs.getDouble("sigma");
            Double ebaymittel = rs.getDouble("ebaymittel");
            if (ebaymittel == 0) {
                priceRange[0] = "";
                priceRange[1] = "";
            } else if (sigma == 0) {
                rangeMax = Math.ceil(ebaymittel + sigma);
                priceRange[0] = "";
                priceRange[1] = rangeMax.toString();
            } else {
                rangeMin = Math.floor(ebaymittel - sigma);
                rangeMax = Math.ceil(ebaymittel + sigma);
                priceRange[0] = rangeMin.toString();
                priceRange[1] = rangeMax.toString();
            }
        }

        return priceRange;
    }

   /**
     * This method is used to get calendar information based on object id.
     *
     * @param addItemRQ
     * @param objectId
     */
    private void getCalendarInformation(ZuzzuAddItemRQ addItemRQ, int objectId) throws SQLException {
        statement = con.prepareStatement(calendar_query);
        statement.setInt(1, objectId);
        rs = statement.executeQuery();
        if (rs != null) {
            logger.info("Rows Count for Calendar information" + rs.getRow());
            if (rs.next()) {
                logger.info("Calendar Infomation :: " + rs.getString("kalendar"));
                addItemRQ.setBookItNow(rs.getString("kalendar"));
            } else {
                addItemRQ.setBookItNow("");
            }
        }
    }

     /**
     * This method is used to get MarketPlace information
     *
     * @param addItemRQ
     * @param ebayItemid
     */
    private void getMarketPlace(ZuzzuAddItemRQ addItemRQ, String id) throws SQLException {
        statement = con.prepareStatement(marketplace_query);
        statement.setString(1, id);
        rs = statement.executeQuery();
        if (rs != null) {
            logger.info("Rows Count for Market Place" + rs.getRow());
            if (rs.next()) {
                logger.info("MarketPlace  :: " + rs.getString("SiteCodeType"));
                addItemRQ.setMarketPlace(rs.getString("SiteCodeType"));

            } else {
                addItemRQ.setMarketPlace("");
            }
        }

    }

    /**
     * This method is required to get the current bid details of the particular
     * offer
     *
     * @param ebayItemId
     * @param bidCountNotification
     * @return
     * @throws SQLException
     */
    public ZuzzuCurrentPriceBidCount getCurrentBidDetails(String ebayItemId, ZuzzuCurrentPriceBidCount bidCountNotification) throws SQLException {
        
        con = zuzzudb.getConnection();
        statement = con.prepareStatement(currentbid_query);
        statement.setString(1, ebayItemId);
        String[] priceRange = new String[2];
        rs = statement.executeQuery();
        ItemStatus itemStatus = new ItemStatus();

        if (rs != null && rs.next()) {
            logger.info("Bid Count  :: "+rs.getInt("anzahlgebote"));
             logger.info("CurrentPrice  :: "+rs.getString("currentbid"));
            itemStatus.setBidCount(rs.getInt("anzahlgebote"));
            itemStatus.setCurrentPrice(rs.getString("currentbid"));
            priceRange = getAuctionPriceRange(rs.getString("id"));
            itemStatus.setPriceRange(priceRange[0] + "" + priceRange[1]);
        }

        bidCountNotification.setEbayItemID(ebayItemId);
        bidCountNotification.setItemStatus(itemStatus);
        

        return bidCountNotification;
    }

    /**
     * This method is used to get the product details of the particular offer
     *
     * @param addItemRQ
     * @param vorlageId
     */
    private void getProductDetails(ZuzzuAddItemRQ addItemRQ, int vorlageId, String ebayItemId) throws SQLException {
        statement = con.prepareStatement(product_query);
        statement.setString(1, ebayItemId);
        rs = statement.executeQuery();
        if (rs != null && rs.next()) {
            logger.info("Arrangement ID  :: "+rs.getInt("arrangement_id"));
            int arrangementId = rs.getInt("arrangement_id");
            getArrangementDetails(arrangementId, vorlageId, addItemRQ);
        }
    }

    /**
     * This method is used to get noOfpersons and Accomodation type based on
     * arrangementId
     *
     * @param arrangementId
     * @param vorlageId
     * @throws SQLException
     */
    private void getArrangementDetails(int arrangementId, int vorlageId, ZuzzuAddItemRQ addItemRQ) throws SQLException {
        if (arrangementId == 0) {
            statement = con.prepareStatement(volarge_query);
            statement.setInt(1, vorlageId);
            rs = statement.executeQuery();
            if (rs != null && rs.next()) {
                 logger.info("NoOfPersons :: "+rs.getInt("personen"));
                   logger.info("Type of Accomodation :: "+rs.getInt("naechte"));
                addItemRQ.setNoOfPersons(rs.getInt("personen"));
                addItemRQ.setAccomadation(rs.getInt("naechte"));
            }
        } else {
            statement = con.prepareStatement(ebay_product_query);
            statement.setInt(1, arrangementId);
            rs = statement.executeQuery();
            if (rs != null && rs.next()) {
                logger.info("NoOfPersons :: "+rs.getInt("standardOccupancy"));
                   logger.info("Type of Accomodation :: "+rs.getInt("lengthOfStay"));
                addItemRQ.setNoOfPersons(rs.getInt("standardOccupancy"));
                addItemRQ.setAccomadation(rs.getInt("lengthOfStay"));

            }
        }
    }

    /**
     * This method is used to insert the request,response fields into ZuzzuPushWSLog table
     * @param zuzzuitemId
     * @param response
     * @param perfTime
     * @param ebayItemId
     * @param request
     * @param status 
     */
    public void insertZuzzuResponse(String zuzzuitemId, String response, long perfTime, String ebayItemId, String request, int status) {
       
        manager.insertIntoZuzzuPushLog(zuzzuitemId, ebayItemId, request, response, 1,status, perfTime);
    }

   
    public ResultSet getRs() {
        return rs;
    }

    public void setRs(ResultSet rs) {
        this.rs = rs;
    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }

    public PreparedStatement getStatement() {
        return statement;
    }

    public void setStatement(PreparedStatement statement) {
        this.statement = statement;
    }

}
