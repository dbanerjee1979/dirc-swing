package dirc.core.event;

public class PingEvent extends IrcEvent {
    private String servername;

    public PingEvent(String servername) {
        super(null, null);
        this.servername = servername;
    }
    
    public String getServername() {
        return servername;
    }
}
