package server.po;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import common.io.YData;
import common.po.YIPoint;

public class Cue implements YData {

	YIPoint	cueTip;
	YIPoint	cueTipOffset;
	int		power	= 0;

	public Cue() {
		cueTip = new YIPoint();
		cueTipOffset = new YIPoint();
	}

	public void read(DataInput input) throws IOException {
		input.readInt();
		cueTip.read(input);
		cueTipOffset.read(input);
		power = input.readInt();
		if (power < 0)
			power = 0;
		if (power > 120)
			power = 120;
	}

	public void write(DataOutput output) throws IOException {
		output.writeInt(20);
		cueTip.write(output);
		cueTipOffset.write(output);
		output.writeInt(power < 0 ? 0 : power > 120 ? 120 : power);
	}

}
