package dirc.core.message;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IrcMessage {
    private String servername;
    private String nickname;
    private String user;
    private String host;
    private String command;
    private List<String> parameters;
    private List<TextStyle> styles;

    public IrcMessage(
            String servername,
            String nickname,
            String user,
            String host,
            String command,
            List<String> parameters,
            List<TextStyle> styles) {
        this.servername = servername;
        this.nickname = nickname;
        this.user = user;
        this.host = host;
        this.command = command;
        this.parameters = parameters;
        this.styles = styles;
    }

    public IrcMessage(
            String command, 
            String... parameters) {
        this(null, null, null, null, command, Arrays.asList(parameters), Collections.<TextStyle> emptyList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("message: ");
        sb.append("[ servername: ").append(servername);
        sb.append(", nickname: ").append(nickname);
        sb.append(", user: ").append(user);
        sb.append(", host: ").append(host);
        sb.append(", command: ").append(command);
        sb.append(", parameters: [");
        for(Iterator<String> p = parameters.iterator(); p.hasNext();) {
            sb.append("'").append(p.next()).append("'");
            if(p.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        sb.append(", styles: ").append(styles);
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

    public String getParameter(int n) {
        return n < parameters.size() ? parameters.get(n) : "";
    }
    
    public String getLastParameter() {
        return parameters.isEmpty() ? "" : parameters.get(parameters.size() - 1);
    }
    
    public String getJoinedParameters(int n) {
        StringBuilder s = new StringBuilder();
        for (Iterator<String> p = parameters.subList(n, parameters.size()).iterator(); p.hasNext();) {
            s.append(p.next());
            if(p.hasNext()) {
                s.append(" ");
            }
        }
        return s.toString();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(command);
        for (Iterator<String> i = parameters.iterator(); i.hasNext();) {
            String p = i.next();
            sb.append(" ");
            if(!i.hasNext()) {
                if(p == null || p.length() == 0 || p.contains(" ")) {
                    sb.append(":");
                }
                if(p == null) {
                    p = "";
                }
            }
            else {
                if(p == null || p.length() == 0) {
                    throw new IllegalArgumentException("Middle parameters must have a value");
                }
                else if(p.contains(" ")) {
                    throw new IllegalArgumentException("Middle parameters cannot have spaces");
                }
                else if(p.charAt(0) == ':') {
                    throw new IllegalArgumentException("Middle parameters cannot start with :");
                }
            }
            sb.append(p);
        }
        sb.append("\r\n");
        return sb.toString();
    }

    public List<TextStyle> getTextStyles() {
        return styles;
    }
}
