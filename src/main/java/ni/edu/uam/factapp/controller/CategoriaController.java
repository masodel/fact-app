package ni.edu.uam.factapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.factapp.dao.CategoriaDAO;
import ni.edu.uam.factapp.model.Categoria;
import ni.edu.uam.factapp.util.DialogoBuscar;

import java.net.URL;
import java.util.ResourceBundle;

public class CategoriaController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;
    @FXML private Button btnAgregar;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colActiva;

    private CategoriaDAO categoriaDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categoriaDAO = CategoriaDAO.getInstance();

        // Mapear nombre
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        // Formatear el boolean activa a un texto legible ("Activa" / "Inactiva")
        colActiva.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActiva() ? "Si" : "No")
        );

        // Enlazar la lista observable a la tabla
        tblCategorias.setItems(categoriaDAO.getListaCategorias());
    }

    @FXML
    private void guardarCategoria() {
        String nombre = txtNombre.getText().trim();
        boolean activa = chkActiva.isSelected();

        // 1. Validación de nombre vacío
        if (nombre.isEmpty()) {
            mostrarAlerta(
                    "Campo Incompleto",
                    "El campo nombre de la categoría no puede estar vacío.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 2. Validación de duplicados
        if (categoriaDAO.existeNombre(nombre)) {
            mostrarAlerta(
                    "Categoría Duplicada",
                    "Ya existe una categoría registrada con el nombre '" + nombre + "'.",
                    Alert.AlertType.ERROR
            );
            txtNombre.requestFocus();
            return;
        }

        // Crear y guardar la categoría
        Categoria nuevaCategoria = new Categoria(null, nombre, activa);
        categoriaDAO.agregarCategoria(nuevaCategoria);

        limpiarCampos();
    }

    private void limpiarCampos() {
        txtNombre.clear();
        chkActiva.setSelected(true); // Se deja marcado por defecto como "Activa"
        txtNombre.requestFocus();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void refrescar() {
        // Vuelve a consultar PostgreSQL y actualiza la lista
        categoriaDAO.cargarCategoriasDesdeBD();
    }

    @FXML
    private void buscarCategoria() {
        DialogoBuscar.ResultadoBusqueda res = DialogoBuscar.mostrar("Categoría");
        if (res == null) return;

        Categoria encontrada = null;

        if (res.getCriterio() == DialogoBuscar.CriterioBusqueda.ID) {
            try {
                int id = Integer.parseInt(res.getValor());
                encontrada = categoriaDAO.obtenerPorId(id);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error de Formato", "El ID ingresado debe ser un número entero válido.", Alert.AlertType.ERROR);
                return;
            }
        } else {
            encontrada = categoriaDAO.getListaCategorias().stream()
                    .filter(c -> c.getNombre() != null && c.getNombre().equalsIgnoreCase(res.getValor()))
                    .findFirst()
                    .orElse(null);
        }

        if (encontrada != null) {
            // Cargar todos los atributos en el formulario
            txtNombre.setText(encontrada.getNombre());
            chkActiva.setSelected(encontrada.isActiva());

            // Seleccionar y enfocar en la tabla
            tblCategorias.getSelectionModel().select(encontrada);
            tblCategorias.scrollTo(encontrada);

            // Mostrar todos los atributos
            String info = String.format("Categoría Encontrada:\n\nID: %d\nNombre: %s\nEstado: %s",
                    encontrada.getId(), encontrada.getNombre(), encontrada.isActiva() ? "Activa" : "Inactiva");
            mostrarAlerta("Resultado de Búsqueda", info, Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("No Encontrado", "No se encontró ninguna categoría con los datos proporcionados.", Alert.AlertType.ERROR);
        }
    }

}