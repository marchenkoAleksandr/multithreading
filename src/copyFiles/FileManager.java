package copyFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.out;

public class FileManager {

    public static void main(String[] args) {

        out.println("Для копирования 1 файла укажите путь к файлу с его полным именем " +
                " или укажите путь к директории для копироания всех файлов");
        String filePath = Scan.getString();
        File fileIn = new File(filePath);
        if (fileIn.isDirectory()) {
            out.println("Ok");
        } else {
            fileIn = createFile(filePath);
        }

        File fileOut;
        if (fileIn.isDirectory()) {
            out.println("Введите путь для копирования всей папки");
            String dirPath = Scan.getString();
            fileOut = createDirectory(dirPath);
        } else {

            out.println("Введите имя директории для записи файла");
            String dirPath = Scan.getString();
            File dir = createDirectory(dirPath);

            out.println("Введите имя будущего файла");
            fileOut = new File(dir, Scan.getString());
        }

        out.println("Укажите количество потоков для копирования файла");
        int executors = Scan.getInt();
        if (executors < 2) {
            executors = 2;
            out.println("Количество потоков - 2");
        }

        out.println("Укажите размер буффера в байтах для копирования файла");
        long buffer = Scan.getLong();

/*
         Выполняем проверку - если файл является директорией, тогда создаем лист файлов и в цикле по очереди
        выполняем копирование каждого файла.
*/

        if (fileIn.isDirectory()) {
            String[] children = fileIn.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(fileIn, children[i]), new File(fileOut, children[i]), buffer, executors);
            }

        } else {
            copyDirectory(fileIn, fileOut, buffer, executors);
        }

    }

/*
    Метод копирования файлов, путем создания листа задач, для Исполнителей
*/

    private static void copyDirectory(File fileIn, File fileOut, long buffer, int executors) {

        try {
            if (fileOut.exists()) {
                fileOut.delete();
                fileOut.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long countBlock = (long) Math.ceil(((double) fileIn.length() / buffer));

        List<Copier> tasks = new ArrayList<>();
        for (int i = 0, pointerPosition = 0; i < countBlock; i++) {

            if ((i + 1) != countBlock) {
                tasks.add(new Copier(fileIn, fileOut, pointerPosition, buffer));
                pointerPosition = (int) (i * buffer);

            } else {
                buffer = fileIn.length() - pointerPosition;
                tasks.add(new Copier(fileIn, fileOut, pointerPosition, buffer));
            }
        }
        ExecutorService service = Executors.newFixedThreadPool(executors);
        tasks.forEach(service::submit);

/*
        Поток для примерного контроля оставшегося времени
*/

        Thread monitorProgress = new Thread(() -> {
            try {
                long temp = 0;
                out.println("Выполняется копирование файла " + fileIn.getName());
                while (true) {

                    Thread.sleep(1000);

                    if (fileIn.length() == fileOut.length()) {
                        out.println("Готово, файл " + fileIn.getName() + " скопирован");
                        temp = 0;
                        break;
                    } else {

                        long totalTime = Copier.progress - temp;

                        if (totalTime != 0) {
                            int time = (int) ((fileIn.length() - Copier.progress) / totalTime);

                            out.println("Примерное оставшееся время " + time + " сек");
                            temp = Copier.progress;
                        } else {
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {/*NOP*/}
        });
        monitorProgress.start();

        try {
            monitorProgress.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        service.shutdown();
    }


    private static File createFile(String filePath) {
        File file = new File(filePath);
        while (!file.isFile()) {
            out.println("Такого файла не существует, введите путь к существующему файлу");
            out.println("Или нажмите N для окончания работы");

            String text = Scan.getString();
            file = new File(text);

            if (text.equals("N")) {
                out.println("Работа закончена");
                System.exit(1);
            }
        }
        return file;
    }

    private static File createDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            out.println("Такой директории не существует, создать директорию?, нажмите Y или N");
            String str2 = Scan.getString();
            if (str2.equals("Y")) {
                out.println(dir.mkdirs());
            }
            if (str2.equals("N")) {
                while (!dir.exists()) {
                    out.println("Введите имя директории");
                    dir = new File(Scan.getString());
                }
            }
        }
        return dir;
    }

}
