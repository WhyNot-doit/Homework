package common;

public interface Constant {

    // 聊天室服务端端口
     int PORT = 8888;
    // 服务器转发传输文件请求的端口,如果当前端口被占用会自动申请重新申请
     int TEMP_PORT = 13664;


    // 客户端发给服务端  (注册请求前缀)
     String REGISTER_PREFIX = "Register@";

    // 客户端发给服务端  (登陆请求前缀)
     String LOGIN_PREFIX = "Login@";

    // 客户端发给服务端  (拉取好友列表请求前缀)
    // 服务端响应数据格式 0:张三-192.168.0.103-8888
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
    // 服务端发给客户端的响应 (注册请求,格式为 1错误信息)
     String RESPONSE_ERROR = "1@";
    // 服务端发给客户端的命令 (让客户端准备接收文件命令)
     String READY_RECEIVE = "Receive@";
    // 服务端发给客户端的命令 (让客户端准备发送文件命令)
     String READY_SEND = "Send@";

    // 服务端发给客户端的通知
     String NOTIFY_PREFIX = "Notify@";


}
