package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({Point.class, LineString.class, LinearRing.class, Polygon.class, MultiGeometry.class})
public abstract class Geometry extends KmlObject {

    protected Geometry() {
    }
}
