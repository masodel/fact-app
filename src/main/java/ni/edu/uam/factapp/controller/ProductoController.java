package ni.edu.uam.factapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import ni.edu.uam.factapp.dao.CategoriaDAO;
import ni.edu.uam.factapp.dao.ProductoDAO;
import ni.edu.uam.factapp.model.Categoria;
import ni.edu.uam.factapp.model.Producto;

import java.io.File;
import java.math.BigDecimal;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;

    // Componentes del HBox de Filtro y Búsqueda
    @FXML private ComboBox<String> cmbFiltroOpciones;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, String> colActivo;

    private final ProductoDAO productoDAO = ProductoDAO.getInstance();
    private FilteredList<Producto> productosFiltrados;
    private String rutaImagen;

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isActivo() ? "Sí" : "No"
                )
        );

        // Cargar categorías activas para el ComboBox del formulario
        FilteredList<Categoria> categoriasActivas = new FilteredList<>(
                CategoriaDAO.getInstance().getListaCategorias(),
                Categoria::isActiva
        );
        cmbCategoria.setItems(categoriasActivas);

        // Estructura: ObservableList -> FilteredList -> TableView
        productosFiltrados = new FilteredList<>(productoDAO.getProductos(), p -> true);
        tblProductos.setItems(productosFiltrados);

        // Opciones del ComboBox de filtros según la guía
        cmbFiltroOpciones.setItems(FXCollections.observableArrayList(
                "Todos los productos",
                "Productos activos",
                "Productos inactivos",
                "Por categoría"
        ));
        cmbFiltroOpciones.getSelectionModel().selectFirst();

        // Cargar lista de categorías en el ComboBox secundario de filtro
        cmbFiltroCategoria.setItems(CategoriaDAO.getInstance().getListaCategorias());

        // Mostrar u ocultar el ComboBox de categorías según la opción seleccionada
        cmbFiltroOpciones.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esPorCategoria = "Por categoría".equals(newVal);
            cmbFiltroCategoria.setVisible(esPorCategoria);
            cmbFiltroCategoria.setManaged(esPorCategoria);
            aplicarFiltrosCombinados();
        });

        cmbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());

        // Búsqueda en tiempo real mientras se escribe
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());

        chkActivo.setSelected(true);
    }

    /**
     * Aplica simultáneamente el filtro seleccionado y la búsqueda dinámica.
     */
    private void aplicarFiltrosCombinados() {
        productosFiltrados.setPredicate(producto -> {
            if (producto == null) return false;

            // 1. Evaluar el Criterio del Filtro
            String opcionFiltro = cmbFiltroOpciones.getValue();
            boolean cumpleFiltro = true;

            if ("Productos activos".equals(opcionFiltro)) {
                cumpleFiltro = producto.isActivo();
            } else if ("Productos inactivos".equals(opcionFiltro)) {
                cumpleFiltro = !producto.isActivo();
            } else if ("Por categoría".equals(opcionFiltro)) {
                Categoria catSeleccionada = cmbFiltroCategoria.getValue();
                if (catSeleccionada != null) {
                    cumpleFiltro = producto.getCategoria() != null
                            && producto.getCategoria().getId().equals(catSeleccionada.getId());
                } else {
                    cumpleFiltro = true; // Si no ha elegido categoría aún, muestra todos los de esa rama
                }
            }

            if (!cumpleFiltro) {
                return false;
            }

            // 2. Evaluar el Criterio de Búsqueda
            String textoBusqueda = txtBuscar.getText();
            if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
                return true;
            }

            textoBusqueda = textoBusqueda.trim();

            // Si el texto inicia con un dígito, se asume búsqueda por ID
            if (Character.isDigit(textoBusqueda.charAt(0))) {
                if (producto.getId() != null) {
                    return String.valueOf(producto.getId()).startsWith(textoBusqueda);
                }
                return false;
            } else {
                // De lo contrario, se asume búsqueda por Nombre
                if (producto.getNombre() != null) {
                    return producto.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase());
                }
                return false;
            }
        });
    }

    @FXML
    private void refrescar() {
        CategoriaDAO.getInstance().cargarCategoriasDesdeBD();
        productoDAO.cargarProductosDesdeBD();
        cmbFiltroCategoria.setItems(CategoriaDAO.getInstance().getListaCategorias());

        // Limpiar controles de filtro y búsqueda
        cmbFiltroOpciones.getSelectionModel().selectFirst();
        cmbFiltroCategoria.getSelectionModel().clearSelection();
        txtBuscar.clear();

        limpiar();
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen del producto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File archivo = chooser.showOpenDialog(
                (Stage) txtCodigo.getScene().getWindow()
        );

        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
        }
    }

    @FXML
    private void guardar() {
        if (txtCodigo.getText().isBlank()
                || txtNombre.getText().isBlank()
                || txtPrecio.getText().isBlank()
                || txtExistencia.getText().isBlank()
                || cmbCategoria.getValue() == null) {

            mensaje(Alert.AlertType.WARNING, "Complete los campos obligatorios.");
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "Precio mayor que cero y existencia no negativa.");
                return;
            }

            Producto producto = new Producto(
                    null,
                    txtCodigo.getText().trim(),
                    txtNombre.getText().trim(),
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagen,
                    chkActivo.isSelected()
            );

            productoDAO.agregar(producto);
            mensaje(Alert.AlertType.INFORMATION, "Producto agregado correctamente.");
            limpiar();

        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
        }
    }

    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        imgProducto.setImage(null);
        rutaImagen = null;
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}