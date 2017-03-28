package dirc.ui.event;

import java.util.Iterator;

import dirc.core.message.IrcMessage;

public class ServerEvent extends IrcEvent {
    private String messageText;

    public ServerEvent(IrcMessage message) {
        StringBuilder messageText = new StringBuilder();
        for (Iterator<String> p = message.getParameters().iterator(); p.hasNext();) {
            messageText.append(p.next());
            if(p.hasNext()) {
                messageText.append(" ");
            }
        }
        this.messageText = messageText.toString();
    }

    @Override
    public String getMessage() {
        return messageText;
    }
}
