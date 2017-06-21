package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({Feature.class, Geometry.class, StyleSelector.class, ColorStyle.class})
public abstract class KmlObject {

    @XmlAttribute
    private String id;
    
    protected KmlObject() {
    }
    
    public String getId() {
        return id;
    }

    public KmlObject setId(String id) {
        if (id == null) throw new IllegalArgumentException("null");
        this.id = id;
        return this;
    }
}
