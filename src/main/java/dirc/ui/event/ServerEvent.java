package dirc.ui.event;

import java.util.Iterator;
import java.util.List;

public class ServerEvent extends IrcEvent {
    private String messageText;

    public ServerEvent(List<String> parameters) {
        StringBuilder messageText = new StringBuilder();
        for (Iterator<String> p = parameters.iterator(); p.hasNext();) {
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
