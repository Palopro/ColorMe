package com.palopro.colorme.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class OverlayView extends View {
    private DrawCallback drawCallback;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallback(DrawCallback drawCallback) {
        this.drawCallback = drawCallback;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawCallback != null) {
            drawCallback.drawCallback(canvas);
        }
    }
}
