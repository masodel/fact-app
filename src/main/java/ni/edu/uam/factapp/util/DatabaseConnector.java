package ni.edu.uam.factapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/fact-app";

    private static final String USER =
            "postgres";

    private static final String PASSWORD =
            "bd1234";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}