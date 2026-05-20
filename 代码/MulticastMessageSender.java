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
import java.nio.ByteBuffer;

//组播消息发送+回复线程
public class MulticastMessageSender extends MulticastBase<DatagramSocket> {

    //构造方法
    public MulticastMessageSender(String multicastAddress, int port, int bufferSize, User user, MyMessageHandler messageHandler) throws IOException {
        super(multicastAddress, port, bufferSize, new DatagramSocket(), user, messageHandler);
    }

    //线程开启
    @Override
    public void start() {
        //线程执行run()方法
        Thread thread = new Thread(this);
        thread.start();
    }

    //消息发送方法
    public void multicast(Message message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(Utils.serialize(message));
        DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), groupAddress, port);
        socket.send(packet);
    }

}
