/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */

package multicast;

import java.io.Serializable;

//用户类
public class User implements Serializable, Cloneable {

    //用户名
    private String name;
    //接收文件的IP地址
    private String IPAddress;
    //接收文件的端口号
    private int port;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    //判断是否为同一用户
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User that = (User) obj;
            return name != null && name.equals(that.name) &&
                    IPAddress != null && IPAddress.equals(that.IPAddress) &&
                    port > 0 && port == that.port;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    //复制方法
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Object clone = super.clone();
        User user = new User(name);
        user.IPAddress = IPAddress;
        user.port = port;
        return user;
    }

}
