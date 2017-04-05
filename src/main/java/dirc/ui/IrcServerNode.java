package dirc.ui;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import dirc.core.event.IrcEvent;
import dirc.core.event.IrcEventListener;
import dirc.core.event.QuitEvent;
import dirc.core.server.IrcServer;

public class IrcServerNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 1L;
    
    private IrcServer server;
    private String hostname;
    private IrcTableModel serverConsoleModel;

    public IrcServerNode(final IrcServer server, final String hostname, final String nickname) {
        this.server = server;
        this.hostname = hostname;
        this.serverConsoleModel = new IrcTableModel(nickname);
        setUserObject(hostname);
        
        server.addEventListener(new IrcEventListener() {
            public void handleEvent(final IrcEvent ev) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        serverConsoleModel.handleEvent(ev);
                    }
                });
                if(ev instanceof QuitEvent && nickname.equals(((QuitEvent) ev).getNickname())) {
                    server.close();
                }
            }
        });
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public IrcTableModel getServerConsoleModel() {
        return serverConsoleModel;
    }

    public void quit() {
        this.server.quit();
    }

    public IrcChannelNode join(String channel) {
        this.server.join(channel);
        
        return new IrcChannelNode();
    }
}
