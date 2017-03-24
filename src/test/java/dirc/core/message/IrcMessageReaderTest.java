package dirc.core.message;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Test;

public class IrcMessageReaderTest {
    @Test
    public void should_read_alpha_command_with_no_arguments() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals("NOTICE", m.getCommand());
    }
    
    @Test
    public void should_read_alpha_command_with_inner_argument() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE abc\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("abc"), m.getParameters());
    }
    
    @Test
    public void should_read_alpha_command_with_multiple_inner_arguments() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE a b c\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("a", "b", "c"), m.getParameters());
    }
    
    @Test
    public void should_read_alpha_command_with_inner_argument_with_inner_colon() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE a:b c\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("a:b", "c"), m.getParameters());
    }
    
    @Test
    public void should_read_alpha_command_with_trailing_argument() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());
    }
    
    @Test
    public void should_read_alpha_command_with_mix_of_middle_and_trailing_arguments() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE a:b c :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("a:b", "c", "Hello World!"), m.getParameters());
    }
    
    public void should_read_numeric_command() throws IOException {
        InputStream is = new ByteArrayInputStream("372 :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals("372", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());
    }
    
    public void should_skip_numeric_command_with_less_than_three_digits() throws IOException {
        InputStream is = new ByteArrayInputStream("37 :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        assertNull(m);
    }
    
    public void should_skip_numeric_command_with_more_than_three_digits() throws IOException {
        InputStream is = new ByteArrayInputStream("3720 :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        assertNull(m);
    }
    
    public void should_skip_alpha_command_with_non_alpha_characters() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE! :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        assertNull(m);
    }
    
    public void should_skip_numeric_command_with_non_alpha_characters() throws IOException {
        InputStream is = new ByteArrayInputStream("372! :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        assertNull(m);
    }
    
    @Test
    public void should_read_servername_prefix() throws IOException {
        InputStream is = new ByteArrayInputStream(":foo.net NOTICE :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));

        IrcMessage m = r.nextMessage();
        assertEquals("foo.net", m.getServername());
        assertNull(m.getNickname());
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());

        assertNull(r.nextMessage());
    }

    @Test
    public void should_read_nickname_prefix() throws IOException {
        InputStream is = new ByteArrayInputStream(":joe NOTICE :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));

        IrcMessage m = r.nextMessage();
        assertNull(m.getServername());
        assertEquals("joe", m.getNickname());
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());

        assertNull(r.nextMessage());
    }

    @Test
    public void should_read_nickname_and_host_prefix() throws IOException {
        InputStream is = new ByteArrayInputStream(":joe@foo.net NOTICE :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));

        IrcMessage m = r.nextMessage();
        assertNull(m.getServername());
        assertEquals("joe", m.getNickname());
        assertEquals("foo.net", m.getHost());
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());

        assertNull(r.nextMessage());
    }

    @Test
    public void should_read_nickname_and_user_and_host_prefix() throws IOException {
        InputStream is = new ByteArrayInputStream(":joe!bob@foo.net NOTICE :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));

        IrcMessage m = r.nextMessage();
        assertNull(m.getServername());
        assertEquals("joe", m.getNickname());
        assertEquals("bob", m.getUser());
        assertEquals("foo.net", m.getHost());
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());

        assertNull(r.nextMessage());
    }
    
    @Test
    public void should_skip_message_with_prefix_having_nickname_and_user_but_no_host() throws IOException {
        InputStream is = new ByteArrayInputStream(":joe!bob NOTICE :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        assertNull(r.nextMessage());
    }

    @Test
    public void should_read_multiple_commands() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :Hello World!\r\nNOTICE :Bye World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));

        IrcMessage m;
        
        m = r.nextMessage();
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());

        m = r.nextMessage();
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Bye World!"), m.getParameters());
    }

    @Test
    public void should_return_null_at_end_of_stream() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));

        IrcMessage m = r.nextMessage();
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());

        assertNull(r.nextMessage());
    }

    @Test
    public void should_skip_invalid_message() throws IOException {
        InputStream is = new ByteArrayInputStream(":foo.net NOTICE!\r\nNOTICE :Hello World!\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));

        IrcMessage m = r.nextMessage();
        assertNull(m.getServername());
        assertEquals("NOTICE", m.getCommand());
        assertEquals(Arrays.asList("Hello World!"), m.getParameters());

        assertNull(r.nextMessage());
    }
}
