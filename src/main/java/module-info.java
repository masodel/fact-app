module ni.edu.uam.factapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    exports ni.edu.uam.factapp.application;
    exports ni.edu.uam.factapp.model;
    opens ni.edu.uam.factapp.controller to javafx.fxml;
    opens ni.edu.uam.factapp.model to javafx.base;
}