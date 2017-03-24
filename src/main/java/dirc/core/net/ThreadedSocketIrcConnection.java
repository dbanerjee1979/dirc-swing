package dirc.core.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import dirc.core.message.IrcMessage;
import dirc.core.message.IrcMessageListener;
import dirc.core.message.IrcMessageReader;

public class ThreadedSocketIrcConnection implements IrcConnection {
    private String hostname;
    private int port;
    private Charset charset;
    private List<IrcMessageListener> listeners;
    private LinkedBlockingQueue<IrcMessage> outbox;
    private Socket s;
    private Thread sendThread;

    public ThreadedSocketIrcConnection(String hostname, int port, Charset charset) {
        this.hostname = hostname;
        this.port = port;
        this.charset = charset;
        this.listeners = new ArrayList<IrcMessageListener>();
    }

    public void addMessageListener(IrcMessageListener listener) {
        this.listeners.add(listener);
    }
    
    private void fireMessageRecieved(IrcMessage m) {
        for (IrcMessageListener l : listeners) {
            l.receivedMessage(m);
        }
    }

    public void connect() throws IOException {
        s = new Socket(hostname, port);
        final InputStream is = s.getInputStream();
        final OutputStream os = s.getOutputStream();

        Thread recvThread = new Thread(hostname + "-recv") {
            public void run() {
                IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
                IrcMessage m = null;
                while((m = r.nextMessage()) != null) {
                    fireMessageRecieved(m);
                }
            }
        };
        recvThread.setDaemon(true);
        recvThread.start();

        outbox = new LinkedBlockingQueue<IrcMessage>();
        sendThread = new Thread(hostname + "-send") {
            @Override
            public void run() {
                PrintWriter w = new PrintWriter(new OutputStreamWriter(os, charset));
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        IrcMessage msg = outbox.take();
                        w.print(msg.serialize());
                        w.flush();
                    } 
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        sendThread.setDaemon(true);
        sendThread.start();
    }
    
    public void close() {
        if(s != null) {
            try {
                s.close();
            } 
            catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        if(sendThread != null) {
            sendThread.interrupt();
        }
    }

    public void sendMessage(IrcMessage message) {
        outbox.offer(message);
    }
}
