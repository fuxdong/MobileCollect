package com.mixislink.modbus;

import com.mixislink.util.ConvertUtils;

/**
 * Created by Fuxudong on 2018/1/18.
 * 生成Modbus协议PDU部分
 */
public class ModbusPdu {
    private final byte[] pdu = new byte[ModbusConstants.MAX_PDU_SIZE];
    //PDU长度
    private int pduSize;

    public ModbusPdu(int pduSize) {
        if ((pduSize < 1) || (pduSize > ModbusConstants.MAX_PDU_SIZE))
            throw new IllegalArgumentException("无效的PDU长度: " + pduSize);
        this.pduSize = pduSize;
    }

    /**
     * 生成客户端请求报文Pdu部分
     * 并返回ModbusPdu对象
     *
     * @param function
     * @param param1
     * @param param2
     */
    private void initRequest(byte function, int param1, int param2) {
        writeByteToPDU(0, function);
        writeInt16ToPDU(1, param1);
        writeInt16ToPDU(3, param2);
    }

    /**
     * 读保持寄存器03，生成请求报文Pdu
     *
     * @param startAddress
     * @param count
     */
    public void InitReadHoldingsRequest(int startAddress, int count) {
        if ((count < 1) || (count > ModbusConstants.MAX_READ_REGS))
            throw new IllegalArgumentException();
        initRequest(ModbusConstants.FN_READ_HOLDING_REGISTERS, startAddress, count);
    }

    /**
     * 读输入寄存器04，生成请求报文Pdu
     *
     * @param startAddress
     * @param count
     */
    public void InitReadInputsRegRequest(int startAddress, int count) {
        if ((count < 1) || (count > ModbusConstants.MAX_READ_REGS))
            throw new IllegalArgumentException();
        initRequest(ModbusConstants.FN_READ_INPUT_REGISTERS, startAddress, count);
    }

    /**
     * 写寄存器06，生成控制报文
     * @param regAddress 寄存器地址
     * @param value 数值
     */
    public void InitWriteRegisterRequest(int regAddress, int value) {
        initRequest(ModbusConstants.FN_WRITE_SINGLE_REGISTER, regAddress, value);
    }

    private void initResponse(int param1, int param2, byte function) {
        writeByteToPDU(0, function);
        writeByteToPDU(1, (byte) param1);
    }

    public int getPduSize() {
        return pduSize;
    }

    public void writeByteToPDU(int offset, byte value) {
        if ((offset < 0) || (offset >= pduSize))
            throw new IndexOutOfBoundsException();
        pdu[offset] = value;
    }

    /**
     * 处理占两个字节的数据
     *
     * @param offset
     * @param value
     */
    public void writeInt16ToPDU(int offset, int value) {
        //功能码的offset=0，并且由于占两个字节，所以offset不能为pdu的最后一位
        if ((offset < 1) || (offset >= pduSize - 1))
            throw new IndexOutOfBoundsException();
        //通过传入的value，得到高位和低位字节
        pdu[offset] = ConvertUtils.highByte(value);
        pdu[offset + 1] = ConvertUtils.lowByte(value);
    }

    public void readFromPdu(int pduOffset, int size, byte[] dest, int destOffset) {
        System.arraycopy(pdu, pduOffset, dest, destOffset, size);
    }
}
