package dirc.core.message;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class IrcMessageReaderColorsTest {
    @Test
    public void should_return_no_style_spans_for_plain_text() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :Test\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Collections.emptyList(), m.getTextStyles());
    }
    
    @Test
    public void should_return_style_span_for_bold_text() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u0002Test\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Test"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(4, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Bold));
    }
    
    @Test
    public void should_return_style_span_for_bold_and_italic_text() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u0002\u001DTest\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Test"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(4, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Bold));
        assertTrue(style.is(TextStyle.Style.Italic));
    }
    
    @Test
    public void should_return_style_span_for_bold_and_italic_and_underlined_text() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u0002\u001D\u001FTest\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Test"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(4, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Bold));
        assertTrue(style.is(TextStyle.Style.Italic));
        assertTrue(style.is(TextStyle.Style.Underlined));
    }
    
    @Test
    public void should_return_multiple_style_span_for_when_style_starts_later_in_text() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u0002Hello \u001DWorld\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(2, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(6, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Bold));
        style = styles.get(1);
        assertEquals(6, style.getStart());
        assertEquals(11, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Bold));
        assertTrue(style.is(TextStyle.Style.Italic));
    }

    @Test
    public void should_return_reset_formatting() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u0002Hello \u000FWorld\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(2, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(6, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Bold));
        style = styles.get(1);
        assertEquals(6, style.getStart());
        assertEquals(11, style.getEnd());
        assertFalse(style.is(TextStyle.Style.Bold));
    }

    @Test
    public void should_change_foreground_color() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u000300Hello World\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(11, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Color));
        assertEquals(TextStyle.Color.White, style.getForeground());
        assertNull(style.getBackground());
    }

    @Test
    public void should_change_background_color() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u0003,00Hello World\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(11, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Color));
        assertNull(style.getForeground());
        assertEquals(TextStyle.Color.White, style.getBackground());
    }

    @Test
    public void should_change_foreground_and_background_color() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u000304,02Hello World\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(11, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Color));
        assertEquals(TextStyle.Color.Red, style.getForeground());
        assertEquals(TextStyle.Color.Blue, style.getBackground());
    }

    @Test
    public void should_restore_defaults_if_no_color_specified() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u0003Hello World\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertNull(style.getForeground());
        assertNull(style.getBackground());
    }

    @Test
    public void should_restore_default_if_color_not_known() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u000304,99Hello World\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(TextStyle.Color.Red, style.getForeground());
        assertNull(style.getBackground());
    }
    
    @Test
    public void should_reset_colors_if_reset_formatting() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :\u000304,02Hello \u000FWorld\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(2, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(0, style.getStart());
        assertEquals(6, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Color));
        assertEquals(TextStyle.Color.Red, style.getForeground());
        assertEquals(TextStyle.Color.Blue, style.getBackground());
        style = styles.get(1);
        assertTrue(style.isPlain());
    }

    @Test
    public void should_ignore_style_at_end() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :Hello World\u0002\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        assertEquals(Collections.emptyList(), m.getTextStyles());
    }

    @Test
    public void should_start_span_from_first_occurence_of_formatting() throws IOException {
        InputStream is = new ByteArrayInputStream("NOTICE :Hello \u0002World\r\n".getBytes("UTF-8"));
        IrcMessageReader r = new IrcMessageReader(is, Charset.forName("UTF-8"));
        IrcMessage m = r.nextMessage();
        
        assertEquals(Arrays.asList("Hello World"), m.getParameters());
        List<TextStyle> styles = m.getTextStyles();
        assertEquals(1, styles.size());
        TextStyle style = styles.get(0);
        assertEquals(6, style.getStart());
        assertEquals(11, style.getEnd());
        assertTrue(style.is(TextStyle.Style.Bold));
    }
}
