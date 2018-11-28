package com.mixislink.socket;

import com.mixislink.modbus.ModbusConstants;
import com.mixislink.modbus.ModbusTcpDataAnalyse;
import com.mixislink.util.ConvertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/4/3.
 */
public class Socketer {
    private Socket socket = null;
    private boolean connSuccess = false;
    private static Socketer socketer = null;
    private String host = null;
    private int port = 0;
    private final Log log = LogFactory.getLog(Socketer.class);
    private ScheduledExecutorService service = null;

    private Socketer() {
        socket = null;
        connSuccess = false;
    }

    public static Socketer init(String host, int port) {
        if (socketer == null) {
            socketer = new Socketer();
            socketer.host = host;
            socketer.port = port;
            socketer.service = Executors.newSingleThreadScheduledExecutor();
            socketer.socketConnection();
        }

        return socketer;
    }

    /**
     * 判断socket连接状态
     * 如果未断开，获取输出流执行发送
     *
     * @param buffer
     */
    public void sendRequest(byte[] buffer) {
        if (connSuccess && socket != null) {
            OutputStream os = null;
            try {
                os = socket.getOutputStream();
                os.write(buffer, 0, (buffer[5] + 6) & 0XFF);
                os.flush();
                log.info("客户端发送的协议报文：" + ConvertUtils.byteArrToHexStr(buffer, 0, (buffer[5] + 6) & 0XFF));
            } catch (IOException e) {
                log.error("往服务器端发送数据出错：", e);
                cleanCurrentSocket();
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e1) {
                        log.error("关闭os输出流出错", e1);
                    }
                }
            }
        } else {
            cleanCurrentSocket();
        }
    }

    public boolean readData(byte[] buffer) {

        if (isConnSuccess()) {
            InputStream in = null;
            try {
                if (socket != null) {
                    in = socket.getInputStream();
                    //循环读取输入流中的数据
                    int data;
                    if ((data = in.read()) != -1) {
                        buffer[0] = (byte) data;
                        in.read(buffer, 1, 5);
                        in.read(buffer, 6, buffer[5] & 0XFF);
                        return true;
                    } else {
                        log.info("server sent data end...");
                        return false;
                    }
                } else {
                    log.info("socket未连接，不能开始接收数据，稍后重试...");
                    cleanCurrentSocket();
                    return false;
                }
            } catch (IOException e) {
                log.error("读输入流数据出错", e);
                cleanCurrentSocket();
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return false;
            }
        } else {
            log.info("socket未连接，不能开始接收数据，稍后重试...");
            return false;
        }
    }


    /**
     * 异常时，关闭socket
     */
    public void cleanCurrentSocket() {
        connSuccess = false;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e1) {
                log.error("关闭socket出错", e1);
            }
            socket = null;
        }
    }


    /**
     * 定时检查socket连接状态
     */
    private void socketConnection() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!connSuccess) {
                    try {
                        socket = new Socket(host, port);
                        log.info("重新连接到服务器，ip：" + host);
                        connSuccess = true;
                    } catch (IOException e) {
                        log.info("重新连接服务器出错，稍后重试...");
                        cleanCurrentSocket();
                    }
                }
            }
        };
        service.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
    }


    public boolean isConnSuccess() {
        return connSuccess;
    }
}
