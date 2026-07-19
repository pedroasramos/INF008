package br.edu.ifba.inf008.plugins.ecommerce.view;

import br.edu.ifba.inf008.plugins.ecommerce.controller.OrderController;
import br.edu.ifba.inf008.plugins.ecommerce.exception.DomainException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class OrderView {

    private final OrderController orderController;
    private final BorderPane root = new BorderPane();
    private final ObservableList<Order> data = FXCollections.observableArrayList();
    private final TableView<Order> table = new TableView<>(data);

    public OrderView(OrderController orderController) {
        this.orderController = orderController;
        buildTable();
        root.setCenter(table);
        root.setBottom(buildButtons());
        refresh();
    }

    public Node getView() {
        return root;
    }

    public void refresh() {
        try {
            data.setAll(orderController.listOrders());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Could not load orders: " + ex.getMessage());
        }
    }

    private void buildTable() {
        TableColumn<Order, String> idCol = new TableColumn<>("Order #");
        idCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getOrder_id())));

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer #");
        customerCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getCustomerId())));

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getStatus().name()));

        TableColumn<Order, String> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("R$ %.2f", d.getValue().getSubtotal())));

        TableColumn<Order, String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("R$ %.2f", d.getValue().getDiscount())));

        TableColumn<Order, String> shippingCol = new TableColumn<>("Shipping");
        shippingCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("R$ %.2f", d.getValue().getShippingCost())));

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("R$ %.2f", d.getValue().getTotal())));

        table.getColumns().setAll(idCol, customerCol, statusCol, subtotalCol, discountCol, shippingCol, totalCol);
        table.setPlaceholder(new Label("No orders placed yet."));
    }

    private Node buildButtons() {
        Button cancelButton = new Button("Cancel Selected");
        cancelButton.setOnAction(e -> cancelSelected());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelected());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh());

        HBox box = new HBox(8, cancelButton, deleteButton, refreshButton);
        box.setPadding(new Insets(10));
        return box;
    }

    private void cancelSelected() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Select an order to cancel.");
            return;
        }
        try {
            orderController.cancelOrder(selected.getOrder_id());
            refresh();
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Select an order to delete.");
            return;
        }
        try {
            orderController.deleteOrder(selected.getOrder_id());
            refresh();
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }
}
