package ni.edu.uam.factapp.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import ni.edu.uam.factapp.model.Categoria;
import ni.edu.uam.factapp.util.DatabaseConnector;

import java.sql.*;

public class CategoriaDAO {

    private static CategoriaDAO instance;
    @Getter
    private final ObservableList<Categoria> listaCategorias;

    private CategoriaDAO() {
        this.listaCategorias = FXCollections.observableArrayList();
        cargarCategoriasDesdeBD();
    }

    public static synchronized CategoriaDAO getInstance() {
        if (instance == null) {
            instance = new CategoriaDAO();
        }
        return instance;
    }

    public void cargarCategoriasDesdeBD() {
        this.listaCategorias.clear();
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY id ASC";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria cat = new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBoolean("activa")
                );
                this.listaCategorias.add(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean agregarCategoria(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cargarCategoriasDesdeBD();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());
            ps.setInt(3, categoria.getId());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                cargarCategoriasDesdeBD();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean eliminar(Categoria categoria) {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoria.getId());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                cargarCategoriasDesdeBD();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existeNombre(String nombre) {
        return listaCategorias.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre.trim()));
    }

    public Categoria obtenerPorId(int id) {
        return listaCategorias.stream()
                .filter(c -> c.getId() != null && c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Valida si un nombre de categoría ya existe (ignorando un ID específico en caso de edición)
    public boolean existeNombre(String nombre, Integer idActual) {
        return listaCategorias.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre.trim())
                        && (idActual == null || !c.getId().equals(idActual)));
    }
}