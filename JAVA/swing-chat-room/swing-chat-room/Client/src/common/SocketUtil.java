package common;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketUtil {
    public Socket socket;
    public BufferedWriter bufferedWriter;
    public BufferedReader bufferedReader;


    public SocketUtil() {

    }

    // 获取socket
    public Socket getSocket() {
        if (this.socket == null || !this.socket.isConnected()) {
            try {
                this.socket = new Socket(Constant.SERVERHOST, Constant.PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.socket;

    }

    // 发送命令
    public void sendCommand(String command) {
        BufferedWriter writer;
        try {
            writer = getWriter();
            writer.write(command + '\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 发送命令和请求参数
    public void sendCommand(String command, String... parameters) {
        BufferedWriter writer;
        try {
            writer = getWriter();
            StringBuilder sb = new StringBuilder();
            sb.append(command);
            for (int i = 0; i < parameters.length; i++) {
                sb.append(parameters[i]);
                sb.append('&');
            }
            if (parameters.length > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append('\n');
            System.out.println(sb.toString());
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取writer
    public BufferedWriter getWriter() {
        if (this.bufferedWriter == null) {
            try {
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream(),
                        StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return this.bufferedWriter;
    }

    // 获取reader
    public BufferedReader getReader() {
        if (this.bufferedReader == null) {
            try {
                this.bufferedReader = new BufferedReader(new InputStreamReader(getSocket().getInputStream(),
                        StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.bufferedReader;
    }


}
