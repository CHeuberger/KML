package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlElement;

public class BoundaryType {

    @XmlElement(name="LinearRing")
    private LinearRing linearRing;
    
    @SuppressWarnings("unused")
    private BoundaryType() {
    }
    
    BoundaryType(LinearRing ring) {
        if (ring == null) throw new IllegalArgumentException("null");
        linearRing = ring;
    }
    
    public LinearRing getLinearRing() {
        return linearRing;
    }
}
