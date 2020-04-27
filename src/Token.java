import java.util.*;
import java.util.Queue;
import java.io.Serializable;

public class Token implements Serializable{

    private Queue<Integer> Q;
    private int[] LN;
    private int N;


    // Token Constructor
    public Token (int N){
        this.N = N;
        this.LN = new int[N];
        this.Q = new LinkedList<>();
        Arrays.fill(this.LN, -1);
    }


    public Queue<Integer> getQ(){
        return this.Q;
    }

    public int[] getLN(){
        return this.LN;
    }

    public void updateLN(int id, int val) {
        this.LN[id] = val;
    }

    public boolean queueIsEmpty() {
        if (this.Q.isEmpty()) return true;
        else return false;
    }

    public boolean queueContains(int val){
        return this.Q.contains(val);
    }

    public void addToQueue(int val){
        this.Q.add(val);
    }

    public int popQueue() {
        int process = this.Q.remove();
        return process;
    }



}
