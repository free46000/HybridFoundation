package com.freelib.hybrid.bridge;

import android.text.TextUtils;

/**
 * @author free46000  2017/06/02
 * @version v1.0
 */
public class Message {
    private String callBackId;
    private String responseId;
    private String handlerName;
    private String data;

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
