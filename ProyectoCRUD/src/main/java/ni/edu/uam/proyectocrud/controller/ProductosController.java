
package ni.edu.uam.proyectocrud.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import ni.edu.uam.proyectocrud.database.ConexionBD;
import ni.edu.uam.proyectocrud.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductosController {

    private static final Logger LOGGER =
            Logger.getLogger(ProductosController.class.getName());

    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtStock;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbMarca;
    @FXML private DatePicker dtpFechaCreacion;
    @FXML private TextField txtDescripcion;

    @FXML private TextField txtBuscar;
    @FXML private DatePicker dtpDesde;
    @FXML private TextField txtPrecioMin;
    @FXML private ComboBox<String> cmbCategoriaBus;
    @FXML private DatePicker dtpHasta;
    @FXML private TextField txtPrecioMax;
    @FXML private CheckBox chkProductosExistentes;
    @FXML private ComboBox<String> cmbOrdenarPor;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Date> colFechaCreacion;

    private final ObservableList<Producto> productos =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarCMBCategoria();
        configurarCMBMarca();
        configurarTblProductos();

        tblProductos.setItems(productos);

        cmbOrdenarPor.getItems().setAll(
                "Nombre: A-Z",
                "Nombre: Z-A",
                "Precio: Menor a Mayor",
                "Precio: Mayor a Menor",
                "Stock: Menor a Mayor",
                "Stock: Mayor a Menor",
                "Fecha de Creación: Más reciente",
                "Fecha de Creación: Más antigua"
        );

        cmbCategoriaBus.getItems().setAll(
                cmbCategoria.getItems()
        );

        tblProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        llenarFormulario(seleccionado);
                    }
                });

        actualizarTabla();
    }

    // Cargar categorías desde PostgreSQL
    private void configurarCMBCategoria() {
        cmbCategoria.getItems().clear();

        String sql =
                "SELECT nombre FROM catalogos.categorias " +
                        "WHERE estado = 1 ORDER BY nombre ASC";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                cmbCategoria.getItems().add(
                        resultSet.getString("nombre")
                );
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar categorías", ex);
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar las categorías",
                    ex.getMessage());
        }
    }

    // Cargar marcas desde PostgreSQL
    private void configurarCMBMarca() {
        cmbMarca.getItems().clear();

        String sql =
                "SELECT nombre FROM catalogos.marcas " +
                        "WHERE estado = 1 ORDER BY nombre ASC";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                cmbMarca.getItems().add(
                        resultSet.getString("nombre")
                );
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar marcas", ex);
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar las marcas",
                    ex.getMessage());
        }
    }

    // Configurar columnas de la tabla
    private void configurarTblProductos() {
        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(
                new PropertyValueFactory<>("categoria"));
        colMarca.setCellValueFactory(
                new PropertyValueFactory<>("marca"));
        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(
                new PropertyValueFactory<>("stock"));
        colFechaCreacion.setCellValueFactory(
                new PropertyValueFactory<>("fechaCreacion"));
    }

    // Preparar el formulario para un nuevo producto
    @FXML
    private void nuevoProducto() {
        limpiar();
    }

    // Guardar producto
    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }

        String sql =
                "INSERT INTO catalogos.productos " +
                        "(codigo, nombre, descripcion, precio, stock, " +
                        "fecha_creacion, id_categoria, id_marca, estado) " +
                        "VALUES (?, ?, ?, ?, ?, ?, " +
                        "(SELECT id_categoria FROM catalogos.categorias " +
                        "WHERE nombre = ?), " +
                        "(SELECT id_marca FROM catalogos.marcas " +
                        "WHERE nombre = ?), 1)";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, txtCodigo.getText().trim());
            statement.setString(2, txtNombre.getText().trim());
            statement.setString(3, txtDescripcion.getText().trim());
            statement.setDouble(4,
                    Double.parseDouble(txtPrecio.getText().trim()));
            statement.setInt(5,
                    Integer.parseInt(txtStock.getText().trim()));
            statement.setDate(6,
                    Date.valueOf(dtpFechaCreacion.getValue()));
            statement.setString(7, cmbCategoria.getValue());
            statement.setString(8, cmbMarca.getValue());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                        "Producto guardado correctamente",
                        "El producto ha sido guardado con éxito.");

                limpiar();
                actualizarTabla();
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Atención",
                        "Producto no guardado",
                        "No se insertó ningún registro.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al guardar producto", ex);
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo guardar el producto",
                    ex.getMessage());
        }
    }

    // Actualizar producto seleccionado
    @FXML
    private void actualizar() {
        Producto seleccionado =
                tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención",
                    "Sin selección",
                    "Selecciona un producto antes de actualizar.");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        String sql =
                "UPDATE catalogos.productos SET " +
                        "codigo = ?, nombre = ?, descripcion = ?, " +
                        "precio = ?, stock = ?, fecha_creacion = ?, " +
                        "id_categoria = (SELECT id_categoria " +
                        "FROM catalogos.categorias WHERE nombre = ?), " +
                        "id_marca = (SELECT id_marca " +
                        "FROM catalogos.marcas WHERE nombre = ?) " +
                        "WHERE id_producto = ? AND estado = 1";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, txtCodigo.getText().trim());
            statement.setString(2, txtNombre.getText().trim());
            statement.setString(3, txtDescripcion.getText().trim());
            statement.setDouble(4,
                    Double.parseDouble(txtPrecio.getText().trim()));
            statement.setInt(5,
                    Integer.parseInt(txtStock.getText().trim()));
            statement.setDate(6,
                    Date.valueOf(dtpFechaCreacion.getValue()));
            statement.setString(7, cmbCategoria.getValue());
            statement.setString(8, cmbMarca.getValue());
            statement.setInt(9, seleccionado.getIdProducto());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                        "Producto actualizado",
                        "El producto ha sido actualizado correctamente.");

                limpiar();
                actualizarTabla();
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Atención",
                        "Sin cambios",
                        "No se actualizó ningún producto activo.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al actualizar producto", ex);
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo actualizar el producto",
                    ex.getMessage());
        }
    }

    // Eliminación lógica del producto
    @FXML
    private void eliminar() {
        Producto seleccionado =
                tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención",
                    "Sin selección",
                    "Selecciona un producto antes de eliminar.");
            return;
        }

        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(
                "¿Deseas eliminar el producto " +
                        seleccionado.getNombre() + "?");
        confirmacion.setContentText(
                "El producto dejará de mostrarse entre los activos.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        String sql =
                "UPDATE catalogos.productos " +
                        "SET estado = 0 " +
                        "WHERE id_producto = ? AND estado = 1";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, seleccionado.getIdProducto());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                        "Producto eliminado",
                        "El producto se desactivó correctamente.");

                limpiar();
                actualizarTabla();
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Atención",
                        "Producto no eliminado",
                        "El producto no existe o ya está inactivo.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al eliminar producto", ex);
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo eliminar el producto",
                    ex.getMessage());
        }
    }

    // Copiar datos de la tabla al formulario
    private void llenarFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(Double.toString(p.getPrecio()));
        txtStock.setText(Integer.toString(p.getStock()));

        cmbCategoria.setValue(p.getCategoria());
        cmbMarca.setValue(p.getMarca());

        if (p.getFechaCreacion() != null) {
            dtpFechaCreacion.setValue(
                    p.getFechaCreacion().toLocalDate());
        } else {
            dtpFechaCreacion.setValue(null);
        }
    }

    // Actualizar tabla con productos activos
    @FXML
    private void actualizarTabla() {
        String sql =
                "SELECT p.id_producto, p.codigo, p.nombre, " +
                        "p.descripcion, p.precio, p.stock, " +
                        "p.fecha_creacion, p.id_categoria, p.id_marca, " +
                        "p.estado, c.nombre AS nombre_categoria, " +
                        "m.nombre AS nombre_marca " +
                        "FROM catalogos.productos p " +
                        "INNER JOIN catalogos.categorias c " +
                        "ON p.id_categoria = c.id_categoria " +
                        "INNER JOIN catalogos.marcas m " +
                        "ON p.id_marca = m.id_marca " +
                        "WHERE p.estado = 1 " +
                        "ORDER BY p.id_producto ASC";

        cargarProductos(sql);
    }

    // Ejecutar consulta y llenar la tabla
    private void cargarProductos(String sql) {
        productos.clear();

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Producto producto = new Producto();

                producto.setIdProducto(
                        resultSet.getInt("id_producto"));
                producto.setCodigo(
                        resultSet.getString("codigo"));
                producto.setNombre(
                        resultSet.getString("nombre"));
                producto.setDescripcion(
                        resultSet.getString("descripcion"));
                producto.setPrecio(
                        resultSet.getDouble("precio"));
                producto.setStock(
                        resultSet.getInt("stock"));

                Date fecha = resultSet.getDate("fecha_creacion");
                producto.setFechaCreacion(fecha);

                producto.setIdCategoria(
                        resultSet.getInt("id_categoria"));
                producto.setIdMarca(
                        resultSet.getInt("id_marca"));
                producto.setEstado(
                        resultSet.getInt("estado") == 1);

                producto.setCategoria(
                        resultSet.getString("nombre_categoria"));
                producto.setMarca(
                        resultSet.getString("nombre_marca"));

                productos.add(producto);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar productos", ex);
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar los productos",
                    ex.getMessage());
        }
    }

    // Validar campos obligatorios y valores numéricos
    private boolean validarCampos() {
        if (txtCodigo.getText().isBlank()
                || txtNombre.getText().isBlank()
                || txtDescripcion.getText().isBlank()
                || txtPrecio.getText().isBlank()
                || txtStock.getText().isBlank()
                || dtpFechaCreacion.getValue() == null
                || cmbCategoria.getValue() == null
                || cmbMarca.getValue() == null) {

            mostrarAlerta(Alert.AlertType.WARNING, "Atención",
                    "Campos incompletos",
                    "Completa todos los campos obligatorios.");
            return false;
        }

        try {
            double precio =
                    Double.parseDouble(txtPrecio.getText().trim());
            int stock =
                    Integer.parseInt(txtStock.getText().trim());

            if (!Double.isFinite(precio)
                    || precio <= 0
                    || stock < 0) {

                mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                        "Valores inválidos",
                        "El precio debe ser mayor que cero y el stock " +
                                "debe ser un entero no negativo.");
                return false;
            }

        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "Valores inválidos",
                    "El precio debe ser numérico y el stock un entero.");
            return false;
        }

        return true;
    }

    // Limpiar formulario y selección
    @FXML
    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();

        dtpFechaCreacion.setValue(null);
        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);

        tblProductos.getSelectionModel().clearSelection();
        txtCodigo.requestFocus();
    }

    // Mostrar alertas
    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String encabezado,
            String mensaje) {

        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Buscar y filtrar productos
    @FXML
    private void buscarProducto() {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id_producto, p.codigo, p.nombre, " +
                        "p.descripcion, p.precio, p.stock, " +
                        "p.fecha_creacion, p.id_categoria, p.id_marca, " +
                        "p.estado, c.nombre AS nombre_categoria, " +
                        "m.nombre AS nombre_marca " +
                        "FROM catalogos.productos p " +
                        "INNER JOIN catalogos.categorias c " +
                        "ON p.id_categoria = c.id_categoria " +
                        "INNER JOIN catalogos.marcas m " +
                        "ON p.id_marca = m.id_marca " +
                        "WHERE p.estado = 1"
        );

        List<Object> parametros = new ArrayList<>();

        String busqueda = txtBuscar.getText().trim();

        if (!busqueda.isEmpty()) {
            sql.append(" AND (p.codigo ILIKE ? OR p.nombre ILIKE ?)");
            parametros.add("%" + busqueda + "%");
            parametros.add("%" + busqueda + "%");
        }

        if (cmbCategoriaBus.getValue() != null) {
            sql.append(" AND c.nombre = ?");
            parametros.add(cmbCategoriaBus.getValue());
        }

        if (dtpDesde.getValue() != null
                && dtpHasta.getValue() != null
                && dtpDesde.getValue().isAfter(dtpHasta.getValue())) {

            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "Rango de fechas inválido",
                    "La fecha inicial no puede ser posterior a la final.");
            return;
        }

        if (dtpDesde.getValue() != null) {
            sql.append(" AND p.fecha_creacion >= ?");
            parametros.add(Date.valueOf(dtpDesde.getValue()));
        }

        if (dtpHasta.getValue() != null) {
            sql.append(" AND p.fecha_creacion <= ?");
            parametros.add(Date.valueOf(dtpHasta.getValue()));
        }

        Double minimo = null;
        Double maximo = null;

        try {
            if (!txtPrecioMin.getText().isBlank()) {
                minimo = Double.parseDouble(
                        txtPrecioMin.getText().trim());
            }

            if (!txtPrecioMax.getText().isBlank()) {
                maximo = Double.parseDouble(
                        txtPrecioMax.getText().trim());
            }

            if ((minimo != null
                    && (!Double.isFinite(minimo) || minimo < 0))
                    || (maximo != null
                    && (!Double.isFinite(maximo) || maximo < 0))) {

                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "Precio inválido",
                    "Los precios deben ser números no negativos.");
            return;
        }

        if (minimo != null && maximo != null && minimo > maximo) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "Rango de precios inválido",
                    "El precio mínimo no puede superar al máximo.");
            return;
        }

        if (minimo != null) {
            sql.append(" AND p.precio >= ?");
            parametros.add(minimo);
        }

        if (maximo != null) {
            sql.append(" AND p.precio <= ?");
            parametros.add(maximo);
        }

        if (chkProductosExistentes.isSelected()) {
            sql.append(" AND p.stock > 0");
        }

        String orden = cmbOrdenarPor.getValue();

        if ("Nombre: A-Z".equals(orden)) {
            sql.append(" ORDER BY p.nombre ASC");
        } else if ("Nombre: Z-A".equals(orden)) {
            sql.append(" ORDER BY p.nombre DESC");
        } else if ("Precio: Menor a Mayor".equals(orden)) {
            sql.append(" ORDER BY p.precio ASC");
        } else if ("Precio: Mayor a Menor".equals(orden)) {
            sql.append(" ORDER BY p.precio DESC");
        } else if ("Stock: Menor a Mayor".equals(orden)) {
            sql.append(" ORDER BY p.stock ASC");
        } else if ("Stock: Mayor a Menor".equals(orden)) {
            sql.append(" ORDER BY p.stock DESC");
        } else if ("Fecha de Creación: Más reciente".equals(orden)) {
            sql.append(" ORDER BY p.fecha_creacion DESC");
        } else if ("Fecha de Creación: Más antigua".equals(orden)) {
            sql.append(" ORDER BY p.fecha_creacion ASC");
        } else {
            sql.append(" ORDER BY p.id_producto ASC");
        }

        productos.clear();

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                statement.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Producto producto = new Producto();

                    producto.setIdProducto(
                            resultSet.getInt("id_producto"));
                    producto.setCodigo(
                            resultSet.getString("codigo"));
                    producto.setNombre(
                            resultSet.getString("nombre"));
                    producto.setDescripcion(
                            resultSet.getString("descripcion"));
                    producto.setPrecio(
                            resultSet.getDouble("precio"));
                    producto.setStock(
                            resultSet.getInt("stock"));

                    producto.setFechaCreacion(
                            resultSet.getDate("fecha_creacion"));

                    producto.setIdCategoria(
                            resultSet.getInt("id_categoria"));
                    producto.setIdMarca(
                            resultSet.getInt("id_marca"));
                    producto.setEstado(
                            resultSet.getInt("estado") == 1);

                    producto.setCategoria(
                            resultSet.getString("nombre_categoria"));
                    producto.setMarca(
                            resultSet.getString("nombre_marca"));

                    productos.add(producto);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar productos", ex);
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo realizar la búsqueda",
                    ex.getMessage());
        }
    }

    // Restablecer los filtros
    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbCategoriaBus.getSelectionModel().clearSelection();
        dtpDesde.setValue(null);
        dtpHasta.setValue(null);
        txtPrecioMin.clear();
        txtPrecioMax.clear();
        chkProductosExistentes.setSelected(false);
        cmbOrdenarPor.getSelectionModel().clearSelection();

        actualizarTabla();
    }
}
