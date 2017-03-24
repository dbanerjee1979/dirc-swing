package dirc.core.message;

import java.util.List;

public class IrcMessage {
    private String servername;
    private String nickname;
    private String user;
    private String host;
    private String command;
    private List<String> parameters;

    public IrcMessage(
            String servername, 
            String nickname, 
            String user, 
            String host, 
            String command, 
            List<String> parameters) {
        this.servername = servername;
        this.nickname = nickname;
        this.user = user;
        this.host = host;
        this.command = command;
        this.parameters = parameters;
    }

    public String getServername() {
        return servername;
    }
    
    public String getNickname() {
        return nickname;
    }

    public String getUser() {
        return user;
    }
    
    public String getHost() {
        return host;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
