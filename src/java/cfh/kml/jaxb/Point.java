package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import cfh.kml.jaxb.adapters.CoordinatesAdapter;

@XmlRootElement(name="Point")
public class Point extends Geometry {

    @XmlElement
    private BooleanType extrude;
    
    @XmlElement
    private AltitudeMode altitudeMode;
    
    @XmlJavaTypeAdapter(CoordinatesAdapter.class)
    private Coordinates coordinates;
    
    @SuppressWarnings("unused")
    private Point() {
    }
    
    public Point(Coord coord) {
        if (coord == null) throw new IllegalArgumentException("null");
        this.coordinates = new Coordinates(coord);
    }
    
    public Point(double longitude, double latitude) {
        this.coordinates = new Coordinates(longitude, latitude);
    }
    
    public Point(double longitude, double latitude, double altitude) {
        this.coordinates = new Coordinates(longitude, latitude, altitude);
    }
    
    public Point setExtrude(boolean extrude) {
        this.extrude = new BooleanType(extrude);
        return this;
    }
    
    public boolean isExtrude() {
        return extrude != null && extrude.isTrue();
    }

    public AltitudeMode getAltitudeMode() {
        return altitudeMode;
    }

    public Point setAltitudeMode(AltitudeMode altitudeMode) {
        if (altitudeMode == null) throw new IllegalArgumentException("null");
        this.altitudeMode = altitudeMode;
        return this;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }
}
