package dirc.core.server;

import dirc.core.message.IrcMessage;
import dirc.core.net.IrcConnection;

public class IrcChannel {
    private IrcConnection connection;

    public IrcChannel(String channelname, IrcConnection connection) {
        this.connection = connection;
        
        this.connection.sendMessage(new IrcMessage("JOIN", channelname));
    }
}
