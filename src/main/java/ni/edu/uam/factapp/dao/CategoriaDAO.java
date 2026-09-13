package ni.edu.uam.factapp.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import ni.edu.uam.factapp.model.Categoria;

public class CategoriaDAO {

    private static CategoriaDAO instance;
    @Getter
    private final ObservableList<Categoria> listaCategorias;
    private int autoincrementId = 1;

    private CategoriaDAO() {
        this.listaCategorias = FXCollections.observableArrayList();
        // Carga de datos iniciales de ejemplo
        agregarCategoria(new Categoria(autoincrementId++, "Bebidas", true));
        agregarCategoria(new Categoria(autoincrementId++, "Lácteos", true));
        agregarCategoria(new Categoria(autoincrementId++, "Snacks", false));
    }

    public static synchronized CategoriaDAO getInstance() {
        if (instance == null) {
            instance = new CategoriaDAO();
        }
        return instance;
    }

    public void agregarCategoria(Categoria categoria) {
        if (categoria.getId() == null) {
            categoria.setId(autoincrementId++);
        }
        this.listaCategorias.add(categoria);
    }

    public boolean existeNombre(String nombre) {
        return listaCategorias.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre.trim()));
    }
}