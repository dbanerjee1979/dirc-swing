package dirc.ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dirc.core.config.IrcNetwork;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        final IrcConnectionTreeModel ircModel = new IrcConnectionTreeModel();
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } 
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        
        final CountDownLatch c = new CountDownLatch(1);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IrcClient f = new IrcClient(ircModel);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                c.countDown();
            }
        });
        c.await();

        IrcNetwork network = IrcNetwork.Builder.with()
                .nickname("sh0rug0ru")
                .username("guest")
                .realName("Duke")
                .addServer("irc.freenode.net", 6667, Charset.forName("UTF-8"))
                .build();
        
        IrcServerNode serverNode = ircModel.connect(network);
        // serverNode.join("#haskell");
        serverNode.quit();
    }
}
