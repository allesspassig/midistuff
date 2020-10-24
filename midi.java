import javax.sound.midi.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;import javax.swing.JScrollPane;import javax.swing.JTextArea;import javax.swing.JCheckBox;import javax.swing.JPanel;import javax.swing.JFileChooser;import javax.swing.JButton;
import java.awt.GridLayout;import java.awt.BorderLayout;import java.awt.FlowLayout;
import java.util.function.Consumer;
import java.io.File;import java.io.IOException;
public class midi {
	public static void main(final String[] args) {
		final JFrame w = new JFrame();
		final JPanel leftPanel = new JPanel(new GridLayout(0,1));
		final JButton loadMidiFileButton = new JButton("Load file");
		final MidiPlayer player = new MidiPlayer();
		if (args.length>0) {
			try {
				player.setSequence(MidiSystem.getSequence(new File(args[0])));
			}catch(final InvalidMidiDataException imde) {
				System.err.println("invalid midi data when reading from command line args");
				imde.printStackTrace();
			}catch(final IOException ioe) {
				System.err.println("ioexception when reading from command line args");
				ioe.printStackTrace();
			}
		}
		loadMidiFileButton.addActionListener(SwingUtils.actionListener(ae->{
			final JFileChooser jfc = new JFileChooser(System.getProperty("user.home"));
			jfc.setFileFilter(new FileNameExtensionFilter("MIDI files","mid","midi"));
			int result = jfc.showOpenDialog(null);
			if (result==JFileChooser.APPROVE_OPTION) {
				final File file = jfc.getSelectedFile();
				try {
					player.setSequence(MidiSystem.getSequence(file));
				}catch(final InvalidMidiDataException imde) {
					System.err.println("invalid midi data when reading from ui chosen file");
					imde.printStackTrace();
				}catch(final IOException ioe) {
					System.err.println("ioexception when reading from ui chosen file");
					ioe.printStackTrace();
				}
			}
		}));
		final JCheckBox messagesShortCheckBox = new JCheckBox("ShortMessages",false);
		final JCheckBox messagesMetaCheckBox = new JCheckBox("MetaMessages",true);
		final JCheckBox messagesSysexCheckBox = new JCheckBox("SysexMessages",true);
		final JTextArea messagesTextArea = new JTextArea();
		final JScrollPane messagesTextAreaScrollPane = new JScrollPane(messagesTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		final JPanel messagesCheckBoxesPanel = new JPanel(new FlowLayout());
		messagesCheckBoxesPanel.add(messagesShortCheckBox);
		messagesCheckBoxesPanel.add(messagesMetaCheckBox);
		messagesCheckBoxesPanel.add(messagesSysexCheckBox);
		leftPanel.add(messagesCheckBoxesPanel);
		leftPanel.add(messagesTextAreaScrollPane);
		leftPanel.add(loadMidiFileButton);
		final PianoUI piano = new PianoUI(36,96,false);
		final Transmitter tr = MidiUtil.firstTransmitterExcept("gervill","real time sequencer");
		final Receiver rec = MidiUtil.firstReceiverExcept("gervill","real time sequencer");
		player.setReceiver(rec);
		tr.setReceiver(new Receiver(){
			public void send(MidiMessage message,final long t) {
				if (message instanceof ShortMessage) {
					if (((ShortMessage)message).getCommand()==ShortMessage.NOTE_ON&&((ShortMessage)message).getData2()==0) {
						try {
							message = new ShortMessage(ShortMessage.NOTE_OFF,((ShortMessage)message).getChannel(),((ShortMessage)message).getData1(),((ShortMessage)message).getData2());
						}catch(final InvalidMidiDataException imde) {}
					}
					final ShortMessage sm = (ShortMessage)message;
					SwingUtilities.invokeLater(()->{
						if (sm.getCommand()==ShortMessage.NOTE_ON)
							piano.noteOn(sm.getData1());
						else if (sm.getCommand()==ShortMessage.NOTE_OFF)
							piano.noteOff(sm.getData1());
						piano.repaint();
					});
					if (messagesShortCheckBox.isSelected()) {
						messagesTextArea.insert(MidiUtil.shortMessageCommand(sm)+":("+sm.getData1()+","+sm.getData2()+")\n",0);
					}
				}else if (message instanceof MetaMessage) {
					final MetaMessage mm = (MetaMessage)message;
					if (messagesMetaCheckBox.isSelected()) {
						final StringBuilder sb = new StringBuilder();
						sb.append(MidiUtil.metaMessageType(mm)+", ");
						for(final byte b:mm.getData())
							sb.append(String.format("%02X ",b));
						sb.append("\n");
						messagesTextArea.insert(sb.toString(),0);
					}
				}else if (message instanceof SysexMessage) {
					final SysexMessage sm = (SysexMessage)message;
					if (messagesSysexCheckBox.isSelected()) {
						final StringBuilder sb = new StringBuilder();
						sb.append("Sysex, ");
						for(final byte b:sm.getData())
							sb.append(String.format("%02X ",b));
						sb.append("\n");
						messagesTextArea.insert(sb.toString(),0);
					}
				}
			}
			public void close() {}
		});
		w.add(leftPanel,BorderLayout.WEST);
		w.add(piano,BorderLayout.CENTER);
		w.add(player,BorderLayout.SOUTH);
		w.pack();
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
	}
}
