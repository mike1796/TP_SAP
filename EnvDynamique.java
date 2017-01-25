import java.util.*;


/**
 * Dynamic environment (for executing the program);
 */

class EnvDynamique {
    
    /** The association between identifiers and values */
    private Map<Idf, Value> values;

    /** External printable form */
    public String toString() { return values.toString(); }
    
    /** Constructor of an empty environment */
    EnvDynamique() { values  = new HashMap<Idf, Value>(); }

    /** Constructor from a static environment 
     * @param envStat: a static environment (should be a singleton)
     * @return an elementary dynamic environment in which all the
     *  variables declared in envStat are associated with the
     *  "undefined" value
     */
    EnvDynamique(EnvStatique envStat) {
        values  = new HashMap<Idf, Value>();        
        Iterator<Idf> i = envStat.getIdfSet().iterator();
        while(i.hasNext()) {
            values.put(i.next(), new Value());
        }
    }
    
    /** Set the value of an identifier 
     * @param i: the identifier 
     * @param v: the new value 
     */
    public void set(Idf i, Value v) {
        values.put(i, v);
    }

    /** get the value of an identifier
     * @param i the identifier 
     * @return the value
     */
    public Value get(Idf i) {
        return values.get(i);
    }


}

/**
 * Values for variables of type integer or boolean; the value can be
 * "non init"
 */
class Value {

    /** distinction between integer, boolean, non-init */
    private enum Kind { kindINT, kindBOOL, kindUNINIT };

    /** the kind */
    private Kind kind ; 

    /** the value when it is an integer */
    private int valint;

    /** the value when it is a boolean */
    private boolean valbool;
    
    /** tester for integers */
    public boolean isInt() { return kind == Kind.kindINT; }

    /** tester for boolean values */
    public boolean isBool() { return kind == Kind.kindBOOL; }

    /** tester for un-initialized values */
    public boolean isUninit() { return kind == Kind.kindUNINIT; }

    /** get the integer value */
    public int intValue() { 
        if (kind != Kind.kindINT) 
            throw new InternalException ("The value is not an integer");
        return valint; 
    }

    /** get the boolean value */
    public boolean boolValue() { 
        if (kind != Kind.kindBOOL) 
            throw new InternalException ("The value is not a boolean");
        return valbool; 
    }

    /** printable external form */
    public String toString () {
        switch(kind) {
        case kindUNINIT: return "(non intialized)";
        case kindINT: return valint+"";
        case kindBOOL: return valbool+"";
        }
        return "";
    }

    /** equality test */
    public boolean equals(Object other) {
        return 
            (other != null) &&
            (other instanceof Value) &&
            (((Value)other).kind == kind) &&
            (kind == Kind.kindINT  && ((Value)other).valint == valint ||
             kind == Kind.kindBOOL && ((Value)other).valbool == valbool);
    }

    /** constructor of an uninitialized value */
    Value() { kind = Kind.kindUNINIT; }
    
    /** constructor of an integer value 
     *     @param v: the value
     */
    Value(int v) {
        kind = Kind.kindINT;
        valint = v;
    }

    /** constructor of a boolean value 
     *     @param b: the value
     */
    Value(boolean b) {
        kind = Kind.kindBOOL;
        valbool = b;
    }
}
