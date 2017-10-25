package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReactorBot {
    final public static int FREE_SPACE = 0;
    final public static int MY_SYMBOL = 1; //You are always 1
    final public static int OPPONENT = 2;

    public static void move(int[][] board) {
        List<Move> moves = new ArrayList<>();

        int symbol = MY_SYMBOL;

        if (check(board, moves, MY_SYMBOL)) return;
        if(check(board,moves,OPPONENT))return;

        Random r = new Random();
        Move m = moves.get(r.nextInt(moves.size()));
        System.out.println(m.i+" "+m.j);
    }

    private static boolean check(int[][] board, List<Move> moves, int symbol) {
        for (int i = 0; i < 3; i++) {
            int count = 0;
            int free = -1;
            for (int j = 0; j < 3; j++) {
                if(board[i][j] == symbol)count++;
                else if(board[i][j] == FREE_SPACE){
                    free = j;
                    moves.add(new Move(i,j));
                }
            }
            if(count == 2 && free != -1){
                System.out.println(i+" "+free);
                return true;
            }
        }

        for (int i = 0; i < 3; i++) {
            int count = 0;
            int free = -1;
            for (int j = 0; j < 3; j++) {
                if(board[j][i] == symbol)count++;
                else if(board[j][i] == FREE_SPACE){
                    free = j;
                }
            }
            if(count == 2 && free != -1){
                System.out.println(free+" "+i);
                return true;
            }
        }

        for(int i=0, count=0, free = -1; i<3; i++){
            if(board[i][i] == symbol){
                count++;
            }else if(board[i][i] == FREE_SPACE){
                free = i;
            }

            if(i == 2 && count == 2 && free != -1){
                System.out.println(free+" "+free);
                return true;
            }
        }

        for(int i=0, count=0, free = -1; i<3; i++){
            if(board[i][2-i] == symbol){
                count++;
            }else if(board[i][2-i] == FREE_SPACE){
                free = i;
            }

            if(i == 2 && count == 2 && free != -1){
                System.out.println(free+" "+(2-free));
                return true;
            }
        }
        return false;
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
