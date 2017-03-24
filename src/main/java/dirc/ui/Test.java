package dirc.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

import dirc.core.message.IrcMessage;
import dirc.core.message.IrcMessageReader;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        final Socket s = new Socket("irc.freenode.net", 6667);
        try {
            Thread t = new Thread() {
                public void run() {
                    try {
                        IrcMessageReader r = new IrcMessageReader(s.getInputStream(), Charset.forName("UTF-8"));
                        IrcMessage m = null;
                        while((m = r.nextMessage()) != null) {
                            System.out.println(m);
                        }
                    }
                    catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            };
            t.start();
            
            PrintWriter w = new PrintWriter(s.getOutputStream());
            w.print("NICK sh0rug0ru\r\n");
            w.print("USER guest 0 * :Duke\r\n");
            w.print("QUIT\r\n");
            w.flush();
            
            t.join();
        }
        finally {
            s.close();
        }
    }
}
