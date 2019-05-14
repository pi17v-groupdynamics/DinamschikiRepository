package Utilities;

import RichelieuCipherProcessor.FileProcessor;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Authorization extends JFrame {

    static String key;

    void extractKey() throws IOException, UniExp {
        File f = new File("Accounts.txt");
        //InputStream f = getClass().getClassLoader().getResourceAsStream("Accounts.txt");
        Scanner reader = new Scanner(f, "UTF-8");
        if(!reader.hasNextLine()){
            reader.close();
            reader = new Scanner(f, "Cp1251");
        }
       /// BufferedReader reader = new BufferedReader(new FileReader("Accounts.txt"));
        String line;
        while(!reader.nextLine().contains("supremeAdmin"));
        if(!reader.hasNextLine())
            throw new UniExp("Не удалось прочитать файл Accounts.txt!");
        line = reader.nextLine();
        key = line.substring(line.lastIndexOf(" ") + 1, line.length());
        reader.close();
    }

    public void saveToFile(String text, String pass) throws UniExp {
        BufferedWriter writer;
        try {
            if(key == null)
                extractKey();
            //FileWriter fw = new FileWriter(new File(this.getClass().getClassLoader()
            //        .getResource("/Accounts.txt").toExternalForm()), true);
            //writer = new BufferedWriter(fw);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Accounts.txt", true),
                    "UTF-8"));
            writer.append("Логин : " + text);
            writer.newLine();
            writer.append("Пароль : " + FileProcessor.proceed(pass, true, key));
            writer.newLine();
            for (int i = 1; i < 50; i++)
                writer.append("=");
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            throw new UniExp("Не удалось открыть или прочитать файл Accounts.txt! " +
                    "Вход в систему невозможен!");
        }
    }

    public boolean[] findAcc(String login, String password) throws UniExp {
        if (login.endsWith("supremeAdmin"))
            throw new UniExp("Доступ запрещен!");
        boolean[] accCheck = new boolean[3];
        boolean next = false;
        try {
            if(key == null)
                extractKey();
            //InputStream accs = getClass().getClassLoader().getResourceAsStream("Accounts.txt");
            File accs = new File("Accounts.txt");
            Scanner reader = new Scanner(accs, "UTF-8");
            if(!reader.hasNextLine()){
                reader.close();
                reader = new Scanner(accs, "Cp1251");
            }
            String line;
            List<String> list = new ArrayList<>();
            if (reader.hasNextLine()) {
                while (reader.hasNextLine()) {
                    line = reader.nextLine();
                    if (!next && !line.equals("supremeAdmin") && !line.endsWith(": " + key)) {
                        if (line.contains("Пароль : ")) {
                            list.add(line.substring(0, line.lastIndexOf(" ") + 1)
                                    + FileProcessor.proceed(line.substring(line.lastIndexOf(" ") + 1, line.length()),
                                    false, key));
                        } else
                            list.add(line);
                    } else {
                        if (line.equals("supremeAdmin"))
                            next = true;
                        else {
                            key = line.substring(line.lastIndexOf(" ") + 1, line.length());
                            next = false;
                        }
                    }
                }
                reader.close();
                //accs.close();
                if (list.indexOf(login) == list.indexOf(password) - 1) {                 // аккаунт найден в файле
                    accCheck[0] = true;
                    accCheck[1] = true;
                    accCheck[2] = true;
                    return accCheck;
                } else if (!list.contains(login)) {           // аккаунт не найден в файле
                    accCheck[0] = true;
                    accCheck[1] = false;
                    accCheck[2] = false;
                    return accCheck;
                } else if (list.contains(login) && !list.contains(password)) {    // аккаунт найден, но пароль неверный
                    accCheck[0] = true;
                    accCheck[1] = true;
                    accCheck[2] = false;
                    return accCheck;
                }
            } else
                throw new UniExp("Файл Accounts.txt пуст! Вход в систему невозможен!");
        } catch (FileNotFoundException e) {
            throw new UniExp("Файл Accounts.txt не найден! Вход в систему невозможен!");
        } catch (IOException e) {
            throw new UniExp("Не удалось открыть или прочитать файл Accounts.txt! " +
                    "Вход в систему невозможен!");
        } catch (NullPointerException npe){
            throw new UniExp("Файл Accounts.txt не найден! Вход в систему невозможен!");
        } catch (UniExp ue) {
            throw new UniExp(ue.getMessage());
        }
        accCheck[0] = false;                     // файл не найден
        accCheck[1] = false;
        accCheck[2] = false;
        return accCheck;
    }
}
