package com.pharmacare.app.model;

public class Product {
  private Integer productId;
  private String name;
  private double unitPrice;
  private int stock;
  private boolean active;

  public Product(){}
  public Product(Integer id, String name, double price, int stock, boolean active){
    this.productId=id; this.name=name; this.unitPrice=price; this.stock=stock; this.active=active;
  }

  public Integer getProductId() { return productId; }
  public void setProductId(Integer id) { this.productId = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public double getUnitPrice() { return unitPrice; }
  public void setUnitPrice(double price) { this.unitPrice = price; }
  public int getStock() { return stock; }
  public void setStock(int stock) { this.stock = stock; }
  public boolean isActive() { return active; }
  public void setActive(boolean a) { this.active = a; }
}
