package cs.ut.engine;

import com.google.common.collect.Lists;
import cs.ut.config.MasterConfiguration;
import cs.ut.config.nodes.CSVConfiguration;
import cs.ut.engine.item.Case;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.StrictMath.max;

public class CsvReader {
    private static final Logger log = Logger.getLogger(CsvReader.class);

    private static final CSVConfiguration config = MasterConfiguration.getInstance().getCSVConfiguration();
    private static String splitter = config.getSplitter();
    private static List<String> emptyValues = config.getEmptyValues();
    private static Integer confThreshold = config.getThreshold();

    private static final String CASE_ID_COL = "case_id_col";
    private static final String ACTIVITY_COL = "activity_col";
    private static final String TIMESTAMP_COL = "timestamp_col";
    private static final String LABEL_NUM_COLS = "label_num_cols";
    private static final String LABEL_CAT_COLS = "label_cat_cols";

    private static final String STATIC = "static";
    private static final String DYNAMIC = "dynamic";
    private static final String NUM_COL = "_num_cols";
    private static final String CAT_COLS = "_cat_cols";

    private Integer rowCount = 0;

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
                if (val.matches(col.toLowerCase())) result.put(CASE_ID_COL, col);
            });

            config.getActivityId().forEach(val -> {
                if (val.matches(col.toLowerCase())) result.put(ACTIVITY_COL, col);
            });
        });

        return result;
    }

    public Map<String, List<String>> generateDatasetParams(Map<String, List<String>> userCols) {
        Long start = System.currentTimeMillis();
        List<Case> cases = parseCsv(userCols.get(CASE_ID_COL).get(0));

        Map<String, Set<String>> colVals = new HashMap<>();
        String timestampCol = identifyTimeStampColumn(cases.get(0).getAttributes());
        cases.forEach(c -> {
            c.getAttributes().remove(timestampCol);
            c.getAttributes().remove(userCols.get(CASE_ID_COL).get(0));
            c.getAttributes().remove(userCols.get(ACTIVITY_COL).get(0));

            if (c.getAttributes().containsKey("remtime")) c.getAttributes().remove("remtime");
            if (c.getAttributes().containsKey("label")) c.getAttributes().remove("label");

            classifyColumns(c);

            c.getAttributes().forEach((k, v) -> {
                if (colVals.containsKey(k)) colVals.get(k).addAll(v);
                else colVals.put(k, v);
            });
        });

        Set<String> alreadyClassifiedColumns = new HashSet<>();
        Map<String, List<String>> resultColumns = new HashMap<>();
        resultColumns.put(DYNAMIC.concat(CAT_COLS), new ArrayList<>());
        resultColumns.put(DYNAMIC.concat(NUM_COL), new ArrayList<>());
        resultColumns.put(STATIC.concat(NUM_COL), new ArrayList<>());
        resultColumns.put(STATIC.concat(CAT_COLS), new ArrayList<>());

        cases.forEach(c -> {
            Map<String, List<String>> map = c.getClassifiedColumns();
            map.put(DYNAMIC.concat(CAT_COLS), new ArrayList<>());
            map.put(DYNAMIC.concat(NUM_COL), new ArrayList<>());
            map.put(STATIC.concat(NUM_COL), new ArrayList<>());
            map.put(STATIC.concat(CAT_COLS), new ArrayList<>());

            c.getDynamicCols().forEach(col -> insertIntoMap(map, DYNAMIC, col, colVals.get(col)));
            c.getStaticCols().forEach(col -> insertIntoMap(map, STATIC, col, colVals.get(col)));

            postProcessCase(resultColumns, c, alreadyClassifiedColumns);
        });

        resultColumns.putAll(userCols);
        resultColumns.get(DYNAMIC.concat(CAT_COLS)).add(userCols.get(ACTIVITY_COL).get(0));
        resultColumns.put(TIMESTAMP_COL, Collections.singletonList(timestampCol));
        resultColumns.put(LABEL_NUM_COLS, Collections.singletonList(JobManager.getInstance().getPredictionType().getParameter()));
        resultColumns.put(LABEL_CAT_COLS, Collections.emptyList());

        Long end = System.currentTimeMillis();
        log.debug(String.format("Finished generating dataset parameters in <%s> ms", Long.toString(end - start)));

        return resultColumns;
    }

    private void postProcessCase(Map<String, List<String>> resultColumns, Case c, Set<String> alreadyClassifiedColumns) {
        Map<String, List<String>> caseCols = c.getClassifiedColumns();

        caseCols.get(STATIC.concat(NUM_COL))
                .forEach(it ->
                        categorizeColumn(it, STATIC.concat(NUM_COL), resultColumns, alreadyClassifiedColumns, new ArrayList<>()));

        caseCols.get(STATIC.concat(CAT_COLS))
                .forEach(it ->
                        categorizeColumn(it, STATIC.concat(CAT_COLS), resultColumns, alreadyClassifiedColumns, Collections.singletonList(STATIC.concat(NUM_COL))));

        caseCols.get(DYNAMIC.concat(NUM_COL))
                .forEach(it ->
                        categorizeColumn(it, DYNAMIC.concat(NUM_COL), resultColumns, alreadyClassifiedColumns, Collections.singletonList(STATIC.concat(NUM_COL))));

        caseCols.get(DYNAMIC.concat(CAT_COLS))
                .forEach(it ->
                        categorizeColumn(it, DYNAMIC.concat(CAT_COLS), resultColumns, alreadyClassifiedColumns, Lists.newArrayList(
                                STATIC.concat(NUM_COL), STATIC.concat(CAT_COLS), DYNAMIC.concat(NUM_COL)
                        )));
    }

    private void categorizeColumn(String column, String key, Map<String, List<String>> results, Set<String> alreadyDone, List<String> lookThrough) {
        if (!alreadyDone.contains(column)) {
            alreadyDone.add(column);
            results.get(key).add(column);
        } else {
            lookThrough.forEach(col -> {
                if (results.get(col).contains(column)) {
                    results.get(col).remove(column);
                    results.get(key).add(column);
                }
            });
        }
    }

    private void insertIntoMap(Map<String, List<String>> map, String cat, String col, Set<String> values) {
        boolean isNumeric = true;

        for (String value : values) {
            Double d;
            try {
                d = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                d = null;
            }

            if (d == null) {
                isNumeric = false;
                break;
            }
        }

        double threshold = max(confThreshold, 0.001 * rowCount);
        if (values.size() < threshold || !isNumeric) {
            map.get(cat.concat(CAT_COLS)).add(col);
        } else {
            map.get(cat.concat(NUM_COL)).add(col);
        }
    }

    private String identifyTimeStampColumn(Map<String, Set<String>> rows) {
        final String[] colName = new String[1];

        List<DateFormat> formats =
                config.getTimestampFormat()
                        .stream()
                        .map(SimpleDateFormat::new)
                        .collect(Collectors.toList());

        rows.forEach((k, v) -> {
            String potentialDate = v.iterator().next();

            formats.forEach(format -> {
                try {
                    if (format.parse(potentialDate) != null) {
                        //is a timestamp column
                        colName[0] = k;
                    }
                } catch (ParseException e) {
                    log.debug(String.format("Not a date column <%s>", k));
                }
            });
        });

        return colName[0];
    }

    private void classifyColumns(Case c) {
        c.getAttributes().forEach((k, v) -> {
            emptyValues.forEach(it -> {
                if (v.contains(it)) v.remove(it);
            });

            if (v.size() == 1) {
                c.getStaticCols().add(k);
            } else {
                c.getDynamicCols().add(k);
            }
        });
    }

    private List<Case> parseCsv(String caseIdCol) {
        log.debug("Started parsing csv...");
        Long start = System.currentTimeMillis();

        List<Case> cases = new ArrayList<>();

        Integer caseIdColIndex = null;
        String[] colHeads;

        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(JobManager.getInstance().getCurrentFile()))) {
            line = br.readLine();
            if (line == null || (line.isEmpty())) {
                throw new RuntimeException("File is empty");
            } else {
                colHeads = line.split(splitter);
                caseIdColIndex = Arrays.asList(colHeads).indexOf(caseIdCol);
            }

            line = br.readLine();
            if (line == null || line.isEmpty()) {
                throw new RuntimeException("File must contain at least 2 rows");
            } else {
                rowCount++;
                processRow(line, cases, caseIdColIndex, colHeads);
            }

            while ((line = br.readLine()) != null) {
                rowCount++;
                processRow(line, cases, caseIdColIndex, colHeads);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed reading csv", e);
        }

        Long end = System.currentTimeMillis();
        log.debug(String.format("Finished parsing csv in <%s> ms", Long.toString(end - start)));
        return cases;
    }

    private void processRow(String row, List<Case> cases, Integer caseIndex, String[] head) {
        String[] cols = row.split(splitter);

        Case c = findCaseById(cols[caseIndex], cases);

        if (c == null) {
            c = new Case();
            c.setId(cols[caseIndex]);
            prepareCase(head, c);
            cases.add(c);
        }

        List<String> keys = new ArrayList<>(c.getAttributes().keySet());

        for (int i = 0; i < cols.length; i++) {
            c.getAttributes().get(keys.get(i)).add(cols[i]);
        }
    }

    private void prepareCase(String[] columns, Case c) {
        Arrays.stream(columns).forEach(head -> c.getAttributes().put(head, new HashSet<>()));
    }

    private Case findCaseById(String id, List<Case> cases) {
        return cases.stream().filter(it -> id.equalsIgnoreCase(it.getId())).findFirst().orElse(null);
    }

    public JSONObject generateJson(Map<String, List<String>> map) {
        JSONObject jsonObject = new JSONObject();

        map.forEach((k, v) -> {
            if (v.size() == 1) {
                jsonObject.put(k, v.get(0));
            } else {
                jsonObject.put(k, v);
            }
        });

        return jsonObject;
    }

    public List<String> getColumnList() {
        return Lists.newArrayList(
                DYNAMIC.concat(NUM_COL),
                DYNAMIC.concat(CAT_COLS),
                STATIC.concat(NUM_COL),
                STATIC.concat(CAT_COLS)
        );
    }
}
