package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Sleep {
    final public static int FREE_SPACE = 0;
    final public static int MY_SYMBOL = 1; //You are always 1
    final public static int OPPONENT = 2;

    public static void sleep(int[][] board)throws InterruptedException {
        sleep(20000);
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
