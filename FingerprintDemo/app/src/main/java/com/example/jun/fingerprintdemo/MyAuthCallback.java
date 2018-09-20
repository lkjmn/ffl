package com.example.jun.fingerprintdemo;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Created by baniel on 7/21/16.
 */
public class MyAuthCallback extends FingerprintManager.AuthenticationCallback{

    private Handler handler = null;

    public MyAuthCallback(Handler handler) {
        super();

        this.handler = handler;
    }
    //多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        super.onAuthenticationError(errMsgId, errString);
        if (handler != null) {
         //   handler.obtainMessage(MainActivity.MSG_AUTH_ERROR, errMsgId, 0).sendToTarget();
            Message message = handler.obtainMessage(MainActivity.MSG_AUTH_ERROR);
            message.arg1 = errMsgId;//传整型
            message.obj = errString;//传String等
            message.sendToTarget();
        }
    }
    //当指纹验证失败的时候回调此函数，失败之后允许多次尝试，失败次数过多会停止一段时间，然后再停止sensor工作
    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        super.onAuthenticationHelp(helpMsgId, helpString);
        if (handler != null) {
          //  handler.obtainMessage(MainActivity.MSG_AUTH_HELP, helpMsgId, 0).sendToTarget();
            Message message = handler.obtainMessage(MainActivity.MSG_AUTH_HELP);
            message.arg1 = helpMsgId;//传整型
            message.obj = helpString;//传String等
            message.sendToTarget();
        }
    }
    //当指纹验证成功后的时候回调此函数，然后再监听指纹sensor
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if (handler != null) {
          //  handler.obtainMessage(MainActivity.MSG_AUTH_SUCCESS).sendToTarget();
            Message message = handler.obtainMessage(MainActivity.MSG_AUTH_SUCCESS);
            message.sendToTarget();

        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();

        if (handler != null) {
            handler.obtainMessage(MainActivity.MSG_AUTH_FAILED).sendToTarget();

        }
    }
}