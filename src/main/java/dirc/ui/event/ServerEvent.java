package dirc.ui.event;

public class ServerEvent extends IrcEvent {
    private String messageText;

    public ServerEvent(String messageText) {
        this.messageText = messageText;
    }

    @Override
    public String getMessage() {
        return messageText;
    }
}
