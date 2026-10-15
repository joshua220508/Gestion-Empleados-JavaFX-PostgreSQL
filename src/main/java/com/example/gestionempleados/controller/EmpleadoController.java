package com.example.gestionempleados.controller;

import com.example.gestionempleados.database.ConexionBD;
import com.example.gestionempleados.model.Empleado;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controlador de la vista de gestión de empleados.
 *
 * Responsabilidades:
 * - Cargar empleados activos en el TableView.
 * - Registrar, actualizar y dar de baja empleados.
 * - Validar los datos introducidos por el usuario.
 * - Buscar usando hasta tres filtros combinados con AND.
 *
 * La base de datos conserva la responsabilidad final de proteger la integridad
 * mediante PRIMARY KEY, UNIQUE, NOT NULL y CHECK.
 */
public class EmpleadoController {

    // ----- Campos del formulario -----
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCargo;
    @FXML private TextField txtSalario;

    // ----- Tabla y columnas -----
    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colCodigo;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colCorreo;
    @FXML private TableColumn<Empleado, String> colCargo;
    @FXML private TableColumn<Empleado, BigDecimal> colSalario;

    // ----- Controles de búsqueda -----
    @FXML private ComboBox<String> cmbFiltro1;
    @FXML private ComboBox<String> cmbFiltro2;
    @FXML private ComboBox<String> cmbFiltro3;
    @FXML private TextField txtFiltro1;
    @FXML private TextField txtFiltro2;
    @FXML private TextField txtFiltro3;

    /*
     * Lista observable que alimenta el TableView.
     * Se reutiliza para reemplazar los resultados y evitar duplicados.
     */
    private final ObservableList<Empleado> empleados =
            FXCollections.observableArrayList();

    /*
     * Mapa cerrado de opciones visibles a nombres de columnas SQL.
     * Los nombres de columnas no se pueden parametrizar con ? en SQL.
     * Por eso solo permitimos estas opciones controladas por la aplicación.
     */
    private static final String[] OPCIONES_FILTRO = {
            "Código", "Nombre", "Correo", "Cargo", "Salario"
    };

    // ----- Inicialización de la vista -----

    @FXML
    private void initialize() {
        configurarColumnas();
        configurarFiltros();
        tablaEmpleados.setItems(empleados);
        cargarEmpleadosActivos();
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colSalario.setCellValueFactory(new PropertyValueFactory<>("salario"));
    }

    private void configurarFiltros() {
        cmbFiltro1.setItems(FXCollections.observableArrayList(OPCIONES_FILTRO));
        cmbFiltro2.setItems(FXCollections.observableArrayList(OPCIONES_FILTRO));
        cmbFiltro3.setItems(FXCollections.observableArrayList(OPCIONES_FILTRO));
    }

    // ----- Carga de datos -----

    /**
     * Consulta exclusivamente los empleados activos y reemplaza los datos
     * visibles en la tabla.
     */
    private void cargarEmpleadosActivos() {
        String sql = """
                SELECT id_empleado, codigo, nombre, correo, cargo,
                       salario, fecha_registro, activo
                FROM empleado
                WHERE activo = TRUE
                ORDER BY id_empleado
                """;

        List<Empleado> resultado = new ArrayList<>();

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet rs = sentencia.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapearEmpleado(rs));
            }

            empleados.setAll(resultado);

        } catch (SQLException e) {
            mostrarError("No se pudieron cargar los empleados.", e);
        }
    }

    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {
        Timestamp fecha = rs.getTimestamp("fecha_registro");

        return new Empleado(
                rs.getInt("id_empleado"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("cargo"),
                rs.getBigDecimal("salario"),
                fecha == null ? null : fecha.toLocalDateTime(),
                rs.getBoolean("activo")
        );
    }

    // ----- Registrar -----

    @FXML
    private void handleGuardar() {
        DatosFormulario datos = leerYValidarFormulario();
        if (datos == null) {
            return;
        }

        String sql = """
                INSERT INTO empleado (codigo, nombre, correo, cargo, salario)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, datos.codigo());
            sentencia.setString(2, datos.nombre());
            sentencia.setString(3, datos.correo());
            sentencia.setString(4, datos.cargo());
            sentencia.setBigDecimal(5, datos.salario());

            int filas = sentencia.executeUpdate();

            if (filas == 1) {
                mostrarInformacion("Empleado registrado correctamente.");
                cargarEmpleadosActivos();
                limpiarFormulario();
            }

        } catch (SQLException e) {
            manejarErrorSQL("No se pudo registrar el empleado.", e);
        }
    }


    @FXML
    private void handleActualizar() {
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un empleado de la tabla antes de actualizar.");
            return;
        }

        DatosFormulario datos = leerYValidarFormulario();
        if (datos == null) {
            return;
        }

        String sql = """
                UPDATE empleado
                SET codigo = ?, nombre = ?, correo = ?, cargo = ?, salario = ?
                WHERE id_empleado = ? AND activo = TRUE
                """;

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, datos.codigo());
            sentencia.setString(2, datos.nombre());
            sentencia.setString(3, datos.correo());
            sentencia.setString(4, datos.cargo());
            sentencia.setBigDecimal(5, datos.salario());
            sentencia.setInt(6, seleccionado.getIdEmpleado());

            int filas = sentencia.executeUpdate();

            if (filas == 1) {
                mostrarInformacion("Empleado actualizado correctamente.");
                cargarEmpleadosActivos();
                limpiarFormulario();
            } else {
                mostrarAdvertencia("No se actualizó ningún registro. Puede que ya no esté activo.");
                cargarEmpleadosActivos();
            }

        } catch (SQLException e) {
            manejarErrorSQL("No se pudo actualizar el empleado.", e);
        }
    }

    // ----- Baja lógica -----

    @FXML
    private void handleEliminar() {
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un empleado de la tabla antes de eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar baja");
        confirmacion.setHeaderText("Dar de baja al empleado");
        confirmacion.setContentText(
                "¿Deseas dar de baja a " + seleccionado.getNombre() + "?"
        );

        if (confirmacion.showAndWait().orElse(null)
                != javafx.scene.control.ButtonType.OK) {
            return;
        }

        String sql = """
                UPDATE empleado
                SET activo = FALSE
                WHERE id_empleado = ? AND activo = TRUE
                """;

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, seleccionado.getIdEmpleado());

            int filas = sentencia.executeUpdate();

            if (filas == 1) {
                mostrarInformacion("Empleado dado de baja. El registro se conserva en PostgreSQL.");
                cargarEmpleadosActivos();
                limpiarFormulario();
            } else {
                mostrarAdvertencia("No se encontró un empleado activo para dar de baja.");
                cargarEmpleadosActivos();
            }

        } catch (SQLException e) {
            mostrarError("No se pudo dar de baja al empleado.", e);
        }
    }


    @FXML
    private void handleLimpiar() {
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtCorreo.clear();
        txtCargo.clear();
        txtSalario.clear();
        tablaEmpleados.getSelectionModel().clearSelection();
    }

    // ----- Búsqueda con hasta tres filtros -----

    @FXML
    private void handleBuscar() {
        List<Filtro> filtros = new ArrayList<>();

        try {
            agregarFiltroSiCompleto(filtros, cmbFiltro1, txtFiltro1);
            agregarFiltroSiCompleto(filtros, cmbFiltro2, txtFiltro2);
            agregarFiltroSiCompleto(filtros, cmbFiltro3, txtFiltro3);
        } catch (FiltroIncompletoException e) {
            return;
        }

        if (filtros.isEmpty()) {
            cargarEmpleadosActivos();
            return;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT id_empleado, codigo, nombre, correo, cargo,
                       salario, fecha_registro, activo
                FROM empleado
                WHERE activo = TRUE
                """);

        for (Filtro filtro : filtros) {
            sql.append(" AND ").append(filtro.columnaSql());

            if (filtro.esSalario()) {
                sql.append(" = ?");
            } else {
                sql.append(" ILIKE ?");
            }
        }

        sql.append(" ORDER BY id_empleado");

        List<Empleado> resultado = new ArrayList<>();

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {

            for (int i = 0; i < filtros.size(); i++) {
                Filtro filtro = filtros.get(i);

                if (filtro.esSalario()) {
                    try {
                        BigDecimal salario = new BigDecimal(filtro.valor());
                        sentencia.setBigDecimal(i + 1, salario);
                    } catch (NumberFormatException e) {
                        mostrarAdvertencia(
                                "El filtro de salario debe ser un número válido, por ejemplo 1250.00."
                        );
                        return;
                    }
                } else {
                    sentencia.setString(i + 1, "%" + filtro.valor() + "%");
                }
            }

            try (ResultSet rs = sentencia.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearEmpleado(rs));
                }
            }

            empleados.setAll(resultado);

            if (resultado.isEmpty()) {
                mostrarInformacion("No se encontraron empleados activos con esos criterios.");
            }

        } catch (SQLException e) {
            mostrarError("No se pudo realizar la búsqueda.", e);
        }
    }

    private void agregarFiltroSiCompleto(
            List<Filtro> filtros,
            ComboBox<String> combo,
            TextField campo
    ) {
        String valor = campo.getText() == null ? "" : campo.getText().trim();

        // Los campos vacíos se ignoran, incluso si se eligió una columna.
        if (valor.isEmpty()) {
            return;
        }

        String opcion = combo.getValue();

        if (opcion == null) {
            mostrarAdvertencia("Selecciona una columna para cada criterio de búsqueda que completes.");
            throw new FiltroIncompletoException();
        }

        String columnaSql = switch (opcion) {
            case "Código" -> "codigo";
            case "Nombre" -> "nombre";
            case "Correo" -> "correo";
            case "Cargo" -> "cargo";
            case "Salario" -> "salario";
            default -> throw new IllegalArgumentException("Columna de filtro no permitida.");
        };

        filtros.add(new Filtro(columnaSql, valor, "Salario".equals(opcion)));
    }

    @FXML
    private void handleQuitarFiltros() {
        cmbFiltro1.getSelectionModel().clearSelection();
        cmbFiltro2.getSelectionModel().clearSelection();
        cmbFiltro3.getSelectionModel().clearSelection();

        txtFiltro1.clear();
        txtFiltro2.clear();
        txtFiltro3.clear();

        cargarEmpleadosActivos();
    }

    // ----- Validación del formulario -----

    private DatosFormulario leerYValidarFormulario() {
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String correo = txtCorreo.getText() == null ? "" : txtCorreo.getText().trim();
        String cargo = txtCargo.getText() == null ? "" : txtCargo.getText().trim();
        String textoSalario = txtSalario.getText() == null ? "" : txtSalario.getText().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || correo.isEmpty()
                || cargo.isEmpty() || textoSalario.isEmpty()) {
            mostrarAdvertencia("Todos los campos del formulario son obligatorios.");
            return null;
        }

        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            mostrarAdvertencia("Introduce un correo electrónico válido.");
            return null;
        }

        BigDecimal salario;
        try {
            salario = new BigDecimal(textoSalario);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("El salario debe ser numérico, por ejemplo 1250.00.");
            return null;
        }

        if (salario.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarAdvertencia("El salario debe ser mayor que cero.");
            return null;
        }

        return new DatosFormulario(codigo, nombre, correo, cargo, salario);
    }

    // ----- Mensajes y manejo de errores -----

    private void manejarErrorSQL(String mensaje, SQLException e) {
        String sqlState = e.getSQLState();

        if ("23505".equals(sqlState)) {
            mostrarAdvertencia(
                    "El código o el correo ya existe. Utiliza un valor diferente."
            );
        } else if ("23514".equals(sqlState)) {
            mostrarAdvertencia("Los datos incumplen una restricción CHECK de PostgreSQL.");
        } else if ("23502".equals(sqlState)) {
            mostrarAdvertencia("Falta un dato obligatorio.");
        } else {
            mostrarError(mensaje, e);
        }
    }

    private void mostrarInformacion(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Información");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Validación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje, SQLException e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de base de datos");
        alerta.setHeaderText(mensaje);
        alerta.setContentText(
                "Detalle: " + e.getMessage()
                        + "\nCódigo SQL: " + e.getSQLState()
        );
        alerta.showAndWait();
    }

    // ----- Tipos auxiliares privados -----

    private record DatosFormulario(
            String codigo,
            String nombre,
            String correo,
            String cargo,
            BigDecimal salario
    ) { }

    private record Filtro(
            String columnaSql,
            String valor,
            boolean esSalario
    ) { }

    /**
     * Se utiliza internamente para interrumpir la recolección de filtros
     * cuando hay un criterio escrito sin columna seleccionada.
     */
    private static class FiltroIncompletoException extends RuntimeException {
    }
}
