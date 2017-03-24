package dirc.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        final Socket s = new Socket("irc.freenode.net", 6667);
        try {
            Thread t = new Thread() {
                public void run() {
                    try {
                        BufferedReader r = new BufferedReader(new InputStreamReader(
                                s.getInputStream(), Charset.forName("UTF-8")));
                        String line = null;
                        while((line = r.readLine()) != null) {
                            System.out.println(line);
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
