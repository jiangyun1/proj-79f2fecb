/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */

package filetransfer;

import multicast.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * UDP文件发送
 */
public class UDPFileSender implements Runnable {

    //目标用户
    private final User user;
    //套接字
    private final DatagramSocket socket;
    private final InetAddress address;
    //待发送的文件
    private final File localFile;
    //数据包大小
    private final int packetSize;
    //用于在窗口打印日志的接口
    private final MyMessageLogger messageLogger;

    public UDPFileSender(User user, File localFile, MyMessageLogger messageLogger) throws IOException {
        this.user = user;
        this.socket = new DatagramSocket();
        // 设置接收/发送数据超时时间
        this.socket.setSoTimeout(1000);
        this.address = InetAddress.getByName(user.getIPAddress());
        this.localFile = localFile;
        this.packetSize = 1024;
        this.messageLogger = messageLogger;
    }

    @Override
    public void run() {
        //在面板中打印日志
        messageLogger.logMessage("开始发送: " + localFile.getName() + "  >>>  " + user.getName());
        //设置文件重发次数
        int tries = 0;
        try {
            while (true) {
                try {
                    //调用文件发送方法
                    sendFile();
                    break;
                } catch (SocketTimeoutException | PortUnreachableException ex) {
                    //在文件传输过程中，只要socket未接收到应答数据包，表示文件传输出现错误，进行重传
                    ++tries;
                    messageLogger.logMessage("发送出错: " + localFile.getName() + "  >>>  " + user.getName() + ", 尝试 " + tries + " 次");
                    if (tries == 3) {
                        //当重传达到三次还未成功，显示发送失败
                        messageLogger.logMessage("发送失败: " + localFile.getName() + "  >>>  " + user.getName());
                        break;
                    }
                }
            }
            //关闭套接字
            stop();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void stop(){
        if (!socket.isClosed())
            socket.close();
    }

    //发送文件方法
    private void sendFile() throws IOException {
        //调用文件信息发送方法，返回文件索引
        long fileId = beginFileTrans();
        // 文件分片序号
        long sequenceNum = 0;
        //
        byte[] data = new byte[packetSize - 17];

        try (FileInputStream fin = new FileInputStream(localFile)) {
            while (true) {
                //从文件中读取数据
                int dataLength = fin.read(data, 0, data.length);
                //读到文件末尾
                if (dataLength == -1) {
                    //调用文件结尾数据发送方法
                    endFileTrans(fileId);
                    break;
                } else {
                    //调用文件数据发送方法
                    long ReceivedSequenceNum=sendFileConent(fileId, data, dataLength, sequenceNum);
                    //在面板中打印日志
                    messageLogger.logMessage("发送中: " + localFile.getName() + "  >>>  " + user.getName() + ", " + ReceivedSequenceNum);
                    ++sequenceNum;
                }
            }
        }
    }

    //文件信息发送方法，发送文件名以及文件大小信息
    private long beginFileTrans() throws IOException {
        //封装文件信息
        byte[] filenameData = localFile.getName().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(filenameData.length + 9)
                .put(FileTransferConfig.BEGIN_FILE_TRANS)
                .putLong(localFile.length())
                .put(filenameData);
        //调用数据发送方法，返回应答数据包
        ByteBuffer ack = sendDataPacket(buffer);
        //返回应答数据包中的文件索引信息
        return ack.getLong(1);
    }

    // 发送文件内容数据方法
    private long sendFileConent(long fileId, byte[] data, int dataLength, long sequenceNum) throws IOException {
        //封装文件数据信息
        ByteBuffer buffer = ByteBuffer.allocate(dataLength + 17)
                .put(FileTransferConfig.FILE_DATA_TRANS)
                .putLong(fileId)
                .putLong(sequenceNum)
                .put(data, 0, dataLength);
        //调用数据发送方法，返回应答数据包
        ByteBuffer ack = sendDataPacket(buffer);
        //返回应答数据包中的文件文件分片数信息
        return ack.getLong(1);
    }

    // 发送文件结束信息发送
    private void endFileTrans(long fileId) throws IOException {
        //封装文件结束信息
        ByteBuffer buffer = ByteBuffer.allocate(9)
                .put(FileTransferConfig.END_FILE_TRANS)
                .putLong(fileId);
        //调用数据发送方法，返回应答数据包
        ByteBuffer ack = sendDataPacket(buffer);
        if(ack.get() == FileTransferConfig.END_FILE_TRANS){
            //在面板中打印日志
            messageLogger.logMessage("发送完成: " + localFile.getName() + "  >>>  " + user.getName());
        }else {
            messageLogger.logMessage("发送出错: " + localFile.getName() + "  >>>  " + user.getName());
        }
    }

    //数据发送方法，返回应答数据包
    private ByteBuffer sendDataPacket(ByteBuffer buffer) throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer.array(), 0, buffer.limit(), address, user.getPort());
        socket.send(packet);
        byte[] replyData = new byte[packetSize];
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length);
        socket.receive(replyPacket);
        ByteBuffer ackBuffer = ByteBuffer.allocate(replyPacket.getLength())
                .put(replyPacket.getData(), 0, replyPacket.getLength());
        ackBuffer.position(0);
        return ackBuffer;
    }
}
