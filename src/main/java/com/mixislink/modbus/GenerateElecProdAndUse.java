package com.mixislink.modbus;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.flying.jdbc.SqlHelper;
import com.flying.jdbc.util.DBConnection;
import com.mixislink.socket.SocketSimClient;
import com.mixislink.util.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Fuxudong on 2018/1/18.
 */
public class GenerateElecProdAndUse {
    private static Log log = LogFactory.getLog(GenerateElecProdAndUse.class);
    private static double elecProd;
    private static double elecUse;
    private static double outputP;
    private static double chargeA;
    private static double chargeU;
    private static double nettingA;
    private static double nettingU;
    public static List<Map> elecList = new ArrayList<>();

    private static int deviceSerial;

    static {
        try {
            //获取桩号
            deviceSerial = SocketSimClient.deviceSerial;

            DruidPooledConnection dbConnection = Init.connPool.getConnection();
            java.sql.Connection connection = dbConnection.getConnection();
            String sql = "SELECT A.PRODUCE_ELEC,A.USE_ELEC FROM T_ANALOG_DATA A WHERE A.ID = ?";
            PreparedStatement pstm = connection.prepareStatement(sql);
            pstm.setInt(1, deviceSerial);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                elecProd = rs.getDouble("PRODUCE_ELEC");
                elecUse = rs.getDouble("USE_ELEC");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void generateData() {
        /**
         * 定时生成用电、发电随机数
         */
        Runnable runnable = new Runnable() {
            public void run() {
                elecProd += (Math.random() * 0.1 + 0.01);
                elecUse += Math.random() * 0.5;
                outputP = Math.random() * 0.5 + 4;
                chargeA = Math.random() * 3 + 15;
                chargeU = Math.random() * 30 + 220;
                nettingA = Math.random() * 3 + 15;
                nettingU = Math.random() * 30 + 220;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                /**
                 * elecList里面是否有数据，没有则往里面插入
                 * 否则，执行更新操作
                 */
                if (elecList.size() > 0) {
                    for (int i = 0; i < elecList.size(); i++) {
                        if ("ELEC_PROD".equals(elecList.get(i).get("CHANNEL_NAME"))) {
                            elecList.get(i).put("CHANNEL_VALUE", new BigDecimal(elecProd).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            elecList.get(i).put("TIME", sdf.format(new Date()));
                        } else if ("ELEC_USE".equals(elecList.get(i).get("CHANNEL_NAME"))) {
                            elecList.get(i).put("CHANNEL_VALUE", new BigDecimal(elecUse).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            elecList.get(i).put("TIME", sdf.format(new Date()));
                        } else if ("ELEC_POWER".equals(elecList.get(i).get("CHANNEL_NAME"))) {
                            elecList.get(i).put("CHANNEL_VALUE", new BigDecimal(outputP).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            elecList.get(i).put("TIME", sdf.format(new Date()));
                        } else if ("CHARGE_A".equals(elecList.get(i).get("CHANNEL_NAME"))) {
                            elecList.get(i).put("CHANNEL_VALUE", new BigDecimal(chargeA).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            elecList.get(i).put("TIME", sdf.format(new Date()));
                        } else if ("CHARGE_U".equals(elecList.get(i).get("CHANNEL_NAME"))) {
                            elecList.get(i).put("CHANNEL_VALUE", new BigDecimal(chargeU).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            elecList.get(i).put("TIME", sdf.format(new Date()));
                        } else if ("NETTING_A".equals(elecList.get(i).get("CHANNEL_NAME"))) {
                            elecList.get(i).put("CHANNEL_VALUE", new BigDecimal(nettingA).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            elecList.get(i).put("TIME", sdf.format(new Date()));
                        } else if ("NETTING_U".equals(elecList.get(i).get("CHANNEL_NAME"))) {
                            elecList.get(i).put("CHANNEL_VALUE", new BigDecimal(nettingU).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            elecList.get(i).put("TIME", sdf.format(new Date()));
                        }
                    }
                } else {
                    Map<String, String> elecProdMap = new HashMap();
                    elecProdMap.put("DEVICE_ID", deviceSerial + "");
                    elecProdMap.put("CHANNEL_NAME", "ELEC_PROD");
                    elecProdMap.put("CHANNEL_VALUE", new BigDecimal(elecProd).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
                    elecProdMap.put("TIME", sdf.format(new Date()));

                    Map<String, String> elecUseMap = new HashMap();
                    elecUseMap.put("DEVICE_ID", deviceSerial + "");
                    elecUseMap.put("CHANNEL_NAME", "ELEC_USE");
                    elecUseMap.put("CHANNEL_VALUE", new BigDecimal(elecUse).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
                    elecUseMap.put("TIME", sdf.format(new Date()));

                    Map<String, String> elecPowerMap = new HashMap();
                    elecPowerMap.put("DEVICE_ID", deviceSerial + "");
                    elecPowerMap.put("CHANNEL_NAME", "ELEC_POWER");
                    elecPowerMap.put("CHANNEL_VALUE", new BigDecimal(outputP).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
                    elecPowerMap.put("TIME", sdf.format(new Date()));

                    Map<String, String> chargeAMap = new HashMap();
                    chargeAMap.put("DEVICE_ID", deviceSerial + "");
                    chargeAMap.put("CHANNEL_NAME", "CHARGE_A");
                    chargeAMap.put("CHANNEL_VALUE", new BigDecimal(chargeA).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
                    chargeAMap.put("TIME", sdf.format(new Date()));

                    Map<String, String> chargeUMap = new HashMap();
                    chargeUMap.put("DEVICE_ID", deviceSerial + "");
                    chargeUMap.put("CHANNEL_NAME", "CHARGE_U");
                    chargeUMap.put("CHANNEL_VALUE", new BigDecimal(chargeU).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
                    chargeUMap.put("TIME", sdf.format(new Date()));

                    Map<String, String> nettingAMap = new HashMap();
                    nettingAMap.put("DEVICE_ID", deviceSerial + "");
                    nettingAMap.put("CHANNEL_NAME", "NETTING_A");
                    nettingAMap.put("CHANNEL_VALUE", new BigDecimal(nettingA).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
                    nettingAMap.put("TIME", sdf.format(new Date()));

                    Map<String, String> nettingUMap = new HashMap();
                    nettingUMap.put("DEVICE_ID", deviceSerial + "");
                    nettingUMap.put("CHANNEL_NAME", "NETTING_U");
                    nettingUMap.put("CHANNEL_VALUE", new BigDecimal(nettingU).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
                    nettingUMap.put("TIME", sdf.format(new Date()));


                    elecList.add(elecProdMap);
                    elecList.add(elecUseMap);
                    elecList.add(elecPowerMap);
                    elecList.add(chargeAMap);
                    elecList.add(chargeUMap);
                    elecList.add(nettingAMap);
                    elecList.add(nettingUMap);
                }
//                System.out.println("elecProd:" + elecProd + "\telecUse:" + elecUse);
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * 返回桩号用电量、发电量、输出功率数据
     *
     * @return
     */
    public static Map returnData() {
        //格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map map = new HashMap();
        map.put("DEVICE_SERIAL", deviceSerial);
        map.put("ELEC_PROD", new BigDecimal(elecProd).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        map.put("ELEC_USE", new BigDecimal(elecUse).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        map.put("OUTPUT_P", new BigDecimal(outputP).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        map.put("CHARGE_A", new BigDecimal(chargeA).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        map.put("CHARGE_U", new BigDecimal(chargeU).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        map.put("NETTING_A", new BigDecimal(nettingA).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        map.put("NETTING_U", new BigDecimal(nettingU).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        map.put("TIME", sdf.format(new Date()));
        return map;
    }
}
