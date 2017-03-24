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
    private Socket s;
    private Sender sender;

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

        Thread receiver = new Receiver(is);
        receiver.start();

        sender = new Sender(os);
        sender.start();
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

        if(sender != null) {
            sender.interrupt();
        }
    }

    public void sendMessage(IrcMessage message) {
        sender.offer(message);
    }

    private final class Sender extends Thread {
        private final OutputStream os;
        private LinkedBlockingQueue<IrcMessage> outbox;

        private Sender(OutputStream os) {
            super(hostname + "-send");
            this.os = os;
            this.outbox = new LinkedBlockingQueue<IrcMessage>();
            setDaemon(true);
        }

        public void offer(IrcMessage message) {
            this.outbox.offer(message);
        }

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
    }

    private final class Receiver extends Thread {
        private final InputStream is;

        private Receiver(InputStream is) {
            super(hostname + "-recv");
            this.is = is;
            setDaemon(true);
        }

        public void run() {
            IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
            IrcMessage m = null;
            while((m = r.nextMessage()) != null) {
                fireMessageRecieved(m);
            }
        }
    }
}
