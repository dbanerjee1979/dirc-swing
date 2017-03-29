package dirc.core.event;

public interface IrcEventListener {
    void handleEvent(IrcEvent ev);
}
