package RichelieuCipherGUI.Controllers;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import RichelieuCipherProcessor.FileProcessor;
import Utilities.UniExp;
import Utilities.state;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class MainFormController {

    @FXML
    private GridPane TwoFileProcessing;             //Потоковая обработка 2 файла. По умолчанию скрыта (visible(false))

    @FXML
    private TextArea OriginalText;

    @FXML
    private MenuBar MenuBar1;

    @FXML
    private TextArea File1ResultText;

    @FXML
    private TextArea File1PathText;

    @FXML
    private Button EncryptButton;

    @FXML
    private Button UnencryptButton;

    @FXML
    private TextArea File2PathText;

    @FXML
    private TextArea File2ResultText;

    @FXML
    private TextArea TextResult;

    @FXML
    private GridPane OneFileProcessing;             //Потоковая обработка 1 файл. По умолчанию скрыта (visible(false))

    @FXML
    private ProgressBar ProgressBar1;

    @FXML
    private Menu N1;

    @FXML
    private MenuItem N2;

    @FXML
    private MenuItem N3;

    @FXML
    private MenuItem N4;

    @FXML
    private TextArea FilePathText;

    @FXML
    private MenuItem N5;

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
    private TextArea FileResultText;

    @FXML
    private Menu N12;

    @FXML
    private Menu N11;

    @FXML
    private MenuItem N14;

    @FXML
    private MenuItem N13;

    @FXML
    private Label ProgressLabel;

    @FXML
    private TextField FilesPathField;

    @FXML
    private Button BrowseButton;

    @FXML
    private Label SettingsLabel;

    @FXML
    private Tab encFiles;

    @FXML
    private Tab encText;

    @FXML
    private TabPane Tabs;

    @FXML
    private Label FilePathLabel;

    private FileChooser dialog;                                                          //Компонент для открытия файлов

    private volatile ArrayList<File> files;                                                           //Список файлов для обработки

    private volatile ArrayList<File> outputs;

    private volatile Button trigger;                           //В событии этой "кнопки" файлы передаются между потоками

    private volatile Button progressChanged;                    //Обновление прогрессбара для сообщения и 1 файла

    private volatile FileProcessor[] processors;                                            //Непосредственно обработчики файлов

    private final Object locker = new Object();                                              //"Блокиратор" для потоков

    private boolean defaultSave, changeOrigs, isTXT;

    private volatile StringBuilder key;

    private volatile StringBuilder outputPath;

    private volatile StringBuilder keyPath;

    private Alert alert;

    private volatile boolean processFiles;

    private int progressIndic;


    private void keySave() throws IOException {
        File f;
        if (isTXT)
            f = new File(keyPath.toString() + ".txt");
        else
            f = new File(keyPath.toString() + ".pdf");
        if (!f.exists())
            f.createNewFile();
        if (isTXT) {
            FileWriter writer = new FileWriter(f);
            writer.write(key.toString());
            writer.close();
        } else {
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            stream.setFont(PDType1Font.TIMES_ROMAN, 12);
            stream.beginText();
            stream.newLineAtOffset(30, 700);
            stream.showText(key.toString());
            stream.endText();
            stream.close();
            doc.save(f.getName());
            doc.close();
        }
    }

    private void disOrEn(boolean op) {
        UnencryptButton.setDisable(op);
        EncryptButton.setDisable(op);
        N8.setDisable(op);
        N11.setDisable(op);
        if (processFiles)
            encText.setDisable(op);
        else
            encFiles.setDisable(op);
    }

    private void acceptInputs(SettingsFormController set, String opSuff, boolean opType) throws UniExp, IOException {
        key.append(set.getKey());
        outputPath.append(set.getOutput());
        keyPath.append(set.getKeyFileName());
        isTXT = set.extension();
        if (keyPath.toString().contains("\\")) {
            File f = new File(keyPath.substring(0, keyPath.lastIndexOf("\\")));
            if (!f.exists())
                if (!f.mkdirs())
                    throw new UniExp("Не удалось создать папку " + keyPath.substring(0, keyPath.lastIndexOf("\\")));
        }
        keySave();
        if (processFiles) {
            changeOrigs = set.changeOrigs();
            defaultSave = set.defaultSave();
            if (((!defaultSave && changeOrigs) || (!changeOrigs && defaultSave)) && !outputPath.toString().equals("")) {
                outputPath.append("\\");
                //else
                // outputPath.append("\\");
            }
            if (outputPath.toString().contains("\\")) {
                File f = new File(outputPath.substring(0, outputPath.lastIndexOf("\\")));
                if (!f.exists())
                    if (!f.mkdirs())
                        throw new UniExp("Не удалось создать папку " + outputPath.toString());
            }
            //}
            // if (processFiles) {
            if (changeOrigs) {
                outputs.addAll(files);
            } else if (defaultSave) {
                for (int i = 0; i < files.size(); i++) {
                    String s = files.get(i).getName();
                    if (s.endsWith(".txt"))
                        s = s.replaceAll("\\.txt", opSuff + ".txt");
                    else
                        s = s.replaceAll("\\.pdf", opSuff + ".pdf");
                    File f = new File(outputPath.toString() + s);
                    if (!f.exists())
                        f.createNewFile();
                    outputs.add(f);
                }
            } else {
                while (outputPath.toString().endsWith("\\"))
                    outputPath.deleteCharAt(outputPath.length() - 1);
                for (int i = 0; i < files.size(); i++) {
                    File f;
                    if (files.size() == 1) {
                        if (isTXT)
                            f = new File(outputPath.toString() + ".txt");
                        else
                            f = new File(outputPath.toString() + ".pdf");
                    } else {
                        if (isTXT)
                            f = new File(outputPath.toString() + (i + 1) + ".txt");
                        else
                            f = new File(outputPath.toString() + (i + 1) + ".pdf");
                    }
                    if (!f.exists())
                        f.createNewFile();
                    outputs.add(f);
                    //if (isTXT)
                    //    outputs.add(new File(outputPath.toString() + (i + 1) + ".txt"));
                    //else
                    //    outputs.add(new File(outputPath.toString() + (i + 1) + ".pdf"));
                }
            }
        } else {
            while (outputPath.toString().endsWith("\\"))
                outputPath.deleteCharAt(outputPath.length() - 1);
            if (isTXT)
                outputPath.append(".txt");
            else
                outputPath.append(".pdf");
            outputs.add(new File(outputPath.toString()));
            if (!outputs.get(0).exists())
                outputs.get(0).createNewFile();
            if (processors[0] == null) {
                processors[0] = new FileProcessor(locker, trigger, progressChanged);
                Thread t = new Thread(processors[0]);
                t.setName("1");
                t.start();
            }
            processors[0].setSingle(true);
            processors[0].setOriginalText(OriginalText.getText());
        }
        processors[0].setOperationType(opType);
        processors[0].setKey(key.toString());
        if (processors[1] != null && outputs.size() > 1) {
            processors[1].setOperationType(opType);
            processors[1].setKey(key.toString());
        }
        if (processFiles)
            disOrEn(true);
        if (processFiles) {
            FilePathLabel.setVisible(false);
            FilesPathField.setVisible(false);
            BrowseButton.setVisible(false);
            if (progressIndic > 1)
                TwoFileProcessing.setVisible(true);
            else {
                OneFileProcessing.setVisible(true);
                processors[0].setSingle(true);
            }
        }
        if (!processFiles || files.size() > 1)
            trigger.fire();
        else {
            processors[0].setInput(files.get(0));
            processors[0].setOutput(outputs.get(0));
            processors[0].setState(state.running);
            files.remove(0);
            synchronized (locker) {
                locker.notifyAll();
            }
        }
    }

    public boolean isWorking() {
        if (processors[0] == null)
            return false;
        for (int i = 0; i < processors.length; i++) {
            if (processors[i] != null)
                if (processors[i].getState() == state.running)
                    return true;
        }
        return false;
    }

    public void close() {
        for (int i = 0; i < processors.length; i++) {
            if (processors[i] != null)
                processors[i].setState(state.exit);
        }
        synchronized (locker) {
            locker.notifyAll();
        }
        try {
            PrintWriter writer = new PrintWriter("EncSetts.txt");
            writer.print("");
            writer.close();
            writer = new PrintWriter("DecSetts.txt");
            writer.print("");
            writer.close();
        } catch (FileNotFoundException fnfe) {

        }
    }

    public void hotKeys(KeyEvent event) {
        if (event.getCode() == KeyCode.F3) {                //"О программе"
            if (!N14.isDisable())
                N14.fire();
            return;
        }
        if (event.getCode() == KeyCode.F4) {         //Справка
            if (!N13.isDisable())
                N13.fire();
            return;
        }
        if (event.getCode() == KeyCode.F1) {         //Выбор файлов
            if (!BrowseButton.isDisabled())
                BrowseButton.fire();
            return;
        }
        if (event.getCode() == KeyCode.F2) {         //Настройки
            if (!N11.isDisable())
                N11.fire();
            return;
        }
        if (!OriginalText.isFocused()) {             //Проверяем, что пользователь не вводит сообщение для шифровки
            if (event.getCode() == KeyCode.E) {      //Шифровка
                if (!EncryptButton.isDisabled())
                    EncryptButton.fire();
                return;
            }
            if (event.getCode() == KeyCode.U) {      //Дешифровка
                if (!UnencryptButton.isDisabled())
                    UnencryptButton.fire();
                return;
            }
            if (event.getCode() == KeyCode.F) {      //Обработка файлов
                if (!encFiles.isSelected())
                    Tabs.getSelectionModel().select(encFiles);
                return;
            }
            if (event.getCode() == KeyCode.T) {      //Обработка текста
                if (!encText.isSelected())
                    Tabs.getSelectionModel().select(encText);
            }
        }
    }

    void openSettings() {
        try {
            MenuBar1.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/RichelieuCipherGUI/Forms/SettingsForm.fxml"));
            Parent root = loader.load();
            SettingsFormController set = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initOwner(MenuBar1.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.getIcons().add(new Image("/Resources/settings.png"));
            stage.setTitle("Настройки");
            if (!set.extractSettings("EncSetts.txt"))
                set.extractSettings("DecSetts.txt");
            stage.showAndWait();
            try {
                set.saveSettings("EncSetts.txt", "DecSetts.txt");
            } catch (IOException ioe) {
            }
            if (set.isChangingUser()) {                                      //Если пользователь нажал "Сменить аккаунт",
                loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/RichelieuCipherGUI/Forms/LoginForm.fxml"));
                root = loader.load();
                stage.setScene(new Scene(root, 350, 500));
                stage.setResizable(false);
                stage.getIcons().add(new Image("/Resources/Richelieu Cipher.png"));
                stage.setTitle("Richelieu Cipher");
                ((Stage) SettingsLabel.getScene().getWindow()).close();     //закрываем текущую форму
                stage.show();                                               //и открываем форму авторизации
            }
        } catch (IOException e) {
            alert.setTitle(null);
            alert.setContentText("Проверьте наличие файлов LoginForm.fxml и SettingsForm.fxml в " +
                    "/RichelieuCipherGUI/Forms/!");
            alert.show();
        }
    }

    @FXML
    void initialize() {
        Tooltip encTip = new Tooltip("Введенный текст или выбранные\n файлы будут зашифрованы в соответствии\n с указанными Вами настройками");
        Tooltip unencTip = new Tooltip("Введенный текст или выбранные\n файлы будут расшифрованы в соответствии\n с указанными Вами настройками");
        int m = 0, n = 0;
        //Tooltip progressTip = new Tooltip("Файлов обработано: " + m + " из " + n);
        dialog = new FileChooser();
        alert = new Alert(Alert.AlertType.ERROR);
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TXT and PDF files", "*.txt", "*.pdf");
        dialog.getExtensionFilters().add(filter);
        key = new StringBuilder();
        outputPath = new StringBuilder();
        outputs = new ArrayList<>();
        files = new ArrayList<>();
        processors = new FileProcessor[2];
        processFiles = false;
        keyPath = new StringBuilder();
        trigger = new Button();
        N1.setDisable(true);
        trigger.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (processFiles) {
                        if (Thread.currentThread().getName().equals("1")) {
                            ProgressBar1.setProgress(ProgressBar1.getProgress() + 1f / progressIndic);
                            if (processors[0].getSaveErrorMsg().equals("")) {
                                if (TwoFileProcessing.isVisible()) {
                                    if (!File1PathText.getText().equals("")) {
                                        File1PathText.setText(File1PathText.getText() + "\n---------------------------" +
                                                "------------------------------------------------------\n");
                                    }
                                    File1PathText.setText(File1PathText.getText() + processors[0].getOriginalText(true));
                                    if (!File1ResultText.getText().equals("")) {
                                        File1ResultText.setText(File1ResultText.getText() + "\n---------------------------" +
                                                "------------------------------------------------------\n");
                                    }
                                    File1ResultText.setText(File1ResultText.getText() + processors[0].getLines());
                                } else {
                                    FilePathText.setText(processors[0].getOriginalText(true));
                                    FileResultText.setText(processors[0].getLines());
                                }
                            } else {
                                alert.setTitle(null);
                                alert.setContentText(processors[0].getSaveErrorMsg());
                                alert.show();
                            }
                        }
                            if (Thread.currentThread().getName().equals("2")) {
                                ProgressBar1.setProgress(ProgressBar1.getProgress() + 1f / progressIndic);
                                if (processors[1].getSaveErrorMsg().equals("")) {
                                    if (!File2PathText.getText().equals("")) {
                                        File2PathText.setText(File2PathText.getText() + "\n---------------------------" +
                                                "------------------------------------------------------\n");
                                    }
                                    File2PathText.setText(File2PathText.getText() + processors[1].getOriginalText(true));
                                    if (!File2ResultText.getText().equals("")) {
                                        File2ResultText.setText(File2ResultText.getText() + "\n---------------------------" +
                                                "------------------------------------------------------\n");
                                    }
                                    File2ResultText.setText(File2ResultText.getText() + processors[1].getLines());
                                } else {
                                    alert.setTitle(null);
                                    alert.setContentText(processors[1].getSaveErrorMsg());
                                    alert.show();
                                }
                            }
                        if (Thread.currentThread().getName().equals("2") && processors[0].getState() == state.waiting) {
                            try {
                                synchronized (processors[1].getLocker()) {
                                    processors[1].getLocker().wait();
                                }
                            } catch (InterruptedException ie) {
                            }
                        }
                        if (processors[0].getState() == state.waiting) {
                            if (files.size() > 0) {
                                processors[0].setInput(files.get(0));
                                processors[0].setOutput(outputs.get(0));
                                processors[0].setState(state.running);
                                files.remove(0);
                                outputs.remove(0);
                            } else throw new UniExp("");
                        }
                        if (processors[1] != null) {
                            if (processors[1].getState() == state.waiting) {
                                if (files.size() > 0) {
                                    processors[1].setInput(files.get(0));
                                    processors[1].setOutput(outputs.get(0));
                                    processors[1].setState(state.running);
                                    files.remove(0);
                                    outputs.remove(0);
                                } else throw new UniExp("");
                            }
                        }
                        synchronized (locker) {
                            locker.notifyAll();
                        }
                    } else {
                        if (outputs.size() > 0) {
                            processors[0].setOutput(outputs.get(0));
                            processors[0].setState(state.running);
                            outputs.remove(0);
                            synchronized (locker) {
                                locker.notifyAll();
                            }
                        } else {
                            TextResult.setText(processors[0].getLines());
                            throw new UniExp("");
                        }
                    }
                } catch (IOException ioe) {
                } catch (UniExp ue) {
                    if (key.length() > 0)
                        key.delete(0, key.length());
                    if (outputPath.length() > 0)
                        outputPath.delete(0, outputPath.length());
                    if (keyPath.length() > 0)
                        keyPath.delete(0, keyPath.length());
                    if (outputs.size() > 0)
                        outputs.clear();
                    N1.setDisable(false);
                    if (processFiles)
                        encText.setDisable(false);
                    else
                        encFiles.setDisable(false);
                }
            }
        });
        progressChanged = new Button();
        progressChanged.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (processors[0].getInputLength() > 0)
                    ProgressBar1.setProgress(processors[0].getProcessed() / processors[0].getInputLength());
                else
                    ProgressBar1.setProgress(100);
            }
        });
        EncryptButton.setTooltip(encTip);
        UnencryptButton.setTooltip(unencTip);
        //ProgressBar1.setTooltip(progressTip);
        processFiles = false;
        encFiles.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (encFiles.isSelected()) {
                    processFiles = true;
                } else {
                    if (processFiles && UnencryptButton.isDisabled()) {
                        TwoFileProcessing.setVisible(false);
                        OneFileProcessing.setVisible(false);
                        FilePathText.setText("");
                        FileResultText.setText("");
                        File1PathText.setText("");
                        File1ResultText.setText("");
                        File2PathText.setText("");
                        File2ResultText.setText("");
                        BrowseButton.setVisible(true);
                        FilesPathField.setVisible(true);
                        FilesPathField.setText("");
                        disOrEn(false);
                    }
                    processFiles = false;
                }
                if (ProgressBar1.getProgress() > 0)
                    ProgressBar1.setProgress(0);
            }
        });
        N11.setOnAction(new EventHandler<ActionEvent>() {         //Событие кнопки "Настройки" меню бара (открывает форму "Настройки")
            @Override
            public void handle(ActionEvent event) {
                openSettings();
            }
        });
        SettingsLabel.setOnMouseClicked(event -> {
            openSettings();
        });

        N1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!BrowseButton.isVisible()) {
                    if (TwoFileProcessing.isVisible()) {
                        TwoFileProcessing.setVisible(false);
                        File1PathText.setText("");
                        File2PathText.setText("");
                        File1ResultText.setText("");
                        File2ResultText.setText("");
                    }
                    if (OneFileProcessing.isVisible()) {
                        OneFileProcessing.setVisible(false);
                        FilePathText.setText("");
                        FileResultText.setText("");
                    }
                    ProgressBar1.setProgress(0);
                    BrowseButton.setVisible(true);
                    FilesPathField.setVisible(true);
                    FilesPathField.setText("");
                    N1.setDisable(true);
                    BrowseButton.fire();
                }
            }
        });

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

        BrowseButton.setOnAction(new EventHandler<ActionEvent>() {         //Выбор файлов для обработки
            @Override
            public void handle(ActionEvent event) {
                Node source = (Node) event.getSource();
                List<File> dialogRes = dialog.showOpenMultipleDialog(source.getScene().getWindow());
                if (dialogRes != null) {
                    if (files.size() == 0)
                        files.addAll(dialogRes);
                    else {
                        for (File f : dialogRes) {
                            try {
                                for (File ff : files) {
                                    if (ff.getAbsolutePath().equals(f.getAbsolutePath()))
                                        throw new UniExp("");
                                }
                                files.add(f);
                            } catch (UniExp ue) {
                            }
                        }
                    }
                    int k = 0;
                    while (k < files.size()) {
                        if (files.get(k).length() == 0) {
                            alert.setTitle(null);
                            alert.setContentText("Файл " + files.get(k).getName() + " пуст!");
                            alert.showAndWait();
                            files.remove(k);
                        } else if (files.get(k).length() > 524288000) {
                            alert.setTitle(null);
                            alert.setContentText("Размер файла " + files.get(k).getName() + " превышает 500 мб. Возможно, " +
                                    "удастся обработать только его часть!");
                            alert.showAndWait();
                        } else
                            k++;
                    }
                    progressIndic = files.size();
                    if (files.size() > 0) {
                        if (processors[0] == null) {
                            processors[0] = new FileProcessor(locker, trigger, progressChanged);
                            Thread t = new Thread(processors[0]);
                            t.setName("1");
                            t.start();
                        }
                        if (processors[1] == null && files.size() > 1) {
                            processors[1] = new FileProcessor(locker, trigger, progressChanged);
                            Thread t = new Thread(processors[1]);
                            t.setName("2");
                            t.start();
                        }
                        if (files.size() == 1)
                            FilesPathField.setText(files.get(0).getName());
                        else
                            for (File f : files) {
                                FilesPathField.setText(FilesPathField.getText() + "\"" + f.getName() + "\"; ");
                            }
                    }
                }
                disOrEn(false);
            }
        });

        EncryptButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //if (!TwoFileProcessing.isVisible() && !OneFileProcessing.isVisible()) {
                try {
                    if (processFiles && files.size() == 0)
                        throw new UniExp("Выберите файлы для обработки!");
                    if (!processFiles && OriginalText.getText().equals(""))
                        throw new UniExp("Введите сообщение для обработки!");
                    EncryptButton.getScene().getWindow();

                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/RichelieuCipherGUI/Forms/SettingsForm.fxml"));
                    try {
                        loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Parent root = loader.getRoot();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.initOwner(EncryptButton.getScene().getWindow());
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.getIcons().add(new Image("/Resources/settings.png"));
                    stage.setTitle("Настройки");
                    SettingsFormController set = loader.getController();
                    set.setProcessText(!processFiles);
                    set.setAvoid(FilesPathField.getText());
                    set.extractSettings("EncSetts.txt");
                    stage.showAndWait();
                    try {
                        set.saveSettings("EncSetts.txt", "");
                    } catch (IOException ioe) {
                    }
                    if (set.isAccept())
                        acceptInputs(set, "enc", true);
                } catch (UniExp ue) {
                    alert.setTitle(null);
                    alert.setContentText(ue.getMessage());
                    alert.show();
                } catch (IOException ioe) {
                    alert.setTitle(null);
                    if (outputPath.toString().endsWith("\\"))
                        alert.setContentText("Не удалось создать файл в папке " + outputPath.toString());
                    else
                        alert.setContentText("Не удалось создать файл " + outputPath.toString());
                    alert.show();
                }
            }
        });

        UnencryptButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (processFiles && files.size() == 0)
                        throw new UniExp("Выберите файлы для обработки!");
                    if (!processFiles && OriginalText.getText().equals(""))
                        throw new UniExp("Введите сообщение для обработки!");
                    UnencryptButton.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/RichelieuCipherGUI/Forms/SettingsForm.fxml"));
                    try {
                        loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Parent root = loader.getRoot();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.initOwner(UnencryptButton.getScene().getWindow());
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(false);
                    stage.getIcons().add(new Image("/Resources/settings.png"));
                    stage.setTitle("Настройки");
                    SettingsFormController set = loader.getController();
                    set.setProcessText(!processFiles);
                    set.setAvoid(FilesPathField.getText());
                    set.extractSettings("DecSetts.txt");
                    stage.showAndWait();
                    try {
                        set.saveSettings("", "DecSetts.txt");
                    } catch (IOException ioe) {
                    }
                    if (set.isAccept())
                        acceptInputs(set, "dec", false);
                } catch (UniExp ue) {
                    alert.setTitle(null);
                    alert.setContentText(ue.getMessage());
                    alert.show();
                } catch (IOException ioe) {
                    alert.setTitle(null);
                    if (outputPath.toString().endsWith("\\"))
                        alert.setContentText("Не удалось создать файл в папке " + outputPath.toString());
                    else
                        alert.setContentText("Не удалось создать файл " + outputPath.toString());
                    alert.show();
                }
            }
        });
    }
}
