package dirc.core.event;

import java.util.List;

import dirc.core.message.TextStyle;

public class MotD extends IrcEvent {
    private String message;
    private List<TextStyle> textStyles;

    public MotD(String message, List<TextStyle> textStyles) {
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
