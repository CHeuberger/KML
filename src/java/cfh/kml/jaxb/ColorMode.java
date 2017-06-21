package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum ColorMode {

    @XmlEnumValue("normal") NORMAL,
    @XmlEnumValue("random") RANDOM;
}
