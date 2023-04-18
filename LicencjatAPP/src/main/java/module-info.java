module g.sample {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.sat4j.core;
    requires java.xml;


    opens sample to javafx.fxml;
    exports sample;
}