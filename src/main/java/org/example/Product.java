package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Product {
    
    private String name;
    private double price;
    
    public Product(String url) {
        scrapeProduct(url);
    }
    
    public String toString() {
        return "========================\nProduct Name: " + name + "\nPrice: " + price;
    }
    public double getPrice() {
        return price;
    }
    
    public String getName() {
        return name;
    }
    public void scrapeProduct(String url) {
        
        try {
            Document document = Jsoup.connect(url).get();
            
            Elements priceHolder = document.select(".priceToPay.reinventPricePriceToPayMargin.aok-align-center.a-price");
            String priceElement = priceHolder.select("span:nth-of-type(2)").text();
            
            Elements nameHolder = document.select(".product-title-word-break.a-size-large");
            
            String nameElement = String.valueOf(nameHolder);
            
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
            
            StringBuilder finalName = new StringBuilder();
            boolean isName = false;
            boolean firstSpace = false;
            
            for (int x = 0; x < nameElement.length(); x++) {
                if (nameElement.charAt(x) == '>') {
                    isName = true;
                    continue;
                }
                
                if (nameElement.charAt(x) == '<' && x != 0) {
                    break;
                }
                
                if (nameElement.charAt(x) == ' ' && isName && !firstSpace) {
                    firstSpace = true;
                    continue;
                    
                }
                
                if (isName && firstSpace) {
                    finalName.append(nameElement.charAt(x));
                }
                
            }
            
            name = finalName.toString();
            price = Double.parseDouble(finalPrice.toString());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}