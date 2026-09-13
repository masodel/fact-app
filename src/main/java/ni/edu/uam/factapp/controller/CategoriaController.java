package ni.edu.uam.factapp.controller;

import javafx.beans.property.SimpleStringProperty;
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
                new SimpleStringProperty(cellData.getValue().isActiva() ? "Activa" : "Inactiva")
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
}