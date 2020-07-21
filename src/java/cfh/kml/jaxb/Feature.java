package cfh.kml.jaxb;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({Placemark.class, Container.class})
public abstract class Feature extends KmlObject {

    @XmlElement
    private String name;
    
    @XmlElement
    private BooleanType open;
    
    @XmlElement
    private BooleanType visibility;
    
    @XmlElement
    private String description;
    
    @XmlElement
    private URI styleUrl;
    
    @XmlElementRef
    private StyleSelector styleSelector;
    
    @XmlElement
    private String snippet;
    
    protected Feature() {
    }

    public String getName() {
        return name;
    }

    public Feature setName(String name) {
        if (name == null) throw new IllegalArgumentException("null");
        this.name = name;
        return this;
    }
    
    public boolean isOPen() {
        return open != null && open.isTrue();
    }
    
    public void setOpen(boolean open) {
        this.open = new BooleanType(open);
    }
    
    public boolean isVisibility() {
        return visibility == null || visibility.isTrue();
    }
    
    public Feature setVisibility(boolean visibility) {
        this.visibility = new BooleanType(visibility);
        return this;
    }
    
    public String getDescription() {
        return description;
    }

    public Feature setDescription(String description) {
        if (description == null) throw new IllegalArgumentException("null");
        this.description = description;
        return this;
    }
    
    public URI getStyleUrl() {
        return styleUrl;
    }
    
    public Feature setStyleUrl(URI styleUrl) {
        if (styleUrl == null) throw new IllegalArgumentException("null");
        this.styleUrl = styleUrl;
        return this;
    }
    
    public StyleSelector getStyleSelector() {
        return styleSelector;
    }
    
    public Feature setStyleSelector(StyleSelector styleSelector) {
        if (styleSelector == null) throw new IllegalArgumentException("null");
        this.styleSelector = styleSelector;
        return this;
    }
    
    public String getSnippet() {
        return snippet;
    }
    
    public Feature setSnippet(String snippet) {
        this.snippet = snippet;
        return this;
    }
}
