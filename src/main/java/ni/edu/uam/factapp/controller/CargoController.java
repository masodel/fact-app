package ni.edu.uam.factapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.factapp.dao.CargoDAO;
import ni.edu.uam.factapp.model.Cargo;

import java.net.URL;
import java.util.ResourceBundle;

public class CargoController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private Button btnAgregar;

    @FXML private TableView<Cargo> tblCargos;
    @FXML private TableColumn<Cargo, String> colNombre;
    @FXML private TableColumn<Cargo, String> colDescripcion;

    private CargoDAO cargoDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Obtener la instancia compartida del DAO
        cargoDAO = CargoDAO.getInstance();

        // Mapear los atributos del modelo Cargo a las columnas de la tabla
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Enlazar la lista observable del DAO con la TableView
        tblCargos.setItems(cargoDAO.getListaCargos());
    }

    @FXML
    private void guardarCargo() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        // Validar que ambos campos tengan información
        if (nombre.isEmpty() || descripcion.isEmpty()) {
            mostrarAlerta(
                    "Campos Incompletos",
                    "Por favor completa tanto el nombre como la descripción del cargo.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        if (cargoDAO.existeNombre(nombre)) {
            mostrarAlerta(
                    "Cargo Duplicado",
                    "Ya existe un cargo registrado con el nombre '" + nombre + "'.",
                    Alert.AlertType.ERROR
            );
            txtNombre.requestFocus();
            return;
        }

        // Crear y guardar el nuevo cargo en el DAO
        Cargo nuevoCargo = new Cargo(null, nombre, descripcion);
        cargoDAO.agregarCargo(nuevoCargo);

        limpiarCampos();
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtNombre.requestFocus();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}