package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.model.Product;

import java.util.List;

public class ProductRepository {
    public void save(Product product){

    }

    public boolean existsById(int id){
        return true;
    }

    public boolean existsByName(String name){return true; }

    public Product findById(int id){

        return product;
    }

    public Product findByName(String name){

        return product;
    }

    public void update(Product product){

    }

    public void delete(int product_id){

    }

    public List<Product> findAll(){
        return List<Product>;
    }
}
