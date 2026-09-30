package ni.edu.uam.factapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.factapp.dao.CategoriaDAO;
import ni.edu.uam.factapp.model.Categoria;

import java.net.URL;
import java.util.ResourceBundle;

public class CategoriaController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;

    @FXML private ComboBox<String> cmbFiltroOpciones;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colActiva;

    private CategoriaDAO categoriaDAO;
    private FilteredList<Categoria> categoriasFiltradas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categoriaDAO = CategoriaDAO.getInstance();

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActiva() ? "Sí" : "No")
        );

        // Estructura: ObservableList -> FilteredList -> TableView
        categoriasFiltradas = new FilteredList<>(categoriaDAO.getListaCategorias(), c -> true);
        tblCategorias.setItems(categoriasFiltradas);

        // Configuración de Filtros
        cmbFiltroOpciones.setItems(FXCollections.observableArrayList(
                "Todas las categorías",
                "Categorías activas",
                "Categorías inactivas"
        ));
        cmbFiltroOpciones.getSelectionModel().selectFirst();

        // Listeners para filtro y búsqueda automática en tiempo real
        cmbFiltroOpciones.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
    }

    private void aplicarFiltrosCombinados() {
        categoriasFiltradas.setPredicate(categoria -> {
            if (categoria == null) return false;

            // 1. Criterio de Estado (Filtro)
            String opcionFiltro = cmbFiltroOpciones.getValue();
            boolean cumpleFiltro = true;

            if ("Categorías activas".equals(opcionFiltro)) {
                cumpleFiltro = categoria.isActiva();
            } else if ("Categorías inactivas".equals(opcionFiltro)) {
                cumpleFiltro = !categoria.isActiva();
            }

            if (!cumpleFiltro) return false;

            // 2. Criterio de Búsqueda
            String textoBusqueda = txtBuscar.getText();
            if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
                return true;
            }

            textoBusqueda = textoBusqueda.trim();

            // Si inicia con un número, busca por ID
            if (Character.isDigit(textoBusqueda.charAt(0))) {
                if (categoria.getId() != null) {
                    return String.valueOf(categoria.getId()).startsWith(textoBusqueda);
                }
                return false;
            } else { // Si es letra, busca por Nombre
                if (categoria.getNombre() != null) {
                    return categoria.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase());
                }
                return false;
            }
        });
    }

    @FXML
    private void guardarCategoria() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "Ingrese el nombre de la categoría.");
            return;
        }

        Categoria categoria = new Categoria(
                null,
                txtNombre.getText().trim(),
                chkActiva.isSelected()
        );

        categoriaDAO.agregarCategoria(categoria);
        mensaje(Alert.AlertType.INFORMATION, "Categoría agregada correctamente.");
        limpiar();
    }

    @FXML
    private void refrescar() {
        categoriaDAO.cargarCategoriasDesdeBD();
        cmbFiltroOpciones.getSelectionModel().selectFirst();
        txtBuscar.clear();
        limpiar();
    }

    private void limpiar() {
        txtNombre.clear();
        chkActiva.setSelected(true);
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}