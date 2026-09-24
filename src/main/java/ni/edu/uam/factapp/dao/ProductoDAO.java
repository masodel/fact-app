package ni.edu.uam.factapp.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ni.edu.uam.factapp.model.Categoria;
import ni.edu.uam.factapp.model.Producto;
import ni.edu.uam.factapp.util.DatabaseConnector;

import java.sql.*;

public class ProductoDAO {

    private static ProductoDAO instance;
    private final ObservableList<Producto> productos;

    private ProductoDAO() {
        this.productos = FXCollections.observableArrayList();
        cargarProductosDesdeBD();
    }

    public static synchronized ProductoDAO getInstance() {
        if (instance == null) {
            instance = new ProductoDAO();
        }
        return instance;
    }

    public ObservableList<Producto> getProductos() {
        return productos;
    }

    // Carga los productos usando 'categoria_id'
    public void cargarProductosDesdeBD() {
        this.productos.clear();
        String sql = "SELECT id, codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo " +
                "FROM producto ORDER BY id ASC";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            CategoriaDAO categoriaDAO = CategoriaDAO.getInstance();

            while (rs.next()) {
                int idCategoria = rs.getInt("categoria_id");
                Categoria categoria = categoriaDAO.obtenerPorId(idCategoria);

                Producto producto = new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        categoria,
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("existencia"),
                        rs.getString("ruta_imagen"),
                        rs.getBoolean("activo")
                );
                this.productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Inserta el producto utilizando 'categoria_id'
    public boolean agregar(Producto producto) {
        String sql = "INSERT INTO producto (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    producto.setId(rs.getInt("id"));
                    this.productos.add(producto);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}