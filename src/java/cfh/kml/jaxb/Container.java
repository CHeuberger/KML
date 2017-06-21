package cfh.kml.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({Document.class, Folder.class})
public abstract class Container extends Feature {

    @XmlElementRef
    private final List<Feature> features = new ArrayList<Feature>();
    
    protected Container() {
    }
    
    public Container add(Feature feature) {
        if (feature == null) throw new IllegalArgumentException("null");
        features.add(feature);
        return this;
    }
    
    public List<Feature> getFeatures() {
        return features;
    }
    
    
    @Override
    public Container setVisibility(boolean visibility) {
        super.setVisibility(visibility);
        for (Feature feature : features) {
            feature.setVisibility(visibility);
        }
        return this;
    }
}
