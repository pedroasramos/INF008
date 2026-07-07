package br.edu.ifba.inf008;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int code_product;
    private String name;
    private String description;
    private double price;
    private int stock;

    private List<Product> products = new ArrayList<>();

    public Product(){};

    public Product(int code_product, String name, String description, double price, int stock) {
        this.code_product = code_product;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void register(String name, double price, String description, int stock, int code_product){
        products.add(new Product(code_product, name, description, price, stock));
    }

    public void list(){
        int size = products.size();
        int i = 0;
        while(i < size){
            System.out.println(products.get(i));
            i++;
        }
    }

    public Product search(int code_product){
        int index = products.indexOf(code_product);
        return products.get(index);
    }

    public Product search(String name){
        int index = products.indexOf(name);
        return products.get(index);
    }

    public int getCode_product() {
        return code_product;
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

    @Override
    public String toString() {
        return "Product{" +
                "code_product=" + code_product +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", products=" + products +
                '}';
    }
}
