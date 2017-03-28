package dirc.ui.event;

public class MotDEnd extends IrcEvent {
    private String message;

    public MotDEnd(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
