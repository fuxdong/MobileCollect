package com.mixislink.socket;

import com.mixislink.util.ConvertUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 2018/4/3.
 */
public class SocketServerHandler implements Runnable {
    private Socket socket;

    public SocketServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        OutputStream os;
        if (socket != null) {
            try {
                os = socket.getOutputStream();
                //长度79
                os.write(ConvertUtils.hexStringToBytes("00000000004F01034C0000000000000198000700010000001000000000000000000000010000000000000000000000000000EA000C0B41003200010000000000000000000000020019000000000B41000100000000"));
                Thread.currentThread().sleep(1000);
                //长度35
                os.write(ConvertUtils.hexStringToBytes("00000000002301032013880001000200F2012C025A0043000000000007000000000549008007350561"));
                Thread.currentThread().sleep(1000);
                //长度9
                os.write(ConvertUtils.hexStringToBytes("00000000000B0103080000000B7FC00000"));
                Thread.currentThread().sleep(1000);
                //长度55
//                os.write(ConvertUtils.hexStringToBytes("00000000003701033400000FA50000104930303030303231303230373135313030000000000000000000000000000000000000000000000000000000A4"));
                os.flush();
                Thread.currentThread().sleep(1000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
