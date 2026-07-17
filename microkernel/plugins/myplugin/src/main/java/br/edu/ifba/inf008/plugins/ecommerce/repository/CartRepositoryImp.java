package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.exception.RepositoryException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.model.CartItem;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;

import java.sql.PreparedStatement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartRepositoryImp implements CartRepository{

    private final ProductRepository productRepository;

    public CartRepositoryImp(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void save(Cart cart) {
        String sqlCart = "INSERT INTO carts () VALUES ()";
        String sqlItem = "INSERT INTO cart_items (cart_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            int generatedCartId;

            try (PreparedStatement stmt = conn.prepareStatement(sqlCart, Statement.RETURN_GENERATED_KEYS)) {
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedCartId = rs.getInt(1);
                        cart.setCart_id(generatedCartId);
                    } else {
                        throw new SQLException("Failed to retrieve the generated cart ID.");
                    }
                }
            }

            insertItems(conn, sqlItem, generatedCartId, cart.getItems());

            conn.commit();

        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RepositoryException("Error saving cart", e);

        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void update(Cart cart) {
        String sqlDeleteItems = "DELETE FROM cart_items WHERE cart_id = ?";
        String sqlItem = "INSERT INTO cart_items (cart_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteItems)) {
                stmt.setInt(1, cart.getCart_id());
                stmt.executeUpdate();
            }

            insertItems(conn, sqlItem, cart.getCart_id(), cart.getItems());

            conn.commit();

        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RepositoryException("Error updating cart", e);

        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void delete(int cart_id) {
        String sql = "DELETE FROM carts WHERE cart_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cart_id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Cart with ID " + cart_id + " not found.");
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error deleting cart", e);
        }
    }

    @Override
    public Cart findById(int cart_id) {
        String sqlCart = "SELECT cart_id FROM carts WHERE cart_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlCart)) {

            stmt.setInt(1, cart_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Cart cart = new Cart();
                cart.setCart_id(rs.getInt("cart_id"));

                for (CartItem item : findItemsByCartId(conn, cart_id)) {
                    cart.addProduct(item.getProduct(), item.getQuantity());
                }

                return cart;
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error searching for cart by ID", e);
        }
    }

    private void insertItems(Connection conn, String sqlItem, int cartId, List<CartItem> items) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sqlItem)) {
            for (CartItem item : items) {
                stmt.setInt(1, cartId);
                stmt.setInt(2, item.getProduct().getProduct_id());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private List<CartItem> findItemsByCartId(Connection conn, int cartId) throws SQLException {
        String sql = "SELECT product_id, quantity FROM cart_items WHERE cart_id = ?";
        List<CartItem> items = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = productRepository.findById(rs.getInt("product_id"));
                    items.add(new CartItem(product, rs.getInt("quantity")));
                }
            }
        }

        return items;
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
