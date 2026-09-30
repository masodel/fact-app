package ni.edu.uam.factapp.util;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class DialogoBuscar {

    public enum CriterioBusqueda {
        ID, NOMBRE
    }

    public static class ResultadoBusqueda {
        private final CriterioBusqueda criterio;
        private final String valor;

        public ResultadoBusqueda(CriterioBusqueda criterio, String valor) {
            this.criterio = criterio;
            this.valor = valor;
        }

        public CriterioBusqueda getCriterio() { return criterio; }
        public String getValor() { return valor; }
    }

    public static ResultadoBusqueda mostrar(String tituloEntidad) {
        Dialog<ResultadoBusqueda> dialog = new Dialog<>();
        dialog.setTitle("Buscar " + tituloEntidad);
        dialog.setHeaderText("Seleccione el criterio e ingrese el valor a buscar:");

        ButtonType btnBuscarType = new ButtonType("Buscar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnBuscarType, ButtonType.CANCEL);

        ToggleGroup group = new ToggleGroup();
        RadioButton rbId = new RadioButton("Buscar por ID");
        rbId.setToggleGroup(group);
        rbId.setSelected(true);

        RadioButton rbNombre = new RadioButton("Buscar por Nombre");
        rbNombre.setToggleGroup(group);

        TextField txtValor = new TextField();
        txtValor.setPromptText("Ingrese el ID o Nombre...");

        VBox content = new VBox(10, rbId, rbNombre, txtValor);
        content.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(content);

        // Deshabilitar botón de aceptar si el campo de texto está vacío
        dialog.getDialogPane().lookupButton(btnBuscarType).setDisable(true);
        txtValor.textProperty().addListener((obs, oldVal, newVal) -> {
            dialog.getDialogPane().lookupButton(btnBuscarType).setDisable(newVal.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnBuscarType) {
                CriterioBusqueda criterio = rbId.isSelected() ? CriterioBusqueda.ID : CriterioBusqueda.NOMBRE;
                return new ResultadoBusqueda(criterio, txtValor.getText().trim());
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}