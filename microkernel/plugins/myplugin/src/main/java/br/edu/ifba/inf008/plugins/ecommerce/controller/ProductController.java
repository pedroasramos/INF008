package br.edu.ifba.inf008.plugins.ecommerce.controller;

import br.edu.ifba.inf008.plugins.ecommerce.model.Product;
import br.edu.ifba.inf008.plugins.ecommerce.service.ProductService;

import java.util.List;

public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    public void createProduct(String name, String description, double price, int stock) {
        Product product = new Product(0, name, description, price, stock);
        productService.save(product);
    }

    public Product getProduct(int productId) {
        return productService.findById(productId);
    }

    public Product getProductByName(String name) {
        return productService.findByName(name);
    }

    public List<Product> listProducts() {
        return productService.findAll();
    }

    public void updateProduct(int productId, String name, String description, double price, int stock) {
        Product product = new Product(productId, name, description, price, stock);
        productService.update(product);
    }

    public void deleteProduct(int productId) {
        productService.remove(productId);
    }
}
