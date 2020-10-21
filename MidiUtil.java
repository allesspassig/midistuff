import javax.sound.midi.*;
import java.util.function.*;
class MidiUtil {
	public static String metaMessageType(final MetaMessage mm) {return metaMessageType(mm.getType());}
	public static String metaMessageType(final int type) {
		switch(type) {
			case 0:return "Sequence number";
			case 1:return "Text";
			case 2:return "Copyright";
			case 3:return "Track name";
			case 4:return "Instrument name";
			case 5:return "Lyrics";
			case 6:return "Marker";
			case 7:return "Cue marker";
			case 9:return "Device name";
			case 32:return "Channel prefix";
			case 33:return "Midi port";
			case 47:return "End of track";
			case 81:return "Set tempo";
			case 84:return "SMPTE offset";
			case 88:return "Time signature";
			case 89:return "Key signature";
			case 127:return "Sequencer specific";
			default:return "??";
		}
	}
	public static String shortMessageCommand(final ShortMessage sm) {return shortMessageCommand(sm.getCommand());}
	public static String shortMessageCommand(final int command) {
		switch(command) {
			case ShortMessage.ACTIVE_SENSING:return "ACTIVE_SENSING";// Active Sensing message (0xFE, or 254).
			case ShortMessage.CHANNEL_PRESSURE:return "CHANNEL_PRESSURE";// Channel Pressure (Aftertouch) message (0xD0, or 208)
			case ShortMessage.CONTINUE:return "CONTINUE";// Continue message (0xFB, or 251).
			case ShortMessage.CONTROL_CHANGE:return "CONTROL_CHANGE";// Control Change message (0xB0, or 176)
			case ShortMessage.END_OF_EXCLUSIVE:return "END_OF_EXCLUSIVE";// End of System Exclusive message (0xF7, or 247).
			case ShortMessage.MIDI_TIME_CODE:return "MIDI_TIME_CODE";// MIDI Time Code Quarter Frame message (0xF1, or 241).
			case ShortMessage.NOTE_OFF:return "NOTE_OFF";// Note Off message (0x80, or 128)
			case ShortMessage.NOTE_ON:return "NOTE_ON";// Note On message (0x90, or 144)
			case ShortMessage.PITCH_BEND:return "PITCH_BEND";// Pitch Bend message (0xE0, or 224)
			case ShortMessage.POLY_PRESSURE:return "POLY_PRESSURE";// Polyphonic Key Pressure (Aftertouch) message (0xA0, or 160)
			case ShortMessage.PROGRAM_CHANGE:return "PROGRAM_CHANGE";// Program Change message (0xC0, or 192)
			case ShortMessage.SONG_POSITION_POINTER:return "SONG_POSITION_POINTER";// Song Position Pointer message (0xF2, or 242).
			case ShortMessage.SONG_SELECT:return "SONG_SELECT";// MIDI Song Select message (0xF3, or 243).
			case ShortMessage.START:return "START";// Start message (0xFA, or 250).
			case ShortMessage.STOP:return "STOP";// Stop message (0xFC, or 252).
			case ShortMessage.SYSTEM_RESET:return "SYSTEM_RESET";// System Reset message (0xFF, or 255).
			case ShortMessage.TIMING_CLOCK:return "TIMING_CLOCK";// Timing Clock message (0xF8, or 248).
			case ShortMessage.TUNE_REQUEST:return "TUNE_REQUEST";// Tune Request message (0xF6, or 246).
			default:return "??";
		}
	}
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
				device.open();
				return ret.apply(device);
			}catch(final MidiUnavailableException mue) {continue;}
		}
		return null;
	}
}

