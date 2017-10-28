package cs.ut.config.items;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Page bean used to store information about pages and redirect uris in the application
 */

@XmlType(propOrder = {"id", "uri"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Page {

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "uri")
    private String uri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}