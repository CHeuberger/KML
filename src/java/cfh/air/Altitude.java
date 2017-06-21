package cfh.air;

public class Altitude {

    public enum Type {
        GND,
        MSL,
        FL;
    }
    
    private final Type type;
    private final double value;  // meters

    public Altitude(Type type, double value) {
        if (type == null) throw new IllegalArgumentException("null");
        if (!(value >= 0)) throw new IllegalArgumentException(Double.toString(value));
        
        this.type = type;
        this.value = value;
    }
    
    public Type getType() {
        return type;
    }
    
    public double getValue() {
        return value;
    }
    
    public int getValueFeet() {
        return (int)(value / 0.3048 + 0.5);
    }
    
    public String toDisplayValue() {
        switch (type) {
            case FL:
                return type.toString() + (int)(value / 30.48 + 0.5) + " (" + (int)(value) + "m)";
            case GND:
            case MSL:
                return getValueFeet() + type.toString() + " (" + (int)(value) + "m)";
            default:
                return toString();
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value + "," + type + "]";
    }
}
