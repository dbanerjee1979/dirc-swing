package dirc.core.message;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dirc.core.message.TextStyle.Style;
import dirc.core.message.TextStyle.Color;

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
    private static final Pattern FORMATTING_PATTERN = Pattern.compile(
            "(\u0002)|(\u001D)|(\u001F)|(\u000F)|(\u0003)([0-9]{2})?(?:,([0-9]{2}))?|(\u0016)");
    
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
        private List<String> parameters;
        private List<TextStyle> styles;
        
        public MessageParser(Scanner s) {
            this.s = s;
            this.parameters = new ArrayList<String>();
            this.styles = new ArrayList<TextStyle>();
        }

        public IrcMessage getMessage() {
            s.useDelimiter("[ \\n\\r]+");
            parsePrefix();
            String command = s.next(COMMAND_PATTERN);
            parseParameters();
            return new IrcMessage(servername, nickname, user, host, command, parameters, styles);
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
                parameters.add(parseTrailing(s.next(TRAILING_PARAMETER_PATTERN).substring(1)));
            }
        }

        private String parseTrailing(String trailing) {
            int start = 0;
            int end = trailing.length();
            TextStyle style = new TextStyle(start, end);

            StringBuffer sb = new StringBuffer();
            Matcher m = FORMATTING_PATTERN.matcher(trailing);
            while(m.find()) {
                m.appendReplacement(sb, "");

                end -= m.group().length();
                if(sb.length() > start) {
                    start = sb.length();
                    styles.add(style.trimRange(start));
                    style = new TextStyle(start, end, style);
                }

                if(m.group(1) != null) {
                    style.set(Style.Bold);
                }
                else if(m.group(2) != null) {
                    style.set(Style.Italic);
                }
                else if(m.group(3) != null) {
                    style.set(Style.Underlined);
                }
                else if(m.group(4) != null) {
                    style.clear();
                }
                else if(m.group(5) != null) {
                    style.setColors(toColor(m.group(6)), toColor(m.group(7)));
                }
                else if(m.group(8) != null) {
                    style.reverseColors();
                }
            }

            if(style.length() > 0) {
                styles.add(style.trimRange(end));
            }
            if(styles.get(0).isPlain()) {
                styles.remove(0);
            }

            m.appendTail(sb);
            return sb.toString();
        }

        private Color toColor(String text) {
            if(text == null) {
                return null;
            }

            int i = Integer.parseInt(text);
            return i < Color.values().length ? Color.values()[i] : null;
        }
    }
}
