package com.marrsattax.capacitorudpbroadcast;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@CapacitorPlugin(name = "CapacitorUdpBroadcast")
public class CapacitorUdpBroadcastPlugin extends Plugin {
    private DatagramSocket datagramSocket;
    private MulticastSocket multicastSocket;
    private SocketListener socketListener;

    @PluginMethod
    public void createSocket(PluginCall call) {

        boolean isMulticast = call.getBoolean("isMulticast", false);
        try {
            if (isMulticast) {
                System.out.println("yes is multicast" );
                if (call.getData().has("port")) {
                    multicastSocket = new MulticastSocket(call.getInt("port", 0));
                } else {
                    call.reject("Please supply a port");
                }

                if (call.getData().has("group")) {
                    String groupAddress = call.getString("group");
                    System.out.println("Join group: " + groupAddress);
                    InetAddress group = InetAddress.getByName(groupAddress);
                    multicastSocket.joinGroup(group);                
                } else {
                    call.reject("Please supply a group");
                }

            } else {
                System.out.println("not multicast" );
                if (call.getData().has("port")) {  
                    datagramSocket = new DatagramSocket(call.getInt("port", 0));
                } else {
                    call.reject("Please supply a port");
                }
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Error creating socket: " + e.getMessage());
        }
    }


    @PluginMethod
    public void listen(PluginCall call) {
        if (socketListener == null) {
            socketListener = new SocketListener();
            socketListener.start();
            call.resolve();
        } else {
            call.reject("Socket listener is already running");
        }
    }

    @PluginMethod
    public void sendMessage(PluginCall call) {
        String message = call.getString("message");
        String address = call.getString("address");
        int port = call.getInt("port", 0);

        try {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet;
            if (multicastSocket != null) {
                packet = new DatagramPacket(data, data.length, InetAddress.getByName(address), port);
                multicastSocket.send(packet);
            } else if (datagramSocket != null) {
                packet = new DatagramPacket(data, data.length, InetAddress.getByName(address), port);
                datagramSocket.send(packet);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Error sending packet: " + e.getMessage());
        }
    }

    @PluginMethod
    public void close(PluginCall call) {
        
        if (multicastSocket != null) {
            multicastSocket.close();
            multicastSocket = null;
        } else if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }

        if (socketListener != null) {
            socketListener.closeSocket(); // Close the socket listener
            socketListener.interrupt();
            socketListener = null;
        }
        call.resolve();
    }

    private class SocketListener extends Thread {
        private volatile boolean isSocketOpen = true;

        public void closeSocket() {
            isSocketOpen = false;
        }

        public void run() {
            byte[] data = new byte[20480];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            while (!isInterrupted() && isSocketOpen) {
                try {
                    if (multicastSocket != null) {
                        multicastSocket.receive(packet);    
                    } else if (datagramSocket != null) {
                        datagramSocket.receive(packet);
                    }
                    
                    // Process received packet
                    String message = new String(data, 0, packet.getLength(), StandardCharsets.UTF_8);
                    String address = packet.getAddress().getHostAddress();
                    int port = packet.getPort();

                    JSObject result = new JSObject();
                    result.put("message", message);
                    result.put("address", address);
                    result.put("port", port);

                    notifyListeners("messageReceived", result);
                } catch (IOException e) {
                    // Handle or log the exception
                    e.printStackTrace();
                }
            }
        }
    }

}
