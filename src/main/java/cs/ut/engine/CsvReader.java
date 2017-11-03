package cs.ut.engine;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.nodes.CSVConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CsvReader {
    private static final Logger log = Logger.getLogger(CsvReader.class);

    private static final CSVConfiguration config = MasterConfiguration.getInstance().getCSVConfiguration();
    private static String splitter = config.getSplitter();

    private static final String CASE_ID_COL = "case_id_col";
    private static final String ACTIVITY_COL = "activity_col";
    private static final String DYNAMIC_CAT_COLS = "dynamic_cat_cols";
    private static final String STATIC_CAT_COLS = "static_cat_cols";
    private static final String DYNAMIC_NUM_COLS = "dynamic_num_cols";
    private static final String STATIC_NUM_COLS = "static_num_cols";
    private static final String TIMESTAMP_COL = "timestamp_col";

    public static List<String> readTableHeader(File f) {
        log.debug("Reading table header.");
        List<String> cols = new ArrayList<>();

        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            line = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!line.isEmpty()) {
            Collections.addAll(cols, line.split(splitter));
        }

        log.debug(String.format("Read <%s> columns as header for file <%s>", cols.size(), f.getName()));
        return cols;
    }

    public static Map<String, String> identifyUserColumns(List<String> cols) {
        Map<String, String> result = new HashMap<>();


        cols.forEach(col -> {
            config.getCaseId().forEach(val -> {
                if (val.equalsIgnoreCase(col)) result.put(CASE_ID_COL, col);
            });

            config.getActivityId().forEach(val -> {
                if (val.equalsIgnoreCase(col)) result.put(ACTIVITY_COL, col);
            });
        });

        return result;
    }

    public void generateDatasetParams(Map<String, List<String>> userCols) {
        Long start = System.currentTimeMillis();
        Map<String, Set<String>> rows = parseCsv();
        userCols.forEach((k, v) -> rows.remove(v.get(0)));

        Map<String, List<String>> categorisedColumns = classifyColumns(rows);
        userCols.forEach(categorisedColumns::put);

        Long end = System.currentTimeMillis();
        log.debug(String.format("Finished generating dataset parameters in <%s> ms", Long.toString(end - start)));
    }

    private Map<String, List<String>> classifyColumns(Map<String, Set<String>> rows) {
        Map<String, List<String>> classes = new HashMap<>();
        classes.put(DYNAMIC_CAT_COLS, new ArrayList<>());
        classes.put(STATIC_CAT_COLS, new ArrayList<>());
        classes.put(DYNAMIC_NUM_COLS, new ArrayList<>());
        classes.put(STATIC_NUM_COLS, new ArrayList<>());

        rows.forEach((k, v) -> {
            if (v.contains("")) {
                // remove empty columns so they do not affect categorization
                v.remove("");
            }

            if (v.size() == 1) {
                //static
                String val = v.iterator().next();

                if (StringUtils.isNumeric(val)) {
                    //numeric
                    classes.get(STATIC_NUM_COLS).add(k);
                } else {
                    //categorical
                    classes.get(STATIC_CAT_COLS).add(k);
                }
            } else {
                //dynamic
                Iterator<String> iterator = v.iterator();
                while (iterator.hasNext()) {
                    if (!StringUtils.isNumeric(iterator.next())) {
                        //is not numeric column
                        classes.get(DYNAMIC_CAT_COLS).add(k);
                        break;
                    }
                }

                classes.get(DYNAMIC_NUM_COLS).add(k);
            }
        });

        return classes;
    }

    private Map<String, Set<String>> parseCsv() {
        log.debug("Started parsing csv...");
        Long start = System.currentTimeMillis();

        Map<String, Set<String>> result = new LinkedHashMap<>();

        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(JobManager.getInstance().getCurrentFile()))) {
            line = br.readLine();
            if (line == null || (line.isEmpty())) {
                throw new RuntimeException("File is empty");
            } else {
                String[] colHeads = line.split(splitter);
                Arrays.stream(colHeads).forEach(head -> result.put(head, new HashSet<>()));
            }

            line = br.readLine();
            if (line == null || line.isEmpty()) {
                throw new RuntimeException("File must contain at least 2 rows");
            } else {
                processRow(line, result);
            }

            while ((line = br.readLine()) != null) {
                processRow(line, result);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed reading csv", e);
        }

        Long end = System.currentTimeMillis();
        log.debug(String.format("Finished parsing csv in <%s> ms", Long.toString(end - start)));
        return result;
    }

    private void processRow(String row, Map<String, Set<String>> map) {
        String[] cols = row.split(splitter);
        List<String> keys = new ArrayList<>(map.keySet());

        if (cols.length != map.keySet().size()) {
            throw new RuntimeException("Row has less/more columns that initial header");
        }

        for (int i = 0; i < cols.length; i++) {
            map.get(keys.get(i)).add(cols[i]);
        }
    }
}
