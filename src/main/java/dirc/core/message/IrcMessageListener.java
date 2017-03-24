package dirc.core.message;

public interface IrcMessageListener {
    void receivedMessage(IrcMessage message);
}
