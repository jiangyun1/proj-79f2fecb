/**
 * 考试科目：计算机网络程序设计
 * 姓名：何颖玺
 * 学号：2019302110068
 * 班级：19级广播电视工程互联网媒体技术方向
 */
package filetransfer;

import multicast.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 用户登录窗口
 */
public class LoginFrame extends JFrame implements ActionListener {

    private JButton loginButton;
    private JLabel label;
    private JTextField textField;

    public LoginFrame() {
        initComponents();
    }

    //初试化窗口布局
    private void initComponents() {
        //使用约束布局
        GridBagConstraints gridBagConstraints;

        label = new JLabel("用户姓名");
        textField = new JTextField();
        loginButton = new JButton("登录");


        Container container = getContentPane();
        container.setLayout(new GridBagLayout());
        gridBagConstraints= new GridBagConstraints();;
        gridBagConstraints.insets = new Insets(30, 39, 0, 0);
        container.add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 95;
        gridBagConstraints.insets = new Insets(27, 12, 23, 0);
        container.add(textField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new Insets(27, 12, 23, 40);
        container.add(loginButton, gridBagConstraints);

        loginButton.addActionListener(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
    }

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                //显示登录窗口
                new LoginFrame().setVisible(true);
            }
        });
    }

    //当按下loginButton时
    @Override
    public void actionPerformed(ActionEvent e) {
        String username = textField.getText();
        //用户名为空
        if (username.length() == 0) {
            //弹出提示对话框
            JOptionPane.showMessageDialog(this, "请输入用户姓名");
        } else {
            try {
                //显示文件传输页面
                new FileTransferFrame(new User(username)).setVisible(true);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            //关闭登录窗口
            dispose();
        }
    }

}
