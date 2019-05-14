package RichelieuCipherGUI.Controllers;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import Utilities.Authorization;
import Utilities.UniExp;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;


public class LoginFormController extends JFrame {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private PasswordField PassField;

    @FXML
    private Menu N1;

    @FXML
    private MenuItem N2;

    @FXML
    private MenuItem N3;

    @FXML
    private MenuItem N4;

    @FXML
    private Button CreateAccButton;

    @FXML
    private MenuItem N5;

    @FXML
    private MenuBar MenuBar1;

    @FXML
    private MenuItem N6;

    @FXML
    private MenuItem N7;

    @FXML
    private Menu N8;

    @FXML
    private MenuItem N9;

    @FXML
    private MenuItem N10;

    @FXML
    private Menu N12;

    @FXML
    private Menu N11;

    @FXML
    private TextField LoginField;

    @FXML
    private Button SignInButton;

    @FXML
    private MenuItem N14;

    @FXML
    private MenuItem N13;

    private MainFormController main;

    private Authorization aut;

    public void hotKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            SignInButton.fire();
        }
    }

    @FXML
    void initialize() {
       // try{
        Tooltip logTip = new Tooltip("Логин должен содержать 4 и более символов");
        Tooltip passTip = new Tooltip("Пароль должен состоять из 4 и более символов");
        aut = new Authorization();
        LoginField.setTooltip(logTip);
        PassField.setTooltip(passTip);
        //if(!new File("Accounts.txt").exists())
         //   aut.createFile();
        SignInButton.setOnAction(event -> {                         //Событие кнопки "Войти" (лишь переходит на Основную форму)
            try {
                String login = "Логин : " + LoginField.getText();
                String password = "Пароль : " + PassField.getText();
                boolean[] accCheck = aut.findAcc(login, password);
                if ((accCheck[0] == true) && (accCheck[1] == true) && (accCheck[2] == true)
                        && LoginField.getLength() >= 4 && PassField.getLength() >= 4) {
                    SignInButton.getScene().getWindow().hide();             //Получает текущую форму и скрывает её
                    FXMLLoader loader = new FXMLLoader();                   //Создаёт объект loader класса FXMLLoader
                    loader.setLocation(getClass().getResource("/RichelieuCipherGUI/Forms/MainForm.fxml"));  //Задаёт расположение объекту loader
                    //try {
                        loader.load();                                      //загружает форму в объект loader по указанному расположению
                    //} catch (IOException e) {
                    //    e.printStackTrace();
                    //}
                    Parent root = loader.getRoot();                         //Загружаем форму
                    Stage stage = new Stage();                              //Создаёт объект stage класса Stage
                    stage.getIcons().add(new Image("/Resources/Richelieu Cipher.png"));     //Устанавливает иконку формы
                    stage.setTitle("Richelieu Cipher");                                         //Устанавливает заголовок формы
                    stage.setScene(new Scene(root));                                            //Создаёт сцену
                    main = loader.getController();
                    stage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent event) {
                            main.hotKeys(event);
                        }
                    });
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            try {
                                if (main.isWorking()) {
                                    Alert workAlert = new Alert(Alert.AlertType.CONFIRMATION);
                                    workAlert.getButtonTypes().clear();
                                    workAlert.getButtonTypes().addAll(new ButtonType("Закрыть"),
                                            new ButtonType("Подождать"));
                                    workAlert.setTitle(null);
                                    workAlert.setContentText("Программа все еще обрабатывает файл или сообщение. " +
                                            "Вы уверены, что хотите выйти?");
                                    Optional<ButtonType> option = workAlert.showAndWait();
                                    if (option != null) {
                                        if (option.get().getText().equals("Подождать"))
                                            throw new UniExp("");
                                    }
                                }
                                main.close();
                            } catch (UniExp e) {
                            }
                        }
                    });
                    stage.showAndWait();                                                          //Выводит форму на экран
                } else if (LoginField.getLength() == 0)
                    showMessage("Введите логин!");
                else if (PassField.getLength() == 0)
                    showMessage("Введите пароль!");
                else if (accCheck[0] == true && accCheck[2] == false)
                    showMessage("Введён неверный пароль!");
                else if (accCheck[0] == false && accCheck[1] == false)
                    showMessage("Аккаунт не найден. Зарегистрируйтесь.");
            } catch (UniExp ue) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(null);
                alert.setContentText(ue.getMessage());
                alert.show();
            }  catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(null);
                alert.setContentText(e.getLocalizedMessage());
                alert.show();
                //e.printStackTrace();
            }
        });

        N14.setOnAction(event -> {                                  //Событие кнопки "О приложении" меню бара"
            MenuBar1.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/RichelieuCipherGUI/Forms/AboutForm.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initOwner(MenuBar1.getScene().getWindow());       //Устанавливает родительское окно
            stage.initModality(Modality.APPLICATION_MODAL);         //Устанавливает модальность окна (блокирует родительское окно)
            stage.setResizable(false);                              //Делает форму немасштабируемой
            stage.getIcons().add(new Image("/Resources/info.png"));
            stage.setTitle("О программе");
            stage.showAndWait();
        });

        N13.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new File("RCHelp.chm"));
                } catch(IOException ioe){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(null);
                    alert.setContentText("Файл RCHelp.chm не найден!");
                    alert.show();
                } catch (IllegalArgumentException iae){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(null);
                    alert.setContentText("Файл RCHelp.chm не найден!");
                    alert.show();
                }
            }
        });

        CreateAccButton.setOnAction(event -> {
            try {
                if (LoginField.getText().length() >= 4 && PassField.getText().length() >= 4
                        && LoginField.getText().length() <= 25 && PassField.getText().length() <= 25) {
                    String login = "Логин : " + LoginField.getText();
                    String password = "Пароль : " + PassField.getText();
                    boolean[] accCheck = aut.findAcc(login, password);
                    if (!(accCheck[0] == true && accCheck[1] == true)) {
                        aut.saveToFile(LoginField.getText(), PassField.getText());

                        showMessage("Логин и пароль сохранены!");
                    } else {
                        showMessage("Аккаунт уже существует!");
                    }
                } else if (LoginField.getText().length() < 4) {
                    showMessage("Логин слишком короткий!");
                } else if (PassField.getText().length() < 4) {
                    showMessage("Пароль слишком короткий!");
                } else if (LoginField.getText().length() > 25) {
                    showMessage("Логин слишком длинный!");
                } else if (PassField.getText().length() > 25) {
                    showMessage("Пароль слишком длинный!");
                }
            } catch (UniExp ue) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(null);
                alert.setContentText(ue.getMessage());
                alert.show();
            }
        });

        LoginField.setOnKeyTyped(event -> {
            TextFormatter<String> formatter = new TextFormatter<String>(change -> {
                change.setText(change.getText().replaceAll("[^a-zA-Z0-9-_]", ""));
                return change;

            });
            LoginField.setTextFormatter(formatter);
        });

        PassField.setOnKeyTyped(event -> {
            TextFormatter<String> formatter = new TextFormatter<String>(change -> {
                change.setText(change.getText().replaceAll("[^a-zA-Z0-9-_]", ""));
                return change;

            });
            PassField.setTextFormatter(formatter);
        });
       // } catch (IOException ioe){
       //     Alert alert = new Alert(Alert.AlertType.ERROR);
       //     alert.setTitle(null);
        //    alert.setContentText("Не удалось создать отсутствующий файл Accounts.txt! Программа будет закрыта.");
        //    alert.showAndWait();
        //    ((Stage) LoginField.getScene().getWindow()).close();
        //}
    }

    void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

