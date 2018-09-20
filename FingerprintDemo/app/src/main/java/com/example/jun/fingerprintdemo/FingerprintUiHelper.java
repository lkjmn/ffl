package com.example.jun.fingerprintdemo;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
public class FingerprintUiHelper  {
    private static final int NONE = 0;
    private int mState = NONE;
    private static final int CANCEL = 1;
    private static final int AUTHENTICATING = 2;
    private  FingerprintManager mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private CryptoObjectCreator mCryptoObjectCreator;
    private WeakReference<IFingerprintResultListener> mFpResultListener;
    private int mFailedTimes = 0;
    /**
     * Constructor for {@link FingerprintUiHelper}.
     */
    FingerprintUiHelper(FingerprintManager fingerprintManager
                        ) {
        mFingerprintManager = fingerprintManager;
        initCryptoObject();
    }
    /**
     * 指纹识别回调接口
     */
    public interface IFingerprintResultListener {
        /** 指纹识别成功 */
        void onAuthenticateSuccess();

        /** 指纹识别失败 */
        void onAuthenticateFailed(int helpId);

        /** 指纹识别发生错误-不可短暂恢复 */
        void onAuthenticateError(int errMsgId);

        /** 开始指纹识别监听成功 */
        void onStartAuthenticateResult(boolean isSuccess);
    }

    private FingerprintManager.AuthenticationCallback mAuthCallback;
    private void prepareData() {
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        if (mAuthCallback == null) {
            mAuthCallback = new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    // 多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证,一般间隔从几秒到几十秒不等
                    // 这种情况不建议重试，建议提示用户用其他的方式解锁或者认证
                    mState = NONE;
                    if (null != mFpResultListener && null != mFpResultListener.get()) {
                        mFpResultListener.get().onAuthenticateError(errMsgId);
                    }
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    mState = NONE;
                   // 建议根据参数helpString返回值，并且仅针对特定的机型做处理，并不能保证所有厂商返回的状态一致
                   // notifyAuthenticationFailed(helpMsgId , helpString.toString());
                   // onFailedRetry(helpMsgId, helpString.toString());
                }

                @Override
                public void onAuthenticationFailed() {
                    mState = NONE;
                  //  notifyAuthenticationFailed(0 , "onAuthenticationFailed");
                 //   onFailedRetry(-1, "onAuthenticationFailed");
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    mState = NONE;
                    mFailedTimes = 0;
                    if (null != mFpResultListener && null != mFpResultListener.get()) {
                        mFpResultListener.get().onAuthenticateSuccess();
                    }
                }
            };
        }
    }
    public void setFingerprintManager(IFingerprintResultListener fingerprintResultListener) {
        mFpResultListener = new WeakReference<IFingerprintResultListener>(fingerprintResultListener);
    }
    private void onFailedRetry(int msgId, String helpString) {
        mFailedTimes++;
        if (mFailedTimes > 5) { // 每个验证流程最多重试5次，这个根据使用场景而定，验证成功时清0
            FPLog.log("on failed retry time more than 5 times");
            return;
        }
        cancelAuthenticate();
//        mHandler.removeCallbacks(mFailedRetryRunnable);
//        mHandler.postDelayed(mFailedRetryRunnable, 300); // 每次重试间隔一会儿再启动
    }
    private void startAuthenticate(FingerprintManager.CryptoObject cryptoObject) {
        prepareData();
        mState = AUTHENTICATING;
        try {
            mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0, mAuthCallback, null);
            notifyStartAuthenticateResult(true, "");
        } catch (SecurityException e) {
            try {
                mFingerprintManager.authenticate(null, mCancellationSignal, 0, mAuthCallback, null);
                notifyStartAuthenticateResult(true, "");
            } catch (SecurityException e2) {
                notifyStartAuthenticateResult(false, Log.getStackTraceString(e2));
            } catch (Throwable throwable) {

            }
        } catch (Throwable throwable) {

        }
    }
    private void notifyStartAuthenticateResult(boolean isSuccess, String exceptionMsg) {
        if (isSuccess) {
            if (mFpResultListener.get() != null) {
                mFpResultListener.get().onStartAuthenticateResult(true);
            }
        } else {
            if (mFpResultListener.get() != null) {
                mFpResultListener.get().onStartAuthenticateResult(false);
            }
        }
    }

    public boolean isFingerprintAuthAvailable() {
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        return mFingerprintManager.isHardwareDetected();
    }
    public void cancelAuthenticate() {
        if (mCancellationSignal != null && mState != CANCEL) {
            mState = CANCEL;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    private void initCryptoObject() {
        try {
            mCryptoObjectCreator = new CryptoObjectCreator(new CryptoObjectCreator.ICryptoObjectCreateListener() {
                @Override
                public void onDataPrepared(FingerprintManager.CryptoObject cryptoObject) {
                    // startAuthenticate(cryptoObject);
                    // 如果需要一开始就进行指纹识别，可以在秘钥数据创建之后就启动指纹认证
                }
            });
        } catch (Throwable throwable) {
        }
    }
    public void onDestroy() {
        cancelAuthenticate();
        mAuthCallback = null;
        mFpResultListener = null;
        mCancellationSignal = null;
        mFingerprintManager = null;
        if (mCryptoObjectCreator != null) {
            mCryptoObjectCreator.onDestroy();
            mCryptoObjectCreator = null;
        }
    }
}
