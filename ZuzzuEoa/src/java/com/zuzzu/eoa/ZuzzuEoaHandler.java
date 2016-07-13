/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zuzzu.eoa;


import com.zuzzu.ZuzzuEndItemRQ;
import com.zuzzu.ZuzzuEndItemRS;
import com.zuzzu.eoa.util.ZuzzuEoaHelper;
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
@RequestMapping(value="/zuzzu")
public class ZuzzuEoaHandler {
   
    @Autowired
    private ZuzzuEoaHelper zuzzuEoaHelper;
    
    //JAXBContext jaxbContext=null;
    
    
    @RequestMapping(value ="/zuzzueoa", method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ZuzzuEndItemRS eoaReqHandle(@RequestBody ZuzzuEndItemRQ zuzzuEndItemRq){
        
        
            ZuzzuEndItemRS zuzzuRes=null;
        System.out.println("its controller");
        
        
        
         if(zuzzuEoaHelper.validateEoaRequest(zuzzuEndItemRq)){
             zuzzuRes=zuzzuEoaHelper.updateRequest(zuzzuEndItemRq.getZuzzuItemId());
         }else
             System.out.println("Invalid XML");
        
         if(zuzzuRes.getAck().equals("success")){
             //Client Url
           boolean clientSendStatus=zuzzuEoaHelper.sendToClient(zuzzuEndItemRq);
           if(clientSendStatus)
             System.out.println("zuzzu updated");
           else
                 System.out.println("problem occured at sendToClient");
             
         }
         
        return zuzzuRes;
    }    

}