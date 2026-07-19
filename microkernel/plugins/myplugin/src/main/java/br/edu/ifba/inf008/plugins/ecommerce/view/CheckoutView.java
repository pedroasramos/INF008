package br.edu.ifba.inf008.plugins.ecommerce.view;

import br.edu.ifba.inf008.plugins.ecommerce.controller.CartController;
import br.edu.ifba.inf008.plugins.ecommerce.controller.CustomerController;
import br.edu.ifba.inf008.plugins.ecommerce.controller.OrderController;
import br.edu.ifba.inf008.plugins.ecommerce.controller.ProductController;
import br.edu.ifba.inf008.plugins.ecommerce.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.NoDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.StudentDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.exception.DomainException;
import br.edu.ifba.inf008.plugins.ecommerce.exception.InvalidPaymentException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.model.CartItem;
import br.edu.ifba.inf008.plugins.ecommerce.model.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.model.Order;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;
import br.edu.ifba.inf008.plugins.ecommerce.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicyFactory;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Registers the minimum flow required by the assignment: pick a customer, build a cart,
 * choose discount / shipping / payment, preview the final total, and confirm the order.
 * Business rules stay in the controller/service/domain layers; this class only wires
 * form input to those calls and renders the result.
 */
public class CheckoutView {

    private final ProductController productController;
    private final CustomerController customerController;
    private final CartController cartController;
    private final OrderController orderController;
    private final Runnable onOrderPlaced;

    private final VBox root = new VBox(10);

    private final ComboBox<Customer> customerCombo = new ComboBox<>();
    private final Label cartLabel = new Label("No active cart");

    private final ComboBox<Product> productCombo = new ComboBox<>();
    private final TextField quantityField = new TextField("1");

    private final ObservableList<CartItem> cartItemsData = FXCollections.observableArrayList();
    private final TableView<CartItem> cartTable = new TableView<>(cartItemsData);

    private final ComboBox<String> discountTypeCombo =
            new ComboBox<>(FXCollections.observableArrayList("NONE", "COUPON", "STUDENT"));
    private final TextField couponField = new TextField();

    private final ComboBox<String> shippingTypeCombo =
            new ComboBox<>(FXCollections.observableArrayList("STANDARD", "EXPRESS", "PICKUP"));

    private final ComboBox<String> paymentTypeCombo =
            new ComboBox<>(FXCollections.observableArrayList("CREDIT_CARD", "PIX", "BOLETO"));

    private final TextField cardNumberField = new TextField();
    private final TextField cardHolderField = new TextField();
    private final TextField cardCvvField = new TextField();
    private final TextField cardExpirationField = new TextField();
    private final GridPane creditCardFields = new GridPane();

    private final TextField pixKeyField = new TextField();
    private final GridPane pixFields = new GridPane();

    private final TextField barcodeField = new TextField();
    private final TextField dueDateField = new TextField();
    private final GridPane boletoFields = new GridPane();

    private final Label subtotalLabel = new Label("Subtotal: R$ 0.00");
    private final Label discountLabel = new Label("Discount: R$ 0.00");
    private final Label shippingLabel = new Label("Shipping: R$ 0.00");
    private final Label totalLabel = new Label("Total: R$ 0.00");

    private Cart currentCart;

    public CheckoutView(ProductController productController, CustomerController customerController,
                         CartController cartController, OrderController orderController, Runnable onOrderPlaced) {
        this.productController = productController;
        this.customerController = customerController;
        this.cartController = cartController;
        this.orderController = orderController;
        this.onOrderPlaced = onOrderPlaced;

        root.setPadding(new Insets(10));
        root.getChildren().addAll(
                buildCustomerSection(),
                new Separator(),
                buildAddProductSection(),
                buildCartTable(),
                new Separator(),
                buildOptionsSection(),
                new Separator(),
                buildTotalsSection(),
                buildConfirmSection()
        );

        totalLabel.setStyle("-fx-font-weight: bold;");
        discountTypeCombo.setValue("NONE");
        shippingTypeCombo.setValue("STANDARD");
        paymentTypeCombo.setValue("CREDIT_CARD");
        couponField.setDisable(true);
        togglePaymentFields();

        discountTypeCombo.valueProperty().addListener((o, ov, nv) -> {
            couponField.setDisable(!"COUPON".equals(nv));
            recalculate();
        });
        couponField.textProperty().addListener((o, ov, nv) -> recalculate());
        shippingTypeCombo.valueProperty().addListener((o, ov, nv) -> recalculate());
        paymentTypeCombo.valueProperty().addListener((o, ov, nv) -> togglePaymentFields());

        refreshCustomersAndProducts();
    }

    public Node getView() {
        return root;
    }

    public void refreshCustomersAndProducts() {
        try {
            Customer selectedCustomer = customerCombo.getValue();
            customerCombo.setItems(FXCollections.observableArrayList(customerController.listCustomers()));
            customerCombo.setValue(selectedCustomer);
        } catch (RuntimeException ex) {
            AlertUtils.showError("Could not load customers: " + ex.getMessage());
        }
        refreshProducts();
    }

    private void refreshProducts() {
        try {
            productCombo.setItems(FXCollections.observableArrayList(productController.listProducts()));
        } catch (RuntimeException ex) {
            AlertUtils.showError("Could not load products: " + ex.getMessage());
        }
    }

    private Node buildCustomerSection() {
        customerCombo.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer c) {
                return c == null ? "" : c.getName() + " (" + c.getEmail() + ")";
            }

            @Override
            public Customer fromString(String s) {
                return null;
            }
        });
        customerCombo.setPrefWidth(280);

        Button newCartButton = new Button("New Cart");
        newCartButton.setOnAction(e -> createCart());

        HBox box = new HBox(8, new Label("Customer:"), customerCombo, newCartButton, cartLabel);
        box.setPadding(new Insets(0, 0, 5, 0));
        return box;
    }

    private Node buildAddProductSection() {
        productCombo.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product p) {
                return p == null ? "" : p.getName() + " - " + p.getSku() + " (stock: " + p.getStock() + ")";
            }

            @Override
            public Product fromString(String s) {
                return null;
            }
        });
        productCombo.setPrefWidth(320);
        quantityField.setPrefWidth(60);

        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> addToCart());

        return new HBox(8, new Label("Product:"), productCombo, new Label("Qty:"), quantityField, addButton);
    }

    private Node buildCartTable() {
        TableColumn<CartItem, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getProduct().getName()));

        TableColumn<CartItem, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getQuantity())));

        TableColumn<CartItem, String> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("R$ %.2f", d.getValue().getPrice())));

        TableColumn<CartItem, String> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("R$ %.2f", d.getValue().calculateSubtotal())));

        cartTable.getColumns().setAll(productCol, qtyCol, priceCol, subtotalCol);
        cartTable.setPlaceholder(new Label("Cart is empty."));
        cartTable.setPrefHeight(160);

        Button removeButton = new Button("Remove Selected Item");
        removeButton.setOnAction(e -> removeSelectedItem());

        return new VBox(6, cartTable, removeButton);
    }

    private Node buildOptionsSection() {
        creditCardFields.setHgap(8);
        creditCardFields.setVgap(6);
        creditCardFields.addRow(0, new Label("Card number:"), cardNumberField, new Label("Holder:"), cardHolderField);
        creditCardFields.addRow(1, new Label("CVV:"), cardCvvField, new Label("Expiration (MM/yy):"), cardExpirationField);

        pixFields.setHgap(8);
        pixFields.setVgap(6);
        pixFields.addRow(0, new Label("Pix key:"), pixKeyField);

        boletoFields.setHgap(8);
        boletoFields.setVgap(6);
        boletoFields.addRow(0, new Label("Barcode:"), barcodeField, new Label("Due date (dd/MM/yyyy):"), dueDateField);

        HBox discountBox = new HBox(8, new Label("Discount:"), discountTypeCombo, new Label("Coupon code:"), couponField);
        HBox shippingBox = new HBox(8, new Label("Shipping:"), shippingTypeCombo);
        HBox paymentTypeBox = new HBox(8, new Label("Payment method:"), paymentTypeCombo);

        VBox box = new VBox(8, discountBox, shippingBox, paymentTypeBox, creditCardFields, pixFields, boletoFields);
        return box;
    }

    private Node buildTotalsSection() {
        VBox box = new VBox(4, subtotalLabel, discountLabel, shippingLabel, totalLabel);
        box.setPadding(new Insets(5, 0, 5, 0));
        return box;
    }

    private Node buildConfirmSection() {
        Button confirmButton = new Button("Confirm Order");
        confirmButton.setOnAction(e -> confirmOrder());
        return new HBox(confirmButton);
    }

    private void togglePaymentFields() {
        boolean creditCard = "CREDIT_CARD".equals(paymentTypeCombo.getValue());
        boolean pix = "PIX".equals(paymentTypeCombo.getValue());
        boolean boleto = "BOLETO".equals(paymentTypeCombo.getValue());

        creditCardFields.setVisible(creditCard);
        creditCardFields.setManaged(creditCard);
        pixFields.setVisible(pix);
        pixFields.setManaged(pix);
        boletoFields.setVisible(boleto);
        boletoFields.setManaged(boleto);
    }

    private void createCart() {
        Customer customer = customerCombo.getValue();
        if (customer == null) {
            AlertUtils.showError("Select a customer first.");
            return;
        }
        try {
            currentCart = cartController.createCart(customer.getCustomer_id());
            cartItemsData.clear();
            cartLabel.setText("Cart #" + currentCart.getCart_id());
            recalculate();
        } catch (RuntimeException ex) {
            AlertUtils.showError("Could not create cart: " + ex.getMessage());
        }
    }

    private void addToCart() {
        if (currentCart == null) {
            AlertUtils.showError("Create a cart first.");
            return;
        }
        Product product = productCombo.getValue();
        if (product == null) {
            AlertUtils.showError("Select a product.");
            return;
        }
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            cartController.addProductToCart(currentCart.getCart_id(), product.getProduct_id(), quantity);
            currentCart = cartController.getCart(currentCart.getCart_id());
            cartItemsData.setAll(currentCart.getItems());
            recalculate();
        } catch (NumberFormatException ex) {
            AlertUtils.showError("Quantity must be a valid integer.");
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }

    private void removeSelectedItem() {
        if (currentCart == null) {
            return;
        }
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Select a cart item to remove.");
            return;
        }
        try {
            cartController.removeProductFromCart(currentCart.getCart_id(), selected.getProduct().getProduct_id());
            currentCart = cartController.getCart(currentCart.getCart_id());
            cartItemsData.setAll(currentCart.getItems());
            recalculate();
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }

    private void recalculate() {
        if (currentCart == null || currentCart.getItems().isEmpty()) {
            subtotalLabel.setText("Subtotal: R$ 0.00");
            discountLabel.setText("Discount: R$ 0.00");
            shippingLabel.setText("Shipping: R$ 0.00");
            totalLabel.setText("Total: R$ 0.00");
            return;
        }

        try {
            Order preview = new Order();
            for (CartItem item : currentCart.getItems()) {
                preview.addItem(item);
            }
            preview.setDiscountPolicy(buildDiscountPolicy());
            preview.setShippingPolicy(ShippingPolicyFactory.fromType(shippingTypeCombo.getValue()));
            preview.calculateTotal();

            subtotalLabel.setText(String.format("Subtotal: R$ %.2f", preview.getSubtotal()));
            discountLabel.setText(String.format("Discount: R$ %.2f", preview.getDiscount()));
            shippingLabel.setText(String.format("Shipping: R$ %.2f", preview.getShippingCost()));
            totalLabel.setText(String.format("Total: R$ %.2f", preview.getTotal()));
        } catch (RuntimeException ex) {
            AlertUtils.showError("Could not calculate the total: " + ex.getMessage());
        }
    }

    private DiscountPolicy buildDiscountPolicy() {
        String type = discountTypeCombo.getValue();
        switch (type) {
            case "COUPON": return new CouponDiscountPolicy(couponField.getText().trim());
            case "STUDENT": return new StudentDiscountPolicy();
            default: return new NoDiscountPolicy();
        }
    }

    private Payable buildPaymentMethod() {
        String type = paymentTypeCombo.getValue();
        switch (type) {
            case "CREDIT_CARD": {
                long cardNumber = parseLong(cardNumberField.getText(), "Card number");
                int cvv = (int) parseLong(cardCvvField.getText(), "CVV");
                return orderController.buildCreditCardPayment(cardNumber, cardHolderField.getText().trim(), cvv,
                        cardExpirationField.getText().trim());
            }
            case "PIX":
                return orderController.buildPixPayment(pixKeyField.getText().trim());
            case "BOLETO":
                return orderController.buildBoletoPayment(barcodeField.getText().trim(), dueDateField.getText().trim());
            default:
                throw new IllegalArgumentException("Select a payment method.");
        }
    }

    private long parseLong(String text, String fieldName) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    private void confirmOrder() {
        if (currentCart == null) {
            AlertUtils.showError("Create a cart and add products first.");
            return;
        }

        try {
            String discountType = discountTypeCombo.getValue();
            String coupon = couponField.getText().trim();
            String shippingType = shippingTypeCombo.getValue();
            Payable payment = buildPaymentMethod();

            Order order = orderController.placeOrder(currentCart.getCart_id(), discountType, coupon, shippingType, payment);

            AlertUtils.showInfo(String.format("Order #%d created with status %s.%nTotal: R$ %.2f",
                    order.getOrder_id(), order.getStatus(), order.getTotal()));

            currentCart = null;
            cartItemsData.clear();
            cartLabel.setText("No active cart");
            recalculate();
            refreshProducts();
            onOrderPlaced.run();

        } catch (InvalidPaymentException ex) {
            AlertUtils.showError("Payment was not confirmed: " + ex.getMessage()
                    + "\nThe cart was kept so you can try another payment method.");
            onOrderPlaced.run();
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }
}
