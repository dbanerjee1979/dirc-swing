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
import dirc.ui.event.NoticeEvent;
import dirc.ui.event.QuitEvent;
import dirc.ui.event.ServerEvent;

public class IrcServer {
    private static final String RPL_WELCOME = "001";
    private static final String RPL_YOURHOST = "002";
    private static final String RPL_CREATED = "003";
    private static final String RPL_MYINFO = "004";
    private static final String RPL_ISUPPORT = "005";
    private static final String RPL_STATCONN = "250";
    private static final String RPL_LUSERCLIENT = "251";
    private static final String RPL_LUSEROP = "252";
    private static final String RPL_LUSERUNKNOWN = "253";
    private static final String RPL_LUSERCHANNELS = "254";
    private static final String RPL_LUSERME = "255";
    private static final String RPL_LOCALUSERS = "265";
    private static final String RPL_GLOBALUSERS = "266";
    private static final String RPL_MOTDSTART = "375";
    private static final String RPL_MOTD = "372";
    private static final String RPL_ENDOFMOTD = "376";
    
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
        if("QUIT".equalsIgnoreCase(message.getCommand())) {
            return new QuitEvent(message.getNickname(), message.getLastParameter());
        }
        if("NOTICE".equalsIgnoreCase(message.getCommand())) {
            return new NoticeEvent(message.getParameter(0), message.getLastParameter());
        }
        else if(RPL_WELCOME.equals(message.getCommand()) ||
                RPL_YOURHOST.equals(message.getCommand()) ||
                RPL_CREATED.equals(message.getCommand()) ||
                RPL_LUSERCLIENT.equals(message.getCommand()) ||
                RPL_LUSERME.equals(message.getCommand()) ||
                RPL_LOCALUSERS.equals(message.getCommand()) ||
                RPL_GLOBALUSERS.equals(message.getCommand()) ||
                RPL_STATCONN.equals(message.getCommand())) {
            return new ServerEvent(message.getLastParameter());
        }
        else if(RPL_MYINFO.equals(message.getCommand()) ||
                RPL_ISUPPORT.equals(message.getCommand()) ||
                RPL_LUSEROP.equals(message.getCommand()) ||
                RPL_LUSERUNKNOWN.equals(message.getCommand()) ||
                RPL_LUSERCHANNELS.equals(message.getCommand())) {
            return new ServerEvent(message.getJoinedParameters(1));
        }
        else if(RPL_MOTDSTART.equals(message.getCommand())) {
            return new MotDStart(message.getLastParameter());
        }
        else if(RPL_MOTD.equals(message.getCommand())) {
            return new MotD(message.getLastParameter());
        }
        else if(RPL_ENDOFMOTD.equals(message.getCommand())) {
            return new MotDEnd(message.getLastParameter());
        }
        return new ServerEvent(message.getJoinedParameters(0));
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
