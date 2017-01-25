import java.util.*;


/**
 * A vector of signs, as a representation for a value in
 * the Cartesian product of n Sign lattices.
 * Invariant to be maintained: 
 *     if one of the values in the vector is BOT, then all 
 *     the values are BOT.
 */
class SignVector extends AbstractValue {

    /** The vector of signs */
    private Sign vector[]; 

    /** External printable form */
    public String toString() { 
	//if (vector[0] == Sign.BOT) return "BOT";
	String s ="<";
	for (int i = 0; i < vector.length; i++)
	    s = s + vector[i] + ' ';
	s = s + ">";
	return s;
    }
	
    /** Constructor from an array
     * @param v a vector of signs
     */
    public SignVector(Sign v []) {
	vector = new Sign [v.length];
	for (int i = 0; i < v.length; i++)
	    vector[i] = v[i];
    }

    /**
     * Union of this with another sign vector 
     * @param a another sign vector, should be in canonical form(BOT
     * everywhere or nowhere)
     */
    public void union(AbstractValue a) {
	SignVector v =(SignVector)a; 
	for (int i = 0; i < vector.length; i++)
	    vector[i] = SignLattice.union(vector[i], v.vector[i]);
    } 
    
    /**
     * equality for sign vectors
     * @param other another sign vector to be compared to this
     * equals works on the canonical form:
     * BOT is assumed to be nowhere or everywhere
     */
    public boolean equals(Object other) {
	//System.out.println("SignVector.equals appele");
	if (!(other instanceof  SignVector)) return false;
	SignVector othervector=(SignVector)other;
	for (int i = 0; i < vector.length; i++)
	    if (vector[i] != othervector.vector[i])
		return false;
	return true;
    }

    /** 
     * Constructor from an int that gives the size; vector is
     * initialized to Bottom
     * @param n the size 
     */
    public SignVector(int n) {
	vector = new Sign[n];
	// initial value is canonical
	for (int i=0; i< vector.length; i++)
	    vector[i] = Sign.BOT;
    }

    /** 
     * Constructor from an int that gives the size, and a sign to be
     * used as initial value
     * @param n the size
     * @param s the sign to be used as initial value 
     */
    public SignVector(int n, Sign s) {
	vector = new Sign[n];
	// initial value is canonical
	for (int i=0; i< vector.length; i++)
	    vector[i] = s;     ;
    }

    /**
     * get the sign at rank 
     * @param i the rank in the vector 
     * @return a sign
     */
    public Sign get(int i) {
 	return vector[i] ;
    }

    /**
     * Build a new vector from this, by changing only one sign,
     * according to the effect of an assignment x:= expr(of sign s)
     * @param i the rank of the variable x whose sign is modified
     * @param a the new sign of this variable 
     * @return the new sign vector, in canonical form
     */
    public SignVector copyChangeAssign(int i, AbstractValue a) {
	SignVector v =(SignVector)a;
	Sign s = v.vector[i];

	// trace -----------------------------------
        System.out.println("copyChangeAssign" + this.toString() 
			   + " " + i + " " +  s);
	// end trace

	Sign copy [] = new Sign [vector.length]; 
	
	if (s == Sign.BOT)
	    for (int j=0; j< vector.length; j++)  copy[j] = Sign.BOT;
	else {
	    // COPY FIRST 
	    for (int j=0; j< vector.length; j++) 
		copy[j] = vector[j];
	    // THEN 
	    if (vector[0] == Sign.BOT) {
		// NO MORE CHANGE, LEAVE bot EVERYWHERE 
	    }
	    else {
		copy[i] = s; 
	    }
	}
	
	System.out.println("returns " + new SignVector(copy));
	return new SignVector(copy);
    }
    
    /**
     * Build a new vector from this, by changing two signs at a time,
     * according to the effect of a condition x # y 
     * @param i j ranks  of the two variables whose signs are changed
     * @param op the operator(either < or >)   
     * @param neg true if simple expr x#y, false if expression !(x#y)
     * @return a new canonical sign vector in which the information 
     *  build from 'vector' and 'i op j' and neg is written.
     */
    public SignVector copyChangeCond(int i, int j, Operator op, boolean neg) {
		
	// trace ------------------------------------------------
	System.out.println("copyChangeCond " + this.toString() + i + op + j 
			   + neg);
	// end trace --------------------------------------------
	Sign copy [] = new Sign [vector.length];
	Sign lhsign = vector[i];
	Sign rhsign = vector[j];
	SignPair sp;
	if (op.theOp == OperatorKind.INF) {
	    if (neg) {
		System.out.println("copyChangeCond LT");
		sp = SignLattice.lt(lhsign, rhsign);
		System.out.println(sp);
	    }
	    else {
		System.out.println("copyChangeCond GEQ");
		sp = SignLattice.geq(lhsign, rhsign);
		System.out.println(sp);
	    }
	}
	else {
	    // the, necessarily LgConstants.SUP 
	    if (neg) {
		sp = SignLattice.gt(lhsign, rhsign);
	    }
	    else {
		sp = SignLattice.leq(lhsign, rhsign);
	    }
	}
		
	if ((sp.o1 == Sign.BOT) || (sp.o2 == Sign.BOT)) {
	    for (int k=0; k< vector.length; k++)  copy[k] = Sign.BOT;
	}
	else {
	    for (int k=0; k< vector.length; k++) {
		copy[k] = vector[k];
	    }
	    copy[i] = sp.o1;
	    copy[j] = sp.o2;
	}
	return new SignVector(copy);
    }
    
    public boolean isBottom() {
	return vector[0] == Sign.BOT;
    }

}


/**
 * The values in the Sign lattice for one numerical variable.
 */
enum Sign { BOT, TOP, ZERO, NZERO, LT, LEQ, GT, GEQ };


/**
 * A pair of signs
 */
class SignPair {

    /** First element of the pair */
    Sign o1;

    /** Second element of the pair */
    Sign o2;

    /** constructor from two signs */
    public SignPair(Sign o1, Sign o2) { this.o1 = o1; this.o2 = o2; }
	
    private static boolean same(Sign o1, Sign o2) {
	return o1 == null ? o2 == null : o1.equals(o2);
    }
	
    /** Get first element */
    Sign getFirst()  { return o1; }

    /** Get second element */
    Sign getSecond() { return o2; }
	
    /** Set first element 
     * @param o new sign for the first element of the pair 
     */
    void setFirst(Sign o)  { o1 = o; }
	
    /** Set second element 
     * @param o new sign for the second element of the pair 
     */
    void setSecond(Sign o) { o2 = o; }
	
    public boolean equals(Object obj) {
	if ( !(obj instanceof SignPair))
	    return false;
	SignPair p =(SignPair)obj;
	return same(p.o1, this.o1) && same(p.o2, this.o2);
    }
	
    public String toString() {
	return "{"+o1+", "+o2+"}";
    }
}


/**
 * A collection of functions for computing in the sign lattice for
 * one numerical variable
 */
class SignLattice {

    static Sign  union(Sign s1, Sign s2) {
	// trace
	// System.out.println(s1 + "   " + s2);
	// ----------------------------------
	switch(s1) {
	case TOP : return  Sign.TOP; 
	case BOT : return  s2;
	case ZERO: 
	    switch(s2) {
	    case ZERO : case LEQ: case GEQ: case TOP: return s2;
	    case BOT  : return Sign.ZERO;
	    case LT   : return Sign.LEQ;
	    case GT   : return Sign.GEQ;
	    case NZERO: return Sign.TOP;
	    }
	case NZERO:
	    switch(s2) {
	    case NZERO: case LT:  case GT: return Sign.NZERO;
	    case ZERO : case GEQ: case LEQ: case TOP: return Sign.TOP;
	    case BOT  : return Sign.NZERO; 
	    }
	case LT:
	    // ========================
	    // A detailler ...
	    return Sign.TOP;
	    // ========================
	case GT:
	    switch(s2) {
	    case BOT  : case GT: return Sign.GT;
	    case NZERO: case  GEQ: return s2;
	    case ZERO : return Sign.GEQ;
	    case LT   : return Sign.NZERO;
	    case LEQ  : case TOP: return Sign.TOP;
	    }
	case LEQ:
	    switch(s2) {
	    case BOT: case ZERO: case LEQ: case LT: return Sign.LEQ;
	    case GEQ: case GT  : case TOP: case NZERO: return Sign.TOP;
	    }
	case GEQ:
	    switch(s2) {
	    case BOT: case  ZERO: case  GEQ: case  GT: return  Sign.GEQ;
	    case LEQ: case  LT  : case  TOP: case  NZERO: return Sign.TOP;
	    }
	}
	// should not be there!
	throw new RuntimeException("Should not be there!");
	//return Sign.TOP;
    }

    /** Table for + */
    static Sign [][] table_plus = { 
	{  Sign.BOT,       Sign.BOT,      Sign.BOT,       Sign.BOT,      Sign.BOT,      Sign.BOT,      Sign.BOT,      Sign.BOT},
	{  Sign.BOT,       Sign.TOP,      Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP},
	{  Sign.BOT,       Sign.TOP,      Sign.ZERO,      Sign.NZERO,    Sign.LT,       Sign.LEQ,      Sign.GT,       Sign.GEQ},
	{  Sign.BOT,       Sign.TOP,      Sign.NZERO,     Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP},
	{  Sign.BOT,       Sign.TOP,      Sign.LT,        Sign.TOP,      Sign.LT,       Sign.LT,       Sign.TOP,      Sign.TOP},
	{  Sign.BOT,       Sign.TOP,      Sign.LEQ,       Sign.TOP,      Sign.LT,       Sign.LEQ,      Sign.TOP,      Sign.TOP},
	{  Sign.BOT,       Sign.TOP,      Sign.GT,        Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.GT,       Sign.GT },
	{  Sign.BOT,       Sign.TOP,      Sign.GEQ,       Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.GT,       Sign.GEQ} };

    /** Table for -. 
     * Accesses: table_minus [column][line]  is the sign of line-column
     */

    static Sign [][] table_minus = { 
	{  Sign.BOT,       Sign.BOT,      Sign.BOT,       Sign.BOT,      Sign.BOT,      Sign.BOT,      Sign.BOT,      Sign.BOT},
	{  Sign.BOT,       Sign.TOP,      Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP},
	{  Sign.BOT,       Sign.TOP,      Sign.ZERO,      Sign.NZERO,    Sign.LT,       Sign.LEQ,      Sign.GT,       Sign.GEQ},
	{  Sign.BOT,       Sign.TOP,      Sign.NZERO,     Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP},
	{  Sign.BOT,       Sign.TOP,      Sign.GT,        Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.GT,       Sign.GT},
	{  Sign.BOT,       Sign.TOP,      Sign.GEQ,       Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.GT,       Sign.GEQ},
	{  Sign.BOT,       Sign.TOP,      Sign.LT,        Sign.TOP,      Sign.LT,       Sign.LT,       Sign.TOP,      Sign.TOP},
	{  Sign.BOT,       Sign.TOP,      Sign.LEQ,       Sign.TOP,      Sign.LT,       Sign.LEQ,      Sign.TOP,      Sign.TOP} };


    /** Table for * */
    static Sign [][] table_mult = { 
	// =========================================
	// A detailler ....
	{  Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP},
	{  Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP},
	{  Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,     Sign.TOP,     Sign.TOP,     Sign.TOP,     Sign.TOP},
	{  Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,    Sign.TOP,    Sign.TOP,      Sign.TOP,    Sign.TOP},
	{  Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,    Sign.TOP,       Sign.TOP,      Sign.TOP,       Sign.TOP},
	{  Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP},
	{  Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,    Sign.TOP,       Sign.TOP,      Sign.TOP,       Sign.TOP},
	{  Sign.TOP,       Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP,      Sign.TOP} 
	// ==========================================
    };


    /** table for the condition >
     * table_cond_gt [s1][s2] = <s1', s2'> such that:
     *  if x has sign s1, y has sign s2 and a transition x>y is taken, 
     * then after that, x has sign s'1, y has sign s'2
     * Example:  table_cond_gt [GEQ][GT] = < GT, GT > 
     */
    static SignPair [][] table_cond_gt = {
	{ new SignPair(Sign.BOT , Sign.BOT ),   new SignPair(Sign.BOT , Sign.TOP ),  new SignPair(Sign.BOT , Sign.ZERO ), new SignPair(Sign.BOT , Sign.NZERO ),  new SignPair(Sign.BOT , Sign.LT ),  new SignPair(Sign.BOT, Sign. LEQ),   new SignPair(Sign.BOT , Sign.GT ),  new SignPair(Sign.BOT , Sign. GEQ)},
	{ new SignPair(Sign.TOP , Sign.BOT ),   new SignPair(Sign.TOP , Sign.TOP ),  new SignPair(Sign.GT , Sign.ZERO ),  new SignPair(Sign.TOP , Sign.NZERO ),  new SignPair(Sign.TOP , Sign.LT),   new SignPair(Sign.TOP , Sign.LEQ ),  new SignPair(Sign.GT , Sign.GT ),   new SignPair(Sign.GEQ , Sign.GEQ )},
	{ new SignPair(Sign.ZERO , Sign.BOT ),  new SignPair(Sign.ZERO , Sign.LT ),  new SignPair(Sign.BOT , Sign.BOT ),  new SignPair(Sign.ZERO , Sign.LT),     new SignPair(Sign.ZERO , Sign.LT ), new SignPair(Sign.ZERO , Sign.LT ),  new SignPair(Sign.BOT , Sign.BOT ), new SignPair(Sign.BOT , Sign.BOT )},
	{ new SignPair(Sign.NZERO , Sign.BOT ), new SignPair(Sign.NZERO , Sign.TOP ),new SignPair(Sign.GT , Sign.ZERO ),  new SignPair(Sign.NZERO , Sign.NZERO ),new SignPair(Sign.NZERO , Sign.LT ),new SignPair(Sign.NZERO , Sign.LEQ ),new SignPair(Sign.GT , Sign.GT ),   new SignPair(Sign.GEQ ,Sign.GEQ )},
	{ new SignPair(Sign.LT , Sign.BOT ),    new SignPair(Sign.LT, Sign.LT ),     new SignPair(Sign.BOT , Sign.BOT ),  new SignPair(Sign.LT , Sign.LT ),      new SignPair(Sign.LT , Sign.LT ),   new SignPair(Sign.LT , Sign.LT ),    new SignPair(Sign.BOT , Sign.BOT ), new SignPair(Sign.BOT , Sign.BOT )},
	{ new SignPair(Sign.LEQ , Sign.BOT ),   new SignPair(Sign.LEQ , Sign.LT ),  new SignPair(Sign.ZERO , Sign.ZERO ),  new SignPair(Sign.LEQ , Sign.LT ),     new SignPair(Sign.LEQ , Sign.LT ),  new SignPair(Sign.LEQ , Sign.LT ),  new SignPair(Sign.BOT , Sign.BOT ), new SignPair(Sign.BOT, Sign.BOT )},
	{ new SignPair(Sign.GT, Sign.BOT ),     new SignPair(Sign.GT , Sign.TOP ),   new SignPair(Sign.GT , Sign.ZERO ),  new SignPair(Sign.GT , Sign.NZERO ),   new SignPair(Sign.GT , Sign.LT ),   new SignPair(Sign.GT , Sign. LEQ),   new SignPair(Sign.GT , Sign.GT ),   new SignPair(Sign.GT , Sign.GEQ )},
	// =====================
	// A detailler ... 
	// table_cond_gt [GEQ][s] = < ..., ...> 
	// avec l'ordre BOT, TOP, ZERO, NZERO, LT, LEQ, GT, GEQ pour s
	{ new SignPair(Sign.TOP , Sign.TOP ),   new SignPair(Sign.TOP, Sign.TOP ),  new SignPair(Sign.TOP , Sign.TOP ),  new SignPair(Sign.TOP, Sign.TOP ),  new SignPair(Sign.TOP, Sign.TOP ),   new SignPair(Sign.TOP , Sign.TOP ),  new SignPair(Sign.TOP , Sign.TOP ),   new SignPair(Sign.TOP , Sign.TOP )}	
	// =====================
    };

    /** Table for condition >=
     * table_cond_geq [s1][s2] = <s1', s2'> such that:
     * if x has sign s1, y has sign s2 and a transition x >= y is taken, 
     * then after that, x has sign s'1, y has sign s'2
     */
    static SignPair [][] table_cond_geq = {
	{ new SignPair(Sign.BOT , Sign.BOT ),   new SignPair(Sign.BOT , Sign.TOP ),  new SignPair(Sign.BOT , Sign.ZERO ), new SignPair(Sign.BOT , Sign.NZERO ),  new SignPair(Sign.BOT , Sign.LT ),  new SignPair(Sign.BOT, Sign. LEQ),   new SignPair(Sign.BOT , Sign.GT ),  new SignPair(Sign.BOT , Sign. GEQ)},
	{ new SignPair(Sign.TOP , Sign.BOT ),   new SignPair(Sign.TOP , Sign.TOP ),  new SignPair(Sign.GEQ, Sign.ZERO ),  new SignPair(Sign.TOP , Sign.NZERO ),  new SignPair(Sign.TOP , Sign.LT),   new SignPair(Sign.TOP , Sign.LEQ ),  new SignPair(Sign.GT , Sign.GT ),   new SignPair(Sign.GEQ , Sign.GEQ )},
	{ new SignPair(Sign.ZERO , Sign.BOT ),  new SignPair(Sign.ZERO , Sign.LEQ),  new SignPair(Sign.ZERO, Sign.ZERO),  new SignPair(Sign.ZERO , Sign.LEQ),    new SignPair(Sign.ZERO , Sign.LT ), new SignPair(Sign.ZERO , Sign.LEQ),  new SignPair(Sign.BOT , Sign.BOT ), new SignPair(Sign.ZERO, Sign.ZERO)},
	{ new SignPair(Sign.NZERO , Sign.BOT ), new SignPair(Sign.NZERO , Sign.TOP ),new SignPair(Sign.GEQ, Sign.ZERO ),  new SignPair(Sign.NZERO , Sign.NZERO ),new SignPair(Sign.NZERO , Sign.LT ),new SignPair(Sign.NZERO , Sign.LEQ ),new SignPair(Sign.NZERO , Sign.GT ),new SignPair(Sign.NZERO ,Sign.GEQ )},
	{ new SignPair(Sign.LT , Sign.BOT ),    new SignPair(Sign.LT, Sign.LT ),     new SignPair(Sign.BOT , Sign.BOT ),  new SignPair(Sign.LT , Sign.LT ),      new SignPair(Sign.LT , Sign.LT ),   new SignPair(Sign.LT , Sign.LT ),    new SignPair(Sign.BOT , Sign.BOT ), new SignPair(Sign.BOT , Sign.BOT )},
	{ new SignPair(Sign.LEQ , Sign.BOT ),   new SignPair(Sign.LEQ , Sign.LEQ ),  new SignPair(Sign.ZERO, Sign.ZERO),  new SignPair(Sign.LEQ , Sign.LT ),     new SignPair(Sign.LEQ , Sign.LT ),  new SignPair(Sign.LEQ , Sign.LEQ ),  new SignPair(Sign.BOT , Sign.BOT ), new SignPair(Sign.ZERO, Sign.ZERO)},
	{ new SignPair(Sign.GT, Sign.BOT ),     new SignPair(Sign.GT , Sign.TOP ),   new SignPair(Sign.GT, Sign.ZERO ),   new SignPair(Sign.GT , Sign.NZERO ),   new SignPair(Sign.GT , Sign.LT ),   new SignPair(Sign.GT , Sign. LEQ),   new SignPair(Sign.GT , Sign.GT ),   new SignPair(Sign.GT , Sign.GEQ )},
	{ new SignPair(Sign.GEQ , Sign.BOT ),   new SignPair(Sign.GEQ , Sign.TOP ),  new SignPair(Sign.GEQ, Sign.ZERO ),  new SignPair(Sign.GEQ , Sign.NZERO ),  new SignPair(Sign.GEQ, Sign.LT ),   new SignPair(Sign.GEQ , Sign.LEQ ),  new SignPair(Sign.GT , Sign.GT ),   new SignPair(Sign.GEQ , Sign.GEQ )}		
    };
    
    /** 
     * Abstract function + for signs 
     */
    static Sign plus(Sign s1, Sign s2) {
	// Accesses: [line][column]
	return SignLattice.table_plus [s1.ordinal()][s2.ordinal()];
    }

    /** 
     * Abstract function - for signs 
     */
    static Sign minus(Sign s1, Sign s2) {
	// Accesses: [column][line]
	return SignLattice.table_minus [s2.ordinal()][s1.ordinal()];
    }

    /** 
     * Abstract function * for signs 
     */
    static Sign mult(Sign s1, Sign s2) {
	// Accesses: [line][column]
	return SignLattice.table_mult [s1.ordinal()][s2.ordinal()];
    }

    static SignPair gt(Sign s1, Sign s2) {
	return SignLattice.table_cond_gt  [s1.ordinal()][s2.ordinal()];
    }
    static SignPair lt(Sign s1, Sign s2) {
	// the table for lt is the transpose of gt
	// use the same table, inverting the indices
	SignPair sp = SignLattice.table_cond_gt  [s2.ordinal()][s1.ordinal()]; 
	// then invert the elements of the result pair 
	return new SignPair(sp.o2, sp.o1);	
    }

    static SignPair geq(Sign s1, Sign s2) {
	return SignLattice.table_cond_geq  [s1.ordinal()][s2.ordinal()];
    }

    static SignPair leq(Sign s1, Sign s2) {
	// the table for leq is the transpose of geq
	// use the same table, inverting the indices
	SignPair sp = SignLattice.table_cond_geq  [s2.ordinal()][s1.ordinal()];
	// then invert the elements of the result pair 
	return new SignPair(sp.o2, sp.o1);	
    }

    static Sign signOfConst(int k) {
	// ================
	// A detailler 
	return Sign.TOP;
	// ================
    }

    static Sign P(Sign i) {
	// for the moment, we don't know how to take the polynomial into account. 
	// A detailler 
	return Sign.TOP;
 	// ================
    }


    public static void main(String args[]) {
	System.out.println("Donnez deux signes(0 BOT, 1 TOP, 2 ZERO,"
			   + "3 NZERO, 4 LT, 5 LEQ, 6 GT, 7 GEQ): ");
	Scanner in = new Scanner(System.in);
	int i1 = in.nextInt();
	int i2 = in.nextInt();
	System.out.println("sign_x = " + Sign.values()[i1] + ", sign_y = " 
			   +  Sign.values()[i2]);
	System.out.println("Signe de la somme = " 
			   + SignLattice.plus(Sign.values()[i1], 
					      Sign.values()[i2]));
	System.out.println("Signes apres transition x > y: "  
			   + SignLattice.gt (Sign.values()[i1], 
					     Sign.values()[i2]));
	System.out.println("Signes apres transition x < y: "  
			   + SignLattice.lt (Sign.values()[i1], 
					     Sign.values()[i2]));
	System.out.println("Signes apres transition x >= y: " 
			   + SignLattice.geq(Sign.values()[i1], 
					     Sign.values()[i2]));
	System.out.println("Signes apres transition x <= y: " 
			   + SignLattice.leq(Sign.values()[i1], 
					     Sign.values()[i2]));
    }
}
