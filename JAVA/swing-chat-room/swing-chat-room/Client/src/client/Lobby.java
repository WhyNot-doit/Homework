package client;

import common.CommonUtil;
import common.Constant;
import common.LogHtmlTemplate;
import common.SocketUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class Lobby {
    private JTextField textField1;
    private JButton sendMessageButton;
    private JButton sendFileButton;
    private JLabel title;
    private JButton log;
    private JTree chatTargetTree;
    private JPanel rootPanel;
    private JPanel channelPanel;
    private JPanel leftPanel;
    private JScrollPane comunicatePanel;
    private JPanel bottomPanel;
    private JTextPane textPane1;
    private JTextField newFriend;
    private JButton addFriendBtn;
    private JPanel friendPanel;
    private SocketUtil socketUtil;
    private String chatTarget;
    private Map<String, StringBuilder> chatLogMap;
    private Map<String, String> chatPanelLogMap;
    private InfoUtil infoUtil;
    // 是否正在传输文件,如果传输文件尚未完成不允许再次传输文件
    private boolean transferFlg;
    // 发送文件的路径
    private String transferFilePath;
    private JPopupMenu popMenu;

    public Lobby(SocketUtil socketUtil, String username) {
        {
            infoUtil = new InfoUtil();
            this.socketUtil = socketUtil;
            chatLogMap = new HashMap<>();
            chatPanelLogMap = new HashMap<>();
            // jtree是用idea的swing desiner 自动生成的,里面填了假数据需要清空
            DefaultTreeModel model = (DefaultTreeModel) chatTargetTree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            root.setUserObject("联系人");
            root.removeAllChildren();
            addFriends(root);
            chatTargetTree.updateUI();
            textPane1.setContentType("text/html");
            JFrame frame = new JFrame("聊天室(当前登陆用户:" + username + ")");
            frame.setContentPane(rootPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setSize(600, 800);
            frame.pack();


            //读取聊天记录到内存中
            readChatLog(username);

            //添加菜单项以及为菜单项添加事件
            popMenu = new JPopupMenu();

            JMenuItem delItem = new JMenuItem("删除好友");
            JMenuItem delSingleItem = new JMenuItem("删除所选对象的聊天记录");
            JMenuItem clearAllItem = new JMenuItem("清空所有的聊天记录");
            ActionListener popmenuActionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) chatTargetTree

                            .getLastSelectedPathComponent();  //得到右键选中的节点

                    if (e.getSource() == delItem) {
                        System.out.println(node.getDepth());
                        // 选中的不是叶子节点
                        if (node.getDepth() != 0
                        ) {
                            return;
                        }
                        String userobj = (String) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                        if (!userobj.equals("离线好友") && !userobj.equals("在线好友")) {
                            // 选中的不是在线好友或者离线好友列表中,不进行处理
                            return;
                        }
                        String friend = (String) node.getUserObject();
                        System.out.println(friend);
                        // 发出删除好友请求
                        socketUtil.sendCommand(Constant.DELETE_PREFIX, friend);
                        ((DefaultTreeModel) chatTargetTree.getModel()).removeNodeFromParent(node);

                    } else if (e.getSource() == delSingleItem) {
                        System.out.println(node.getDepth());
                        // 选中的不是叶子节点
                        if (node.getDepth() != 0
                        ) {
                            return;
                        }
                        String userParentobj = (String) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                        if (!userParentobj.equals("离线好友") && !userParentobj.equals("在线好友") && !node.getUserObject().equals("聊天室")) {
                            // 必须选中好友或者聊天室
                            return;
                        }
                        String friend = (String) node.getUserObject();
                        System.out.println(friend);
                        chatLogMap.remove(friend);
                        chatPanelLogMap.remove(friend);
                        textPane1.setText("");
                        File file =
                                new File(Constant.CHATLOG_PATH + File.separatorChar + username + File.separatorChar + friend +
                                        ".html");
                        file.delete();
                    } else if (e.getSource() == clearAllItem) {
                        // 清空所有聊天记录
                        chatLogMap.clear();
                        chatPanelLogMap.clear();
                        textPane1.setText("");

                        File files = new File(Constant.CHATLOG_PATH + File.separatorChar + username);
                        if (!files.exists()) {
                            return;
                        }
                        File[] childrenfiles = files.listFiles();
                        if (childrenfiles == null) {
                            return;
                        }
                        for (File file :
                                childrenfiles) {
                            if (file.getName().endsWith("html"))
                                file.delete();
                        }

                    }
                }
            };
            delItem.addActionListener(popmenuActionListener);
            delSingleItem.addActionListener(popmenuActionListener);
            clearAllItem.addActionListener(popmenuActionListener);
            popMenu.add(delItem);
            popMenu.add(delSingleItem);
            popMenu.add(clearAllItem);
            //设置选择模式,如果要选中节点，必须要有这个
            TreeSelectionModel selectionModel = chatTargetTree.getSelectionModel();
            selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);  //单选模式
            sendMessageButton.setEnabled(false);
            sendFileButton.setEnabled(false);
            chatTargetTree.setEditable(false);
            //设置点击jtree节点的事件,根据点击的节点来更新 选择的聊天对象,更新聊天面板,发送信息和发送文件按钮的状态
            chatTargetTree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    //把当前选中节点的路径，显示到文本域中
                    //e里面有事件源发生的所有信息
                    TreePath newLeadSelectionPath = e.getNewLeadSelectionPath();
                    if (newLeadSelectionPath == null) {
                        return;
                    }
                    int pathCount = newLeadSelectionPath.getPathCount();

                    //将treePath转换成字符串，显示到文本域中
                    // 拿到点击路径
                    String s = newLeadSelectionPath.toString();
                    // 四级目录且是在线好友 (联系人=>我的好友=>在线好友=>xxx)可以发送消息和文件
                    if (pathCount == 4) {
                        // 切分出点击路径中最后一个项目即为点击的内容
                        int index = s.lastIndexOf(",");
                        chatTarget = s.substring(index + 1, s.length() - 1).trim();
                        if (s.contains("在线好友")) {
                            title.setText("当前聊天对象:" + chatTarget);
                            sendMessageButton.setEnabled(true);
                            sendFileButton.setEnabled(true);
                        } else {
                            title.setText("当前聊天对象:" + chatTarget + "(已离线)");
                            sendMessageButton.setEnabled(false);
                            sendFileButton.setEnabled(false);
                        }
                        if (chatPanelLogMap.get(chatTarget) == null) {
                            textPane1.setText("");
                        } else {
                            textPane1.setText(chatPanelLogMap.get(chatTarget));
                        }
                    } else if (s.contains("聊天室")) {
                        chatTarget = "聊天室";
                        title.setText("当前聊天频道为:聊天室");
                        if (chatPanelLogMap.get("聊天室") == null) {
                            textPane1.setText("");
                        } else {
                            textPane1.setText(chatPanelLogMap.get("聊天室"));
                        }
                        sendMessageButton.setEnabled(true);
                        sendFileButton.setEnabled(false);
                    } else {
                        chatTarget = "";
                        title.setText("未选择聊天对象");
                        textPane1.setText("");
                        sendMessageButton.setEnabled(false);
                        sendFileButton.setEnabled(false);
                    }

                }
            });
        }

        // 点击发送消息按钮
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField1.getText();
                System.out.println(chatTarget);
                chatTarget = chatTarget.trim();
                if (chatTarget.equals("聊天室")) {
                    socketUtil.sendCommand(Constant.GROUP_CHAT_PREFIX, text);
                } else {
                    socketUtil.sendCommand(Constant.CHAT_PREFIX, chatTarget, text);
                }

                StringBuilder sb = chatLogMap.getOrDefault(chatTarget, new StringBuilder());
                chatLogMap.put(chatTarget, sb);
                String html = LogHtmlTemplate.appendContentTohtml(sb, LogHtmlTemplate.SEND, username, text);
                textPane1.setText(html);
                chatPanelLogMap.put(chatTarget, html);
                textField1.setText("");
            }
        });


        //启动一个新线程接收处理服务器发来的消息
        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    try {
                        BufferedReader reader = socketUtil.getReader();
                        while (true) {
                            String s = reader.readLine();
                            System.out.println(s);
                            if (s == null) {
                                continue;
                            }
                            int c = s.indexOf("@");
                            if (c != -1) {
                                // 切出命令前缀
                                String command = s.substring(0, c + 1);
                                // 切出请求体
                                String body = s.substring(c + 1);
                                System.out.println(command);
                                System.out.println(body);
                                switch (command) {
                                    case Constant.CHAT_PREFIX:
                                        System.out.println(body);
                                        String[] split = body.split("&");

                                        StringBuilder sb = chatLogMap.getOrDefault(split[0], new StringBuilder());
                                        chatLogMap.put(split[0], sb);
                                        String html = LogHtmlTemplate.appendContentTohtml(sb, LogHtmlTemplate.REC,
                                                split[0], split[1]);
                                        chatPanelLogMap.put(split[0], html);

                                        if (chatTarget == null || !chatTarget.trim().equals(split[0])) {
                                            CommonUtil.playMsgSound("msg.wav");
                                            infoUtil.show("收到了一条新消息", split[0] + "给你发送了一条新消息");
                                        } else {
                                            textPane1.setText(html);
                                        }
                                        break;

                                    case Constant.ADD_PREFIX:
                                        String[] addResult = body.split("&");
                                        //有新好友
                                        DefaultMutableTreeNode newfriend = new DefaultMutableTreeNode(addResult[0]);
                                        if (addResult[1].equals("1")) {
                                            addNodeToOnlineFriend(newfriend);
                                        } else {
                                            addNodeToOfflineFriend(newfriend);
                                        }
                                        chatTargetTree.updateUI();
                                        infoUtil.show("提醒", "添加好友成功!");
                                        break;
                                    case Constant.DELETE_PREFIX:
                                        deleteNodefromOnlineFriend(body);
                                        chatTargetTree.updateUI();
                                        infoUtil.show("提醒", body + "已将你从好友列表移除");
                                        break;
                                    case Constant.NOTIFY_PREFIX:
                                        System.out.println(body);
                                        if (body.startsWith("online=")) {
                                            // 上线的好友
                                            String substring = body.substring(7);

                                            DefaultTreeModel model = (DefaultTreeModel) chatTargetTree.getModel();
                                            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                                            // 我的好友
                                            DefaultMutableTreeNode friends =
                                                    (DefaultMutableTreeNode) root.getChildAt(1);
                                            DefaultMutableTreeNode online =
                                                    (DefaultMutableTreeNode) friends.getChildAt(0);
                                            online.add(new DefaultMutableTreeNode(substring));

                                            // 离线好友
                                            DefaultMutableTreeNode offline =
                                                    (DefaultMutableTreeNode) friends.getChildAt(1);
                                            Enumeration children = offline.children();
                                            while (children.hasMoreElements()) {
                                                DefaultMutableTreeNode childeren =
                                                        (DefaultMutableTreeNode) children.nextElement();
                                                String userObject = (String) childeren.getUserObject();
                                                if (userObject.trim().equals(substring)) {
                                                    // 从离线好友列表中删除
                                                    offline.remove(childeren);
                                                    break;
                                                }
                                            }
                                            infoUtil.show("好友上线提醒", substring + "上线了!");
                                            chatTargetTree.updateUI();
                                        } else if (body.startsWith("offline=")) {
                                            // 下线的好友
                                            String substring = body.substring(8);
                                            DefaultTreeModel model = (DefaultTreeModel) chatTargetTree.getModel();
                                            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                                            // 我的好友
                                            DefaultMutableTreeNode friends =
                                                    (DefaultMutableTreeNode) root.getChildAt(1);
                                            // 离线好友
                                            DefaultMutableTreeNode offline =
                                                    (DefaultMutableTreeNode) friends.getChildAt(1);
                                            offline.add(new DefaultMutableTreeNode(substring));
                                            // 在线好友列表中移除
                                            DefaultMutableTreeNode online =
                                                    (DefaultMutableTreeNode) friends.getChildAt(0);
                                            Enumeration children = online.children();
                                            while (children.hasMoreElements()) {
                                                DefaultMutableTreeNode childeren =
                                                        (DefaultMutableTreeNode) children.nextElement();
                                                String userObject = (String) childeren.getUserObject();
                                                if (userObject.trim().equals(substring)) {
                                                    // 从离线好友列表中删除
                                                    online.remove(childeren);
                                                    break;
                                                }
                                            }
                                            infoUtil.show("好友下线提醒", substring + "下线了!");
                                            chatTargetTree.updateUI();
                                        } else {
                                            infoUtil.show("收到了一条新消息", body);
                                        }
                                        break;
                                    case Constant.GROUP_CHAT_PREFIX:

                                        String[] fromAndMessage = body.split("&");
                                        StringBuilder groupchat = chatLogMap.getOrDefault("聊天室",
                                                new StringBuilder());
                                        chatLogMap.put("聊天室", groupchat);
                                        String grouphtml = LogHtmlTemplate.appendContentTohtml(groupchat,
                                                LogHtmlTemplate.REC,
                                                fromAndMessage[0], fromAndMessage[1]);
                                        chatPanelLogMap.put("聊天室", grouphtml);
                                        if (chatTarget == null || !chatTarget.trim().equals("聊天室")) {
                                            CommonUtil.playMsgSound("msg.wav");
                                            infoUtil.show("收到了一条新消息", "有新的群聊消息!");
                                        } else {
                                            textPane1.setText(grouphtml);
                                        }
                                        break;
                                    case Constant.READY_SEND:
                                        sendFile(transferFilePath, body);
                                        break;
                                    case Constant.READY_RECEIVE:
                                        String[] parameters = body.split("&");
                                        receiveFile(parameters[1], parameters[0], parameters[2]);
                                        break;
                                    default:
                                        System.out.println("命令错误" + s);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (transferFlg) {
                    return;
                }
                chatTarget = chatTarget.trim();
                JFileChooser chooser = new JFileChooser();             //设置选择器
                chooser.setMultiSelectionEnabled(false);             //设为多选
                int returnVal = chooser.showOpenDialog(sendFileButton);        //是否打开文件选择框
                if (returnVal == JFileChooser.APPROVE_OPTION) {          //如果符合文件类型
                    String filepath = chooser.getSelectedFile().getAbsolutePath();      //获取绝对路径
                    transferFilePath = filepath;
                    System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
                    socketUtil.sendCommand(Constant.FILE_PREFIX, chatTarget, chooser.getSelectedFile().getName());
                    sendFileButton.setText("文件传输中");

                    StringBuilder sb = chatLogMap.getOrDefault(chatTarget, new StringBuilder());
                    String html = LogHtmlTemplate.appendContentTohtml(sb, LogHtmlTemplate.SEND, username, filepath);
                    textPane1.setText(html);
                    chatPanelLogMap.put(chatTarget, html);
                    transferFlg = true;
                }
            }
        });
        chatTargetTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                TreePath path = chatTargetTree.getPathForLocation(e.getX(), e.getY()); // 关键是这个方法的使用

                if (path == null) {  //JTree上没有任何项被选中
                    return;
                }
                chatTargetTree.setSelectionPath(path);
                if (e.getButton() == 3) {
                    popMenu.show(chatTargetTree, e.getX(), e.getY());
                }
            }
        });
        addFriendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newuser = newFriend.getText().trim();
                if (newuser.isEmpty()) {
                    return;
                }
                socketUtil.sendCommand(Constant.ADD_PREFIX, newuser);
                newFriend.setText("");
            }
        });

        // 查看聊天记录
        log.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 调用cmd自动打开接收到的文件目录
                File file = new File(Constant.CHATLOG_PATH + File.separatorChar + username);
                try {
                    String chatlog = "";
                    if (chatTarget != null && !chatTarget.isEmpty() && chatPanelLogMap.containsKey(chatTarget)) {
                        chatlog = chatTarget.trim() + ".html";
                    }
                    Runtime.getRuntime().exec("explorer.exe /select, " + file.getAbsolutePath() + File.separatorChar + chatlog);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        // 启动定时任务每隔五秒钟将聊天记录刷到文件中
        java.util.Timer timer = new java.util.Timer();

        // 开启定时执行任务,每过五秒,刷新用户信息到文件中,
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                File file = new File(Constant.CHATLOG_PATH + File.separatorChar + username);
                file.mkdirs();
                String absoulutePath =
                        file.getAbsolutePath();
                try {
                    for (String key :
                            chatPanelLogMap.keySet()) {
                        FileOutputStream fileOutputStream =
                                new FileOutputStream(absoulutePath + File.separatorChar + key + ".html");
                        fileOutputStream.write(chatPanelLogMap.get(key).getBytes(StandardCharsets.UTF_8));
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("刷新文件失败");
                }
            }
        }, 5000, 5000);
    }

    private void deleteNodefromOnlineFriend(String body) {
        DefaultTreeModel model = (DefaultTreeModel) chatTargetTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        // 我的好友
        DefaultMutableTreeNode friends =
                (DefaultMutableTreeNode) root.getChildAt(1);
        // 在线好友列表中移除
        DefaultMutableTreeNode online =
                (DefaultMutableTreeNode) friends.getChildAt(0);
        Enumeration children = online.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode childeren =
                    (DefaultMutableTreeNode) children.nextElement();
            String userObject = (String) childeren.getUserObject();
            if (userObject.trim().equals(body)) {
                // 从离线好友列表中删除
                online.remove(childeren);
                break;
            }
        }
    }

    // 添加节点到在线好友上
    private void addNodeToOnlineFriend(DefaultMutableTreeNode newfriend) {
        DefaultTreeModel model = (DefaultTreeModel) chatTargetTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        // 我的好友
        DefaultMutableTreeNode friends =
                (DefaultMutableTreeNode) root.getChildAt(1);
        DefaultMutableTreeNode online =
                (DefaultMutableTreeNode) friends.getChildAt(0);
        online.add(newfriend);
    }

    // 添加节点到离线好友上
    private void addNodeToOfflineFriend(DefaultMutableTreeNode newfriend) {
        DefaultTreeModel model = (DefaultTreeModel) chatTargetTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        // 我的好友
        DefaultMutableTreeNode friends =
                (DefaultMutableTreeNode) root.getChildAt(1);
        // 离线好友
        DefaultMutableTreeNode offline =
                (DefaultMutableTreeNode) friends.getChildAt(1);
        offline.add(newfriend);
    }


    // 请求服务器拉取好友列表
    public void addFriends(DefaultMutableTreeNode root) {

        String request = Constant.PULL_PREFIX;
        socketUtil.sendCommand(request);
        String s;
        try {
            s = socketUtil.getReader().readLine();
            if (s == null) {
                root.add(new DefaultMutableTreeNode("获取联系人失败"));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            root.add(new DefaultMutableTreeNode("获取联系人失败"));
            return;
        }
        DefaultMutableTreeNode friends = new DefaultMutableTreeNode("我的好友");
        DefaultMutableTreeNode online = new DefaultMutableTreeNode("在线好友");
        DefaultMutableTreeNode offline = new DefaultMutableTreeNode("离线好友");
        int c = s.indexOf("@");
        if (s != null && c != -1) {
            // 切出响应体
            String body = s.substring(c + 1);
            String[] split = body.split("&");
            if (split[0].length() > 7) {
                String[] onlinefriends = split[0].substring(7).split(",");
                for (int i = 0; i < onlinefriends.length; i++) {
                    online.add(new DefaultMutableTreeNode(onlinefriends[i]));
                }
            }
            if (split[1].length() > 8) {
                String[] offlinefriends = split[1].substring(8).split(",");
                for (int i = 0; i < offlinefriends.length; i++) {
                    offline.add(new DefaultMutableTreeNode(offlinefriends[i]));
                }
            }

        }
        DefaultMutableTreeNode groupchat = new DefaultMutableTreeNode("聊天室");
        friends.add(online);
        friends.add(offline);
        root.add(groupchat);
        root.add(friends);
    }

    // 向服务端传输文件
    public void sendFile(String url, String port) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket tempsocket;
                try {
                    tempsocket = new Socket(Constant.SERVERHOST, Integer.valueOf(port));
                } catch (IOException e) {
                    e.printStackTrace();
                    transferFlg = false;
                    sendFileButton.setText("发送文件");
                    infoUtil.show("错误", "建立连接失败,无法发送文件");
                    return;
                }
                System.out.println("成功连接服务端准备发送");
                FileInputStream fis = null;
                BufferedOutputStream bos = null;
                File file = new File(url);
                try {
                    fis = new FileInputStream(file);
                    bos = new BufferedOutputStream(tempsocket.getOutputStream());//client.getOutputStream()
                    // 返回此套接字的输出流
                    // 开始传输文件
                    System.out.println("======== 开始传输文件 ========");
                    byte[] bytes = new byte[1024];
                    int length;

                    while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                        bos.write(bytes, 0, length);
                        bos.flush();
                    }
                    // infoUtil.show("提示", "文件传输完成");
                    System.out.println("======== 文件传输成功 ========");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端文件传输异常");
                } finally {
                    transferFlg = false;
                    sendFileButton.setText("发送文件");
                    try {
                        if (fis != null) {
                            fis.close();
                        }

                        if (bos != null) {
                            bos.close();
                        }

                        tempsocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    // 向服务端接收文件
    public void receiveFile(String port, String from, String filename) throws IOException {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Socket tempsocket;
                try {
                    tempsocket = new Socket(Constant.SERVERHOST, Integer.valueOf(port));
                } catch (IOException e) {
                    e.printStackTrace();
                    infoUtil.show("错误", "接收文件失败,创立连接失败");
                    return;
                }
                System.out.println("成功连接服务端准备接收");
                BufferedInputStream bis = null;
                FileOutputStream fos = null;

                try {
                    bis = new BufferedInputStream(tempsocket.getInputStream());


                    File directory = new File(Constant.DOWNLOAD_PATH);
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    File file = new File(directory.getAbsolutePath() + File.separatorChar + filename);
                    System.out.println("file" + file);
                    fos = new FileOutputStream(file);

                    // 开始接收文件
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = bis.read(bytes, 0, bytes.length)) != -1) {
                        fos.write(bytes, 0, length);
                        fos.flush();
                    }
                    StringBuilder sb = chatLogMap.getOrDefault(from, new StringBuilder());
                    String html = LogHtmlTemplate.appendContentTohtml(sb, LogHtmlTemplate.REC, from,
                            file.getAbsolutePath());
                    if (chatTarget != null && chatTarget.trim().equals(from)) {
                        textPane1.setText(html);
                    }
                    chatPanelLogMap.put(from, html);
                    System.out.println("======== 文件接收成功 [File Name：" + filename + "] ");
                    infoUtil.show("提示", "收到了" + from + "发送的文件: " + file.getAbsolutePath() + "\n正在接收请稍候");
                    // 调用cmd自动打开接收到的文件目录
                    Runtime.getRuntime().exec("explorer.exe /select, " + file.getAbsolutePath());

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端文件传输异常");
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                        if (bis != null) {
                            bis.close();
                        }
                        tempsocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    // 读取聊天记录文件到内存中
    private void readChatLog(String username) {
        File files = new File(Constant.CHATLOG_PATH + File.separatorChar + username);
        if (!files.exists()) {
            return;
        }
        File[] files1 = files.listFiles();
        if (files1 == null) {
            return;
        }

        for (File file :
                files1) {
            // 读取用户列表
            int filelength = (int) file.length();
            if (filelength == 0) {
                continue;
            }
            byte[] filecontent = new byte[filelength];
            try {
                FileInputStream in = new FileInputStream(file);
                in.read(filecontent);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String s = new String(filecontent, StandardCharsets.UTF_8);
            int index = s.indexOf("<body>");
            System.out.println(index);
            int end = s.lastIndexOf("</body>");
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf(".html"));
            System.out.println(fileName);
            System.out.println(end);
            System.out.println(s.length() - end);
            String substring = s.substring(index + 6, end);
            System.out.println(substring);
            chatLogMap.put(fileName, new StringBuilder(substring));
            chatPanelLogMap.put(fileName, s);
        }


    }

}
