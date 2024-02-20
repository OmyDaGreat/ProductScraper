package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

public class Product {
    
    private String name;
    private double price;
    
    private final String url;
    
    private String img;
    
    private String domain;
    
    public Product(String url) {
        this.url = url;
        scrapeProduct(url);
    }
    
    public String toString() {
        return "========================\nProduct URL: " + url + "\nProduct image: " + img + "\nProduct's Marketplace: " + domain + "\nProduct Name: " + name + "\nPrice: " + price;
    }
    public double getPrice() {
        return price;
    }
    
    public String getName() {
        return name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getImg() {
        return img;
    }
    public void scrapeProduct(String url) {
        
        String subPriceHolder = "";
        String subPriceElement = "";
        String subNameHolder = "";
        String subNameElement = "";
        
        boolean isAmazon = false;
        boolean isEbay = false;
        
        if (this.getProductWebsite(url).equals("www.amazon.com")) {
            isAmazon = true;
            subPriceHolder = ".priceToPay.reinventPricePriceToPayMargin.aok-align-center.a-price";
            subPriceElement = "span:nth-of-type(2)";
            subNameHolder = ".product-title-word-break.a-size-large";
        } else if (this.getProductWebsite(url).equals("www.ebay.com")) {
            isEbay = true;
            subPriceHolder = ".x-price-primary";
            subPriceElement = ".ux-textspans";
            subNameHolder = ".x-item-title__mainTitle";
            subNameElement = ".ux-textspans--BOLD.ux-textspans";
        }
        
        try {
            Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; Windows NT 10.5;) Gecko/20100101 Firefox/65.6").get();
            
            Elements priceHolder = document.select(subPriceHolder);
            String priceElement = priceHolder.select(subPriceElement).text();
            
            Elements nameHolder = document.select(subNameHolder);
            
            if (isAmazon) {
                Element imageHolder = document.getElementById("imgTagWrapperId");
                
                assert imageHolder != null;
                Elements imageTag = imageHolder.getElementsByTag("img");
                img = imageTag.attr("src");
                name = imageTag.attr("alt");
                
                this.setAmazonElements(priceElement);
            } else if (isEbay) {
                String nameElement = nameHolder.select(subNameElement).text();
                
                Elements imageHolder = document.select(".image.active.image-treatment.ux-image-carousel-item");
                Elements imageTag = Objects.requireNonNull(imageHolder.first()).getElementsByTag("img");
                
                img = imageTag.attr("src");
                
                this.setEbayElements(priceElement, nameElement);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getProductWebsite(String url) {
        StringBuilder domain2 = new StringBuilder();
        int slashCount = 0;
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == '/') {
                if (slashCount == 2) {
                    break;
                }
                slashCount++;
                domain2.append(url.charAt(i));
                continue;
            }
            
            if (slashCount == 2) {
                domain2.append(url.charAt(i));
            }
            
        }
        
        switch (domain2.toString()) {
            case "//www.amazon.com":
                this.domain = "www.amazon.com";
                return "www.amazon.com";
            case "//www.ebay.com":
                this.domain = "www.ebay.com";
                return "www.ebay.com";
            default:
                return "What are you even doing?";
        }
    }
    
    public void setAmazonElements(String priceElement) {
        StringBuilder finalPrice = new StringBuilder();
        for (int i = 0; i < priceElement.length(); i++) {
            if (priceElement.charAt(i) == '$') {
                continue;
            }
            if (priceElement.charAt(i) == ' ') {
                break;
            }
            finalPrice.append(priceElement.charAt(i));
        }
        
        price = Double.parseDouble(finalPrice.toString());
    }
    
    public void setEbayElements(String priceElement, String nameElement) {
        StringBuilder finalPrice = new StringBuilder();
        boolean isPrice = false;
        for (int i = 0; i < priceElement.length(); i++) {
            
            if (priceElement.charAt(i) == '$') {
                isPrice = true;
                continue;
            }
            if (isPrice) {
                finalPrice.append(priceElement.charAt(i));
            }
        }
        
        name = nameElement;
        price = Double.parseDouble(finalPrice.toString());
    }
}