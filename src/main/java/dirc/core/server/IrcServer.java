package dirc.core.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dirc.core.event.IrcEvent;
import dirc.core.event.IrcEventListener;
import dirc.core.event.MotD;
import dirc.core.event.MotDEnd;
import dirc.core.event.MotDStart;
import dirc.core.event.NoticeEvent;
import dirc.core.event.PingEvent;
import dirc.core.event.QuitEvent;
import dirc.core.event.ServerEvent;
import dirc.core.message.IrcMessage;
import dirc.core.message.IrcMessageListener;
import dirc.core.net.IrcConnection;

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
    private Map<String, IrcChannel> channels;

    public IrcServer(IrcConnection connection) {
        this.connection = connection;
        this.listeners = new ArrayList<IrcEventListener>();
        this.channels = new HashMap<String, IrcChannel>();
        
        connection.addMessageListener(new IrcMessageListener() {
            public void receivedMessage(IrcMessage message) {
                IrcEvent ev = translateEvent(message);
                if(ev instanceof PingEvent) {
                    pong(((PingEvent) ev).getServername());
                } 
                else {
                    fireEvent(ev);
                }
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
            return new QuitEvent("*", message.getLastParameter());
        }
        if("NOTICE".equalsIgnoreCase(message.getCommand())) {
            return new NoticeEvent(message.getParameter(0), message.getLastParameter());
        }
        if("PING".equalsIgnoreCase(message.getCommand())) {
            return new PingEvent(message.getParameter(0));
        }
        else if("JOIN".equalsIgnoreCase(message.getCommand())) {
            return new ServerEvent(message.getParameter(0), message.getLastParameter());
        }
        else if(RPL_WELCOME.equals(message.getCommand()) ||
                RPL_YOURHOST.equals(message.getCommand()) ||
                RPL_CREATED.equals(message.getCommand()) ||
                RPL_LUSERCLIENT.equals(message.getCommand()) ||
                RPL_LUSERME.equals(message.getCommand()) ||
                RPL_LOCALUSERS.equals(message.getCommand()) ||
                RPL_GLOBALUSERS.equals(message.getCommand()) ||
                RPL_STATCONN.equals(message.getCommand())) {
            return new ServerEvent(message.getParameter(0), message.getLastParameter());
        }
        else if(RPL_MYINFO.equals(message.getCommand()) ||
                RPL_ISUPPORT.equals(message.getCommand()) ||
                RPL_LUSEROP.equals(message.getCommand()) ||
                RPL_LUSERUNKNOWN.equals(message.getCommand()) ||
                RPL_LUSERCHANNELS.equals(message.getCommand())) {
            return new ServerEvent(message.getParameter(0), message.getJoinedParameters(1));
        }
        else if(RPL_MOTDSTART.equals(message.getCommand())) {
            return new MotDStart(message.getParameter(0), message.getLastParameter(), message.getTextStyles());
        }
        else if(RPL_MOTD.equals(message.getCommand())) {
            return new MotD(message.getParameter(0), message.getLastParameter(), message.getTextStyles());
        }
        else if(RPL_ENDOFMOTD.equals(message.getCommand())) {
            return new MotDEnd(message.getParameter(0), message.getLastParameter(), message.getTextStyles());
        }
        return new ServerEvent("*", message.getJoinedParameters(0));
    }

    public void connect() throws IOException {
        this.connection.connect();
    }
    
    public void close() {
        this.connection.close();
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
    
    public void pong(String servername) {
        this.connection.sendMessage(new IrcMessage("PONG", servername));
    }

    public IrcChannel join(String channelname) {
        IrcChannel channel = new IrcChannel(channelname, this.connection);
        channels.put(channelname, channel);
        return channel;
    }
}
