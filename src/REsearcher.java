/*
16/5/2019
Ash Russell 1245165
Tegan Walsh 1263564
*/

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;

public class REsearcher {
    private static LinkedList<State> fsmStates = new LinkedList<>();
    private static Dequeue dequeue;

    public static void main(String[] args) {
        buildMachine();
        try {
            //Setup input from file passed in as arg
            if (args.length != 1) throw new Exception("Please enter a single argument " +
                    "specifying file to search. Usage: java REsearcher <filename>");
            BufferedReader in = new BufferedReader(new FileReader(args[0]));
            String line, str;
            while ((line = in.readLine()) != null) {
                str = line;
                //Starting from each character in the line, attempt to match pattern.
                while (str.length() > 0) {
                    //Restart dequeue with SCAN and initial state
                    dequeue = new Dequeue();
                    dequeue.push(new State(-1, (char) 0, -1, -1));
                    dequeue.addToHead(fsmStates.getFirst());
                    //If match in string, print whole line and move onto next line
                    if (match(str)) {
                        System.out.println(line);
                        break;
                    } else str = str.substring(1);
                }
            }
            in.close();
        } catch (FileNotFoundException f) {
            System.err.println("Error: please enter a valid filepath as argument.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static boolean match(String str) {
        State currState;
        boolean branchActive = false;
        while (str.length() > 0) {
            currState = dequeue.pop();

            //SCAN state: if dequeue empty then return false, otherwise put SCAN at tail making next states current
            if (currState.getStateNum() == -1) {
                if (dequeue.isEmpty()) return false;
                else dequeue.push(currState);
            }
            //Branching state, space char as character. Add both states to current states, head of dequeue
            else if (currState.getCharacter() == ' ') {
                dequeue.addToHead(fsmStates.get(currState.getNext1()));
                dequeue.addToHead(fsmStates.get(currState.getNext2()));
                branchActive = true;
            }
            //Match made, if final state return true otherwise move onto next state and next character of string
            else if (currState.getCharacter() == str.charAt(0) || currState.getCharacter() == '.') {
                currState = fsmStates.get(currState.getNext1());
                if (currState.getNext1() == 0 && currState.getNext2() == 0) return true;
                dequeue.push(currState);
                str = str.substring(1);
            }
            //Give a second run if branch
            else if (branchActive) branchActive = false;
                //No match/branch/SCAN, return false
            else return false;
        }
        //Final state not reached with end of string, return false
        return false;
    }

    private static void buildMachine() {
        int statenum, next1, next2;
        String character;
        String[] line;
        try {
            //Read each line/state from stdin and add to list of states.
            Scanner in = new Scanner(System.in);
            while (in.hasNextLine()) {
                line = in.nextLine().split("[ ]+"); //Separated by any number of spaces
                statenum = Integer.parseInt(line[0]);
                if (line.length == 5) { //Account for case with character
                    character = line[2]; //Ignore line[1] = bar
                    next1 = Integer.parseInt(line[3]);
                    next2 = Integer.parseInt(line[4]);
                } else if (line.length == 4) { //Account for case with no character
                    character = " ";                                    //Branch state indicator = space
                    next1 = Integer.parseInt(line[2]);
                    next2 = Integer.parseInt(line[3]);
                } else
                    throw new Exception("Wrong number of items in line.");
                if (character.length() != 1) throw new Exception("Only input one character per state.");
                fsmStates.add(statenum, new State(statenum, character.charAt(0), next1, next2));
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

class State {
    private int stateNum;
    private char character;
    private int next1;
    private int next2;

    State(int s, char c, int n1, int n2) {
        stateNum = s;
        character = c;
        next1 = n1;
        next2 = n2;
    }

    char getCharacter() {
        return character;
    }

    int getStateNum() {
        return stateNum;
    }

    int getNext1() {
        return next1;
    }

    int getNext2() {
        return next2;
    }
}

class Dequeue {
    private Node head, tail;
    private int count = 0;

    //Add to tail of queue
    void push(State state) {
        if (head == null) {
            head = new Node(state, null, null);
            tail = head;
        } else {
            Node temp = new Node(state, tail, null);
            tail.next = temp;
            tail = temp;
        }
        count++;
    }

    //Add to head of queue
    void addToHead(State state) {
        if (head == null) {
            head = new Node(state, null, null);
            tail = head;
        } else {
            Node temp = new Node(state, null, head);
            head.previous = temp;
            head = temp;
        }
        count++;
    }

    //Pop head of queue
    State pop() {
        if (head == null) return null;
        Node temp = head;
        head = head.next;
        head.previous = null;
        count--;
        return temp.state;
    }

    boolean isEmpty() {
        return count == 0;
    }

    class Node {
        State state;
        Node previous, next;

        Node(State s, Node p, Node n) {
            state = s;
            previous = p;
            next = n;
        }
    }
}


