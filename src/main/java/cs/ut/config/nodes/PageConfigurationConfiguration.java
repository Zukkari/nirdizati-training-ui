package cs.ut.config.nodes;


import cs.ut.config.items.Page;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Service class that provider Page objects
 */
public class PageConfigurationConfiguration {
    private List<Page> pages;

    @XmlElement(name = "page")
    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    /**
     * Finds required Page object by name if it exists, throws NullPointer exception if it could not be found
     * @param name page identified that is defined in configuration.xml
     * @return Page object that contains id and uri that was configured in configuration.xml
     */
    public Page getByPageName(final String name) {
        return pages.stream().filter(it -> it.getId().equals(name)).findFirst().get();
    }
}
