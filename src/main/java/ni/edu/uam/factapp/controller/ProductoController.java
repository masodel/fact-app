package ni.edu.uam.factapp.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.transformation.FilteredList;

import ni.edu.uam.factapp.dao.CategoriaDAO;
import ni.edu.uam.factapp.dao.ProductoDAO;
import ni.edu.uam.factapp.model.Categoria;
import ni.edu.uam.factapp.model.Producto;
import ni.edu.uam.factapp.util.DialogoBuscar;

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

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, String> colActivo;

    private final ProductoDAO productoDAO = ProductoDAO.getInstance();
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

        FilteredList<Categoria> categoriasActivas = new FilteredList<>(
                CategoriaDAO.getInstance().getListaCategorias(),
                Categoria::isActiva
        );

        cmbCategoria.setItems(categoriasActivas);

        // Vincula el TableView directamente a la lista única del DAO
        tblProductos.setItems(productoDAO.getProductos());

        chkActivo.setSelected(true);
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

            // Se agrega al DAO, lo que notifica automáticamente a la vista
            productoDAO.agregar(producto);

            mensaje(Alert.AlertType.INFORMATION, "Producto agregado correctamente.");
            limpiar();

        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
        }
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtCodigo.getScene().getWindow();
        stage.close();
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

    @FXML
    private void refrescar() {
        CategoriaDAO.getInstance().cargarCategoriasDesdeBD();
        productoDAO.cargarProductosDesdeBD();
    }

    @FXML
    private void buscarProducto() {
        DialogoBuscar.ResultadoBusqueda res = DialogoBuscar.mostrar("Producto");
        if (res == null) return;

        Producto encontrado = null;

        if (res.getCriterio() == DialogoBuscar.CriterioBusqueda.ID) {
            try {
                int id = Integer.parseInt(res.getValor());
                encontrado = productoDAO.getProductos().stream()
                        .filter(p -> p.getId() != null && p.getId() == id)
                        .findFirst()
                        .orElse(null);
            } catch (NumberFormatException e) {
                mensaje(Alert.AlertType.ERROR, "El ID ingresado debe ser un número entero válido.");
                return;
            }
        } else {
            encontrado = productoDAO.getProductos().stream()
                    .filter(p -> p.getNombre() != null && p.getNombre().equalsIgnoreCase(res.getValor()))
                    .findFirst()
                    .orElse(null);
        }

        if (encontrado != null) {
            // Cargar todos los atributos en los campos de la interfaz
            txtCodigo.setText(encontrado.getCodigo());
            txtNombre.setText(encontrado.getNombre());
            cmbCategoria.setValue(encontrado.getCategoria());
            txtPrecio.setText(encontrado.getPrecioVenta() != null ? encontrado.getPrecioVenta().toString() : "0.00");
            txtExistencia.setText(String.valueOf(encontrado.getExistencia()));
            chkActivo.setSelected(encontrado.isActivo());

            if (encontrado.getRutaImagen() != null && !encontrado.getRutaImagen().isBlank()) {
                this.rutaImagen = encontrado.getRutaImagen();
                try {
                    imgProducto.setImage(new Image(rutaImagen));
                } catch (Exception e) {
                    imgProducto.setImage(null);
                }
            } else {
                imgProducto.setImage(null);
                this.rutaImagen = null;
            }

            // Seleccionar y enfocar en la TableView
            tblProductos.getSelectionModel().select(encontrado);
            tblProductos.scrollTo(encontrado);

            // Mostrar todos los atributos
            String detalles = String.format(
                    "Producto Encontrado:\n\nID: %d\nCódigo: %s\nNombre: %s\nCategoría: %s\nPrecio Venta: %s\nExistencia: %d\nRuta Imagen: %s\nEstado: %s",
                    encontrado.getId(),
                    encontrado.getCodigo(),
                    encontrado.getNombre(),
                    encontrado.getCategoria() != null ? encontrado.getCategoria().getNombre() : "Sin Categoría",
                    encontrado.getPrecioVenta(),
                    encontrado.getExistencia(),
                    encontrado.getRutaImagen() != null ? encontrado.getRutaImagen() : "Ninguna",
                    encontrado.isActivo() ? "Activo" : "Inactivo"
            );

            mensaje(Alert.AlertType.INFORMATION, detalles);
        } else {
            mensaje(Alert.AlertType.ERROR, "No se encontró ningún producto con los datos ingresados.");
        }
    }
}