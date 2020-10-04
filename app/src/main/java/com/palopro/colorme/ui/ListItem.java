package com.palopro.colorme.ui;

import android.view.View;

public class ListItem {
    private String title;
    private String description;
    private View.OnClickListener onClickListener;

    public ListItem(String title, String description, View.OnClickListener onClickListener) {
        this.title = title;
        this.description = description;
        this.onClickListener = onClickListener;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
