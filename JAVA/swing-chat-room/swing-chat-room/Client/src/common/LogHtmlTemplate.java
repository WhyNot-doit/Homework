package common;

import java.time.LocalDateTime;

public class LogHtmlTemplate {
    public static final String htmlHeader = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <style>\n" +
            ".msg_left {\n" +
            "\tcolor: rgb(51, 0, 255);\n" +
            "\t}\n" +
            "    .msg_right{\n" +
            "        text-align: right;\n" +
            "        color: rgb(249, 36, 36);\n" +
            "    }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>";
    public static final int SEND = 1;
    public static final int REC = 0;
    public static final String htmlEnder = "</body>\n" +
            "</html>";

    public static final String templateLeft = "<div class=\"msg_left\">\n" +
            "<a>%s</a>\n" +
            "<a>%s :</a>\n" +
            "<div>%s</div>\n" +
            "</div> ";
    public static final String templateRight = "<div class=\"msg_right\">\n" +
            "<a>%s</a>&nbsp;\n" +
            "<a>%s :</a>\n" +
            "<div>%s</div>\n" +
            "</div> ";

    public static void editContentRecieve(StringBuilder content, String from, String message) {
        content.append(String.format(templateLeft, from, LocalDateTime.now(), message));
    }

    public static void editContentSend(StringBuilder content, String from, String message) {
        System.out.println(from);
        System.out.println(message);
        content.append(String.format(templateRight, from, LocalDateTime.now(), message));
    }

    // 将消息编辑进html文档中并返回html文件,type:0为接收的消息左居中,type:1为发送的消息右居中
    public static String appendContentTohtml(StringBuilder content, int type, String fromOrsend, String message) {
        if (type == REC) {
            LogHtmlTemplate.editContentRecieve(content, fromOrsend, message);
        } else if (type == SEND) {
            LogHtmlTemplate.editContentSend(content, fromOrsend, message);
        } else {
            return "";
        }
        return LogHtmlTemplate.htmlHeader + content + LogHtmlTemplate.htmlEnder;
    }
}
