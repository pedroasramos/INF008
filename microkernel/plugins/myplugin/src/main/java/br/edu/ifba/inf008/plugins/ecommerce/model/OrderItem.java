package br.edu.ifba.inf008.plugins.ecommerce.model;

public class OrderItem {
    private Product product;
    private int quantity;
    private double unitPrice;

    public OrderItem(Product product, int quantity, double unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double calculateSubtotal(){
        return unitPrice * quantity;
    }
}
