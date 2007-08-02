/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM. Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import org.lateralgm.components.IntegerField;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.BackgroundPreview;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Room;

public class RoomFrame extends ResourceFrame<Room>
	{
	private static final long serialVersionUID = 1L;
	private static ImageIcon frameIcon = Room.ICON[Room.ROOM];
	private static ImageIcon saveIcon = LGM.getIconForKey("RoomFrame.SAVE"); //$NON-NLS-1$

	JLabel statX;
	JLabel statY;
	JLabel statObj;
	JLabel statId;

	public JButton load;
	public JLabel width;
	public JLabel height;
	public JCheckBox transparent;
	public JButton edit;
	public JCheckBox smooth;
	public JCheckBox preload;
	public JCheckBox tileset;

	public JPanel side2;
	public IntegerField tWidth;
	public IntegerField tHeight;
	public IntegerField hOffset;
	public IntegerField vOffset;
	public IntegerField hSep;
	public IntegerField vSep;
	public BackgroundPreview preview;
	public boolean imageChanged = false;

	//TODO:
	public JPanel makeObjectsPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		return panel;
		}

	//TODO:
	public JPanel makeSettingsPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		return panel;
		}

	//TODO:
	public JPanel makeTilesPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		return panel;
		}

	//TODO:
	public JPanel makeBackgroundsPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		return panel;
		}

	//TODO:
	public JPanel makeViewsPane()
		{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		return panel;
		}

	public RoomFrame(Room res, ResNode node)
		{
		super(res,node);

		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		setMinimumSize(new Dimension(450,320));
		setSize(560,320);
		setFrameIcon(frameIcon);

		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.setMinimumSize(new Dimension(200,280));
		pane.setMaximumSize(new Dimension(200,Integer.MAX_VALUE));
		pane.setPreferredSize(new Dimension(200,280));

		JTabbedPane tabs = new JTabbedPane();
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.addTab(Messages.getString("RoomFrame.TAB_OBJECTS"),makeObjectsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_SETTINGS"),makeSettingsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_TILES"),makeTilesPane()); //$NON-NLS-1$
		String bks = Messages.getString("RoomFrame.TAB_BACKGROUNDS"); //$NON-NLS-1$
		tabs.addTab(bks,makeBackgroundsPane());
		tabs.addTab(Messages.getString("RoomFrame.TAB_VIEWS"),makeViewsPane()); //$NON-NLS-1$
		pane.add(tabs);

		save.setText(Messages.getString("RoomFrame.SAVE")); //$NON-NLS-1$
		save.setIcon(saveIcon);
//		addDim(pane,save,130,24);
		add(pane);

		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.setPreferredSize(new Dimension(240,280));

		pane.add(new JScrollPane());

		JPanel stat = new JPanel();
		stat.setMaximumSize(new Dimension(200,11));
		statX = new JLabel("x:");
		statX.setPreferredSize(new Dimension(25,14));
		stat.add(statX);
//		JToolBar.Separator sep = new JToolBar.Separator();
//		sep.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//		sep.setPreferredSize(new Dimension());
		JLabel l = new JLabel();
		l.setMinimumSize(new Dimension(5,5));
		l.setMaximumSize(new Dimension(5,5));
		stat.add(l);

		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
		sep.setMinimumSize(new Dimension(2,13));
		sep.setMaximumSize(new Dimension(2,13));
//		stat.add(sep);

		l = new JLabel();
		l.setMinimumSize(new Dimension(5,5));
		l.setMaximumSize(new Dimension(5,5));
		stat.add(l);
//		addDim(stat,sep,10,10);
//		stat.add(sep);

//		stat.addSeparator(new Dimension(10,10));
		statY = new JLabel("y:");
		statY.setPreferredSize(new Dimension(25,13));
		stat.add(statY);
//		stat.add(new JToolBar.Separator());
		statObj = new JLabel("object:");
		statObj.setPreferredSize(new Dimension(50,13));
		stat.add(statObj);
//		stat.add(new JToolBar.Separator());
		statId = new JLabel("id:");
		statId.setPreferredSize(new Dimension(50,13));
		stat.add(statId);

		pane.add(stat);
		add(pane);
		}

	//TODO:
	@Override
	public boolean resourceChanged()
		{
		return true;
		/*return !resOriginal.getName().equals(name.getText())
		 || resOriginal.transparent != transparent.isSelected()
		 || resOriginal.smoothEdges != smooth.isSelected()
		 || resOriginal.preload != preload.isSelected()
		 || resOriginal.useAsTileSet != tileset.isSelected()
		 || resOriginal.tileWidth != tWidth.getIntValue()
		 || resOriginal.tileHeight != tWidth.getIntValue()
		 || resOriginal.horizOffset != hOffset.getIntValue()
		 || resOriginal.vertOffset != vOffset.getIntValue()
		 || resOriginal.horizSep != hSep.getIntValue() || resOriginal.vertSep != vSep.getIntValue();*/
		}

	@Override
	public void revertResource()
		{
		LGM.currentFile.rooms.replace(res.getId(),resOriginal);
		}

	//TODO:
	@Override
	public void updateResource()
		{
		res.setName(name.getText());
		/*res.transparent = transparent.isSelected();
		 res.smoothEdges = smooth.isSelected();
		 res.preload = preload.isSelected();
		 res.useAsTileSet = tileset.isSelected();
		 res.tileWidth = tWidth.getIntValue();
		 res.tileHeight = tWidth.getIntValue();
		 res.horizOffset = hOffset.getIntValue();
		 res.vertOffset = vOffset.getIntValue();
		 res.horizSep = hSep.getIntValue();
		 res.vertSep = vSep.getIntValue();*/
		resOriginal = res.copy();
		}

	//TODO:
	public void actionPerformed(ActionEvent e)
		{

		super.actionPerformed(e);
		}
	}
