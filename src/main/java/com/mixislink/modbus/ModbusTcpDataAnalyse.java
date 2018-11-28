package com.mixislink.modbus;

import com.mixislink.socket.SocketSimClient;
import com.mixislink.util.ConvertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Fuxudong on 2018/1/17.
 * modbus服务器端发送报文解析
 */
public class ModbusTcpDataAnalyse {

    private static final Log log = LogFactory.getLog(ModbusTcpDataAnalyse.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 解析服务器端发送的报文
     *
     * @param buffer
     */
    public List<Map> analyse(byte[] buffer) {
        int length = buffer[5] & 0XFF;
        String message;
        if (length == 79) {//发送报文的长度字节为79
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6);
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 0;
            int to = 38;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 21) {//发送报文的长度字节为21
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 38;
            int to = 47;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 35) {//发送报文的长度字节为35
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 47;
            int to = 63;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 11) {//发送报文的长度字节为11
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 63;
            int to = 65;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 31) {//发送报文的长度字节为31
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 65;
            int to = 72;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 61) {//发送报文的长度字节为61
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 72;
            int to = 101;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 9) {//发送报文的长度字节为9
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 101;
            int to = 104;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 55) {//发送报文的长度字节为55
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 104;
            int to = 117;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 99) {//发送报文的长度字节为99
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 117;
            int to = 141;
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 5) {//发送报文的长度字节为99
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到服务器端发送的报文：" + message.toUpperCase());

            //数据从JSONArray的第几项开始
            int from = 141;
            int to = Init.modbusDataJson.size();
            String date = sdf.format(new Date());
            return analyseData(from, to, buffer, date);
        } else if (length == 6) {
            message = ConvertUtils.byteArrToHexStr(buffer, 0, length + 6).trim();
            log.info("接收到控制报文：" + message.toUpperCase());
        } else {
            log.error("接收到错误的报文，无法解析");
        }
        return null;
    }

    /**
     * @param from   从JSON配置文件中第几项数据开始读取
     * @param to     到JSON配置文件中第几项数据结束
     * @param buffer socket中读到的数据流，转换为byte[]
     * @param date   时间
     */
    public List<Map> analyseData(int from, int to, byte[] buffer, String date) {
        List<Map> list = new ArrayList<>();
        for (int i = from; i < to; i++) {
            Map jsonMap = (Map) Init.modbusDataJson.get(i);

            if (!"".equals(jsonMap.get("name"))) {
                //数据部分从第10个字节处开始
                int start = 9;
                //获取前若干项数据所读的字节数
                for (int k = from; k < i; k++) {
                    start += Integer.parseInt(((Map) Init.modbusDataJson.get(k)).get("byte").toString());
                }
                Map map = setPropValue(jsonMap, start, buffer, date);
                if (map != null && !map.isEmpty()) {
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * 读取和封装数据
     *
     * @param map    json配置文件里的第n项数据
     * @param buffer 服务器端数据报文转化为String[]，改属性数据部分是从buffer[start]处开始的
     * @param date   时间
     */
    public Map setPropValue(Map map, int start, byte[] buffer, String date) {
        StringBuffer val = new StringBuffer();
        Map dataMap = new HashMap();
        dataMap.put("CHANNEL_NAME", map.get("name"));
        dataMap.put("DEVICE_ID", SocketSimClient.deviceSerial + "");

        /**
         * 根据类型处理数据
         */
        if ("INT16U".equals(map.get("type"))) {

            int value = ConvertUtils.readInt16FromPDU(start, true, buffer);

            //根据数据精度得到真实数据
            if (!"1".equals(map.get("decimal"))) {
                double actuVal = new BigDecimal(value).multiply(new BigDecimal(map.get("decimal").
                        toString())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                dataMap.put("CHANNEL_VALUE", actuVal);
                log.info(map.get("name") + "：" + actuVal + "，TIME：" + date);
            } else {
                dataMap.put("CHANNEL_VALUE", value);
                log.info(map.get("name") + "：" + value + "，TIME：" + date);
            }
        } else if ("INT16S".equals(map.get("type"))) {
            int value = ConvertUtils.readInt16FromPDU(start, false, buffer);

            //根据数据精度得到真实数据
            if (!"1".equals(map.get("decimal"))) {
                double actuVal = new BigDecimal(value).multiply(new BigDecimal(map.get("decimal").
                        toString())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                dataMap.put("CHANNEL_VALUE", actuVal);
                log.info(map.get("name") + "：" + actuVal + "，TIME：" + date);
            } else {

                dataMap.put("CHANNEL_VALUE", value);
                log.info(map.get("name") + "：" + value + "，TIME：" + date);
            }
        } else if ("INT32U".equals(map.get("type"))) {
            int value = (buffer[start] << 24) | (buffer[start + 1] << 16) |
                    (buffer[start + 2] << 8) | (buffer[start + 3] & 0XFF);

            //根据数据精度得到真实数据
            if (!"1".equals(map.get("decimal"))) {
                double actuVal = new BigDecimal(value).multiply(new BigDecimal(map.get("decimal").
                        toString())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                dataMap.put("CHANNEL_VALUE", actuVal);
                log.info(map.get("name") + "：" + actuVal + "，TIME：" + date);
            } else {

                dataMap.put("CHANNEL_VALUE", value);
                log.info(map.get("name") + "：" + value + "，TIME：" + date);
            }
        } else if ("FLOAT".equals(map.get("type"))) {
            float value = Float.intBitsToFloat((buffer[start + 3] & 0xff) | ((buffer[start + 2] & 0xff) << 8)
                    | ((buffer[start + 1] & 0xff) << 16) | ((buffer[start] & 0xff) << 24));

            //根据数据精度得到真实数据
            if (!"1".equals(map.get("decimal"))) {
                double actuVal = new BigDecimal(value).multiply(new BigDecimal(map.get("decimal").
                        toString())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                dataMap.put("CHANNEL_VALUE", actuVal);
                log.info(map.get("name") + "：" + actuVal + "，TIME：" + date);
            } else {

                dataMap.put("CHANNEL_VALUE", value);
                log.info(map.get("name") + "：" + value + "，TIME：" + date);
            }
        } else {//BYTE
            if (val.toString().length() >= 32) {

                log.info(map.get("name") + "：" + val.toString() + "，TIME：" + date);
            } else {
                int value = (byte) ConvertUtils.readInt16FromPDU(start, false, buffer);

                //根据数据精度得到真实数据
                if (!"1".equals(map.get("decimal"))) {
                    double actuVal = new BigDecimal(value).multiply(new BigDecimal(map.get("decimal").
                            toString())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                    dataMap.put("CHANNEL_VALUE", actuVal);
                    log.info(map.get("name") + "：" + actuVal + "，TIME：" + date);
                } else {

                    dataMap.put("CHANNEL_VALUE", value);
                    log.info(map.get("name") + "：" + value + "，TIME：" + date);
                }
            }
        }

        dataMap.put("TIME", date);
        return dataMap;
    }

    /**
     * 存储数据到Map中
     *
     * @param channelName
     * @param channelValue
     * @param date
     * @return
     */
    public Map storMap(String channelName, String channelValue, String date) {
        Map<String, String> elecProdMap = new HashMap();
        elecProdMap.put("DEVICE_ID", SocketSimClient.deviceSerial + "");
        elecProdMap.put("CHANNEL_NAME", channelName);
        elecProdMap.put("CHANNEL_VALUE", channelValue);
        elecProdMap.put("TIME", date);
        return elecProdMap;
    }
}
