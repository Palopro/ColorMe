package com.palopro.colorme.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.palopro.colorme.R;
import com.palopro.colorme.views.CameraConnectionFragment;
import com.palopro.colorme.views.CameraOpenListener;
import com.palopro.colorme.views.DrawCallback;
import com.palopro.colorme.views.OverlayView;

public abstract class BaseCameraActivity extends AppCompatActivity implements OnImageAvailableListener {
    private static final String TAG = BaseCameraActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST = 1;
    private static final long DELAY_OVERLAY_VISIBLE = 500;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private boolean useCamera2API;

    private boolean debug = false;

    private Handler handler;
    private HandlerThread handlerThread;

    protected String cameraId;
    protected int cameraFacingDirection = CameraCharacteristics.LENS_FACING_BACK;
    CameraConnectionFragment fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate " + this);
        super.onCreate(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }
    }

    @Override
    protected synchronized void onStart() {
        Log.d(TAG, "onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onPause() {
        Log.d(TAG, "onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            Log.e(TAG, "Exception!" + e);
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        Log.d(TAG, "onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        Log.d(TAG, "onDestroy " + this);
        super.onDestroy();
    }

    protected int getCameraFacingDirection() {
        return cameraFacingDirection;
    }

    public void setCameraFacingDirection(int cameraFacingDirection) {
        this.cameraFacingDirection = cameraFacingDirection;
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setFragment();
                } else {
                    requestPermission();
                }
            }
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                Toast.makeText(BaseCameraActivity.this, "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
        }
    }

    private void setFragment() {
        cameraId = chooseCamera();
        fragment = CameraConnectionFragment.newInstance(
                new CameraConnectionFragment.ConnectionCallback() {
                    @Override
                    public void onPreviewSizeChosen(Size size, Size cameraViewSize, int cameraRotation) {
                        BaseCameraActivity.this.onPreviewSizeChosen(size, cameraViewSize, cameraRotation);
                    }
                },
                this,
                getLayoutId(),
                getDesiredPreviewFrameSize(),
                new CameraOpenListener() {
                    @Override
                    public void onCameraOpened() {
                        final Handler handler = new Handler();
                        handler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                OverlayView overlayView = findViewById(R.id.overlay);
                                                if (overlayView != null) {
                                                    overlayView.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        });
                                    }
                                }, DELAY_OVERLAY_VISIBLE
                        );
                    }
                }
        );

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.camera_frame, fragment)
                .commit();

        fragment.setCamera(cameraId);
    }

    protected void toggleCameraFacingDirection() {
        final OverlayView overlayView = findViewById(R.id.overlay);
        if (overlayView != null) {
            overlayView.setVisibility(View.INVISIBLE);
        }

        if (cameraFacingDirection == CameraCharacteristics.LENS_FACING_FRONT) {
            cameraFacingDirection = CameraCharacteristics.LENS_FACING_BACK;
        } else {
            cameraFacingDirection = CameraCharacteristics.LENS_FACING_FRONT;
        }

        cameraId = chooseCamera();
        fragment.switchCamera(cameraId);
    }

    private String chooseCamera() {
        final CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : cameraManager.getCameraIdList()) {
                final CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

                final Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing != cameraFacingDirection) {
                    continue;
                }

                final StreamConfigurationMap map =
                        cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                        || isHardwareLevelSupported(cameraCharacteristics,
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                Log.i(TAG, "Camera API lv2?: " + useCamera2API);
                return cameraId;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Not allowed to access camera: " + e);
        }

        return null;
    }

    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    public boolean isDebug() {
        return debug;
    }

    public void requestRender() {
        final OverlayView overlay = (OverlayView) findViewById(R.id.overlay);
        if (overlay != null) {
            overlay.postInvalidate();
        }
    }

    public void setCallback(final DrawCallback callback) {
        final OverlayView overlay = (OverlayView) findViewById(R.id.overlay);
        if (overlay != null) {
            overlay.setCallback(callback);
        }
    }

    public void onSetDebug(final boolean debug) {
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            debug = !debug;
            requestRender();
            onSetDebug(debug);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected abstract void onPreviewSizeChosen(final Size previewSize, final Size cameraViewSize, final int rotation);

    protected abstract int getLayoutId();

    protected Size getDesiredPreviewFrameSize() {
        return new Size(640, 480);
    }
}
