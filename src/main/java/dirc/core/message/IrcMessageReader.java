package dirc.core.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
    private static final Pattern SERVERNAME_PREFIX_PATTERN = Pattern.compile(
            "([A-z0-9][A-z0-9-]*[A-z0-9]*(?:\\.[A-z0-9][A-z0-9-]*[A-z0-9]*)+)");
    private static final Pattern NICKNAME_PREFIX_PATTERN = Pattern.compile(
            "([A-z\\x5B-\\x60\\x7B-\\x7D](?:[A-z0-9\\x5B-\\x60\\x7B-\\x7D-]){0,8})(?:(?:!([^!@]+))?@([^!@]+))?");
    private static final Pattern COMMAND_PATTERN = Pattern.compile(
            "[A-z]+|[0-9]{3}");
    private static final Pattern FORMATTING_PATTERN = Pattern.compile(
            "(\u0002)|(\u001D)|(\u001F)|(\u000F)|(\u0003)([0-9]{2})?(?:,([0-9]{2}))?|(\u0016)");
    
    private InputStreamReader r;

    /**
     * State machine - each enum represents a parse state, and the next method represents the state transition to the
     * next parse state.
     */
    private enum State {
        Init {
            @Override
            public State next(char c, MessageParser p) {
                if(c == ':') {
                    return Prefix;
                }
                else if(isCommandChar(c)) {
                    p.appendToken(c);
                    return Command;
                }
                else if(isCRLF(c)) {
                    return Init;
                }
                return Error;
            }
        }, 
        Prefix {
            @Override
            public State next(char c, MessageParser p) {
                if((c == ' ' || isCRLF(c)) && p.consumePrefix()) {
                    return c == ' ' ? Command : Error;
                }
                else {
                    p.appendToken(c);
                    return Prefix;
                }
            }
        },
        Command {
            @Override
            public State next(char c, MessageParser p) {
                if(isCommandChar(c)) {
                    p.appendToken(c);
                    return Command;
                }
                else if((c == ' ' || isCRLF(c)) && p.consumeCommand()) {
                    return c == ' ' ? Parameters : Done;
                }
                return Error;
            }
        },
        Parameters {
            @Override
            public State next(char c, MessageParser p) {
                if(c == ':') {
                    return TrailingParameter;
                }
                else {
                    p.appendToken(c);
                    return MiddleParameter;
                }
            }
        },
        MiddleParameter {
            @Override
            public State next(char c, MessageParser p) {
                if(c == ' ' || isCRLF(c)) {
                    p.consumeParameter();
                    return c == ' ' ? Parameters : Done;
                }
                else {
                    p.appendToken(c);
                    return MiddleParameter;
                }
            }
        },
        TrailingParameter {
            @Override
            public State next(char c, MessageParser p) {
                if(isCRLF(c)) {
                    p.consumeParameter();
                    return Done;
                }
                else {
                    p.appendToken(c);
                    return TrailingParameter;
                }
            }
        },
        Done {
            @Override
            public State next(char c, MessageParser p) {
                return null;
            }
        },
        Error {
            @Override
            public State next(char c, MessageParser p) {
                if(isCRLF(c)) {
                    return Init;
                }
                return Error;
            }
        };

        public abstract State next(char c, MessageParser p);

        protected boolean isCommandChar(char c) {
            return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9';
        }
        
        protected boolean isCRLF(char c) {
            return c == '\r' || c == '\n';
        }
    }

    /**
     * Create a message reader
     * 
     * @param is      - input byte stream of IRC server responses
     * @param charset - character encoding of the byte stream
     * @throws UnsupportedEncodingException 
     */
    public IrcMessageReader(InputStream is, Charset charset) throws IOException {
        //s = new Scanner(is, charset.name());
        r = new InputStreamReader(is, charset.name());
    }

    /**
     * Read the next message from the IRC server
     * 
     * @return response transfromed to {@link IrcMessage} format, or null if no more data from stream
     * @throws IOException 
     */
    public IrcMessage nextMessage() throws IOException {
        return new MessageParser(r).getMessage();
    }

    /**
     * Parse the input stream to produce a {@link IrcMessage}
     * 
     * Holds parsing state for a single parse. Create a new instance for each parse.
     */
    public static class MessageParser {
        private InputStreamReader r;
        private String command;
        private String servername;
        private String nickname;
        private String user;
        private String host;
        private List<String> parameters;
        private List<TextStyle> styles;
        private StringBuilder token;

        /**
         * Create a message parser with fresh parse state
         * 
         * @param r - {@link Scanner} wrapping the input stream
         */
        public MessageParser(InputStreamReader r) {
            this.r = r;
            init();
        }

        /**
         * Reset MessageParser state
         */
        private void init() {
            this.servername = null;
            this.nickname = null;
            this.user = null;
            this.host = null;
            this.parameters = new ArrayList<String>();
            this.styles = new ArrayList<TextStyle>();
            this.token = new StringBuilder();
        }
        
        /**
         * Consume the prefix token, mapping the token to the servername or the components of the user information
         * 
         * @return true if mapping was successful, false otherwise
         */
        public boolean consumePrefix() {
            String prefix = getToken();
            Matcher m;
            if(SERVERNAME_PREFIX_PATTERN.matcher(prefix).matches()) {
                servername = prefix;
                return true;
            }
            else if((m = NICKNAME_PREFIX_PATTERN.matcher(prefix)).matches()) {
                nickname = m.group(1);
                user = m.group(2);
                host = m.group(3);
                return true;
            }
            return false;
        }
        
        /**
         * Consume a command token, validating the alpha token or the numeric token
         * 
         * @return true if valid command, false otherwise
         */
        public boolean consumeCommand() {
            String commandVal = getToken();
            if(COMMAND_PATTERN.matcher(commandVal).matches()) {
                command = commandVal;
                return true;
            }
            return false;
        }
        
        public void consumeParameter() {
            parameters.add(parseTrailingFormatting(getToken()));
        }

        /**
         * Parse the next message from the input stream
         * 
         * @return the parsed message
         * 
         * @throws IOException 
         */
        public IrcMessage getMessage() throws IOException {
            int i;
            State s = State.Init;
            while((i = r.read()) != -1 && (s = s.next((char) i, this)) != State.Done) {
                if(s == State.Init) {
                    init();
                }
            }
            return i == -1 ? null : new IrcMessage(servername, nickname, user, host, command, parameters, styles);
        }
        
        /**
         * Reset token to initial state to start building a new one
         */
        private void resetToken() {
            token = new StringBuilder();
        }
        
        /**
         * Add the character to the token
         * 
         * @param c - the character to add
         */
        private void appendToken(int c) {
            token.append((char) c);
        }
        
        /**
         * Extract String value out of token, resetting it in the process
         * 
         * @return the String value of the accumulated token characters
         */
        private String getToken() {
            String tokenVal = token.toString();
            resetToken();
            return tokenVal;
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
