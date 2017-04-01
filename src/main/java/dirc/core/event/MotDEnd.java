package dirc.core.event;

import java.util.List;

import dirc.core.message.TextStyle;

public class MotDEnd extends IrcEvent {
    public MotDEnd(String recipient, String message, List<TextStyle> textStyles) {
        super(recipient, message, textStyles);
    }
}
