package com.freelib.hybrid.bridge;

/**
 * @author weizx  2017/06/06
 * @version v1.0
 */
public interface MessageHandler {

    void handler(Message message, CallBack responseCallBack);

}
