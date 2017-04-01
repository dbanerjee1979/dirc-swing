package dirc.core.event;

public class ServerEvent extends IrcEvent {
    public ServerEvent(String recipient, String messageText) {
        super(recipient, messageText);
    }
}
