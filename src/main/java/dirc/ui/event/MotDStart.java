package dirc.ui.event;

public class MotDStart extends IrcEvent {
    private String message;

    public MotDStart(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
