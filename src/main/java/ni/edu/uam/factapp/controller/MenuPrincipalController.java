package ni.edu.uam.factapp.controller;

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
    private void abrirCategoria() {

        try {

            SceneManager.abrirVentana(
                    "/ni/edu/uam/factapp/fxml/categoria-view.fxml",
                    "Gestion de categorias"
            );

        } catch (IOException e) {

            Alert alerta = new Alert(
                    Alert.AlertType.ERROR,
                    "No fue posible abrir el módulo de categorias.",
                    ButtonType.OK
            );

            alerta.showAndWait();
        }
    }

    @FXML
    private void abrirCargo() {

        try {

            SceneManager.abrirVentana(
                    "/ni/edu/uam/factapp/fxml/cargo-view.fxml",
                    "Gestion de cargos"
            );

        } catch (IOException e) {

            Alert alerta = new Alert(
                    Alert.AlertType.ERROR,
                    "No fue posible abrir el módulo de cargo.",
                    ButtonType.OK
            );

            alerta.showAndWait();
        }
    }


}
