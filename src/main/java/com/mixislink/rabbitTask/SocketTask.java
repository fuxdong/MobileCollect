package com.mixislink.rabbitTask;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.mixislink.modbus.*;
import com.mixislink.socket.Socketer;
import com.mixislink.util.RabbitMq;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhonghuan on 2018/1/18.
 */
public class SocketTask {
    private Log log = LogFactory.getLog(SocketTask.class);
    private PreparedStatement psmt;

    private Queue<Map> dataQueue = null;
    private Socketer socketer = null;
    private RabbitMq mq;
    private ModbusTcpDataAnalyse tcpDataAnalyse = new ModbusTcpDataAnalyse();

    //定义消息队列明
    private static final String QUEUE_NAME_SIMU = "ELECT_DATA";
    private static final String QUEUE_NAME = "ELEC_REALITY";

    public SocketTask(RabbitMq mq) {
        GenerateElecProdAndUse.generateData();
        this.mq = mq;
    }

    public SocketTask(Queue<Map> dataQueue, Socketer socketer, RabbitMq mq) {
        this.dataQueue = dataQueue;
        this.socketer = socketer;
        this.mq = mq;
    }

    /**
     * 数据处理代码，包含更新存储发电量、用电量数据
     * 使用websocket定时往前端发送数据
     * 以及定时往消息服务器发送数据，用于同步数据到中心
     */
    public void sendDataToMQ() {
        Runnable sendTask = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Map dataMap = dataQueue.poll();
                    if (dataMap != null) {
                        try {
                            if (!mq.sendMq(JSONObject.fromObject(dataMap).toString(), QUEUE_NAME)) {
                                if (dataQueue.size() <= 300000) {
                                    dataQueue.offer(dataMap);
                                }
                                try {
                                    Thread.sleep(500l);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {

                                log.info("发送数据到消息服务器,CHANNEL_NAME：" + dataMap.get("CHANNEL_NAME") + ",CHANNEL_VALUE：" + dataMap.get("CHANNEL_VALUE") +
                                        ",TIME：" + dataMap.get("TIME") + ",DEVICE_ID：" + dataMap.get("DEVICE_ID"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(5 * 1000l);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        Thread sendThread = new Thread(sendTask);
        sendThread.start();
    }

    /**
     * 读socket中的输入流
     */
    public void receiveData() {
        Runnable receiveHandle = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    byte[] buffer = new byte[ModbusConstants.MAX_PDU_SIZE + 7];
                    if (socketer.readData(buffer)) {
                        List<Map> datas = tcpDataAnalyse.analyse(buffer);
                        if (datas != null && !datas.isEmpty()) {
                            for (Map map : datas) {
                                dataQueue.offer(map);
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        Thread readThread = new Thread(receiveHandle);
        readThread.start();
    }

    /**
     * 客户端数据发送处理
     */
    public void sendCommandToServer() {
        //获取暂停时间
        int pauseTime;
        try {
            pauseTime = Integer.parseInt(Init.modbusProp.getProperty("PAUSE_TIME"));
        } catch (Exception e) {
            pauseTime = 1000;
        }
        ModbusTcpDataGenerate generate = new ModbusTcpDataGenerate();
        ModbusPdu mp1 = new ModbusPdu(5);
        //从地址为1280的寄存器开始，读38个寄存器的数据，每个寄存器返回2个字节数据，共78个字节数据
        mp1.InitReadHoldingsRequest(1280, 38);
        byte[] reqData1 = generate.generateData(mp1);

        ModbusPdu mp2 = new ModbusPdu(5);
        //从地址为2050的寄存器开始，读14个寄存器的数据，每个寄存器返回2个字节数据，共28个字节数据
        mp2.InitReadHoldingsRequest(2050, 14);
        byte[] reqData2 = generate.generateData(mp2);

        ModbusPdu mp3 = new ModbusPdu(5);
        //从地址为4110的寄存器开始，读29个寄存器的数据，每个寄存器返回2个字节数据，共58个字节数据
        mp3.InitReadHoldingsRequest(4110, 29);
        byte[] reqData3 = generate.generateData(mp3);

        ModbusPdu mp4 = new ModbusPdu(5);
        //从地址为‭4162的寄存器开始，读3个寄存器的数据，每个寄存器返回2个字节数据，共6个字节数据
        mp4.InitReadHoldingsRequest(4162, 3);
        byte[] reqData4 = generate.generateData(mp4);

        ModbusPdu mp5 = new ModbusPdu(5);
        //从地址为4910的寄存器开始，读26个寄存器的数据，每个寄存器返回2个字节数据，共52个字节数据
        mp5.InitReadHoldingsRequest(4910, 26);
        byte[] reqData5 = generate.generateData(mp5);

        ModbusPdu mp6 = new ModbusPdu(5);
        //从地址为1360的寄存器开始，读9个寄存器的数据，每个寄存器返回2个字节数据，共18个字节数据
        mp6.InitReadHoldingsRequest(1360, 9);
        byte[] reqData6 = generate.generateData(mp6);

        ModbusPdu mp7 = new ModbusPdu(5);
        //从地址为1536的寄存器开始，读16个寄存器的数据，每个寄存器返回2个字节数据，共32个字节数据
        mp7.InitReadHoldingsRequest(1536, 16);
        byte[] reqData7 = generate.generateData(mp7);

        ModbusPdu mp8 = new ModbusPdu(5);
        //从地址为1616的寄存器开始，读4个寄存器的数据，每个寄存器返回2个字节数据，共8个字节数据
        mp8.InitReadHoldingsRequest(1616, 4);
        byte[] reqData8 = generate.generateData(mp8);

        ModbusPdu mp9 = new ModbusPdu(5);
        //从地址为4948的寄存器开始，读48个寄存器的数据，每个寄存器返回2个字节数据，共96个字节数据
        mp9.InitReadHoldingsRequest(4948, 48);
        byte[] reqData9 = generate.generateData(mp9);

        ModbusPdu mp10 = new ModbusPdu(5);
        //从地址为256的寄存器开始，读1个寄存器的数据，每个寄存器返回2个字节数据，共2个字节数据
        mp10.InitReadHoldingsRequest(256, 1);
        byte[] reqData10 = generate.generateData(mp10);

        long timeStart = 0l;
        long timeEnd = 0l;
        while (true) {
            try {
                timeStart = System.currentTimeMillis();
                socketer.sendRequest(reqData1);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData2);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData3);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData4);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData5);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData6);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData7);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData8);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData9);
                Thread.sleep(pauseTime);
                socketer.sendRequest(reqData10);
                timeEnd = System.currentTimeMillis();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(120 * 1000l - timeEnd + timeStart);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 模拟发送数据
     */
    public void sendSimulateData() {
        String sql = "UPDATE T_ANALOG_DATA SET PRODUCE_ELEC=?,USE_ELEC=?,TIME=? WHERE ID = ?";
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                DruidPooledConnection dbConnection = null;
                Connection connection = null;
                Map map = GenerateElecProdAndUse.returnData();
                /*log.info("ELEC_PROD:" + map.get("ELEC_PROD") +
                        "\tELEC_USE:" + map.get("ELEC_USE") + "\tTIME:" + map.get("TIME"));*/
                try {
                    dbConnection = Init.connPool.getConnection();
                    connection = dbConnection.getConnection();
                    psmt = connection.prepareStatement(sql);
                    //更新发电量、用电量数据到数据库
                    try {
                        psmt.setObject(1, map.get("ELEC_PROD"));
                        psmt.setObject(2, map.get("ELEC_USE"));
                        psmt.setObject(3, map.get("TIME"));
                        psmt.setObject(4, map.get("DEVICE_SERIAL"));
                        psmt.executeUpdate();
                    } catch (SQLException e) {
                        log.error("执行update操作出错！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            log.error("关闭Connection连接出错", e);
                        }
                    }
                    if (dbConnection != null) {
                        try {
                            dbConnection.close();
                        } catch (SQLException e) {
                            log.error("关闭Druid连接出错", e);
                        }
                    }
                }

                for (int i = 0; i < GenerateElecProdAndUse.elecList.size(); i++) {
                    try {
                        mq.sendMq(JSONObject.fromObject(GenerateElecProdAndUse.elecList.get(i)).toString(), QUEUE_NAME_SIMU);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    log.info("发送数据到消息服务器,CHANNEL_NAME：" + GenerateElecProdAndUse.elecList.get(i).get("CHANNEL_NAME") + ",CHANNEL_VALUE：" + GenerateElecProdAndUse.elecList.get(i).get("CHANNEL_VALUE") +
                            ",TIME：" + GenerateElecProdAndUse.elecList.get(i).get("TIME") + ",DEVICE_ID：" + GenerateElecProdAndUse.elecList.get(i).get("DEVICE_ID"));
                }
            }
        };
        //定时任务，启动时延迟2分钟执行，以后每隔5分钟执行一次
        ScheduledExecutorService service2 = Executors.newSingleThreadScheduledExecutor();
        service2.scheduleAtFixedRate(runnable2, 20, 120, TimeUnit.SECONDS);
    }
}
