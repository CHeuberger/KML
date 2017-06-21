package cfh.kml.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Folder")
public class Folder extends Container {

    @SuppressWarnings("unused")
    private Folder() {
    }
    
    public Folder(String name) {
        setName(name);
    }
}
