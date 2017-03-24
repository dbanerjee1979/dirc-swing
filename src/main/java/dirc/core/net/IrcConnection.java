package dirc.core.net;

import java.io.IOException;

import dirc.core.message.IrcMessage;
import dirc.core.message.IrcMessageListener;

public interface IrcConnection {
    void addMessageListener(IrcMessageListener listener);
    void connect() throws IOException;
    void close();
    void sendMessage(IrcMessage message);
}
