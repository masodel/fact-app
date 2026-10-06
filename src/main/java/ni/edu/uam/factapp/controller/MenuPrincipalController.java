package ni.edu.uam.factapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ni.edu.uam.factapp.util.DatabaseUtil;
import ni.edu.uam.factapp.util.SceneManager;

import java.io.IOException;
import java.util.Optional;

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

    @FXML
    private void abrirEmpleado() {

        try {

            SceneManager.abrirVentana(
                    "/ni/edu/uam/factapp/fxml/empleado-view.fxml",
                    "Gestion de Empleados"
            );

        } catch (IOException e) {

            Alert alerta = new Alert(
                    Alert.AlertType.ERROR,
                    "No fue posible abrir el módulo de Empleado.",
                    ButtonType.OK
            );

            alerta.showAndWait();
        }
    }

    @FXML
    private void limpiarBaseDeDatos() {
        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Está seguro de que desea eliminar TODOS los datos de las tablas? La estructura se mantendrá intacta, pero no se podrá recuperar la información.",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacion.setTitle("Confirmar vaciado de Base de Datos");
        confirmacion.setHeaderText("¡Atención!");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            try {
                // Ejecuta la consulta SQL masiva
                DatabaseUtil.vaciarTodasLasTablas();

                Alert exito = new Alert(
                        Alert.AlertType.INFORMATION,
                        "Se han eliminado todos los registros de la base de datos con éxito.",
                        ButtonType.OK
                );
                exito.setTitle("Éxito");
                exito.setHeaderText(null);
                exito.showAndWait();

            } catch (Exception e) {
                Alert error = new Alert(
                        Alert.AlertType.ERROR,
                        "Error al ejecutar las consultas SQL de eliminación: " + e.getMessage(),
                        ButtonType.OK
                );
                error.setTitle("Error SQL");
                error.setHeaderText(null);
                error.showAndWait();
            }
        }
    }
}
