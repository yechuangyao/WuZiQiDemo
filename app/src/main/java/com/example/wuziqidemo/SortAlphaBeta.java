package com.example.wuziqidemo;

import android.graphics.Point;

class SortAlphaBeta {
    private int BLACK = 1;
    private int H_FOUR = 400;//活4
    private int[][] board = new int[15][15];

    void sortAlphaBeta(int[][] c_board)//对节点进行排序
    {
        for (int i = 0; i < 15; i++) {
            System.arraycopy(c_board[i], 0, board[i], 0, 15);
        }
    }

    Point startSearch()//调用此函数开始进行搜索，返回最终结果的作弊
    {
        int MAX_VALUE = 9999;
        int alpha = -MAX_VALUE;
        Point pos = new Point();
        int qcount = 0;
        //初始化棋盘数组
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j] == 0)
                    qcount++;
            }
        }

        PointScore[] array = new PointScore[qcount];
        for (int i = 0; i < qcount; i++) {
            array[i] = new PointScore();
        }
        qcount = 0;
        for (int k = 0; k < 15; k++) {
            for (int p = 0; p < 15; p++) {
                if (board[k][p] == 0) {
                    array[qcount].pos.set(k, p);
                    qcount++;
                }
            }
        }
        evaAndSort(board, array, qcount, 2);


        for (int a = 0; a < qcount; a++) {
            int[][] c_board = new int[15][15];

            for (int i = 0; i < 15; i++) {
                System.arraycopy(board[i], 0, c_board[i], 0, 15);
            }

            int x = array[a].pos.x;
            int y = array[a].pos.y;
            c_board[x][y] = 2;
            int DEPTH = 3;
            int value = Search(c_board, DEPTH - 1, array[a],
                    alpha, MAX_VALUE, qcount - 1, BLACK);
            //剪枝
            if (value > alpha) {
                alpha = value;
                pos.set(x, y);
            }
        }
        return pos;
    }

    private int evaluation(int[][] c_board, int side, int x, int y)     //对节点进行估值
    {
        int posScore[][] = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
                {0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0},
                {0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0},
                {0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 1, 0},
                {0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0},
                {0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 1, 0},
                {0, 1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1, 0},
                {0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 1, 0},
                {0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0},
                {0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 1, 0},
                {0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0},
                {0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };      //对棋盘上靠中间位置进行赋分，使电脑下棋尽量靠中间
        int value = 0;
        value += posScore[x][y];

        int tempX = x;
        int tempY = y;
        int qcount = 0;
        int pos1;
        int pos2;
        //横向判断
        while (c_board[tempX][tempY] == side) {
            if (tempX != 14) {
                tempX++;
                qcount++;
            } else {
                qcount++;
                break;
            }
        }
        pos1 = c_board[tempX][tempY];
        qcount--;
        tempX = x;
        tempY = y;
        while (c_board[tempX][tempY] == side) {
            if (tempX != 0) {
                tempX--;
                qcount++;
            } else {
                qcount++;
                break;
            }
        }
        pos2 = c_board[tempX][tempY];
        //冲2
        int C_TWO = 2;
        //活2
        int H_TWO = 9;
        //冲3
        int C_THREE = 20;
        //活3
        int H_THREE = 30;
        //冲4
        int C_FOUR = 300;
        switch (qcount) {
            case 2: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_TWO);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_TWO);
            }
            break;
            case 3: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_THREE);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_THREE);

            }
            break;
            case 4: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_FOUR);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_FOUR);
            }
            break;
            case 5:
                value = value + 999;
                break;
            default:
                break;
        }
        //纵向判断
        tempX = x;
        tempY = y;
        qcount = 0;
        while (c_board[tempX][tempY] == side) {
            if (tempY != 14) {
                tempY++;
                qcount++;
            } else {
                qcount++;
                break;
            }
        }
        pos1 = c_board[tempX][tempY];
        qcount--;
        tempX = x;
        tempY = y;
        while (c_board[tempX][tempY] == side) {
            if (tempY != 0) {
                tempY--;
                qcount++;
            } else {
                qcount++;
                break;
            }
        }
        pos2 = c_board[tempX][tempY];
        switch (qcount) {
            case 2: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_TWO);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_TWO);
            }
            break;
            case 3: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_THREE);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_THREE);

            }
            break;
            case 4: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_FOUR);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_FOUR);
            }
            break;
            case 5:
                value = value + 999;
                break;
            default:
                break;
        }
        //左斜判断
        tempX = x;
        tempY = y;
        qcount = 0;
        while (c_board[tempX][tempY] == side) {
            if (tempX != 14 && tempY != 14) {
                tempX++;
                tempY++;
                qcount++;
            } else {
                qcount++;
                break;
            }
        }
        pos1 = c_board[tempX][tempY];
        qcount--;
        tempX = x;
        tempY = y;
        while (tempX != 0 && tempY != 0 && c_board[tempX--][tempY--] == side) {
            qcount++;
        }
        pos2 = c_board[tempX][tempY];
        switch (qcount) {
            case 2: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_TWO);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_TWO);
            }
            break;
            case 3: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_THREE);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_THREE);
            }
            break;
            case 4: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_FOUR);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_FOUR);
            }
            break;
            case 5:
                value = value + 999;
                break;
            default:
                break;
        }
        //右斜判断
        tempX = x;
        tempY = y;
        qcount = 0;
        while (c_board[tempX][tempY] == side) {
            if (tempY != 0 && tempX != 14) {
                tempX++;
                tempY--;
                qcount++;
            } else {
                qcount++;
                break;
            }
        }
        pos1 = c_board[tempX][tempY];
        qcount--;
        tempX = x;
        tempY = y;
        while (c_board[tempX][tempY] == side) {
            if (tempX != 0 && tempY != 14) {
                tempX--;
                tempY++;
                qcount++;
            } else {
                qcount++;
                break;
            }
        }
        pos2 = c_board[tempX][tempY];
        switch (qcount) {
            case 2: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_TWO);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_TWO);
            }
            break;
            case 3: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_THREE);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_THREE);
            }
            break;
            case 4: {
                if (pos1 == 0 && pos2 == 0)
                    value = value + judgeValue(side, H_FOUR);
                else if (pos1 + pos2 != 0 && pos1 * pos2 == 0)
                    value = value + judgeValue(side, C_FOUR);
            }
            break;
            case 5:
                value = value + 999;
                break;
            default:
                break;
        }
        return value;
    }

    private int Search(int[][] c_board, int depth, PointScore ps, int alpha, int beta, int step, int side)//搜索函数
    {
        if (side == BLACK) {
            PointScore[] array = new PointScore[step];
            for (int i = 0; i < step; i++) {
                array[i] = new PointScore();
            }
            int qcount = 0;
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 15; j++) {
                    if (c_board[i][j] == 0) {
                        array[qcount].pos.set(i, j);
                        qcount++;
                    }
                }
            }
            evaAndSort(c_board, array, step, side);
            //到达叶节点
            if (depth == 1) {
                return array[qcount].value + ps.value;
            }
            //游戏结束
            else if (array[0].value > 999) {
                return -999;
            }
            //到达子节点
            for (int k = 0; k < step; k++) {
                int x = array[k].pos.x;
                int y = array[k].pos.y;
                if (alpha >= beta) {
                    return beta;
                }
                c_board[x][y] = side;
                int WHITE = 2;
                int value = Search(c_board, depth - 1, array[k], alpha, beta, step - 1, WHITE);
                c_board[x][y] = 0;

                if (value < beta) {
                    beta = value;
                }
            }
            return beta + ps.value;
        } else {
            PointScore[] array = new PointScore[step];
            for (int i = 0; i < step; i++) {
                array[i] = new PointScore();
            }
            int qcount = 0;
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 15; j++) {
                    if (c_board[i][j] == 0) {
                        array[qcount].pos.set(i, j);
                        qcount++;
                    }
                }
            }
            evaAndSort(c_board, array, step, side);
            //到达叶节点
            if (depth == 1) {
                return array[0].value - ps.value;
            }
            //游戏结束
            else if (array[0].value > 999) {
                return 999;
            }
            for (int k = 0; k < step; k++) {
                int x = array[k].pos.x;
                int y = array[k].pos.y;
                if (alpha >= beta) {
                    return alpha;
                }
                c_board[x][y] = side;
                int value = Search(c_board, depth - 1, array[k], alpha, beta, step - 1, BLACK);
                c_board[x][y] = 0;

                if (value > alpha) {
                    alpha = value;
                }
            }
            return alpha - ps.value;
        }
    }

    private void evaAndSort(int[][] c_board, PointScore[] array, int step, int side) {
        PointScore temp;
        int x;
        int y;
        //遍历所有可能性并估分
        for (int k = 0; k < step; k++) {
            x = array[k].pos.x;
            y = array[k].pos.y;
            c_board[x][y] = side;
            array[k].value = evaluation(c_board, side, array[k].pos.x, array[k].pos.y);
            c_board[x][y] = 0;
        }
        //排序
        for (int i = 0; i < step; i++) {
            for (int j = i + 1; j < step - 1; j++) {
                if (array[i].value < array[j].value) {
                    temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            }
        }
    }

    private int judgeValue(int side, int point)//判断得分
    {
        if (side == BLACK && point == H_FOUR) {
            return point + 350;
        } else
            return point;
    }
}
