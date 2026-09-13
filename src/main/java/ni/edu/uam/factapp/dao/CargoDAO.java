package ni.edu.uam.factapp.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import ni.edu.uam.factapp.model.Cargo;

public class CargoDAO {

    private static CargoDAO instance;
    // Retorna la lista observable para enlazarla directamente a la TableView
    @Getter
    private final ObservableList<Cargo> listaCargos;
    private int autoincrementId = 1;

    // Constructor privado para el patrón Singleton
    private CargoDAO() {
        this.listaCargos = FXCollections.observableArrayList();
        // Carga de datos iniciales opcionales
        agregarCargo(new Cargo(autoincrementId++, "Administrador", "Acceso total al sistema"));
        agregarCargo(new Cargo(autoincrementId++, "Cajero", "Encargado de facturación y cobro"));
    }

    // Obtener la instancia única de CargoDAO
    public static synchronized CargoDAO getInstance() {
        if (instance == null) {
            instance = new CargoDAO();
        }
        return instance;
    }

    // Agregar un nuevo cargo a la lista compartida
    public void agregarCargo(Cargo cargo) {
        if (cargo.getId() == null) {
            cargo.setId(autoincrementId++);
        }
        this.listaCargos.add(cargo);
    }
    // Verificar si existe un cargo registrado con el mismo nombre
    public boolean existeNombre(String nombre) {
        return listaCargos.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre.trim()));
    }

}