package cfh.kml.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Document")
public class Document extends Container {

    @XmlElementRef
    private final List<StyleSelector> styles = new ArrayList<StyleSelector>();
    
    public Document() {
    }
    
    public Document(String name) {
        setName(name);
    }
    
    public Document add(StyleSelector style) {
        if (style == null) throw new IllegalArgumentException("null");
        styles.add(style);
        return this;
    }
    
    public List<StyleSelector> getStyles() {
        return styles;
    }
}
