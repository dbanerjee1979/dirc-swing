package dirc.ui.event;

public class NoticeEvent extends IrcEvent {
    private String message;

    public NoticeEvent(String recipient, String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
