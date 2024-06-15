package client;


import common.Constant;
import common.SocketUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

// 注册界面
public class RegistUI {
    private SocketUtil socketUtil;

    public RegistUI(SocketUtil socketUtil) {
        JFrame frame = new JFrame();
        frame.setTitle("注册");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        //设置窗口的大小
        frame.setSize(600, 600);
        Container container = frame.getContentPane();
        frame.setLayout(new GridLayout(5, 1));


        JPanel pan1 = new JPanel();

        JLabel title = new JLabel("注册到聊天室");
        title.setFont(new Font("宋体", Font.BOLD, 20));
        pan1.add(title);
        container.add(pan1);
        JPanel inputPanel = new JPanel();
        container.add(inputPanel);

        JLabel nameLabel = new JLabel("账   号");
        nameLabel.setFont(new Font("宋体", Font.BOLD, 20));
        JTextField account = new JTextField(20);
        inputPanel.add(nameLabel);
        inputPanel.add(account);

        JPanel passwordPanel = new JPanel();
        JLabel idLabel = new JLabel("密   码");
        idLabel.setFont(new Font("宋体", Font.BOLD, 20));
        JPasswordField passwordField = new JPasswordField("", 20);
        passwordField.setEchoChar('*');
        passwordPanel.add(idLabel);
        passwordPanel.add(passwordField);
        container.add(passwordPanel);

        JPanel passwordComfirm_Panel = new JPanel();
        JLabel passwordComfirm_Label = new JLabel("确认密码");
        passwordComfirm_Label.setFont(new Font("宋体", Font.BOLD, 20));
        JPasswordField passwordConfirm_Field = new JPasswordField("", 20);
        passwordConfirm_Field.setEchoChar('*');
        passwordComfirm_Panel.add(passwordComfirm_Label);
        passwordComfirm_Panel.add(passwordConfirm_Field);
        container.add(passwordComfirm_Panel);

        JPanel buttonPanel = new JPanel();
        container.add(buttonPanel);
        JButton loginButton = new JButton("注册");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 进行校验
                String accountText = account.getText().trim();
                String passwordText = passwordField.getText().trim();
                String passwordConfirmText = passwordConfirm_Field.getText().trim();
                if (accountText.equals("") || passwordText.equals("")) {
                    JOptionPane.showMessageDialog(null, "账号,密码,不能为空!");
                } else if (!passwordConfirmText.equals(passwordText)) {
                    JOptionPane.showMessageDialog(null, "两次密码输入不一致!");
                } else {
                    // 注册逻辑
                    BufferedWriter writer = socketUtil.getWriter();
                    if (writer == null) {
                        JOptionPane.showMessageDialog(null, "网络连接失败请检查socket端口是否正确!");
                        return;
                    }
                    String registCommand = Constant.REGISTER_PREFIX + accountText + "&" + passwordText + "\n";
                    System.out.println(registCommand);
                    try {
                        writer.write(registCommand);
                        writer.flush();
                        BufferedReader reader = socketUtil.getReader();
                        String response = reader.readLine();
                        System.out.println(response);
                        if (response.startsWith(Constant.RESPONSE_OK)) {
                            JOptionPane.showMessageDialog(null, "注册成功!请登录");
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, response.split("@")[1]);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "网络连接失败,注册失败!");
                        return;
                    }
                    //   new ReadyToTest(new User(accountText, passwordText, classText, null));
                }

            }
        });
        buttonPanel.add(loginButton);

        // 优化界面显示
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((screenSize.width - frame.getWidth()) / 2 - 30,
                (screenSize.height - frame.getHeight()) / 2 - 30,
                frame.getWidth(), frame.getHeight());
    }


}