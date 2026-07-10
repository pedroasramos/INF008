package br.edu.ifba.inf008.plugins.ecommerce.model;

public class Product {
    private int product_id;
    private String name;
    private String description;
    private double price;
    private int stock;

    public void increaseStock(int quantity){
        stock += quantity;
    }

    public void decreaseStock(int quantity){
        stock -= quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
