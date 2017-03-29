package dirc.core.event;

public class QuitEvent extends IrcEvent {
    private String nickname;
    private String message;

    public QuitEvent(String nickname, String message) {
        this.nickname = nickname;
        this.message = message;
    }

    public String getNickname() {
        return nickname;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
