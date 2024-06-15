package server;

import common.CommonUtil;
import common.Constant;
import entity.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 服务端程序入口
public class Main {

    private static Map<String, User> usermap = new HashMap<>();
    // 在线用户map
    private static Map<String, Socket> onlineClientmap = new ConcurrentHashMap<String, Socket>();

    public static void main(String[] args) throws IOException {
        System.out.println("为啥");
        System.out.println(System.getProperty("file.encoding"));
        Main main = new Main();
        // 启动一个线程接收服务器控制台命令
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner sc = new Scanner(System.in);
                while (true) {
                    String s = sc.nextLine();
                    System.out.println("接收到服务器的指令" + s);
                    main.publishMessage(Constant.NOTIFY_PREFIX + "服务器发来的公告:" + s);
                }
            }
        }).start();

        Timer timer = new Timer();
        // 开启定时执行任务,每过五秒,刷新用户信息到文件中,
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream("userdata.txt");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("接入文件失败");
                }
                StringBuilder sb = new StringBuilder();
                for (User user :
                        usermap.values()) {

                    sb.append(user.getAccount());
                    sb.append(" ");
                    sb.append(user.getPassword());
                    sb.append(" ");
                    if (user.getFriends() != null) {
                        for (String friend :
                                user.getFriends()) {
                            sb.append(friend);
                            sb.append(",");
                        }
                        if (user.getFriends().size() > 0) {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                    }
                    sb.append("\n");
                }
                try {
                    fileOutputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.flush();
                    fileOutputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("刷新文件失败");
                }
            }
        }, 10000, 5000);
        main.init();


    }

    public void init() throws IOException {

        readUserData();
        System.out.println("读取用户完成!");
        for (Map.Entry<String, User> single:
        usermap.entrySet() ) {
            System.out.println(single.getKey());
            System.out.println(single.getValue());
        }

       // System.out.println(usermap);
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(Constant.PORT);
            System.out.println("服务器启动完成!");
            while (true) {
                Socket socket = null;
                try {
                    socket = ss.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("connected from " + socket.getRemoteSocketAddress());

                Thread t = new Handler(socket);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readUserData() throws IOException {
        // 读取用户列表
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(new FileInputStream("./userdata.txt"),
                        StandardCharsets.UTF_8));
        String data;
        while ((data = bufferedReader.readLine()) != null) {
            String[] user = data.split(" ");
            Set<String> friends = null;
            if (user.length >= 3) {
                friends = new HashSet<String>();
                Collections.addAll(friends, user[2].split(","));
            }
            usermap.put(user[0], new User(user[0], user[1], friends));
        }
    }

    class Handler extends Thread {
        private Socket sock;
        private String username;

        public Handler(Socket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            try (InputStream input = this.sock.getInputStream()) {
                try (OutputStream output = this.sock.getOutputStream()) {
                    handle(input, output);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    this.sock.close();
                } catch (IOException ioe) {
                }
                System.out.println("有人离线了");
                for (String user : onlineClientmap.keySet()
                ) {
                    if (onlineClientmap.get(user).isClosed()) {
                        String disconnectMessage = Constant.NOTIFY_PREFIX + "offline=" + user;
                        onlineClientmap.remove(user);
                        // 向所有客户端推送离线消息
                        try {
                            publishMessageToFriends(user, disconnectMessage);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }


        private void handle(InputStream input, OutputStream output) throws Exception {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

            while (true) {
                String s = reader.readLine();
                if (s == null) {
                    continue;
                }
                int c = s.indexOf("@");
                if (c != -1) {
                    // 切出命令前缀
                    String command = s.substring(0, c + 1);
                    // 切出请求体
                    String body = s.substring(c + 1);
                    switch (command) {
                        case Constant
                                .CHAT_PREFIX:
                            doChat(writer, body, this.username);
                            break;
                        case Constant
                                .GROUP_CHAT_PREFIX:

                            String groupmessage = CommonUtil.joinRequestBody(Constant.GROUP_CHAT_PREFIX,
                                    this.username, body);
                            publishMessageWithoutSelf(this.username, groupmessage);
                            break;
                        case Constant
                                .REGISTER_PREFIX:
                            doRegister(writer, body);
                            break;
                        case Constant
                                .LOGIN_PREFIX:
                            doLogin(writer, body);
                            break;

                        case Constant
                                .PULL_PREFIX:
                            pullFriends(writer, this.username);
                            break;
                        case Constant
                                .FILE_PREFIX:
                            String fileParameter[] = body.split("&");
                            doDispathFile(writer, this.username, fileParameter[0], fileParameter[1]);
                            break;
                        case Constant.ADD_PREFIX:
                            // 添加好友
                            doAddFriend(writer, username, body);
                            break;
                        case Constant.DELETE_PREFIX:
                            // 删除好友,双向删除
                            usermap.get(username).getFriends().remove(body);
                            usermap.get(body).getFriends().remove(username);

                            // 如果对方在线就告诉对方被删除了
                            if (onlineClientmap.containsKey(body)) {
                                writeMsg(Constant.DELETE_PREFIX + username, onlineClientmap.get(body));
                            }
                            break;
                        default:
                            System.out.println("命令错误");
                            System.out.println("请求命令" + command);
                            System.out.println("请求体" + body);
                            break;
                    }
                }
            }
        }

        // 处理登陆请求
        private void doLogin(BufferedWriter writer, String body) throws IOException {
            String[] user = body.split("&");
            if (user.length == 2) {
                System.out.println("正在判断能否登陆");
                String account = user[0];
                String password = user[1];
                System.out.println(account);
                System.out.println(password);
                if (onlineClientmap.containsKey(account)) {
                    writer.write(Constant.RESPONSE_ERROR + "当前用户已登陆,不允许重复登陆!" + "\n");
                    writer.flush();
                    return;
                }
                if (usermap.containsKey(account) && usermap.get(account).getPassword().equals(password)) {
                    writer.write(Constant.RESPONSE_OK + "\n");
                    writer.flush();
                    this.username = account;
                    // 向所有客户端推送上线消息
                    publishMessageToFriends(account, Constant.NOTIFY_PREFIX + "online=" + user[0]);
                    onlineClientmap.put(user[0], sock);
                } else {
                    writer.write(Constant.RESPONSE_ERROR + "用户名或密码输入错误!" + "\n");
                    writer.flush();
                }

            }
        }

    }

    private void doAddFriend(BufferedWriter writer, String username, String body) throws IOException {
        if (username.equals(body)) {
            // 不能重复添加
            return;
        }
        Set<String> friends1 = usermap.get(username).getFriends();
        if (friends1 == null) {
            friends1 = new HashSet<>();
            usermap.get(username).setFriends(friends1);
        }
        if (friends1.contains(body)) {
            // 已经是好友的话不能再次添加
            return;
        }
        // 添加好友,双向添加
        if (!usermap.containsKey(body)) {
            //不存在的好友不能添加
            writer.write(CommonUtil.joinRequestBody(Constant.NOTIFY_PREFIX,
                    "添加好友失败," + body + "不存在!") + '\n');
            writer.flush();
            return;
        }
        friends1.add(body);
        Set<String> friends = usermap.get(body).getFriends();

        if (friends == null) {
            friends = new HashSet<String>();
            usermap.get(body).setFriends(friends);
        }
        friends.add(username);
        StringBuilder response = new StringBuilder(Constant.ADD_PREFIX);
        response.append(body);
        response.append("&");
        if (onlineClientmap.containsKey(body)) {
            response.append("1");
        } else {
            response.append("0");
        }
        response.append('\n');
        System.out.println(response);
        writer.write(response.toString());
        writer.flush();
        // 如果对方在线告诉对方添加成功
        if (onlineClientmap.containsKey(body)) {
            String tellAdd = Constant.ADD_PREFIX + username + "&" + "1";
            writeMsg(tellAdd, onlineClientmap.get(body));
        }
    }

    // 处理发送文件的请求,服务端开启一个临时socket,告知端口号给发送方和接收方;双方连接完毕后一个发一个收;服务端只转发
    private void doDispathFile(BufferedWriter writer, String send, String receive, String fileName) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket tempServersocket = null;
                Socket recSocket = null;
                Socket sendSocket = null;
                int tempport = Constant.TEMP_PORT;
                try {
                    while (true) {
                        try {
                            tempServersocket = new ServerSocket(tempport);
                            break;
                        } catch (IOException io) {
                            tempport++;
                        }
                    }
                    Integer localPort = tempServersocket.getLocalPort();
                    System.out.println("转发用socket启动完成!");

                    System.out.println(receive);
                    // 通知接收方 告知发送者是谁,接收文件线程的新端口
                    if (onlineClientmap.containsKey(receive)) {
                        writeMsg(CommonUtil.joinRequestBody(Constant.READY_RECEIVE, send, localPort.toString(),
                                        fileName),
                                onlineClientmap.get(receive));
                    }

                    recSocket = tempServersocket.accept();
                    // 通知发送方让其准备连接
                    writer.write(CommonUtil.joinRequestBody(Constant.READY_SEND, localPort.toString()) + '\n');
                    writer.flush();
                    System.out.println("发出了send命令等待客户端连接");
                    sendSocket = tempServersocket.accept();

                    BufferedInputStream bis = new BufferedInputStream(sendSocket.getInputStream());

                    BufferedOutputStream bos = new BufferedOutputStream(recSocket.getOutputStream());

                    // 开始转发流
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    while ((length = bis.read(bytes, 0, bytes.length)) != -1) {
                        bos.write(bytes, 0, length);
                        bos.flush();
                    }
                    bos.close();
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (tempServersocket != null) {
                            tempServersocket.close();
                        }
                        if (recSocket != null) {
                            recSocket.close();
                        }
                        if (recSocket != null) {
                            sendSocket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    // 推送消息给所有在线者除自己(发送群聊消息用)
    private void publishMessageWithoutSelf(String username, String groupmessage) {
        {
            try {
                for (String key :
                        onlineClientmap.keySet()) {
                    if (!key.equals(username) && onlineClientmap.get(key).isConnected()) {
                        writeMsg(groupmessage, onlineClientmap.get(key));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // 处理一对一的聊天消息的转发
    private void doChat(BufferedWriter writer, String body, String username) throws Exception {

        String[] split = body.split("&");
        String toTarget = split[0];
        String message = split[1];
        System.out.println(toTarget);
        System.out.println(message);
        message = CommonUtil.joinRequestBody(Constant.CHAT_PREFIX, username, message);
        System.out.println(message);
        Socket socket = onlineClientmap.get(toTarget);
        writeMsg(message, socket);

    }

    // 推送消息给所有在线好友(上线下线通知)
    private void publishMessageToFriends(String username, String Message) throws IOException {
        // 该用户的好友
        Set<String> friends = usermap.get(username).getFriends();
        if (friends == null) {
            return;
        }
        for (String friend : friends
        ) {
            if (onlineClientmap.containsKey(friend)) {
                Socket socket = onlineClientmap.get(friend);
                writeMsg(Message, socket);
            }
        }
    }

    // 处理注册请求
    private void doRegister(BufferedWriter writer, String body) throws IOException {
        String[] user = body.split("&");
        if (user.length == 2) {
            String account = user[0];
            String password = user[1];

            if (!usermap.containsKey(account)) {
                String response = Constant.RESPONSE_OK + "注册成功\n";
                System.out.println(response);
                writer.write(response);
                usermap.put(account, new User(account, password, null));
            } else {
                writer.write(Constant.RESPONSE_ERROR + "用户名重复!" + "\n");
            }
            writer.flush();
        }
    }

    // 返回好友列表
    private void pullFriends(BufferedWriter writer, String username) throws IOException {
        System.out.println(username);
        if (usermap.containsKey(username)) {
            Set<String> friends = usermap.get(username).getFriends();
            StringBuilder online = new StringBuilder();
            StringBuilder offline = new StringBuilder();
            if (friends != null) {
                for (String friend : friends
                ) {
                    if (onlineClientmap.containsKey(friend)) {
                        // 在线好友
                        online.append(friend);
                        online.append(',');
                    } else {
                        // 不在线好友
                        offline.append(friend);
                        offline.append(',');
                    }
                }
            }

            StringBuilder response = new StringBuilder(Constant.PULL_PREFIX);
            response.append("online=");
            response.append(online);
            response.append('&');
            response.append("offline=");
            response.append(offline);
            response.append('\n');
            System.out.println(response);
            writer.write(response.toString());
        } else {
            writer.write(Constant.RESPONSE_ERROR + "获取好友列表失败!" + "\n");
        }
        writer.flush();
    }

    // 发送指定消息到socket
    private void writeMsg(String Message, Socket socket) throws IOException {
        BufferedWriter toOther = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                StandardCharsets.UTF_8));
        System.out.println("给指定的socket发送消息");
        toOther.write(Message + "\n");
        toOther.flush();
    }

    // 群发消息给所有在线客户但(服务端的公告)
    private void publishMessage(String Message) {
        try {
            for (Socket socket :
                    onlineClientmap.values()) {
                if (socket.isConnected()) {
                    writeMsg(Message, socket);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
