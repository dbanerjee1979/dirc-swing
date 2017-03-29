package dirc.ui.event;

public class NoticeEvent extends IrcEvent {
    private String recipient;
    private String message;

    public NoticeEvent(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
