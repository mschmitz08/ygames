package common.po;

import java.util.Vector;

public final class NineBallTrainingSetup extends PoolSetup implements PoolConsts {

	protected static YIPoint	initPos[];
	static {
		initPos = NineBallSetup.initPos;
	}

	private IBall				whiteBall;
	private YIPoint				rackPos[];

	public NineBallTrainingSetup(Pool _pcls57) {
		super(_pcls57);
		rackPos = NineBallSetup.createRackPos();
		_p();
	}

	@Override
	public boolean ap() {
		return true;
	}

	@Override
	public YRectangle getAimBallInitArea() {
		return (YRectangle) PoolSetup.getProperty("AIM_BALL_INIT_AREA");
	}

	@Override
	public int getBallCount() {
		return 10;
	}

	@Override
	public YIPoint getInitPos(int i) {
		if (i >= rackPos.length)
			return new YIPoint(0, 0);
		return rackPos[i];
	}

	@Override
	public int getSlotIndex() {
		int i = pool.m_turn != 0 ? pool.type1 : pool.type0;
		if (i == 0)
			return -1;
		Vector<IBall> vector = pool.getBallInPlayArea();
		for (int j = 0; j < vector.size(); j++) {
			IBall _lcls124 = vector.elementAt(j);
			if (_lcls124.getType() == i)
				return -1;
		}

		int k = (int) Math.random() * 6 + 1;
		return k;
	}

	@Override
	public int getState() {
		super.turnChanged = false;
		return 0;
	}

	@Override
	public IBall getWhiteBall() {
		return whiteBall;
	}

	@Override
	public boolean isFaul() {
		if (pool.firstCollidedBall == null)
			return true;
		int i = pool.m_turn != 0 ? pool.type1 : pool.type0;
		if (i == 0)
			return false;
		return pool.firstCollidedBall.getType() != i;
	}

	@Override
	public boolean isWhiteBall(IBall _pcls124) {
		return true;
	}

	@Override
	public boolean nl() {
		return false;
	}

	@Override
	public boolean ql(IBall _pcls124) {
		return true;
	}

	@Override
	public boolean whiteBallPocketed() {
		return pool.turnPocketed.contains(whiteBall);
	}
}
