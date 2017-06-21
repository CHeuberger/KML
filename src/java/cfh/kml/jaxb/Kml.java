package cfh.kml.jaxb;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

@XmlRootElement
public class Kml {

    @XmlAttribute
    public final static String xmlns="http://www.opengis.net/kml/2.2";
    
    @XmlElementRef
    private final List<KmlObject> objects = new ArrayList<KmlObject>();

    public Kml() {
    }
    
    public Kml add(KmlObject object) {
        if (object == null) throw new IllegalArgumentException("null");
        objects.add(object);
        return this;
    }

    public List<KmlObject> getObjects() {
        return objects;
    }
    
    public void marshalTo(OutputStream output) throws JAXBException {
        marshalTo("", output);
    }
    
    public void marshalTo(String namespace, OutputStream output) throws JAXBException {
        QName qName = new QName(namespace, "kml");
        JAXBElement<Kml> root = new JAXBElement<Kml>(qName, Kml.class, this);
        JAXBContext context = JAXBContext.newInstance(Kml.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(root, output);
    }
}
