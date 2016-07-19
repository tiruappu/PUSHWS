/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comm;

/**
 * This class is required to hold all the queries related to offer information.
 * @author sowmya
 */
public interface ZuzzuSqlQueries {
    
    
    String offer_query="SELECT auktion.id, auktion.cusebeda_objekt_id AS objectID, auktion.ebayueberschrift, auktion.anzahlgebote,"
                    + "auktion.currentbid, auktion.startpreis, auktion.ebaysiteid, DATE_ADD(auktion.startdatum, INTERVAL auktion.dauer DAY) as endDate,"
                    + "auktion.ebaysofortkauf,if(auktion.retailprice is NULL,0,auktion.retailprice) as retailprice, auktion.untertitel, auktion.endpreis, auktion.ebaysofortkauf,"
                    + "auktion.status,auktion.ebaysiteid,ebaydaten.galeriebild_id,ebaydaten.ebayname,auktion.AuctionMasterTypeID, auktion.quantity, auktion.dauer,auktion.startdatum,auktion.ebayueberschrift,auktion.ebaycountry,auktion.vorlage_id "
                    + "FROM ebay.auktion LEFT JOIN ebay.ebaydaten ON auktion.cusebeda_objekt_id = ebaydaten.cusebeda_objekt_id "
                    + "WHERE auktion.ebayitemid = ?";
    
    String geo_query=" SELECT objekt_x_geoData.objekt_id,"
                + "objekt_x_geoData.longitude, objekt_x_geoData.latitude"
                + " FROM cusebeda.objekt_x_geoData"
                + " WHERE objekt_x_geoData.objekt_id  =";
    
    
    String address_query="SELECT objekt.bezeichnung, objekt.ort as city, objekt.strasse as street, "
                + "laender.bezeichnung as land, text.text as region "
                + "FROM cusebeda.objekt, cusebeda.laender, cusebeda.verwaltungseinheiten,"
                + "cumulida.finder, cumulida.text"
                + " WHERE objekt.id= ?"
                + " AND objekt.laender_id = laender.id"
                + " AND laender.sprache_id ='"+ZuzzuSqlQueries.language+"'"
                + " AND objekt.verwaltungseinheiten_id = verwaltungseinheiten.id"
                + " AND verwaltungseinheiten.finder_id = finder.id"
                + " AND finder.text_id = text.id"
                + " AND text.cusebeda_sprache_id ='"+ZuzzuSqlQueries.language+"'";
    
    String language="2";
    
    String image_query= "SELECT request FROM ebay3.apiCallRequest WHERE uuid like ?";
    
    String percentaile="%";
    
    String sold_quantity_query="SELECT sum(quantity_purchased) as soldnumbers FROM ebay.transaction where ebayitemid = ?"; 
    
    String site_currency="SELECT ebaystammdaten.siteid.id, ebaystammdaten.currency.kurz"
                          + " FROM ebaystammdaten.currency, ebaystammdaten.siteid"
                          + " WHERE ebaystammdaten.currency.id = ebaystammdaten.siteid.currency_id"
                          + " AND siteid.id = ?";
    
    String calendar_query="SELECT IF(objekt_x_kalender.kalender = 1, 1, 0) as kalendar FROM cusebeda.objekt LEFT JOIN "
            + "cusebeda.objekt_x_kalender  ON objekt_x_kalender.cusebeda_objekt_id = objekt.id LEFT JOIN cusebeda.laender "
            + "ON objekt.laender_id = laender.id LEFT JOIN cusebeda.verwaltungseinheiten "
            + "ON objekt.verwaltungseinheiten_id =  verwaltungseinheiten.id LEFT JOIN cumulida.finder "
            + "ON verwaltungseinheiten.finder_id = finder.id LEFT JOIN cumulida.text ON finder.text_id = text.id "
            + "WHERE objekt.id =?  AND laender.sprache_id ='" + language + "'" + " "
            + "AND text.cusebeda_sprache_id ='" + language + "'";
    
    String marketplace_query="select SiteCodeType from ebay.ebaysite where id=?";
    
    String currentbid_query="select id,currentbid,anzahlgebote from ebay.auktion where ebayitemid = ?";
    
    String product_query="select v.arrangement_id from ebay.auktion ak,ebay.vorlage v where ak.vorlage_id=v.id and ak.ebayitemid=?";
    
    String volarge_query="select naechte,personen from ebay.vorlagen_arrangement where vorlage_id=? ";
    
    String ebay_product_query="select standardOccupancy,lengthOfStay from ebay_product.product where  id =?";
    
    String insert_query="insert into zuzzu.ZuzzuPushWSLog (zuzzuItemId,ebayItemID,request,response,type,status,processTime) values (?,?,?,?,?,?,?)" ;
            
}
