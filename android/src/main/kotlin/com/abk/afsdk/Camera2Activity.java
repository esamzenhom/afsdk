package com.abk.afsdk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.abk.afsdk.camera2.Camera2Helper;
import com.abk.afsdk.camera2.Camera2Listener;
import com.abk.utils.LogUtil;
import com.huawei.hms.ml.scan.HmsScan;


import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Camera2Activity extends Activity implements Camera2Listener {
    private static final String TAG = "Camera2Activity";
    private static final int ACTION_REQUEST_PERMISSIONS = 1;
    public static final String BUNDLE_KEY_HW = "hms_scan";
    public static final String BUNDLE_KEY_AX = "anxin_scan";
    private Camera2Helper camera2Helper;
    TextureView textureView;

    // 显示的旋转角度
    private int displayOrientation;
    // 是否手动镜像预览
    private boolean isMirrorPreview;
    // 实际打开的cameraId
    private String openedCameraId;
    // 当前获取的帧数
    private int currentIndex = 0;
    // 处理的间隔帧
    private static final int PROCESS_INTERVAL = 30;
    // 线程池
    private ExecutorService imageProcessExecutor;
    // 默认打开的CAMERA
    public static final String CAMERA_ID = "camera_id";
    public static final String DECODE_LIB = "decode_lib";
    public static final String IS_FIXED_FOUCUS = "fixed_focus";
    // 需要的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };
    private static MyHandler handler;

    private static long startTime;
    public static final String SCAN_TIME = "time";
    public static final String SCAN_RESULT = "scanResult";

    private String cameraId = Camera2Helper.CAMERA_ID_BACK;
    private String decodeLib = Camera2Helper.DECODE_LIB_HW;
    private boolean isFixedFocus = false;
    Animation mTop2Bottom, mBottom2Top;
    private ImageView mIvScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        setContentView(R.layout.activity_camera2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        imageProcessExecutor = Executors.newSingleThreadExecutor();

        String decodeLib = getIntent().getStringExtra(Camera2Activity.DECODE_LIB);
        if (!TextUtils.isEmpty(decodeLib)) {
            this.decodeLib = decodeLib;
        }

        String cameraId = getIntent().getStringExtra(Camera2Activity.CAMERA_ID);
        if (!TextUtils.isEmpty(cameraId)) {
            this.cameraId = cameraId;
        }

        isFixedFocus = getIntent().getBooleanExtra(Camera2Activity.IS_FIXED_FOUCUS, false);
        initData();
        initAnimationView();
    }

    void initCamera() {
        camera2Helper = new Camera2Helper.Builder()
                .cameraListener(this)
//                .maxPreviewSize(new Point(1280, 720))
                .minPreviewSize(new Point(1280, 720))
                .specificCameraId(cameraId)
                .context(getApplicationContext())
                .previewOn(textureView)
                .previewViewSize(new Point(textureView.getWidth(), textureView.getHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .setDecodeLib(decodeLib)
                .setFixedFocus(isFixedFocus)
                .build();
        camera2Helper.start();
    }

    public void initData() {
        textureView = findViewById(R.id.texture_preview_camera);
        handler = new MyHandler(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initCamera();
        }
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                initCamera();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        if (camera2Helper != null) {
            camera2Helper.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera2Helper != null) {
            camera2Helper.start();
        }
    }

    @Override
    public void onCameraClosed() {
        Log.i(TAG, "onCameraClosed: ");
    }

    @Override
    public void onCameraError(Exception e) {
        e.printStackTrace();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIvScan.clearAnimation();
    }

    @Override
    protected void onDestroy() {
        if (imageProcessExecutor != null) {
            imageProcessExecutor.shutdown();
            imageProcessExecutor = null;
        }
        if (camera2Helper != null) {
            camera2Helper.release();
        }
        super.onDestroy();
    }

    @Override
    public void onPreview(byte[] y, byte[] u, byte[] v, Size previewSize, int stride) {

    }

    @Override
    public void onCameraOpened(CameraDevice cameraDevice, String cameraId, Size previewSize, int displayOrientation, boolean isMirror) {
        Log.i(TAG, "onCameraOpened:  previewSize = " + previewSize.getWidth() + "x" + previewSize.getHeight());
    }

    public void switchCamera(View view) {
        if (camera2Helper != null) {
            camera2Helper.switchCamera();
        }
    }


    private static class MyHandler extends Handler {
        private final WeakReference<Camera2Activity> camera2ActivityWeakReference;

        public MyHandler(Camera2Activity mineActivity) {
            camera2ActivityWeakReference = new WeakReference<>(mineActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Camera2Activity activity = camera2ActivityWeakReference.get();
            Bundle bundle = msg.getData();
            if (activity != null) {
                switch (msg.what) {
                    case 1000:
                        HmsScan hmsScan = bundle.getParcelable(BUNDLE_KEY_HW);
                        if (!TextUtils.isEmpty(hmsScan.getOriginalValue())) {
                            LogUtil.i(TAG, "----- 解析结果：" + hmsScan.getOriginalValue());
                            long endTime = System.currentTimeMillis() - startTime;
                            LogUtil.i(TAG, "----- 扫码时间花费：" + endTime);
                            Intent intent = new Intent();
                            intent.putExtra(SCAN_RESULT, hmsScan);
                            intent.putExtra(SCAN_TIME, endTime);
                            camera2ActivityWeakReference.get().setResult(RESULT_OK, intent);
                            camera2ActivityWeakReference.get().finish();
                        }
                        break;
                    case 2000:
                        String scancode = bundle.getString(BUNDLE_KEY_AX);
                        if (!TextUtils.isEmpty(scancode)) {
                            LogUtil.i(TAG, "----- 解析结果：" + scancode);
                            long endTime = System.currentTimeMillis() - startTime;
                            LogUtil.i(TAG, "----- 扫码时间花费：" + endTime);
                            Intent intent = new Intent();
                            intent.putExtra(SCAN_RESULT, scancode);
                            intent.putExtra(SCAN_TIME, endTime);
                            camera2ActivityWeakReference.get().setResult(RESULT_OK, intent);
                            camera2ActivityWeakReference.get().finish();
                        }
                        break;
                }
            }
        }
    }

    public static void send_message(Message message) {
        if (message != null) {
            handler.sendMessage(message);
        }
    }

    private void initAnimationView() {
        mIvScan = findViewById(R.id.scan_line);

        /**
         * x：从0点到0点
         * y:从20%到80%
         */
        mTop2Bottom = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);

        /**
         * x：从0点到0点
         * y:从80%到20%
         */
        mBottom2Top = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.1f);

        mBottom2Top.setRepeatMode(Animation.RESTART);
        mBottom2Top.setInterpolator(new LinearInterpolator());
        mBottom2Top.setDuration(1500);
        mBottom2Top.setFillEnabled(true);//使其可以填充效果从而不回到原地
        mBottom2Top.setFillAfter(true);//不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        mBottom2Top.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIvScan.startAnimation(mTop2Bottom);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mTop2Bottom.setRepeatMode(Animation.RESTART);
        mTop2Bottom.setInterpolator(new LinearInterpolator());
        mTop2Bottom.setDuration(2000);
        mTop2Bottom.setFillEnabled(true);
        mTop2Bottom.setFillAfter(true);
        mTop2Bottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIvScan.startAnimation(mTop2Bottom);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mIvScan.startAnimation(mTop2Bottom);
    }

}