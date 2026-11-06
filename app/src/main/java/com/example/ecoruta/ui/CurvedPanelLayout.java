package com.example.ecoruta.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.content.res.TypedArray;
import android.widget.FrameLayout;

import com.example.ecoruta.R;

public class CurvedPanelLayout extends FrameLayout {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public CurvedPanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CurvedPanelLayout);
        paint.setColor(attributes.getColor(R.styleable.CurvedPanelLayout_panelColor,
                getResources().getColor(R.color.eco_navy, context.getTheme())));
        attributes.recycle();
        setWillNotDraw(false);
        setClipToPadding(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float edge = getResources().getDisplayMetrics().density * 30f;
        float lift = getResources().getDisplayMetrics().density * 18f;
        path.reset();
        path.moveTo(0f, edge);
        path.quadTo(width / 2f, -lift, width, edge);
        path.lineTo(width, height);
        path.lineTo(0f, height);
        path.close();
        canvas.drawPath(path, paint);
    }
}
