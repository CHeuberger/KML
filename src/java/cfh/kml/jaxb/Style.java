package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Style")
public class Style extends StyleSelector {

    // TODO other styles
    
    @XmlElement(name="LineStyle")
    private LineStyle lineStyle;
    
    @XmlElement(name="PolyStyle")
    private PolyStyle polyStyle;
    
    public Style() {
    }

    public Style(String id) {
        setId(id);
    }
    
    public Style setLineStyle(LineStyle lineStyle) {
        if (lineStyle == null) throw new IllegalArgumentException("null");
        this.lineStyle = lineStyle;
        return this;
    }
    
    public LineStyle getLineStyle() {
        return lineStyle;
    }
    
    public Style setPolyStyle(PolyStyle polyStyle) {
        if (polyStyle == null) throw new IllegalArgumentException("null");
        this.polyStyle = polyStyle;
        return this;
    }
    
    public PolyStyle getPolyStyle() {
        return polyStyle;
    }
}
