package dirc.core.event;

import java.util.Collections;
import java.util.List;

import dirc.core.message.TextStyle;

public abstract class IrcEvent {
    public abstract String getMessage();

    public List<TextStyle> getTextStyles() {
        return Collections.emptyList();
    }
}
