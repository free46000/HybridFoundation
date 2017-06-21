package com.freelib.hybrid;

import com.freelib.hybrid.bridge.JsBridge;
import com.freelib.hybrid.bridge.MessageHandler;

/**
 * @author free46000  2017/06/08
 * @version v1.0
 */
public class JsApi {
    private JsBridge jsBridge;

    public JsApi(JsBridge jsBridge) {
        this.jsBridge = jsBridge;
    }

    public void registConnectBluetoothHandler(MessageHandler messageHandler) {
        jsBridge.registMessageHandler("connectBluetooth", messageHandler);
    }

}
