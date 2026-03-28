package y.po;

import java.awt.Event;

import y.controls.YahooComponent;
import y.controls.YahooControl;
import y.controls.YahooPannel;
import y.ycontrols.TableControlContainer;
import y.yutils.YahooGamesTable;

public class PoolControlContainer extends TableControlContainer {

	YahooPoolTable	poolTable;

	public PoolControlContainer(YahooGamesTable _pcls99) {
		super(_pcls99);
		poolTable = (YahooPoolTable) _pcls99;
	}

	@Override
	public void close() {
		poolTable = null;
	}

	@Override
	public boolean createControlPannel(YahooControl container, int[] counter) {
		boolean result = super.createControlPannel(container, counter);
		YahooControl cnt = new YahooControl();
		YahooComponent obj = poolTable.cmbGhostStyle;
		cnt.addChildObject(obj, 10, 2, 0, 1, 1, 0, 0, 1, 0, 1, 0);
		YahooPannel pannel = new YahooPannel("ghost", cnt,
				table.table_side_tabcolor_bg, table.table_side_tabcolor_fg);
		container.addChildObject(pannel, 10, 1, 2, 1, 1, 0, ++counter[0], 1, 0,
				1, 0);
		return result;
	}

	@Override
	public boolean eventActionEvent(Event event, Object obj) {
		if (event.target == poolTable.cmbGhostStyle)
			poolTable.applyGhostStyleSelection();
		else if (event.target == table.chkSound)
			table.getApplet().Kg(table.chkSound.isChecked());
		else
			return super.eventActionEvent(event, obj);
		return true;
	}

}
