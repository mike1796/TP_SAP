import java.util.*;
import java.io.*;




enum NatureTransition { COND, ASSIGN };


abstract class TransitionLabel { 
    abstract NatureTransition getNature();

    abstract String toDotFile();

    abstract void computeAbstractSigns(InfoAttached infoSource, 
                                       InfoAttached infoTarget,
                                       EnvStatique ES);


}


class CondTransition extends TransitionLabel {
    static CondTransition condTRUE = 
        new CondTransition(ExprNode.buildTrueNode(),true);


    ExprNode theCond;
    NatureTransition getNature() { return NatureTransition.COND; }

    CondTransition(ExprNode c, boolean normal_or_neg) { 
        if (normal_or_neg) 
            theCond = c;
        else 
            theCond = ExprNode.buildNotNode (c);
    }

    public void computeAbstractSigns(InfoAttached infoSource, 
                                     InfoAttached infoTarget,
                                     EnvStatique ES){

        // on doit faire un calcul avec :
        // -- les deux idfs x, y de l'expression theCond(supposee simple), 
        //    leurs signes dans infoSource 
        // -- l'operateur parmi (<, >)
        // On obtient les signes de x, y dans infoTarget 

        ExprNode.SimpleExpr se = theCond.getSimpleExpr();

        if (se == null) {
            // l'expression n'est pas de la forme x#y avec # dans < > 
            // ni de la forme ! (x#y), ni !...! (x#y)
            // on ignore la transition = recopie info source dans target
            infoTarget.accumulateNew(infoSource);
        }
        else {
            // System.out.println ("ICI");
            // ici on doit faire :
            //    new_target := new_target U old_source inter (x # y)
            // se.lhs, se.rhs donnent x, y 
            // se.sign donne l'operateur (< ou > forcement)
            // se.neg dit si c'est nie ou pas. (true = pas nie)
            infoTarget.accumulateNew(infoSource, se.lhs, se.rhs, 
                                     se.sign, se.neg);
        }
    }

 
    public String toString() { 
        return "["+ theCond + "]";
    }

    public String toDotFile() {
        return theCond+"";
    }
}


class AssignTransition extends TransitionLabel {

    AffNode theAssign; 

    NatureTransition getNature() { return NatureTransition.ASSIGN; }

    AssignTransition(AffNode a) { theAssign = a; }

    public String toString() { 
        return "["+ theAssign + "]";
    }

    public String toDotFile() {
        return theAssign+"" ;
    }

    /** Compute the sign of an expression
     * @param expr the expression
     * @return the sign of the expression according to the signs of
     *        the variables that occur in it (according to
     *        theOldSigns)
     * @throws InternalException if it is not an arithmetic expression
     */
    public Sign signOfExpr(EnvStatique ES, InfoAttached info, ExprNode expr) {
        ExprNode fg = expr.fg;
        ExprNode fd = expr.fd;
        Idf idf = expr.idf;
        Operator operator = expr.operator;


        switch(expr.theKind){
        case READ:
            return Sign.TOP;
        case BOOLCONST:   
            throw new InternalException 
                ("This should be an arithmetic expression");
        case INTCONST:    
            return SignLattice.signOfConst(expr.value);
        case IDF:  
            if (ES.getType(idf) != Type.INT)
                throw new InternalException 
                    ("This should be an arithmetic expression " + idf);
            else 
                return ((SignVector)info.getAbstractValue()).get
                    (info.theCG.indexOfIdf(idf));
        case UNARY: 
            // les operateurs unaires, forcement bool (pas de - unaire dans Lg.jj)
            throw new InternalException
                ("This should be an arithmetic expression");
        case BINARY:
            Sign  s1 = signOfExpr(ES, info, fg);
            Sign  s2 = signOfExpr(ES, info, fd);

            switch(operator.theOp) {
            case EGAL:
            case INF:
            case SUP:
            case OR:
            case AND:
                throw new InternalException 
                    ("This should be an arithmetic expression");
            case DIV:
                throw new InternalException 
                    ("This should be a SIMPLE arithmetic expression");
            case MULT:
                return SignLattice.mult(s1, s2);
            case MOINS:
                return SignLattice.minus(s1, s2);
            case PLUS:
                return SignLattice.plus(s1, s2);
            }
        default:
            throw new InternalException("Should not get there!");
        }
    }  

    Sign signOfP(InfoAttached info, Idf x) {
        Sign signOfx = ((SignVector)info.getAbstractValue()).get
            (info.theCG.indexOfIdf(x));
        return SignLattice.P(signOfx);
    }





    public void computeAbstractSigns(InfoAttached infoSource, 
                                     InfoAttached infoTarget,
                                     EnvStatique ES){
        if (ES.getType(theAssign.getIdf()) != Type.INT) {
            System.out.println ("      not integer " + theAssign.getIdf());
            infoTarget.accumulateNew(infoSource);
        }
        else {
            System.out.println ("    integer         " + theAssign.getIdf());
            Sign es;

            if (theAssign instanceof PAffNode) 
                es = signOfP(infoSource, ((PAffNode)theAssign).x);
            else 
                es = signOfExpr(ES, infoSource, theAssign.getExpr());
 
            // mise a jour etat but, etape n
            Idf ii = theAssign.getIdf(); 
            // on envoie un SignVector ou seul le champ [ii] importe
            infoTarget.accumulateNew
                (infoSource, ii,
                 new SignVector(infoSource.theCG.getNumberOfIntIdfs(),es)); 
        }
    }

 

}


class Transition {
    TransitionLabel theLabel;
    ControlPoint    theTarget;

    Transition(TransitionLabel l, ControlPoint c) {
        theLabel = l;
        theTarget = c;
    }

    public String toString() { 
        return "---" + theLabel + "---->" + theTarget;
    }
    public String toDotFile() {
        return theTarget + "[label=\"" + theLabel.toDotFile() + "\"]";
    }

    public void computeAbstractSigns(InfoAttached info, EnvStatique ES) {
        // info is the info attached to the source state 
        theLabel.computeAbstractSigns(info, theTarget.getInfo(), ES); 
    }

 
}


/**
 * A control point in the control graph 
 */
class ControlPoint {
    static int counter = 0;
    String name; 
    InfoAttached info;
    ArrayList<Transition>  theTransitions;
   
    ControlPoint(String subname) {
        theTransitions = new ArrayList<Transition>();
        name = subname+counter;
        counter++;
    }
    
    public InfoAttached getInfo() { return info; }
    
    public String toString() { 
        return 
            "\"" + name + 
            " " + info + 
            "\""; 
    }
    
    public String toDotFile() {
        String st = "";
        Iterator<Transition> i = theTransitions.iterator();
        while (i.hasNext()) {
            st = st + this + "->" + i.next().toDotFile();
            st += "\n";
        }
        return st;        
    }
    
    public String printTransitions() {
        String st = "";
        Iterator<Transition> i = theTransitions.iterator();
        while (i.hasNext()) {
            st = st + i.next();
            st += "\n";
        }
        return st;
    }
    
    void attach(InfoAttached info) { this.info = info;} 
    
    
    void addCondTransition(CondTransition ct, ControlPoint t) {
        theTransitions.add(new Transition(ct, t));
    }
    
    void addAssignTransition(AssignTransition at, ControlPoint t) {
        theTransitions.add(new Transition(at,t));
    }
}


/**
 * The control graph: one entry point, one exit point, potentially one error state 
 * among the set of control points
 */
class ControlGraph {
    List<ControlPoint> theControlPoints;
    ControlPoint entry, exit, error;

    boolean errorIsReachable(){
        return error != null && !error.info.getAbstractValue().isBottom();
    }

    /** Maps an identifier to an index in the vector of signs */
    private Map<Idf,Integer> idf2index;

    public  int indexOfIdf(Idf i) { 
        // System.out.println("-------------- idf = " + i);
        // System.out.println("-------------- idf2index = " + idf2index);
        return idf2index.get(i).intValue(); 
    }
    public  int getNumberOfIntIdfs() { return numberOfIntIdfs; }
    private int numberOfIntIdfs = 0; 

    public void setIdf2Index(EnvStatique theES) {
        // count the variables of type int in the static envt theES
        // and build the correspondence between 
        // identifiers and indices.
        numberOfIntIdfs = 0; 
        idf2index = new HashMap<Idf, Integer>();
        for (Idf ii: theES.getIdfSet()){
            if (theES.getType(ii)==Type.INT) {
                idf2index.put(ii, new Integer(numberOfIntIdfs));
                numberOfIntIdfs ++; 
            }
        }
    }


    public void computeAbstractSigns(EnvStatique ES) {
        // trace 
        System.out.println(ES);
        // ----------------------------------------
        
        // init: attach BOT values for all integer variables, 
        // except in the initial state where the old value is set to TOP 
        // (As if the first step  of the fix-point computation was already done) 
        for (ControlPoint cp: theControlPoints) {
            cp.attach(new InfoAttached(this, 
                                       new SignVector(numberOfIntIdfs),
                                       new SignVector(numberOfIntIdfs)));
        }
        entry.attach
            (new InfoAttached(this, 
                              new SignVector(numberOfIntIdfs,Sign.TOP),
                              new SignVector(numberOfIntIdfs))); 
        
        // une phase de calcul de point fixe qui dit si elle a touche
        // a qq chose on travaille en lisant dans theSigns et en
        // ecrivant dans theNewSigns et a la fin on commit. theSigns
        // est initialise a BOT, et l'autre a BOT, pour pouvoir faire
        // des unions au fur et a mesure dans le parcours de graphe en
        // avant : A -> B on fait l'union avec la valeur deja la, qui
        // avait ete placee par une transition C -> B vue avant.
        
        System.out.println("============= INIT ===========================");
        System.out.println(this); 
        int pass = 1;
        while (true) {
            System.out.println("============= Passe numero " 
                               + pass++ + " ============" ); 

            // parcours des transitions 
            for (ControlPoint cp: theControlPoints) {
                for (Transition tr: cp.theTransitions) {
                    tr.computeAbstractSigns(cp.info, ES); 
                }
            }
                
            // l'etat intial n'a pas ete traite par la boucle sur les points
            // puisqu'on touche aux etats buts des transitions
            // on y met new=TOP toujours
        
            entry.info.setNew(new SignVector(numberOfIntIdfs,Sign.TOP)); 
        
            // ici tout le monde a un nouveau NewSign
            //System.out.println(" ========== AVANT COMMIT ========= ");
            //System.out.println(this);
            
            // commit, pour chaque point
            boolean change = false; 
            for (ControlPoint cp: theControlPoints) {
                // System.out.println("commit for state " + cp);
        
                if(cp.info.commit(new SignVector(numberOfIntIdfs))) {
                    change = true;
                }
            }

            //System.out.println(" ========== APRES COMMIT ========= ");
            System.out.println(this);
            //System.out.println("                    CHANGE" + change);
            if (! change) break;
            //if (pass > 16) break;
        }

    }

 
    public String toDotFile() {
        Iterator<ControlPoint> i = theControlPoints.iterator();
        String st = "digraph g {\n"; 
        while(i.hasNext()) {
            ControlPoint cp = i.next();
            st = st + cp.toDotFile();
            st = st + "\n";
        }
        st += "\n}\n";
        return st;
    }

    public String toString() {
        Iterator<ControlPoint> i = theControlPoints.iterator();
        String st = ""; 
        while (i.hasNext()) {
            ControlPoint cp = i.next();
            st = st + "Control point "  + cp + " has info attached  " 
                + cp.info + "\n";
            st = st + "Control point " + cp + " has transitions:" + "\n";
            st = st + cp.printTransitions ();
            st = st + "\n\n\n";
        }
        return st;
    }

    // ======================================================================
    // Constructors 

    void addLast(ControlGraph g) {
        theControlPoints.addAll(g.theControlPoints);
        exit.addCondTransition(CondTransition.condTRUE,g.entry);
        exit = g.exit;
        if (g.error != null) error = g.error;
    }    
    
    ControlGraph() {
        entry = new ControlPoint("Entry");
        exit = entry;
        theControlPoints = new ArrayList<ControlPoint>();
        theControlPoints.add(entry);
    }
    
    ControlGraph(AssertNode an) {
        // A completer 
    }

    // contructor for single-transition graph, made of a TRUE expression.
    // to be used for the write expressions.
    ControlGraph(WriteNode dummy) {
        // A completer 
    }

    ControlGraph(AffNode an) {
        // A completer 
    }


    // Various constructors for the conditional instructions and the loops
    // A completer ... 



    /**
     * Generate a part of the graph that tests condition ec,
     * by enrolling the boolean combinations into branches.
     * <p>
     * Whenever the condition is guaranteed to be true (resp. false),
     * a connection to tt is made (resp. to ff).
     * All the auxiliary points are added to cp. 
     * @param ec a Boolean expression
     * @param tt ff entry points of graphs for the code to reach when the 
     *   condition is true, resp. false
     * @param cp a list of control points to which auxiliary 
     *   points should be added
     */
    void generateCondition 
        (ExprNode ec, 
         ControlPoint begin, 
         ControlPoint tt, ControlPoint ff,
         List<ControlPoint> cp) {
    
        // A completer si on veut deplier les conditions booleennes. 
    }
}
