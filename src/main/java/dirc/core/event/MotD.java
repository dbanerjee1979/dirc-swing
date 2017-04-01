package dirc.core.event;

import java.util.List;

import dirc.core.message.TextStyle;

public class MotD extends IrcEvent {
    public MotD(String recipient, String message, List<TextStyle> textStyles) {
        super("-", message, textStyles);
    }
}
