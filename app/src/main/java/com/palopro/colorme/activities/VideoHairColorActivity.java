package com.palopro.colorme.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.palopro.colorme.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;

import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.imagesegmentation.BlendMode;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationMaskOptions;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictorOptions;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.video.ExportVideoOptions;
import ai.fritz.vision.video.FritzVisionImageFilter;
import ai.fritz.vision.video.FritzVisionVideo;
import ai.fritz.vision.video.filters.imagesegmentation.MaskBlendCompoundFilter;

public class VideoHairColorActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final int HAIR_ALPHA = 180;
    private static final float HAIR_CONFIDENCE_THRESHOLD = .5f;

    private VideoView videoView;
    private ProgressBar progressBar;
    private TextView progressText;
    private MenuItem exportAction;

    private FritzVisionSegmentationPredictor hairPredictor;
    private FritzVisionSegmentationMaskOptions maskOptions;

    private File exportFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hair_color);

        videoView = findViewById(R.id.video_view);
        videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.start();
        });

        progressBar = findViewById(R.id.export_progress);
        progressText = findViewById(R.id.progress_text);

        FritzVisionSegmentationPredictorOptions options = new FritzVisionSegmentationPredictorOptions();
        options.confidenceThreshold = HAIR_CONFIDENCE_THRESHOLD;

        SegmentationOnDeviceModel segmentationOnDeviceModel = FritzVisionModels.getHairSegmentationOnDeviceModel(ModelVariant.FAST);

        hairPredictor = FritzVision.ImageSegmentation.getPredictor(segmentationOnDeviceModel, options);

        maskOptions = new FritzVisionSegmentationMaskOptions();
        maskOptions.maxAlpha = HAIR_ALPHA;

        Intent filePicker = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        filePicker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(filePicker, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu, menu);
        exportAction = menu.findItem(R.id.export_button);
        exportAction.setVisible(false);
        exportAction.setOnMenuItemClickListener(menuItem -> {
            try {
                saveProcessedVideo();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to save video.");
            }
            menuItem.setEnabled(false);
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void saveProcessedVideo() throws IOException {
        File file = getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        String exportPath = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp4");
        File destFile = new File(exportPath);

        try (FileChannel source = new FileInputStream(exportFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }

        Toast.makeText(VideoHairColorActivity.this, "Saved video to " + exportPath, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        File cacheDir = getCacheDir();
        File[] cachedFiles = cacheDir.listFiles();

        if (cachedFiles != null) {
            for (File file : cachedFiles) {
                file.delete();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startProcessing(data.getData());
        } else {
            finish();
        }
    }

    private void startProcessing(Uri videoUri) {
        try {
            exportFile = File.createTempFile("tempExport", ".mp4", getCacheDir());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create a destination file.");
        }

        FritzVisionImageFilter[] filters = {
                new MaskBlendCompoundFilter(hairPredictor, maskOptions, MaskClass.HAIR, BlendMode.SOFT_LIGHT)
        };

        FritzVisionVideo fritzVisionVideo = new FritzVisionVideo(videoUri, filters);

        ExportVideoOptions exportVideoOptions = new ExportVideoOptions();
        exportVideoOptions.copyAudio = true;
        exportVideoOptions.frameInterval = 2;

        final String exportPath = exportFile.getAbsolutePath();

        fritzVisionVideo.export(exportPath, exportVideoOptions, new FritzVisionVideo.ExportProgressCallback() {
            @Override
            public void onProgress(Float response) {
                int progress = (int) (response * 100);
                updateProgress(progress);
            }

            @Override
            public void onComplete() {
                hairPredictor.close();

                Uri uri = Uri.fromFile(exportFile);
                displayVideo(uri);
            }
        });
    }

    private void displayVideo(final Uri videoUri) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.INVISIBLE);
            progressText.setVisibility(View.INVISIBLE);
            exportAction.setVisible(true);
            videoView.setVideoURI(videoUri);
        });
    }

    private void updateProgress(final int progress) {
        runOnUiThread(() -> {
            progressBar.setProgress(progress);
            progressText.setText(String.format(Locale.US, "Processing %d%%", progress));
        });
    }
}