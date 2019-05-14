/*
12/5/2019
Ash Russell 1245165
Tegan Walsh 1263564
RECompiler accepts a regexp pattern as a command-line argument. It then produces, as standard output,
a description of the corresponding FSM, such that each line of output includes four things: the state-number,
a string containing the input-symbol(s) this state must match (or branch-state indicator), and two numbers
indicating the two possible next states if a match is made.

*/

import java.util.ArrayList;


public class REcompiler
{

    public static void main (String[] args) {
        if (args.length != 1) {
            System.out.println("Error - one argument only");
            System.exit(1);
        }

        char[] p = args[0].toCharArray();
        System.out.println("PROGRAM: START");

        for (int i = 0; i < p.length; i++)
            System.out.print(p[i]);

        System.out.println();
        Compiler c = new Compiler(p);

        c.parse();
    }

}

class Compiler
{
//    Grammar
//	-------
//    E -> T
//    E -> T E
//    T -> F
//    T -> F*
//    T -> F+T
//    F -> v
//    F -> (E)
//
//    ArrayList<Character> ch = new ArrayList<Character>();
//    ArrayList<Integer> next1 = new ArrayList<Integer>();
//    ArrayList<Integer> next2 = new ArrayList<Integer>();
//    ArrayList<Character> p;

    char[] ch;
    int[] next1;
    int[] next2;
    char[] p;
    int j;
    int state = 0;

    public Compiler(char[] passedValue)
    {
        p = passedValue;
        ch = new char[999];
        next1 = new int[999];
        next2 = new int[999];
    }

    void parse()
    {
        //inital = the start state for the expression
        int initial = expression();
        set_state(state,' ',0,0);
        for (int i = 0; i < p.length; i++) System.out.println(i + " | " + ch[i] + " " + next1[i] + " " + next2[i]);
        System.out.println("Inital start state: " + initial);
    }

    int expression()
    {
        int r;
        r = term();
        if (j < p.length) {
            if (isvocab(p[j]) || p[j] == '(')
                expression();
        }
        return(r);
    }

    int term()
    {
        int r,t1,t2,f;
        f=state-1;
        r=t1=factor();
        if(p[j]=='*')
        {
            set_state(state,' ',state+1,t1);
            j++;
            r=state;
            state++;
        }
        return(r);
    }

    int factor()
    {
        int r = -1;
        if(isvocab(p[j]))
        {
            set_state(state, p[j],state+1,state+1);
            j++;
            r=state;
            state++;
        }
        else
        if(p[j]=='(')
        {
            j++;
            r=expression();
            System.out.println('*');
            if (j>=p.length) errorState("1) Reached end of expression without matching bracket");
            if(p[j]==')')
                j++;
            else errorState("2) No matching bracket");
        }
        //if (p[j] == ')') errorState("Unmatched bracket");
        return(r);
    }

    void set_state(int s, char c, int n1, int n2)
    {
        ch[s]=c;
        next1[s]=n1;
        next2[s]=n2;
    }

    boolean isvocab(char c) {
        char[] compare = {'(', '+', ')'};
        for (char v: compare)
            if (c == v) return false;
        return true;
    }

    void errorState(String s)
    {
        System.out.println("Error: " + s);
        System.exit(1);
    }
}
