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

    public boolean actualizar(Cargo cargo) {
        String sql = "UPDATE cargo SET nombre = ?, descripcion = ? WHERE id = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cargo.getNombre());
            ps.setString(2, cargo.getDescripcion());
            ps.setInt(3, cargo.getId());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                cargarCargosDesdeBD();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean eliminar(Cargo cargo) {
        String sql = "DELETE FROM cargo WHERE id = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cargo.getId());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                cargarCargosDesdeBD();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existeNombre(String nombre) {
        return listaCargos.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre.trim()));
    }

    public Cargo obtenerPorId(int id) {
        return listaCargos.stream()
                .filter(c -> c.getId() != null && c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Valida si un nombre de cargo ya existe (ignorando un ID específico en caso de edición)
    public boolean existeNombre(String nombre, Integer idActual) {
        return listaCargos.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre.trim())
                        && (idActual == null || !c.getId().equals(idActual)));
    }
}