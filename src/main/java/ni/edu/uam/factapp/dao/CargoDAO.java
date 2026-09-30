package ni.edu.uam.factapp.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import ni.edu.uam.factapp.model.Cargo;
import ni.edu.uam.factapp.util.DatabaseConnector;

import java.sql.*;

public class CargoDAO {

    private static CargoDAO instance;
    @Getter
    private final ObservableList<Cargo> listaCargos;

    private CargoDAO() {
        this.listaCargos = FXCollections.observableArrayList();
        cargarCargosDesdeBD();
    }

    public static synchronized CargoDAO getInstance() {
        if (instance == null) {
            instance = new CargoDAO();
        }
        return instance;
    }

    // Carga todos los cargos registrados en PostgreSQL
    public void cargarCargosDesdeBD() {
        this.listaCargos.clear();
        String sql = "SELECT id, nombre, descripcion FROM cargo ORDER BY id ASC";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cargo cargo = new Cargo(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                );
                this.listaCargos.add(cargo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Inserta el nuevo cargo en PostgreSQL y actualiza la lista
    public boolean agregarCargo(Cargo cargo) {
        String sql = "INSERT INTO cargo (nombre, descripcion) VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cargo.getNombre());
            ps.setString(2, cargo.getDescripcion());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cargarCargosDesdeBD();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Valida si un nombre de cargo ya existe en la lista
    public boolean existeNombre(String nombre) {
        return listaCargos.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre.trim()));
    }

    // Recupera un objeto Cargo por su ID
    public Cargo obtenerPorId(int id) {
        return listaCargos.stream()
                .filter(c -> c.getId() != null && c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}