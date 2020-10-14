package com.palopro.colorme.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class AutoFixTextureView extends TextureView {
    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public AutoFixTextureView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoFixTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFixTextureView(final Context context) {
        this(context, null);
    }

    public void setAspectRatio(final int width, final int height) {
        if (width < 0 || height < 0) {
            throw  new IllegalArgumentException("Size cannot be negative");
        }
        ratioWidth = width;
        ratioHeight = height;
        requestLayout();
    }
}
