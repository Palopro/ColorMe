package com.palopro.colorme.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.palopro.colorme.R;
import com.palopro.colorme.ui.DividerSeparator;
import com.palopro.colorme.ui.ListItem;
import com.palopro.colorme.ui.MainListRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.Fritz;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "d9b274bddd4d41ff970bc00c449de18d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fritz.configure(this, API_KEY);

        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.main_list_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Adding divider
        DividerSeparator dividerSeparator = new DividerSeparator(this, Color.GRAY, 1);
        recyclerView.addItemDecoration(dividerSeparator);

        // Adding adapter
        MainListRecyclerViewAdapter adapter = new MainListRecyclerViewAdapter(getListItems());
        recyclerView.setAdapter(adapter);
        recyclerView.setClickable(true);
    }


    private List<ListItem> getListItems() {
        List<ListItem> listItems = new ArrayList<>();

        listItems.add(new ListItem(
                getString(R.string.hair_color_title),
                getString(R.string.hair_color_description),
                v -> {
                    Context context = v.getContext();
                    Intent liveHairColorIntent = new Intent(context, LiveHairColorActivity.class);
                    context.startActivity(liveHairColorIntent);
                }
        ));

        listItems.add(new ListItem(
                getString(R.string.video_hair_color_title),
                getString(R.string.video_hair_color_description),
                v -> {
                    Context context = v.getContext();
                    Intent selectVideo = new Intent(context, VideoHairColorActivity.class);
                    context.startActivity(selectVideo);
                }
        ));

        return listItems;
    }
}
