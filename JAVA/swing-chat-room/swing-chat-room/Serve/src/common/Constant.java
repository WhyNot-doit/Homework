package common;

public interface Constant {

    // �����ҷ���˶˿�
     int PORT = 8888;
    // ������ת�������ļ�����Ķ˿�,�����ǰ�˿ڱ�ռ�û��Զ�������������
     int TEMP_PORT = 13664;


    // �ͻ��˷��������  (ע������ǰ׺)
     String REGISTER_PREFIX = "Register@";

    // �ͻ��˷��������  (��½����ǰ׺)
     String LOGIN_PREFIX = "Login@";

    // �ͻ��˷��������  (��ȡ�����б�����ǰ׺)
    // �������Ӧ���ݸ�ʽ 0:����-192.168.0.103-8888
     String PULL_PREFIX = "Pull@";
    // �ͻ��˷��������  (��Ӻ�������ǰ׺)
     String ADD_PREFIX = "Add@";
    // �ͻ��˷��������  (ɾ����������ǰ׺)
     String DELETE_PREFIX = "Delete@";
    // �ͻ��˷�������� Chat@to�û�&��Ϣ ͬʱҲ�� ����˷�������� (˽������ǰ׺) Chat@from�û�&��Ϣ
     String CHAT_PREFIX = "Chat@";
    // �ͻ��˷�������� GroupChat@��Ϣ ͬʱҲ�� ����˷�������� (Ⱥ������ǰ׺) GroupChat@from�û�&��Ϣ
     String GROUP_CHAT_PREFIX = "GroupChat@";
    // �ͻ��˷��������  (�����ļ�����ǰ׺)
     String FILE_PREFIX = "File@";


    // ����˷����ͻ��˵���Ӧ (��ʽΪ 0���� ����OK)
     String RESPONSE_OK = "0@";
    // ����˷����ͻ��˵���Ӧ (ע������,��ʽΪ 1������Ϣ)
     String RESPONSE_ERROR = "1@";
    // ����˷����ͻ��˵����� (�ÿͻ���׼�������ļ�����)
     String READY_RECEIVE = "Receive@";
    // ����˷����ͻ��˵����� (�ÿͻ���׼�������ļ�����)
     String READY_SEND = "Send@";

    // ����˷����ͻ��˵�֪ͨ
     String NOTIFY_PREFIX = "Notify@";


}
