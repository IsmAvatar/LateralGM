/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.lateralgm.components.ColorSelect;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.library.LibArgument;

public class Argument
	{
	public static final byte ARG_EXPRESSION = 0;
	public static final byte ARG_STRING = 1;
	public static final byte ARG_BOTH = 2;
	public static final byte ARG_BOOLEAN = 3;
	public static final byte ARG_MENU = 4;
	public static final byte ARG_COLOR = 13;
	@Deprecated
	public static final byte ARG_FONTSTRING = 15;
	public static final byte ARG_SPRITE = 5;
	public static final byte ARG_SOUND = 6;
	public static final byte ARG_BACKGROUND = 7;
	public static final byte ARG_PATH = 8;
	public static final byte ARG_SCRIPT = 9;
	public static final byte ARG_GMOBJECT = 10;
	public static final byte ARG_ROOM = 11;
	public static final byte ARG_FONT = 12;
	public static final byte ARG_TIMELINE = 14;

	public byte kind = ARG_EXPRESSION;
	public String val = "";
	public ResId res = null; // for references to Resources

	private JComponent editor;

	public Argument(byte kind, String val, ResId res)
		{
		this.kind = kind;
		this.val = val;
		this.res = res;
		}

	public Argument()
		{
		}

	public static byte getResourceKind(byte argumentKind)
		{
		switch (argumentKind)
			{
			case ARG_SPRITE:
				return Resource.SPRITE;
			case ARG_SOUND:
				return Resource.SOUND;
			case ARG_BACKGROUND:
				return Resource.BACKGROUND;
			case ARG_PATH:
				return Resource.PATH;
			case ARG_SCRIPT:
				return Resource.SCRIPT;
			case ARG_GMOBJECT:
				return Resource.GMOBJECT;
			case ARG_ROOM:
				return Resource.ROOM;
			case ARG_FONT:
				return Resource.FONT;
			case ARG_TIMELINE:
				return Resource.TIMELINE;
			default:
				return 0;
			}
		}

	private String getNoSelectionString(byte resourceKind)
		{
		String key;
		switch (resourceKind)
			{
			case Resource.SPRITE:
				key = "Argument.SPRITE";
				break;
			case Resource.SOUND:
				key = "Argument.SOUND";
				break;
			case Resource.BACKGROUND:
				key = "Argument.BACKGROUND";
				break;
			case Resource.PATH:
				key = "Argument.PATH";
				break;
			case Resource.SCRIPT:
				key = "Argument.SCRIPT";
				break;
			case Resource.GMOBJECT:
				key = "Argument.OBJECT";
				break;
			case Resource.ROOM:
				key = "Argument.ROOM";
				break;
			case Resource.TIMELINE:
				key = "Argument.TIMELINE";
				break;
			default:
				key = "";
			}
		return String.format(Messages.getString("Argument.NO_SELECTION"),Messages.getString(key));
		}

	private JComponent makeEditor(LibArgument la)
		{
		switch (kind)
			{
			case ARG_BOOLEAN:
				final String[] sab = { "false","true" };
				return new JComboBox(sab);
			case ARG_MENU:
				if (la == null) return new JTextField(val);
				final String[] sam = la.menu.split("\\|"); //$NON-NLS-1$
				return new JComboBox(sam);
			case ARG_COLOR:
				return new ColorSelect(Util.convertGmColor(Integer.parseInt(val)));
			case ARG_SPRITE:
			case ARG_SOUND:
			case ARG_BACKGROUND:
			case ARG_PATH:
			case ARG_SCRIPT:
			case ARG_GMOBJECT:
			case ARG_ROOM:
			case ARG_FONT:
			case ARG_TIMELINE:
				byte rk = getResourceKind(kind);
				return new ResourceMenu(rk,getNoSelectionString(rk),120);
			default:
				return new JTextField(val);
			}
		}

	/**
	 * Gets a JComponent editor for this Argument. Defaults to raw JTextField.
	 * @param la - The corresponding LibArgument, used for Menus.
	 * May be null, but then a menu will default to JTextField.
	 * @return One of JButton, JComboBox, JColorChooser, ResourceMenu, or JTextField
	 */
	public JComponent getEditor(LibArgument la)
		{
		if (editor == null)
			{
			editor = makeEditor(la);
			discard();
			}
		return editor;
		}

	public String toString(LibArgument la)
		{
		byte rk = getResourceKind(kind);
		switch (kind)
			{
			case ARG_BOOLEAN:
				return Boolean.toString(val != "0");
			case ARG_MENU:
				String[] sam = la.menu.split("\\|");
				try
					{
					return sam[Integer.parseInt(val)];
					}
				catch (NumberFormatException nfe)
					{
					}
				catch (IndexOutOfBoundsException be)
					{
					}
				return val;
			case ARG_COLOR:
				try
					{
					return String.format("%06X",Integer.parseInt(val));
					}
				catch (NumberFormatException e)
					{
					}
				return val;
			default:
				if (rk <= 0)
					return val;
				else
					{
					try
						{
						return LGM.currentFile.getList(rk).get(res).getName();
						}
					catch (NullPointerException e)
						{
						}
					return "<none>";
					}
			}
		}

	/** Commits any changes in the JComponent editor to update this Argument. */
	public void commit()
		{
		if (editor instanceof JTextField)
			{
			val = ((JTextField) editor).getText();
			return;
			}
		if (editor instanceof JComboBox)
			{
			val = Integer.toString(((JComboBox) editor).getSelectedIndex());
			return;
			}
		if (editor instanceof ColorSelect)
			{
			val = Integer.toString(Util.getGmColor(((ColorSelect) editor).getSelectedColor()));
			}
		if (editor instanceof ResourceMenu)
			{
			Resource sel = ((ResourceMenu) editor).getSelected();
			if (sel == null)
				res = null;
			else
				res = sel.getId();
			return;
			}
		}

	public void discard()
		{
		if (editor instanceof JTextField)
			{
			((JTextField) editor).setText(val);
			}
		else if (editor instanceof JComboBox)
			{
			((JComboBox) editor).setSelectedIndex(Integer.parseInt(val));
			}
		else if (editor instanceof ColorSelect)
			{
			((ColorSelect) editor).setSelectedColor(Util.convertGmColor(Integer.parseInt(val)));
			}
		else if (editor instanceof ResourceMenu)
			{
			try
				{
				Resource s = LGM.currentFile.getList(getResourceKind(kind)).get(res);
				((ResourceMenu) editor).setSelected(s);
				}
			catch (NumberFormatException nfe)
				{
				}
			catch (NullPointerException npe)
				{
				}
			}
		}
	}
