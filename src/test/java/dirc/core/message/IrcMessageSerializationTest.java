package dirc.core.message;

import static org.junit.Assert.*;

import org.junit.Test;

public class IrcMessageSerializationTest {
    @Test
    public void should_serialize_non_space_parameters_with_middle_parameters() {
        IrcMessage msg = new IrcMessage("USER", "guest", "0", "*", "Joe");
        assertEquals("USER guest 0 * Joe\r\n", msg.serialize());
    }
    
    @Test
    public void should_serialize_middle_colon_parameter() {
        IrcMessage msg = new IrcMessage("USER", "guest:1", "0", "*", "John Q. Public");
        assertEquals("USER guest:1 0 * :John Q. Public\r\n", msg.serialize());
    }
    
    @Test
    public void should_serialize_trailing_space_parameter_with_trailing_parameter() {
        IrcMessage msg = new IrcMessage("USER", "guest", "0", "*", "John Q. Public");
        assertEquals("USER guest 0 * :John Q. Public\r\n", msg.serialize());
    }
    
    @Test
    public void should_serialize_null_trailing_parameter_with_blank_trailing_parameter() {
        IrcMessage msg = new IrcMessage("USER", "guest", "0", "*", null);
        assertEquals("USER guest 0 * :\r\n", msg.serialize());
    }
    
    @Test
    public void should_serialize_empty_trailing_parameter_with_blank_trailing_parameter() {
        IrcMessage msg = new IrcMessage("USER", "guest", "0", "*", "");
        assertEquals("USER guest 0 * :\r\n", msg.serialize());
    }
    
    @Test
    public void should_serialize_blank_trailing_parameter_with_blank_trailing_parameter() {
        IrcMessage msg = new IrcMessage("USER", "guest", "0", "*", "   ");
        assertEquals("USER guest 0 * :   \r\n", msg.serialize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_reject_middle_space_parameter() {
        IrcMessage msg = new IrcMessage("USER", "John Q. Public", "0", "*", "guest");
        msg.serialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_reject_middle_leading_colon_parameter() {
        IrcMessage msg = new IrcMessage("USER", ":guest", "0", "*", "John Q. Public");
        msg.serialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_reject_null_middle_parameter() {
        IrcMessage msg = new IrcMessage("USER", null, "0", "*", "John Q. Public");
        msg.serialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_reject_empty_middle_parameter() {
        IrcMessage msg = new IrcMessage("USER", "", "0", "*", "John Q. Public");
        msg.serialize();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void should_reject_blank_middle_parameter() {
        IrcMessage msg = new IrcMessage("USER", "   ", "0", "*", "John Q. Public");
        msg.serialize();
    }
}