import javax.sound.midi.*;
import javax.swing.JPanel;
import java.awt.Color;import java.awt.Graphics;
import java.util.Collection;import java.util.LinkedHashMap;import java.util.Iterator;
import java.util.Optional;
public class SheetMusicPanel extends JPanel {
	public static class NoteRenderingHints {
		public static NoteRenderingHints DEFAULT = new NoteRenderingHints(0,Color.BLACK);
		public int transposeHalfSteps;
		public Color color;
		public NoteRenderingHints(final Color color) {
			this(0,color);
		}
		public NoteRenderingHints(final int transposeHalfSteps) {
			this(transposeHalfSteps,Color.BLACK);
		}
		public NoteRenderingHints(final int transposeHalfSteps,final Color color) {
			this.transposeHalfSteps = transposeHalfSteps;
			this.color = color;
		}
	}
	private long currTick,windowTickFuture,windowTickPast;
	private final LinkedHashMap<MidiEvent,NoteRenderingHints> events;//todo: don't assume this is ordered; instead use a proper data structure or order it on the fly
	public SheetMusicPanel() {
		events = new LinkedHashMap<>();
		currTick = 0;
		windowTickFuture = 1000;
		windowTickPast = 1000;
	}
	public SheetMusicPanel(final Collection<MidiEvent> events) {
		this();
		addNotes(events);
	}
	public SheetMusicPanel(final MidiEvent[] events) {
		this();
		addNotes(events);
	}
	public void addNotes(final Collection<MidiEvent> events) {
		for (final MidiEvent me:events) {
			this.events.put(me,NoteRenderingHints.DEFAULT);
		}
	}
	public void addNotes(final MidiEvent[] events) {
		for (final MidiEvent me:events) {
			this.events.put(me,NoteRenderingHints.DEFAULT);
		}
	}
	public void removeAllNotes() {
		this.events.clear();
	}
	public void setWindow(final long ticksFuture,final long ticksPast) {
		windowTickFuture = ticksFuture;
		windowTickPast = ticksPast;
	}
	public void setCurrTick(final long currTick) {
		this.currTick = currTick;
	}
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		MidiEvent me;
		long tick;
		final double currentX = (double)windowTickPast/(windowTickFuture+windowTickPast)*g.getClipBounds().getWidth();
		g.drawLine((int)currentX,0,(int)currentX,(int)g.getClipBounds().getHeight());
		final Iterator<MidiEvent> eventsIterator = events.keySet().iterator();
		while (eventsIterator.hasNext()) {
			if (eventsIterator.next().getTick()>currTick-windowTickPast) {//the event is after our current tick
				break;
			}
		}
		while (eventsIterator.hasNext()&&(tick=(me=eventsIterator.next()).getTick())<currTick+windowTickFuture) {
			final Optional<Byte> note = MidiUtil.messageNote(me.getMessage());
			if (note.isPresent()) {
				final Color col = events.get(me).color;
				g.setColor(col);
				final double x = (double)(tick-currTick+windowTickPast)/(windowTickFuture+windowTickPast)*g.getClipBounds().getWidth();
				final double y = (double)(events.get(me).transposeHalfSteps+note.get())/128d*g.getClipBounds().getHeight();
				g.fillOval((int)(x-10),(int)(y-8),(int)(2*10),(int)(2*8));
				if (me.getMessage() instanceof ShortMessage) {//must be true
					final ShortMessage sm = (ShortMessage)me.getMessage();
					switch(sm.getCommand()) {
						case ShortMessage.NOTE_ON:
						case ShortMessage.NOTE_OFF:
							g.setColor(new Color(255-col.getRed(),255-col.getGreen(),255-col.getBlue()));
							final String s = ""+sm.getData2();
							g.drawString(s,(int)(x-g.getFontMetrics().stringWidth(s)/2d),(int)(y+g.getFontMetrics().getHeight()/2d));
					}
				}
			}
		}
	}
}

