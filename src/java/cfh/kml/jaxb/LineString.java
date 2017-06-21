package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import cfh.kml.jaxb.adapters.CoordinatesAdapter;

@XmlRootElement(name="LineString")
public class LineString extends Geometry {
    
    @XmlElement
    private BooleanType extrude;
    
    @XmlElement
    private BooleanType tessellate;
    
    @XmlElement
    private AltitudeMode altitudeMode;
    
    @XmlJavaTypeAdapter(CoordinatesAdapter.class)
    private final Coordinates coordinates = new Coordinates();

    public LineString() {
    }

    public LineString setExtrude(boolean extrude) {
        this.extrude = new BooleanType(extrude);
        return this;
    }
    
    public boolean isExtrude() {
        return extrude != null && extrude.isTrue();
    }
    
    public LineString setTessellate(boolean tessellate) {
        this.tessellate = new BooleanType(tessellate);
        return this;
    }
    
    public boolean isTessellate() {
        return tessellate != null && tessellate.isTrue();
    }

    public AltitudeMode getAltitudeMode() {
        return altitudeMode;
    }

    public LineString setAltitudeMode(AltitudeMode altitudeMode) {
        if (altitudeMode == null) throw new IllegalArgumentException("null");
        this.altitudeMode = altitudeMode;
        return this;
    }
    
    public Coordinates getCoordinates() {
        return coordinates;
    }
    
    public LineString add(Coord coord) {
        if (coord == null) throw new IllegalArgumentException("null");
        coordinates.add(coord);
        return this;
    }
    
    public LineString add(double longitude, double latitude) {
        coordinates.add(new Coord(latitude, longitude));
        return this;
    }
    
    public LineString add(double longitude, double latitude, double altitude) {
        coordinates.add(new Coord(latitude, longitude, altitude));
        return this;
    }
    
    public LineString close() {
        coordinates.close();
        return this;
    }
}
