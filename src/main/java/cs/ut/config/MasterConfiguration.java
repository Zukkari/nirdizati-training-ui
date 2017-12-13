package cs.ut.config;

import cs.ut.config.items.ModelParameter;
import cs.ut.config.nodes.CsvConfiguration;
import cs.ut.config.nodes.DirectoryPathConfiguration;
import cs.ut.config.nodes.HeaderConfiguration;
import cs.ut.config.nodes.ModelConfiguration;
import cs.ut.config.nodes.PageConfiguration;
import cs.ut.config.nodes.ThreadPoolConfiguration;
import cs.ut.exceptions.NirdizatiRuntimeException;
import cs.ut.util.JsonReaderKt;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MasterConfiguration {
    private static final Logger log = Logger.getLogger(MasterConfiguration.class);

    private static MasterConfiguration master;

    private DirectoryPathConfiguration directoryPathConfiguration;

    private ModelConfiguration modelConfiguration;

    private CsvConfiguration csvConfiguration;

    private ThreadPoolConfiguration threadPoolConfiguration;

    private HeaderConfiguration headerConfiguration;

    private PageConfiguration pageConfiguration;

    private MasterConfiguration() {
        configureLogger();
    }

    /**
     * Instantiates master configuration or if it has already been instantiated then returns the object
     *
     * @return master configuration object that contains configurable values defined in configuration.xml
     */
    public static MasterConfiguration getInstance() {
        if (master == null) {
            master = new MasterConfiguration();
        }
        return master;
    }

    public HeaderConfiguration getHeaderConfiguration() {
        if (headerConfiguration == null) {
            headerConfiguration = readClass(HeaderConfiguration.class, "headerConfiguration");
        }
        return headerConfiguration;
    }

    /**
     * Training model configuration for Nirdizati application declared in configuration.xml
     *
     * @return Model configuration parameters
     */
    public ModelConfiguration getModelConfiguration() {
        if (modelConfiguration == null) {
            modelConfiguration = readClass(ModelConfiguration.class, "modelConfig");
            modelConfiguration.prepareData();
        }
        return modelConfiguration;
    }

    /**
     * Thread pool configuration declared in configuration.xml
     *
     * @return configuration for thread pool
     */
    public ThreadPoolConfiguration getThreadPoolConfiguration() {
        if (threadPoolConfiguration == null) {
            threadPoolConfiguration = readClass(ThreadPoolConfiguration.class, "threadpool");
        }
        return threadPoolConfiguration;
    }

    /**
     * Optimized parameters for logs
     *
     * @return Map of optimized parameters where key is file name and value is list of optimized parameters
     */
    public Map<String, List<ModelParameter>> getOptimizedParams() {
        return JsonReaderKt.readHyperParameterJson();
    }

    /**
     * Configures logger and Enables appenders for Log4j
     */
    private void configureLogger() {
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().setAdditivity(false);

        ConsoleAppender ca = new ConsoleAppender();
        ca.setLayout(new PatternLayout("<%d{ISO8601}> <%p> <%C{1}.class:%L> <%m>%n"));
        ca.setThreshold(Level.DEBUG);
        ca.activateOptions();

        Logger.getRootLogger().addAppender(ca);

        FileAppender fileAppender = new FileAppender();
        fileAppender.setLayout(new PatternLayout("<%d{ISO8601}> <%p> <%C{1}.class:%L> <%m>%n"));
        fileAppender.setName("nirdizati_ui_log.log");
        fileAppender.setFile("nirdizati_ui_log.log");
        fileAppender.setThreshold(Level.DEBUG);
        fileAppender.setAppend(true);
        fileAppender.activateOptions();

        Logger.getRootLogger().addAppender(fileAppender);
    }

    /**
     * Directory configuration where Nirdizati will look for various elements declared in configuration.xml
     *
     * @return DirectoryPathConfiguration
     */
    public DirectoryPathConfiguration getDirectoryPathConfiguration() {
        if (directoryPathConfiguration == null) {
            directoryPathConfiguration = readClass(DirectoryPathConfiguration.class, "paths");
            directoryPathConfiguration.createTmpDir();
        }
        return directoryPathConfiguration;
    }

    /**
     * CSV reader configuration that defines user columns for csv parsing
     *
     * @return CSVConfiguration
     */
    public CsvConfiguration getCSVConfiguration() {
        if (csvConfiguration == null) {
            csvConfiguration = readClass(CsvConfiguration.class, "csvConfig");
        }
        return csvConfiguration;
    }

    /**
     * Page configuration that defines redirects in Nirdizati Training UI
     *
     * @return PageConfiguration
     */
    public PageConfiguration getPageConfiguration() {
        if (pageConfiguration == null) {
            pageConfiguration = readClass(PageConfiguration.class, "pageConfig");
        }
        log.debug(String.format("Successfully retrieved %s page configurations", pageConfiguration.getPage().size()));
        return pageConfiguration;
    }

    private <T> T readClass(Class<T> clazz, String nodeName) {
        File file = new File(getClass().getClassLoader().getResource("configuration.xml").getFile());
        JAXBContext jaxbContext = null;
        Unmarshaller unmarshaller = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setNamespaceAware(true);
        DocumentBuilder db = null;
        Document doc;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new NirdizatiRuntimeException("Failed to parse config", e, false, false);
        }

        NodeList node = doc.getElementsByTagName(nodeName);

        try {
            jaxbContext = JAXBContext.newInstance(clazz);
            unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(node.item(0), clazz).getValue();
        } catch (JAXBException e) {
            throw new NirdizatiRuntimeException("Failed to read directories", e, false, false);
        }
    }
}