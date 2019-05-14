package RichelieuCipherGUI.Controllers;

import Utilities.KeyGenerator;
import Utilities.UniExp;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.*;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.*;
import java.io.*;
import java.security.Key;
import java.util.Scanner;

public class SettingsFormController {

    @FXML
    private Button ChangeKey;

    @FXML
    private Button RandomKey;

    @FXML
    private Menu N1;

    @FXML
    private MenuItem N2;

    @FXML
    private Button ChangeSavePath;

    @FXML
    private MenuItem N3;

    @FXML
    private MenuItem N4;

    @FXML
    private MenuItem N5;

    @FXML
    private TextField UserKey;

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
    private MenuItem N14;

    @FXML
    private TextField SaveFilePathField;

    @FXML
    private MenuItem N13;

    @FXML
    private TextField KeyInputField;

    @FXML
    private TextField KeyPathField;

    @FXML
    private Button OKButton;

    @FXML
    private Button ApplyButton;

    @FXML
    private Button ChangeAcc;

    @FXML
    private Button ImportKey;

    @FXML
    private RadioButton DefaultSaveRad;

    @FXML
    private RadioButton ChangeOriginalRad;

    @FXML
    private ChoiceBox<String> ExtensionChoice;

    private boolean processText;

    private KeyGenerator gener;

    private FileChooser dialog;

    private FileChooser fileDialog;

    private Alert alert;

    private boolean changingUser;

    private boolean accept;

    private String avoid;


    void saveSettings(String encFile, String decFile) throws IOException {
        File saver;
        String[] files = {encFile, decFile};
        for (int i = 0; i < 2; i++) {
            if (!files[i].equals("")) {
                saver = new File(files[i]);
                if (!saver.exists()) {
                    boolean createRes = saver.createNewFile();
                    if (!createRes)
                        throw new IOException();
                }
                PrintWriter writer = new PrintWriter(saver.getName());
                writer.write(SaveFilePathField.getText() + "\r\n" + ChangeOriginalRad.isSelected() + "\r\n"
                        + DefaultSaveRad.isSelected() + "\r\n" + ExtensionChoice.getValue().toString() + "\r\n"
                        + KeyInputField.getText() + "\r\n" + KeyPathField.getText());
                writer.close();
            }
        }
    }

    boolean extractSettings(String fileName) throws IOException {
        File source;
        boolean sucess = false;
        source = new File(fileName);
        if (source.exists()) {
            if (source.length() > 0) {
                Scanner reader = new Scanner(source, "UTF-8");
                if (!reader.hasNextLine()) {
                    reader.close();
                    reader = new Scanner(source, "Cp1251");
                }
                if (reader.hasNextLine()) {
                    SaveFilePathField.setText(reader.nextLine());
                    if (reader.hasNextLine()) {
                        if (!processText) {
                            if (reader.nextLine().equals(true))
                                ChangeOriginalRad.setSelected(true);
                            else ChangeOriginalRad.setSelected(false);
                        } else reader.nextLine();
                        if (reader.hasNextLine()) {
                            if (!processText) {
                                if (reader.nextLine().equals(true))
                                    DefaultSaveRad.setSelected(true);
                                else DefaultSaveRad.setSelected(false);
                            } else reader.nextLine();
                            if (reader.hasNextLine()) {
                                ExtensionChoice.setValue(reader.nextLine());
                                if (reader.hasNextLine()) {
                                    KeyInputField.setText(reader.nextLine());
                                    if (reader.hasNextLine()) {
                                        KeyPathField.setText(reader.nextLine());
                                        sucess = true;
                                    }
                                }
                            }
                        }
                    }
                }
                reader.close();
            }
        }
        return sucess;
    }

    public void setAvoid(String avoid) {
        this.avoid = avoid;
    }

    public void setProcessText(boolean processText) {
        this.processText = processText;
        if (processText) {
            ChangeOriginalRad.setDisable(true);
            DefaultSaveRad.setDisable(true);
        }
    }

    String getOutput() throws UniExp {
        if (!changeOrigs() && !defaultSave() && SaveFilePathField.getText().equals(""))
            throw new UniExp("Введите полное имя выходного файла или путь к папке выходных данных!");
        if (!changeOrigs() && !defaultSave() && SaveFilePathField.getText().endsWith("\\"))
            return SaveFilePathField.getText().substring(0, SaveFilePathField.getText().lastIndexOf("\\"));
        return SaveFilePathField.getText();
    }

    String getKeyFileName() throws UniExp {
        if (avoid.contains("\"") && avoid.contains(";")) {
            if ((ExtensionChoice.getValue().equals("*.txt") && avoid.contains("\"" + KeyPathField.getText() + ".txt\"")
                    || (ExtensionChoice.getValue().equals("*.pdf") &&
                    avoid.contains("\"" + KeyPathField.getText() + ".pdf\""))))
                throw new UniExp("Файл ключа не должен совпадать с файлом исходных данных!");
        } else if ((ExtensionChoice.getValue().equals("*.txt") && avoid.equals(KeyPathField.getText() + ".txt"))
                || (ExtensionChoice.getValue().equals("*.pdf") && avoid.equals(KeyPathField.getText() + ".pdf")))
            throw new UniExp("Файл ключа не должен совпадать с файлом исходных данных!");
        if (KeyPathField.getText().equals(""))
            return "key";
        return KeyPathField.getText();
    }

    String getKey() throws UniExp {
        if (KeyInputField.getText().equals(""))
            throw new UniExp("Введите ключ шифровки!");
        return KeyInputField.getText();
    }

    boolean extension() {
        return ExtensionChoice.getValue().equals("*.txt");
    }

    boolean changeOrigs() {
        return ChangeOriginalRad.isSelected();
    }

    boolean defaultSave() {
        return DefaultSaveRad.isSelected();
    }

    String extractKey(File keyFile) throws UniExp, IOException {
        String newKey;
        if (keyFile.getName().endsWith(".txt")) {
            Scanner sc = new Scanner(keyFile);
            newKey = sc.nextLine();
        } else {
            PDFParser parser = new PDFParser(new RandomAccessBufferedFileInputStream(keyFile));
            PDDocument doc = new PDDocument(parser.getDocument());
            parser.parse();
            PDFTextStripper stripper = new PDFTextStripper();
            newKey = stripper.getText(doc).replaceAll(System.getProperty("line.separator"), "");
            doc.close();
        }
        for (char c : newKey.toCharArray()) {
            if (!Character.isDigit(c) && c != ' ')
                throw new UniExp("Ключ не должен содержать буквы и специальные символы (за исключением пробела)!");
        }
        return newKey;
    }

    public boolean isChangingUser() {
        return changingUser;
    }

    public boolean isAccept() {
        return accept;
    }

    void pathInputCheck(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= 'а' && c <= 'я') || (c >= 'А' && c <= 'Я')
                || (c >= '0' && c <= '9')) && c != '-' && c != ' ' && c != '_' && c != '\\')
            event.consume();
    }

    @FXML
    void initialize() {
        Tooltip changeAccTip = new Tooltip("Выход из текущей учетной записи в окно входа");
        Tooltip savePathTip = new Tooltip("Путь, по которому будет сохранён результат работы программы");
        Tooltip keyPathTip = new Tooltip("Путь, по которому находится ключ шифрования");
        Tooltip rndKeyTip = new Tooltip("Программа сгенерирует случайный ключ шифрования");
        alert = new Alert(Alert.AlertType.ERROR);
        accept = false;
        changingUser = false;
        dialog = new FileChooser();
        fileDialog = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TXT and PDF files", "*.txt", "*.pdf");
        fileDialog.getExtensionFilters().add(filter);
        dialog.getExtensionFilters().add(filter);
        gener = new KeyGenerator();
        ExtensionChoice.getItems().addAll("*.txt", "*.pdf");
        ExtensionChoice.setValue(ExtensionChoice.getItems().get(0));
        ChangeAcc.setTooltip(changeAccTip);
        SaveFilePathField.setTooltip(savePathTip);
        KeyPathField.setTooltip(keyPathTip);
        KeyPathField.setText("key");
        RandomKey.setTooltip(rndKeyTip);
        N13.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new File("RCHelp.chm"));
                } catch(IOException ioe){
                    alert.setContentText("Файл RCHelp.chm не найден!");
                    alert.show();
                } catch (IllegalArgumentException iae){
                    alert.setContentText("Файл RCHelp.chm не найден!");
                    alert.show();
                }
            }
        });
        KeyPathField.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                pathInputCheck(event);
            }
        });
        SaveFilePathField.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                pathInputCheck(event);
            }
        });
        KeyPathField.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (KeyPathField.getText().endsWith("\\"))
                    KeyPathField.setText(KeyPathField.getText().substring(0, KeyPathField.getText().lastIndexOf("\\")));
            }
        });
        ChangeAcc.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changingUser = true;
                ((Stage) ChangeAcc.getScene().getWindow()).close();
            }
        });
        ChangeSavePath.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Node source = (Node) event.getSource();
                File f = dialog.showSaveDialog(source.getScene().getWindow());
                if (f != null) {
                    if (f.getName().contains("."))
                        SaveFilePathField.setText(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")));
                    else
                        SaveFilePathField.setText(f.getAbsolutePath());
                    if (!KeyPathField.getText().equals("")
                            && KeyPathField.getText().equals(SaveFilePathField.getText())) {
                        SaveFilePathField.setText("");
                        alert.setContentText("Файл-результат и файл ключа не должны совпадать!");
                        alert.show();
                    }
                }
            }
        });
        ChangeKey.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Node source = (Node) event.getSource();
                File f = dialog.showSaveDialog(source.getScene().getWindow());
                if (f != null) {
                    if (f.getName().contains("."))
                        KeyPathField.setText(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")));
                    else
                        KeyPathField.setText(f.getAbsolutePath());
                    if (!SaveFilePathField.getText().equals("")
                            && SaveFilePathField.getText().equals(KeyPathField.getText())) {
                        KeyPathField.setText("");
                        alert.setContentText("Файл-результат и файл ключа не должны совпадать!");
                        alert.show();
                    }
                }
            }
        });
        DefaultSaveRad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (DefaultSaveRad.isSelected())
                    ChangeOriginalRad.setDisable(true);
                else ChangeOriginalRad.setDisable(false);
            }
        });
        ChangeOriginalRad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (ChangeOriginalRad.isSelected()) {
                    DefaultSaveRad.setDisable(true);
                    ChangeSavePath.setDisable(true);
                    SaveFilePathField.setText("");
                    SaveFilePathField.setDisable(true);
                } else {
                    DefaultSaveRad.setDisable(false);
                    ChangeSavePath.setDisable(false);
                    SaveFilePathField.setDisable(false);
                }
            }
        });
        if (processText) {
            DefaultSaveRad.setDisable(true);
            ChangeOriginalRad.setDisable(true);
        }
        N7.setOnAction(event -> {                       //Событие кнопки "Закрыть" меню бара
            Stage stage = (Stage) MenuBar1.getScene().getWindow();
            stage.close();
        });

        N14.setOnAction(event -> {                      //Событие кнопки "О приложении" меню бара"
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
            stage.initOwner(MenuBar1.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.getIcons().add(new Image("/Resources/info.png"));
            stage.setTitle("О программе");
            stage.showAndWait();
        });

        OKButton.setOnAction(event -> {                                 //Событие кнопки "ОК" (закрывает форму)
            try {
                Stage stage = (Stage) OKButton.getScene().getWindow();
                if (avoid != null) {                                    //Если пользователь выбрал файлы для обработки
                    getKey();                                               //Закрываем форму
                    getOutput();                                            //только если
                    getKeyFileName();                                       //все поля заполнены
                }
                stage.close();
                accept = true;                                          //"Сообщает" контроллеру главной формы,
                // что можно брать ключ и имена файлов
            } catch (UniExp ue) {
                alert.setContentText(ue.getMessage());
                alert.show();
            }
        });
        RandomKey.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                KeyInputField.setText(gener.generate());
            }
        });
        KeyInputField.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                char c = event.getCharacter().charAt(0);
                if (c >= '1' && c <= '9') {
                    if (KeyInputField.getText().contains(" ")) {
                        if (KeyInputField.getText().substring(KeyInputField.getText().lastIndexOf(" "),
                                KeyInputField.getText().length()).contains(c + ""))
                            event.consume();
                    } else if (KeyInputField.getText().contains("" + c))
                        event.consume();
                } else if (c != ' ' || KeyInputField.getText().charAt(KeyInputField.getText().length() - 1) == ' ')
                    event.consume();
            }
        });
        KeyInputField.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    if (!KeyInputField.getText().equals("")) {
                        for (char c : KeyInputField.getText().toCharArray()) {
                            if (!Character.isDigit(c) && c != ' ')
                                throw new UniExp("");
                        }
                        if (!gener.keyCheck(KeyInputField.getText()))
                            throw new UniExp("Ключ не должен быть возрастающей или убывающей последовательностью!");
                        KeyInputField.setText(gener.checkNums(KeyInputField.getText()));
                    }
                } catch (UniExp e) {
                    if (e.getMessage().equals(""))
                        KeyInputField.setText("");
                    else {
                        alert.setTitle(null);
                        alert.setContentText(e.getMessage());
                        alert.show();
                    }
                }
            }
        });
        ImportKey.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Node source = (Node) event.getSource();
                    File keyFile = fileDialog.showOpenDialog(source.getScene().getWindow());
                    if (keyFile.length() == 0)
                        throw new UniExp("Файл пуст!");
                    KeyInputField.setText(extractKey(keyFile));
                    KeyPathField.setText(keyFile.getName().substring(0, keyFile.getName().lastIndexOf(".")));
                } catch (IOException ioe) {
                    alert.setTitle(null);
                    alert.setContentText("Не удалось открыть файл!");
                    alert.show();
                } catch (UniExp ue) {
                    alert.setTitle(null);
                    alert.setContentText(ue.getMessage());
                    alert.show();
                } catch (NullPointerException npe) {
                }
            }
        });
    }
}
