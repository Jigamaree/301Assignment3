/*
12/5/2019
Ash Russell 1245165
Tegan Walsh 1263564
RECompiler accepts a regexp pattern as a command-line argument. It then produces, as standard output,
a description of the corresponding FSM, such that each line of output includes four things: the state-number,
a string containing the input-symbol(s) this state must match (or branch-state indicator), and two numbers
indicating the two possible next states if a match is made.

*/

import java.awt.desktop.SystemEventListener;

import static jdk.internal.jline.internal.Log.error;

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

    }

}

public class theCompiler
{
    char[] ch;
    int[] next1;
    int[] next2;

    char[] p;
    int j;
    int state = 1;

    /*****************************************************************/
    void parse()
    {
        int initial;

        initial=expression();
        if( p[j] ) error(); // In C, zero is false, not zero is true
        set_state(state,' ',0,0);
    }


    void set_state(int s, char c, int n1, int n2)
    {
        //int set_state(int s, char c, int n1, int n2)
        ch[s]=c;
        next1[s]=n1;
        next2[s]=n2;
    }

    boolean isvocab(char c)
    {
        char[] compareList = {'.','*','?', '|', '(', ')', '[', ']', '^', 'A', '\\'};
        for (char l: compareList){
            if (c == l) return false;
        }
        return true;
    }

    int expression()
    {
        int r;

        r=term();
        if(isvocab(p[j])||p[j]=='[') expression();
        return(r);
    }

    int term()
    {
        int r;
        int t1;
        int t2;
        int f;

        f = state-1;
        r = t1=factor();

        if(p[j]=='*'){
            set_state(state,' ',state+1,t1);
            j++; r=state; state++;
        }
        if(p[j]=='+'){
            if(next1[f]==next2[f])
                next2[f]=state;
            next1[f]=state;
            f=state-1;
            j++;r=state;state++;
            t2=term();
            set_state(r,' ',t1,t2);
            if(next1[f]==next2[f])
                next2[f]=state;
            next1[f]=state;
        }
        return(r);
    }

    int factor()
    {
        int r;

        if(isvocab(p[j])){
            set_state(state,p[j],state+1,state+1);
            j++;r=state; state++;
        }
        else
        if(p[j]=='['){
            j++; r=expression();
            if(p[j]==']')
                j++;
            else
                error();
        }
        else
            error();
        return(r);
    }
}
