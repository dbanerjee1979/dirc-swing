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
}
