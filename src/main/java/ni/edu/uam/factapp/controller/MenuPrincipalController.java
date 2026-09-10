package ni.edu.uam.factapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ni.edu.uam.factapp.util.SceneManager;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private void abrirProductos() {

        try {

            SceneManager.abrirVentana(
                    "/ni/edu/uam/factapp/fxml/producto-view.fxml",
                    "Gestión de productos"
            );

        } catch (IOException e) {

            Alert alerta = new Alert(
                    Alert.AlertType.ERROR,
                    "No fue posible abrir el módulo de productos.",
                    ButtonType.OK
            );

            alerta.showAndWait();
        }
    }


    @FXML
    private void salir() {

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Desea cerrar la aplicación?",
                ButtonType.OK,
                ButtonType.CANCEL
        );

        alerta.setTitle("Salir");
        alerta.setHeaderText("Confirmar salida");

        if (alerta.showAndWait()
                .orElse(ButtonType.CANCEL) == ButtonType.OK) {

            Platform.exit();
        }
    }

}
