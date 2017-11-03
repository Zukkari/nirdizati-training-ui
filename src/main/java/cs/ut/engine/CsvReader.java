package cs.ut.engine;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.nodes.CSVConfiguration;
import cs.ut.engine.item.Case;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String LABEL_NUM_COLS = "label_num_cols";
    private static final String LABEL_CAT_COLS = "label_cat_cols";

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
        List<Case> cases = parseCsv(userCols.get(CASE_ID_COL).get(0));

        String timestampCol = identifyTimeStampColumn(cases.get(0).getAttributes());
        cases.forEach(c -> {
            c.getAttributes().remove(timestampCol);
            c.getAttributes().remove(userCols.get(CASE_ID_COL).get(0));

            if (c.getAttributes().containsKey("remtime")) c.getAttributes().remove("remtime");
            if (c.getAttributes().containsKey("label")) c.getAttributes().remove("label");

            c.setClassifiedCols(classifyColumns(c.getAttributes()));
        });

        Map<String, List<String>> classifiedColumnns = new HashMap<>();
        classifiedColumnns.putAll(userCols);
        classifiedColumnns.putAll(collectResultFromCases(cases));

        Long end = System.currentTimeMillis();
        log.debug(String.format("Finished generating dataset parameters in <%s> ms", Long.toString(end - start)));
    }

    private Map<String, List<String>> collectResultFromCases(List<Case> cases) {
        return new HashMap<>();
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
                    try {
                        Double d = Double.parseDouble(val);
                        log.debug(String.format("Successfully parsed double <%s>", d));
                        classes.get(STATIC_NUM_COLS).add(k);
                    } catch (NumberFormatException e) {
                        log.debug(String.format("Value is not a double <%s>", val));
                        //categorical
                        classes.get(STATIC_CAT_COLS).add(k);
                    }
                }
            } else {

                boolean found = false;
                //dynamic
                for (String aV : v) {
                    if (!StringUtils.isNumeric(aV)) {
                        //is not numeric column
                        classes.get(DYNAMIC_CAT_COLS).add(k);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    classes.get(DYNAMIC_NUM_COLS).add(k);
                }
            }
        });

        return classes;
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
                processRow(line, cases, caseIdColIndex, colHeads);
            }

            while ((line = br.readLine()) != null) {
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
}
