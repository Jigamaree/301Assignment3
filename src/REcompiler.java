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
1, 2, 3, 4, 6, 7*, 11

ACTIVE BUGS:
1) Can't catch stray ) brackets; treated like the end of the expression and will cut off regex early in worst
    case scenarios.
    eg: ab)c

OTHER BUGS:
(not sure whether failing these are failing the specs, but have taken note)
1) If the entire expression is bracketed, the program fails. Redundant?
    eg: (ab)
2) for | function: (ab)|c fails. Might be alright since is technically left recursive and therefore
   outside the specs?
*/

import java.io.BufferedWriter;

public class REcompiler
{

    public static void main (String[] args) {
        //simple error check
        if (args.length != 1) {
            System.out.println("Error - one argument only");
            System.exit(1);
        }

        //imports input to array
        char[] p = args[0].toCharArray();

        //prints regex to screen: error checking purposes
        System.out.print("Regex: ");
        for (int i = 0; i < p.length; i++)
            System.out.print(p[i]);
        System.out.println();

        //creates new compiler object, and then starts the compiling process
        Compiler c = new Compiler(p);
        c.parse();

        /* STANDARD OUTPUT GOES HERE - could easily move it to c.parse, but easier to
        find here! Also less easily confused, since parse write it to screen. */
        char[] ch = c.ch;
        int[] next1 = c.next1;
        int[] next2 = c.next2;
        int state = c.state;

        for(int i = 0; i <= state; i++)
        {
            //output (ch[i] + " " + next1[i] + " " + next2[i]);
        }


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

class Compiler
{

    //global variables
    char[] ch;
    int[] next1;
    int[] next2;
    char[] p;
    int j;
    int state = 0;

    /*creates compiler; im sure there's a more elegant solution to the arrays
    besides initalising them to a stupid high number, but beats me what it is :D */
    public Compiler(char[] passedValue)
    {
        p = passedValue;
        ch = new char[999];
        next1 = new int[999];
        next2 = new int[999];
    }

    /*first called function; keeps tract of the inital state that the expression starts
    from. Once it returns the expression has been complete; it then writes the resulting
    FSM to the screen */
    void parse()
    {
        //inital = the start state for the expression
        int initial = expression();
        set_state(state,' ',0,0);
        for (int i = 0; i <= state; i++)
        {
            if (i < 10) System.out.println(i + " | " + ch[i] + " " + next1[i] + " " + next2[i]);
            else System.out.println(i + " | " + ch[i] + " " + next1[i] + " " + next2[i]);
        }
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
        if (j == p.length) return(r);

        //if we can see something 0-infinite times
        if(p[j]=='*')
        {
            set_state(state,' ',state+1,t1);
            j++;
            r=state;
            state++;
        }

        //the OR statement of this regex
        if(p[j]=='|')
        {
            if (f == -1) f = 0;
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
        int r = 0;

        //is just a literal
        if(isvocab(p[j]))
        {
            set_state(state, p[j],state+1,state+1);
            j++;
            r=state;
            state++;
        }

        //is bracketed
        else if(p[j]=='(')
        {
            checkForEmpty();
            j++;
            r=expression();
            System.out.println('*');
            if (j>=p.length) errorState("Reached end of regex before finding matching bracket");
            if(p[j]==')')
                j++;
            else errorState("No matching bracket");
        }

        //is in square brackets - NOT IMPLEMENTED
        else if (p[j] == '[')
            {
                checkForEmpty();
                j++;
                //if this returns clear, if the second value is a ] we can add that to our checklist safely
                if(p[j+1]==']')
                {
                /* go through, add all to list
                then cycle through list, n1 going to next part of list, n2 going to end
                eg: a[bcd]e
                0 | a 1 1
                1 | b 2 4
                2 | c 3 4
                3 | d 4 4
                4 | e 5 5
                5 |   0 0
                */
                }

        }

        //is in ^[] brackets - NOT IMPLEMENTED
        else if (p[j] == '^')
        {
            if (p[j+1] != '[') errorState("^ was not followed by [");
            else
            {
                /* Think will be similar process to above? may need to transform p[] into a
                string array (changing all other calls as necessary), adding all values contained
                in ^[] into one string (eg "abcd1@"), transform that into a char array and then
                loop through? not sure how this would be represented in
                state | ch n1 n2
                form though
                */
            }
        }

        //if we want to make a special character not special anymore. :( NOT IMPLEMENTED
        else if (p[j] == '\\')
        {
            /*
            Think will involve some sort of variable on the factor level which makes
            isvocab() return true - dummy code in 'isvocab'
            boolean isException;
            This may make more sense to have earlier in the factor method!
            */
        }

        return(r);
    }

    //sets the state in the arrays
    void set_state(int s, char c, int n1, int n2)
    {
        //System.out.println(s + " | " + c + " " + n1 + " " + n2);
        ch[s]=c;
        next1[s]=n1;
        next2[s]=n2;
    }

    //checks the value isnt special
    boolean isvocab(char c) {
        if (c == '.') return true;
        /*
        if (isException == true)
        {
        isException = false;
        return true;
        }
        */
        char[] compare = {'(', '+', ')', '^', '*'};
        for (char v: compare)
            if (c == v) return false;
        return true;
    }

    //quits out of the program if the regex at any point violates any specifications; reports to screen error
    void errorState(String s)
    {
        System.out.println("Error: " + s);
        System.exit(1);
    }

    /*method checks that if a left bracket - (, [ - that it is used legally within the grammar
    this does NOT check the contents itself is legal, nor for missing brackets */
    void checkForEmpty()
    {
        if (p.length == 1 || (p[j]=='(' && p[j+1] == ')')) errorState("Brackets cannot be empty");
        if (p[j] == '[')
        {
            if (p[j+1] == ']')
            {
                for (int i = j+1; i <= p.length; i++)
                {
                    if (p[i] == ']') return;
                }
                errorState("did not have a closing square bracket");
            }

        }
    }
}
