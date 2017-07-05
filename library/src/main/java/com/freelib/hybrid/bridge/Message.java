package com.freelib.hybrid.bridge;

import android.text.TextUtils;

import java.util.LinkedList;

/**
 * @author free46000  2017/06/02
 * @version v1.0
 */
public class Message {
    private static final int MAX_POOL_SIZE = 5;
    private static final Object poolSync = new Object();
    private static final LinkedList<Message> messagePool = new LinkedList<>();

    private String callBackId;
    private String responseId;
    private String handlerName;
    private String data;

    private Message() {
    }

    public static Message getMessage() {

        if (messagePool.isEmpty()) {
            return new Message();
        }

        synchronized (poolSync) {
            return messagePool.poll();
        }
    }

    public void recycle() {
        callBackId = null;
        responseId = null;
        handlerName = null;
        data = null;

        synchronized (poolSync) {
            if (messagePool.size() < MAX_POOL_SIZE) {
                messagePool.add(this);
            }
        }
    }

    public boolean isResponse() {
        return !TextUtils.isEmpty(responseId);
    }

    public boolean isNeedCallBack() {
        return !TextUtils.isEmpty(callBackId);
    }

    public String getCallBackId() {
        return callBackId;
    }

    public void setCallBackId(String callBackId) {
        this.callBackId = callBackId;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
