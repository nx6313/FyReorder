package com.fy.niu.fyreorder.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngdong on 2018/3/18.
 */

public class PrintUtil {
    /**
     * 检查是否有纸指令
     */
    public static final byte[] stateBype = new byte[]{0x10, 0x04, 0x04};
    /**
     * 检查是否有纸指令
     */
    //  public static final byte[] stateBype2 = new byte[] { 0x1B, 0x76 };
    /**
     * 居左对齐
     */
    public static final byte[] toLeft = new byte[]{0x1B, 0x61, 0x00};
    /**
     * 居中对齐
     */
    public static final byte[] toCenter = new byte[]{0x1B, 0x61, 0x01};
    /**
     * 居右对齐
     */
    public static final byte[] toRight = new byte[]{0x1B, 0x61, 0x02};
    /**
     * 加大2倍
     */
    public static final byte[] toLarge = new byte[]{0x1D, 0x21, 0x11};
    /**
     * 取消加大
     */
    public static final byte[] cancleLarge = new byte[]{0x1D, 0x21, 0x00};
    /**
     * 加粗
     */
    public static final byte[] toLarge2 = new byte[]{0x1B, 0x45, 0x01};
    /**
     * 取消加粗
     */
    public static final byte[] cancleLarge2 = new byte[]{0x1B, 0x45, 0x00};
    /**
     * 加载走纸命令
     */
    public static final byte[] cut = new byte[]{0x1B, 0x64, 0x03};
    /**
     * 加载走纸命令
     */
    public static final byte[] cut2 = new byte[]{0x1B, 0x64, 0x01};

    /**
     * 设置模型
     */
    public static final byte[] setCodeModel = new byte[]{0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00};
    /**
     * 设置单元格大小
     */
    public static final byte[] setCodeSize = new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x09};
    /**
     * 设置纠错正等级
     */
    public static final byte[] setCodeLevel = new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30};
    /**
     * 加载二维码
     */
    public static byte[] setCode = new byte[8];
    /**
     * 打印二维码
     */
    public static final byte[] printCode = new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30};

    /**
     * 设置加载二维码指令
     *
     * @param code
     */
    public static void doSetCode(String code) {
        setCode[0] = 0x1D;
        setCode[1] = 0x28;
        setCode[2] = 0x6B;
        setCode[3] = (byte) (code.length() + 3);
        setCode[4] = 0x00;
        setCode[5] = 0x31;
        setCode[6] = 0x50;
        setCode[7] = 0x30;
    }

    /**
     * 合并两个byte数组
     *
     * @param byte_1
     * @param byte_2
     * @return
     */
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * int[]转byte[]
     *
     * @param arg
     * @return
     */
    public static byte[] intTobyte(int arg[]) {
        byte[] byteTmp = new byte[arg.length];
        for (int i = 0; i < arg.length; i++) {
            byteTmp[i] = (byte) arg[i];
        }
        return byteTmp;
    }

    /**
     * byte转Byte
     *
     * @param srcArray
     * @param cpyArray
     */
    public static void CopyArray(byte[] srcArray, Byte[] cpyArray) {
        for (int index = 0; index < cpyArray.length; index++) {
            cpyArray[index] = srcArray[index];
        }
    }

    /**
     * List<Byte>转换为byte[]
     *
     * @param ByteArray
     * @return
     */
    public static byte[] convertFromListByteArrayTobyteArray(
            List<Byte> ByteArray) {
        byte[] byteArray = new byte[ByteArray.size()];
        for (int index = 0; index < byteArray.length; index++) {
            byteArray[index] = ByteArray.get(index);
        }

        return byteArray;
    }

    /**
     * 去重复
     *
     * @param li
     * @return
     */
    public static List<String> removeDuplicateWithOrder(List<String> li) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < li.size(); i++) {
            String str = li.get(i);  //获取传入集合对象的每一个元素
            if (!list.contains(str)) {   //查看新集合中是否有指定的元素，如果没有则加入
                list.add(str);
            }
        }
        return list;  //返回集合
    }
}
