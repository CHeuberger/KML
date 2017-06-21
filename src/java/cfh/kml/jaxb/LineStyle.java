package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlElement;

public class LineStyle extends ColorStyle {

    @XmlElement
    private Float width;
    
    public LineStyle() {
    }
    
    public LineStyle setWidth(float width) {
        if (width < 0) throw new IllegalArgumentException(String.valueOf(width));
        this.width = Float.valueOf(width);
        return this;
    }
    
    public float getWidth() {
        return width;
    }
}
