/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */

package multicast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

//组播消息接收+回复线程
public class MulticastMessageReceiver extends MulticastBase<MulticastSocket> implements Runnable {

    public MulticastMessageReceiver(String multicastAddress, int port, int bufferSize, User user, MyMessageHandler messageHandler) throws IOException {
        super(multicastAddress, port, bufferSize, new MulticastSocket(port), user, messageHandler);
    }

    public void start() throws IOException {

        //列出所有组播网络接⼝
        Enumeration<NetworkInterface> networkEnum = NetworkInterface.getNetworkInterfaces();
        while (networkEnum.hasMoreElements()) {
            NetworkInterface network = networkEnum.nextElement();
            if (network.isUp() && !network.isLoopback()) {
                //在指定接口，加入组播组
                socket.joinGroup(new InetSocketAddress(groupAddress, 0), network);
            }
        }
        //线程执行run()方法
        Thread thread = new Thread(this);
        thread.start();
    }

}
