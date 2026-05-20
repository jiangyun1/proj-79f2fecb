/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */
package filetransfer;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * UDP文件接收类，负责文件数据接收
 */
public class UDPFileReceiver implements Runnable {

    //套接字
    private final DatagramSocket socket;
    //本地接收文件的文件夹
    private final String localFolder;
    //接收文件列表
    private final ArrayList<ReceiveFile> files;
    //用于在窗口打印日志的接口
    private final MyMessageLogger messageLogger;
    //数据包大小
    private int packetSize;

    private DatagramPacket datagramPacket;
    private volatile boolean exit = false;

    public UDPFileReceiver(String localFolder, int port, MyMessageLogger messageLogger) throws IOException {
        this.socket = new DatagramSocket(port);
        this.localFolder = localFolder;
        this.messageLogger = messageLogger;
        files = new ArrayList<>();
        packetSize = 1024;
    }

    //返回套接字
    public DatagramSocket getSocket() {
        return socket;
    }


    //线程停止方法
    public void stop() {
        exit=true;
    }

    //线程运行
    @Override
    public void run() {
        byte[] data = new byte[packetSize];
        try {
            while (!exit) {
                DatagramPacket packet = new DatagramPacket(data, data.length);
                //接收数据包
                socket.receive(packet);
                datagramPacket = packet;
                //获取数据包类型
                byte type = data[0];
                //获取数据包中其他数据
                ByteBuffer buffer = ByteBuffer.allocate(packet.getLength() - 1).put(data, 1, packet.getLength() - 1);
                buffer.position(0);
                //根据不同数据包类型，调用不同方法
                switch (type) {
                    //接收到文件信息
                    case FileTransferConfig.BEGIN_FILE_TRANS:
                        beginFileReceive(buffer);
                        break;
                    //接收到文件数据
                    case FileTransferConfig.FILE_DATA_TRANS:
                        saveFileContent(buffer);
                        break;
                    //接收到文件尾
                    case FileTransferConfig.END_FILE_TRANS:
                        endFileReceive(buffer);
                        break;
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //接收文件信息方法，接收文件信息并发送接收应答数据包
    private void beginFileReceive(ByteBuffer buffer) throws IOException {
        //获取接收到的文件大小
        long fileSize = buffer.getLong();
        //获取接收到的文件名
        byte[] data = buffer.array();
        String fileName = new String(data, 8, data.length - 8);
        //在指定文件夹中创建接收文件
        ReceiveFile file = new ReceiveFile(localFolder + File.separatorChar + fileName, fileSize);
        //在接收文件列表中添加文件
        files.add(file);

        //发送响应数据包，其中有接收文件的索引，用于后续文件传输
        ByteBuffer ackBuffer = ByteBuffer.allocate(9)
                .put(FileTransferConfig.BEGIN_FILE_TRANS)
                .putLong(file.getFileId());
        sendAck(ackBuffer);
        //在面板中打印日志
        messageLogger.logMessage("开始接收: " + file.getLocalFile().getName());
    }

    //接收文件数据方法，保存数据到文件并发送接收应答包
    private void saveFileContent(ByteBuffer buffer) throws IOException {
        //获取文件索引
        long fileId = buffer.getLong();
        //获取分片数
        long sequenceNum = buffer.getLong();
        //找到指定的接收文件
        ReceiveFile file = findFile(fileId);
        if (file != null) {
            //获取数据包中的文件数据
            byte[] data = buffer.array();
            //调用文件写入方法，接收到的数据写入
            file.appendToFile(data, 16, data.length - 16);
            //在面板中打印日志
            messageLogger.logMessage("接收: " + file.getLocalFile().getName()+" , "+ sequenceNum);
            //发送响应数据包，其中有接收到文件的分片数，用于后续文件传输
            ByteBuffer ackBuffer = ByteBuffer.allocate(9)
                    .put(FileTransferConfig.FILE_DATA_TRANS)
                    .putLong(sequenceNum);
            sendAck(ackBuffer);
        }
    }

    //结束文件接收方法
    private void endFileReceive(ByteBuffer buffer) throws IOException {
        //获取文件索引
        long fileId = buffer.getLong();
        //找到指定文件
        ReceiveFile file = findFile(fileId);
        if (file != null) {
            //调用endReceive()，如果正常接收返回true
            //当文件接收正确时，发送响应数据包
            if(file.endReceive())
            {
                ByteBuffer ackBuffer = ByteBuffer.allocate(1)
                        .put(FileTransferConfig.END_FILE_TRANS);
                sendAck(ackBuffer);
                //在面板中打印日志
                messageLogger.logMessage("接收完成: " + file.getLocalFile().getName());
            }
            else {
                //文件接收错误时，不发送响应数据包
                //在面板中打印日志
                messageLogger.logMessage("接收失败: " + file.getLocalFile().getName());
            }

        }
    }

    //根据文件索引找到文件
    private ReceiveFile findFile(long fileId) {
        for (ReceiveFile file : files) {
            if (file.getFileId() == fileId) {
                return file;
            }
        }
        return null;
    }

    // 发送应答数据包方法
    private void sendAck(ByteBuffer buffer) throws IOException {
        byte[] data = buffer.array();
        DatagramPacket packet = new DatagramPacket(data, 0, data.length, datagramPacket.getAddress(), datagramPacket.getPort());
        socket.send(packet);
    }

}
