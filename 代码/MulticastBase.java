/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */

package multicast;

import util.Utils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

//组播相关的基类
public abstract class MulticastBase<S extends DatagramSocket> implements Runnable {

    //定义消息类别
    public static final byte USER_JOIN = 1;
    public static final byte USER_JOIN_ACK = 2;
    public static final byte USER_LEAVE = 3;

    //用户对象
    protected final User user;
    //组播地址
    protected final InetAddress groupAddress;
    //组播端口号
    protected final int port;
    //消息缓冲区大小
    protected final int bufferSize;
    //套接字
    protected final S socket;
    //消息处理接口
    private final MyMessageHandler messageHandler;
    //线程停止标志
    public volatile boolean exit = false;

    public MulticastBase(String groupAddress, int port, int bufferSize, S socket, User user, MyMessageHandler messageHandler) throws IOException {
        this.groupAddress = InetAddress.getByName(groupAddress);
        this.port = port;
        this.bufferSize = bufferSize;
        this.socket = socket;
        this.user = user;
        this.messageHandler = messageHandler;
    }

    public User getUser() {
        return user;
    }

    public InetAddress getGroupAddress() {
        return groupAddress;
    }

    public int getPort() {
        return port;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public S getSocket() {
        return socket;
    }

    //线程开始
    public abstract void start() throws IOException;

    //线程结束
    public void stop() {
        exit=true;
    }

    //发送Message方法
    public void sendMessage(Message message) throws IOException {
        //调用工具类，将message序列化
        ByteBuffer buffer = ByteBuffer.wrap(Utils.serialize(message));
        DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), message.getAddress(), message.getPort());
        //使用socket发送数据包
        socket.send(packet);
    }

    //接收Message方法
    public Message receiveMessage() throws IOException, ClassNotFoundException {
        DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
        //使用socket接收数据包
        socket.receive(packet);
        //使用工具类，将接收到的数据包进行反序列化
        Message message = Utils.deserialize(packet.getData(), 0, packet.getLength());
        message.setAddress(packet.getAddress());
        message.setPort(packet.getPort());
        //返回接收到的消息
        return message;
    }

    //线程运行
    @Override
    public void run() {
        try {
            while (!exit) {
                //接收message
                Message message = receiveMessage();
                //通过Handle处理收到的消息
                messageHandler.process(message, this);
            }
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
