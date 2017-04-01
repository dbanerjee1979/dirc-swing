package dirc.core.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class IrcNetwork {
    public String nickname;
    public String username;
    public String realName;
    private List<IrcServerInfo> servers;

    private IrcNetwork() {
        this.servers = new ArrayList<IrcServerInfo>();
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getRealName() {
        return realName;
    }

    public IrcServerInfo getServer() {
        return servers.get(0);
    }
    
    public static class Builder {
        private IrcNetwork n;

        public Builder() {
            n = new IrcNetwork();
        }
        
        public static Builder with() {
            return new Builder();
        }

        public IrcNetwork build() {
            return n;
        }

        public Builder nickname(String nickname) {
            n.nickname = nickname;
            return this;
        }

        public Builder username(String username) {
            n.username = username;
            return this;
        }

        public Builder realName(String realName) {
            n.realName = realName;
            return this;
        }

        public Builder addServer(String hostname, int port, Charset charset) {
            n.servers.add(new IrcServerInfo(hostname, port, charset));
            return this;
        }
    }
}
