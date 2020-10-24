import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyListener;import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;import java.awt.event.MouseEvent;import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import java.util.HashSet;
import java.util.function.BiConsumer;
public class PianoUI extends JPanel implements KeyListener,MouseListener,MouseMotionListener {
	private int minNote,maxNote;
	private boolean autoExtend;
	private boolean[] notesOn;
	private int hovered = -1;
	public double keyHeightRatio=.6,keyWidthRatio=.4;
	public Color pressedColor = Color.RED;
	private final HashSet<BiConsumer<MouseEvent,Integer>> mouseListeners;
	public PianoUI(final int minNoteInclusive,final int maxNoteInclusive,final boolean autoExtendKeyboard) {
		minNote = minNoteInclusive;
		maxNote = maxNoteInclusive+1;
		autoExtend = autoExtendKeyboard;
		notesOn = new boolean[maxNote-minNote];
		mouseListeners = new HashSet<>();
	}
	public boolean isAutoExtendKeyboard() {return autoExtend;}//todo: implement autoextend
	public void setAutoExtendKeyboard(final boolean autoExtendKeyboard) {autoExtend = autoExtendKeyboard;}
	public void noteOn(final int note) {
		if (note>=minNote&&note<maxNote) {
			notesOn[note-minNote]=true;
		}
	}
	public void noteOff(final int note) {
		if (note>=minNote&&note<maxNote) {
			notesOn[note-minNote]=false;
		}
	}
	public void paintComponent(final Graphics g) {
		final int bigKeys = bigKeys(minNote,maxNote-1);
		final boolean leastSmall = smallKey(minNote);
		final boolean mostSmall = smallKey(maxNote-1);
		final double bigW = g.getClipBounds().getWidth()/(bigKeys+((leastSmall&&mostSmall)?keyWidthRatio:((leastSmall||mostSmall)?keyWidthRatio/2d:0)));
		double x = leastSmall?keyWidthRatio*bigW/2d:0;
		for (int i=minNote;i<maxNote;++i) {
			final boolean smallKey = smallKey(i);
			if (notesOn[i-minNote]) {
				g.setColor(pressedColor);
			}else if (smallKey)
				g.setColor(Color.BLACK);
			else
				g.setColor(Color.WHITE);
			drawKey(g,i,bigW,x);
			if (!smallKey)
				x += bigW;
		}
		final String str = ""+(System.currentTimeMillis()/1000)+"."+(System.currentTimeMillis()%1000);
		final int y = (int)g.getClipBounds().getHeight()-g.getFontMetrics().getHeight()-10;
		g.setColor(Color.YELLOW);
		g.fillRect(10,y,g.getFontMetrics().stringWidth(str),g.getFontMetrics().getHeight());
		g.setColor(Color.BLACK);
		g.drawString(str,10,y+g.getFontMetrics().getHeight());
	}
	public void drawKey(final Graphics g,final int note,final double bigWidth,final double offsetFromBigEdge) {
		switch(noteMod12(note)) {
			//▙ ▟▖▟▙ ▟▖▟▖▟
			//0123456789AB
			case 0://▙
			case 5:
				g.fillRect((int)offsetFromBigEdge,(int)(keyHeightRatio*g.getClipBounds().getHeight()),(int)bigWidth,(int)((1-keyHeightRatio)*g.getClipBounds().getHeight()));
				g.fillRect((int)offsetFromBigEdge,0,(int)(bigWidth*(1-keyWidthRatio/2d)),(int)(keyHeightRatio*g.getClipBounds().getHeight()));
				return;
			case 2://▟▖
			case 7:
			case 9:
				g.fillRect((int)offsetFromBigEdge,(int)(keyHeightRatio*g.getClipBounds().getHeight()),(int)bigWidth,(int)((1-keyHeightRatio)*g.getClipBounds().getHeight()));
				g.fillRect((int)(offsetFromBigEdge+bigWidth*keyWidthRatio/2d),0,(int)(bigWidth*(1-keyWidthRatio)),(int)(keyHeightRatio*g.getClipBounds().getHeight()));
				return;
			case 4://▟
			case 0xB:
				g.fillRect((int)offsetFromBigEdge,(int)(keyHeightRatio*g.getClipBounds().getHeight()),(int)bigWidth,(int)((1-keyHeightRatio)*g.getClipBounds().getHeight()));
				g.fillRect((int)(offsetFromBigEdge+bigWidth*keyWidthRatio/2d),0,(int)(bigWidth*(1-keyWidthRatio/2d)),(int)(keyHeightRatio*g.getClipBounds().getHeight()));
				return;
			case 1:
			case 3:
			case 6:
			case 8:
			case 0xA:
				g.fillRect((int)(offsetFromBigEdge-keyWidthRatio*bigWidth/2d),0,(int)(keyWidthRatio*bigWidth),(int)(keyHeightRatio*g.getClipBounds().getHeight()));
				return;
		}
	}
	public int bigKeys(final int minNoteInclusive,final int maxNoteInclusive) {
		final int diff = maxNoteInclusive-minNoteInclusive;
		if (diff<0)
			return 0;
		else if (diff==0)
			return smallKey(minNoteInclusive)?0:1;
		int bigKeys=0;
		int minOctaveAligned,maxOctaveAligned;
		for (minOctaveAligned=minNoteInclusive;minOctaveAligned%12!=0&&minOctaveAligned<=maxNoteInclusive;++minOctaveAligned)
			if (!smallKey(minOctaveAligned))
				++bigKeys;
		for (maxOctaveAligned=maxNoteInclusive;maxOctaveAligned%12!=11&&maxOctaveAligned>minOctaveAligned;--maxOctaveAligned)
			if (!smallKey(maxOctaveAligned))
				++bigKeys;
		if (minOctaveAligned%12==0&&maxOctaveAligned%12==11)
			bigKeys += (maxOctaveAligned-minOctaveAligned+1)/12*7;
		return bigKeys;
	}
	public int noteMod12(final int note) {
		if (note<0)return 12+(note%12);
		else return note%12;
	}
	public boolean smallKey(final int note) {
		switch(noteMod12(note)) {
			case 0:
			case 2:
			case 4:
			case 5:
			case 7:
			case 9:
			case 11:
				return false;
			case 1:
			case 3:
			case 6:
			case 8:
			case 10:
				return true;
			default:
				return false;
		}
	}
	//KeyListener
	public void keyPressed(final KeyEvent ke) {}
	public void keyReleased(final KeyEvent ke) {}
	public void keyTyped(final KeyEvent ke) {}
	//MouseListener
	public void mouseEntered(final MouseEvent me) {}
	public void mouseExited(final MouseEvent me) {}
	public void mousePressed(final MouseEvent me) {doMouse(me);}
	public void mouseReleased(final MouseEvent me) {doMouse(me);}
	public void mouseClicked(final MouseEvent me) {doMouse(me);}
	//MouseMotionListener
	public void mouseMoved(final MouseEvent me) {doMouse(me);}
	public void mouseDragged(final MouseEvent me) {doMouse(me);}
	//todo: actually make this compute the selected key
	private void doMouse(final MouseEvent me) {
		final int bigKeys = bigKeys(minNote,maxNote-1);
		final boolean leastSmall = smallKey(minNote);
		final boolean mostSmall = smallKey(maxNote-1);
		final double bigW = getWidth()/(bigKeys+((leastSmall&&mostSmall)?keyWidthRatio:((leastSmall||mostSmall)?keyWidthRatio/2d:0)));
		final double bigKeyX = (double)(me.getX()-(leastSmall?keyWidthRatio*bigW/2d:0))/getWidth();
		int note;
		if (me.getY()>keyHeightRatio*getHeight()) {
			note = (int)(bigKeyX);
		}else {
			note = (int)(bigKeyX);
		}
		for (final BiConsumer<MouseEvent,Integer> bc: mouseListeners) {
			bc.accept(me,note);
		}
	}
}

