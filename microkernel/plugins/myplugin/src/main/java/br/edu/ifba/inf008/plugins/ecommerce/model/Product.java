package br.edu.ifba.inf008.plugins.ecommerce.model;

public class Product {
    private int product_id;
    private String sku;
    private String name;
    private String description;
    private double price;
    private int stock;

    public Product(int product_id, String sku, String name, String description, double price, int stock) {
        this.product_id = product_id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void increaseStock(int quantity){
        stock += quantity;
    }

    public void decreaseStock(int quantity){
        stock -= quantity;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public boolean hasStock(int quantity){
        return quantity <= getStock();
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

}
