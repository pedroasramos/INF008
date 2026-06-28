CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    customer_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE stock_movements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    reason VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE shipping_methods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    base_cost DECIMAL(10, 2) NOT NULL,
    estimated_days INT NOT NULL,
    free_shipping_threshold DECIMAL(10, 2)
);

CREATE TABLE discounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    discount_type VARCHAR(30) NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    cart_id BIGINT,
    shipping_method_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount_total DECIMAL(10, 2) NOT NULL,
    shipping_total DECIMAL(10, 2) NOT NULL,
    grand_total DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_orders_cart
        FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT fk_orders_shipping_method
        FOREIGN KEY (shipping_method_id) REFERENCES shipping_methods (id)
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE order_discounts (
    order_id BIGINT NOT NULL,
    discount_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (order_id, discount_id),
    CONSTRAINT fk_order_discounts_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_discounts_discount
        FOREIGN KEY (discount_id) REFERENCES discounts (id)
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transaction_reference VARCHAR(80),
    failure_reason VARCHAR(160),
    paid_at TIMESTAMP NULL,
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
);

INSERT INTO customers (full_name, email, customer_type) VALUES
('Ana Souza', 'ana.souza@example.com', 'REGULAR'),
('Bruno Lima', 'bruno.lima@example.com', 'STUDENT'),
('Carla Mendes', 'carla.mendes@example.com', 'REGULAR');

INSERT INTO products (sku, name, description, unit_price, active) VALUES
('NB-IDEA-14', 'IdeaBook 14 Laptop', 'Portable laptop for programming classes', 3500.00, TRUE),
('MS-WIRE-01', 'Wireless Mouse', 'Ergonomic wireless mouse', 89.90, TRUE),
('KB-MECH-01', 'Mechanical Keyboard', 'Mechanical keyboard with ABNT2 layout', 299.90, TRUE),
('MN-24-FHD', 'Full HD Monitor', '24 inch monitor for development workstations', 899.00, TRUE),
('HS-USB-01', 'USB Headset', 'Headset with noise reduction microphone', 149.90, TRUE),
('WC-HD-01', 'HD Webcam', 'Webcam for remote classes and meetings', 199.90, TRUE),
('BP-NOTE-01', 'Laptop Backpack', 'Water resistant backpack for notebooks', 179.90, TRUE),
('CH-USB-C', 'USB-C Charger', 'Fast charger for mobile devices', 129.90, TRUE);

INSERT INTO stock_movements (product_id, movement_type, quantity, reason) VALUES
(1, 'INBOUND', 8, 'Initial stock'),
(2, 'INBOUND', 30, 'Initial stock'),
(3, 'INBOUND', 15, 'Initial stock'),
(4, 'INBOUND', 10, 'Initial stock'),
(5, 'INBOUND', 20, 'Initial stock'),
(6, 'INBOUND', 12, 'Initial stock'),
(7, 'INBOUND', 18, 'Initial stock'),
(8, 'INBOUND', 25, 'Initial stock'),
(1, 'OUTBOUND', 1, 'Confirmed order'),
(2, 'OUTBOUND', 2, 'Confirmed order'),
(4, 'RESERVED', 1, 'Pending order');

INSERT INTO carts (customer_id, status) VALUES
(1, 'CONVERTED'),
(2, 'CONVERTED'),
(3, 'OPEN');

INSERT INTO cart_items (cart_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 3500.00),
(1, 2, 2, 89.90),
(2, 3, 1, 299.90),
(2, 5, 1, 149.90),
(3, 4, 1, 899.00),
(3, 8, 3, 129.90);

INSERT INTO shipping_methods (code, name, base_cost, estimated_days, free_shipping_threshold) VALUES
('STANDARD', 'Standard Shipping', 25.00, 7, 500.00),
('EXPRESS', 'Express Shipping', 60.00, 2, NULL),
('PICKUP', 'Store Pickup', 0.00, 1, 0.00),
('ECONOMY', 'Economy Shipping', 15.00, 12, 300.00);

INSERT INTO discounts (code, name, discount_type, value, active) VALUES
('WELCOME10', 'Welcome Coupon', 'PERCENTAGE', 10.00, TRUE),
('STUDENT15', 'Student Discount', 'PERCENTAGE', 15.00, TRUE),
('FIXED50', 'Fixed Coupon', 'FIXED_AMOUNT', 50.00, TRUE),
('INACTIVE20', 'Inactive Campaign', 'PERCENTAGE', 20.00, FALSE);

INSERT INTO orders (customer_id, cart_id, shipping_method_id, status, subtotal, discount_total, shipping_total, grand_total) VALUES
(1, 1, 1, 'PAID', 3679.80, 367.98, 0.00, 3311.82),
(2, 2, 2, 'PAYMENT_FAILED', 449.80, 67.47, 60.00, 442.33),
(3, 3, 4, 'PENDING_PAYMENT', 1288.70, 50.00, 0.00, 1238.70);

INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(1, 1, 1, 3500.00, 3500.00),
(1, 2, 2, 89.90, 179.80),
(2, 3, 1, 299.90, 299.90),
(2, 5, 1, 149.90, 149.90),
(3, 4, 1, 899.00, 899.00),
(3, 8, 3, 129.90, 389.70);

INSERT INTO order_discounts (order_id, discount_id, amount) VALUES
(1, 1, 367.98),
(2, 2, 67.47),
(3, 3, 50.00);

INSERT INTO payments (order_id, payment_method, status, amount, transaction_reference, failure_reason, paid_at) VALUES
(1, 'CREDIT_CARD', 'APPROVED', 3311.82, 'cc_approved_1001', NULL, CURRENT_TIMESTAMP),
(2, 'PIX', 'FAILED', 442.33, 'pix_failed_1002', 'Invalid payment confirmation', NULL),
(3, 'BOLETO', 'PENDING', 1238.70, 'boleto_pending_1003', NULL, NULL);
