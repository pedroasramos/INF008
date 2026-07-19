package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.exception.RepositoryException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImp implements ProductRepository{

    private static final String SELECT_WITH_STOCK =
            "SELECT p.id, p.sku, p.name, p.description, p.unit_price, " +
            "COALESCE(SUM(CASE WHEN sm.movement_type = 'INBOUND' THEN sm.quantity " +
            "                   WHEN sm.movement_type IN ('OUTBOUND', 'RESERVED') THEN -sm.quantity " +
            "                   ELSE 0 END), 0) AS stock " +
            "FROM products p LEFT JOIN stock_movements sm ON sm.product_id = p.id ";

    private static final String GROUP_BY = "GROUP BY p.id, p.sku, p.name, p.description, p.unit_price";

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products (sku, name, description, unit_price) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            int generatedId;
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, product.getSku());
                stmt.setString(2, product.getName());
                stmt.setString(3, product.getDescription());
                stmt.setDouble(4, product.getPrice());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        product.setProduct_id(generatedId);
                    } else {
                        throw new SQLException("Failed to retrieve the generated product ID.");
                    }
                }
            }

            if (product.getStock() > 0) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO stock_movements (product_id, movement_type, quantity, reason) VALUES (?, 'INBOUND', ?, ?)")) {
                    stmt.setInt(1, generatedId);
                    stmt.setInt(2, product.getStock());
                    stmt.setString(3, "Initial stock");
                    stmt.executeUpdate();
                }
            }

            conn.commit();

        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RepositoryException("Error saving product", e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public boolean existsById(int product_id) {
        String sql = "SELECT 1 FROM products WHERE id = ? LIMIT 1";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, product_id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error verifying product existence by ID", e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM products WHERE name = ? LIMIT 1";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error checking for product existence by name", e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("sku"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("unit_price"),
                rs.getInt("stock")
        );
    }

    @Override
    public Product findById(int product_id) {
        String sql = SELECT_WITH_STOCK + "WHERE p.id = ? " + GROUP_BY;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, product_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error searching for product by ID", e);
        }
    }

    @Override
    public Product findByName(String name) {
        String sql = SELECT_WITH_STOCK + "WHERE p.name = ? " + GROUP_BY;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error retrieving product by name", e);
        }
    }

    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET sku = ?, name = ?, description = ?, unit_price = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getSku());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getProduct_id());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Product with ID " + product.getProduct_id() + " not found.");
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error updating product", e);
        }
    }

    @Override
    public void registerStockMovement(int product_id, String movementType, int quantity, String reason) {
        String sql = "INSERT INTO stock_movements (product_id, movement_type, quantity, reason) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, product_id);
            stmt.setString(2, movementType);
            stmt.setInt(3, quantity);
            stmt.setString(4, reason);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Error registering stock movement", e);
        }
    }

    @Override
    public void delete(int product_id) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM stock_movements WHERE product_id = ?")) {
                stmt.setInt(1, product_id);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
                stmt.setInt(1, product_id);
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Product with ID " + product_id + " not found.");
                }
            }

            conn.commit();

        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RepositoryException("Error deleting product", e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = SELECT_WITH_STOCK + GROUP_BY;
        List<Product> products = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error retrieving all products", e);
        }

        return products;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                throw new RepositoryException("Error reverting transaction", e);
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                throw new RepositoryException("Error closing connection", e);
            }
        }
    }
}
