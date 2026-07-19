package br.edu.ifba.inf008.plugins.ecommerce.view;

import br.edu.ifba.inf008.plugins.ecommerce.controller.CustomerController;
import br.edu.ifba.inf008.plugins.ecommerce.exception.DomainException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Customer;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CustomerView {

    private final CustomerController customerController;
    private final BorderPane root = new BorderPane();
    private final ObservableList<Customer> data = FXCollections.observableArrayList();
    private final TableView<Customer> table = new TableView<>(data);

    private final TextField nameField = new TextField();
    private final TextField emailField = new TextField();
    private final ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("REGULAR", "STUDENT"));

    public CustomerView(CustomerController customerController) {
        this.customerController = customerController;
        typeCombo.setValue("REGULAR");
        buildTable();
        root.setCenter(table);
        root.setBottom(buildForm());
        refresh();
    }

    public Node getView() {
        return root;
    }

    public void refresh() {
        try {
            data.setAll(customerController.listCustomers());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Could not load customers: " + ex.getMessage());
        }
    }

    private void buildTable() {
        TableColumn<Customer, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getCustomer_id())));

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getEmail()));

        TableColumn<Customer, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getCustomerType()));

        table.getColumns().setAll(idCol, nameCol, emailCol, typeCol);
        table.setPlaceholder(new Label("No customers registered yet."));
    }

    private Node buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Name:"), nameField, new Label("Email:"), emailField, new Label("Type:"), typeCombo);

        Button addButton = new Button("Add Customer");
        addButton.setOnAction(e -> addCustomer());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelected());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh());

        HBox buttons = new HBox(8, addButton, deleteButton, refreshButton);
        buttons.setPadding(new Insets(0, 10, 10, 10));

        return new VBox(grid, buttons);
    }

    private void addCustomer() {
        try {
            customerController.createCustomer(nameField.getText().trim(), emailField.getText().trim(), typeCombo.getValue());
            nameField.clear();
            emailField.clear();
            typeCombo.setValue("REGULAR");
            refresh();
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        Customer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Select a customer to delete.");
            return;
        }
        try {
            customerController.deleteCustomer(selected.getCustomer_id());
            refresh();
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }
}
