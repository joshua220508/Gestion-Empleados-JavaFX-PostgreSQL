module com.example.gestionempleados {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.gestionempleados to javafx.fxml;
    opens com.example.gestionempleados.controller to javafx.fxml;
    opens com.example.gestionempleados.model to javafx.base;

    exports com.example.gestionempleados;
}