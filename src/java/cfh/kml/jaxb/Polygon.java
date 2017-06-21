package cfh.kml.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Polygon")
public class Polygon extends Geometry {
    
    @XmlElement
    private BooleanType extrude;
    
    @XmlElement
    private BooleanType tessellate;
    
    @XmlElement
    private AltitudeMode altitudeMode;
    
    @XmlElement()
    private BoundaryType outerBoundaryIs;
    
    @XmlElement()
    private final List<BoundaryType> innerBoundaryIs = new ArrayList<BoundaryType>();
    
    public Polygon() {
    }

    public Polygon setExtrude(boolean extrude) {
        this.extrude = new BooleanType(extrude);
        return this;
    }
    
    public boolean isExtrude() {
        return extrude != null && extrude.isTrue();
    }
    
    public Polygon setTessellate(boolean tessellate) {
        this.tessellate = new BooleanType(tessellate);
        return this;
    }
    
    public boolean isTessellate() {
        return tessellate != null && tessellate.isTrue();
    }

    public AltitudeMode getAltitudeMode() {
        return altitudeMode;
    }

    public Polygon setAltitudeMode(AltitudeMode altitudeMode) {
        if (altitudeMode == null) throw new IllegalArgumentException("null");
        this.altitudeMode = altitudeMode;
        return this;
    }
    
    public LinearRing getOuterBoundaryIs() {
        return outerBoundaryIs.getLinearRing();
    }
    
    public Polygon setOuterBoundaryIs(LinearRing outerBoundary) {
        if (outerBoundary == null) throw new IllegalArgumentException("null");
        this.outerBoundaryIs = new BoundaryType(outerBoundary);
        return this;
    }
    
    public Polygon addInnerBoundaryIs(LinearRing innerBoundary) {
        if (innerBoundary == null) throw new IllegalArgumentException("null");
        this.innerBoundaryIs.add(new BoundaryType(innerBoundary));
        return this;
    }
}
