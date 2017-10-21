package cs.ut.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Bean that contains information about a single header item
 */

@XmlType(propOrder = {"label", "redirect", "enabled", "position"})
@XmlAccessorType(XmlAccessType.FIELD)
public class HeaderItem {

    @XmlElement(name = "label")
    private String label;

    @XmlElement(name = "redirect")
    private String redirect;

    @XmlElement(name = "position")
    private Integer position;

    @XmlElement(name = "enabled")
    private boolean enabled;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "HeaderItem{" +
                "label='" + label + '\'' +
                ", redirect='" + redirect + '\'' +
                ", position=" + position +
                '}';
    }
}
