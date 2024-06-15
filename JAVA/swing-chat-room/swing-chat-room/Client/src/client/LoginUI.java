package client;


import common.CommonUtil;
import common.Constant;
import common.SocketUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

// 登陆界面
public class LoginUI {
    SocketUtil socketUtil;

    public LoginUI(SocketUtil socketUtil) {
        this.socketUtil = socketUtil;
        JFrame frame = new JFrame();
        frame.setTitle("聊天室");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        //设置窗口的大小
        frame.setSize(800, 800);
        Container container = frame.getContentPane();
        frame.setLayout(new GridLayout(5, 1));
        container.setBackground(new Color(168, 238, 232));

        JPanel pan1 = new JPanel();
        JLabel title = new JLabel("请先登陆或注册");
        title.setFont(new Font("宋体", Font.BOLD, 20));
        pan1.add(title);
        container.add(pan1);
        JPanel inputPanel = new JPanel();

        container.add(inputPanel);


        JLabel nameLabel = new JLabel("账号");
        nameLabel.setFont(new Font("宋体", Font.BOLD, 20));
        JTextField name = new JTextField("test", 20);
        inputPanel.add(nameLabel);
        inputPanel.add(name);

        JPanel passwordPanel = new JPanel();
        JLabel idLabel = new JLabel("密码");
        idLabel.setFont(new Font("宋体", Font.BOLD, 20));
        JPasswordField passwordField = new JPasswordField("test", 20);
        passwordField.setEchoChar('*');
        passwordPanel.add(idLabel);
        passwordPanel.add(passwordField);
        container.add(passwordPanel);

        JPanel buttonPanel = new JPanel();
        container.add(buttonPanel);
        JButton loginButton = new JButton("登陆");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 进行校验
                String nameText = name.getText().trim();
                String idText = passwordField.getText().trim();

                if (nameText.equals("") || idText.equals("")) {
                    JOptionPane.showMessageDialog(null, "账号,密码,不能为空!");
                } else {
                    String command = Constant.LOGIN_PREFIX;
                    socketUtil.sendCommand(command, nameText, idText);
                    try {
                        String response = socketUtil.getReader().readLine();
                        System.out.println("response is" + response);
                        if (response.startsWith(Constant.RESPONSE_OK)) {
                            JOptionPane.showMessageDialog(null, "登陆成功!");
                            frame.dispose();
                            new Lobby(socketUtil, nameText);
                        } else {
                            JOptionPane.showMessageDialog(null, CommonUtil.pareseBody(response));
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }


                }

            }
        });
        buttonPanel.add(loginButton);

        JButton registButton = new JButton("注册");
        buttonPanel.add(registButton);
        registButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegistUI(socketUtil);
            }
        });

        JButton exitButton = new JButton("退出");
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);//隐藏窗体
                System.exit(0);//退出程序
            }
        });
        buttonPanel.add(exitButton);

        // 优化界面显示
        frame.pack();
        setWindowsMiddleShow(frame);
    }


    /**
     * 将窗体居中显示
     *
     * @param frame 需要居中显示的窗体
     */
    public static void setWindowsMiddleShow(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2,
                frame.getWidth(), frame.getHeight());
    }
}