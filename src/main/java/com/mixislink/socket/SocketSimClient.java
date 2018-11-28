package com.mixislink.socket;

import com.mixislink.modbus.*;
import com.mixislink.rabbitTask.ConsumeTask;
import com.mixislink.rabbitTask.SocketTask;
import com.mixislink.util.RabbitMq;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Fuxudong on 2018/1/29.
 * 模拟socket客户端程序
 */
public class SocketSimClient {

    private static final Log log = LogFactory.getLog(SocketSimClient.class);
    public static int deviceSerial = 0;
    public static int deviceAddr;
    private static String host;
    private static int port = 0;
    private static boolean isSimulate = false;

    public static void main(String[] args) {
        //初始化配置
        Init.start();

        for (int i = 0; i < args.length; i++) {
            if (args[i].contains("=")) {
                String[] key = args[i].split("=");
                String name = key[0].toLowerCase();
                String value = key[1];
                if ("host".equals(name)) {
                    if (value.matches("[\\d]{1,3}[.][\\d]{1,3}[.][\\d]{1,3}[.][\\d]{1,3}")) {
                        host = value;
                    } else if (value.matches("[\\d]{1,3}[.][\\d]{1,3}[.][\\d]{1,3}[.][\\d]{1,3}:[\\d]+")) {
                        host = value.split(":")[0];
                        port = Integer.parseInt(value.split(":")[1]);
                    } else {
                        log.error("IP格式错误");
                        System.exit(-1);
                    }
                } else if ("port".equals(name)) {
                    if (value != null && !"".equals(value)) {
                        port = Integer.parseInt(value);
                    }
                } else if ("serial".equals(name)) {
                    if (value != null && !"".equals(value)) {
                        deviceSerial = Integer.parseInt(value);
                    }
                } else if ("simulate".equals(name)) {
                    if (value != null && !"".equals(value)) {
                        isSimulate = Boolean.parseBoolean(value);
                    }
                }
            }
        }

        //获取充电桩号
        deviceSerial = (deviceSerial == 0 ? Integer.parseInt(Init.modbusProp.getProperty("DEVICE_SERIAL")) : deviceSerial);
        //获取设备地址
        deviceAddr = Integer.parseInt(Init.modbusProp.getProperty("DEVICE_ADDR"));
        //获取服务器IP
        host = ("".equals(host) ? Init.modbusProp.getProperty("HOST") : host);
        //获取端口号
        port = (port == 0 ? Integer.parseInt(Init.modbusProp.getProperty("PORT")) : port);

        /**
         * 根据传入的参数，判断是模拟环境还是真实采集环境
         */
        if (!isSimulate) {
            Queue<Map> dataQueue = new LinkedBlockingQueue<>();
            Socketer socketer = Socketer.init(host, port);
            RabbitMq mq = RabbitMq.init();
            SocketTask task = new SocketTask(dataQueue, socketer, mq);
            task.receiveData();

            task.sendDataToMQ();
            ConsumeTask consumeTask = new ConsumeTask(deviceSerial, mq, socketer);
            Thread thread = new Thread(consumeTask);
            thread.start();
            task.sendCommandToServer();
        } else {
            RabbitMq mq = RabbitMq.init();
            SocketTask task = new SocketTask(mq);
            task.sendSimulateData();
        }
    }
}