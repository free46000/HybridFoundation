package com.freelib.hybrid.bridge;

import android.os.Build;
import android.os.Looper;
import android.webkit.WebView;

import com.google.gson.Gson;

/**
 * @author weizx  2017/06/02
 * @version v1.0
 */
public class JsMethod {
    private static final String EXEC_JS_FORMAT_VALUE = "javascript:%s('%s');";


    public static boolean execJs(WebView webView, Message message) {
        String messageStr = new Gson().toJson(message);
        String jsCode = String.format(EXEC_JS_FORMAT_VALUE, Const.RECEIVE_MESSAGE_METHOD, messageStr);

        execJs(webView, jsCode);
        return true;
    }


    private static void execJs(WebView webView, String jsCode) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            webView.post(() -> execJs(webView, jsCode));
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(jsCode, null);
        } else {
            webView.loadUrl(jsCode);
        }

    }


}
