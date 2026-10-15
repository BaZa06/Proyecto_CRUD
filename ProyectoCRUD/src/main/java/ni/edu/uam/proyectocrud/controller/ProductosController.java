package ni.edu.uam.proyectocrud.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.proyectocrud.database.ConexionBD;
import ni.edu.uam.proyectocrud.model.Productos;

public class ProductosController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtStock;

    @FXML
    private ComboBox<String> cmbCategoria;

    @FXML
    private ComboBox<String> cmbMarca;

    @FXML
    private DatePicker dtpFecha;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private TextField txtBuscar;

    @FXML
    private DatePicker dtpDesde;

    @FXML
    private TextField txtPrecioMin;

    @FXML
    private ComboBox<String> cmbCategoriaBus;

    @FXML
    private DatePicker dtpHasta;

    @FXML
    private TextField txtPrecioMax;

    @FXML
    private CheckBox chkProductosExistentes;

    @FXML
    private ComboBox<String> cmbOrdenarPor;

    @FXML
    private TableView<Productos> tblProductos;
    private TableColumn<Productos, String> colCodigo;
    private TableColumn<Productos, String> colNombre;
    private TableColumn<Productos, String> colCategoria;
    private TableColumn<Productos, String> colMarca;
    private TableColumn<Productos, Double> colPrecio;
    private TableColumn<Productos, Integer> colStock;
    private TableColumn<Productos, java.time.LocalDate> colFechaCreacion;

    private final ObservableList<Productos> productos = FXCollections.observableArrayList();
}
