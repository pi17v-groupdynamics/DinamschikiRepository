package RichelieuCipherGUI.Controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AboutFormController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button OKButton;

    @FXML
    void initialize() {
        OKButton.setOnAction(event -> {                                 //Событие кнопки "ОК" (закрывает форму)
            Stage stage = (Stage) OKButton.getScene().getWindow();      //Присваивание переменной stage текущую форму
            stage.close();                                              //Закрытие формы
        });

    }
}
