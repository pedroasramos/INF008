package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.exception.DuplicateEntityException;
import br.edu.ifba.inf008.plugins.ecommerce.exception.EntityNotFoundException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepositoryImp;

import java.util.List;

public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public void save(Product product) {
        if (existsByName(product.getName())) {
            throw new DuplicateEntityException("There is already a product with that name: " + product.getName());
        }
        repository.save(product);
    }

    private boolean existsById(int id) {
        return repository.existsById(id);
    }

    private boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    public Product findById(int id) {
        Product product = repository.findById(id);
        if (product == null) {
            throw new EntityNotFoundException("Product", id);
        }
        return product;
    }

    public Product findByName(String product_name) {
        Product product = repository.findByName(product_name);
        if (product == null) {
            throw new EntityNotFoundException("Product with name " + product_name + " not found.");
        }
        return product;
    }

    public void update(Product product) {
        if (!existsById(product.getProduct_id())) {
            throw new EntityNotFoundException("Product", product.getProduct_id());
        }
        repository.update(product);
    }

    public void remove(int product_id) {
        if (!existsById(product_id)) {
            throw new EntityNotFoundException("Product", product_id);
        }
        repository.delete(product_id);
    }

    public List<Product> findAll() {
        return repository.findAll();
    }
}
