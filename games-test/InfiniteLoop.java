package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InfiniteLoop {
    final public static int FREE_SPACE = 0;
    final public static int MY_SYMBOL = 1; //You are always 1
    final public static int OPPONENT = 2;

    public static void move(int[][] board) {
        while(true)System.out.println("0 0");
    }
}

class Move {
    int i;
    int j;

    public Move(int i, int j) {
        this.i = i;
        this.j = j;
    }
}
