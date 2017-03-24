package dirc.core.message;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcMessageReader {
    private static final String SERVERNAME_PREFIX_PATTERN = 
            ":[A-z0-9][A-z0-9-]*[A-z0-9]*(?:\\.[A-z0-9][A-z0-9-]*[A-z0-9]*)+";
    private static final Pattern NICKNAME_PREFIX_PATTERN = Pattern.compile(
            ":([A-z\\x5B-\\x60\\x7B-\\x7D](?:[A-z0-9\\x5B-\\x60\\x7B-\\x7D-]){0,8})(?:(?:!([^!@]+))?@([^!@]+))?");
    private static final String COMMAND_PATTERN = 
            "[A-z]+|[0-9]{3}";
    private static final String MIDDLE_PARAMETER_PATTERN = 
            "[^ :\\n\\r][^ \\n\\r]*";
    private static final String TRAILING_PARAMETER_PATTERN = 
            ":[^\\n\\r]*";
    
    private Scanner s;

    public IrcMessageReader(InputStream is, Charset charset) {
        s = new Scanner(is, charset.name());
    }

    public IrcMessage nextMessage() {
        while(s.hasNextLine()) {
            try {
                return new MessageParser(s).getMessage();
            }
            catch(NoSuchElementException ex) {
                abortRestOfLine();
            }
        }
        return null;
    }

    private void abortRestOfLine() {
        if(s.hasNextLine()) {
            s.nextLine();
        }
    }
    
    public static class MessageParser {
        private Scanner s;
        private String servername;
        private String nickname;
        private String user;
        private String host;
        private ArrayList<String> parameters;
        
        public MessageParser(Scanner s) {
            this.s = s;
        }

        public IrcMessage getMessage() {
            s.useDelimiter("[ \\n\\r]+");
            parsePrefix();
            String command = s.next(COMMAND_PATTERN);
            parseParameters();
            return new IrcMessage(servername, nickname, user, host, command, parameters);
        }
        
        private void parsePrefix() {
            if(s.hasNext(SERVERNAME_PREFIX_PATTERN)) {
                servername = s.next(SERVERNAME_PREFIX_PATTERN).substring(1);
            }
            else if(s.hasNext(NICKNAME_PREFIX_PATTERN)) {
                parseNickname();
            }
        }

        private void parseNickname() {
            Matcher m = NICKNAME_PREFIX_PATTERN.matcher(s.next(NICKNAME_PREFIX_PATTERN));
            if(m.matches()) {
                nickname = m.group(1);
                user = m.group(2);
                host = m.group(3);
            }
        }

        private void parseParameters() {
            parameters = new ArrayList<String>();
            parseMiddleParameters();
            s.skip(" *");
            s.useDelimiter("[\\n\\r]+");
            parseTrailingParameter();
        }

        private void parseMiddleParameters() {
            while(s.hasNext(MIDDLE_PARAMETER_PATTERN)) {
                parameters.add(s.next(MIDDLE_PARAMETER_PATTERN));
            }
        }
        
        private void parseTrailingParameter() {
            if(s.hasNext(TRAILING_PARAMETER_PATTERN)) {
                parameters.add(s.next(TRAILING_PARAMETER_PATTERN).substring(1));
            }
        }
    }
}
