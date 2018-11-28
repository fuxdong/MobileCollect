package com.mixislink.util;

/**
 * Created by zhonghuan on 17/5/8.
 */
public class ConvertUtils {
    static final char[] HEX_CHAR_TABLE = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /*//将10进制转换成16进制
    public static String convert10ToHexString(String bString){
        return Integer.toHexString(Integer.parseInt(bString));
    }*/
    //将二进制转换成10进制
    public static int convert2To10String(String bString) {
        return Integer.valueOf(bString, 2);
    }

    //将二进制转换成16进制
    public static String convert2ToHexString(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }
    //16进制转成二进制

    public static String convertHexTo2String(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(hexString
                    .substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    /**
     * 校验和
     *
     * @param msg    需要计算校验和的byte数组
     * @param length 校验和位数
     * @return 计算出的校验和数组
     */
    public static byte[] SumCheck(byte[] msg, int length) {
        long mSum = 0;
        byte[] mByte = new byte[length];

        /** 逐Byte添加位数和 */
        for (byte byteMsg : msg) {
            long mNum = ((long) byteMsg >= 0) ? (long) byteMsg : ((long) byteMsg + 256);
            mSum += mNum;
        } /** end of for (byte byteMsg : msg) */

        /** 位数和转化为Byte数组 */
        for (int liv_Count = 0; liv_Count < length; liv_Count++) {
            mByte[length - liv_Count - 1] = (byte) (mSum >> (liv_Count * 8) & 0xff);
        } /** end of for (int liv_Count = 0; liv_Count < length; liv_Count++) */

        return mByte;
    }

    public static String toHexString(byte[] data) {
        if (data == null || data.length == 0)
            return null;
        byte[] hex = new byte[data.length * 2];
        int index = 0;
        for (byte b : data) {
            int v = b & 0xFF;
            hex[index++] = (byte) HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = (byte) HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex);
    }


    public static byte[] hexStringToBytes(String data) {
        if (data == null || "".equals(data))
            return null;
        data = data.toUpperCase();
        int length = data.length() / 2;
        char[] dataChars = data.toCharArray();
        byte[] byteData = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            byteData[i] = (byte) (charToByte(dataChars[pos]) << 4 | charToByte(dataChars[pos + 1]));
        }
        return byteData;
    }

    //将接收到的命令每隔两个字符加上一个空格
    public static String addSpace(String acceptCommand) {
        String regex = "(.{2})";
        acceptCommand = acceptCommand.replaceAll(regex, "$1 ");
        return acceptCommand;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String byteToHexString(byte b) {
        int data = b & 0xff;
        String hexData = Integer.toHexString(data);
        if (hexData.length() < 2)
            return "0" + hexData;
        else
            return hexData;
    }

    public static String byteArrToHexStr(byte[] data, int offset, int length) {
        if ((data.length == 0) || (offset > data.length) || (length < offset))
            return "";
        length = Math.min(data.length - offset, length);
        StringBuffer buf = new StringBuffer(length * 3);
        for (int i = 0; i < length; i++) {
            int b = data[i + offset] & 0xFF;
            buf.append(Integer.toHexString(b >>> 4));
            buf.append(Integer.toHexString(b & 0xF));
            if (i < length - 1)
                buf.append(" ");
        }
        return buf.toString();
    }

    public static final int bytesToInt16(byte highByte, byte lowByte, boolean unsigned) {
        // returned value is signed
        int i = (((int) highByte) << 8) | (((int) lowByte) & 0xFF);
        if (unsigned)
            return i & 0xFFFF;
        else
            return i;
    }

    public static final int ints16ToInt32(int lowInt16, int highInt16) {
        return (highInt16 << 16) | (lowInt16 & 0xFFFF);
    }


    public static int readInt16FromPDU(int offset, boolean unsigned, byte[] buffer) {

        return bytesToInt16(buffer[offset], buffer[offset + 1], unsigned);
    }

    /**
     * 通过传入的数值，生成PDU地址或数量的高位字节
     *
     * @param int16
     * @return
     */
    public static final byte highByte(int int16) {
        return (byte) (int16 >>> 8);
    }

    /**
     * 通过传入的数值，生成PDU地址或数量的低位字节
     *
     * @param int16
     * @return
     */
    public static final byte lowByte(int int16) {
        return (byte) (int16);
    }

    public static void main(String[] args) {
        System.out.println(2 >> 1);

    }
}
