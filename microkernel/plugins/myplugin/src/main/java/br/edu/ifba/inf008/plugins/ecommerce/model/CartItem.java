package br.edu.ifba.inf008.plugins.ecommerce.model;

public class CartItem {
    private Product product;
    private int quantity;
    private double price;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
    }

    public double calculateSubtotal(){
        return product.getPrice() * quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public double getPrice() {
        return price;
    }
}
