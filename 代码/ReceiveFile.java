/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */
package filetransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//文件接收类
public class ReceiveFile {

    //接收文件索引
    private static long receiveFileId = 0;
    private final long fileId;
    //文件大小
    private long fileSize;
    private final File localFile;
    private long lastReceived;
    private long totalReceived;
    //文件输出流
    private FileOutputStream fout;


    public ReceiveFile(String path, long fileSize) {
        //当有ReceiveFile创建
        receiveFileId += 1;
        //设置文件id
        this.fileId = receiveFileId;
        //设置文件大小
        this.fileSize = fileSize;
        //文件
        this.localFile = new File(path);
        //接收到的总文件大小
        totalReceived = 0;
        lastReceived = System.currentTimeMillis();
    }
    //返回文件ID
    public long getFileId() {
        return fileId;
    }

    //返回文件
    public File getLocalFile() {
        return localFile;
    }

    public long getLastReceived() {
        return lastReceived;
    }

    public long getTotalReceived() {
        return totalReceived;
    }

    //将接收到的文件数据，写入文件中
    public boolean appendToFile(byte[] data, int offset, int length) throws IOException {
        if (totalReceived == 0) {
            fout = new FileOutputStream(localFile);
        }
        fout.write(data, offset, length);
        fout.flush();
        lastReceived = System.currentTimeMillis();
        totalReceived += length;
        return true;
    }

    //结束文件写入方法
    public boolean endReceive() {
        try {
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //文件大小等于接收到的文件大小，返回TRUE
        //不相等，返回FALSE
        return totalReceived == fileSize;
    }

}
