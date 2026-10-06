package ni.edu.uam.factapp.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {

    public static void vaciarTodasLasTablas() throws SQLException {
        // Orden importante para no romper Foreign Keys:
        // 1. Tablas que dependen de otras (Producto, Empleado)
        // 2. Tablas independientes / padre (Categoria, Cargo)
        String[] sqlQueries = {
                "DELETE FROM producto",
                "DELETE FROM empleado",
                "DELETE FROM categoria",
                "DELETE FROM cargo"
        };

        // Si tu motor de BD maneja Autoincrement / Identity y quieres reiniciar los IDs a 1,
        // en PostgreSQL / MySQL / SQL Server puedes usar TRUNCATE con CASCADE según el motor:
        // TRUNCATE TABLE producto, empleado, categoria, cargo RESTART IDENTITY CASCADE;

        try (Connection conn = DatabaseConnector.connect(); // Ajusta según tu clase de conexión
             Statement stmt = conn.createStatement()) {

            // Desactivamos autocommit para ejecutar todo dentro de una sola transacción
            conn.setAutoCommit(false);

            try {
                for (String query : sqlQueries) {
                    stmt.executeUpdate(query);
                }
                // Confirmamos la eliminación masiva
                conn.commit();
            } catch (SQLException e) {
                // Si ocurre algún fallo, revertimos los cambios
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}