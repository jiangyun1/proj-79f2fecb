/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */

package multicast;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.StringJoiner;

//消息类
public class Message<P extends Serializable> implements Serializable {

    //消息类型
    private byte messageType;
    //序列化的消息载荷
    private P payload;
    //IP地址
    private volatile InetAddress address;
    //端口号
    private volatile int port;


    public Message(byte messageType, P payload) {
        this(messageType, payload, null, 0);
    }

    public Message(byte messageType, P payload, InetAddress address, int port) {
        this.messageType = messageType;
        this.payload = payload;
        this.address = address;
        this.port = port;
    }

    //返回消息类型
    public byte getMessageType() {
        return messageType;
    }

    //返回消息载荷
    public P getPayload() {
        return payload;
    }

    //返回消息IP
    public InetAddress getAddress() {
        return address;
    }

    //设置消息IP
    public void setAddress(InetAddress address) {
        this.address = address;
    }
    //返回端口号
    public int getPort() {
        return port;
    }
    //设置端口号
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return new StringJoiner(",")
                .add("{messageType=" + messageType)
                .add("address=" + address.getHostAddress())
                .add("port=" + port)
                .add("payload=" + payload + "}")
                .toString();
    }
}
