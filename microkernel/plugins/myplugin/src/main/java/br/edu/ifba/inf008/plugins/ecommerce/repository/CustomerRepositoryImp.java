package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.exception.RepositoryException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepositoryImp implements CustomerRepository{

    @Override
    public void save(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, customer_type) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getCustomerType());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    customer.setCustomer_id(rs.getInt(1));
                } else {
                    throw new SQLException("Failed to retrieve the generated customer ID.");
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error saving customer", e);
        }
    }

    @Override
    public boolean existsById(int customer_id) {
        String sql = "SELECT 1 FROM customers WHERE id = ? LIMIT 1";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customer_id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error checking for customer existence by ID", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM customers WHERE email = ? LIMIT 1";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error checking for customer existence by email", e);
        }
    }

    @Override
    public Customer findById(int customer_id) {
        String sql = "SELECT id, full_name, email, customer_type FROM customers WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customer_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error searching for customer by id", e);
        }
    }

    @Override
    public Customer findByEmail(String email) {
        String sql = "SELECT id, full_name, email, customer_type FROM customers WHERE email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error retrieving customer by email", e);
        }
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, email = ?, customer_type = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getCustomerType());
            stmt.setInt(4, customer.getCustomer_id());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Customer with ID " + customer.getCustomer_id() + " not found.");
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error updating customer", e);
        }
    }

    @Override
    public void delete(int customer_id) {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customer_id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Customer with ID " + customer_id + " not found.");
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error deleting customer", e);
        }
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT id, full_name, email, customer_type FROM customers";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error retrieving all customers", e);
        }

        return customers;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("customer_type")
        );
    }
}
