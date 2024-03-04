package org.example;

import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Product {

  private String name;
  private double price = -1;

  private String url;

  private String img;

  private String domain;

  private final String amazonPriceHolder =
    ".priceToPay.reinventPricePriceToPayMargin.aok-align-center.a-price";
  private final String amazonPriceElement = "span:nth-of-type(2)";

  private final String amazonNameHolder =
    ".product-title-word-break.a-size-large";
  private final String ebayPriceHolder = ".x-price-primary";
  private final String ebayPriceElement = ".ux-textspans";

  private final String ebayNameHolder = ".x-item-title__mainTitle";
  private final String ebayNameElement = ".ux-textspans--BOLD.ux-textspans";

  public Product(String url) {
    this.url = url;
    scrapeProduct(url);
  }

  public String toString() {
    return (
      "========================\nProduct URL: " +
      url +
      "\nProduct image: " +
      img +
      "\nProduct's Marketplace: " +
      domain +
      "\nProduct Name: " +
      name +
      "\nPrice: " +
      price
    );
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
    String productURL = url;

    String subPriceHolder = "";
    String subPriceElement = "";
    String subNameHolder = "";
    String subNameElement = "";

    boolean isAmazon = false;
    boolean isEbay = false;

    if (this.getProductWebsite(url).equals("www.amazon.com")) {
      isAmazon = true;
      subPriceHolder = amazonPriceHolder;
      subPriceElement = amazonPriceElement;
      subNameHolder = amazonNameHolder;
    } else if (this.getProductWebsite(url).equals("www.ebay.com")) {
      isEbay = true;
      subPriceHolder = ebayPriceHolder;
      subPriceElement = ebayPriceElement;
      subNameHolder = ebayNameHolder;
      subNameElement = ebayNameElement;
    }

    try {
      Document document = Jsoup.connect(productURL)
        .userAgent(RandomUserAgent.getRandomUserAgent())
        .get();

      Elements itemsHolder = document.select(
        ".recs-item-list-padding.recs-col-items-1.recs-item-list-container"
      );
      System.out.println(itemsHolder.outerHtml());

      Elements priceHolder = document.select(subPriceHolder);
      String priceElement = priceHolder.select(subPriceElement).text();
      System.out.println(priceHolder.select(subPriceElement).outerHtml());
      Elements nameHolder = document.select(subNameHolder);

      // ArrayList<String> urls = new ArrayList<>();
      // Elements urlsHolder = document.getElementsByTag("a");
      //  for (Element link : urlsHolder) {
      //     if (!urls.contains(link.attr("abs:href"))) {
      //         urls.add(link.attr("abs:href"));
      //     }
      //     System.out.println(link.attr("abs:href"));
      //  }

      String nameElement = "";

      if (isAmazon) {
        if (priceElement.equals("")) {
          Elements newPriceHolder = document.select(
            ".apexPriceToPay.a-size-medium.a-text-price.a-price"
          );
          priceElement = newPriceHolder.select("span:nth-of-type(2)").text();
        }
        nameElement = String.valueOf(nameHolder);

        Element imageHolder = document.getElementById("imgTagWrapperId");
        Elements imageTag = imageHolder.getElementsByTag("img");
        img = imageTag.attr("src");
        name = imageTag.attr("alt");

        this.setAmazonElements(priceElement);
      } else if (isEbay) {
        nameElement = nameHolder.select(subNameElement).text();

        Elements imageHolder = document.select(
          ".image.active.image-treatment.ux-image-carousel-item"
        );
        Elements imageTag = imageHolder.first().getElementsByTag("img");

        img = imageTag.attr("src");

        this.setEbayElements(priceElement, nameElement);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getProductWebsite(String url) {
    String domain = "";
    int slashCount = 0;
    for (int i = 0; i < url.length(); i++) {
      if (url.substring(i, i + 1).equals("/")) {
        if (slashCount == 2) {
          break;
        }
        slashCount++;
        domain += url.substring(i, i + 1);
        continue;
      }

      if (slashCount == 2) {
        domain += url.substring(i, i + 1);
      }
    }

    switch (domain) {
      case "//www.amazon.com":
        this.domain = "www.amazon.com";
        return "www.amazon.com";
      case "//www.ebay.com":
        this.domain = "www.ebay.com";
        return "www.ebay.com";
    }
    return null;
  }

  public void setAmazonElements(String priceElement) {
    String finalPrice = "";
    for (int i = 0; i < priceElement.length(); i++) {
      if (priceElement.substring(i, i + 1).equals("$")) {
        continue;
      }
      if (priceElement.substring(i, i + 1).equals(" ")) {
        break;
      }
      finalPrice += priceElement.substring(i, i + 1);
    }

    System.out.println(finalPrice);

    price = Double.parseDouble(finalPrice);
  }

  public void setEbayElements(String priceElement, String nameElement) {
    String finalPrice = "";
    boolean isPrice = false;
    for (int i = 0; i < priceElement.length(); i++) {
      if (priceElement.substring(i, i + 1).equals("$")) {
        isPrice = true;
        continue;
      }
      if (isPrice) {
        finalPrice += priceElement.substring(i, i + 1);
      }
    }

    name = nameElement;
    price = Double.parseDouble(finalPrice);
  }

  public ArrayList<String> findProduct(String url, int depth, boolean isAmazon)
    throws IOException, InterruptedException {
    ArrayList<String> links = new ArrayList<>();
    if (depth == 0) {
      return null;
    }

    Document doc = Jsoup.connect(url)
      .userAgent(RandomUserAgent.getRandomUserAgent())
      .get();
    if (!isAmazon) {
      Elements itemsHolder = doc.select(
        ".recs-item-list-padding.recs-col-items-1.recs-item-list-container"
      );
      System.out.println(itemsHolder.html());
      for (Element item : itemsHolder) {
        String realURL = item.attr("abs:href");

        if (realURL != "") {
          Product temp = new Product(realURL);
          temp.scrapeProduct(realURL);

          if (temp.getPrice() < this.getPrice()) {
            links.add(temp.getUrl());
            findProduct(realURL, depth--, isAmazon);
          }
        }
      }
    }

    return links;
  }
}
