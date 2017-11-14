package cs.ut.config;

import cs.ut.config.items.HeaderItem;
import cs.ut.config.items.ModelProperties;
import cs.ut.config.nodes.CSVConfiguration;
import cs.ut.config.nodes.DirectoryPathConfiguration;
import cs.ut.config.nodes.ModelConfiguration;
import cs.ut.config.nodes.PageConfiguration;
import cs.ut.engine.Worker;
import cs.ut.exceptions.NirdizatiRuntimeException;
import cs.ut.util.JsonReaderKt;
import org.apache.log4j.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

@XmlRootElement(name = "configuration")
public class MasterConfiguration {
    private static final Logger log = Logger.getLogger(MasterConfiguration.class);

    private static MasterConfiguration master;

    @XmlElement(name = "pageConfig")
    private PageConfiguration pageConfiguration;

    @XmlElementWrapper(name = "headerConfiguration")
    @XmlElement(name = "headerItem")
    private List<HeaderItem> headerItems;

    @XmlElement(name = "modelConfig")
    private ModelProperties modelProperties;

    @XmlElementWrapper(name = "extensions")
    @XmlElement(name = "ext")
    private List<String> extensions;

    @XmlElementWrapper(name = "userCols")
    @XmlElement(name = "col")
    private List<String> userCols;

    private DirectoryPathConfiguration directoryPathConfiguration;

    private ModelConfiguration modelConfiguration;

    private CSVConfiguration csvConfiguration;

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
            try {
                master.readMasterConfig();
            } catch (JAXBException e) {
                throw new NirdizatiRuntimeException("Failed to read master configuration", e);
            }
        }
        return master;
    }

    /**
     * Reads configuration.xml file and parses it into Java objects.
     *
     * @throws JAXBException if configuration is defined incorrectly
     */
    private void readMasterConfig() throws JAXBException {
        log.debug("Reading master configuration");
        File file = new File(getClass().getClassLoader().getResource("configuration.xml").getFile());

        JAXBContext jaxbContext = JAXBContext.newInstance(MasterConfiguration.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        MasterConfiguration configuration = (MasterConfiguration) unmarshaller.unmarshal(file);
        log.debug("Finished reading configuration");

        headerItems = configuration.getHeaderItems();
        log.debug(String.format("Successfully read %s header items", headerItems.size()));
        extensions = configuration.getExtensions();
        userCols = configuration.getUserCols();

        getDirectoryPathConfiguration().validatePathsExist();

        JsonReaderKt.readHyperParameterJson();

        log.debug("Successfully read master configuration");

        /* Start worker thread */
        Worker.getInstance().start();
    }

    public List<HeaderItem> getHeaderItems() {
        return headerItems;
    }

    public ModelConfiguration getModelConfiguration() {
        if (modelConfiguration == null) {
            modelProperties = readClass(ModelProperties.class, "modelConfig");
            modelConfiguration = new ModelConfiguration(modelProperties);
        }
        return modelConfiguration;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public List<String> getUserCols() {
        return userCols;
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

    public DirectoryPathConfiguration getDirectoryPathConfiguration() {
        if (directoryPathConfiguration == null) {
            directoryPathConfiguration = readClass(DirectoryPathConfiguration.class, "paths");
        }
        return directoryPathConfiguration;
    }

    public CSVConfiguration getCSVConfiguration() {
        if (csvConfiguration == null) {
            csvConfiguration = readClass(CSVConfiguration.class, "csvConfig");
        }
        return csvConfiguration;
    }

    public PageConfiguration getPageConfiguration() {
        if (pageConfiguration == null) {
            pageConfiguration = readClass(PageConfiguration.class, "pageConfig");
        }
        log.debug(String.format("Successfully retrieved %s page configurations", pageConfiguration.getPages().size()));
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
            throw new NirdizatiRuntimeException(e);
        }

        NodeList node = doc.getElementsByTagName(nodeName);

        try {
            jaxbContext = JAXBContext.newInstance(clazz);
            unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(node.item(0), clazz).getValue();
        } catch (JAXBException e) {
            throw new NirdizatiRuntimeException("Failed to read directories", e);
        }
    }

    public static void main(String[] args) {
        MasterConfiguration.getInstance().getModelConfiguration();
    }
}
