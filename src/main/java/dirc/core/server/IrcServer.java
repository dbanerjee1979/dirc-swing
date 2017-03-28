package dirc.core.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dirc.core.message.IrcMessage;
import dirc.core.message.IrcMessageListener;
import dirc.core.message.MotD;
import dirc.core.net.IrcConnection;
import dirc.ui.event.IrcEvent;
import dirc.ui.event.IrcEventListener;
import dirc.ui.event.MotDEnd;
import dirc.ui.event.MotDStart;
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
        System.out.println(message.toString());
        List<String> ps = message.getParameters();
        if("QUIT".equalsIgnoreCase(message.getCommand())) {
            return new QuitEvent(message.getNickname(), message.getLastParameter());
        }
        else if("001".equals(message.getCommand()) ||
                "002".equals(message.getCommand()) ||
                "003".equals(message.getCommand()) ||
                "004".equals(message.getCommand()) ||
                "005".equals(message.getCommand()) ||
                "251".equals(message.getCommand()) ||
                "252".equals(message.getCommand()) ||
                "253".equals(message.getCommand()) ||
                "254".equals(message.getCommand()) ||
                "255".equals(message.getCommand())) {
            return new ServerEvent(ps.subList(ps.size() > 1 ? 1 : 0, ps.size()));
        }
        else if("265".equals(message.getCommand()) ||
                "266".equals(message.getCommand()) ||
                "250".equals(message.getCommand())) {
            return new ServerEvent(ps.subList(ps.size() - 1, ps.size()));
        }
        else if("375".equals(message.getCommand())) {
            return new MotDStart(message.getLastParameter());
        }
        else if("372".equals(message.getCommand())) {
            return new MotD(message.getLastParameter());
        }
        else if("376".equals(message.getCommand())) {
            return new MotDEnd(message.getLastParameter());
        }
        return new ServerEvent(ps);
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
