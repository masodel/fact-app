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
import java.util.Optional;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;

    @FXML private Button btnGuardar;
    @FXML private Button btnRefrescar;

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

    private Producto productoEnEdicion = null;

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

        FilteredList<Categoria> categoriasActivas = new FilteredList<>(
                CategoriaDAO.getInstance().getListaCategorias(),
                Categoria::isActiva
        );
        cmbCategoria.setItems(categoriasActivas);

        productosFiltrados = new FilteredList<>(productoDAO.getProductos(), p -> true);
        tblProductos.setItems(productosFiltrados);

        tblProductos.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    cargarModoEdicion(row.getItem());
                }
            });
            return row;
        });

        cmbFiltroOpciones.setItems(FXCollections.observableArrayList(
                "Todos los productos",
                "Productos activos",
                "Productos inactivos",
                "Por categoría"
        ));
        cmbFiltroOpciones.getSelectionModel().selectFirst();
        cmbFiltroCategoria.setItems(CategoriaDAO.getInstance().getListaCategorias());

        cmbFiltroOpciones.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esPorCategoria = "Por categoría".equals(newVal);
            cmbFiltroCategoria.setVisible(esPorCategoria);
            cmbFiltroCategoria.setManaged(esPorCategoria);
            aplicarFiltrosCombinados();
        });

        cmbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());

        chkActivo.setSelected(true);
    }

    private void cargarModoEdicion(Producto p) {
        if (p == null) return;
        this.productoEnEdicion = p;

        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        cmbCategoria.setValue(p.getCategoria());
        txtPrecio.setText(p.getPrecioVenta() != null ? p.getPrecioVenta().toString() : "");
        txtExistencia.setText(String.valueOf(p.getExistencia()));
        chkActivo.setSelected(p.isActivo());

        if (p.getRutaImagen() != null && !p.getRutaImagen().isBlank()) {
            this.rutaImagen = p.getRutaImagen();
            try {
                imgProducto.setImage(new Image(rutaImagen));
            } catch (Exception e) {
                imgProducto.setImage(null);
            }
        } else {
            imgProducto.setImage(null);
            this.rutaImagen = null;
        }

        btnGuardar.setText("Actualizar");
        btnRefrescar.setText("Eliminar");
        btnRefrescar.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");

        mensaje(Alert.AlertType.INFORMATION,
                "Modo Edición Activado",
                "Ha seleccionado el producto: " + p.getNombre() + ".\nPuede modificar sus atributos o eliminarlo.");
    }

    @FXML
    private void guardar() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (codigo.isBlank() || nombre.isBlank() || txtPrecio.getText().isBlank()
                || txtExistencia.getText().isBlank() || cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Campos Incompletos", "Complete los campos obligatorios.");
            return;
        }

        // Obtener ID actual si estamos editando (null si estamos creando)
        Integer idActual = (productoEnEdicion != null) ? productoEnEdicion.getId() : null;

        // Validar duplicado de CÓDIGO
        if (productoDAO.existeCodigo(codigo, idActual)) {
            mensaje(Alert.AlertType.WARNING, "Código Duplicado", "Ya existe un producto registrado con el código: " + codigo);
            return;
        }

        // Validar duplicado de NOMBRE
        if (productoDAO.existeNombre(nombre, idActual)) {
            mensaje(Alert.AlertType.WARNING, "Nombre Duplicado", "Ya existe un producto registrado con el nombre: " + nombre);
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "Datos Inválidos", "El precio debe ser mayor que cero y la existencia no negativa.");
                return;
            }

            if (productoEnEdicion == null) {
                // MODO CREACIÓN
                Producto nuevo = new Producto(
                        null,
                        codigo,
                        nombre,
                        cmbCategoria.getValue(),
                        precio,
                        existencia,
                        rutaImagen,
                        chkActivo.isSelected()
                );
                productoDAO.agregar(nuevo);
                mensaje(Alert.AlertType.INFORMATION, "Éxito", "Producto agregado correctamente.");
            } else {
                // MODO EDICIÓN
                productoEnEdicion.setCodigo(codigo);
                productoEnEdicion.setNombre(nombre);
                productoEnEdicion.setCategoria(cmbCategoria.getValue());
                productoEnEdicion.setPrecioVenta(precio);
                productoEnEdicion.setExistencia(existencia);
                productoEnEdicion.setRutaImagen(rutaImagen);
                productoEnEdicion.setActivo(chkActivo.isSelected());

                productoDAO.actualizar(productoEnEdicion);
                mensaje(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            }

            refrescarYLimpiar();

        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Error de Formato", "Precio o existencia no válidos.");
        }
    }

    @FXML
    private void accionBotonSecundario() {
        if (productoEnEdicion != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("¿Está seguro de eliminar el producto?");
            confirmacion.setContentText("Producto: " + productoEnEdicion.getNombre() + "\nEsta acción no se puede deshacer.");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                productoDAO.eliminar(productoEnEdicion);
                mensaje(Alert.AlertType.INFORMATION, "Eliminado", "El producto se ha eliminado correctamente.");
                refrescarYLimpiar();
            }
        } else {
            refrescarYLimpiar();
        }
    }

    private void refrescarYLimpiar() {
        CategoriaDAO.getInstance().cargarCategoriasDesdeBD();
        productoDAO.cargarProductosDesdeBD();
        cmbFiltroCategoria.setItems(CategoriaDAO.getInstance().getListaCategorias());

        limpiarFormulario();
        aplicarFiltrosCombinados();
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        imgProducto.setImage(null);
        rutaImagen = null;

        productoEnEdicion = null;
        btnGuardar.setText("Guardar");
        btnRefrescar.setText("Refrescar");
        btnRefrescar.setStyle("");
    }

    private void aplicarFiltrosCombinados() {
        productosFiltrados.setPredicate(producto -> {
            if (producto == null) return false;

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
                }
            }

            if (!cumpleFiltro) return false;

            String textoBusqueda = txtBuscar.getText();
            if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
                return true;
            }

            textoBusqueda = textoBusqueda.trim();

            if (Character.isDigit(textoBusqueda.charAt(0))) {
                return producto.getId() != null && String.valueOf(producto.getId()).startsWith(textoBusqueda);
            } else {
                return producto.getNombre() != null && producto.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase());
            }
        });
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen del producto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File archivo = chooser.showOpenDialog((Stage) txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
        }
    }

    private void mensaje(Alert.AlertType tipo, String titulo, String texto) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}