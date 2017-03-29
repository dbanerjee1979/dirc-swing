package dirc.core.event;

import java.util.List;

import dirc.core.message.TextStyle;

public class MotDStart extends IrcEvent {
    private String message;
    private List<TextStyle> textStyles;

    public MotDStart(String message, List<TextStyle> textStyles) {
        this.message = message;
        this.textStyles = textStyles;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public List<TextStyle> getTextStyles() {
        return textStyles;
    }
}
