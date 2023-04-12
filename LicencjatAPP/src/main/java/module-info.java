module com.example.licencjatapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.xml;
    requires org.sat4j.core;

    opens sample to javafx.fxml;
    exports sample;
}