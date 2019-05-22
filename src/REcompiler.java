/*
12/5/2019
Ash Russell 1245165
Tegan Walsh 1263564
RECompiler accepts a regexp pattern as a command-line argument. It then produces, as standard output,
a description of the corresponding FSM, such that each line of output includes four things: the state-number,
a string containing the input-symbol(s) this state must match (or branch-state indicator), and two numbers
indicating the two possible next states if a match is made.

Currently satisfies current rules:
(* = known bugs exist)
1, 2, 3, 4, 5, 6, 7, 8, 10, 11

Does not work:
^[]

ACTIVE BUGS:
1) Backslash does not work on backslash.
2) A list followed by a bracketed list does not work as intended 

*/

import java.io.BufferedWriter;
import java.util.ArrayList;

public class REcompiler {

    public static void main(String[] args) {
        System.out.println("REcompiler: Start!");

        //simple error check
        if (args.length != 1) {
            System.out.println("Error - one argument only");
            System.exit(1);
        }

        //imports input to array
        char[] p = args[0].toCharArray();

        checkBrackets(p);
        //assuming no errors, convert any [abc] lists to (a|b|c) form
        char[] pp = convertLists(p);

        Compiler c = new Compiler(pp);

        //prints regex to screen: error checking purposes
        System.out.print("Regex: ");
        for (int i = 0; i < p.length; i++)
            System.out.print(p[i]);
        System.out.println();

        //prints converted
        System.out.print("Converted Regex: ");
        for (int i = 0; i < pp.length; i++)
            System.out.print(pp[i]);
        System.out.println();

        //creates new compiler object, and then starts the compiling process
        c.parse();
    }

    static public void checkBrackets(char[] p)
    {
        int roundLeft = 0;
        int roundRight = 0;

        for (int j = 0; j < p.length; j++)
        {
            if (p.length == 1 || (p[j] == '(' && p[j + 1] == ')')) mainErrorState("Brackets cannot be empty");

            if (p[j] == '(' && p[j -1] != '\\') roundLeft++;
            else if (p[j] == ')' && p[j -1] != '\\') roundRight++;

            else if (p[j] == '[' && p[j -1] != '\\')
            {
                j++;
                //account for []abc] case
                if (p[j] ==  ']'&& p[j -1] != '\\') j++;
                if (j>=p.length) mainErrorState("No match for [ bracket");

                while (p[j] != ']'&& p[j -1] != '\\')
                {
                    j++;
                    if (j>=p.length) mainErrorState("No match for [ bracket");
                }
                j++;
            }

            else if (p[j] == ']' && p[j-1] != '\\') mainErrorState("Unmatched ] bracket!");
        }

        if (roundLeft < roundRight) mainErrorState("Too many ) brackets!");
        if (roundRight < roundLeft) mainErrorState("Too many ( brackets!");
        else
        {
            System.out.println("CheckBrackets: SUCCESS");
            System.out.println("");
        }
    }

    static public char[] convertLists(char[] p)
    {
        String compare = "(+)*?|[";
        String output = "";

        for (int j = 0; j < p.length; j++)
        {

            if (p[j] == '\\')
            {
                //do nothing
            }

            if (p[j] == '\\' && p[j-1] == '\\')
            {
                output = output + '\\' + p[j];
            }

            //just regular part of the expression
            else if (p[j] != '[')
                output = output + p[j];

            else if (p[j] == '[' && p[j-1] == '\\')
            {
                output = output + '\\' + '[';
            }


            else
            {
                output = output + '(';
                j++;
                //deal with []abc]
                if (p[j] == ']')
                {
                    output = output + ']';
                    j++;
                    output = output +'|';
                }

                //list loop!
                while (p[j] != ']')
                {
                    String s = "" + p[j];
                    if(compare.contains(s))
                    {
                        output = output + '\\' + p[j];
                    }

                    else output = output + p[j];
                    j++;
                    output = output + '|';
                }
                output = output.substring(0,output.length()-1);
                output = output + ')';
            }

        }

        char[] finalArray = output.toCharArray();
        return finalArray;
    }

    //static public

    //quits out of the program if the regex at any point violates any specifications; reports to screen error
    public static void mainErrorState(String s) {
        System.out.println("Error: " + s);
        System.exit(1);
    }

}


/*

GRAMMAR:
E -> T
E -> T E

T -> F
T -> F*
T -> F|T

F -> v
F -> . (wildcard; matches everything)
F -> (E)

NOT IMPLEMENTED:

T -> F? (seen either 0 or 1 times)

F -> [v v v v] (list of values; will match one)
F -> ^[v v v v] (as long as its NOT one of those values, you good my dude)
F -> \v (where v is a value that would otherwise have a special meaning)
 */

class Compiler {

    //global variables
    char[] ch;
    int[] next1;
    int[] next2;
    char[] p;
    int j;
    int state = 0;
    char[] compare = {'(', '+', ')', '^', '*', '?', '|', '[', ']'};
    boolean isException = false;


    /*creates compiler; im sure there's a more elegant solution to the arrays
    besides initalising them to a stupid high number, but beats me what it is :D */
    public Compiler(char[] passedValue) {
        p = passedValue;
        ch = new char[999];
        next1 = new int[999];
        next2 = new int[999];
    }

    /*first called function; keeps tract of the inital state that the expression starts
    from. Once it returns the expression has been complete; it then writes the resulting
    FSM to the screen */
    void parse() {
        System.out.println();
        System.out.println("Compiler: Start!");
        set_state(state, ' ', 0, 0);
        state++;
        //inital = the start state for the expression
        int initial = expression();
        //finishing equation
        set_state(state, ' ', 0, 0);
        //setting the proper start state
        next1[0] = initial;
        next2[0] = initial;
        for (int i = 0; i <= state; i++) {
            if (i < 10) System.out.println(i + " | " + ch[i] + " " + next1[i] + " " + next2[i]);
            else System.out.println(i + " | " + ch[i] + " " + next1[i] + " " + next2[i]);
        }
        System.out.println("Inital start state: " + initial);
    }

    int expression() {
        int r;
        r = term();
        if (j < p.length) {
            if (isvocab(p[j]) || p[j] == '(')
                expression();
        }
        return (r);
    }

    int term() {
        int r, t1, t2, f;
        f = state - 1;
        r = t1 = factor();
        if (j == p.length) return (r);

        //if we can see something 0-infinite times
        if (p[j] == '*') {
            set_state(state, ' ', state + 1, t1);
            j++;
            r = state;
            state++;
            if(j >= p.length) return r;
        }

        //if we can see something 0-1 times
        if (p[j] == '?') {
            set_state(state, ' ', state + 1, t1);
            j++;
            r = state;

            //state++;

            for (int i = 0; i <= state; i++ )
            {
                if (next1[i] == state) next1[i] = state+1;
                if (next2[i] == state) next2[i] = state+1;
            }

            state++;

            if(j >= p.length) return r;
        }

        //the OR statement of this regex
        if (p[j] == '|') {
            if (f == -1) f = 0;
            if (next1[f] == next2[f])
                next2[f] = state;
            next1[f] = state;
            f = state - 1;
            j++;
            r = state;
            state++;
            t2 = term();
            set_state(r, ' ', t1, t2);
            if (next1[f] == next2[f])
                next2[f] = state;
            next1[f] = state;
        }
        return (r);
    }

    int factor() {
        int r = 0;

        //escaped character management
        if(p[j] == '\\')
        {
                isException = true;
                j++;
        }

        //is just a literal
        if (isvocab(p[j])) {
            set_state(state, p[j], state + 1, state + 1);
            j++;
            r = state;
            state++;
        }

        //is bracketed
        else if (p[j] == '(') {
            j++;
            r = expression();
            if (j >= p.length) errorState("Reached end of regex before finding matching bracket");
            if (p[j] == ')')
                j++;
            else errorState("No matching bracket");
        }


        return (r);
    }

    //sets the state in the arrays
    void set_state(int s, char c, int n1, int n2) {
        //System.out.println(s + " | " + c + " " + n1 + " " + n2);
        ch[s] = c;
        next1[s] = n1;
        next2[s] = n2;
    }

    //checks the value isnt special
    boolean isvocab(char c) {

        if (isException == true)
        {
            isException = false;
            return true;
        }

        if (c == '.') return true;

        String s = "" + c;
        String compare = "(+)*?|[";
            if(compare.contains(s)) return false;
        return true;
    }

    //quits out of the program if the regex at any point violates any specifications; reports to screen error
    public static void errorState(String s) {
        System.out.println("Error: " + s);
        System.exit(1);
    }

}
