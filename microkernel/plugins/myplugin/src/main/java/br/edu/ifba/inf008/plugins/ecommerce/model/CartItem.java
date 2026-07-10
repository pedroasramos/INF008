package br.edu.ifba.inf008.plugins.ecommerce.model;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double calculateSubtotal(){
        return product.getPrice() * quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}
