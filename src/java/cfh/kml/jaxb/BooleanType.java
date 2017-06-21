package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlValue;

public class BooleanType {

    @XmlValue
    private final Integer value;
    
    @SuppressWarnings("unused")
    private BooleanType() {
        this.value = 0;
    }
    
    protected BooleanType(boolean value) {
        this.value = Integer.valueOf(value ? 1 : 0);
    }
    
    public boolean isTrue() {
        return value != null && value.intValue() != 0;
    }
}
