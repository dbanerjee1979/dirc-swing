package dirc.core.config;

import java.nio.charset.Charset;

public class IrcServerInfo {
    private String hostname;
    private int port;
    private Charset charset;

    public IrcServerInfo(String hostname, int port, Charset charset) {
        this.hostname = hostname;
        this.port = port;
        this.charset = charset;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public int getPort() {
        return port;
    }
    
    public Charset getCharset() {
        return charset;
    }
}
