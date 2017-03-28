package dirc.core.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dirc.core.message.IrcMessage;
import dirc.core.message.IrcMessageListener;
import dirc.core.net.IrcConnection;
import dirc.ui.event.IrcEvent;
import dirc.ui.event.IrcEventListener;
import dirc.ui.event.QuitEvent;
import dirc.ui.event.ServerEvent;

public class IrcServer {
    private IrcConnection connection;
    private List<IrcEventListener> listeners;

    public IrcServer(IrcConnection connection) {
        this.connection = connection;
        this.listeners = new ArrayList<IrcEventListener>();
        connection.addMessageListener(new IrcMessageListener() {
            public void receivedMessage(IrcMessage message) {
                fireEvent(translateEvent(message));
            }
        });
    }
    
    public void addEventListener(IrcEventListener eventListener) {
        this.listeners.add(eventListener);
    }
    
    private void fireEvent(IrcEvent ev) {
        for (IrcEventListener l : listeners) {
            l.handleEvent(ev);
        }
    }
    
    private IrcEvent translateEvent(IrcMessage message) {
        if("QUIT".equalsIgnoreCase(message.getCommand())) {
            return new QuitEvent(message.getNickname(), message.getLastParameter());
        }
        return new ServerEvent(message);
    }

    public void connect() throws IOException {
        this.connection.connect();
    }

    public void nickname(String nick) {
        this.connection.sendMessage(new IrcMessage("NICK", nick));
    }

    public void username(String username, String realname) {
        this.connection.sendMessage(new IrcMessage("USER", username, "0", "*", realname));
    }

    public void quit() {
        this.connection.sendMessage(new IrcMessage("QUIT"));
    }
}
