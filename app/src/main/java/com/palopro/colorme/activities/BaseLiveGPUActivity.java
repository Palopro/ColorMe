package com.palopro.colorme.activities;

import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Size;
import android.widget.Button;
import android.widget.ImageButton;

import com.palopro.colorme.R;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.vision.FritzSurfaceView;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionOrientation;
import ai.fritz.vision.ImageOrientation;

public abstract class BaseLiveGPUActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {

    private static final String TAG = BaseLiveGPUActivity.class.getSimpleName();
    private final AtomicBoolean computing = new AtomicBoolean(false);

    private ImageOrientation orientation;
    protected Button chooseModelBtn;
    protected ImageButton cameraSwitchBtn;
    protected FritzVisionImage fritzVisionImage;
    protected FritzSurfaceView fritzSurfaceView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final Size cameraSize, final int rotation) {
        orientation = FritzVisionOrientation.getImageOrientationFromCamera(this, cameraId);
        chooseModelBtn = findViewById(R.id.choose_model_button);
        cameraSwitchBtn = findViewById(R.id.camera_switch_button);
        fritzSurfaceView = findViewById(R.id.gpu_image_view);

        cameraSwitchBtn.setOnClickListener(view -> toggleCameraFacingDirection());

    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        final Image image = reader.acquireLatestImage();

        if (image == null) {
            return;
        }

        if (!computing.compareAndSet(false, true)) {
            image.close();
            return;
        }
        fritzVisionImage = FritzVisionImage.fromMediaImage(image, orientation);
        runInference(fritzVisionImage);
        image.close();
        computing.set(false);
    }

    protected abstract int getLayoutId();

    @Override
    public void onSetDebug(final boolean debug) {

    }

    protected abstract void runInference(FritzVisionImage fritzVisionImage);
}
