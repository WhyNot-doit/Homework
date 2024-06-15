package common;

public interface Constant {

//    String SERVERHOST = "127.0.0.1";
//    int PORT = 8888;
//    String SERVERHOST = "lts.nat123.fun";
//    String SERVERHOST = "120.26.68.165";
//    int PORT = 10897;
//    int PORT = 9418;

    //    // 聊天室服务端地址
    String SERVERHOST = "127.0.0.1";
    //    // 聊天室服务端端口
    int PORT = 8888;
    // 客户端接收别人文件时存放的文件夹,默认在相对目录的download文件夹下
    String DOWNLOAD_PATH = "download";
    // 存放聊天记录的文件夹,默认在相对目录的chatlog文件夹下
    String CHATLOG_PATH = "chatlog";

    // 客户端发给服务端  (注册请求前缀)
    String REGISTER_PREFIX = "Register@";

    // 客户端发给服务端  (登陆请求前缀)
    String LOGIN_PREFIX = "Login@";

    // 客户端发给服务端  (拉取好友列表请求前缀)
    String PULL_PREFIX = "Pull@";
    // 客户端发给服务端  (添加好友请求前缀)
    String ADD_PREFIX = "Add@";
    // 客户端发给服务端  (删除好友请求前缀)
    String DELETE_PREFIX = "Delete@";
    // 客户端发给服务端 Chat@to用户&消息 同时也是 服务端发给服务端 (私聊请求前缀) Chat@from用户&消息
    String CHAT_PREFIX = "Chat@";
    // 客户端发给服务端 GroupChat@消息 同时也是 服务端发给服务端 (群聊请求前缀) GroupChat@from用户&消息
    String GROUP_CHAT_PREFIX = "GroupChat@";
    // 客户端发给服务端  (发送文件请求前缀)
    String FILE_PREFIX = "File@";


    // 服务端发给客户端的响应 (格式为 0数据 代表OK)
    String RESPONSE_OK = "0@";
    // 服务端发给客户端的命令 (让客户端准备接收文件命令)
    String READY_RECEIVE = "Receive@";
    // 服务端发给客户端的命令 (让客户端准备发送文件命令)
    String READY_SEND = "Send@";

    // 服务端发给客户端的通知
    String NOTIFY_PREFIX = "Notify@";


}
