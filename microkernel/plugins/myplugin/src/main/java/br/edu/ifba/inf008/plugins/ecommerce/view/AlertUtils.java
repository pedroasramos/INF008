package br.edu.ifba.inf008.plugins.ecommerce.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class AlertUtils {

    private AlertUtils() {
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Operation failed");
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
