package cfh.air;

public enum AirspaceType {
/*
AC  class  ; where class can be: 
  R                       restricted 
  Q                       danger 
  P                       prohibited 
  A                       Class A 
  B                       Class B 
  C                       Class C 
  D                       Class D  
  GP                      glider prohibited  
  CTR                     CTR 
  W                       Wave Window 
  
  TMZ                     TMZ
  RMZ                     RMZ
 */
    
    RESTRICTED("R"),
    DANGER("Q"),
    PROHIBITED("P"),
    CLASS_A("A"),
    CLASS_B("B"),
    CLASS_C("C"),
    CLASS_D("D"),
    CLASS_E("E"),
    CLASS_F("F"),
    CLASS_G("G"),
    GLIDER_PROHIBITED("GP"),
    GLIDER_SECTOR("GSEC"),
    CTR("CTR"),
    WAVE("W"),
    TMZ("TMZ"),
    RMZ("RMZ");
    
    private final String code;
    
    private AirspaceType(String code) {
        assert code != null;
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static AirspaceType getType(String code) {
        for (AirspaceType type : values()) {
            if (type.getCode().equals(code))
                return type;
        }
        return null;
    }
}
