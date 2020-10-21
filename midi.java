import javax.sound.midi.*;
import javax.swing.SwingUtilities;import javax.swing.JFrame;
public class midi {
	public static void main(final String[] args) {
		final JFrame w = new JFrame();
		final PianoUI piano = new PianoUI(36,96,false);
		final Transmitter tr = MidiUtil.firstTransmitterExcept("gervill","real time sequencer");
		final Receiver rec = MidiUtil.firstReceiverExcept("gervill","real time sequencer");
		tr.setReceiver(new Receiver(){
			public void send(final MidiMessage message,final long t) {
				if (message instanceof ShortMessage) {
					final ShortMessage sm = (ShortMessage)message;
					//MidiUtil.shortMessageCommand(sm)
					if (sm.getData2()==0)
						piano.noteOff(sm.getData1());
					else
						piano.noteOn(sm.getData1());
					SwingUtilities.invokeLater(()->{piano.repaint();});
				}else if (message instanceof MetaMessage) {
					final MetaMessage mm = (MetaMessage)message;
					System.out.print("MetaMessage type = "+MidiUtil.metaMessageType(mm)+", ");
					for(final byte b:mm.getData())
						System.out.print(String.format("%02X ",b));
					System.out.println();
				}else if (message instanceof SysexMessage) {
					final SysexMessage sm = (SysexMessage)message;
					System.out.println("SysexMessage ");
					for(final byte b:sm.getData())
						System.out.print(String.format("%02X ",b));
					System.out.println();
				}
			}
			public void close() {}
		});
		w.add(piano);
		w.pack();
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
	}
}
