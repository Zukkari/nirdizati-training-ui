package cs.ut.config;


import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.List;

@XmlRootElement(name = "pageConfig")
public class PageConfigurationProvider {
    private static final Logger log = Logger.getLogger(PageConfigurationProvider.class);

    private List<Page> pages;

    @XmlElement(name = "page")
    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public void readPages() throws JAXBException {
        log.debug("Started reading pages from configuration...");
        File file = new File(getClass().getClassLoader().getResource("configuration.xml").getFile());
        JAXBContext jaxbContext = JAXBContext.newInstance(PageConfigurationProvider.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        PageConfigurationProvider pagesProvider = (PageConfigurationProvider) unmarshaller.unmarshal(file);
        this.pages = pagesProvider.getPages();
        log.debug(String.format("Successfully read %s pages from configuration", pages.size()));
    }

    public Page getByPageName(final String name) {
        return pages.stream().filter(it -> it.getId().equals(name)).findFirst().get();
    }
}
