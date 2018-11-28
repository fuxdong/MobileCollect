package com.mixislink.modbus;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.flying.jdbc.util.DBConnectionPoolByDruid;
import com.mixislink.util.FileUtils;
import net.sf.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

/**
 * Created by Fuxudong on 2018/1/18.
 * 初始化数据
 */
public class Init {
    private static Log log = LogFactory.getLog(Init.class);
    public static Properties modbusProp = null;
    public static JSONArray modbusDataJson = null;
    public static DBConnectionPoolByDruid connPool = DBConnectionPoolByDruid.getInstance();

    public static void start() {
        initPropData();
    }

    /**
     * 解析配置文件
     */
    public static void initPropData() {
        try {
            modbusProp = FileUtils.loadPropFile("config/modbus.properties");
            modbusDataJson = FileUtils.loadJsonFile("config/modbusDataAddress.json");
            log.debug("开始解析配置文件");
        } catch (Exception e) {
            log.error("解析配置文件出错");
        }
    }
}
