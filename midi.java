import javax.sound.midi.*;
import java.util.List;
import java.util.Scanner;
import java.util.function.*;
public class midi {
	public static void main(final String[] args) {
		final Transmitter tr = MidiUtil.firstTransmitterExcept("gervill","real time sequencer");
		final Receiver rec = MidiUtil.firstReceiverExcept("gervill","real time sequencer");
		tr.setReceiver(new Receiver(){
			public void send(final MidiMessage mm,final long t) {
				System.out.println("time "+t+", message "+mm);
			}
			public void close() {}
		});
		final javax.swing.JFrame w = new javax.swing.JFrame();
		w.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
		while (w.isVisible()) {
			try {
				final MidiMessage mm = new ShortMessage(ShortMessage.NOTE_ON,0,(int)(Math.random()*100),93);
				rec.send(mm,-1);
			}catch(final InvalidMidiDataException imde) {System.out.println("invalid data");}
			try{Thread.sleep(100);}catch(final InterruptedException ie){}
		}
	}
}
class MidiUtil {
	public static MidiDevice.Info[] allInfo() {return MidiSystem.getMidiDeviceInfo();}
	public static int maxRec(final MidiDevice.Info info) {
		try {
			return maxRec(MidiSystem.getMidiDevice(info));
		}catch(final MidiUnavailableException mue) {
			return 0;
		}
	}
	public static int maxRec(final MidiDevice dev) {
		if (dev==null)
			return 0;
		else
			return dev.getMaxReceivers();
	}
	public static int maxTr(final MidiDevice.Info info) {
		try {
			return maxTr(MidiSystem.getMidiDevice(info));
		}catch(final MidiUnavailableException mue) {
			return 0;
		}
	}
	public static int maxTr(final MidiDevice dev) {
		if (dev==null)
			return 0;
		else
			return dev.getMaxTransmitters();
	}
	public static String amountToStr(final int amount) {
		switch(amount) {
			case -1:
				return "unlimited";
			case 0:
				return "no";
			default:
				return ""+amount;
		}
	}
	public static Receiver firstReceiverExcept(final String... unacceptableStrings) {
		return firstBlahExcept(info->{
			for (int j=0;j<unacceptableStrings.length;++j) {
				if (info.getName().toLowerCase().contains(unacceptableStrings[j].toLowerCase())||unacceptableStrings[j].toLowerCase().contains(info.getName().toLowerCase()))
					return false;
			}
			return true;
		},dev->dev.getMaxReceivers()!=0,dev->{try{return dev.getReceiver();}catch(final MidiUnavailableException mue){return null;}});
	}
	public static Transmitter firstTransmitterExcept(final String... unacceptableStrings) {
		return firstBlahExcept(info->{
			for (int j=0;j<unacceptableStrings.length;++j) {
				if (info.getName().toLowerCase().contains(unacceptableStrings[j].toLowerCase())||unacceptableStrings[j].toLowerCase().contains(info.getName().toLowerCase()))
					return false;
			}
			return true;
		},dev->dev.getMaxTransmitters()!=0,dev->{try{return dev.getTransmitter();}catch(final MidiUnavailableException mue){return null;}});
	}
	public static <T> T firstBlahExcept(final Function<MidiDevice.Info,Boolean> acceptableInfo,final Function<MidiDevice,Boolean> acceptableDevice,final Function<MidiDevice,T> ret) {
		final MidiDevice.Info[] infos = allInfo();
		for (int i=0;i<infos.length;++i) {
			if (!acceptableInfo.apply(infos[i]))
				continue;
			try {
				final MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
				if (!acceptableDevice.apply(device))
					continue;
				System.out.println("going with device "+device+", info "+infos[i]+", "+device.getMaxReceivers()+" supported receivers, "+device.getMaxTransmitters()+" supported transmitters");
				device.open();
				return ret.apply(device);
			}catch(final MidiUnavailableException mue) {continue;}
		}
		return null;
	}
}
