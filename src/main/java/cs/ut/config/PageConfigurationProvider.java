package cs.ut.config;


import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class PageConfigurationProvider {
    private List<Page> pages;

    @XmlElement(name = "page")
    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public Page getByPageName(final String name) {
        return pages.stream().filter(it -> it.getId().equals(name)).findFirst().get();
    }
}
