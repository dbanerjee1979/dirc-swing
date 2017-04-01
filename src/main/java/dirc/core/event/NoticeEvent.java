package dirc.core.event;

import java.util.List;

import dirc.core.message.TextStyle;

public class NoticeEvent extends IrcEvent {
    public NoticeEvent(String recipient, String message) {
        super(recipient, message);
    }

    public NoticeEvent(String recipient, String message, List<TextStyle> textStyles) {
        super(recipient, message, textStyles);
    }
}
