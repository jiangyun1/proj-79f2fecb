/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */

package util;

import java.io.*;

//工具类
public class Utils {

    //反序列化方法，将字节数组反序列化成指定对象类型
    public static <T> T deserialize(byte[] data, int offset, int length) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data, offset, length);
        ObjectInputStream ins = new ObjectInputStream(bais);
        return (T) ins.readObject();
    }
    //序列化方法，把指定对象序列化成字节数组
    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream outs = new ObjectOutputStream(baos);
        outs.writeObject(object);
        return baos.toByteArray();

    }

}
