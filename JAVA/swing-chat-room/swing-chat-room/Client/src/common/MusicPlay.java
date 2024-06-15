package common;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * 播放背景音乐(仅支持WAV格式)
 */
public class MusicPlay {
    private static AudioClip clip;


    public static AudioClip getClip(String name) {
        try {
            if (clip == null) {
                File file = new File("./" + name);
                URI uri = file.toURI();//获取资源
                URL url = uri.toURL();//获取整个地址
                return Applet.newAudioClip(url);//通过小程序播放该地址的音频
            } else {
                return clip;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("播放错误！！");
            return null;
        }
    }


    //使用AudioClip中的方法，控制音频的播放动作
    public void loop() {//x循环播放
        clip.loop();
    }

    public static void play(String name) {//x单次播放
        if (clip == null) {
            clip = getClip(name);
        }
        clip.play();
    }

    public void stop() {//x关闭音频
        clip.stop();
    }

}

