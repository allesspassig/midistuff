import javax.swing.SwingUtilities;
import java.awt.event.ActionListener;import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;import javax.swing.event.ChangeEvent;
import java.awt.event.MouseListener;import java.awt.event.MouseEvent;import java.awt.event.MouseMotionListener;
import java.util.function.Consumer;
public class SwingUtils {
	public static ActionListener actionListener(final Consumer<ActionEvent> actionPerformed) {
		return new ActionListener(){public void actionPerformed(final ActionEvent ae){actionPerformed.accept(ae);}};
	}
	public static ChangeListener changeListener(final Consumer<ChangeEvent> stateChanged) {
		return new ChangeListener(){public void stateChanged(final ChangeEvent ce){stateChanged.accept(ce);}};
	}
	public static MouseListener mousePressReleaseListener(final Consumer<MouseEvent> mousePressed,final Consumer<MouseEvent> mouseReleased) {
		return new MouseListener(){
			public void mouseClicked(final MouseEvent me){}
			public void mouseEntered(final MouseEvent me){}
			public void mouseExited(final MouseEvent me){}
			public void mousePressed(final MouseEvent me){mousePressed.accept(me);}
			public void mouseReleased(final MouseEvent me){mouseReleased.accept(me);}
		};
	}
	public static MouseListener mouseReleaseListener(final Consumer<MouseEvent> mouseReleased) {
		return new MouseListener(){
			public void mouseClicked(final MouseEvent me){}
			public void mouseEntered(final MouseEvent me){}
			public void mouseExited(final MouseEvent me){}
			public void mousePressed(final MouseEvent me){}
			public void mouseReleased(final MouseEvent me){mouseReleased.accept(me);}
		};
	}
	public static MouseListener mousePressListener(final Consumer<MouseEvent> mousePressed) {
		return new MouseListener(){
			public void mouseClicked(final MouseEvent me){}
			public void mouseEntered(final MouseEvent me){}
			public void mouseExited(final MouseEvent me){}
			public void mousePressed(final MouseEvent me){mousePressed.accept(me);}
			public void mouseReleased(final MouseEvent me){}
		};
	}
	public static MouseMotionListener mouseMotionListener(final Consumer<MouseEvent> mouseMoved,final Consumer<MouseEvent> mouseDragged) {
		return new MouseMotionListener(){
			public void mouseMoved(final MouseEvent me){mouseMoved.accept(me);}
			public void mouseDragged(final MouseEvent me){mouseDragged.accept(me);}
		};
	}
	public static MouseMotionListener mouseMoveListener(final Consumer<MouseEvent> mouseMoved) {
		return new MouseMotionListener(){
			public void mouseMoved(final MouseEvent me){mouseMoved.accept(me);}
			public void mouseDragged(final MouseEvent me){}
		};
	}
	public static MouseMotionListener mouseDragListener(final Consumer<MouseEvent> mouseDragged) {
		return new MouseMotionListener(){
			public void mouseMoved(final MouseEvent me){}
			public void mouseDragged(final MouseEvent me){mouseDragged.accept(me);}
		};
	}
	public static void onEDT(final Runnable r) {
		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			SwingUtilities.invokeLater(r);
	}
}
