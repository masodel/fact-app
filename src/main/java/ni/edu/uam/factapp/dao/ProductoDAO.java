package ni.edu.uam.factapp.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ni.edu.uam.factapp.model.Producto;

public class ProductoDAO {

    private static ProductoDAO instance;
    private final ObservableList<Producto> productos;

    private ProductoDAO() {
        this.productos = FXCollections.observableArrayList();
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

    public void agregar(Producto producto) {
        this.productos.add(producto);
    }
}
