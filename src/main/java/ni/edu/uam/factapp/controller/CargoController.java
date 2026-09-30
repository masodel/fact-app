package ni.edu.uam.factapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.factapp.dao.CargoDAO;
import ni.edu.uam.factapp.model.Cargo;
import ni.edu.uam.factapp.util.DialogoBuscar;

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

    @FXML
    private void buscarCargo() {
        DialogoBuscar.ResultadoBusqueda res = DialogoBuscar.mostrar("Cargo");
        if (res == null) return;

        Cargo encontrado = null;

        if (res.getCriterio() == DialogoBuscar.CriterioBusqueda.ID) {
            try {
                int id = Integer.parseInt(res.getValor());
                encontrado = cargoDAO.getListaCargos().stream()
                        .filter(c -> c.getId() != null && c.getId() == id)
                        .findFirst()
                        .orElse(null);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error de Formato", "El ID ingresado debe ser un número entero válido.", Alert.AlertType.ERROR);
                return;
            }
        } else {
            encontrado = cargoDAO.getListaCargos().stream()
                    .filter(c -> c.getNombre() != null && c.getNombre().equalsIgnoreCase(res.getValor()))
                    .findFirst()
                    .orElse(null);
        }

        if (encontrado != null) {
            // Cargar todos los atributos en el formulario
            txtNombre.setText(encontrado.getNombre());
            txtDescripcion.setText(encontrado.getDescripcion());

            // Seleccionar y enfocar el registro en la tabla
            tblCargos.getSelectionModel().select(encontrado);
            tblCargos.scrollTo(encontrado);

            // Mostrar todos los atributos
            String info = String.format("Cargo Encontrado:\n\nID: %d\nNombre: %s\nDescripción: %s",
                    encontrado.getId(), encontrado.getNombre(), encontrado.getDescripcion());
            mostrarAlerta("Resultado de Búsqueda", info, Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("No Encontrado", "No se encontró ningún cargo con los datos proporcionados.", Alert.AlertType.ERROR);
        }
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