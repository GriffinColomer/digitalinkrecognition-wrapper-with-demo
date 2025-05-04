package com.example.wrapper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Paint paint;
    private List<Path> paths = new ArrayList<>();
    private Path currentPath;
    private List<List<PointF>> strokes = new ArrayList<>();
    private List<PointF> currentStroke;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0xFF000000);
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public List<List<PointF>> getStrokes() {
        return strokes;
    }

    public void clear() {
        paths.clear();
        strokes.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Path path : paths) {
            canvas.drawPath(path, paint);
        }
        if (currentPath != null) {
            canvas.drawPath(currentPath, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                currentStroke = new ArrayList<>();
                currentStroke.add(new PointF(x, y));
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                currentStroke.add(new PointF(x, y));
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                currentPath.lineTo(x, y);
                currentStroke.add(new PointF(x, y));
                paths.add(currentPath);
                strokes.add(currentStroke);
                currentPath = null;
                currentStroke = null;
                invalidate();
                return true;
        }

        return false;
    }
}