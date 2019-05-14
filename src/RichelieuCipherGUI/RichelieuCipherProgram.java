package RichelieuCipherGUI;

import RichelieuCipherGUI.Controllers.LoginFormController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class RichelieuCipherProgram extends Application {

    LoginFormController log;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Forms/LoginForm.fxml"));
        Parent root = loader.load();
        primaryStage.getIcons().add(new Image("/Resources/Richelieu Cipher.png"));
        primaryStage.setTitle("Richelieu Cipher");
        primaryStage.setScene(new Scene(root, 350, 500));
        log = loader.getController();
        primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                log.hotKey(event);
            }
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
