/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */
package filetransfer;

//定义文件传输数据包的配置信息
public class FileTransferConfig {

    //文件传输开始类型
    public final static byte BEGIN_FILE_TRANS = 1;
    //传输文件内容类型
    public final static byte FILE_DATA_TRANS = 2;
    //文件传输结束
    public final static byte END_FILE_TRANS = 3;

}
