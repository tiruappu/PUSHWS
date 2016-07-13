/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.eoa.util;

import com.zuzzu.ZuzzuEndItemRQ;
import com.zuzzu.ZuzzuEndItemRS;
import com.zuzzu.eoa.dao.ZuzzuEoaDao;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import static java.lang.System.out;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author tirupathi
 */
@Component
public class ZuzzuEoaHelper {
    
    @Autowired
    public ZuzzuEoaDao zuzzuEoaDao;
    
    public boolean validateEoaRequest(ZuzzuEndItemRQ zuzzuEndItemRQ){
       boolean validation=false;
        boolean zuzzuChannelIdCheck=false;
        boolean zuzzueBayCheck=false;
        boolean zuzzuIdChecking=false;
        boolean ebayIdChecking=false;
        System.out.println(zuzzuEndItemRQ.getChannelId()+"==="+zuzzuEndItemRQ.getZuzzuItemId()+"==="+zuzzuEndItemRQ.getEbayItemId());
        zuzzuEoaDao.test();
        //ChannelId is mandatory
        if(zuzzuEndItemRQ.isSetChannelId() && zuzzuEndItemRQ.getChannelId() == 2){
            zuzzuChannelIdCheck=true;
           
            if(zuzzuEndItemRQ.isSetEbayItemId() && zuzzuEndItemRQ.isSetZuzzuItemId() && zuzzuEndItemRQ.getZuzzuItemId() !=null && !zuzzuEndItemRQ.getZuzzuItemId().isEmpty() &&  zuzzuEndItemRQ.getEbayItemId()!=null && !zuzzuEndItemRQ.getEbayItemId().equals("")    ){
            
               zuzzueBayCheck= zuzzuEoaDao.zuzzuEbayCheck(String.valueOf(zuzzuEndItemRQ.getEbayItemId()),zuzzuEndItemRQ.getZuzzuItemId()); 
                
            System.out.println("tiru");
           
            }else if(zuzzuEndItemRQ.getZuzzuItemId()!= null && zuzzuEndItemRQ.isSetZuzzuItemId()){
              zuzzuIdChecking = zuzzuEoaDao.zuzzuCheck(zuzzuEndItemRQ.getZuzzuItemId());
                
            }else
                ebayIdChecking =zuzzuEoaDao.ebayItemIdCheck(zuzzuEndItemRQ.getEbayItemId());
                      
        }else
        zuzzuChannelIdCheck=false;
        
        System.out.println("this is result");
        System.out.println(zuzzuChannelIdCheck+"==="+zuzzueBayCheck+"==="+zuzzuIdChecking);
        if(zuzzuChannelIdCheck && (zuzzueBayCheck || zuzzuIdChecking || ebayIdChecking))
        validation=true;
        else
            validation=false;
        
        System.out.println(validation);
        return validation;
    }
    
    public ZuzzuEndItemRS updateRequest(String zuzzuId){
        
        ZuzzuEndItemRS zuzzurs=new ZuzzuEndItemRS();
        System.out.println("before update");
        String updateStatus=zuzzuEoaDao.updateZuzzuEoa(zuzzuId);
        if(updateStatus.equals("success")){
            zuzzurs.setAck("success");
        }else{
            zuzzurs.setAck("fail");
           
        }
        
        System.out.println(zuzzurs.getAck());
        return zuzzurs;
    }
    
    public boolean sendToClient(ZuzzuEndItemRQ zuzzuEndItemRq){
        JAXBContext jaxbContext=null;
        boolean clientStatus=false;
        
         try{
            if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("com.zuzzu");
        }
        //ObjectOutputStream os = new ObjectOutputStream(out);
        StringWriter sw=new StringWriter();
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal(zuzzuEndItemRq, sw);
             System.out.println("Request"+sw.toString());
        long startTime = System.currentTimeMillis();
        URL url = new URL("http://192.168.9.161:8080/WebApplication3/NewServlet");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", sw.toString());
        conn.connect();
        conn.getResponseCode();
        System.out.println("Connection Code " + conn.getResponseCode());
        clientStatus=true;
        this.generateResponse(conn, startTime, zuzzuEndItemRq);
            }catch(Exception e){
                clientStatus=false;
                e.printStackTrace();
            } 
        
        
        return clientStatus;
    }
    
    
    private boolean generateResponse(HttpURLConnection conn, long startTime, ZuzzuEndItemRQ zuzzuendItemRQ) throws IOException {
        
        boolean clientResponse=false;
        
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
            System.out.println(response.toString());
        }
        long endTime = System.currentTimeMillis();
        long perfTime = startTime - endTime;
        if (response != null) {
            clientResponse=true;
            //addItemManger.zuzuResponse(addItemRQ, response.toString(), perfTime);
        }
        return clientResponse;
    }
    
}
