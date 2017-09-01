package com.mocoo.hang.rtprinter.utils;

/**
 * Created by Administrator on 2015/6/11.
 */
public class ByteUtil {

    private static final String TAG = "ByteUtil";

    private ByteUtil()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static String byteArray2HexStr(byte[] byteArray) {

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            String hex = Integer.toHexString(byteArray[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
//            hexString.append("0x" + hex.toUpperCase() + ",");
            hexString.append(hex.toUpperCase() + ",");
        }
        hexString.deleteCharAt(hexString.length() - 1);
        LogUtils.d(TAG,"hexString = " + hexString.toString());
        return hexString.toString();
    }

    public static String intArray2HexStr(int[] intArray) {

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < intArray.length; i++) {
            String hex = Integer.toHexString(intArray[i]);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexString.append("0x" + hex.toUpperCase() + ",");
        }
        hexString.deleteCharAt(hexString.length() - 1);
        return hexString.toString();
    }


    public static byte hexStr2Byte(String hexStr) {
        char[] chars = hexStr.toUpperCase().toCharArray();
        int b = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            int differ = c >= 'A' ? 'A' - 10 : '0' - 0;
            b += (c - differ) * Math.pow(16, chars.length-1-i);
        }
        return (byte)b;
    }


}
