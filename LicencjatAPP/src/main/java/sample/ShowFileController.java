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
        text.setText("Wielomiany lewostronne równania: \n"+s1+"\nWielomiany prawostronne równania: \n"+s2);

    }

}
