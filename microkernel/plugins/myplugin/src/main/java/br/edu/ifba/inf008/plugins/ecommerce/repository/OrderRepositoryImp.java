package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.StudentDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.exception.RepositoryException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Order;
import br.edu.ifba.inf008.plugins.ecommerce.model.OrderItem;
import br.edu.ifba.inf008.plugins.ecommerce.model.OrderStatus;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicyFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists orders against the support database schema (orders/order_items/payments/order_discounts).
 * That schema does not store the concrete discount coupon or payment instrument details, so on
 * read-back {@code discountPolicy} and {@code paymentMethod} are not reconstructed: the frozen
 * subtotal/discount/shippingCost/total/status columns are the source of truth for historical orders.
 */
public class OrderRepositoryImp implements OrderRepository{

    private final ProductRepository productRepository;

    public OrderRepositoryImp(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void save(Order order) {
        String sqlOrder = "INSERT INTO orders (customer_id, cart_id, shipping_method_id, status, " +
                "subtotal, discount_total, shipping_total, grand_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlItem = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) " +
                "VALUES (?, ?, ?, ?, ?)";

        String sqlPayment = "INSERT INTO payments (order_id, payment_method, status, amount, failure_reason, paid_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            int shippingMethodId = resolveShippingMethodId(conn, order.getShippingPolicy());
            int generatedOrderId;

            try (PreparedStatement stmt = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getCustomerId());
                if (order.getCartId() != null) {
                    stmt.setInt(2, order.getCartId());
                } else {
                    stmt.setNull(2, Types.BIGINT);
                }
                stmt.setInt(3, shippingMethodId);
                stmt.setString(4, order.getStatus().name());
                stmt.setDouble(5, order.getSubtotal());
                stmt.setDouble(6, order.getDiscount());
                stmt.setDouble(7, order.getShippingCost());
                stmt.setDouble(8, order.getTotal());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedOrderId = rs.getInt(1);
                        order.setOrder_id(generatedOrderId);
                    } else {
                        throw new SQLException("Failed to retrieve the generated order ID.");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlItem)) {
                for (OrderItem item : order.getOrderItems()) {
                    stmt.setInt(1, generatedOrderId);
                    stmt.setInt(2, item.getProduct().getProduct_id());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getUnitPrice());
                    stmt.setDouble(5, item.calculateSubtotal());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlPayment)) {
                stmt.setInt(1, generatedOrderId);
                stmt.setString(2, PolicyTypeResolver.resolvePaymentType(order.getPaymentMethod()));
                String paymentStatus = toPaymentStatus(order.getStatus());
                stmt.setString(3, paymentStatus);
                stmt.setDouble(4, order.getTotal());
                if (order.getStatus() == OrderStatus.INVALID_PAYMENT || order.getStatus() == OrderStatus.CANCELLED) {
                    stmt.setString(5, "Payment could not be confirmed: invalid payment data or state.");
                } else {
                    stmt.setNull(5, Types.VARCHAR);
                }
                if (order.getStatus() == OrderStatus.PAID) {
                    stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                } else {
                    stmt.setNull(6, Types.TIMESTAMP);
                }
                stmt.executeUpdate();
            }

            linkDiscountIfKnown(conn, order);

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RepositoryException("Error reverting transaction", rollbackEx);
                }
            }
            throw new RepositoryException("Error saving order", e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    throw new RepositoryException("Error closing connection", closeEx);
                }
            }
        }
    }

    @Override
    public Order find(int order_id) {
        String sqlOrder = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlOrder)) {

            stmt.setInt(1, order_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                List<OrderItem> items = findItemsByOrderId(conn, order_id);
                return mapRow(conn, rs, items);
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error searching for order by ID", e);
        }
    }

    @Override
    public void update(Order order) {
        String sql = "UPDATE orders SET status = ?, subtotal = ?, discount_total = ?, " +
                "shipping_total = ?, grand_total = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, order.getStatus().name());
            stmt.setDouble(2, order.getSubtotal());
            stmt.setDouble(3, order.getDiscount());
            stmt.setDouble(4, order.getShippingCost());
            stmt.setDouble(5, order.getTotal());
            stmt.setInt(6, order.getOrder_id());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Order with ID " + order.getOrder_id() + " not found.");
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error updating order", e);
        }
    }

    @Override
    public List<Order> findAll() {
        String sqlOrder = "SELECT * FROM orders";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlOrder);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int orderId = rs.getInt("id");
                List<OrderItem> items = findItemsByOrderId(conn, orderId);
                orders.add(mapRow(conn, rs, items));
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error retrieving all orders", e);
        }

        return orders;
    }

    @Override
    public void delete(int order_id) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM order_discounts WHERE order_id = ?")) {
                stmt.setInt(1, order_id);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM payments WHERE order_id = ?")) {
                stmt.setInt(1, order_id);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                stmt.setInt(1, order_id);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM orders WHERE id = ?")) {
                stmt.setInt(1, order_id);
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Order with ID " + order_id + " not found.");
                }
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RepositoryException("Error reverting transaction", rollbackEx);
                }
            }
            throw new RepositoryException("Error deleting order", e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    throw new RepositoryException("Error closing connection", closeEx);
                }
            }
        }
    }

    private List<OrderItem> findItemsByOrderId(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT product_id, quantity, unit_price FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = productRepository.findById(rs.getInt("product_id"));
                    items.add(new OrderItem(
                            product,
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    ));
                }
            }
        }

        return items;
    }

    private Order mapRow(Connection conn, ResultSet rs, List<OrderItem> orderItems) throws SQLException {
        ShippingPolicy shippingPolicy = resolveShippingPolicy(conn, rs.getInt("shipping_method_id"));

        Order order = new Order(
                rs.getInt("id"),
                orderItems,
                null,
                shippingPolicy,
                null,
                OrderStatus.valueOf(rs.getString("status")),
                rs.getDouble("subtotal"),
                rs.getDouble("discount_total"),
                rs.getDouble("shipping_total"),
                rs.getDouble("grand_total")
        );
        order.setCustomerId(rs.getInt("customer_id"));
        int cartId = rs.getInt("cart_id");
        order.setCartId(rs.wasNull() ? null : cartId);
        return order;
    }

    private int resolveShippingMethodId(Connection conn, ShippingPolicy policy) throws SQLException {
        String code = PolicyTypeResolver.resolveShippingType(policy);
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM shipping_methods WHERE code = ?")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                throw new SQLException("Shipping method not found for code: " + code);
            }
        }
    }

    private ShippingPolicy resolveShippingPolicy(Connection conn, int shippingMethodId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT code FROM shipping_methods WHERE id = ?")) {
            stmt.setInt(1, shippingMethodId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return ShippingPolicyFactory.fromType(rs.getString("code"));
                }
                return null;
            }
        }
    }

    /**
     * Links the order to a matching row in {@code discounts} for audit purposes, when one exists.
     * Coupons that do not match a known discount code are still fully applied on the order
     * (discount_total already carries the amount); the link is best-effort supplementary data.
     */
    private void linkDiscountIfKnown(Connection conn, Order order) throws SQLException {
        if (order.getDiscountPolicy() == null || order.getDiscount() <= 0) {
            return;
        }

        String code;
        if (order.getDiscountPolicy() instanceof CouponDiscountPolicy) {
            code = ((CouponDiscountPolicy) order.getDiscountPolicy()).getCoupon();
        } else if (order.getDiscountPolicy() instanceof StudentDiscountPolicy) {
            code = "STUDENT15";
        } else {
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM discounts WHERE code = ?")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                int discountId = rs.getInt("id");
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO order_discounts (order_id, discount_id, amount) VALUES (?, ?, ?)")) {
                    insert.setInt(1, order.getOrder_id());
                    insert.setInt(2, discountId);
                    insert.setDouble(3, order.getDiscount());
                    insert.executeUpdate();
                }
            }
        }
    }

    private String toPaymentStatus(OrderStatus status) {
        switch (status) {
            case PAID: return "PAID";
            case PENDING: return "PENDING";
            case INVALID_PAYMENT: return "INVALID";
            case CANCELLED: return "FAILED";
            default: return status.name();
        }
    }
}
