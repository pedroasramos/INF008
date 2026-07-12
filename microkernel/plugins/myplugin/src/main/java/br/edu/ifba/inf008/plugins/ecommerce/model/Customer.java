package br.edu.ifba.inf008.plugins.ecommerce.model;

public class Customer {
    private int customer_id;
    private String name;
    private String email;

    public Customer(int customer_id, String name, String email) {
        this.customer_id = customer_id;
        this.name = name;
        this.email = email;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
