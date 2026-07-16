package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.model.Product;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepositoryImp;

import java.util.List;

public class ProductService {

    private ProductRepositoryImp repository;

    public ProductService(ProductRepositoryImp repository) {
        this.repository = repository;
    }

    public void save(Product product){
        if(existsByName(product.getName())){
            throw new IllegalArgumentException("Existing Product");
        }
        repository.save(product);
    }

    private boolean existsById(int id){
        return repository.existsById(id);
    }

    private boolean existsByName(String name){
        return repository.existsByName(name);
    }

    public Product findById(int id){
        Product product = repository.findById(id);
        return product;
    }

    public Product findByName(String product_name){
        return repository.findByName(product_name);
    }

    public void update(Product product){
        if(!existsById(product.getProduct_id())){
            throw new IllegalArgumentException("Product not found");
        }
        repository.update(product);
    }

    public void remove(int product_id){
        if(!existsById(product_id)){
            throw new IllegalArgumentException("Product not found");
        }
        repository.delete(product_id);
    }

    public List<Product> findAll(){
        return repository.findAll();
    }
}
