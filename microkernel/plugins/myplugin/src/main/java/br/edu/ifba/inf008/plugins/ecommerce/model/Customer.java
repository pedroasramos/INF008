package br.edu.ifba.inf008.plugins.ecommerce.model;

public class Customer {
    private int customer_id;
    private String name;
    private String email;
    private String customerType;

    public Customer(String name, String email, String customerType) {
        this(0, name, email, customerType);
    }

    public Customer(int customer_id, String name, String email, String customerType) {
        this.customer_id = customer_id;
        this.name = name;
        this.email = email;
        this.customerType = customerType;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCustomerType() {
        return customerType;
    }
}
