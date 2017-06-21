package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlElement;

public class PolyStyle extends ColorStyle {

    @XmlElement
    private BooleanType fill;
    
    @XmlElement
    private BooleanType outline;
    
    public PolyStyle() {
    }
    
    public PolyStyle setFill(boolean fill) {
        this.fill = new BooleanType(fill);
        return this;
    }
    
    public boolean isFill() {
        return fill == null || fill.isTrue();
    }
    
    public PolyStyle setOutline(boolean outline) {
        this.outline = new BooleanType(outline);
        return this;
    }
    
    public boolean isOutline() {
        return outline == null || outline.isTrue();
    }
}
