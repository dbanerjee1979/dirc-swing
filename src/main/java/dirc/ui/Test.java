package dirc.ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

import dirc.core.message.IrcMessage;
import dirc.core.message.IrcMessageListener;
import dirc.core.net.IrcConnection;
import dirc.core.net.ThreadedSocketIrcConnection;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        final IrcConnection c = new ThreadedSocketIrcConnection("irc.freenode.net", 6667, Charset.forName("UTF-8"));
        final CountDownLatch shutdown = new CountDownLatch(1);
        c.addMessageListener(new IrcMessageListener() {
            public void receivedMessage(IrcMessage message) {
                System.out.println(message);
                if("sh0rug0ru".equals(message.getNickname()) && "QUIT".equals(message.getCommand())) {
                    c.close();
                    shutdown.countDown();
                }
            }
        });
        c.connect();
        c.sendMessage(new IrcMessage("NICK", "sh0rug0ru"));
        c.sendMessage(new IrcMessage("USER", "guest", "0", "*", "Duke"));
        c.sendMessage(new IrcMessage("QUIT"));
        shutdown.await();
        c.close();
    }
}
