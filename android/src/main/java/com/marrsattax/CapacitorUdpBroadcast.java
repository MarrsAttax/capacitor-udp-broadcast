package com.marrsattax.capacitorudpbroadcast;

import com.getcapacitor.JSObject;

public interface CapacitorUdpBroadcast {
    void createSocket(JSObject options, Callback callback);    
    void listen(Callback callback);
    void sendMessage(String message, String address, int port, Callback callback);
    void close(Callback callback);

    interface Callback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
