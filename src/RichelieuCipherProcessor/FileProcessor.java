package RichelieuCipherProcessor;

import Utilities.UniExp;
import Utilities.state;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FileProcessor implements Runnable {
    private final Object locker, writeLocker;
    private volatile state st;
    private File input, output;
    private boolean encrypt;
    private StringBuilder key;
    private Scanner txtReader;
    private FileWriter txtWriter;
    private PDFParser pdfParser;
    private PDDocument pdfInput, pdfOutput;
    private PDFTextStripper pdfReader;
    private PDPageContentStream pdfWriter;
    private Thread fileWriter;
    private volatile StringBuilder lines;
    private PDFont font;
    private PDPage page;
    private int startPosition;
    private Alert error;
    private Button trigger;
    private Button changeProgressIfSingle;
    private volatile StringBuilder text;
    private boolean single;
    private String saveErrorMsg;
    private int textInpurLen;
    private ArrayList<String> linesSplit;

    public FileProcessor(Object locker, Button trigger, Button changeProgress) {
        this.locker = locker;
        this.trigger = trigger;
        changeProgressIfSingle = changeProgress;
        writeLocker = new Object();
        key = new StringBuilder();
        single = false;
        fileWriter = new Thread(new Runnable() {
            @Override
            public void run() {
                writeToFile();
            }
        });
        fileWriter.start();
        lines = new StringBuilder();
        error = new Alert(Alert.AlertType.ERROR);
        st = state.waiting;
        text = new StringBuilder();
        textInpurLen = 0;
        linesSplit = new ArrayList<>();
    }

    public String getSaveErrorMsg() {
        return saveErrorMsg;
    }

    public void setKey(String newKey) {
        if (key.length() > 0)
            key.delete(0, key.length());
        key.append(newKey);
    }

    public Object getLocker() {
        return locker;
    }

    public void setSingle(boolean isSingle) {
        single = isSingle;
    }

    public void setOperationType(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public void setState(state newState) {
        st = newState;
    }

    public void setInput(File file) throws IOException {
        input = file;
        startPosition = 0;
        if (txtReader != null)
            txtReader.close();
        if (file.getName().endsWith(".txt")) {
            txtReader = new Scanner(file, "UTF-8");
            if (!txtReader.hasNextLine()) {
                txtReader.close();
                txtReader = new Scanner(file, "Cp1251");
                if (!txtReader.hasNextLine()) {
                    txtReader.close();
                    txtReader = new Scanner(file, "Cp1252");
                }
            }
        } else {
            pdfParser = new PDFParser(new RandomAccessBufferedFileInputStream(file));
            pdfInput = new PDDocument(pdfParser.getDocument());
            pdfReader = new PDFTextStripper();
        }
    }

    public void setOriginalText(String newText) {
        if (text.length() > 0)
            text.delete(0, text.length());
        text.append(newText);
    }

    public void setOutput(File file) throws IOException {
        output = file;
        if (txtWriter != null)
            txtWriter.close();
        if (pdfOutput != null)
            pdfOutput.close();
        if (file.getName().endsWith(".txt"))
            txtWriter = new FileWriter(file);
        else {
            pdfOutput = new PDDocument();
            if (pdfInput != null)
                font = PDType0Font.load(pdfInput, getClass().getClassLoader().getResourceAsStream("Resources/arial.ttf"));
            else
                font = PDType0Font.load(pdfOutput, getClass().getClassLoader().getResourceAsStream("Resources/arial.ttf"));
        }
        fileWriter.setName("Запись в " + output.getName());
    }

    public String getLines() {
        String res = lines.toString();
        lines.delete(0, lines.length());
        return res;
    }

    public String getOriginalText(boolean delAfter) {
        String res = text.toString();
        if (delAfter)
            text.delete(0, text.length());
        return res;
    }

    public int getProcessed() {
        return startPosition;
    }

    public long getInputLength() {
        if (input != null)
            return input.length();
        else
            return textInpurLen;
    }

    public state getState() {
        return st;
    }

    public static String proceed(String message, boolean encrypt, String key) {
        StringBuilder result = new StringBuilder();
        int position;
        int processed = 0;
        String[] strs = key.split(" ");
        try {
            while (result.length() < message.length()) {
                for (int i = 0; i < strs.length; i++) {
                    if (!encrypt && (processed + strs[i].length() >= message.length())) {
                        StringBuilder temp = new StringBuilder();
                        int max = message.length() - processed;
                        for (int k = 0; k < strs[i].length(); k++) {
                            if (Character.getNumericValue(strs[i].charAt(k)) - 1 < max)
                                temp.append(strs[i].charAt(k));
                        }
                        strs[i] = temp.toString();
                    }
                    for (int j = 0; j < strs[i].length(); j++) {
                        if (encrypt)
                            position = Character.getNumericValue(strs[i].charAt(j)) - 1;
                        else
                            position = strs[i].indexOf("" + (j + 1));
                        if (position != -1 && position + processed < message.length()) {
                            result.append(message.charAt(processed + position));
                            if (result.length() == message.length())
                                throw new UniExp("");
                        }
                    }
                    processed += strs[i].length();
                    if (processed >= message.length())
                        throw new UniExp("");
                }
            }
        } catch (UniExp e) {
        }
        return result.toString();
    }

    private void writeLines() throws InterruptedException {
        synchronized (writeLocker) {
            writeLocker.notifyAll();
            writeLocker.wait();
        }
        startPosition = lines.length();
        if (single)
            changeProgressIfSingle.fire();
    }

    private void writeToFile() {
        while (st != state.exit) {
            try {
                synchronized (writeLocker) {
                    writeLocker.wait();
                }
                try {
                    if (st == state.exit && lines.length() == 0) throw new UniExp("");
                    if (output.getName().endsWith(".txt")) {
                        txtWriter.write(lines.substring(startPosition, lines.length()));
                    } else {
                        int height = 750;
                        page = new PDPage();
                        pdfOutput.addPage(page);
                        pdfWriter = new PDPageContentStream(pdfOutput, page);
                        pdfWriter.setFont(font, 12);
                        pdfWriter.beginText();
                        pdfWriter.newLineAtOffset(30, height);
                        String[] linesSplit = lines.substring(startPosition, lines.length())
                                .replaceAll("\t", "  ").replaceAll("\r", "").replaceAll("\uFEFF", "")
                                .replaceAll("\n", System.getProperty("line.separator")).split(System.getProperty("line.separator"));
                        for (int i = 0; i < linesSplit.length; i++) {
                            pdfWriter.showText(linesSplit[i]);
                            height -= 12;
                            if (height > 50)
                                pdfWriter.newLineAtOffset(0, -12);
                            else if (i != linesSplit.length - 1) {
                                pdfWriter.endText();
                                pdfWriter.close();
                                page = new PDPage();
                                pdfOutput.addPage(page);
                                height = 750;
                                pdfWriter = new PDPageContentStream(pdfOutput, page);
                                pdfWriter.setFont(font, 12);
                                pdfWriter.beginText();
                                pdfWriter.newLineAtOffset(30, height);
                            }
                        }
                        pdfWriter.endText();
                        pdfWriter.close();
                    }
                } catch (UniExp ue) {
                } catch (IOException ioe) {
                }
                synchronized (writeLocker) {
                    writeLocker.notifyAll();
                }
            } catch (InterruptedException ie) {
            }
        }

    }

    private void textPreparer(String str) {
        if (str.length() <= 87) {
            text.append(str + System.getProperty("line.separator"));
            lines.append(FileProcessor.proceed(str, encrypt, key.toString()) + System.getProperty("line.separator"));
        } else {
            linesSplit.clear();
            int j = 0;
            if (str.contains("не знал"))
                System.out.println("check");
            while (j * 87 + 87 < str.length()) {
                linesSplit.add(str.substring(j * 87, j * 87 + 87));
                text.append(linesSplit.get(j) + System.getProperty("line.separator"));
                lines.append(FileProcessor.proceed(linesSplit.get(j),
                        encrypt, key.toString()) + System.getProperty("line.separator"));
                j++;
            }
            if (linesSplit.size() * 87 < str.length()) {
                linesSplit.add(str.substring(j * 87, str.length()));
                text.append(linesSplit.get(j) + System.getProperty("line.separator"));
                lines.append(FileProcessor.proceed(linesSplit.get(j),
                        encrypt, key.toString()) + System.getProperty("line.separator"));
            }
        }
    }

    public void run() {
        while (st != state.exit) {
            try {
                try {
                    while (st == state.waiting) {
                        synchronized (locker) {
                            locker.wait();
                        }
                    }
                    if (st == state.exit) throw new UniExp("");
                    if (saveErrorMsg == null || !saveErrorMsg.equals(""))
                        saveErrorMsg = "";
                    startPosition = 0;
                    //if (input.getName().equals(output.getName()))
                     //   System.out.println("aaaaa");
                    long startTime = System.nanoTime(), currTime;
                    if (text.length() == 0) {
                        if (input.getName().endsWith(".txt")) {
                            while (txtReader.hasNextLine()) {
                                textPreparer(txtReader.nextLine());
                                //String s = txtReader.nextLine();
                                //text.append(s + System.getProperty("line.separator"));
                                //lines.append(FileProcessor.proceed(s, encrypt, key.toString()) + System.getProperty("line.separator"));
                                if (lines.length() - startPosition >= 3000 && !input.getName().equals(output.getName())) {
                                    writeLines();
                                }
                                currTime = System.nanoTime();
                                if (currTime - startTime > 300000000000l)
                                    throw new UniExp("");
                                if (st == state.exit) throw new UniExp("");
                            }
                            if (startPosition < lines.length()) {
                                writeLines();
                            }
                            txtReader.close();
                        } else {
                            pdfParser.parse();
                            for (int i = 1; i <= pdfInput.getPages().getCount(); i++) {
                                pdfReader.setStartPage(i);
                                pdfReader.setEndPage(i);
                                for (String str : pdfReader.getText(pdfInput).split(System.getProperty("line.separator"))) {
                                    textPreparer(str);
                                }
                                writeLines();
                                currTime = System.nanoTime();
                                if (currTime - startTime > 300000000000l)
                                    throw new UniExp("");
                                if (st == state.exit) throw new UniExp("");
                            }
                        }
                    } else {
                        String[] strs = text.toString().replaceAll("\r", "").split("\n");
                        textInpurLen = text.length();
                        text.delete(0, text.length());
                        for (int i = 0; i < strs.length; i++)
                            lines.append(FileProcessor.proceed(strs[i], encrypt, key.toString()) + System.getProperty("line.separator"));
                        writeLines();
                    }
                } catch (InterruptedException ie) {
                } catch (UniExp ue) {
                    try {
                        if (startPosition < lines.length()) {
                            writeLines();
                        }
                    } catch (InterruptedException ie) {
                    }
                    if (txtReader != null)
                        txtReader.close();
                    synchronized (writeLocker) {
                        writeLocker.notifyAll();
                    }
                } catch (IOException ioe) {
                }
                if (output != null) {
                    if (output.getName().endsWith(".pdf")) {
                        pdfOutput.save(output);
                        pdfOutput.close();
                    } else
                        txtWriter.close();
                    if (pdfInput != null)
                        pdfInput.close();
                }
            } catch (IOException ioe) {
                saveErrorMsg = "Не удается сохранить файл " + output.getName() + "!";
                System.out.println(ioe.getMessage());
            }
            if (st != state.exit) {
                input = null;
                output = null;
                st = state.waiting;
                trigger.fire();
                if (single)
                    single = false;
            }
            if (st == state.exit)
                synchronized (writeLocker) {
                    writeLocker.notifyAll();
                }
        }
    }

}
