package com.abk.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.nio.charset.Charset;

@SuppressLint("DefaultLocale")
public class HexUtil {
    public static final String GBK = "GBK";//GBK编码格式
    public static final String UTF8 = "UTF-8";//UTF-8编码格式

    /**
     * 包含startPos 不包含endPos
     *
     * @param data
     * @param startPos
     * @param endPos
     * @return
     */
    public static byte[] subByteArray(byte[] data, int startPos, int endPos) {
        if (endPos <= startPos || data.length < endPos)
            return null;
        int len = endPos - startPos;
        byte[] bt = new byte[len];
        System.arraycopy(data, startPos, bt, 0, len);
        return bt;
    }

    public static byte[] hexStringToByte(String hex) {
        hex = hex.toUpperCase();
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    public static String charBytesToString(byte[] data) {
        int i = 0;
        for (; i < data.length; i++) {
            if (data[i] == 0) {
                break;
            }
        }
        return new String(data, 0, i);
    }

    public static String bcd2str(byte[] bcds) {
        char[] ascii = "0123456789abcdef".toCharArray();
        byte[] temp = new byte[bcds.length * 2];
        for (int i = 0; i < bcds.length; i++) {
            temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
            temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
        }
        StringBuffer res = new StringBuffer();

        for (int i = 0; i < temp.length; i++) {
            res.append(ascii[temp[i]]);
        }
        return res.toString().toUpperCase();
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static byte[] int2bytes(int num) {
        return int2bytes(num, true);
    }

    public static byte[] int2bytes(int num, boolean isBigEndian) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            if (isBigEndian) {
                b[i] = (byte) (num >>> (24 - i * 8));
            } else {
                b[3 - i] = (byte) (num >>> (24 - i * 8));
            }
        }
        return b;
    }

    public static int bytes2int(byte[] b) {
        return bytes2int(b, true);
    }

    public static int bytes2int(byte[] b, boolean isBigEndian) {
        int mask = 0xff;
        int temp = 0;
        int res = 0;
        for (int i = 0; i < 4; i++) {
            res <<= 8;
            if (isBigEndian) {
                temp = b[i] & mask;
            } else {
                temp = b[3 - i] & mask;
            }
            res |= temp;
        }
        return res;
    }

    public static byte[] long2bytes(long num) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (num >>> (56 - (i * 8)));
        }
        return b;
    }

    public static long bytes2long(byte[] b) {
        long temp = 0;
        long res = 0;
        for (int i = 0; i < 8; i++) {
            res <<= 8;
            temp = b[i] & 0xff;
            res |= temp;
        }
        return res;
    }

    public static int sockBytes2int(byte[] res, int startPos) {
        int targets = (res[startPos] & 0xff) | ((res[startPos + 1] << 8) & 0xff00) // | 表示按位或
                | ((res[startPos + 2] << 24) >>> 8) | (res[startPos + 3] << 24);
        return targets;
    }

    public static short sockBytes2short(byte[] res, int startPos) {
        short targets = (short) ((res[startPos] & 0xff) | ((res[startPos + 1] << 8) & 0xff00));
        return targets;
    }

    public static short bytes2short(byte[] b) {
        return bytes2short(b, true);
    }

    public static short bytes2short(byte[] b, boolean isBigEndian) {
        // byte[] b=new byte[]{1,2,3,4};
        int mask = 0xff;
        int temp = 0;
        short res = 0;
        for (int i = 0; i < 2; i++) {
            res <<= 8;
            if (isBigEndian)
                temp = b[i] & mask;
            else
                temp = b[1 - i] & mask;
            res |= temp;
        }
        return res;
    }

    public static byte[] short2bytes(short num) {
        return short2bytes(num, true);
    }

    public static byte[] short2bytes(short num, boolean isBigEndian) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            if (isBigEndian)
                targets[i] = (byte) ((num >>> offset) & 0xff);
            else
                targets[1 - i] = (byte) ((num >>> offset) & 0xff);

        }
        return targets;
    }

    public static byte[] byteMerger(byte[] byte1, byte[] byte2) {
        byte[] byte3 = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, byte3, 0, byte1.length);
        System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
        return byte3;
    }

    public static String getBinaryStrFromByteArr(byte[] bArr) {
        String result = "";
        for (byte b : bArr) {
            result += getBinaryStrFromByte(b);
        }
        return result;
    }


    public static String getBinaryStrFromByte(byte b) {
        String result = "";
        byte a = b;
        for (int i = 0; i < 8; i++) {
            byte c = a;
            a = (byte) (a >> 1);
            a = (byte) (a << 1);
            if (a == c) {
                result = "0" + result;
            } else {
                result = "1" + result;
            }
            a = (byte) (a >> 1);
        }
        return result;
    }

    public static String getBinaryStrFromByte2(byte b) {
        String result = "";
        byte a = b;
        for (int i = 0; i < 8; i++) {
            result = (a % 2) + result;
            a = (byte) (a >> 1);
        }
        return result;
    }

    public static String getBinaryStrFromByte3(byte b) {
        String result = "";
        byte a = b;
        for (int i = 0; i < 8; i++) {
            result = (a % 2) + result;
            a = (byte) (a / 2);
        }
        return result;
    }


    public static byte[] toByteArray(int iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);

        }
        return bLocalArr;
    }


    public static byte[] xor(byte[] op1, byte[] op2) {
        if (op1.length != op2.length) {
            throw new IllegalArgumentException("参数错误，长度不一致");
        }
        byte[] result = new byte[op1.length];
        for (int i = 0; i < op1.length; i++) {
            result[i] = (byte) (op1[i] ^ op2[i]);
        }
        return result;
    }

    /**
     * 将字符串按照charsetName编码格式的字节数组
     *
     * @param data        字符串
     * @param charsetName 编码格式
     * @return
     */
    public static byte[] stringToBytes(String data, String charsetName) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        if (TextUtils.isEmpty(charsetName)) {
            return null;
        }
        try {
            Charset charset = Charset.forName(charsetName);
            return data.getBytes(charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将charsetName编码格式的字节数组转换为字符串
     *
     * @param bytes
     * @param charsetName
     * @return
     */
    public static String bytesToString(byte[] bytes, String charsetName) {
        if (bytes == null || bytes.length < 1) {
            return null;
        }
        if (TextUtils.isEmpty(charsetName)) {
            return null;
        }
        try {
            return new String(bytes, Charset.forName(charsetName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String Byte2Unicode(byte abyte[], int st, int bEnd) { // 不包含bEnd
        StringBuffer sb = new StringBuffer("");
        for (int j = st; j < bEnd; ) {
            int lw = abyte[j++];
            if (lw < 0) lw += 256;
            int hi = abyte[j++];
            if (hi < 0) hi += 256;
            char c = (char) (lw + (hi << 8));
            sb.append(c);
        }
        return sb.toString();
    }

    public static boolean isHex(String str) {
        String regex = "^[A-Fa-f0-9]+$";
        if (!TextUtils.isEmpty(str) && str.matches(regex))
            return true;
        else
            return false;
    }

    public static int takePositiveNumberByOneByte(int number) {
        return number >= 0 ? number : 256 + number;
    }

    public static int takePositiveNumberByTwoByte(int number) {
        return number >= 0 ? number : 65536 + number;
    }


    /**
     * 将byte数组以16进制字符显示
     *
     * @param b
     */
    public static String showResult16Str(byte[] b) {
        if (b == null) {
            return "";
        }
        String rs = "";
        int bl = b.length;
        byte bt;
        String bts = "";
        int btsl;
        for (int i = 0; i < bl; i++) {
            bt = b[i];
            bts = Integer.toHexString(bt);
            btsl = bts.length();
            if (btsl > 2) {
                bts = bts.substring(btsl - 2).toUpperCase();
            } else if (btsl == 1) {
                bts = "0" + bts.toUpperCase();
            } else {
                bts = bts.toUpperCase();
            }
//       System.out.println("i::"+i+">>>bts::"+bts);
            rs += bts;
        }
        return rs;
    }


    public static byte[] hexString2ByteArray(String bs) {
        int bsLength = bs.length();
        if (bsLength % 2 != 0) {
            return null;
        }
        byte[] cs = new byte[bsLength / 2];
        String st = "";
        for (int i = 0; i < bsLength; i = i + 2) {
            st = bs.substring(i, i + 2);
            cs[i / 2] = (byte) Integer.parseInt(st, 16);
        }
        return cs;
    }

}
