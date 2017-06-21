package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum AltitudeMode {

    @XmlEnumValue("clampToGround")    CLAMP,
    @XmlEnumValue("relativeToGround") RELATIVE,
    @XmlEnumValue("absolute")         ABSOLUTE;
}
