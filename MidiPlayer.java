import javax.sound.midi.*;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;import javax.swing.JProgressBar;import javax.swing.JButton;
import java.awt.GridLayout;
import java.util.HashMap;
class MidiPlayer extends JPanel {
	private Sequence sequence;
	private Receiver receiver;

	private final JProgressBar scrubber;
	private final int maxScrubber = 9999;//your display is probably not this wide
	private final JButton pausePlay,repeat;
	private final String pausePlayPausedText = "Play";
	private final String pausePlayPlayingText = "Pause";
	private final String repeatYesText = "Don't Repeat";
	private final String repeatNoText = "Do Repeat";

	private MidiEvent[] allEvents;
	private Thread player;
	private boolean restart;
	private int currEventIdx;
	private double ticksPerMillisecond,millisecondsPerTick;
	private long sequenceTickLength,currentTick;

	public MidiPlayer() {this(null,null);}
	public MidiPlayer(final Sequence sequence,final Receiver receiver) {
		super(new GridLayout(1,0));
		player = null;
		restart = false;
		currEventIdx = 0;
		sequenceTickLength = 0;
		currentTick = 0;
		setReceiver(receiver);
		setSequence(sequence);
		pausePlay = new JButton(pausePlayPausedText);
		repeat = new JButton(repeatNoText);
		scrubber = new JProgressBar(JProgressBar.HORIZONTAL,0,0);
		scrubber.setMaximum(maxScrubber);
		pausePlay.addActionListener(SwingUtils.actionListener(ae->{
			SwingUtilities.invokeLater(()->{
				if (player==null) {
					start();
				}else {
					stop();
				}
			});
		}));
		repeat.addActionListener(SwingUtils.actionListener(ae->{
			SwingUtilities.invokeLater(()->{
				if (repeat.getText().equals(repeatYesText)) {
					repeat.setText(repeatNoText);
				}else if (repeat.getText().equals(repeatNoText)) {
					repeat.setText(repeatYesText);
				}
			});
		}));
		scrubber.addChangeListener(SwingUtils.changeListener(ce->{
			setCurrTick((long)(((double)scrubber.getValue()/maxScrubber)*sequenceTickLength));
		}));
		scrubber.addMouseListener(SwingUtils.mousePressReleaseListener(me->{
			restart = (player!=null);
			if (restart) stop();
		},me->{
			if (restart) start();
		}));
		add(repeat);
		add(pausePlay);
		add(scrubber);
	}
	public void setReceiver(final Receiver receiver) {
		if (receiver!=null) {
			this.receiver = receiver;
		}
	}
	//returns false if tick<0 or if (tick is too large and we aren't repeating back to the beginning)
	//you should stop playing if this returns false
	private boolean setCurrTick(final long tick) {
		if (tick<0) {
			currentTick = 0;
			return false;
		}else if (tick>=sequenceTickLength) {
			if (repeat.getText().equals(repeatYesText)) {
				currentTick = tick%sequenceTickLength;
				return true;
			}else {
				currentTick = sequenceTickLength-1;
				return false;
			}
		}else {
			currentTick = tick;
			return true;
		}
	}
	public void setSequence(final Sequence sequence) {
		if (sequence!=null) {
			currEventIdx = 0;
			sequenceTickLength = sequence.getTickLength();
			//sequence.getMicrosecondLength()
			final float divisionType = sequence.getDivisionType();
			if (divisionType==Sequence.PPQ) {//todo: don't assume 1 beat per second
				ticksPerMillisecond = sequence.getResolution()/1000d;
				millisecondsPerTick = 1000d/sequence.getResolution();
			}else if (divisionType==Sequence.SMPTE_24) {
				ticksPerMillisecond = sequence.getResolution()/(24/1000d);
				millisecondsPerTick = (24/1000d)/sequence.getResolution();
			}else if (divisionType==Sequence.SMPTE_25) {
				ticksPerMillisecond = sequence.getResolution()/(25/1000d);
				millisecondsPerTick = (25/1000d)/sequence.getResolution();
			}else if (divisionType==Sequence.SMPTE_30) {
				ticksPerMillisecond = sequence.getResolution()/(30/1000d);
				millisecondsPerTick = (30/1000d)/sequence.getResolution();
			}else if (divisionType==Sequence.SMPTE_30DROP) {
				ticksPerMillisecond = sequence.getResolution()/(29.97/1000d);
				millisecondsPerTick = (29.97/1000d)/sequence.getResolution();
			}else {
				ticksPerMillisecond = 1;
				millisecondsPerTick = 1;
			}
			ticksPerMillisecond *= 3d;
			millisecondsPerTick /= 3d;
			scrubber.setValue(0);
			allEvents = loadTrackEvents(sequence.getTracks());
		}
	}
	private MidiEvent[] loadTrackEvents(final Track[] tracks) {
		int nEvents = 0;
		final int nTracks = tracks.length;
		for (int i=0;i<nTracks;++i) {
			nEvents += tracks[i].size();
			//System.out.println(nEvents);
		}
		final MidiEvent[] out = new MidiEvent[nEvents];
		final int[] trackIndicies = new int[nTracks];
		for (int j=0;j<nEvents;++j) {
			long minNextTick = -1;
			int minNextTickTrack = -1;
			for (int i=0;i<nTracks;++i) {
				if (trackIndicies[i]<tracks[i].size()) {
					final long tick = tracks[i].get(trackIndicies[i]).getTick();
					if (minNextTick==-1||minNextTickTrack==-1||tick<minNextTick) {//if two tracks have the same next tick, the track with smaller index is chosen because of the strict < instead of <=
						minNextTickTrack = i;
						minNextTick = tick;
					}
				}
			}
			//System.out.println(nTracks+", "+minNextTickTrack+", "+tracks[minNextTickTrack].size()+", "+trackIndicies[minNextTickTrack]);
			out[j] = tracks[minNextTickTrack].get(trackIndicies[minNextTickTrack]++);
		}
		return out;
	}
	public void start() {
		pausePlay.setText(pausePlayPlayingText);
		if (player!=null) {
			player.interrupt();
			try {
				player.join(1);
			}catch(final InterruptedException ie) {}
			player = null;
		}
		player = new Thread(()->{
			final long startTime = System.currentTimeMillis();
			long startTick = currentTick;
			//todo: initially try starting with i=currEventIdx, and if we don't immediately fail, don't try to start from i=0
			long tick;
			for (int i=0;i<allEvents.length;++i) {
				tick=allEvents[i].getTick();
				if (tick>startTick) {//the event is after our current tick
					currEventIdx = i;
					break;
				}
			}
			while (!Thread.interrupted()&&setCurrTick(startTick+(long)((System.currentTimeMillis()-startTime)*ticksPerMillisecond))) {
				startTick = startTick % sequenceTickLength;
				for (;allEvents[currEventIdx].getTick()<currentTick;++currEventIdx) {
					receiver.send(allEvents[currEventIdx].getMessage(),-1);
				}
				//try {Thread.sleep(3);}catch(final InterruptedException ie){}
			}
			pausePlay.setText(pausePlayPausedText);
		});
		player.start();
	}
	public void stop() {
		pausePlay.setText(pausePlayPausedText);
		if (player!=null) {
			player.interrupt();
			try {
				player.join(1);
			}catch(final InterruptedException ie) {}
			player = null;
		}
	}
}
