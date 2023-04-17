package sample;

import javafx.fxml.FXML;
import javafx.scene.text.Text;


public class ShowFileController {

    @FXML
    Text text;
    void setText(Algebra algebra){
        text.setText(algebra.showAlgebraFile());

    }

}
