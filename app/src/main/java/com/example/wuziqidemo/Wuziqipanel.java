package com.example.wuziqidemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Wuziqipanel extends View {
    private SortAlphaBeta aiPlayer = new SortAlphaBeta();
    private boolean mIsAI = true;

    private int mPanelWidth;        //棋盘宽度
    private float mLineHeight;      //线间距
    private int MAX_LINE = 15;      //棋盘横竖线数
    private int MAX_COUNT_IN_LINE = 5;      //直线上有相同五子就赢
    private int MAX_PIECES = MAX_LINE * MAX_LINE;       //最大下子数

    private Paint mPaint = new Paint();

    private Bitmap mWhitePiece;          //白棋
    private Bitmap mBlackPiece;         //黑棋

    private float ratioPieceOfLineHeight = 3 * 1.0f / 4;        //棋子相对线间距的比例

    //白棋先手，当前轮到白棋
    private boolean mIsWhite = true;
    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();

    private Point LastPoint;        //当前下的最后一个棋子

    private boolean mIsGameOver;        //判断游戏结束变量
    private boolean mIsWhiteWinner;     //判断胜负方，true则白棋胜，否则黑棋胜

    public int[][] StateBoard = new int[MAX_LINE][MAX_LINE]; //棋盘状态二维数组

    /*
     * 初始化棋盘状态数组
     */
    private void InitializeStateBoard() {
        for (int i = 0; i < MAX_LINE; i++)
            for (int j = 0; j < MAX_LINE; j++) {
                StateBoard[i][j] = 0;     //0表示没有棋子
            }
    }

    /*
     * 初始化自定义View
     */
    public Wuziqipanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
//        setBackgroundColor(0x44ff0000);
        init();
    }

    private void init() {
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
    }

    /*
     * 测量获取棋盘的宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width, width);
    }

    /*
     * 监察棋盘的宽高大小变化，确定棋子的大小
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth = w;
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;

        int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
    }

    /*
     * 触摸屏幕下棋事件
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsGameOver) return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            Point p = getValidPoint(x, y);
            if (mWhiteArray.contains(p) || mBlackArray.contains(p)) {
                return false;
            }

            if (mIsWhite) {
                mWhiteArray.add(p);
                LastPoint = p;
                mIsWhite = !mIsWhite;
            } else if (!mIsAI) {      //不是人机对战
                mBlackArray.add(p);
                LastPoint = p;
                mIsWhite = !mIsWhite;
            }
            invalidate();
        }
        return true;
    }

    /*
     * 获得正当的点
     */
    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    /*
     * 绘制棋盘和棋子
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPiece(canvas);
        new MyAsyncTask().execute();
        checkGameOver();        //检查是否游戏结束
    }

    /*
     * 判断是否游戏结束
     */
    private void checkGameOver() {
        boolean whiteWin = checkFiveInLine(mWhiteArray);
        boolean blackWin = checkFiveInLine(mBlackArray);

        if (whiteWin || blackWin) {
            mIsGameOver = true;
            mIsWhiteWinner = whiteWin;
            String text = mIsWhiteWinner ? "白棋胜利" : "黑棋胜利";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        } else if (mWhiteArray.size() + mBlackArray.size() == MAX_PIECES) {
            String text = "和棋";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * 检查棋盘中是否有相同的五子相连
     */
    private boolean checkFiveInLine(List<Point> points) {
        for (Point p : points) {        //遍历所有的棋子
            int x = p.x;
            int y = p.y;
            boolean win = checkHorizontal(x, y, points);
            if (win) return true;
            win = checkVertical(x, y, points);
            if (win) return true;
            win = checkLeftDiagonal(x, y, points);
            if (win) return true;
            win = checkRightDiagonal(x, y, points);
            if (win) return true;
        }
        return false;
    }

    /*
     * 检查水平方向是否有相同的五子相连
     */
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y))) {
                count++;
            } else {
                break;
            }
        }
        return count == MAX_COUNT_IN_LINE;
    }

    /*
     * 检查竖直方向是否有相同的五子相连
     */
    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }
        return count == MAX_COUNT_IN_LINE;
    }

    /*
     * 检查左斜方向是否有相同的五子相连
     */
    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        return count == MAX_COUNT_IN_LINE;
    }

    /*
     * 检查右斜方向是否有相同的五子相连
     */
    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        return count == MAX_COUNT_IN_LINE;
    }

    /*
     * 绘制棋子
     */
    private void drawPiece(Canvas canvas) {
        for (int i = 0, n = mWhiteArray.size(); i < n; i++) {
            Point whitePoint = mWhiteArray.get(i);
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    (whitePoint.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight, null);
        }

        for (int i = 0, n = mBlackArray.size(); i < n; i++) {
            Point blackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    (blackPoint.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight, null);
        }
        /*
         * 给当前下的最后一个棋子做标记
         */
        Paint tempPaint = new Paint();
        tempPaint.setARGB(255, 255, 0, 0);
        tempPaint.setAntiAlias(true);
        tempPaint.setStrokeWidth(2);
        if (LastPoint != null) {
            canvas.drawLine(LastPoint.x * mLineHeight, LastPoint.y * mLineHeight,
                    ((LastPoint.x + 1) * mLineHeight), LastPoint.y * mLineHeight, tempPaint);
            canvas.drawLine(LastPoint.x * mLineHeight, LastPoint.y * mLineHeight,
                    LastPoint.x * mLineHeight, (LastPoint.y + 1) * mLineHeight, tempPaint);
            canvas.drawLine((LastPoint.x + 1) * mLineHeight, LastPoint.y * mLineHeight,
                    (LastPoint.x + 1) * mLineHeight, (LastPoint.y + 1) * mLineHeight, tempPaint);
            canvas.drawLine(LastPoint.x * mLineHeight, (LastPoint.y + 1) * mLineHeight,
                    ((LastPoint.x + 1) * mLineHeight), (LastPoint.y + 1) * mLineHeight, tempPaint);
        }
    }

    /*
     * 绘制棋盘
     */
    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeight;

        for (int i = 0; i < MAX_LINE; i++) {
            int startX = (int) (lineHeight / 2);
            int endX = (int) (w - lineHeight / 2);
            int y = (int) ((0.5 + i) * lineHeight);
            canvas.drawLine(startX, y, endX, y, mPaint);
        }
        for (int i = 0; i < MAX_LINE; i++) {
            int startY = (int) (lineHeight / 2);
            int endY = (int) (w - lineHeight / 2);
            int x = (int) ((0.5 + i) * lineHeight);
            canvas.drawLine(x, startY, x, endY, mPaint);
        }
    }

    /*
     * 重新开一局游戏
     */
    public void start(boolean peopleOrAI) {
        mWhiteArray.clear();
        mBlackArray.clear();
        InitializeStateBoard();
        mIsGameOver = false;
        mIsWhiteWinner = false;
        mIsWhite = true;
        mIsAI = peopleOrAI;        //人机对战和人人对战切换
        invalidate();
    }

    /*
     * 后台运行的存储与恢复
     */
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAME_OVER = "instance_game_over";
    private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /*
     * 悔棋
     */
    public void regretLastStep() {
        if (mIsAI && mIsWhite) {
            if (mWhiteArray != null && !mWhiteArray.isEmpty()) {
                mWhiteArray.remove(mWhiteArray.size() - 1);
                if (mBlackArray != null && !mBlackArray.isEmpty() && mIsAI) {
                    mBlackArray.remove(mBlackArray.size() - 1);
                }
                if (mWhiteArray != null && !mWhiteArray.isEmpty()) {
                    LastPoint = mWhiteArray.get(mWhiteArray.size() - 1);
                }
            }
            invalidate();
        } else if (!mIsAI) {
            if (mIsWhite) {
                if (mBlackArray != null && !mBlackArray.isEmpty()) {
                    mBlackArray.remove(mBlackArray.size() - 1);
                    mIsWhite = !mIsWhite;
                    if (mWhiteArray != null && !mWhiteArray.isEmpty()) {
                        LastPoint = mWhiteArray.get(mWhiteArray.size() - 1);
                    }
                    invalidate();
                }
            } else {
                if (mWhiteArray != null && !mWhiteArray.isEmpty()) {
                    mWhiteArray.remove(mWhiteArray.size() - 1);
                    mIsWhite = !mIsWhite;
                    if (mBlackArray != null && !mBlackArray.isEmpty()) {
                        LastPoint = mBlackArray.get(mBlackArray.size() - 1);
                    }
                    invalidate();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends AsyncTask<Object, Object, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            invalidate();     //刷新？？没有这个是不行的
        }

        @Override
        protected Void doInBackground(Object... params) {
            Point point;
            if (mIsAI) {     //是人机作战
                if (!mIsWhite) {
                    for (Point p : mBlackArray) {        //遍历所有黑棋
                        int x = p.x;
                        int y = p.y;
                        StateBoard[x][y] = 1;
                    }
                    for (Point p : mWhiteArray) {        //遍历所有白棋
                        int x = p.x;
                        int y = p.y;
                        StateBoard[x][y] = 2;
                    }
                    aiPlayer.sortAlphaBeta(StateBoard);
                    point = aiPlayer.startSearch();
                    mBlackArray.add(point);
                    LastPoint = point;
                    mIsWhite = !mIsWhite;
                }
            }
            return null;
        }
    }
}
