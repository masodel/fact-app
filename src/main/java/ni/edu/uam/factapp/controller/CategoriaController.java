package ni.edu.uam.factapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.factapp.dao.CategoriaDAO;
import ni.edu.uam.factapp.dao.ProductoDAO;
import ni.edu.uam.factapp.model.Categoria;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategoriaController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;

    @FXML private Button btnAgregar;
    @FXML private Button btnRefrescar;

    @FXML private ComboBox<String> cmbFiltroOpciones;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colActiva;

    private CategoriaDAO categoriaDAO;
    private FilteredList<Categoria> categoriasFiltradas;
    private Categoria categoriaEnEdicion = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categoriaDAO = CategoriaDAO.getInstance();

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActiva() ? "Sí" : "No")
        );

        categoriasFiltradas = new FilteredList<>(categoriaDAO.getListaCategorias(), c -> true);
        tblCategorias.setItems(categoriasFiltradas);

        tblCategorias.setRowFactory(tv -> {
            TableRow<Categoria> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    cargarModoEdicion(row.getItem());
                }
            });
            return row;
        });

        cmbFiltroOpciones.setItems(FXCollections.observableArrayList(
                "Todas las categorías",
                "Categorías activas",
                "Categorías inactivas"
        ));
        cmbFiltroOpciones.getSelectionModel().selectFirst();

        cmbFiltroOpciones.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
    }

    private void cargarModoEdicion(Categoria c) {
        if (c == null) return;
        this.categoriaEnEdicion = c;

        txtNombre.setText(c.getNombre());
        chkActiva.setSelected(c.isActiva());

        btnAgregar.setText("Actualizar");
        btnRefrescar.setText("Eliminar");
        btnRefrescar.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");

        mensaje(Alert.AlertType.INFORMATION, "Modo Edición Activado", "Ha seleccionado la categoría: " + c.getNombre());
    }

    @FXML
    private void guardarCategoria() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isBlank()) {
            mensaje(Alert.AlertType.WARNING, "Atención", "Ingrese el nombre de la categoría.");
            return;
        }

        // Obtener ID actual si estamos editando
        Integer idActual = (categoriaEnEdicion != null) ? categoriaEnEdicion.getId() : null;

        // Validar duplicado de NOMBRE
        if (categoriaDAO.existeNombre(nombre, idActual)) {
            mensaje(Alert.AlertType.WARNING, "Nombre Duplicado", "Ya existe una categoría registrada con el nombre: " + nombre);
            return;
        }

        if (categoriaEnEdicion == null) {
            Categoria nueva = new Categoria(null, nombre, chkActiva.isSelected());
            categoriaDAO.agregarCategoria(nueva);
            mensaje(Alert.AlertType.INFORMATION, "Éxito", "Categoría agregada correctamente.");
        } else {
            categoriaEnEdicion.setNombre(nombre);
            categoriaEnEdicion.setActiva(chkActiva.isSelected());
            categoriaDAO.actualizar(categoriaEnEdicion);
            mensaje(Alert.AlertType.INFORMATION, "Éxito", "Categoría actualizada correctamente.");
        }

        refrescarYLimpiar();
    }

    @FXML
    private void accionBotonSecundario() {
        if (categoriaEnEdicion != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("¿Está seguro de eliminar la categoría?");
            confirmacion.setContentText("Categoría: " + categoriaEnEdicion.getNombre());

            Optional<ButtonType> res = confirmacion.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                categoriaDAO.eliminar(categoriaEnEdicion);
                mensaje(Alert.AlertType.INFORMATION, "Eliminado", "Categoría eliminada con éxito.");
                refrescarYLimpiar();
                ProductoDAO.getInstance().cargarProductosDesdeBD();
            }
        } else {
            refrescarYLimpiar();
        }
    }

    private void refrescarYLimpiar() {
        categoriaDAO.cargarCategoriasDesdeBD();
        txtNombre.clear();
        chkActiva.setSelected(true);

        categoriaEnEdicion = null;
        btnAgregar.setText("Agregar");
        btnRefrescar.setText("Refrescar");
        btnRefrescar.setStyle("");

        aplicarFiltrosCombinados();
    }

    private void aplicarFiltrosCombinados() {
        categoriasFiltradas.setPredicate(categoria -> {
            if (categoria == null) return false;

            String opcionFiltro = cmbFiltroOpciones.getValue();
            boolean cumpleFiltro = true;

            if ("Categorías activas".equals(opcionFiltro)) {
                cumpleFiltro = categoria.isActiva();
            } else if ("Categorías inactivas".equals(opcionFiltro)) {
                cumpleFiltro = !categoria.isActiva();
            }

            if (!cumpleFiltro) return false;

            String textoBusqueda = txtBuscar.getText();
            if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) return true;

            textoBusqueda = textoBusqueda.trim();

            if (Character.isDigit(textoBusqueda.charAt(0))) {
                return categoria.getId() != null && String.valueOf(categoria.getId()).startsWith(textoBusqueda);
            } else {
                return categoria.getNombre() != null && categoria.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase());
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