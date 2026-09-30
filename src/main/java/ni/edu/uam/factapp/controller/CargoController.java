package ni.edu.uam.factapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.factapp.dao.CargoDAO;
import ni.edu.uam.factapp.model.Cargo;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CargoController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;

    @FXML private Button btnAgregar;
    @FXML private Button btnRefrescar;

    @FXML private ComboBox<String> cmbFiltroOpciones;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Cargo> tblCargos;
    @FXML private TableColumn<Cargo, String> colNombre;
    @FXML private TableColumn<Cargo, String> colDescripcion;

    private CargoDAO cargoDAO;
    private FilteredList<Cargo> cargosFiltrados;
    private Cargo cargoEnEdicion = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargoDAO = CargoDAO.getInstance();

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        cargosFiltrados = new FilteredList<>(cargoDAO.getListaCargos(), c -> true);
        tblCargos.setItems(cargosFiltrados);

        tblCargos.setRowFactory(tv -> {
            TableRow<Cargo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    cargarModoEdicion(row.getItem());
                }
            });
            return row;
        });

        cmbFiltroOpciones.setItems(FXCollections.observableArrayList("Todos los cargos"));
        cmbFiltroOpciones.getSelectionModel().selectFirst();

        cmbFiltroOpciones.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
    }

    private void cargarModoEdicion(Cargo c) {
        if (c == null) return;
        this.cargoEnEdicion = c;

        txtNombre.setText(c.getNombre());
        txtDescripcion.setText(c.getDescripcion());

        btnAgregar.setText("Actualizar");
        btnRefrescar.setText("Eliminar");
        btnRefrescar.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");

        mensaje(Alert.AlertType.INFORMATION, "Modo Edición Activado", "Ha seleccionado el cargo: " + c.getNombre());
    }

    @FXML
    private void guardarCargo() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "Atención", "Ingrese el nombre del cargo.");
            return;
        }

        if (cargoEnEdicion == null) {
            Cargo nuevo = new Cargo(null, txtNombre.getText().trim(), txtDescripcion.getText().trim());
            cargoDAO.agregarCargo(nuevo);
            mensaje(Alert.AlertType.INFORMATION, "Éxito", "Cargo agregado correctamente.");
        } else {
            cargoEnEdicion.setNombre(txtNombre.getText().trim());
            cargoEnEdicion.setDescripcion(txtDescripcion.getText().trim());
            cargoDAO.actualizar(cargoEnEdicion);
            mensaje(Alert.AlertType.INFORMATION, "Éxito", "Cargo actualizado correctamente.");
        }

        refrescarYLimpiar();
    }

    @FXML
    private void accionBotonSecundario() {
        if (cargoEnEdicion != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("¿Está seguro de eliminar el cargo?");
            confirmacion.setContentText("Cargo: " + cargoEnEdicion.getNombre());

            Optional<ButtonType> res = confirmacion.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                cargoDAO.eliminar(cargoEnEdicion);
                mensaje(Alert.AlertType.INFORMATION, "Eliminado", "Cargo eliminado con éxito.");
                refrescarYLimpiar();
            }
        } else {
            refrescarYLimpiar();
        }
    }

    private void refrescarYLimpiar() {
        cargoDAO.cargarCargosDesdeBD();
        txtNombre.clear();
        txtDescripcion.clear();

        cargoEnEdicion = null;
        btnAgregar.setText("Agregar");
        btnRefrescar.setText("Refrescar");
        btnRefrescar.setStyle("");

        aplicarFiltrosCombinados();
    }

    private void aplicarFiltrosCombinados() {
        cargosFiltrados.setPredicate(cargo -> {
            if (cargo == null) return false;

            String textoBusqueda = txtBuscar.getText();
            if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) return true;

            textoBusqueda = textoBusqueda.trim();

            if (Character.isDigit(textoBusqueda.charAt(0))) {
                return cargo.getId() != null && String.valueOf(cargo.getId()).startsWith(textoBusqueda);
            } else {
                return cargo.getNombre() != null && cargo.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase());
            }
        });
    }

    private void mensaje(Alert.AlertType tipo, String titulo, String texto) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}