package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicyFactory;
import br.edu.ifba.inf008.plugins.ecommerce.exception.RepositoryException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Order;
import br.edu.ifba.inf008.plugins.ecommerce.model.OrderItem;
import br.edu.ifba.inf008.plugins.ecommerce.model.OrderStatus;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;
import br.edu.ifba.inf008.plugins.ecommerce.payment.*;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicyFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepositoryImp implements OrderRepository{

    private final ProductRepository productRepository;

    public OrderRepositoryImp(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void save(Order order) {
        String sqlOrder = "INSERT INTO orders (discount_policy_type, discount_coupon, " +
                "shipping_policy_type, payment_method_type, payment_barcode, payment_due_date, " +
                "payment_card_number, payment_card_holder, payment_card_expiration, payment_pix_key, " +
                "status, subtotal, discount, shipping_cost, total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlItem = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            int generatedOrderId;

            try (PreparedStatement stmt = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                String discountType = PolicyTypeResolver.resolveDiscountType(order.getDiscountPolicy());
                String shippingType = PolicyTypeResolver.resolveShippingType(order.getShippingPolicy());
                String paymentType = PolicyTypeResolver.resolvePaymentType(order.getPaymentMethod());

                stmt.setString(1, discountType);
                stmt.setString(2, discountType.equals("COUPON")
                        ? ((CouponDiscountPolicy) order.getDiscountPolicy()).getCoupon() : null);

                stmt.setString(3, shippingType);
                stmt.setString(4, paymentType);

                if (paymentType.equals("BANK_SLIP")) {
                    BankSlipPayment p = (BankSlipPayment) order.getPaymentMethod();
                    stmt.setString(5, p.getBarcode());
                    stmt.setString(6, p.getDueDate());
                } else {
                    stmt.setNull(5, Types.VARCHAR);
                    stmt.setNull(6, Types.VARCHAR);
                }

                if (paymentType.equals("CREDIT_CARD")) {
                    CreditCardPayment p = (CreditCardPayment) order.getPaymentMethod();
                    stmt.setLong(7, p.getCardNumber());
                    stmt.setString(8, p.getHolder());
                    stmt.setString(9, p.getExpirationDate());
                } else {
                    stmt.setNull(7, Types.BIGINT);
                    stmt.setNull(8, Types.VARCHAR);
                    stmt.setNull(9, Types.VARCHAR);
                }

                stmt.setString(10, paymentType.equals("PIX")
                        ? ((PixPayment) order.getPaymentMethod()).getPixKey() : null);

                stmt.setString(11, order.getStatus().name());
                stmt.setDouble(12, order.getSubtotal());
                stmt.setDouble(13, order.getDiscount());
                stmt.setDouble(14, order.getShippingCost());
                stmt.setDouble(15, order.getTotal());

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
                    stmt.addBatch();
                }
                stmt.executeBatch();
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
        String sqlOrder = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlOrder)) {

            stmt.setInt(1, order_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                List<OrderItem> items = findItemsByOrderId(conn, order_id);
                return mapRow(rs, items);
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error searching for order by ID", e);
        }
    }

    @Override
    public void update(Order order) {
        String sql = "UPDATE orders SET status = ?, subtotal = ?, discount = ?, " +
                "shipping_cost = ?, total = ? WHERE order_id = ?";

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
                List<OrderItem> items = findItemsByOrderId(conn, rs.getInt("order_id"));
                orders.add(mapRow(rs, items));
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error retrieving all orders", e);
        }

        return orders;
    }

    @Override
    public void delete(int order_id) {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order_id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Order with ID " + order_id + " not found.");
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error deleting order", e);
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

    private Order mapRow(ResultSet rs, List<OrderItem> orderItems) throws SQLException {
        DiscountPolicy discountPolicy = DiscountPolicyFactory.fromRow(
                rs.getString("discount_policy_type"), rs.getString("discount_coupon"));

        ShippingPolicy shippingPolicy = ShippingPolicyFactory.fromType(rs.getString("shipping_policy_type"));

        Long cardNumber = rs.getObject("payment_card_number") != null ? rs.getLong("payment_card_number") : null;

        Payable paymentMethod = PayableFactory.fromRow(
                rs.getString("payment_method_type"),
                rs.getString("payment_barcode"),
                rs.getString("payment_due_date"),
                cardNumber,
                rs.getString("payment_card_holder"),
                rs.getString("payment_card_expiration"),
                rs.getString("payment_pix_key"));

        return new Order(
                rs.getInt("order_id"),
                orderItems,
                discountPolicy,
                shippingPolicy,
                paymentMethod,
                OrderStatus.valueOf(rs.getString("status")),
                rs.getDouble("subtotal"),
                rs.getDouble("discount"),
                rs.getDouble("shipping_cost"),
                rs.getDouble("total")
        );
    }
}
