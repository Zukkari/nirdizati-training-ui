package cs.ut.jobs;

import cs.ut.engine.CsvReader;
import cs.ut.engine.FileWriter;
import cs.ut.exceptions.NirdizatiRuntimeException;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataSetGenerationJob extends Job {
    private Map<String, List<String>> parameters;
    private JSONObject json;
    private File file;
    private String fileName;

    public DataSetGenerationJob(Map<String, List<String>> identifiedColumns, File currentFile) {
        this.parameters= identifiedColumns;
        this.file = currentFile;
    }

    @Override
    public void preProcess() {
        fileName = FilenameUtils.getBaseName(file.getName());

        json = new JSONObject();
        json.put(CsvReader.CASE_ID_COL, parameters.remove(CsvReader.CASE_ID_COL).get(0));
        json.put(CsvReader.TIMESTAMP_COL, parameters.remove(CsvReader.TIMESTAMP_COL).get(0));
        json.put(CsvReader.ACTIVITY_COL, parameters.remove(CsvReader.ACTIVITY_COL).get(0));
        json.put(CsvReader.LABEL_NUM_COLS, parameters.remove(CsvReader.LABEL_NUM_COLS));
        json.put(CsvReader.LABEL_CAT_COLS, new ArrayList<>());

        parameters.forEach((k, v) -> json.put(k, v));
    }

    @Override
    public void execute() {
        FileWriter writer = new FileWriter();
        writer.writeJsonToDisk(json, fileName, datasetDir);
    }

    @Override
    public void postExecute() {
        File result = new File(coreDir.concat(datasetDir).concat(fileName.concat(".json")));
        if (!result.exists()) {
            throw new NirdizatiRuntimeException(String.format("Could not write file to disk <%s>", result.getAbsolutePath()));
        }
    }
}
