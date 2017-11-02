package cs.ut.engine;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CsvReader {

    public static List<String> readTableHeader(File f) {
        List<String> cols = new ArrayList<>();

        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(f))){
            line = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!line.isEmpty()) {
            Collections.addAll(cols, line.split("[,;]"));
        }

        return cols;
    }
}
