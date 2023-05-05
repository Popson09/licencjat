package sample;

import javafx.fxml.FXML;
import javafx.scene.text.Text;


public class ShowFileController {

    @FXML
    Text text;
    void setText(Algebra algebra){
        text.setText(algebra.showAlgebraFile());

    }
    void showRes(String s){
        text.setText(s);

    }
    void showEqStatus(String s1,String s2){
        text.setText("Lewe równania: \n"+s1+"Prawe równania: \n"+s2);

    }

}
