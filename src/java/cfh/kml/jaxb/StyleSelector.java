package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({Style.class})
public abstract class StyleSelector extends KmlObject {

    protected StyleSelector() {
    }
}
