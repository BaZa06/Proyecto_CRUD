module ni.edu.uam.proyectocrud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ni.edu.uam.proyectocrud.model to javafx.base;

    exports ni.edu.uam.proyectocrud.controller to javafx.fxml;
    opens ni.edu.uam.proyectocrud.controller to javafx.fxml;

    opens ni.edu.uam.proyectocrud to javafx.fxml;
    exports ni.edu.uam.proyectocrud;
}