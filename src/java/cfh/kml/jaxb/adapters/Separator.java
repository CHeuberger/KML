package cfh.kml.jaxb.adapters;

public class Separator {
    private final String separator;
    private boolean first = true;
    
    public Separator(String separator) {
        if (separator == null) throw new IllegalArgumentException();
        this.separator = separator;
    }
    
    @Override
    public String toString() {
        if (first) {
            first = false;
            return "";
        } else {
            return separator;
        }
    }
}