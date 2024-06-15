package common;


public class CommonUtil {

    // 从消息中分离请求命令,返回请求参数
    public static String joinRequestBody(String command, String... parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(command);
        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i]);
            sb.append('&');
        }
        if (parameters.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

}
