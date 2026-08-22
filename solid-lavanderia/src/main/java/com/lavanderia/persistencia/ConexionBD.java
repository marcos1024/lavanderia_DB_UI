package com.lavanderia.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto unico de acceso a la conexion JDBC.
 * Lee host/usuario/password desde db.properties (classpath), asi los datos
 * de conexion no quedan hardcodeados en el codigo.
 */
public final class ConexionBD {
    private static final Properties PROPIEDADES = cargarPropiedades();

    private ConexionBD() {
    }

    private static Properties cargarPropiedades() {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("No se encontro db.properties en el classpath (src/main/resources)");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer db.properties", e);
        }
        return props;
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(
                PROPIEDADES.getProperty("db.url"),
                PROPIEDADES.getProperty("db.user"),
                PROPIEDADES.getProperty("db.password"));
    }
}
