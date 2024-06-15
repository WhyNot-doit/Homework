package entity;

import java.util.Set;

public class User {
    // 账号名
    private String account;
    // 密码
    private String password;

    // 好友列表
    private Set<String> friends;


    public User(String account, String password, Set<String> friends) {
        this.account = account;
        this.password = password;
        this.friends = friends;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public Set<String> getFriends() {
        return friends;
    }

    public void setFriends(Set<String> friends) {
        this.friends = friends;
    }
}
