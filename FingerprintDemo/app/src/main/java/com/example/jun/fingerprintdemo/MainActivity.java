package com.example.jun.fingerprintdemo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    FingerprintManager fingerprintManager;
    @RequiresApi(api = Build.VERSION_CODES.M)
    KeyguardManager keyguardManager;
    private FingerprintUiHelper mFingerprintUiHelper;
    private static final String ACTION_SETTING = "android.settings.SETTINGS";
    private KeyguardLockScreenManager mKeyguardLockScreenManager;
    private ImageView mFingerGuideImg;
    private TextView mFingerGuideTxt;
    private MyAuthCallback myAuthCallback = null;
    private Handler handler = null;
    public static final int MSG_AUTH_SUCCESS = 100;
    public static final int MSG_AUTH_FAILED = 101;
    public static final int MSG_AUTH_ERROR = 102;
    public static final int MSG_AUTH_HELP = 103;
    private CancellationSignal mCancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        keyguardManager = getSystemService(KeyguardManager.class);
        fingerprintManager = getSystemService(FingerprintManager.class);
        mFingerprintUiHelper = new FingerprintUiHelper(
                getSystemService(FingerprintManager.class)
        );
        mKeyguardLockScreenManager = new KeyguardLockScreenManager(this);
        mFingerGuideImg = (ImageView) findViewById(R.id.fingerprint_guide);
        mFingerGuideTxt = (TextView) findViewById(R.id.fingerprint_guide_tip);
        mFingerprintUiHelper.setFingerprintManager(mResultListener);
        initViewListeners();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Log.d(TAG, "msg: " + msg.what + " ,arg1: " + msg.arg1);
                switch (msg.what) {
                    case MSG_AUTH_SUCCESS:
                        Toast.makeText(
                                MainActivity.this,
                                "指纹识别成功",
                                Toast.LENGTH_LONG).show();
                        break;
                    case MSG_AUTH_FAILED:
                        Toast.makeText(
                                MainActivity.this,
                                "指纹 验证失败",
                                Toast.LENGTH_LONG).show();
                        break;
                    case MSG_AUTH_ERROR:
                      //  handleErrorCode(msg.arg1);
                        String errString = (String) msg.obj;
                        Toast.makeText(
                                MainActivity.this,
                                errString,
                                Toast.LENGTH_LONG).show();
                        break;
                    case MSG_AUTH_HELP:
                      //  handleHelpCode(msg.arg1);
                        String helpString = (String) msg.obj;
                        Toast.makeText(
                                MainActivity.this,
                                helpString,
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

    }
//    private void handleHelpCode(int code) {
//        String msg="";
//        switch (code) {
//
//            case FingerprintManager.FINGERPRINT_ACQUIRED_GOOD:
//                 msg="指纹识别成功";
//                break;
//            case FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
//                msg=getString(
//                        com.android.internal.R.string.fingerprint_acquired_imager_dirty);
//                break;
//            case FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT:
//msg=getString(
//        com.android.internal.R.string.fingerprint_acquired_insufficient);
//                break;
//            case FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL:
//msg=getString(
//        com.android.internal.R.string.fingerprint_acquired_partial);
//                break;
//            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST:
//                msg=getString(
//                        com.android.internal.R.string.fingerprint_acquired_too_fast);
//                break;
//            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW:
//                msg=getString(
//                        com.android.internal.R.string.fingerprint_acquired_too_slow);
//                break;
//        }
//        Toast.makeText(
//                MainActivity.this,
//                msg,
//                Toast.LENGTH_LONG).show();
//    }
//    private void handleErrorCode(int code) {
//        String msg="";
//        switch (code) {
//            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
//                msg=getString(com.android.internal.R.string.fingerprint_error_canceled);
//                break;
//            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
//                msg=getString(
//                        com.android.internal.R.string.fingerprint_error_hw_not_available);
//                break;
//            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
//                msg=getString(com.android.internal.R.string.fingerprint_error_lockout);
//                break;
//            case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
//                msg=getString(
//                        com.android.internal.R.string.fingerprint_error_no_space);
//                break;
//            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
//                msg=getString(com.android.internal.R.string.fingerprint_error_timeout);
//                break;
//            case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
//                msg=getString(com.android.internal.R.string.fingerprint_error_unable_to_process);
//                break;
//        }
//        Toast.makeText(
//                MainActivity.this,
//                msg,
//                Toast.LENGTH_LONG).show();
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivity(intent);
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
    private void initViewListeners() {
        findViewById(R.id.fingerprint_recognition_start).setOnClickListener(this);
        findViewById(R.id.fingerprint_recognition_cancel).setOnClickListener(this);
        findViewById(R.id.fingerprint_recognition_sys_unlock).setOnClickListener(this);
        findViewById(R.id.fingerprint_recognition_sys_setting).setOnClickListener(this);
    }

    private FingerprintUiHelper.IFingerprintResultListener mResultListener = new FingerprintUiHelper.IFingerprintResultListener() {
        @Override
        public void onAuthenticateSuccess() {
            Toast.makeText(
                    MainActivity.this,
                    "指纹识别成功",
                    Toast.LENGTH_LONG).show();
            resetGuideViewState();
        }

        @Override
        public void onAuthenticateFailed(int helpId) {
            Toast.makeText(
                    MainActivity.this,
                    "指纹识别失败，请重试",
                    Toast.LENGTH_LONG).show();
            mFingerGuideTxt.setText("指纹识别失败，请重试！");
        }

        @Override
        public void onAuthenticateError(int errMsgId) {
            resetGuideViewState();
            Toast.makeText(
                    MainActivity.this,
                    "指纹识别错误，等待几秒之后再重试",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStartAuthenticateResult(boolean isSuccess) {

        }
    };
    private void resetGuideViewState() {
        mFingerGuideTxt.setText(R.string.fingerprint_recognition_guide_tip);
        mFingerGuideImg.setBackgroundResource(R.drawable.fingerprint_normal);
    }
    /**
     * 开始指纹识别
     */
    private void startFingerprintRecognition() {
        if (!fingerprintManager.isHardwareDetected()) {
            Toast.makeText(this, "Your device doesn't support fingerprint authentication", Toast.LENGTH_LONG).show();
            openFingerPrintSettingPage(this);
        }  else if (!fingerprintManager.hasEnrolledFingerprints()) {

            Toast.makeText(
                    this,
                    "No fingerprint configured. Please register at least one fingerprint in your device's Settings",
                    Toast.LENGTH_LONG).show();
            openFingerPrintSettingPage(this);
        } else if (!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(
                    this,
                    "Please enable lock screen security in your device's Settings",
                    Toast.LENGTH_LONG).show();
            openFingerPrintSettingPage(this);
        }else{
            // start fingerprint auth here.
            try {
                myAuthCallback = new MyAuthCallback(handler);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                CryptoObjectHelper cryptoObjectHelper = new CryptoObjectHelper();
                if (mCancellationSignal == null) {
                    mCancellationSignal = new CancellationSignal();
                }
                fingerprintManager.authenticate(cryptoObjectHelper.buildCryptoObject(), mCancellationSignal, 0, myAuthCallback, null);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Fingerprint init failed! Try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static void openFingerPrintSettingPage(Context context) {
        Intent intent = new Intent(ACTION_SETTING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    //方法：控件View的点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fingerprint_recognition_start:
                startFingerprintRecognition();
                break;
            case R.id.fingerprint_recognition_cancel:
                cancelFingerprintRecognition();
//                   case R.id.btn2:
//                        Toast.makeText(MainActivity.this, "btn2", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fingerprint_recognition_sys_unlock:
                startFingerprintRecognitionUnlockScreen();
                break;
            case R.id.fingerprint_recognition_sys_setting:
                openFingerPrintSettingPage(this);
                break;
        }
    }

    private void cancelFingerprintRecognition() {
       // mFingerprintUiHelper.cancelAuthenticate();
        mCancellationSignal.cancel();
        mCancellationSignal = null;
    }

    private void startFingerprintRecognitionUnlockScreen() {
        if (mKeyguardLockScreenManager == null) {
            return;
        }
        if (!mKeyguardLockScreenManager.isOpenLockScreenPwd()) {

            Toast.makeText(this,
                    "系统没有设置锁屏密码，请设置！",
                    Toast.LENGTH_LONG).show();
            openFingerPrintSettingPage(this);
            return;
        }
        mKeyguardLockScreenManager.showAuthenticationScreen(this);
    }

}
