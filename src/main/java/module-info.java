module com.example.whiteboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires controlsfx;

    opens com.example.whiteboard to javafx.fxml;
    exports com.example.whiteboard;
}