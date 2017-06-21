package cfh.kml.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="MultiGeometry")
public class MultiGeometry extends Geometry {

    @XmlElementRef
    private final List<Geometry> geometries = new ArrayList<Geometry>();
    
    public MultiGeometry() {
    }
    
    public MultiGeometry(Geometry... geometries) {
        for (Geometry geometry : geometries) {
            add(geometry);
        }
    }
    
    public MultiGeometry add(Geometry geometry) {
        if (geometry == null) throw new IllegalArgumentException("null");
        this.geometries.add(geometry);
        return this;
    }
}
