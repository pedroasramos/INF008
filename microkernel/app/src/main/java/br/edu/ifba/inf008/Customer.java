package br.edu.ifba.inf008;

public class Customer {
    private int customer_id;
    private String name;
    private String email;
    private String type;

    public Customer(String name, String email, String type) {
        this.name = name;
        this.email = email;
        this.type = type;
    }
}
