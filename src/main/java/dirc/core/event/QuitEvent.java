package dirc.core.event;

public class QuitEvent extends IrcEvent {
    private String nickname;

    public QuitEvent(String nickname, String message) {
        super("*", message);
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
