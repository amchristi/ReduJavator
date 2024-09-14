import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by arpit on 6/27/16.
 */
public class FileWriterUtil {

    public static void write(String fullFilePath, String text ) {

        BufferedWriter output = null;
        try {
            java.io.File file = new File(fullFilePath);
            output = new BufferedWriter(new FileWriter(file));
            output.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
