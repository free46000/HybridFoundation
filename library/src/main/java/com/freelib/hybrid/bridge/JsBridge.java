package com.freelib.hybrid.bridge;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author free46000  2017/06/02
 * @version v1.0
 */
public class JsBridge {
    private long uniqueId = 1;
    private ConcurrentHashMap<String, WeakReference<CallBack>> callBackMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, WeakReference<MessageHandler>> handlerMap = new ConcurrentHashMap<>();

    protected WebView webView;

    public JsBridge(WebView webView) {
        this.webView = webView;
    }

    public void registMessageHandler(@NonNull String handlerName, @NonNull MessageHandler messageHandler) {
        handlerMap.put(handlerName, new WeakReference<>(messageHandler));
    }

    public void unRegistMessageHandler(@NonNull String handlerName) {
        handlerMap.remove(handlerName);
    }


    public void sendMessage(@NonNull Message message) {
        sendMessage(message, null);
    }

    public void sendMessage(@NonNull Message message, CallBack callBack) {
        if (callBack != null) {
            message.setCallBackId("callBack_" + uniqueId++ + '_' + message.getHandlerName());
            callBackMap.put(message.getCallBackId(), new WeakReference<>(callBack));
        }

        JsMethod.execJs(webView, message);

        message.recycle();
    }

    /**
     * @param urlStr url字符串
     * @return 是否处理此消息
     */
    public boolean receiveUrl(@NonNull String urlStr) {

        Uri uri = Uri.parse(urlStr);
        if (uri == null) {
            return false;
        }

        if (!Const.PROTOCOL_SCHEME_NAME.equals(uri.getScheme())) {
            return false;
        }

        String param = uri.getQueryParameter(Const.PARAMETER_NAME);
        Message message = new Gson().fromJson(param, Message.class);

        if (message == null) {
            return true;
        }

        if (message.isResponse()) {
            onMessageCallBack(message);
            return true;
        }

        handlerMessage(message);
        return true;
    }

    private void handlerMessage(@NonNull Message message) {
        String handlerName = message.getHandlerName();

        if (TextUtils.isEmpty(handlerName)) {
            return;
        }

        WeakReference<MessageHandler> handlerReference = handlerMap.get(handlerName);

        if (handlerReference == null) {
            return;
        }

        MessageHandler messageHandler = handlerReference.get();

        if (messageHandler == null) {
            return;
        }

        CallBack callBack;
        if (message.isNeedCallBack()) {
            callBack = buildResponseCallBack(message.getCallBackId());
        } else {
            //不需要callback的创建空的callback，防止在使用callBack时抛出空指针
            callBack = buildEmptyResponseCallBack();
        }

        messageHandler.handler(message, callBack);
    }

    public CallBack buildResponseCallBack(@NonNull final String callBackId) {
        if (TextUtils.isEmpty(callBackId)) {
            return null;
        }

        return responseMessage -> {
            responseMessage.setResponseId(callBackId);
            sendMessage(responseMessage);
        };
    }

    private CallBack buildEmptyResponseCallBack() {
        return responseMessage -> Log.e(Const.LOG_TAG, "本消息不需要给js回调结果");
    }


    public void onMessageCallBack(@NonNull Message message) {
        String responseId = message.getResponseId();

        if (TextUtils.isEmpty(responseId)) {
            return;
        }

        WeakReference<CallBack> callBackReference = callBackMap.get(responseId);

        if (callBackReference == null) {
            return;
        }

        callBackMap.remove(responseId);
        CallBack callBack = callBackReference.get();

        if (callBack == null) {
            return;
        }

        callBack.onMessageCallBack(message);
    }

}
