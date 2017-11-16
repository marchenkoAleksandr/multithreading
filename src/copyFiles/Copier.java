package copyFiles;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


public class Copier implements Runnable {

    private File sourceLocation;  // Исходный файл
    private File targetLocation;  // файл куда пишем
    private int pointerPosition;  // Позиция указателя
    private long buffer;
    public static long progress;


    public Copier(File sourceLocation, File targetLocation, int pointerPosition, long buffer) {
        this.sourceLocation = sourceLocation;
        this.targetLocation = targetLocation;
        this.pointerPosition = pointerPosition;
        this.buffer = buffer;
    }


    @Override
    public void run() {

        try (RandomAccessFile in = new RandomAccessFile(sourceLocation, "r");
             RandomAccessFile out = new RandomAccessFile(targetLocation, "rw")) {

            in.seek(pointerPosition);
            out.seek(pointerPosition);

            byte[] buf = new byte[(int) buffer];
            in.read(buf);
            out.write(buf);

            if (progress >= in.length()) {
                progress = 0;
            } else {
                progress += buffer;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}