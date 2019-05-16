/*
16/5/2019
Ash Russell 1245165
Tegan Walsh 1263564
*/

import java.util.LinkedList;
import java.util.Scanner;

public class REsearcher {
    private static LinkedList<State> fsmStates = new LinkedList<>();
    private static Dequeue dequeue = new Dequeue();

    public static void main(String[] args) {
        buildMachine();

        //TEMP, output all read states:
        for (State s : fsmStates) {
            System.out.println(s.getCharacter());
        }
    }

    private static void buildMachine() {
        int statenum, next1, next2;
        String character;
        String[] line;
        //Add SCAN to dequeue
        dequeue.push(new State(-1, (char) 0, -1, -1));
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
                    character = " ";
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
        //Add initial state to dequeue
        dequeue.addToHead(fsmStates.getFirst());
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
    Node pop() {
        if (head == null) return null;
        Node temp = head;
        head = head.next;
        head.previous = null;
        count--;
        return temp;
    }

    State getHeadState() {
        return head.state;
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


