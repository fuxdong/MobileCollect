package com.mixislink.modbus;

import com.mixislink.modbus.Init;
import com.mixislink.modbus.ModbusConstants;
import com.mixislink.modbus.ModbusPdu;
import com.mixislink.socket.SocketSimClient;
import com.mixislink.util.ConvertUtils;

/**
 * Created by Fuxudong on 2018/1/29.
 * modbusTcp报文生成类
 * 里面实现了功能码03和04的报文生成
 */
public class ModbusTcpDataGenerate {

    //获取事物标识符
    private int transactionSerial = Integer.parseInt(Init.modbusProp.getProperty("TRANSACTION_SIGN"));
    //获取协议标识符
    private int protocolSign = Integer.parseInt(Init.modbusProp.getProperty("PROTOCOL_SIGN"));

    /**
     * 生成modbusTcp报文
     *
     * @param modbusPdu
     * @return
     */
    public byte[] generateData(ModbusPdu modbusPdu) {

        byte[] buffer = new byte[ModbusConstants.MAX_PDU_SIZE + 7];
        //MBAP Header 占7个字节
        //事物标识符2个字节
        buffer[0] = ConvertUtils.highByte(transactionSerial);
        buffer[1] = ConvertUtils.lowByte(transactionSerial);
        //协议标识符2个字节
        buffer[2] = ConvertUtils.highByte(protocolSign);
        buffer[3] = ConvertUtils.lowByte(protocolSign);
        //协议长度2个字节，getPduSize+1是因为，PDU前面、长度后面还有1个字节单元标识符
        int size = modbusPdu.getPduSize() + 1;
        buffer[4] = ConvertUtils.highByte(size);
        buffer[5] = ConvertUtils.lowByte(size);
        //单元标识符1个字节
        buffer[6] = (byte) SocketSimClient.deviceAddr;
        //复制PDU部分到buffer中
        modbusPdu.readFromPdu(0, modbusPdu.getPduSize(), buffer, 7);
        return buffer;
    }
}
