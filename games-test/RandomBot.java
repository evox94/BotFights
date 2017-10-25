package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBot {
    final public static int FREE_SPACE = 0;
    final public static int MY_SYMBOL = 1; //You are always 1
    final public static int OPPONENT = 2;

    public static void move(int[][] board) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == FREE_SPACE) {
                    moves.add(new Move(i, j));
                }
            }
        }
        Random r = new Random();
        Move m = moves.get(r.nextInt(moves.size()));
        System.out.println(m.i+" "+m.j);
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