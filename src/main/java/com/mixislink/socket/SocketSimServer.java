package com.mixislink.socket;

import com.mixislink.util.ConvertUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fuxudong on 2018/1/29.
 * 模拟socket服务器端程序
 */
public class SocketSimServer {
    static InputStream is = null;
    static OutputStream os = null;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("连接：" + socket.getInetAddress());
            new SocketSimServer().read();
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            SocketServerHandler handler = new SocketServerHandler(socket);
            service.scheduleAtFixedRate(handler, 0, 5, TimeUnit.SECONDS);
        }
    }

    public void read() throws IOException {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int data = 0;
                        if ((data = is.read()) != -1) {
                            byte[] head = new byte[6];
                            head[0] = (byte) data;
                            is.read(head, 1, 5);
                            int length = Integer.parseInt((Integer.toHexString(head[4])).concat(Integer.toHexString(head[5])), 16);
                            byte[] overData = new byte[length];
                            for (int i = 0; i < length; i++) {
                                overData[i] = (byte) is.read();
                            }
                            byte[] newData = new byte[head.length + overData.length];
                            System.arraycopy(head, 0, newData, 0, head.length);
                            System.arraycopy(overData, 0, newData, head.length, overData.length);
                            System.out.println(ConvertUtils.addSpace(ConvertUtils.toHexString(newData)));
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }.start();
    }
}
