package common;

import java.io.File;


public class CommonUtil {
    // 从消息中分离请求命令,返回请求参数
    public static String pareseBody(String text) {
        int c = text.indexOf("@");
        if (c != -1) {
            // 切出请求体
            String body = text.substring(c + 1);
            System.out.println(body);
            return body;
        }
        return "";
    }


    /**
     * 播放消息提示音
     */
    public static void playMsgSound(String wavpath) {
        try {
            File file = new File(wavpath);
            System.out.println(file.getAbsolutePath());
            MusicPlay.play(wavpath);
        } catch (Exception e) {
            System.out.println("播放提示音失败");
            e.printStackTrace();
        }
    }
}
