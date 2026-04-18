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
		YahooControl ghostControl = new YahooControl();
		YahooComponent obj = poolTable.cmbGhostStyle;
		ghostControl.addChildObject(obj, 10, 2, 0, 1, 1, 0, 0, 1, 0, 1, 0);
		YahooPannel pannel = new YahooPannel("ghost", ghostControl,
				table.table_side_tabcolor_bg, table.table_side_tabcolor_fg);
		container.addChildObject(pannel, 10, 1, 2, 1, 1, 0, ++counter[0], 1, 0,
				1, 0);
		YahooControl tableColorControl = new YahooControl();
		tableColorControl.addChildObject(poolTable.cmbTableColor, 10, 2, 0, 1, 1,
				0, 0, 1, 0, 1, 0);
		YahooPannel tablePannel = new YahooPannel("table", tableColorControl,
				table.table_side_tabcolor_bg, table.table_side_tabcolor_fg);
		container.addChildObject(tablePannel, 10, 1, 2, 1, 1, 0, ++counter[0], 1,
				0, 1, 0);
		poolTable.customTableColorPanel = poolTable.tableColorEditor;
		poolTable.tableColorEditor.visible = false;
		container.addChildObject(poolTable.tableColorEditor, 0, 0, true);
		poolTable.applyTableColorSelection();
		return result;
	}

	@Override
	public boolean eventActionEvent(Event event, Object obj) {
		if (event.target == poolTable.cmbGhostStyle)
			poolTable.applyGhostStyleSelection();
		else if (event.target == poolTable.cmbTableColor)
			poolTable.applyTableColorSelection();
		else if (event.target == table.chkSound)
			table.getApplet().Kg(table.chkSound.isChecked());
		else
			return super.eventActionEvent(event, obj);
		return true;
	}

	@Override
	public boolean eventMouseDown(Event event, int x, int y) {
		if (poolTable != null && poolTable.isCustomTableColorPanelVisible()
				&& event.target != poolTable.tableColorEditor
				&& event.target != poolTable.cmbTableColor)
			poolTable.setCustomTableColorPanelVisible(false);
		return super.eventMouseDown(event, x, y);
	}

}
