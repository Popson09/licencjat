module com.example.aplikacja {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.xml;
    requires javafx.graphics;

    opens sample to javafx.fxml;
    exports sample;
}