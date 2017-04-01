package dirc.core.event;

import java.util.Collections;
import java.util.List;

import dirc.core.message.TextStyle;

public abstract class IrcEvent {
    private String recipient;
    private String message;
    private List<TextStyle> textStyles;

    public IrcEvent(String recipient, String message) {
        this(recipient, message, Collections.<TextStyle> emptyList());
    }

    public IrcEvent(String recipient, String message, List<TextStyle> textStyles) {
        this.recipient = recipient;
        this.message = message;
        this.textStyles = textStyles;
    }

    public String getRecipient() {
        return recipient;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<TextStyle> getTextStyles() {
        return textStyles;
    }
}
