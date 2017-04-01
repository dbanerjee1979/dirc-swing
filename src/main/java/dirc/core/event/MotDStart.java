package dirc.core.event;

import java.util.List;

import dirc.core.message.TextStyle;

public class MotDStart extends IrcEvent {
    public MotDStart(String recipient, String message, List<TextStyle> textStyles) {
        super(recipient, message, textStyles);
    }
}
