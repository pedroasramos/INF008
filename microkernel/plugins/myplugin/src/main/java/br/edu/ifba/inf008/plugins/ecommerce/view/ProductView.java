package br.edu.ifba.inf008.plugins.ecommerce.view;

import br.edu.ifba.inf008.plugins.ecommerce.controller.ProductController;
import br.edu.ifba.inf008.plugins.ecommerce.exception.DomainException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class ProductView {

    private final ProductController productController;
    private final BorderPane root = new BorderPane();
    private final ObservableList<Product> data = FXCollections.observableArrayList();
    private final TableView<Product> table = new TableView<>(data);

    private final TextField skuField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField descriptionField = new TextField();
    private final TextField priceField = new TextField();
    private final TextField stockField = new TextField();

    public ProductView(ProductController productController) {
        this.productController = productController;
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
            data.setAll(productController.listProducts());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Could not load products: " + ex.getMessage());
        }
    }

    private void buildTable() {
        TableColumn<Product, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getSku()));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getDescription()));

        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("R$ %.2f", d.getValue().getPrice())));

        TableColumn<Product, String> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getStock())));

        table.getColumns().setAll(skuCol, nameCol, descCol, priceCol, stockCol);
        table.setPlaceholder(new Label("No products registered yet."));
    }

    private Node buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("SKU:"), skuField, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Description:"), descriptionField, new Label("Price:"), priceField, new Label("Stock:"), stockField);

        Button addButton = new Button("Add Product");
        addButton.setOnAction(e -> addProduct());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelected());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh());

        HBox buttons = new HBox(8, addButton, deleteButton, refreshButton);
        buttons.setPadding(new Insets(0, 10, 10, 10));

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(grid, buttons);
        return box;
    }

    private void addProduct() {
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());

            productController.createProduct(skuField.getText().trim(), nameField.getText().trim(),
                    descriptionField.getText().trim(), price, stock);

            skuField.clear();
            nameField.clear();
            descriptionField.clear();
            priceField.clear();
            stockField.clear();

            refresh();
        } catch (NumberFormatException ex) {
            AlertUtils.showError("Price and stock must be valid numbers.");
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Select a product to delete.");
            return;
        }
        try {
            productController.deleteProduct(selected.getProduct_id());
            refresh();
        } catch (DomainException ex) {
            AlertUtils.showError(ex.getMessage());
        } catch (RuntimeException ex) {
            AlertUtils.showError("Unexpected error: " + ex.getMessage());
        }
    }
}
