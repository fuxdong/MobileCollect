package com.mixislink.rabbitTask;

import com.mixislink.modbus.ModbusPdu;
import com.mixislink.modbus.ModbusTcpDataGenerate;
import com.mixislink.socket.SocketSimClient;
import com.mixislink.socket.Socketer;
import com.mixislink.util.RabbitMq;
import com.rabbitmq.client.*;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Fuxudong on 2018/4/3.
 * 接收中心程序发送的消息
 * 控制充电、放电的停止与启动
 */
public class ConsumeTask implements Runnable {
    private Log log = LogFactory.getLog(ConsumeTask.class);
    private Connection conn = null;
    private Channel channel = null;
    private String EXCHANGE_NAME = "START_DATA";
    private RabbitMq mq;
    private int deviceSerial;
    private Socketer socketer;

    public ConsumeTask(int deviceSerial, RabbitMq mq, Socketer socketer) {
        this.mq = mq;
        this.deviceSerial = deviceSerial;
        this.socketer = socketer;
    }

    private Channel getChannel() {
        try {
            if (conn == null) {
                conn = mq.newConnection();
            }
            if (conn != null && channel == null) {
                channel = conn.createChannel();
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        } catch (TimeoutException e) {
            e.printStackTrace();
            close();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }

        return channel;
    }

    private void close() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (TimeoutException e1) {
                e1.printStackTrace();
            }
            channel = null;
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            conn = null;
        }
    }

    @Override
    public void run() {
        boolean success = false;
        while (!success) {
            channel = getChannel();
            if (channel != null) {
                try {
                    Consumer consumer = initConsumer();
                    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, EXCHANGE_NAME, "");
                    channel.basicConsume(queueName, true, consumer);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                    success = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    close();
                    success = false;
                }
            } else {
                success = false;
                try {
                    Thread.sleep(5 * 1000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Consumer initConsumer() {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                if (!"".equals(message)) {
                    JSONObject jsonObject = JSONObject.fromObject(message);
                    if (jsonObject.get("DEVICE_ID") != null && !"".equals(jsonObject.get("DEVICE_ID")) && deviceSerial == Integer.parseInt(jsonObject.get("DEVICE_ID").toString())) {
                        ModbusTcpDataGenerate generate = new ModbusTcpDataGenerate();
                        ModbusPdu mp = new ModbusPdu(5);
                        byte[] reqData;
                        if ("START_CHARGE".equals(jsonObject.get("START_STATUS"))) {
                            mp.InitWriteRegisterRequest(0, Integer.parseInt(jsonObject.get("START_VALUE").toString()));
                            reqData = generate.generateData(mp);
                        } else {
                            mp.InitWriteRegisterRequest(80, Integer.parseInt(jsonObject.get("START_VALUE").toString()));
                            reqData = generate.generateData(mp);
                        }
                        log.info("接收到中心程序发送的数据,START_STATUS：" + jsonObject.get("START_STATUS") + ",START_VALUE：" + jsonObject.get("START_VALUE"));

                        socketer.sendRequest(reqData);
                    }
                }
            }
        };
        return consumer;
    }

}
