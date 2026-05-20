/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */

package filetransfer;

import multicast.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 文件传输窗口
 */
public class FileTransferFrame extends JFrame {

    //组播地址
    private static final String groupAddress = "224.5.6.7";
    // 组播端口号
    private static final int port = 123456;
    // 组播消息缓冲区大小
    private static final int bufferSize = 1024;
    //用户
    private User user;
    //组播消息接收
    private MulticastMessageReceiver mcReceiver;
    //组播消息发送
    private MulticastMessageSender mcSender;
    // 文件接收
    private UDPFileReceiver fileReceiver;
    //在线用户列表
    private JList<User> userJList;
    private JScrollPane scrollPane;
    private JSplitPane splitPane;
    //文件发送面板
    private FileTransferPanel fileTransferPanel;

    public FileTransferFrame(User user) throws InterruptedException {
        this.user = user;
        setTitle(user.getName());
        //初始化页面布局
        initComponents();
        //开始网络服务
        startNetworkService();
    }



    private void initComponents() {

        userJList = new JList<User>(new DefaultListModel<User>());
        scrollPane = new JScrollPane(userJList);
        scrollPane.setMinimumSize(new Dimension(150, 100));
        scrollPane.setPreferredSize(scrollPane.getMinimumSize());
        //用户列表设置可以多选
        userJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileTransferPanel = new FileTransferPanel();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, fileTransferPanel);
        splitPane.setDividerLocation(150);
        splitPane.setDividerSize(4);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //添加窗口监听
        addWindowListener(new WindowAdapter() {
            @Override
            //当窗口关闭时，组播发送Leave消息，停止网络服务
            public void windowClosing(WindowEvent e) {
                try {
                    mcSender.multicast(new Message(MulticastBase.USER_LEAVE, user));
                    stopNetworkService();
                    System.exit(0);
                } catch (IOException|InterruptedException ex) {
                    ex.printStackTrace();
                }

            }
        });
        setPreferredSize(new Dimension(600, 400));
        pack();
    }

    /**
     * 启动网络相关的服务
     */
    private void startNetworkService() throws InterruptedException {
        try {
            //创建文件接收线程
            fileReceiver = new UDPFileReceiver(fileTransferPanel.receiveFolderField.getText(), 0, fileTransferPanel);
            // 获取自动分配的端口号
            user.setPort(fileReceiver.getSocket().getLocalPort());
            fileTransferPanel.logMessage("文件接收端口: " + user.getPort());
            new Thread(fileReceiver).start();
            // 组播信息发送
            mcSender = new MulticastMessageSender(groupAddress, port, bufferSize, user, new MulticastSenderHandler());
            // 组播信息接收
            mcReceiver = new MulticastMessageReceiver(groupAddress, port, bufferSize, user, new MulticastReceiverHandler());
            mcReceiver.start();
            mcSender.start();
            // 发送组播消息，用户上线
            mcSender.multicast(new Message(MulticastBase.USER_JOIN, user));
        } catch (IOException ex) {
            //出现异常时停止网络服务
            stopNetworkService();
        }
    }

    /**
     * 关闭网络服务
     */
    private void stopNetworkService() throws InterruptedException {

        if (fileReceiver != null) {

            fileReceiver.stop();
        }
        if (mcSender != null) {

            mcSender.stop();
        }
        if (mcReceiver != null) {

            mcReceiver.stop();
        }
    }

    //用户加入，更新界面
    private void userJoin(User joinUser) {
        if (!user.equals(joinUser)) {
            //获取在线用户列表
            DefaultListModel<User> model = (DefaultListModel<User>) userJList.getModel();
            //更新UI
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!model.contains(joinUser)) {
                        fileTransferPanel.logMessage("用户[" + joinUser + "]加入");
                        //在列表中添加对象
                        model.addElement(joinUser);
                    }
                }
            });
        }
    }

    // 用户离开,更新界面
    private void userLeave(User leaveUser) {
        fileTransferPanel.logMessage("用户[" + leaveUser + "]离开");
        //获取在线用户列表
        DefaultListModel<User> model = (DefaultListModel<User>) userJList.getModel();
        //更新UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //从列表中移除对象
                model.removeElement(leaveUser);
            }
        });
    }

    // 发送文件
    private void sendFile(File file) {
        DefaultListModel<User> model = (DefaultListModel<User>) userJList.getModel();
        //获取列表中选中的用户对象索引
        int[] selectedIndices = userJList.getSelectedIndices();
        //未选中接收对象，弹出窗口提示
        if (selectedIndices.length == 0) {
            JOptionPane.showMessageDialog(this, "请选择接收用户");
            return;
        }
        userJList.setEnabled(false);
        fileTransferPanel.setEnabled(false);
        //根据选中的用户对象，创建固定数量的线程的线程池
        ExecutorService executor = Executors.newFixedThreadPool(selectedIndices.length);
        for (int index : selectedIndices) {
            User user = model.getElementAt(index);
            try {
                //创建文件发送线程
                UDPFileSender sender = new UDPFileSender(user, file, fileTransferPanel);
                //任务提交到线程池
                executor.submit(sender);
            } catch (IOException ex) {
                fileTransferPanel.logMessage("发送文件出错:" + ex.getMessage());
                ex.printStackTrace();
            }
        }
        executor.shutdown();
        try {
           // 当前线程阻塞，直到等所有已提交的任务执行完或者等超时时间到,或者当前线程被中断
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        userJList.setEnabled(true);
        fileTransferPanel.setEnabled(true);
    }

    //文件传输面板
    class FileTransferPanel extends JPanel implements MyMessageLogger, ActionListener {

        private JLabel label1;
        private JLabel label2;
        private JScrollPane scrollPane;
        private JButton sendButton;
        private JTextArea logArea;
        private JTextField receiveFolderField;
        private JTextField sendFileField;

        public FileTransferPanel() {
            //初始化面板
            initComponents();
        }

        private void initComponents() {

            label1 = new JLabel("接收文件夹");
            //根据用户名创建接收文件夹
            File directory = new File("G:\\UDPFileTransfer\\User\\"+user.getName());
            if(!directory.exists()) {
                directory.mkdir();
            }
            receiveFolderField = new JTextField(directory.getAbsolutePath());
            //显示接收文件夹，不允许在窗口修改
            receiveFolderField.setEditable(false);

            label2 = new JLabel("发送文件");
            sendFileField = new JTextField();
            sendButton = new JButton("发送");

            scrollPane = new JScrollPane();
            logArea = new JTextArea();
            logArea.setEditable(false);
            logArea.setFont(Font.getFont("微软雅黑"));

            //设置布局
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(9, 17, 0, 0);
            add(label1, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(6, 12, 0, 0);
            add(receiveFolderField, gbc);

            gbc = new GridBagConstraints();
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(9, 16, 0, 0);
            add(label2, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 2;
            gbc.ipadx = 160;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(6, 12, 0, 0);
            add(sendFileField, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 4;
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(6, 6, 0, 16);
            add(sendButton, gbc);

            logArea.setColumns(20);
            logArea.setRows(5);
            scrollPane.setViewportView(logArea);

            gbc = new GridBagConstraints();
            gbc.gridy = 4;
            gbc.gridwidth = 5;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(6, 16, 16, 16);
            add(scrollPane, gbc);

            //为发送按钮添加事件监听
            sendButton.addActionListener(this);
        }

        //事件监听，当按下SendButton时
        @Override
        public void actionPerformed(ActionEvent e) {
                //获取待发送的文件
            File file = new File(sendFileField.getText());
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "文件" + sendFileField.getText() + "不存在，重新输入文件路径");
            } else {
                //调用发送文件方法
                sendFile(file);
            }
        }

        //在logArea中显示日志，更新UI
        @Override
        public void logMessage(String message) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    logArea.append(message + System.lineSeparator());
                }
            });
        }
    }

    /**
     * 组播消息发送处理类
     */
    class MulticastSenderHandler implements MyMessageHandler<MulticastMessageSender> {
        //实现process方法，用于处理MulticastSender接收到的message
        @Override
        public void process(Message message, MulticastMessageSender sender) throws IOException {
            //判断接收到的message类型
            switch (message.getMessageType()) {
                //用户回复消息类型
                case MulticastBase.USER_JOIN_ACK:
                    //获取消息中的用户对象信息
                    User user = (User) message.getPayload();
                    user.setIPAddress(message.getAddress().getHostAddress());
                    //调用用户加入方法，将用户添加到自己的用户列表
                    userJoin(user);
                    break;
            }
        }
    }

    /**
     * 组播消息接收处理类
     */
    class MulticastReceiverHandler implements MyMessageHandler<MulticastMessageReceiver> {
        //实现process方法，用于处理MulticastReceiver接收到的message
        @Override
        public void process(Message message, MulticastMessageReceiver receiver) throws IOException {
            //判断接收到的message类型
            switch (message.getMessageType()) {
                //用户加入信息
                case MulticastBase.USER_JOIN: {
                    //发送文件回复消息
                    receiver.sendMessage(new Message(MulticastBase.USER_JOIN_ACK, receiver.getUser(), message.getAddress(), message.getPort()));
                    //获取接收到的消息中的用户信息
                    User user = (User) message.getPayload();
                    user.setIPAddress(message.getAddress().getHostAddress());
                    //调用用户加入方法，将用户添加到自己的用户列表
                    userJoin(user);
                    break;
                }
                //用户离开信息
                case MulticastBase.USER_LEAVE: {
                    //获取接收到的消息中的用户信息
                    User user = (User) message.getPayload();
                    user.setIPAddress(message.getAddress().getHostAddress());
                    //调用用户离开方法，将用户从自己的用户列表中移除
                    userLeave(user);
                    break;
                }
            }
        }
    }

}
