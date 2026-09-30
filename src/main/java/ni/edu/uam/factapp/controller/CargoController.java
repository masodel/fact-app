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
import java.util.ResourceBundle;

public class CargoController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;

    @FXML private ComboBox<String> cmbFiltroOpciones;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Cargo> tblCargos;
    @FXML private TableColumn<Cargo, String> colNombre;
    @FXML private TableColumn<Cargo, String> colDescripcion;

    private CargoDAO cargoDAO;
    private FilteredList<Cargo> cargosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargoDAO = CargoDAO.getInstance();

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Estructura: ObservableList -> FilteredList -> TableView
        cargosFiltrados = new FilteredList<>(cargoDAO.getListaCargos(), c -> true);
        tblCargos.setItems(cargosFiltrados);

        // Opciones de Filtro
        cmbFiltroOpciones.setItems(FXCollections.observableArrayList(
                "Todos los cargos"
        ));
        cmbFiltroOpciones.getSelectionModel().selectFirst();

        // Listeners para filtro y búsqueda automática en tiempo real
        cmbFiltroOpciones.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
    }

    private void aplicarFiltrosCombinados() {
        cargosFiltrados.setPredicate(cargo -> {
            if (cargo == null) return false;

            // 1. Criterio de Filtro (Reservado para extensiones)
            boolean cumpleFiltro = true;

            if (!cumpleFiltro) return false;

            // 2. Criterio de Búsqueda
            String textoBusqueda = txtBuscar.getText();
            if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
                return true;
            }

            textoBusqueda = textoBusqueda.trim();

            // Si inicia con un número, busca por ID
            if (Character.isDigit(textoBusqueda.charAt(0))) {
                if (cargo.getId() != null) {
                    return String.valueOf(cargo.getId()).startsWith(textoBusqueda);
                }
                return false;
            } else { // Si es letra, busca por Nombre
                if (cargo.getNombre() != null) {
                    return cargo.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase());
                }
                return false;
            }
        });
    }

    @FXML
    private void guardarCargo() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "Ingrese el nombre del cargo.");
            return;
        }

        Cargo cargo = new Cargo(
                null,
                txtNombre.getText().trim(),
                txtDescripcion.getText().trim()
        );

        cargoDAO.agregarCargo(cargo);
        mensaje(Alert.AlertType.INFORMATION, "Cargo agregado correctamente.");
        limpiar();
    }

    @FXML
    private void refrescar() {
        cargoDAO.cargarCargosDesdeBD();
        cmbFiltroOpciones.getSelectionModel().selectFirst();
        txtBuscar.clear();
        limpiar();
    }

    private void limpiar() {
        txtNombre.clear();
        txtDescripcion.clear();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}