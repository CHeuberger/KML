package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Placemark")
public class Placemark extends Feature {

    @XmlElementRef
    private final Geometry geometry;
    
    @SuppressWarnings("unused")
    private Placemark() {
        geometry = null;
    }
    
    public Placemark(String name, Geometry geometry) {
        this.geometry = geometry;
        setName(name);
    }
    
    public Geometry getGeometry() {
        return geometry;
    }
}
