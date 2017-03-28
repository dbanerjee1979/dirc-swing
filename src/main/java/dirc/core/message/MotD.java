package dirc.core.message;

import dirc.ui.event.IrcEvent;

public class MotD extends IrcEvent {
    private String message;

    public MotD(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
