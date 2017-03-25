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

/**
 * Adapter around an {@link InputStream} of server responses which converts the byte sequences into a
 * structured IRC message. The structure of the IRC message is specified in IRC 2812. The IRC server will
 * encode characters to byte sequences using a character set which must be specified when creating an instance of
 * this class. Most IRC servers use UTF-8.
 * 
 * @see https://tools.ietf.org/html/rfc2812#section-2.3
 */
public class IrcMessageReader {
    // Regular expressions defined below correspond to the IRC message format defined in IRC 2812
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

    /**
     * Create a message reader
     * 
     * @param is      - input stream of IRC server responses
     * @param charset - character encoding of the server responses
     */
    public IrcMessageReader(InputStream is, Charset charset) {
        s = new Scanner(is, charset.name());
    }

    /**
     * Read the next message from the IRC server
     * 
     * @return response transfromed to {@link IrcMessage} format, or null if no more data from stream
     */
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

    /**
     * Skip past the rest of input to the end of line on malformed server response
     */
    private void abortRestOfLine() {
        if(s.hasNextLine()) {
            s.nextLine();
        }
    }
    
    /**
     * Parse the input stream to produce a {@link IrcMessage}
     * 
     * Holds parsing state for a single parse. Create a new instance for each parse.
     */
    public static class MessageParser {
        private Scanner s;
        private String servername;
        private String nickname;
        private String user;
        private String host;
        private List<String> parameters;
        private List<TextStyle> styles;

        /**
         * Create a message parser with fresh parse state
         * 
         * @param s - {@link Scanner} wrapping the input stream
         */
        public MessageParser(Scanner s) {
            this.s = s;
            this.parameters = new ArrayList<String>();
            this.styles = new ArrayList<TextStyle>();
        }

        /**
         * Parse the next message from the input stream
         * 
         * @return the parsed message
         * 
         * @throws NoSuchElementException on parse error 
         */
        public IrcMessage getMessage() {
            // Reset delimiter to include spaces. Prior to trailing command, all message elements are spaces separted
            s.useDelimiter("[ \\n\\r]+");
            parsePrefix();
            String command = s.next(COMMAND_PATTERN);
            parseParameters();
            return new IrcMessage(servername, nickname, user, host, command, parameters, styles);
        }
        
        /**
         * Parse the prefix (if present). The IRC RFC (seems to) ambiguously define nickname and servername,
         * if the dotted portion of the servername is optional.
         * 
         * Top prevent ambiguity, the servername will require dots, which cannot be present in a nickname.
         */
        private void parsePrefix() {
            if(s.hasNext(SERVERNAME_PREFIX_PATTERN)) {
                servername = s.next(SERVERNAME_PREFIX_PATTERN).substring(1);
            }
            else if(s.hasNext(NICKNAME_PREFIX_PATTERN)) {
                parseNickname();
            }
        }

        /**
         * Parse nickname into three component parts (nickname, user and host). The nick is only strictly required
         * for identification (since only one nick can be registered on a server).
         */
        private void parseNickname() {
            Matcher m = NICKNAME_PREFIX_PATTERN.matcher(s.next(NICKNAME_PREFIX_PATTERN));
            if(m.matches()) {
                nickname = m.group(1);
                user = m.group(2);
                host = m.group(3);
            }
        }

        /**
         * Parse parameters, first middle parameters then the trailing parameter.
         */
        private void parseParameters() {
            parseMiddleParameters();
            parseTrailingParameter();
        }

        /**
         * Parse middle parameters. Cannot have spaces or leading colon.
         */
        private void parseMiddleParameters() {
            while(s.hasNext(MIDDLE_PARAMETER_PATTERN)) {
                parameters.add(s.next(MIDDLE_PARAMETER_PATTERN));
            }
        }
        
        /**
         * Parse trailing parameter. Last parameter which can have spaces.
         */
        private void parseTrailingParameter() {
            // Set delimiter to newline, to allow the trailing parameter to include spaces
            // Skip leading spaces to arrive at the trailing parameter token manually
            s.skip(" *");
            s.useDelimiter("[\\n\\r]+");
            if(s.hasNext(TRAILING_PARAMETER_PATTERN)) {
                parameters.add(parseTrailingFormatting(s.next(TRAILING_PARAMETER_PATTERN).substring(1)));
            }
        }

        /**
         * The trailing parameter is the only one for which IRC formatting codes will be considered. This method
         * strips out the control codes from the trailing parameter and creates {@link TextStyle} objects which
         * describe the requested formatting.
         * 
         * The IRC formatting codes are an unofficial subprotocol independent of the RFC, and are pretty much
         * defined the mIRC client's interpretation of certain control codes.
         * 
         * @param trailing - the trailing parameter with formatting control codes embedded
         * 
         * @return the trailing parameter with formatting control codes stripped out
         * 
         * @see https://en.wikichip.org/wiki/irc/colors
         */
        private String parseTrailingFormatting(String trailing) {
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
                    style.toggle(Style.Bold);
                }
                else if(m.group(2) != null) {
                    style.toggle(Style.Italic);
                }
                else if(m.group(3) != null) {
                    style.toggle(Style.Underlined);
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

        /**
         * Utility method to convert a text representation of a number (guaranteed to be between 1 and 2 digits),
         * to the corresponding value from the {@link Color} enumeration. A missing value or a value that doesn't map to
         * the color codes will be mapped to null, which is considered the default color.
         * 
         * @param text - up to two digit value to map
         * 
         * @return corresponding {@link Color} value
         */
        private Color toColor(String text) {
            if(text == null) {
                return null;
            }

            int i = Integer.parseInt(text);
            return i < Color.values().length ? Color.values()[i] : null;
        }
    }
}
