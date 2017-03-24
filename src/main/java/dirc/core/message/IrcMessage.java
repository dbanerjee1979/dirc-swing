package dirc.core.message;

import java.util.Arrays;
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

    public IrcMessage(
            String command, 
            String... parameters) {
        this(null, null, null, null, command, Arrays.asList(parameters));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("message: ");
        sb.append("[ servername: ").append(servername);
        sb.append(", nickname: ").append(nickname);
        sb.append(", user: ").append(user);
        sb.append(", host: ").append(host);
        sb.append(", command: ").append(command);
        sb.append(", parameters: ").append(parameters);
        sb.append("]");
        return sb.toString();
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

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        if(servername != null) {
        }
        else if(nickname != null) {
        }
        else {
            sb.append(command);
            for (String p : parameters) {
                sb.append(" ");
                sb.append(p);
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
