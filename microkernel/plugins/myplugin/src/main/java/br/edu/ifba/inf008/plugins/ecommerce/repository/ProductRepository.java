package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.model.Product;

import java.util.List;

public interface ProductRepository {
    public void save(Product product);
    public boolean existsById(int id);
    public boolean existsByName(String name);
    public Product findById(int id);
    public Product findByName(String name);
    public void update(Product product);
    public void delete(int product_id);
    public List<Product> findAll();
    public void registerStockMovement(int product_id, String movementType, int quantity, String reason);
}
