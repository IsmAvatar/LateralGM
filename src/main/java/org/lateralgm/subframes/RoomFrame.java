/*
 * Copyright (C) 2007, 2008, 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, 2014 Robert B. Colton
 * Copyright (C) 2014, egofree
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.lateralgm.main.Util.deRef;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.NumberField;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.EditorScrollPane;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.RoomEditor;
import org.lateralgm.components.visual.RoomEditor.CommandHandler;
import org.lateralgm.components.visual.RoomEditor.PRoomEditor;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Room.Piece;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.View;
import org.lateralgm.resources.sub.View.PView;
import org.lateralgm.subframes.CodeFrame.CodeHolder;
import org.lateralgm.ui.swing.propertylink.ButtonModelLink;
import org.lateralgm.ui.swing.propertylink.DocumentLink;
import org.lateralgm.ui.swing.propertylink.FormattedLink;
import org.lateralgm.ui.swing.propertylink.PropertyLinkFactory;
import org.lateralgm.ui.swing.util.ArrayListModel;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.AddPieceInstance;
import org.lateralgm.util.ModifyPieceInstance;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;
import org.lateralgm.util.RemovePieceInstance;
import org.lateralgm.util.ShiftPieceInstances;

public class RoomFrame extends InstantiableResourceFrame<Room,PRoom> implements
		ListSelectionListener,CommandHandler,UpdateListener,FocusListener,ChangeListener
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon CODE_ICON = LGM.getIconForKey("RoomFrame.CODE"); //$NON-NLS-1$

	private final RoomEditor editor;
	private final EditorScrollPane editorPane;
	public final JTabbedPane tabs;
	public JLabel statX, statY, statId, statSrc;

	//ToolBar
	private JButton zoomIn, zoomOut, undo, redo, deleteInstances, shiftInstances, roomControls, fill,
			cut, copy, paste;
	private JToggleButton gridVis, gridIso, selectObject, selectRegion, snapToGrid, addOnTop,
			addMultiple;

	//Objects
	public JCheckBox oUnderlying, oLocked;
	private ButtonModelLink<PInstance> loLocked;
	public JList<Instance> oList;
	private Instance lastObj = null; //non-guaranteed copy of oList.getLastSelectedValue()
	private JButton addObjectButton, deleteObjectButton;
	public ResourceMenu<GmObject> oNew, oSource;
	private PropertyLink<PInstance,ResourceReference<GmObject>> loSource;
	private JTextField objectName;
	public NumberField objectHorizontalPosition, objectVerticalPosition, objectScaleX, objectScaleY,
			objectRotation, objectAlpha;
	public ColorSelect objectColor;
	public PropertyLink<PInstance,Color> loColour;
	private FormattedLink<PInstance> loX, loY, loScaleX, loScaleY, loRotation, loAlpha;
	private DocumentLink<PInstance> loName;
	private JButton oCreationCode;

	//Settings
	private JTextField sCaption;
	private JCheckBox sPersistent;
	private JButton sCreationCode, showButton;
	private JPopupMenu showMenu;
	public HashMap<CodeHolder,CodeFrame> codeFrames = new HashMap<CodeHolder,CodeFrame>();

	private JCheckBoxMenuItem sSObj, sSTile, sSBack, sSFore, sSView;
	//Tiles
	public JComboBox<Integer> tileLayer;
	// List of tiles layers in the current room
	Vector<Integer> layers = new Vector<Integer>();
	private JButton addLayer, deleteLayer, changeLayer;
	public JCheckBox tUnderlying, tLocked, tHideOtherLayers, tEditOtherLayers;
	private ButtonModelLink<PTile> ltLocked;
	public TileSelector tSelect;
	private JScrollPane tScroll;
	public JList<Tile> tList;
	private Tile lastTile = null; //non-guaranteed copy of tList.getLastSelectedValue()
	private JButton deleteTileButton;
	public ResourceMenu<Background> taSource, teSource;
	private PropertyLink<PTile,ResourceReference<Background>> ltSource;
	public NumberField tsX, tsY, tileHorizontalPosition, tileVerticalPosition, teDepth;
	private FormattedLink<PTile> ltsX, ltsY, ltX, ltY, ltDepth;

	//Backgrounds
	private JCheckBox bDrawColor, bVisible, bForeground, bTileH, bTileV, bStretch;
	private ButtonModelLink<PBackgroundDef> lbVisible, lbForeground, lbTileH, lbTileV, lbStretch;
	private ColorSelect bColor;
	private JList<JLabel> bList;
	/**Guaranteed valid version of bList.getLastSelectedIndex()*/
	private int lastValidBack = -1;
	private ResourceMenu<Background> bSource;
	private PropertyLink<PBackgroundDef,ResourceReference<Background>> lbSource;
	private NumberField bX, bY, bH, bV;
	private FormattedLink<PBackgroundDef> lbX, lbY, lbH, lbV;
	private final BgDefPropertyListener bdpl = new BgDefPropertyListener();
	//Views
	private JCheckBox vEnabled, vVisible;
	private ButtonModelLink<PView> lvVisible;
	private JList<JLabel> vList;
	/**Guaranteed valid version of vList.getLastSelectedIndex()*/
	private int lastValidView = -1;
	private NumberField vRX, vRY, vRW, vRH;
	private NumberField vPX, vPY, vPW, vPH;
	private FormattedLink<PView> lvRX, lvRY, lvRW, lvRH, lvPX, lvPY, lvPW, lvPH;
	private ResourceMenu<GmObject> vObj;
	private PropertyLink<PView,ResourceReference<GmObject>> lvObj;
	private NumberField vOHBor, vOVBor, vOHSp, vOVSp;
	private FormattedLink<PView> lvOHBor, lvOVBor, lvOHSp, lvOVSp;
	private final ViewPropertyListener vpl = new ViewPropertyListener();

	private final PropertyLinkFactory<PRoomEditor> prelf;
	private JCheckBox vClear;

	// Undo system elements
	public UndoManager undoManager;
	public UndoableEditSupport undoSupport;
	// Save the object's properties for the undo
	private String pieceOriginalName = null;
	private Point pieceOriginalPosition = null;
	private Point2D pieceOriginalScale = null;
	private Double pieceOriginalRotation = null;
	private Integer pieceOriginalAlpha = null;
	// Used to record the select piece before losing the focus.
	public Piece selectedPiece = null;

	public RoomEditor getRoomEditor()
		{
		return editor;
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.add(save);
		tool.addSeparator();

		// Action fired when the delete instances button is clicked
		Action deleteAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					deleteAction();
					}
			};

		// Action fired when the cut is clicked
		Action cutAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					if (tabs.getSelectedIndex() == Room.TAB_TILES)
						editor.copySelectionTiles();
					else
						editor.copySelectionInstances();

					if (editor.selection != null) deleteAction(false);
					}
			};

		cut = new JButton(LGM.getIconForKey("RoomFrame.CUT"));
		cut.setToolTipText(Messages.getString("RoomFrame.CUT"));
		// Bind the ctrl X keystroke with the cut button
		KeyStroke ctrlXKey = KeyStroke.getKeyStroke(Messages.getKeyboardString("RoomFrame.CUT"));
		cut.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlXKey,"cut");
		cut.getActionMap().put("cut",cutAction);
		cut.addActionListener(cutAction);
		tool.add(cut);

		// Action fired when the copy button is clicked
		Action copyAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					if (tabs.getSelectedIndex() == Room.TAB_TILES)
						editor.copySelectionTiles();
					else
						editor.copySelectionInstances();
					}
			};

		copy = new JButton(LGM.getIconForKey("RoomFrame.COPY"));
		copy.setToolTipText(Messages.getString("RoomFrame.COPY"));
		// Bind the ctrl C keystroke with the copy button
		KeyStroke ctrlCKey = KeyStroke.getKeyStroke(Messages.getKeyboardString("RoomFrame.COPY"));
		copy.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlCKey,"copy");
		copy.getActionMap().put("copy",copyAction);
		copy.addActionListener(copyAction);
		tool.add(copy);

		// Action fired when the paste button is clicked
		Action pasteAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					editor.activatePasteMode();
					}
			};

		paste = new JButton(LGM.getIconForKey("RoomFrame.PASTE"));
		paste.setToolTipText(Messages.getString("RoomFrame.PASTE"));
		// Bind the ctrl V keystroke with the paste button
		KeyStroke ctrlVKey = KeyStroke.getKeyStroke(Messages.getKeyboardString("RoomFrame.PASTE"));
		paste.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlVKey,"paste");
		paste.getActionMap().put("paste",pasteAction);
		paste.addActionListener(pasteAction);
		tool.add(paste);
		tool.addSeparator();

		// Action fired when the undo button is clicked
		Action undoAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					Piece selectedPiece = editor.getSelectedPiece();

					// If there is a selected piece, deselect it
					if (selectedPiece != null) selectedPiece.setSelected(false);

					undoManager.undo();
					refreshUndoRedoButtons();
					}
			};

		undo = new JButton(LGM.getIconForKey("RoomFrame.UNDO"));
		undo.setToolTipText(Messages.getString("RoomFrame.UNDO"));
		// Bind the undo keystroke with the undo button
		KeyStroke undoKey = KeyStroke.getKeyStroke(Messages.getKeyboardString("RoomFrame.UNDO"));
		undo.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(undoKey,"ctrlZ");
		undo.getActionMap().put("ctrlZ",undoAction);
		undo.addActionListener(undoAction);
		tool.add(undo);

		// Action fired when the redo button is clicked
		Action redoAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					undoManager.redo();
					refreshUndoRedoButtons();
					}
			};

		redo = new JButton(LGM.getIconForKey("RoomFrame.REDO"));
		redo.setToolTipText(Messages.getString("RoomFrame.REDO"));
		// Bind the redo keystroke with the undo button
		KeyStroke redoKey = KeyStroke.getKeyStroke(Messages.getKeyboardString("RoomFrame.REDO"));
		redo.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(redoKey,"ctrlY");
		redo.getActionMap().put("ctrlY",redoAction);
		redo.addActionListener(redoAction);
		tool.add(redo);
		tool.addSeparator();

		// if the select object button has been clicked, deactivate the selection region button
		Action selectObjectAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					editor.deactivatePasteMode();

					if (selectObject.isSelected())
						editor.deactivateSelectRegionMode();
					else
						editor.activateSelectRegionMode();
					}
			};

		selectObject = new JToggleButton(LGM.getIconForKey("RoomFrame.SELECT_OBJECT"));
		selectObject.setToolTipText(Messages.getString("RoomFrame.SELECT_OBJECT"));
		selectObject.addActionListener(selectObjectAction);
		prelf.make(selectObject,PRoomEditor.SINGLE_SELECTION);
		tool.add(selectObject);

		// if the select region button has been clicked, deactivate the selection object button
		Action selectRegionAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					editor.deactivatePasteMode();

					if (selectRegion.isSelected())
						editor.deactivateSelectObjectMode();
					else
						editor.activateSelectObjectMode();
					}
			};

		selectRegion = new JToggleButton(LGM.getIconForKey("RoomFrame.SELECT_REGION"));
		selectRegion.setToolTipText(Messages.getString("RoomFrame.SELECT_REGION"));
		selectRegion.addActionListener(selectRegionAction);
		prelf.make(selectRegion,PRoomEditor.MULTI_SELECTION);
		tool.add(selectRegion);

		fill = new JButton(LGM.getIconForKey("RoomFrame.FILL"));
		fill.setToolTipText(Messages.getString("RoomFrame.FILL"));
		fill.addActionListener(this);
		tool.add(fill);
		tool.addSeparator();

		deleteInstances = new JButton(LGM.getIconForKey("RoomFrame.DELETE"));
		deleteInstances.setToolTipText(Messages.getString("RoomFrame.DELETE"));
		// Bind the delete keystroke with the delete button
		KeyStroke deleteKey = KeyStroke.getKeyStroke(Messages.getKeyboardString("RoomFrame.DELETE"));
		deleteInstances.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(deleteKey,"delete");
		deleteInstances.getActionMap().put("delete",deleteAction);
		deleteInstances.addActionListener(deleteAction);
		tool.add(deleteInstances);

		shiftInstances = new JButton(LGM.getIconForKey("RoomFrame.SHIFT"));
		shiftInstances.setToolTipText(Messages.getString("RoomFrame.SHIFT"));
		shiftInstances.addActionListener(this);
		tool.add(shiftInstances);
		tool.addSeparator();

		// if the alt key has been pressed
		Action altKeyPressedAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					// If bouton was clicked with the mouse
					if (actionEvent.getActionCommand() != null) return;
					editor.altKeyPressed();
					}
			};

		// if the alt key has been released
		Action altKeyReleasedAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					// If bouton was clicked with the mouse
					if (actionEvent.getActionCommand() != null) return;
					editor.altKeyReleased();
					}
			};

		snapToGrid = new JToggleButton(LGM.getIconForKey("RoomFrame.SNAP_TO_GRID"));
		snapToGrid.setToolTipText(Messages.getString("RoomFrame.SNAP_TO_GRID"));
		snapToGrid.setActionCommand("snapToGrid");
		// Link the alt key 'pressed'
		KeyStroke altKeyPressed = KeyStroke.getKeyStroke(KeyEvent.VK_ALT,InputEvent.ALT_DOWN_MASK,false);
		snapToGrid.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(altKeyPressed,"altKeyPressed");
		snapToGrid.getActionMap().put("altKeyPressed",altKeyPressedAction);
		snapToGrid.addActionListener(altKeyPressedAction);
		// Link the alt key 'released'
		KeyStroke altKeyReleased = KeyStroke.getKeyStroke(KeyEvent.VK_ALT,0,true);
		snapToGrid.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(altKeyReleased,"altKeyReleased");
		snapToGrid.getActionMap().put("altKeyReleased",altKeyReleasedAction);
		snapToGrid.addActionListener(altKeyReleasedAction);
		prelf.make(snapToGrid,PRoomEditor.SNAP_TO_GRID);
		tool.add(snapToGrid);

		// if the ctrl key has been pressed
		Action ctrlKeyPressedAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					// If bouton was clicked with the mouse
					if (actionEvent.getActionCommand() != null) return;
					editor.ctrlKeyPressed();
					}
			};

		// if the ctrl key has been released
		Action ctrlKeyReleasedAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					// If bouton was clicked with the mouse
					if (actionEvent.getActionCommand() != null) return;
					editor.ctrlKeyReleased();
					}
			};

		addOnTop = new JToggleButton(LGM.getIconForKey("RoomFrame.ADD_ON_TOP"));
		addOnTop.setToolTipText(Messages.getString("RoomFrame.ADD_ON_TOP"));
		addOnTop.setActionCommand("addOnTop");
		// Link the ctrl key 'pressed'
		KeyStroke ctrlKeyPressed = KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,
				InputEvent.CTRL_DOWN_MASK,false);
		addOnTop.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlKeyPressed,"ctrlKeyPressed");
		addOnTop.getActionMap().put("ctrlKeyPressed",ctrlKeyPressedAction);
		addOnTop.addActionListener(ctrlKeyPressedAction);
		// Link the ctrl key 'released'
		KeyStroke ctrlKeyReleased = KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,0,true);
		addOnTop.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlKeyReleased,"ctrlKeyReleased");
		addOnTop.getActionMap().put("ctrlKeyReleased",ctrlKeyReleasedAction);
		addOnTop.addActionListener(ctrlKeyReleasedAction);
		prelf.make(addOnTop,PRoomEditor.ADD_ON_TOP);
		tool.add(addOnTop);

		// if the shift key has been pressed
		Action shiftKeyPressedAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					// If bouton was clicked with the mouse
					if (actionEvent.getActionCommand() != null) return;
					editor.shiftKeyPressed();
					}
			};

		// if the shift key has been released
		Action shiftKeyReleasedAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
					{
					// If bouton was clicked with the mouse
					if (actionEvent.getActionCommand() != null) return;
					editor.shiftKeyReleased();
					}
			};

		addMultiple = new JToggleButton(LGM.getIconForKey("RoomFrame.ADD_MULTIPLE"));
		addMultiple.setToolTipText(Messages.getString("RoomFrame.ADD_MULTIPLE"));
		addMultiple.setActionCommand("addMultiple");
		// Link the shift key 'pressed'
		KeyStroke shiftKeyPressed = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,
				InputEvent.SHIFT_DOWN_MASK,false);
		addMultiple.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(shiftKeyPressed,"shiftKeyPressed");
		addMultiple.getActionMap().put("shiftKeyPressed",shiftKeyPressedAction);
		addMultiple.addActionListener(shiftKeyPressedAction);
		// Link the shift key 'released'
		KeyStroke shiftKeyReleased = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,0,true);
		addMultiple.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(shiftKeyReleased,"shiftKeyReleased");
		addMultiple.getActionMap().put("shiftKeyReleased",shiftKeyReleasedAction);
		addMultiple.addActionListener(shiftKeyReleasedAction);
		prelf.make(addMultiple,PRoomEditor.ADD_MULTIPLE);
		tool.add(addMultiple);
		tool.addSeparator();

		zoomIn = new JButton(LGM.getIconForKey("RoomFrame.ZOOM_IN")); //$NON-NLS-1$
		zoomIn.setToolTipText(Messages.getString("RoomFrame.ZOOM_IN"));
		prelf.make(zoomIn,PRoomEditor.ZOOM,1,RoomEditor.ZOOM_MAX);
		tool.add(zoomIn);
		zoomOut = new JButton(LGM.getIconForKey("RoomFrame.ZOOM_OUT")); //$NON-NLS-1$
		zoomOut.setToolTipText(Messages.getString("RoomFrame.ZOOM_OUT"));
		prelf.make(zoomOut,PRoomEditor.ZOOM,-1,RoomEditor.ZOOM_MIN);
		tool.add(zoomOut);
		tool.addSeparator();

		gridVis = new JToggleButton(LGM.getIconForKey("RoomFrame.GRID_VISIBLE"));
		gridVis.setToolTipText(Messages.getString("RoomFrame.GRID_VISIBLE"));
		prelf.make(gridVis,PRoomEditor.SHOW_GRID);
		tool.add(gridVis);
		gridIso = new JToggleButton(LGM.getIconForKey("RoomFrame.GRID_ISOMETRIC"));
		gridIso.setToolTipText(Messages.getString("RoomFrame.GRID_ISOMETRIC"));
		plf.make(gridIso,PRoom.ISOMETRIC);
		tool.add(gridIso);
		//tool.addSeparator();

		// Add the grid sizers
		JLabel lab = new JLabel(Messages.getString("RoomFrame.GRID_X")); //$NON-NLS-1$
		NumberField nf = new NumberField(0,999);
		nf.setMaximumSize(nf.getPreferredSize());
		prelf.make(nf,PRoomEditor.GRID_OFFSET_X);
		tool.add(lab);
		tool.add(nf);

		lab = new JLabel(Messages.getString("RoomFrame.GRID_Y")); //$NON-NLS-1$
		nf = new NumberField(0,999);
		nf.setMaximumSize(nf.getPreferredSize());
		prelf.make(nf,PRoomEditor.GRID_OFFSET_Y);
		tool.add(lab);
		tool.add(nf);

		lab = new JLabel(Messages.getString("RoomFrame.GRID_W")); //$NON-NLS-1$
		nf = new NumberField(1,999);
		nf.setMaximumSize(nf.getPreferredSize());
		plf.make(nf,PRoom.SNAP_X);
		tool.add(lab);
		tool.add(nf);

		lab = new JLabel(Messages.getString("RoomFrame.GRID_H")); //$NON-NLS-1$
		nf = new NumberField(1,999);
		nf.setMaximumSize(nf.getPreferredSize());
		plf.make(nf,PRoom.SNAP_Y);
		tool.add(lab);
		tool.add(nf);

		tool.addSeparator();
		showMenu = makeShowMenu();
		showButton = new JButton(Messages.getString("RoomFrame.SHOW")); //$NON-NLS-1$
		showButton.addActionListener(this);
		tool.add(showButton);
		tool.addSeparator();

		roomControls = new JButton(LGM.getIconForKey("RoomFrame.ROOM_CONTROLS"));
		roomControls.setToolTipText(Messages.getString("RoomFrame.ROOM_CONTROLS"));
		roomControls.addActionListener(this);
		tool.add(roomControls);

		return tool;
		}

	private static class ObjectListComponentRenderer implements ListCellRenderer<Instance>
		{
		private final JLabel lab = new JLabel();
		private final ListComponentRenderer lcr = new ListComponentRenderer();

		public ObjectListComponentRenderer()
			{
			lab.setOpaque(true);
			}

		public Component getListCellRendererComponent(JList<? extends Instance> list, Instance val,
				int ind, boolean selected, boolean focus)
			{
			Instance i = val;
			ResourceReference<GmObject> ro = i.properties.get(PInstance.OBJECT);
			GmObject o = deRef(ro);
			String name = o == null ? Messages.getString("RoomFrame.NO_OBJECT") : o.getName();
			lcr.getListCellRendererComponent(list,lab,ind,selected,focus);
			lab.setText(name + "  " + i.properties.get(PInstance.ID) + "  " +
				i.properties.get(PInstance.NAME));
			lab.setText(String.format("%10s %6s %s", name, i.properties.get(PInstance.ID),
				i.properties.get(PInstance.NAME)));
			ResNode rn = o == null ? null : o.getNode();
			lab.setIcon(rn == null ? null : rn.getIcon());
			return lab;
			}
		}

	private static class TileListComponentRenderer implements ListCellRenderer<Tile>
		{
		private final JLabel lab = new JLabel();
		private final TileIcon ti = new TileIcon();
		private final ListComponentRenderer lcr = new ListComponentRenderer();

		public TileListComponentRenderer()
			{
			lab.setOpaque(true);
			lab.setIcon(ti);
			}

		public Component getListCellRendererComponent(JList<? extends Tile> list, Tile val, int ind,
				boolean selected, boolean focus)
			{
			Tile t = val;
			ResourceReference<Background> rb = t.properties.get(PTile.BACKGROUND);
			Background bg = deRef(rb);
			String name = bg == null ? Messages.getString("RoomFrame.NO_BACKGROUND") : bg.getName();
			lab.setText(name + " " + t.properties.get(PTile.ID) + " " + t.properties.get(PTile.NAME));
			ti.tile = t;
			lcr.getListCellRendererComponent(list,lab,ind,selected,focus);
			return lab;
			}

		static class TileIcon implements Icon
			{
			Tile tile;

			public int getIconHeight()
				{
				return tile.getSize().height;
				}

			public int getIconWidth()
				{
				return tile.getSize().width;
				}

			public void paintIcon(Component c, Graphics g, int x, int y)
				{
				ResourceReference<Background> rb = tile.properties.get(PTile.BACKGROUND);
				Background bg = deRef(rb);
				BufferedImage bi = bg == null ? null : bg.getBackgroundImage();
				if (bi != null)
					{
					Point p = tile.getBackgroundPosition();
					Dimension d = tile.getSize();
					g.drawImage(bi,0,0,d.width,d.height,p.x,p.y,p.x + d.width,p.y + d.height,c);
					}
				}
			}
		}

	public static final DataFlavor INSTANCE_FLAVOR = new DataFlavor(Instance.class,"Instance"); //$NON-NLS-1$

	public static class ObjectListTransferable implements Transferable
		{
		private static final DataFlavor[] FLAVORS = { INSTANCE_FLAVOR };
		private final List<Instance> instanceList;

		public ObjectListTransferable(List<Instance> list)
			{
			instanceList = list;
			}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
			{
			if (flavor == INSTANCE_FLAVOR)
				{
				return instanceList;
				}
			throw new UnsupportedFlavorException(flavor);
			}

		public DataFlavor[] getTransferDataFlavors()
			{
			return FLAVORS;
			}

		public boolean isDataFlavorSupported(DataFlavor flavor)
			{
			return flavor == INSTANCE_FLAVOR;
			}
		}

	public class ObjectListTransferHandler extends TransferHandler
		{
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = -5228856790388285912L;
		private int[] indices = null;
		private ArrayList<Instance> instanceList = null;
		private int addIndex = -1; //Location where items were added
		private int addCount = 0; //Number of items added.

		public ObjectListTransferHandler(ActiveArrayList<Instance> insts)
			{
			instanceList = insts;
			}

		public boolean canImport(TransferHandler.TransferSupport info)
			{
			return info.isDataFlavorSupported(INSTANCE_FLAVOR);
			}

		protected Transferable createTransferable(JComponent c)
			{
			JList<Instance> list = (JList<Instance>) c;
			indices = list.getSelectedIndices();
			return new ObjectListTransferable(list.getSelectedValuesList());
			}

		/**
		 * We support both copy and move actions.
		 */
		public int getSourceActions(JComponent c)
			{
			return TransferHandler.COPY_OR_MOVE;
			}

		/**
		 * Perform the actual import.  This demo only supports drag and drop.
		 */
		public boolean importData(TransferHandler.TransferSupport info)
			{
			if (!info.isDrop())
				{
				return false;
				}

			JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
			int index = dl.getIndex();
			boolean insert = dl.isInsert();

			// Get the instance that is being dropped.
			Transferable t = info.getTransferable();
			List<Instance> data = null;

			try
				{
				data = (List<Instance>) t.getTransferData(INSTANCE_FLAVOR);
				}
			catch (Exception e)
				{
				LGM.showDefaultExceptionHandler(e);
				}

			addIndex = index;
			addCount = data.size();

			// Perform the actual import.
			for (int i = 0; i < addCount; i++)
				{
				instanceList.add(index++,data.get(i));
				}
			return true;
			}

		/**
		 * Remove the items moved from the list.
		 */
		protected void exportDone(JComponent c, Transferable data, int action)
			{
			JList<Instance> source = (JList<Instance>) c;

			if (action == TransferHandler.MOVE)
				{
				for (int i = indices.length - 1; i >= 0; i--)
					{
					instanceList.remove(indices[i]);
					}
				}

			indices = null;
			addCount = 0;
			addIndex = -1;
			}
		}

	public JPanel makeObjectsPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		oNew = new ResourceMenu<GmObject>(GmObject.class,
				Messages.getString("RoomFrame.NO_OBJECT"),true,110); //$NON-NLS-1$
		oNew.addActionListener(this);
		oUnderlying = new JCheckBox(Messages.getString("RoomFrame.OBJ_UNDERLYING")); //$NON-NLS-1$
		prelf.make(oUnderlying,PRoomEditor.DELETE_UNDERLYING_OBJECTS);

		MouseAdapter mouseListenerForInstances = new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
					{
					// Display the selected instance with a border and centered in the editor window
					showSelectedInstance();
					}
			};

		oList = new JList<Instance>(new ArrayListModel<Instance>(res.instances));
		oList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		oList.setVisibleRowCount(8);
		oList.setCellRenderer(new ObjectListComponentRenderer());
		oList.setSelectedIndex(0);
		oList.addListSelectionListener(this);
		oList.addMouseListener(mouseListenerForInstances);
		//TODO: Finish
		//oList.setDragEnabled(true);
		//oList.setDropMode(DropMode.ON_OR_INSERT);
		//oList.setTransferHandler(new ObjectListTransferHandler(res.instances));
		JScrollPane sp = new JScrollPane(oList);
		addObjectButton = new JButton(Messages.getString("RoomFrame.OBJ_ADD")); //$NON-NLS-1$
		addObjectButton.addActionListener(this);
		deleteObjectButton = new JButton(Messages.getString("RoomFrame.OBJ_DELETE")); //$NON-NLS-1$
		deleteObjectButton.addActionListener(this);

		JPanel edit = new JPanel();
		String title = Messages.getString("RoomFrame.OBJ_INSTANCES"); //$NON-NLS-1$
		edit.setBorder(BorderFactory.createTitledBorder(title));
		GroupLayout layout2 = new GroupLayout(edit);
		layout2.setAutoCreateGaps(true);
		layout2.setAutoCreateContainerGaps(true);
		edit.setLayout(layout2);

		oSource = new ResourceMenu<GmObject>(GmObject.class,
			Messages.getString("RoomFrame.NO_OBJECT"),true,110); //$NON-NLS-1$

		oLocked = new JCheckBox(Messages.getString("RoomFrame.OBJ_LOCKED")); //$NON-NLS-1$
		JLabel lObjName = new JLabel(Messages.getString("RoomFrame.OBJ_NAME")); //$NON-NLS-1$
		objectName = new JTextField();
		objectName.addFocusListener(this);
		JLabel lObjX = new JLabel(Messages.getString("RoomFrame.OBJ_X")); //$NON-NLS-1$
		objectHorizontalPosition = new NumberField(-99999,99999);
		objectHorizontalPosition.setColumns(4);
		objectHorizontalPosition.addFocusListener(this);
		JLabel lObjY = new JLabel(Messages.getString("RoomFrame.OBJ_Y")); //$NON-NLS-1$
		objectVerticalPosition = new NumberField(-99999,99999);
		objectVerticalPosition.setColumns(4);
		objectVerticalPosition.addFocusListener(this);
		JLabel lObjScaleX = new JLabel(Messages.getString("RoomFrame.SCALE_X")); //$NON-NLS-1$
		objectScaleX = new NumberField(0.1,9999.0,1.0,2);
		objectScaleX.addFocusListener(this);
		JLabel lObjScaleY = new JLabel(Messages.getString("RoomFrame.SCALE_Y")); //$NON-NLS-1$
		objectScaleY = new NumberField(0.1,9999.0,1.0,2);
		objectScaleY.addFocusListener(this);
		JLabel lObjRotation = new JLabel(Messages.getString("RoomFrame.ROTATION")); //$NON-NLS-1$
		objectRotation = new NumberField(0.0,360.0,0.0,2);
		objectRotation.addFocusListener(this);
		JLabel lObjColour = new JLabel(Messages.getString("RoomFrame.COLOUR")); //$NON-NLS-1$
		objectColor = new ColorSelect(Color.WHITE,false);
		objectColor.addItemListener(new ItemListener() {
		Color originalColor = objectColor.getSelectedColor();
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			// If no object is selected, return
			int selectedIndex = oList.getSelectedIndex();
			if (selectedIndex == -1) return;

			// Save the selected instance
			selectedPiece = oList.getSelectedValue();
			if (selectedPiece == null) return;

			Color newColour = objectColor.getSelectedColor();

			// If the color has changed, save it in the undo
			if (!originalColor.equals(newColour))
				{
				// Record the effect of modifying the color of an object for the undo
				UndoableEdit edit = new ModifyPieceInstance(RoomFrame.this,selectedPiece,originalColor,
						newColour);
				// notify the listeners
				undoSupport.postEdit(edit);
				originalColor = objectColor.getSelectedColor();
				}
		}

		});

		JLabel lObjAlpha = new JLabel(Messages.getString("RoomFrame.ALPHA")); //$NON-NLS-1$
		objectAlpha = new NumberField(0,255,255);
		objectAlpha.addFocusListener(this);
		oCreationCode = new JButton(Messages.getString("RoomFrame.OBJ_CODE")); //$NON-NLS-1$
		oCreationCode.setIcon(CODE_ICON);
		oCreationCode.addActionListener(this);

		layout2.setHorizontalGroup(layout2.createParallelGroup()
		/**/.addComponent(oSource)
		/**/.addGroup(layout2.createSequentialGroup()
		/*	*/.addGroup(layout2.createParallelGroup(Alignment.TRAILING)
		/*		*/.addComponent(lObjName)
		/*		*/.addComponent(lObjX)
		/*		*/.addComponent(lObjScaleX)
		/*		*/.addComponent(lObjRotation)
		/*		*/.addComponent(lObjColour))
		/*	*/.addGroup(layout2.createParallelGroup()
		/*		*/.addComponent(oLocked)
		/*		*/.addComponent(objectName)
		/*		*/.addGroup(layout2.createSequentialGroup()
		/*			*/.addGroup(layout2.createParallelGroup()
		/*				*/.addComponent(objectHorizontalPosition)
		/*				*/.addComponent(objectScaleX)
		/*				*/.addComponent(objectRotation))
		/*			*/.addGroup(layout2.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(lObjY)
		/*				*/.addComponent(lObjScaleY)
		/*				*/.addComponent(lObjAlpha))
		/*			*/.addGroup(layout2.createParallelGroup()
		/*				*/.addComponent(objectVerticalPosition)
		/*				*/.addComponent(objectScaleY)
		/*				*/.addComponent(objectAlpha)))
		/*		*/.addGroup(layout2.createSequentialGroup()
		/*			*/.addComponent(objectColor))))
		/**/.addComponent(oCreationCode,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE));

		layout2.setVerticalGroup(layout2.createSequentialGroup()
		/**/.addComponent(oSource)
		/**/.addComponent(oLocked)
		/**/.addGroup(layout2.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(lObjName)
		/*	*/.addComponent(objectName))
		/**/.addGroup(layout2.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(lObjX)
		/*	*/.addComponent(objectHorizontalPosition)
		/*	*/.addComponent(lObjY)
		/*	*/.addComponent(objectVerticalPosition))
		/**/.addGroup(layout2.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(lObjScaleX)
		/*	*/.addComponent(objectScaleX)
		/*	*/.addComponent(lObjScaleY)
		/*	*/.addComponent(objectScaleY))
		/**/.addGroup(layout2.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(lObjRotation)
		/*	*/.addComponent(objectRotation)
		/*	*/.addComponent(lObjAlpha)
		/*	*/.addComponent(objectAlpha))
		/**/.addGroup(layout2.createParallelGroup(Alignment.CENTER)
		/*	*/.addComponent(lObjColour)
		/*	*/.addComponent(objectColor))
		/**/.addComponent(oCreationCode));

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(oNew)
		/**/.addComponent(oUnderlying)
		/**/.addComponent(sp,DEFAULT_SIZE,120,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(addObjectButton,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*		*/.addComponent(deleteObjectButton,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
		/**/.addComponent(edit));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(oNew)
		/**/.addComponent(oUnderlying)
		/**/.addComponent(sp)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(addObjectButton)
		/*		*/.addComponent(deleteObjectButton))
		/**/.addComponent(edit,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE));

		// Make sure the selected object in the list is activated
		fireObjUpdate();
		return panel;

		}

	private JPopupMenu makeShowMenu()
		{
		JPopupMenu showMenu = new JPopupMenu();
		String st = Messages.getString("RoomFrame.SHOW_OBJECTS"); //$NON-NLS-1$
		sSObj = new JCheckBoxMenuItem(st);
		prelf.make(sSObj,PRoomEditor.SHOW_OBJECTS);
		showMenu.add(sSObj);
		st = Messages.getString("RoomFrame.SHOW_TILES"); //$NON-NLS-1$
		sSTile = new JCheckBoxMenuItem(st);
		prelf.make(sSTile,PRoomEditor.SHOW_TILES);
		showMenu.add(sSTile);
		st = Messages.getString("RoomFrame.SHOW_BACKGROUNDS"); //$NON-NLS-1$
		sSBack = new JCheckBoxMenuItem(st);
		prelf.make(sSBack,PRoomEditor.SHOW_BACKGROUNDS);
		showMenu.add(sSBack);
		st = Messages.getString("RoomFrame.SHOW_FOREGROUNDS"); //$NON-NLS-1$
		sSFore = new JCheckBoxMenuItem(st);
		prelf.make(sSFore,PRoomEditor.SHOW_FOREGROUNDS);
		showMenu.add(sSFore);
		st = Messages.getString("RoomFrame.SHOW_VIEWS"); //$NON-NLS-1$
		sSView = new JCheckBoxMenuItem(st);
		prelf.make(sSView,PRoomEditor.SHOW_VIEWS);
		showMenu.add(sSView);
		return showMenu;
		}

	public JPanel makeSettingsPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		JLabel lName = new JLabel(Messages.getString("RoomFrame.NAME")); //$NON-NLS-1$

		JLabel lCaption = new JLabel(Messages.getString("RoomFrame.CAPTION")); //$NON-NLS-1$
		sCaption = new JTextField();
		plf.make(sCaption.getDocument(),PRoom.CAPTION);

		JLabel lWidth = new JLabel(Messages.getString("RoomFrame.WIDTH")); //$NON-NLS-1$
		NumberField sWidth = new NumberField(1,999999);
		plf.make(sWidth,PRoom.WIDTH);

		JLabel lHeight = new JLabel(Messages.getString("RoomFrame.HEIGHT")); //$NON-NLS-1$
		NumberField sHeight = new NumberField(1,999999);
		plf.make(sHeight,PRoom.HEIGHT);

		JLabel lSpeed = new JLabel(Messages.getString("RoomFrame.SPEED")); //$NON-NLS-1$
		NumberField sSpeed = new NumberField(1,9999);
		plf.make(sSpeed,PRoom.SPEED);

		String str = Messages.getString("RoomFrame.PERSISTENT"); //$NON-NLS-1$
		sPersistent = new JCheckBox(str);
		plf.make(sPersistent,PRoom.PERSISTENT);

		str = Messages.getString("RoomFrame.CREATION_CODE"); //$NON-NLS-1$
		sCreationCode = new JButton(str,CODE_ICON);
		sCreationCode.addActionListener(this);

		JPanel pg = makeGridPane();

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lName)
		/*		*/.addComponent(name,DEFAULT_SIZE,120,MAX_VALUE))
		/**/.addComponent(lCaption)
		/**/.addComponent(sCaption,DEFAULT_SIZE,120,MAX_VALUE)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(lWidth)
		/*				*/.addComponent(lHeight)
		/*				*/.addComponent(lSpeed))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(sWidth)
		/*				*/.addComponent(sHeight)
		/*				*/.addComponent(sSpeed)))
		/**/.addComponent(sPersistent)
		/**/.addComponent(sCreationCode,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(pg));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lName)
		/*		*/.addComponent(name))
		/**/.addComponent(lCaption)
		/**/.addComponent(sCaption,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lWidth)
		/*		*/.addComponent(sWidth))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lHeight)
		/*		*/.addComponent(sHeight))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lSpeed)
		/*		*/.addComponent(sSpeed))
		/**/.addComponent(sPersistent)
		/**/.addComponent(sCreationCode)
		/**/.addComponent(pg));
		return panel;
		}

	public JPanel makeGridPane()
		{
		JPanel pg = new JPanel();
		GroupLayout lr = new GroupLayout(pg);
		pg.setLayout(lr);
		pg.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoomFrame.GRID"))); //$NON-NLS-1$

		JLabel lGX = new JLabel(Messages.getString("RoomFrame.GRID_X")); //$NON-NLS-1$
		NumberField sGX = new NumberField(0,999);
		prelf.make(sGX,PRoomEditor.GRID_OFFSET_X);
		JLabel lGY = new JLabel(Messages.getString("RoomFrame.GRID_Y")); //$NON-NLS-1$
		NumberField sGY = new NumberField(0,999);
		prelf.make(sGY,PRoomEditor.GRID_OFFSET_Y);
		JLabel lGW = new JLabel(Messages.getString("RoomFrame.GRID_W")); //$NON-NLS-1$
		NumberField sGW = new NumberField(1,999);
		plf.make(sGW,PRoom.SNAP_X);
		JLabel lGH = new JLabel(Messages.getString("RoomFrame.GRID_H")); //$NON-NLS-1$
		NumberField sGH = new NumberField(1,999);
		plf.make(sGH,PRoom.SNAP_Y);
		lr.setHorizontalGroup(lr.createSequentialGroup().addContainerGap()
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(lGX)
		/*		*/.addComponent(lGY)).addGap(4)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(sGX)
		/*		*/.addComponent(sGY)).addGap(8)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(lGW)
		/*		*/.addComponent(lGH)).addGap(4)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(sGW)
		/*		*/.addComponent(sGH)).addContainerGap());
		lr.setVerticalGroup(lr.createSequentialGroup().addGap(4)
		/**/.addGroup(lr.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lGX)
		/*		*/.addComponent(sGX)
		/*		*/.addComponent(lGW)
		/*		*/.addComponent(sGW)).addGap(4)
		/**/.addGroup(lr.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lGY)
		/*		*/.addComponent(sGY)
		/*		*/.addComponent(lGH)
		/*		*/.addComponent(sGH)).addGap(8));

		return pg;
		}

	public JPanel makeTilesPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		JLabel layer = new JLabel(Messages.getString("RoomFrame.CURRENT_TILE_LAYER"));

		Room currentRoom = editor.getRoom();

		// If there are already tiles in the room, get the list of layers and store it in a vector
		if (!currentRoom.tiles.isEmpty())
			{
			layers = new Vector<Integer>();
			int depth;

			for (Tile tile : currentRoom.tiles)
				{
				depth = tile.getDepth();
				if (!layers.contains(depth)) layers.add(depth);
				}

			}
		else
			{
			layers.add(0);
			}

		// Sort the layers in descending order
		Collections.sort(layers,Collections.reverseOrder());

		tileLayer = new JComboBox<Integer>(layers);
		tileLayer.setMaximumSize(new Dimension(Integer.MAX_VALUE,tileLayer.getHeight()));

		addLayer = new JButton(Messages.getString("RoomFrame.TILE_LAYER_ADD"));
		addLayer.addActionListener(this);
		deleteLayer = new JButton(Messages.getString("RoomFrame.TILE_LAYER_DELETE"));
		deleteLayer.addActionListener(this);
		changeLayer = new JButton(Messages.getString("RoomFrame.TILE_LAYER_CHANGE"));
		changeLayer.addActionListener(this);

		tHideOtherLayers = new JCheckBox(Messages.getString("RoomFrame.TILE_HIDE_OTHER_LAYERS"));
		tHideOtherLayers.addActionListener(this);

		tEditOtherLayers = new JCheckBox(Messages.getString("RoomFrame.TILE_EDIT_OTHER_LAYERS"));
		tEditOtherLayers.addActionListener(this);

		JTabbedPane tab = new JTabbedPane();
		tab.addTab(Messages.getString("RoomFrame.TILE_ADD"),makeTilesAddPane());
		tab.addTab(Messages.getString("RoomFrame.TILE_EDIT"),makeTilesEditPane());
		tab.setSelectedIndex(0);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(layer)
		/*	*/.addComponent(tileLayer))
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(addLayer,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE)
		/*	*/.addComponent(deleteLayer,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE)
		/*	*/.addComponent(changeLayer,DEFAULT_SIZE,PREFERRED_SIZE,MAX_VALUE))
		/**/.addGroup(
				layout.createSequentialGroup()
				/*	*/.addComponent(tHideOtherLayers,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				/*	*/.addComponent(tEditOtherLayers))
		/**/.addComponent(tab));

		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(layer)
		/*	*/.addComponent(tileLayer))
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(addLayer)
		/*	*/.addComponent(deleteLayer)
		/*	*/.addComponent(changeLayer))
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(tHideOtherLayers)
		/*	*/.addComponent(tEditOtherLayers))
		/**/.addComponent(tab));

		fireTileUpdate();
		return panel;
		}

	public JPanel makeTilesAddPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		taSource = new ResourceMenu<Background>(Background.class,
				Messages.getString("RoomFrame.NO_BACKGROUND"),true,110);
		taSource.addActionListener(this);
		tSelect = new TileSelector();
		tScroll = new JScrollPane(tSelect);
		tScroll.setPreferredSize(tScroll.getSize());
		tUnderlying = new JCheckBox(Messages.getString("RoomFrame.TILE_UNDERLYING")); //$NON-NLS-1$
		prelf.make(tUnderlying,PRoomEditor.DELETE_UNDERLYING_TILES);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(tScroll)
		/**/.addComponent(taSource)
		/**/.addComponent(tUnderlying));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(tScroll)
		/**/.addComponent(taSource)
		/**/.addComponent(tUnderlying));

		return panel;
		}

	//XXX: Extract to own class?
	public static class TileSelector extends JLabel
		{
		private static final long serialVersionUID = 1L;
		public int tx, ty;
		private ResourceReference<Background> bkg;

		public TileSelector()
			{
			super();
			setVerticalAlignment(TOP);
			enableEvents(MouseEvent.MOUSE_PRESSED);
			enableEvents(MouseEvent.MOUSE_DRAGGED);
			}

		public void setBackground(ResourceReference<Background> bkg)
			{
			this.bkg = bkg;
			Background b = deRef(bkg);
			if (b == null)
				{
				setIcon(null);
				setPreferredSize(new Dimension(0,0));
				return;
				}
			setPreferredSize(new Dimension(b.getWidth(),b.getHeight()));
			BufferedImage bi = b.getDisplayImage();
			setIcon(bi == null ? null : new ImageIcon(bi));
			}

		@Override
		public void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			Background b = deRef(bkg);
			if (b == null) return;
			//BufferedImage img = bkg.getDisplayImage();
			//if (img == null) return;

			Shape oldClip = g.getClip(); //backup the old clip
			Rectangle oldc = g.getClipBounds();
			//Set the clip properly
			g.setClip(new Rectangle(oldc.x,oldc.y,Math.min(oldc.x + oldc.width,b.getWidth()) - oldc.x,
					Math.min(oldc.y + oldc.height,b.getHeight()) - oldc.y));

			if ((Boolean) b.get(PBackground.USE_AS_TILESET))
				{
				g.setXORMode(Color.BLACK);
				g.setColor(Color.WHITE);
				g.drawRect(tx,ty,(Integer) b.get(PBackground.TILE_WIDTH),
						(Integer) b.get(PBackground.TILE_HEIGHT));
				g.setPaintMode(); //just in case
				}
			g.setClip(oldClip); //restore the clip
			}

		@Override
		protected void processMouseEvent(MouseEvent e)
			{
			if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1
					&& e.getX() < getPreferredSize().width && e.getY() < getPreferredSize().height)
				selectTile(e.getX(),e.getY());
			super.processMouseEvent(e);
			}

		@Override
		protected void processMouseMotionEvent(MouseEvent e)
			{
			if (e.getID() == MouseEvent.MOUSE_DRAGGED
					&& (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
				selectTile(e.getX(),e.getY());
			super.processMouseMotionEvent(e);
			}

		public void selectTile(int x, int y)
			{
			Background hardBkg = deRef(bkg);
			if (hardBkg == null)
				{
				tx = x;
				ty = y;
				}
			else if (!(Boolean) hardBkg.get(PBackground.USE_AS_TILESET))
				{
				tx = 0;
				ty = 0;
				}
			else
				{
				int w = (Integer) hardBkg.get(PBackground.TILE_WIDTH)
						+ (Integer) hardBkg.get(PBackground.H_SEP);
				int h = (Integer) hardBkg.get(PBackground.TILE_HEIGHT)
						+ (Integer) hardBkg.get(PBackground.V_SEP);
				int ho = hardBkg.get(PBackground.H_OFFSET);
				int vo = hardBkg.get(PBackground.V_OFFSET);
				tx = (int) Math.floor((x - ho) / w) * w + ho;
				ty = (int) Math.floor((y - vo) / h) * h + vo;
				}
			repaint();
			}
		}

	public JPanel makeTilesEditPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		MouseAdapter mouseListenerForTiles = new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
					{
					// Display the selected tile with a border and centered in the editor window
					showSelectedTile();
					}
			};

		tList = new JList<Tile>(new ArrayListModel<Tile>(res.tiles));
		tList.addListSelectionListener(this);
		tList.addMouseListener(mouseListenerForTiles);
		tList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tList.setCellRenderer(new TileListComponentRenderer());
		JScrollPane sp = new JScrollPane(tList);
		deleteTileButton = new JButton(Messages.getString("RoomFrame.TILE_DELETE")); //$NON-NLS-1$
		deleteTileButton.addActionListener(this);
		tLocked = new JCheckBox(Messages.getString("RoomFrame.TILE_LOCKED")); //$NON-NLS-1$

		JPanel pSet = new JPanel();
		pSet.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoomFrame.TILESET"))); //$NON-NLS-1$
		GroupLayout psl = new GroupLayout(pSet);
		psl.setAutoCreateGaps(true);
		psl.setAutoCreateContainerGaps(true);
		pSet.setLayout(psl);
		teSource = new ResourceMenu<Background>(Background.class,
				Messages.getString("RoomFrame.NO_BACKGROUND"),true,110); //$NON-NLS-1$
		JLabel ltsx = new JLabel(Messages.getString("RoomFrame.TILESET_X")); //$NON-NLS-1$
		tsX = new NumberField(0);
		tsX.setColumns(4);
		JLabel ltsy = new JLabel(Messages.getString("RoomFrame.TILESET_Y")); //$NON-NLS-1$
		tsY = new NumberField(0);
		tsY.setColumns(4);
		psl.setHorizontalGroup(psl.createParallelGroup()
		/**/.addComponent(teSource)
		/**/.addGroup(psl.createSequentialGroup()
		/*		*/.addComponent(ltsx)
		/*		*/.addComponent(tsX)
		/*		*/.addComponent(ltsy)
		/*		*/.addComponent(tsY)));
		psl.setVerticalGroup(psl.createSequentialGroup()
		/**/.addComponent(teSource)
		/**/.addGroup(psl.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(ltsx)
		/*		*/.addComponent(tsX)
		/*		*/.addComponent(ltsy)
		/*		*/.addComponent(tsY)));

		JPanel pTile = new JPanel();
		pTile.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoomFrame.TILE"))); //$NON-NLS-1$
		GroupLayout ptl = new GroupLayout(pTile);
		ptl.setAutoCreateGaps(true);
		ptl.setAutoCreateContainerGaps(true);
		pTile.setLayout(ptl);
		JLabel ltx = new JLabel(Messages.getString("RoomFrame.TILE_X")); //$NON-NLS-1$
		tileHorizontalPosition = new NumberField(0);
		tileHorizontalPosition.setColumns(4);
		tileHorizontalPosition.addFocusListener(this);
		JLabel lty = new JLabel(Messages.getString("RoomFrame.TILE_Y")); //$NON-NLS-1$
		tileVerticalPosition = new NumberField(0);
		tileVerticalPosition.setColumns(4);
		tileVerticalPosition.addFocusListener(this);
		JLabel ltl = new JLabel(Messages.getString("RoomFrame.TILE_LAYER")); //$NON-NLS-1$
		teDepth = new NumberField(0);
		teDepth.setColumns(8);
		ptl.setHorizontalGroup(ptl.createParallelGroup()
		/**/.addGroup(ptl.createSequentialGroup()
		/*		*/.addComponent(ltx)
		/*		*/.addComponent(tileHorizontalPosition)
		/*		*/.addComponent(lty)
		/*		*/.addComponent(tileVerticalPosition))
		/**/.addGroup(ptl.createSequentialGroup()
		/*		*/.addComponent(ltl)
		/*		*/.addComponent(teDepth)));
		ptl.setVerticalGroup(ptl.createSequentialGroup()
		/**/.addGroup(ptl.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(ltx)
		/*		*/.addComponent(tileHorizontalPosition)
		/*		*/.addComponent(lty)
		/*		*/.addComponent(tileVerticalPosition))
		/**/.addGroup(ptl.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(ltl)
		/*		*/.addComponent(teDepth)));

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(sp,DEFAULT_SIZE,120,MAX_VALUE)
		/**/.addComponent(deleteTileButton,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/**/.addComponent(tLocked)
		/**/.addComponent(pSet)
		/**/.addComponent(pTile));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(sp,DEFAULT_SIZE,60,MAX_VALUE)
		/**/.addComponent(deleteTileButton)
		/**/.addComponent(tLocked)
		/**/.addComponent(pSet)
		/**/.addComponent(pTile));
		return panel;
		}

	//TODO 1.7?: Batch tile operations
	public static JPanel makeTilesBatchPane()
		{
		JPanel panel = new JPanel();
		//		GroupLayout layout = new GroupLayout(panel);
		//		layout.setAutoCreateGaps(true);
		//		layout.setAutoCreateContainerGaps(true);
		//		panel.setLayout(layout);
		//TODO: Translate
		panel.add(new JLabel("<html>This tab will offer ways to<br />"
				+ "perform batch operations on several<br />"
				+ "tiles at once, or regions of tiles.</html>"));

		return panel;
		}

	public JPanel makeBackgroundsPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		bDrawColor = new JCheckBox(Messages.getString("RoomFrame.DRAW_COLOR")); //$NON-NLS-1$
		plf.make(bDrawColor,PRoom.DRAW_BACKGROUND_COLOR);
		JLabel lColor = new JLabel(Messages.getString("RoomFrame.COLOR")); //$NON-NLS-1$
		bColor = new ColorSelect();
		plf.make(bColor,PRoom.BACKGROUND_COLOR);

		JLabel[] backLabs = new JLabel[res.backgroundDefs.size()];
		for (int i = 0; i < backLabs.length; i++)
			{
			backLabs[i] = new JLabel(" " + Messages.getString("RoomFrame.BACK") + i); //$NON-NLS-1$
			boolean v = res.backgroundDefs.get(i).properties.get(PBackgroundDef.VISIBLE);
			backLabs[i].setFont(backLabs[i].getFont().deriveFont(v ? Font.BOLD : Font.PLAIN));
			backLabs[i].setOpaque(true);
			}
		bList = new JList<JLabel>(backLabs);
		bList.setCellRenderer(new ListComponentRenderer());
		bList.addListSelectionListener(this);
		bList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bList.setVisibleRowCount(4);
		JScrollPane sp = new JScrollPane(bList);

		for (BackgroundDef d : res.backgroundDefs)
			d.properties.getUpdateSource(PBackgroundDef.VISIBLE).addListener(bdpl);

		bVisible = new JCheckBox(Messages.getString("RoomFrame.BACK_VISIBLE")); //$NON-NLS-1$
		bForeground = new JCheckBox(Messages.getString("RoomFrame.BACK_FOREGROUND")); //$NON-NLS-1$

		bSource = new ResourceMenu<Background>(Background.class,
				Messages.getString("RoomFrame.NO_BACKGROUND"),true,150); //$NON-NLS-1$

		bTileH = new JCheckBox(Messages.getString("RoomFrame.BACK_TILE_HOR")); //$NON-NLS-1$
		JLabel lbx = new JLabel(Messages.getString("RoomFrame.BACK_X")); //$NON-NLS-1$
		bX = new NumberField(0);
		bX.setColumns(4);
		bTileV = new JCheckBox(Messages.getString("RoomFrame.BACK_TILE_VERT")); //$NON-NLS-1$
		JLabel lby = new JLabel(Messages.getString("RoomFrame.BACK_Y")); //$NON-NLS-1$
		bY = new NumberField(0);
		bY.setColumns(4);
		bStretch = new JCheckBox(Messages.getString("RoomFrame.BACK_STRETCH")); //$NON-NLS-1$
		JLabel lbh = new JLabel(Messages.getString("RoomFrame.BACK_HSPEED")); //$NON-NLS-1$
		bH = new NumberField(-999,999);
		JLabel lbv = new JLabel(Messages.getString("RoomFrame.BACK_VSPEED")); //$NON-NLS-1$
		bV = new NumberField(-999,999);

		bList.setSelectedIndex(0);

		Insets spi = sp.getInsets();
		int spmh = bList.getMaximumSize().height + spi.bottom + spi.top;
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(bDrawColor)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lColor)
		/*		*/.addComponent(bColor))
		/**/.addComponent(sp)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(bVisible)
		/*		*/.addComponent(bForeground))
		/**/.addComponent(bSource)
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(lbx)
		/*		*/.addComponent(bX)
		/*		*/.addComponent(lby)
		/*		*/.addComponent(bY))
		/**/.addGroup(layout.createSequentialGroup()
		/*		*/.addGroup(layout.createParallelGroup(Alignment.TRAILING)
		/*				*/.addComponent(lbh)
		/*				*/.addComponent(lbv))
		/*		*/.addGroup(layout.createParallelGroup()
		/*				*/.addComponent(bH)
		/*				*/.addComponent(bV)))
		/**/.addComponent(bTileH)
		/**/.addComponent(bTileV)
		/**/.addComponent(bStretch));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(bDrawColor)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE,false)
		/*		*/.addComponent(lColor)
		/*		*/.addComponent(bColor))
		/**/.addComponent(sp,DEFAULT_SIZE,DEFAULT_SIZE,spmh)
		/**/.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(bVisible)
		/*		*/.addComponent(bForeground))
		/**/.addComponent(bSource)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lbx)
		/*		*/.addComponent(bX)
		/*		*/.addComponent(lby)
		/*		*/.addComponent(bY))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lbh)
		/*		*/.addComponent(bH))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lbv)
		/*		*/.addComponent(bV))
		/**/.addComponent(bTileH)
		/**/.addComponent(bTileV)
		/**/.addComponent(bStretch));
		return panel;
		}

	public JPanel makePhysicsPane()
		{
		JPanel panel = new JPanel();

		JCheckBox phyWorldCB = new JCheckBox(Messages.getString("RoomFrame.PHY_WORLD_ENABLED"));
		plf.make(phyWorldCB,PRoom.PHYSICS_WORLD);

		JLabel pixMetersLabel = new JLabel(Messages.getString("RoomFrame.PHY_PIXELSPERMETER"));
		NumberField pixMetersField = new NumberField(0.1000);
		plf.make(pixMetersField,PRoom.PHYSICS_PIXTOMETERS);
		pixMetersField.setColumns(16);

		JLabel gravityXLabel = new JLabel(Messages.getString("RoomFrame.PHY_GRAVITY_X"));
		NumberField gravityXField = new NumberField(0.0);
		plf.make(gravityXField,PRoom.PHYSICS_GRAVITY_X);
		gravityXField.setColumns(16);

		JLabel gravityYLabel = new JLabel(Messages.getString("RoomFrame.PHY_GRAVITY_Y"));
		NumberField gravityYField = new NumberField(10.0);
		plf.make(gravityYField,PRoom.PHYSICS_GRAVITY_Y);
		gravityYField.setColumns(16);

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(phyWorldCB)
		/**/.addGroup(layout.createSequentialGroup()
		/*  */.addComponent(pixMetersLabel)
		/*  */.addComponent(pixMetersField))
		/**/.addGroup(layout.createSequentialGroup()
		/*  */.addComponent(gravityXLabel)
		/*  */.addComponent(gravityXField))
		/**/.addGroup(layout.createSequentialGroup()
		/*  */.addComponent(gravityYLabel)
		/*  */.addComponent(gravityYField)));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(phyWorldCB)
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(pixMetersLabel)
		/*  */.addComponent(pixMetersField))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(gravityXLabel)
		/*  */.addComponent(gravityXField))
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(gravityYLabel)
		/*  */.addComponent(gravityYField)));

		return panel;
		}

	public JPanel makeViewsPane()
		{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		vEnabled = new JCheckBox(Messages.getString("RoomFrame.VIEWS_ENABLED")); //$NON-NLS-1$
		plf.make(vEnabled,PRoom.VIEWS_ENABLED);
		vClear = new JCheckBox(Messages.getString("RoomFrame.VIEWS_CLEAR")); //$NON-NLS-1$
		plf.make(vClear,PRoom.VIEWS_CLEAR);

		JLabel[] viewLabs = new JLabel[res.views.size()];
		for (int i = 0; i < viewLabs.length; i++)
			{
			viewLabs[i] = new JLabel(" " + Messages.getString("RoomFrame.VIEW") + i); //$NON-NLS-1$
			boolean v = res.views.get(i).properties.get(PView.VISIBLE);
			viewLabs[i].setFont(viewLabs[i].getFont().deriveFont(v ? Font.BOLD : Font.PLAIN));
			viewLabs[i].setOpaque(true);
			}
		vList = new JList<JLabel>(viewLabs);
		vList.setCellRenderer(new ListComponentRenderer());
		//vList.setVisibleRowCount(4);
		vList.addListSelectionListener(this);
		vList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(vList);

		for (View v : res.views)
			{
			v.properties.getUpdateSource(PView.VISIBLE).addListener(vpl);
			v.properties.getUpdateSource(PView.OBJECT).addListener(vpl);
			v.properties.getUpdateSource(PView.VIEW_W).addListener(vpl);
			v.properties.getUpdateSource(PView.VIEW_H).addListener(vpl);
			v.properties.getUpdateSource(PView.BORDER_H).addListener(vpl);
			v.properties.getUpdateSource(PView.BORDER_V).addListener(vpl);
			}

		vVisible = new JCheckBox(Messages.getString("RoomFrame.VIEW_ENABLED")); //$NON-NLS-1$

		JTabbedPane tp = makeViewsDimensionsPane();
		JPanel pf = makeViewsFollowPane();

		vList.setSelectedIndex(0);

		Insets spi = sp.getInsets();
		int spmh = vList.getMaximumSize().height + spi.bottom + spi.top;
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(vEnabled)
		/**/.addComponent(vClear)
		/**/.addComponent(sp)
		/**/.addComponent(vVisible)
		/**/.addComponent(tp)
		/**/.addComponent(pf));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(vEnabled)
		/**/.addComponent(vClear)
		/**/.addComponent(sp,DEFAULT_SIZE,DEFAULT_SIZE,spmh)
		/**/.addComponent(vVisible)
		/**/.addComponent(tp,DEFAULT_SIZE,DEFAULT_SIZE,PREFERRED_SIZE)
		/**/.addComponent(pf));
		return panel;
		}

	private JTabbedPane makeViewsDimensionsPane()
		{
		JPanel pr = new JPanel();
		GroupLayout lr = new GroupLayout(pr);
		pr.setLayout(lr);

		JLabel lRX = new JLabel(Messages.getString("RoomFrame.VIEW_X")); //$NON-NLS-1$
		vRX = new NumberField(0,999999);
		JLabel lRW = new JLabel(Messages.getString("RoomFrame.VIEW_W")); //$NON-NLS-1$
		vRW = new NumberField(1,999999);
		JLabel lRY = new JLabel(Messages.getString("RoomFrame.VIEW_Y")); //$NON-NLS-1$
		vRY = new NumberField(0,999999);
		JLabel lRH = new JLabel(Messages.getString("RoomFrame.VIEW_H")); //$NON-NLS-1$
		vRH = new NumberField(1,999999);
		lr.setHorizontalGroup(lr.createSequentialGroup().addContainerGap()
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(lRX)
		/*		*/.addComponent(lRY)).addGap(4)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(vRX)
		/*		*/.addComponent(vRY)).addGap(8)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(lRW)
		/*		*/.addComponent(lRH)).addGap(4)
		/**/.addGroup(lr.createParallelGroup()
		/*		*/.addComponent(vRW)
		/*		*/.addComponent(vRH)).addContainerGap());
		lr.setVerticalGroup(lr.createSequentialGroup().addGap(4)
		/**/.addGroup(lr.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lRX)
		/*		*/.addComponent(vRX)
		/*		*/.addComponent(lRW)
		/*		*/.addComponent(vRW)).addGap(4)
		/**/.addGroup(lr.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lRY)
		/*		*/.addComponent(vRY)
		/*		*/.addComponent(lRH)
		/*		*/.addComponent(vRH)).addGap(8));

		JPanel pp = new JPanel();
		GroupLayout lp = new GroupLayout(pp);
		pp.setLayout(lp);

		JLabel lPX = new JLabel(Messages.getString("RoomFrame.PORT_X")); //$NON-NLS-1$
		vPX = new NumberField(0,999999);
		JLabel lPW = new JLabel(Messages.getString("RoomFrame.PORT_W")); //$NON-NLS-1$
		vPW = new NumberField(1,999999);
		JLabel lPY = new JLabel(Messages.getString("RoomFrame.PORT_Y")); //$NON-NLS-1$
		vPY = new NumberField(0,999999);
		JLabel lPH = new JLabel(Messages.getString("RoomFrame.PORT_H")); //$NON-NLS-1$
		vPH = new NumberField(1,999999);
		lp.setHorizontalGroup(lp.createSequentialGroup().addContainerGap()
		/**/.addGroup(lp.createParallelGroup()
		/*		*/.addComponent(lPX)
		/*		*/.addComponent(lPY)).addGap(4)
		/**/.addGroup(lp.createParallelGroup()
		/*		*/.addComponent(vPX)
		/*		*/.addComponent(vPY)).addGap(8)
		/**/.addGroup(lp.createParallelGroup()
		/*		*/.addComponent(lPW)
		/*		*/.addComponent(lPH)).addGap(4)
		/**/.addGroup(lp.createParallelGroup()
		/*		*/.addComponent(vPW)
		/*		*/.addComponent(vPH)).addContainerGap());
		lp.setVerticalGroup(lp.createSequentialGroup().addGap(4)
		/**/.addGroup(lp.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lPX)
		/*		*/.addComponent(vPX)
		/*		*/.addComponent(lPW)
		/*		*/.addComponent(vPW)).addGap(4)
		/**/.addGroup(lp.createParallelGroup(Alignment.BASELINE)
		/*		*/.addComponent(lPY)
		/*		*/.addComponent(vPY)
		/*		*/.addComponent(lPH)
		/*		*/.addComponent(vPH)).addGap(8));

		JTabbedPane tp = new JTabbedPane();
		tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tp.addTab(Messages.getString("RoomFrame.VIEW_IN_ROOM"),pr); //$NON-NLS-1$
		tp.addTab(Messages.getString("RoomFrame.PORT"),pp); //$NON-NLS-1$
		return tp;
		}

	private JPanel makeViewsFollowPane()
		{
		JPanel pf = new JPanel();
		pf.setBorder(BorderFactory.createTitledBorder(Messages.getString("RoomFrame.FOLLOW"))); //$NON-NLS-1$
		GroupLayout lf = new GroupLayout(pf);
		pf.setLayout(lf);

		vObj = new ResourceMenu<GmObject>(GmObject.class,
				Messages.getString("RoomFrame.NO_OBJECT"),true,110); //$NON-NLS-1$

		JLabel lH = new JLabel(Messages.getString("RoomFrame.VIEW_HORIZONTAL"));
		JLabel lV = new JLabel(Messages.getString("RoomFrame.VIEW_VERTICAL"));
		JLabel lBorder = new JLabel(Messages.getString("RoomFrame.VIEW_BORDER"));
		JLabel lSpeed = new JLabel(Messages.getString("RoomFrame.VIEW_SPEED"));
		vOHBor = new NumberField(0,32000);
		vOHSp = new NumberField(-1,32000);
		vOVBor = new NumberField(0,32000);
		vOVSp = new NumberField(-1,32000);
		lf.setHorizontalGroup(lf.createSequentialGroup().addContainerGap()
		/**/.addGroup(lf.createParallelGroup()
		/*		*/.addComponent(vObj)
		/*		*/.addGroup(lf.createSequentialGroup()
		/*				*/.addGroup(lf.createParallelGroup(Alignment.TRAILING)
		/*						*/.addComponent(lH)
		/*						*/.addComponent(lV)).addGap(4)
		/*				*/.addGroup(lf.createParallelGroup()
		/*						*/.addComponent(lBorder)
		/*						*/.addComponent(vOHBor)
		/*						*/.addComponent(vOVBor)).addGap(4)
		/*				*/.addGroup(lf.createParallelGroup()
		/*						*/.addComponent(lSpeed)
		/*						*/.addComponent(vOHSp)
		/*						*/.addComponent(vOVSp)))).addContainerGap());
		lf.setVerticalGroup(lf.createSequentialGroup().addGap(4)
		/**/.addComponent(vObj).addGap(4)
		/*		*/.addGroup(lf.createParallelGroup(Alignment.BASELINE)
		/*				*/.addComponent(lBorder)
		/*				*/.addComponent(lSpeed)).addGap(4)
		/*		*/.addGroup(lf.createParallelGroup(Alignment.BASELINE)
		/*				*/.addComponent(lH)
		/*				*/.addComponent(vOHBor)
		/*				*/.addComponent(vOHSp)).addGap(4)
		/*		*/.addGroup(lf.createParallelGroup(Alignment.BASELINE)
		/*				*/.addComponent(lV)
		/*				*/.addComponent(vOVBor)
		/*				*/.addComponent(vOVSp)).addGap(8));
		return pf;
		}

	private JPanel makeStatsPane()
		{
		JPanel stat = new JPanel();
		stat.setLayout(new BoxLayout(stat,BoxLayout.X_AXIS));
		stat.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));

		statX = new JLabel(Messages.getString("RoomFrame.STAT_X")); //$NON-NLS-1$
		statX.setMaximumSize(new Dimension(50,14));
		stat.add(statX);

		//stat.add(new JLabel("|")); //$NON-NLS-1$
		//visible divider    ^   since JSeparator isn't visible
		JSeparator sep;
		sep = new JSeparator(JSeparator.VERTICAL);
		sep.setMaximumSize(new Dimension(8,sep.getMaximumSize().height));
		stat.add(sep);

		statY = new JLabel(Messages.getString("RoomFrame.STAT_Y")); //$NON-NLS-1$
		statY.setMaximumSize(new Dimension(50,13));
		stat.add(statY);

		sep = new JSeparator(JSeparator.VERTICAL);
		sep.setMaximumSize(new Dimension(8,sep.getMaximumSize().height));
		stat.add(sep);

		statId = new JLabel();
		stat.add(statId);

		sep = new JSeparator(JSeparator.VERTICAL);
		sep.setMaximumSize(new Dimension(8,sep.getMaximumSize().height));
		stat.add(sep);

		statSrc = new JLabel();
		stat.add(statSrc); //resizes at will, so no Max size

		return stat;
		}

	public RoomFrame(Room res, ResNode node)
		{
		super(res,node);
		editor = new RoomEditor(res,this);
		prelf = new PropertyLinkFactory<PRoomEditor>(editor.properties,null);

		GroupLayout layout = new GroupLayout(getContentPane())
			{
				@Override
				public void layoutContainer(Container parent)
					{
					Dimension m = RoomFrame.this.getMinimumSize();
					Dimension s = RoomFrame.this.getSize();
					Dimension r = new Dimension(Math.max(m.width,s.width),Math.max(m.height,s.height));
					if (!r.equals(s))
						RoomFrame.this.setSize(r);
					else
						super.layoutContainer(parent);
					}
			};

		setLayout(layout);

		JToolBar tools = makeToolBar();

		//conveniently, these tabs happen to have the same indexes as GM's tabs
		tabs = new JTabbedPane();
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.addTab(Messages.getString("RoomFrame.TAB_OBJECTS"),makeObjectsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_SETTINGS"),makeSettingsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_TILES"),makeTilesPane()); //$NON-NLS-1$
		String bks = Messages.getString("RoomFrame.TAB_BACKGROUNDS"); //$NON-NLS-1$
		tabs.addTab(bks,makeBackgroundsPane());
		tabs.addTab(Messages.getString("RoomFrame.TAB_VIEWS"),makeViewsPane()); //$NON-NLS-1$
		tabs.addTab(Messages.getString("RoomFrame.TAB_PHYSICS"),makePhysicsPane()); //$NON-NLS-1$

		int selectedTab = (Integer) res.get(PRoom.CURRENT_TAB);
		tabs.setSelectedIndex(selectedTab);
		tabs.addChangeListener(this);

		res.instanceUpdateSource.addListener(this);
		res.tileUpdateSource.addListener(this);

		editorPane = new EditorScrollPane(editor, true);
		prelf.make(editorPane,PRoomEditor.ZOOM);

		// If the view tab is selected, display the first selected view centered
		if (selectedTab == Room.TAB_VIEWS)
			{
			showSelectedView();
			editor.roomVisual.setViewsVisible(true);
			}

		JPanel stats = makeStatsPane();

		SequentialGroup orientationGroup = layout.createSequentialGroup();
		if (!Prefs.rightOrientation) {
			orientationGroup.addComponent(tabs,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE);
		}
		orientationGroup.addGroup(layout.createParallelGroup()
		/*		*/.addComponent(editorPane,200,640,DEFAULT_SIZE)
		/*		*/.addComponent(stats));
		if (Prefs.rightOrientation) {
			orientationGroup.addComponent(tabs,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE);
		}

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addComponent(tools,PREFERRED_SIZE,PREFERRED_SIZE,MAX_VALUE)
		/**/.addGroup(orientationGroup));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addComponent(tools,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(tabs)
		/*	*/.addGroup(layout.createSequentialGroup()
		/*		*/.addComponent(editorPane,DEFAULT_SIZE,480,DEFAULT_SIZE)
		/*		*/.addComponent(stats))));

		// initialize the undo/redo system
		undoManager = new UndoManager();
		undoManager.setLimit(Prefs.undoHistorySize);
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(new UndoAdapter());
		refreshUndoRedoButtons();

		if (res.get(PRoom.REMEMBER_WINDOW_SIZE))
			{
			int h = res.get(PRoom.EDITOR_HEIGHT);
			int w = res.get(PRoom.EDITOR_WIDTH);
			Dimension d = LGM.mdi.getSize();
			if (d.width <= w && d.height <= h) {
				maximize = true;
			} else {
				setSize(w, h);
			}
			}
		else
			pack();
		}

	private boolean maximize;

	// maximizes over-sized RoomFrames, since setMaximum can't
	// be called until after it's been added to the MDI
	@Override
	public void setVisible(boolean b)
		{
		super.setVisible(b);
		if (!maximize) return;
		try
			{
			setMaximum(true);
			}
		catch (PropertyVetoException e)
			{
			setSize((Integer) res.get(PRoom.EDITOR_WIDTH),(Integer) res.get(PRoom.EDITOR_HEIGHT));
			e.printStackTrace();
			}
		}

	public static class ListComponentRenderer implements ListCellRenderer<Object>
		{
		public Component getListCellRendererComponent(JList<? extends Object> list, Object val,
				int ind, boolean selected, boolean focus)
			{
			Component lab = (Component) val;
			if (selected)
				{
				lab.setBackground(list.getSelectionBackground());
				lab.setForeground(list.getSelectionForeground());
				}
			else
				{
				lab.setBackground(list.getBackground());
				lab.setForeground(list.getForeground());
				}
			return lab;
			}
		}

	@Override
	protected boolean areResourceFieldsEqual()
		{
		return (res.backgroundDefs.equals(resOriginal.backgroundDefs)
				&& res.views.equals(resOriginal.views) && res.instances.equals(resOriginal.instances) && res.tiles.equals(resOriginal.tiles));
		}

	@Override
	public void commitChanges()
		{
		res.setName(name.getText());

		for (CodeFrame cf : codeFrames.values())
			cf.commitChanges();

		if (res.get(PRoom.REMEMBER_WINDOW_SIZE))
			{
			res.put(PRoom.CURRENT_TAB,tabs.getSelectedIndex());
			Dimension s = getSize();
			res.put(PRoom.EDITOR_WIDTH,s.width);
			res.put(PRoom.EDITOR_HEIGHT,s.height);
			}
		}

	// Window which displays the room controls
	public static JFrame roomControlsFrame;

	private void deleteAction()
		{
		deleteAction(true);
		}

	private void deleteAction(boolean askConfirmation)
		{
		boolean tilesTabIsSelected = (tabs.getSelectedIndex() == Room.TAB_TILES);

		String message;
		int result = 0;

		if (askConfirmation)
			{
			// Set message
			if (tilesTabIsSelected)
				message = Messages.getString("RoomFrame.DELETE_TILES");
			else
				message = Messages.getString("RoomFrame.DELETE_OBJECTS");

			// Get a confirmation from the user
			result = JOptionPane.showConfirmDialog(this,message,
					Messages.getString("RoomFrame.DELETE_TITLE"),JOptionPane.YES_NO_OPTION);
			}

		if (result == JOptionPane.YES_OPTION || askConfirmation == false)
			{

			Piece selectedPiece = editor.getSelectedPiece();

			// If there is a selected piece, deselect it
			if (selectedPiece != null) selectedPiece.setSelected(false);

			Room currentRoom = editor.getRoom();

			// Stores several actions in one compound action for the undo
			CompoundEdit compoundEdit = new CompoundEdit();

			// If the user draw a region
			if (editor.selection != null)
				{
				Rectangle selection = editor.selection;

				if (tilesTabIsSelected)
					deleteTilesInSelection(selection,compoundEdit);

				else
					deleteInstancesInSelection(selection,compoundEdit);

				}
			else
				{

				if (tilesTabIsSelected)
					{
					// Record the effect of removing all tiles for the undo
					for (int i = currentRoom.tiles.size() - 1; i >= 0; i--)
						{
						UndoableEdit edit = new RemovePieceInstance(this,(Piece) currentRoom.tiles.get(i),i);
						compoundEdit.addEdit(edit);
						}

					// Remove all tiles
					currentRoom.tiles.clear();
					}
				else
					{
					// Record the effect of removing all instances for the undo
					for (int i = currentRoom.instances.size() - 1; i >= 0; i--)
						{
						UndoableEdit edit = new RemovePieceInstance(this,(Piece) currentRoom.instances.get(i),i);
						compoundEdit.addEdit(edit);
						}

					// Remove all instances
					currentRoom.instances.clear();
					}

				}

			// Save the action for the undo
			compoundEdit.end();
			undoSupport.postEdit(compoundEdit);

			}

		}

	// Delete all instances for a given selection
	public void deleteInstancesInSelection(Rectangle selection, CompoundEdit compoundEdit)
		{
		Room currentRoom = editor.getRoom();
		Point instancePosition;

		// Remove each instance in the selection
		for (int i = currentRoom.instances.size() - 1; i >= 0; i--)
			{
			instancePosition = currentRoom.instances.get(i).getPosition();

			// If the instance is in the selected region
			if (instancePosition.x >= selection.x && instancePosition.x < (selection.x + selection.width)
					&& instancePosition.y >= selection.y
					&& instancePosition.y < (selection.y + selection.height))
				{
				// Record the effect of removing an instance for the undo
				UndoableEdit edit = new RemovePieceInstance(this,(Piece) currentRoom.instances.get(i),i);
				compoundEdit.addEdit(edit);

				currentRoom.instances.remove(i);
				}
			}

		}

	// Delete all tiles for a given selection
	public void deleteTilesInSelection(Rectangle selection, CompoundEdit compoundEdit)
		{
		Room currentRoom = editor.getRoom();

		// Get the selected layer
		Integer depth = (Integer) tileLayer.getSelectedItem();

		Point tilePosition;

		// Remove each tile with the selected layer
		for (int i = currentRoom.tiles.size() - 1; i >= 0; i--)
			{
			tilePosition = currentRoom.tiles.get(i).getPosition();

			// If the tile is in the selected region
			if (tilePosition.x >= selection.x && tilePosition.x < (selection.x + selection.width)
					&& tilePosition.y >= selection.y && tilePosition.y < (selection.y + selection.height))
				{
				// If the were editing only the current layer, and if the tile is not in the current layer
				if (!tEditOtherLayers.isSelected() && currentRoom.tiles.get(i).getDepth() != depth)
					continue;

				// Record the effect of removing a tile for the undo
				UndoableEdit edit = new RemovePieceInstance(this,(Piece) currentRoom.tiles.get(i),i);
				compoundEdit.addEdit(edit);

				currentRoom.tiles.remove(i);
				}

			}

		}

	@Override
	public void actionPerformed(ActionEvent e)
		{
		Object eventSource = e.getSource();

		if (eventSource == fill)
			{
			// if the user didn't make any selection
			if (editor.selection == null) return;

			boolean tilesTabIsSelected = (tabs.getSelectedIndex() == Room.TAB_TILES);
			boolean objectsTabIsSelected = (tabs.getSelectedIndex() == Room.TAB_OBJECTS);
			boolean snapToGridMode = editor.properties.get(PRoomEditor.SNAP_TO_GRID);
			boolean deleteUnderlyingObjects = editor.properties.get(PRoomEditor.DELETE_UNDERLYING_OBJECTS);
			boolean deleteUnderlyingTiles = editor.properties.get(PRoomEditor.DELETE_UNDERLYING_TILES);

			// If no object is selected
			if (objectsTabIsSelected && oNew.getSelected() == null) return;
			if (tilesTabIsSelected && taSource.getSelected() == null) return;

			// If there is a selected piece, deselect it
			if (selectedPiece != null) selectedPiece.setSelected(false);

			Room currentRoom = editor.getRoom();
			Rectangle selection = editor.selection;

			// Get the 'snap' properties of the current room
			int snapX = currentRoom.properties.get(PRoom.SNAP_X);
			int snapY = currentRoom.properties.get(PRoom.SNAP_Y);

			Dimension cellDimension = null;
			Dimension tileDimension = null;

			// Stores several actions in one compound action for the undo
			CompoundEdit compoundEdit = new CompoundEdit();

			// If the tiles tab is selected,
			if (tilesTabIsSelected)
				{
				// If the 'Delete underlying' option is checked, delete all tiles for the selected region
				if (deleteUnderlyingTiles) deleteTilesInSelection(selection,compoundEdit);

				// Get and store the tile's dimension
				ResourceReference<Background> bkg = taSource.getSelected();
				Background b = bkg.get();

				if (!(Boolean) b.get(PBackground.USE_AS_TILESET))
					tileDimension = new Dimension(b.getWidth(),b.getHeight());
				else
					tileDimension = new Dimension((Integer) b.get(PBackground.TILE_WIDTH),
							(Integer) b.get(PBackground.TILE_HEIGHT));
				}

			// If object's tab is selected and the 'Delete underlying' option is checked, delete all instances for the selected region
			if (objectsTabIsSelected && deleteUnderlyingObjects)
				deleteInstancesInSelection(selection,compoundEdit);

			// If snapping is deactivated, use the piece's width for setting its position
			if (snapToGridMode == false)
				{
				if (objectsTabIsSelected)
					{
					ResourceReference<GmObject> instanceObject = oNew.getSelected();
					BufferedImage image = instanceObject.get().getDisplayImage();

					// If there is no image for this instance, use the default sprite image
					if (image == null)
						{
						ImageIcon emptySprite = LGM.getIconForKey("Resource.EMPTY_OBJ");
						cellDimension = new Dimension(emptySprite.getIconWidth(),emptySprite.getIconHeight());
						}
					else
						{
						cellDimension = new Dimension(image.getWidth(),image.getHeight());
						}

					}
				else
					{
					cellDimension = tileDimension;
					}
				}
			else
				{
				// Use snapping for setting the piece's position
				cellDimension = new Dimension(snapX,snapY);
				}

			int numberOfColumns = editor.selection.width / cellDimension.width;
			int numberOfRows = editor.selection.height / cellDimension.height;

			// Browse each cell of the selected region
			for (int i = 0; i < numberOfColumns; i++)
				for (int j = 0; j < numberOfRows; j++)
					{
					// Position of the current piece
					Point newPosition = new Point(selection.x + (cellDimension.width * i),selection.y
							+ (cellDimension.height * j));

					// If object's tab is selected, add a new object
					if (objectsTabIsSelected)
						{
						Instance newInstance = res.addInstance();
						newInstance.properties.put(PInstance.OBJECT,oNew.getSelected());
						newInstance.setPosition(newPosition);

						// Record the effect of adding a new instance for the undo
						UndoableEdit edit = new AddPieceInstance(this,newInstance,
								currentRoom.instances.size() - 1);
						compoundEdit.addEdit(edit);
						}

					// If the tile's tab is selected, add a new tile
					if (tilesTabIsSelected)
						{
						ResourceReference<Background> bkg = taSource.getSelected();

						Tile newTile = new Tile(currentRoom,LGM.currentFile);
						newTile.properties.put(PTile.BACKGROUND,bkg);
						newTile.setBackgroundPosition(new Point(tSelect.tx,tSelect.ty));
						newTile.setPosition(newPosition);
						newTile.setSize(tileDimension);
						newTile.setDepth((Integer) tileLayer.getSelectedItem());

						currentRoom.tiles.add(newTile);

						// Record the effect of adding a new tile for the undo
						UndoableEdit edit = new AddPieceInstance(this,newTile,currentRoom.tiles.size() - 1);
						compoundEdit.addEdit(edit);
						}

					}

			// Save the action for the undo
			compoundEdit.end();
			undoSupport.postEdit(compoundEdit);

			}

		// If the user has pressed the 'Add' new layer button
		if (eventSource == addLayer)
			{
			// Create the panel with the depth property
			JPanel myPanel = new JPanel();
			myPanel.add(new JLabel(Messages.getString("RoomFrame.TILE_DEPTH")));
			NumberField depth = new NumberField(Integer.MIN_VALUE,Integer.MAX_VALUE,0);
			myPanel.add(depth);

			int result = JOptionPane.showConfirmDialog(this,myPanel,
					Messages.getString("RoomFrame.ADD_NEW_TILE"),JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			// If the user has pressed the OK button
			if (result == JOptionPane.OK_OPTION)
				{
				// Get the new layer's depth
				Integer newDepth = new Integer(depth.getIntValue());

				// If the layer is new, add it
				if (!layers.contains(newDepth))
					{
					layers.add(newDepth);
					// Sort the layers in descending order
					Collections.sort(layers,Collections.reverseOrder());
					// Select the new layer
					tileLayer.setSelectedItem(newDepth);
					}
				}

			}

		// If the user has pressed the 'delete' tile's layer button
		if (eventSource == deleteLayer)
			{
			// Get a confirmation from the user
			int result = JOptionPane.showConfirmDialog(this,
					Messages.getString("RoomFrame.DELETE_TILE_LAYER"),
					Messages.getString("RoomFrame.DELETE_TITLE"),JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.YES_OPTION)
				{
				Room currentRoom = editor.getRoom();
				// Get the selected layer
				Integer depth = (Integer) tileLayer.getSelectedItem();

				// Stores several actions in one compound action for the undo
				CompoundEdit compoundEdit = new CompoundEdit();

				// Remove each tile with the selected layer
				for (int i = currentRoom.tiles.size() - 1; i >= 0; i--)
					{
					if (currentRoom.tiles.get(i).getDepth() == depth)
						{
						// Record the effect of removing a tile for the undo
						UndoableEdit edit = new RemovePieceInstance(this,(Piece) currentRoom.tiles.get(i),i);
						compoundEdit.addEdit(edit);

						currentRoom.tiles.remove(i);
						}
					}

				// Save the action for the undo
				compoundEdit.end();
				undoSupport.postEdit(compoundEdit);

				// Remove the layer from the combo box
				layers.remove(depth);

				if (layers.size() == 0) layers.add(0);

				tileLayer.setSelectedIndex(0);
				}
			}

		// If the user has pressed the 'Change' tile's layer button
		if (eventSource == changeLayer)
			{
			JPanel myPanel = new JPanel();
			myPanel.add(new JLabel(Messages.getString("RoomFrame.TILE_DEPTH")));
			// Get the selected layer
			final Integer depth = (Integer) tileLayer.getSelectedItem();
			NumberField depthField = new NumberField(Integer.MIN_VALUE,Integer.MAX_VALUE,depth);
			myPanel.add(depthField);

			int result = JOptionPane.showConfirmDialog(this,myPanel,
					Messages.getString("RoomFrame.CHANGE_TILE_LAYER"),JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			// Get the new layer's depth
			final Integer newDepth = new Integer(depthField.getIntValue());

			if (result == JOptionPane.OK_OPTION && newDepth != depth)
				{
				Room currentRoom = editor.getRoom();

				// Stores several actions in one compound action for the undo
				//CompoundEdit compoundEdit = new CompoundEdit();

				// Update each tile with the selected layer
				for (int i = currentRoom.tiles.size() - 1; i >= 0; i--)
					{
					final Tile tile = currentRoom.tiles.get(i);
					if (tile.getDepth() == depth)
						{
						tile.setDepth(newDepth);
						}
					}

				//TODO: Save the action for the undo
				//compoundEdit.end();
				//undoSupport.postEdit(compoundEdit);

				// Replace the layer from the combo box
				layers.remove(depth);
				if (!layers.contains(newDepth)) layers.add(newDepth);
				// Sort the layers in descending order
				Collections.sort(layers,Collections.reverseOrder());
				// Select the new layer
				tileLayer.setSelectedItem(newDepth);
				}
			}

		// If the user has clicked on the 'Edit other layers' checkbox
		if (eventSource == tEditOtherLayers)
			{
			if (tEditOtherLayers.isSelected())
				editor.editOtherLayers(true);
			else
				editor.editOtherLayers(false);
			}

		// If the user has clicked on the 'Hide other layers' checkbox
		if (eventSource == tHideOtherLayers)
			{
			if (tHideOtherLayers.isSelected())
				editor.roomVisual.setVisibleLayer((Integer) tileLayer.getSelectedItem());
			else
				editor.roomVisual.setVisibleLayer(null);
			}

		// If the user has pressed the 'room controls' button
		if (eventSource == roomControls)
			{
			// If the window is not open
			if (roomControlsFrame == null)
				{
				// Create a window and set the properties
				roomControlsFrame = new JFrame(Messages.getString("RoomControls.TITLE"));
				roomControlsFrame.setIconImage(LGM.getIconForKey("RoomFrame.ROOM_CONTROLS").getImage());
				Border padding = BorderFactory.createEmptyBorder(15,15,15,15);
				JPanel contentPanel = new JPanel();
				contentPanel.setBorder(padding);
				roomControlsFrame.setContentPane(contentPanel);
				roomControlsFrame.setLayout(new BoxLayout(roomControlsFrame.getContentPane(),
						BoxLayout.Y_AXIS));
				roomControlsFrame.setResizable(false);
				roomControlsFrame.setLocationRelativeTo(null);

				// Add the labels
				roomControlsFrame.add(new JLabel(Messages.getString("RoomControls.LEFT_BUTTON")));
				roomControlsFrame.add(new JLabel(Messages.getString("RoomControls.LEFT_BUTTON_CTRl")));
				roomControlsFrame.add(new JLabel(Messages.getString("RoomControls.LEFT_BUTTON_ALT")));
				roomControlsFrame.add(new JLabel(Messages.getString("RoomControls.LEFT_BUTTON_SHIFT")));
				roomControlsFrame.add(Box.createRigidArea(new Dimension(0,5)));
				roomControlsFrame.add(new JLabel(Messages.getString("RoomControls.RIGHT_BUTTON")));

				// When closing the window, set the window to null
				roomControlsFrame.addWindowListener(new java.awt.event.WindowAdapter()
					{
						@Override
						public void windowClosing(java.awt.event.WindowEvent windowEvent)
							{
							roomControlsFrame = null;
							}
					});
				roomControlsFrame.pack();
				roomControlsFrame.setVisible(true);
				}
			else
				{
				// Make sure the window is visible and not minimized
				roomControlsFrame.setState(Frame.NORMAL);
				roomControlsFrame.setVisible(true);
				}

			}

		// If the user has pressed the shift instances button
		if (eventSource == shiftInstances)
			{
			Room currentRoom = editor.getRoom();

			// Get the 'snap' properties of the current room
			int snapX = currentRoom.properties.get(PRoom.SNAP_X);
			int snapY = currentRoom.properties.get(PRoom.SNAP_Y);

			// Display the text fields with the snap properties
			NumberField txtHorizontalShift = new NumberField(-999999,999999,snapX);
			NumberField txtVerticalShift = new NumberField(-999999,999999,snapY);

			// Create the panel with the shift properties
			JPanel myPanel = new JPanel();
			myPanel.add(new JLabel(Messages.getString("RoomFrame.HORIZONTAL_SHIFT")));
			myPanel.add(txtHorizontalShift);
			myPanel.add(Box.createHorizontalStrut(7));
			myPanel.add(new JLabel(Messages.getString("RoomFrame.VERTICAL_SHIFT")));
			myPanel.add(txtVerticalShift);

			// If the tiles tab is selected, shift the tiles
			boolean tilesTabIsSelected = (tabs.getSelectedIndex() == Room.TAB_TILES);

			String panelTitle;

			// Set the panel title
			if (tilesTabIsSelected)
				panelTitle = Messages.getString("RoomFrame.SHIFT_TILES_TITLE");
			else
				panelTitle = Messages.getString("RoomFrame.SHIFT_OBJECTS_TITLE");

			int result = JOptionPane.showConfirmDialog(this,myPanel,panelTitle,
					JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);

			if (result == JOptionPane.OK_OPTION)
				{
				// If there is no tiles to shift
				if (currentRoom.tiles.size() == 0 && tilesTabIsSelected) return;

				// If there is no objects to shift
				if (currentRoom.instances.size() == 0 && tilesTabIsSelected == false) return;

				// Get the shift values
				int horizontalShift = txtHorizontalShift.getIntValue();
				int verticalShift = txtVerticalShift.getIntValue();

				// If the position is the same
				if (horizontalShift == 0 & verticalShift == 0) return;

				Piece selectedPiece = editor.getSelectedPiece();

				// If there is a selected piece, deselect it
				if (selectedPiece != null) selectedPiece.setSelected(false);

				// If the tiles tab is selected, shift the tiles
				if (tilesTabIsSelected)
					{

					for (Tile tile : currentRoom.tiles)
						{
						// Select the instance in the list, otherwise it is sometimes not correctly selected
						tList.setSelectedValue(tile,false);

						Point newPosition = new Point(tile.getPosition().x + horizontalShift,
								tile.getPosition().y + verticalShift);
						tile.setPosition(newPosition);
						}

					}
				else
					// Shift the objects
					{

					for (Instance instance : currentRoom.instances)
						{
						// Select the instance in the list, otherwise it is sometimes not correctly selected
						oList.setSelectedValue(instance,false);

						Point newPosition = new Point(instance.getPosition().x + horizontalShift,
								instance.getPosition().y + verticalShift);
						instance.setPosition(newPosition);
						}
					}

				// Record the effect of shifting instances for the undo
				UndoableEdit edit = new ShiftPieceInstances(this,tilesTabIsSelected,horizontalShift,
						verticalShift);
				// notify the listeners
				undoSupport.postEdit(edit);

				}

			return;
			}

		if (eventSource == showButton)
			{
			showMenu.show(showButton,0,showButton.getHeight());
			return;
			}

		// If the user has pressed the 'Add' object button
		if (eventSource == addObjectButton)
			{
			// If no object is selected
			if (oNew.getSelected() == null) return;

			Piece selectedPiece = editor.getSelectedPiece();

			// If there is a selected piece, deselect it
			if (selectedPiece != null) selectedPiece.setSelected(false);

			// Add the new object instance
			Instance newObject = res.addInstance();
			newObject.properties.put(PInstance.OBJECT,oNew.getSelected());
			newObject.setPosition(new Point());

			int numberOfObjects = res.instances.size();

			// Record the effect of adding an object for the undo
			UndoableEdit edit = new AddPieceInstance(this,newObject,numberOfObjects - 1);
			// notify the listeners
			undoSupport.postEdit(edit);

			oList.setSelectedIndex(numberOfObjects - 1);

			return;
			}

		// If the user has pressed the 'Delete' object  button
		if (eventSource == deleteObjectButton)
			{
			int selectedIndex = oList.getSelectedIndex();
			if (selectedIndex == -1) return;

			Instance instance = oList.getSelectedValue();
			if (instance == null) return;

			Piece selectedPiece = editor.getSelectedPiece();

			// If there is a selected piece, deselect it
			if (selectedPiece != null) selectedPiece.setSelected(false);

			// Record the effect of removing an object for the undo
			UndoableEdit edit = new RemovePieceInstance(this,instance,selectedIndex);
			// notify the listeners
			undoSupport.postEdit(edit);

			CodeFrame codeFrame = codeFrames.get(res.instances.remove(selectedIndex));
			if (codeFrame != null) codeFrame.dispose();
			oList.setSelectedIndex(Math.min(res.instances.size() - 1,selectedIndex));
			return;
			}

		if (eventSource == taSource)
			{
			tSelect.setBackground(taSource.getSelected());
			return;
			}

		// If the user has pressed the 'Delete' tile  button
		if (eventSource == deleteTileButton)
			{
			int selectedIndex = tList.getSelectedIndex();
			if (selectedIndex >= res.tiles.size() || selectedIndex < 0) return;

			Piece selectedPiece = editor.getSelectedPiece();

			// If there is a selected piece, deselect it
			if (selectedPiece != null) selectedPiece.setSelected(false);

			Tile tile = tList.getSelectedValue();

			// Record the effect of removing an object for the undo
			UndoableEdit edit = new RemovePieceInstance(this,tile,selectedIndex);
			// notify the listeners
			undoSupport.postEdit(edit);

			res.tiles.remove(selectedIndex);
			tList.setSelectedIndex(Math.min(res.tiles.size() - 1,selectedIndex));
			return;
			}

		if (e.getSource() == sCreationCode)
			{
			openRoomCreationCode();
			return;
			}
		if (e.getSource() == oCreationCode)
			{
			if (lastObj != null) openInstanceCodeFrame(lastObj);
			return;
			}
		super.actionPerformed(e);
		}

	public void fireObjUpdate()
		{
		Instance selectedInstance = oList.getSelectedValue();
		if (lastObj == selectedInstance) return;
		lastObj = selectedInstance;
		PropertyLink.removeAll(loLocked,loSource,loName,loX,loY,loScaleX,loScaleY,loColour,loRotation,loAlpha);

		if (selectedInstance != null)
			{
			PropertyLinkFactory<PInstance> iplf = new PropertyLinkFactory<PInstance>(
					selectedInstance.properties,this);
			loLocked = iplf.make(oLocked,PInstance.LOCKED);
			loSource = iplf.make(oSource,PInstance.OBJECT);
			loName = iplf.make(objectName.getDocument(), PInstance.NAME);
			loX = iplf.make(objectHorizontalPosition,PInstance.X);
			loY = iplf.make(objectVerticalPosition,PInstance.Y);
			loScaleX = iplf.make(objectScaleX,PInstance.SCALE_X);
			loScaleY = iplf.make(objectScaleY,PInstance.SCALE_Y);
			loRotation = iplf.make(objectRotation,PInstance.ROTATION);
			loColour = iplf.make(objectColor,PInstance.COLOR);
			loAlpha = iplf.make(objectAlpha,PInstance.ALPHA);
			}
		}

	@Override
	public Dimension getMinimumSize()
		{
		Dimension p = getContentPane().getSize();
		Dimension l = getContentPane().getMinimumSize();
		Dimension s = getSize();
		l.width += s.width - p.width;
		l.height += s.height - p.height;
		return l;
		}

	public void fireTileUpdate()
		{
		Tile selectedTile = tList.getSelectedValue();
		if (lastTile == selectedTile) return;
		lastTile = selectedTile;
		PropertyLink.removeAll(ltDepth,ltLocked,ltSource,ltsX,ltsY,ltX,ltY);

		if (selectedTile != null)
			{
			PropertyLinkFactory<PTile> tplf = new PropertyLinkFactory<PTile>(selectedTile.properties,this);
			ltDepth = tplf.make(teDepth,PTile.DEPTH);
			ltLocked = tplf.make(tLocked,PTile.LOCKED);
			ltSource = tplf.make(teSource,PTile.BACKGROUND);
			ltsX = tplf.make(tsX,PTile.BG_X);
			ltsY = tplf.make(tsY,PTile.BG_Y);
			ltX = tplf.make(tileHorizontalPosition,PTile.ROOM_X);
			ltY = tplf.make(tileVerticalPosition,PTile.ROOM_Y);
			}
		}

	public void fireBackUpdate()
		{
		int i = bList.getSelectedIndex();
		if (lastValidBack == i) return;
		if (i < 0)
			{
			bList.setSelectedIndex(lastValidBack < 0 ? 0 : lastValidBack);
			return;
			}
		lastValidBack = i;
		PropertyLink.removeAll(lbVisible,lbForeground,lbSource,lbX,lbY,lbTileH,lbTileV,lbStretch,lbH,
				lbV);
		BackgroundDef b = res.backgroundDefs.get(i);
		PropertyLinkFactory<PBackgroundDef> bdplf = new PropertyLinkFactory<PBackgroundDef>(
				b.properties,this);
		lbVisible = bdplf.make(bVisible,PBackgroundDef.VISIBLE);
		lbForeground = bdplf.make(bForeground,PBackgroundDef.FOREGROUND);
		lbSource = bdplf.make(bSource,PBackgroundDef.BACKGROUND);
		lbX = bdplf.make(bX,PBackgroundDef.X);
		lbY = bdplf.make(bY,PBackgroundDef.Y);
		lbTileH = bdplf.make(bTileH,PBackgroundDef.TILE_HORIZ);
		lbTileV = bdplf.make(bTileV,PBackgroundDef.TILE_VERT);
		lbStretch = bdplf.make(bStretch,PBackgroundDef.STRETCH);
		lbH = bdplf.make(bH,PBackgroundDef.H_SPEED);
		lbV = bdplf.make(bV,PBackgroundDef.V_SPEED);
		}

	public void fireViewUpdate()
		{
		int i = vList.getSelectedIndex();
		if (lastValidView == i) return;
		if (i < 0)
			{
			bList.setSelectedIndex(lastValidView < 0 ? 0 : lastValidView);
			return;
			}
		lastValidView = i;
		PropertyLink.removeAll(lvVisible,lvRX,lvRY,lvRW,lvRH,lvPX,lvPY,lvPW,lvPH,lvObj,lvOHBor,lvOVBor,
				lvOHSp,lvOVSp);
		View view = res.views.get(i);
		PropertyLinkFactory<PView> vplf = new PropertyLinkFactory<PView>(view.properties,this);
		lvVisible = vplf.make(vVisible,PView.VISIBLE);
		lvRX = vplf.make(vRX,PView.VIEW_X);
		lvRY = vplf.make(vRY,PView.VIEW_Y);
		lvRW = vplf.make(vRW,PView.VIEW_W);
		lvRH = vplf.make(vRH,PView.VIEW_H);
		lvPX = vplf.make(vPX,PView.PORT_X);
		lvPY = vplf.make(vPY,PView.PORT_Y);
		lvPW = vplf.make(vPW,PView.PORT_W);
		lvPH = vplf.make(vPH,PView.PORT_H);
		lvObj = vplf.make(vObj,PView.OBJECT);
		lvOHBor = vplf.make(vOHBor,PView.BORDER_H);
		lvOVBor = vplf.make(vOVBor,PView.BORDER_V);
		lvOHSp = vplf.make(vOHSp,PView.SPEED_H);
		lvOVSp = vplf.make(vOVSp,PView.SPEED_V);
		}

	// Display the selected tile with a border and centered in the editor window
	private void showSelectedTile()
		{
		Piece selectedPiece = editor.getSelectedPiece();

		// If there is a selected piece, deselect it
		if (selectedPiece != null) selectedPiece.setSelected(false);

		// Display the selected tile with white border
		Tile tile = tList.getSelectedValue();
		if (tile == null) return;
		tile.setSelected(true);

		// Save the selected tile
		editor.setSelectedPiece(tile);

		Point tilePosition = tile.getPosition();

		centerObjectInViewport(tilePosition,tile.getSize().width,tile.getSize().height,null);
		}

	// Display the selected instance with a border and centered in the editor window
	private void showSelectedInstance()
		{
		Piece selectedPiece = editor.getSelectedPiece();

		// If there is a selected piece, deselect it
		if (selectedPiece != null) selectedPiece.setSelected(false);

		// Display the selected instance with white border
		Instance instance = oList.getSelectedValue();
		if (instance == null)
			{
			return;
			}
		instance.setSelected(true);

		// Save the selected instance
		editor.setSelectedPiece(instance);

		Point instancePosition = instance.getPosition();

		// Get the image dimension
		ResourceReference<GmObject> instanceObject = instance.properties.get(PInstance.OBJECT);

		BufferedImage instanceImage = null;
		if (instanceObject != null)
			{
			GmObject inst = instanceObject.get();
			if (inst != null) instanceImage = inst.getDisplayImage();
			}

		if (instanceImage == null)
			{
			ImageIcon emptySprite = LGM.getIconForKey("Resource.EMPTY_OBJ");
			centerObjectInViewport(instancePosition, emptySprite.getIconWidth(),
				emptySprite.getIconHeight(), null);
			}
		else
			{
			centerObjectInViewport(instancePosition, instanceImage.getWidth(), instanceImage.getHeight(),
				null);
			}
		}

	// Center an object in the viewport of the rooms editor
	private void centerObjectInViewport(Point objectPosition, int objectWidth, int objectHeight,
			Instance instanceToFollow)
		{
		JViewport viewport = editorPane.getViewport();

		Point newViewportPosition = new Point(0,0);

		int zoomLevel = editor.properties.get(PRoomEditor.ZOOM);

		// Viewport scale when zooming out
		int viewportScale = 0;

		if (zoomLevel == -1) viewportScale = 3;

		if (zoomLevel == 0) viewportScale = 2;

		// If we are zooming out
		if (zoomLevel < 1)
			{
			newViewportPosition.x = objectPosition.x
					- (viewport.getWidth() * viewportScale - objectWidth) / 2;
			newViewportPosition.y = objectPosition.y
					- (viewport.getHeight() * viewportScale - objectHeight) / 2;
			}
		else
			{
			newViewportPosition.x = objectPosition.x - (viewport.getWidth() - objectWidth * zoomLevel)
					/ (2 * zoomLevel);
			newViewportPosition.y = objectPosition.y - (viewport.getHeight() - objectHeight * zoomLevel)
					/ (2 * zoomLevel);
			}

		// If the new position of the object is above the room origin coordinates, use the room coordinates for the new object coordinates
		if (newViewportPosition.x < editor.getOverallBounds().x)
			newViewportPosition.x = editor.getOverallBounds().x;

		if (newViewportPosition.y < editor.getOverallBounds().y)
			newViewportPosition.y = editor.getOverallBounds().y;

		if (instanceToFollow == null)
			{
			// If the object position is above the viewport coordinates, use the object coordinates for the new viewport coordinates
			if (objectPosition.x < newViewportPosition.x) newViewportPosition.x = objectPosition.x;

			if (objectPosition.y < newViewportPosition.y) newViewportPosition.y = objectPosition.x;
			}

		// For the new viewport position, take into account the visual offset of the border and the zoom level
		editor.visualToComponent(newViewportPosition);

		viewport.setViewPosition(newViewportPosition);
		}

	// Display the selected view in the center of the window
	private void showSelectedView()
		{
		if (editorPane == null) return;

		Room currentRoom = editor.getRoom();

		// If the views are not enabled
		if ((Boolean) currentRoom.get(PRoom.VIEWS_ENABLED) == false) return;

		// Get the selected view
		View view = res.views.get(vList.getSelectedIndex());

		// If the view is not visible, don't show it
		if ((Boolean) view.properties.get(PView.VISIBLE) == false) return;

		// Get the reference to the 'Object following' object
		ResourceReference<GmObject> objectToFollowReference = null;

		// If there is 'Object following' object for the selected view
		if (view.properties.get(PView.OBJECT) != null)
			objectToFollowReference = view.properties.get(PView.OBJECT);

		Instance instanceToFollow = null;

		// If there is an object to follow, get the first instance in the room
		if (objectToFollowReference != null)
			{
			for (Instance instance : currentRoom.instances)
				{
				ResourceReference<GmObject> instanceObject = instance.properties.get(PInstance.OBJECT);

				if (instanceObject == objectToFollowReference)
					{
					instanceToFollow = instance;
					break;
					}
				}
			}

		// Properties of the view
		Point viewPosition = new Point(0,0);
		int viewWidth = (Integer) view.properties.get(PView.VIEW_W);
		int viewHeight = (Integer) view.properties.get(PView.VIEW_H);

		// If there is an instance to follow, use the instance properties for centering the view
		if (instanceToFollow != null)
			{
			// Get the instance position
			Point instancePosition = new Point(0,0);
			instancePosition.x = (Integer) instanceToFollow.properties.get(PInstance.X);
			instancePosition.y = (Integer) instanceToFollow.properties.get(PInstance.Y);

			viewPosition.x = instancePosition.x - viewWidth / 2;
			viewPosition.y = instancePosition.y - viewHeight / 2;

			// Set this new location into the view properties
			view.properties.put(PView.OBJECT_FOLLOWING_X,viewPosition.x);
			view.properties.put(PView.OBJECT_FOLLOWING_Y,viewPosition.y);
			}
		else
			{
			// Get the properties of the view
			viewPosition.x = view.properties.get(PView.VIEW_X);
			viewPosition.y = view.properties.get(PView.VIEW_Y);

			view.properties.put(PView.OBJECT_FOLLOWING_X,-1);
			view.properties.put(PView.OBJECT_FOLLOWING_Y,-1);
			}

		centerObjectInViewport(viewPosition,viewWidth,viewHeight,instanceToFollow);
		}

	// if an item of a listbox has been selected
	public void valueChanged(ListSelectionEvent e)
		{
		if (e.getValueIsAdjusting()) return;

		if (e.getSource() == oList) fireObjUpdate();
		if (e.getSource() == tList) fireTileUpdate();
		if (e.getSource() == bList) fireBackUpdate();
		if (e.getSource() == vList)
			{
			fireViewUpdate();
			showSelectedView();
			}
		}

	public CodeFrame openCodeFrame(CodeHolder code, String titleFormat, Object titleArg)
		{
		CodeFrame frame = codeFrames.get(code);
		if (frame == null)
			{
			frame = new CodeFrame(code,titleFormat,titleArg);
			codeFrames.put(code,frame);
			frame.addInternalFrameListener(new InternalFrameAdapter()
				{
					@Override
					public void internalFrameClosed(InternalFrameEvent e)
						{
						CodeFrame f = ((CodeFrame) e.getSource());
						codeFrames.remove(f.codeHolder);
						f.removeInternalFrameListener(this);
						}
				});
			LGM.mdi.add(frame);
			LGM.mdi.addZChild(this,frame);
			frame.toTop();
			}
		else
			frame.toTop();

		return frame;
		}

	public CodeFrame openInstanceCodeFrame(Instance inst)
		{
		return openCodeFrame(inst,Messages.getString("RoomFrame.TITLE_FORMAT_CREATION"),
				Messages.format("RoomFrame.INSTANCE",inst.properties.get(PInstance.ID)));
		}

	public CodeFrame openInstanceCodeFrame(int id, boolean select)
		{
		Instance inst = findInstance(id,select);
		if (inst != null)
			{
			return openCodeFrame(inst,Messages.getString("RoomFrame.TITLE_FORMAT_CREATION"),
					Messages.format("RoomFrame.INSTANCE",inst.properties.get(PInstance.ID)));
			}
		return null;
		}

	public CodeFrame openRoomCreationCode()
		{
		return openCodeFrame(res,Messages.getString("RoomFrame.TITLE_FORMAT_CREATION"),res.getName()); //$NON-NLS-1$
		}

	public Instance findInstance(int id, boolean select)
		{
		ListModel<Instance> model = oList.getModel();

		for (int i = 0; i < model.getSize(); i++)
			{
			Instance inst = model.getElementAt(i);
			if (inst.getID() == id)
				{
				if (select)
					{
					oList.setSelectedIndex(i);
					}
				return inst;
				}
			}
		return null;
		}

	@Override
	public void removeUpdate(DocumentEvent e)
		{
		CodeFrame f = codeFrames.get(res);
		if (f != null) f.setTitleFormatArg(name.getText());
		super.removeUpdate(e);
		}

	@Override
	public void insertUpdate(DocumentEvent e)
		{
		CodeFrame f = codeFrames.get(res);
		if (f != null) f.setTitleFormatArg(name.getText());
		super.insertUpdate(e);
		}

	@Override
	public void dispose()
		{
		Piece selectedPiece = editor.getSelectedPiece();

		// If there is a selected piece, deselect it
		if (selectedPiece != null) selectedPiece.setSelected(false);

		super.dispose();
		for (CodeFrame cf : codeFrames.values())
			cf.dispose();
		// XXX: These components could still be referenced by InputContext or similar.
		// Removing their references to this frame is therefore necessary in order to ensure
		// garbage collection.
		oNew.removeActionListener(this);
		oList.removeListSelectionListener(this);
		addObjectButton.removeActionListener(this);
		deleteObjectButton.removeActionListener(this);
		oCreationCode.removeActionListener(this);
		sCreationCode.removeActionListener(this);
		showButton.removeActionListener(this);
		taSource.removeActionListener(this);
		tList.removeListSelectionListener(this);
		deleteTileButton.removeActionListener(this);
		bList.removeListSelectionListener(this);
		vList.removeListSelectionListener(this);
		editorPane.setViewport(null);
		setLayout(null);
		}

	public void updated(UpdateEvent e)
		{
		if (e.source == res.instanceUpdateSource)
			oList.setPrototypeCellValue(null);
		else if (e.source == res.tileUpdateSource) tList.setPrototypeCellValue(null);
		}

	private void bdvListUpdate(boolean isBgDef, UpdateSource s, boolean v)
		{
		int ls = (isBgDef ? res.backgroundDefs : res.views).size();
		for (int i = 0; i < ls; i++)
			{
			UpdateSource s2 = (isBgDef ? res.backgroundDefs.get(i).properties
					: res.views.get(i).properties).updateSource;
			if (s2 != s) continue;
			JList<?> l = isBgDef ? bList : vList;
			JLabel ll = (JLabel) l.getModel().getElementAt(i);
			ll.setFont(ll.getFont().deriveFont(v ? Font.BOLD : Font.PLAIN));
			l.setPrototypeCellValue(null);
			break;
			}
		}

	private class BgDefPropertyListener extends PropertyUpdateListener<PBackgroundDef>
		{
		@Override
		public void updated(PropertyUpdateEvent<PBackgroundDef> e)
			{
			if (e.key == PBackgroundDef.VISIBLE) bdvListUpdate(true,e.source,(Boolean) e.map.get(e.key));
			}
		}

	private class ViewPropertyListener extends PropertyUpdateListener<PView>
		{
		@Override
		public void updated(PropertyUpdateEvent<PView> e)
			{
			if (e.key == PView.VISIBLE) bdvListUpdate(false,e.source,(Boolean) e.map.get(e.key));

			// If the 'Object following' object has been changed, update the display of the view
			if (e.key == PView.OBJECT || e.key == PView.VISIBLE) showSelectedView();

			// If we are modifying the view dimension
			if (e.key == PView.VIEW_W || e.key == PView.VIEW_H || e.key == PView.BORDER_H
					|| e.key == PView.BORDER_V)
				{
				// Get the selected view
				View view = res.views.get(vList.getSelectedIndex());

				// If there is 'Object following' object for the selected view, update the display of the view
				if (view.properties.get(PView.OBJECT) != null) showSelectedView();
				}
			}
		}

	// When a resource has been updated, reset the undo manager
	public void resetUndoManager()
		{
		undoManager.discardAllEdits();
		refreshUndoRedoButtons();
		}

	/**
	* An undo/redo adapter. The adapter is notified when an undo edit occur(e.g. add or remove from the list)
	* The adapter extract the edit from the event, add it to the UndoManager, and refresh the GUI
	*/

	private class UndoAdapter implements UndoableEditListener
		{
		public void undoableEditHappened(UndoableEditEvent evt)
			{
			UndoableEdit edit = evt.getEdit();
			undoManager.addEdit(edit);
			refreshUndoRedoButtons();
			}
		}

	/**
	* This method is called after each undoable operation
	* in order to refresh the presentation state of the undo/redo GUI
	*/

	public void refreshUndoRedoButtons()
		{
		// refresh undo
		undo.setEnabled(undoManager.canUndo());

		// refresh redo
		redo.setEnabled(undoManager.canRedo());
		}

	// When a text field related to a piece property gains the focus
	public void focusGained(FocusEvent event)
		{
		pieceOriginalPosition = null;
		pieceOriginalScale = null;
		pieceOriginalRotation = null;
		pieceOriginalAlpha = null;
		selectedPiece = null;

		// If we are modifying objects
		if (event.getSource() == objectName || event.getSource() == objectHorizontalPosition
				|| event.getSource() == objectVerticalPosition || event.getSource() == objectScaleX
				|| event.getSource() == objectScaleY || event.getSource() == objectRotation
				|| event.getSource() == objectAlpha)
			{
			// If no object is selected, return
			int selectedIndex = oList.getSelectedIndex();
			if (selectedIndex == -1) return;

			// Save the selected instance
			selectedPiece = oList.getSelectedValue();

			// If we are modifying the name, save it for the undo
			if (event.getSource() == objectName)
				{
				pieceOriginalName = selectedPiece.getName();
				return;
				}

			// If we are modifying the position, save it for the undo
			if (event.getSource() == objectHorizontalPosition
					|| event.getSource() == objectVerticalPosition)
				{
				pieceOriginalPosition = new Point(selectedPiece.getPosition());
				return;
				}

			// If we are modifying the scale, save it for the undo
			if (event.getSource() == objectScaleX || event.getSource() == objectScaleY)
				{
				Point2D newScale = selectedPiece.getScale();
				pieceOriginalScale = new Point2D.Double(newScale.getX(),newScale.getY());
				return;
				}

			// If we are modifying the rotation, save it for the undo
			if (event.getSource() == objectRotation)
				{
				pieceOriginalRotation = new Double(selectedPiece.getRotation());
				return;
				}

			// If we are modifying the alpha, save it for the undo
			if (event.getSource() == objectAlpha)
				{
				pieceOriginalAlpha = new Integer(selectedPiece.getAlpha());
				return;
				}

			}
		// We are modifying tiles
		else
			{
			// If no tile is selected, return
			int selectedIndex = tList.getSelectedIndex();
			if (selectedIndex == -1) return;

			// Save the selected tile
			selectedPiece = tList.getSelectedValue();

			// Save the position of the tile for the undo
			pieceOriginalPosition = new Point(selectedPiece.getPosition());
			}
		}

	// When a text field related to a piece position has lost the focus
	public void focusLost(FocusEvent event)
		{
		processFocusLost();
		}

	// Save the position of a piece for the undo
	public void processFocusLost()
		{
		if (selectedPiece == null) return;

		// If we are modifying objects
		if (selectedPiece instanceof Instance)
			{
			// If no object is selected, return
			int selectedIndex = oList.getSelectedIndex();
			if (selectedIndex == -1) return;

			// If we have changed the name
			if (pieceOriginalName != null)
				{
				// Get the new name of the object
				String objectNewName = new String(selectedPiece.getName());

				// If the rotation of the object has been changed
				if (objectNewName != pieceOriginalName)
					{
					// Record the effect of rotating an object for the undo
					UndoableEdit edit = new ModifyPieceInstance(this,selectedPiece,pieceOriginalName,
							objectNewName);
					// notify the listeners
					undoSupport.postEdit(edit);
					}
				}

			// If we have changed the position
			if (pieceOriginalPosition != null)
				{
				// Get the new position of the object
				Point objectNewPosition = new Point(selectedPiece.getPosition());

				// If the position of the object has been changed
				if (!objectNewPosition.equals(pieceOriginalPosition))
					{
					// Record the effect of moving an object for the undo
					UndoableEdit edit = new ModifyPieceInstance(this,selectedPiece,pieceOriginalPosition,
							objectNewPosition);
					// notify the listeners
					undoSupport.postEdit(edit);
					}
				}

			// If we have changed the scale
			if (pieceOriginalScale != null)
				{
				// Get the new scale of the object
				Point2D objectNewScale = selectedPiece.getScale();

				// If the scale of the object has been modified
				if (!objectNewScale.equals(pieceOriginalScale))
					{
					// Record the effect of modifying the scale an object for the undo
					UndoableEdit edit = new ModifyPieceInstance(this,selectedPiece,pieceOriginalScale,
							new Point2D.Double(objectNewScale.getX(),objectNewScale.getY()));
					// notify the listeners
					undoSupport.postEdit(edit);
					}
				}

			// If we have changed the rotation
			if (pieceOriginalRotation != null)
				{
				// Get the new rotation of the object
				Double objectNewRotation = new Double(selectedPiece.getRotation());

				// If the rotation of the object has been changed
				if (objectNewRotation != pieceOriginalRotation)
					{
					// Record the effect of rotating an object for the undo
					UndoableEdit edit = new ModifyPieceInstance(this,selectedPiece,pieceOriginalRotation,
							objectNewRotation);
					// notify the listeners
					undoSupport.postEdit(edit);
					}
				}

			// If we have changed the alpha value
			if (pieceOriginalAlpha != null)
				{
				// Get the new alpha of the object
				Integer objectNewAlpha = new Integer(selectedPiece.getAlpha());

				// If the alpha value of the object has been changed
				if (objectNewAlpha != pieceOriginalAlpha)
					{
					// Record the effect of modifying the alpha value of an object for the undo
					UndoableEdit edit = new ModifyPieceInstance(this,selectedPiece,pieceOriginalAlpha,
							objectNewAlpha);
					// notify the listeners
					undoSupport.postEdit(edit);
					}
				}
			}
		// We are modifying tiles
		else
			{
			// If no tile is selected, return
			int selectedIndex = tList.getSelectedIndex();
			if (selectedIndex == -1) return;

			// Get the new position of the tile
			Point tileNewPosition = new Point(selectedPiece.getPosition());

			// If the position of the tile has been changed
			if (!tileNewPosition.equals(pieceOriginalPosition))
				{
				// Record the effect of moving an tile for the undo
				UndoableEdit edit = new ModifyPieceInstance(this,selectedPiece,pieceOriginalPosition,
						tileNewPosition);
				// notify the listeners
				undoSupport.postEdit(edit);
				}

			}

		selectedPiece = null;
		}

	// When a new tab is selected
	public void stateChanged(ChangeEvent event)
		{
		// If the views tab is selected, always display the views
		if (tabs.getSelectedIndex() == Room.TAB_VIEWS)
			{
			showSelectedView();
			editor.roomVisual.setViewsVisible(true);
			}
		else
			{
			editor.roomVisual.setViewsVisible(false);
			}
		}

	}
