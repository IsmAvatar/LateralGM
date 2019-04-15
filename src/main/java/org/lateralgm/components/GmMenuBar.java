/*
 * Copyright (C) 2006, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyrigth (C) 2013 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;

public class GmMenuBar extends JMenuBar
	{
	private static final long serialVersionUID = 1L;
	private List<JMenuItem> recentFiles = new ArrayList<JMenuItem>();
	private GmMenu recentMenu;

	public static final void setTextAndAlt(JMenuItem item, String input)
		{
		Matcher m = Pattern.compile("\t+([^\\s])$").matcher(input); //$NON-NLS-1$
		if (m.find())
			{
			int alt = m.group(1).toUpperCase(Locale.ENGLISH).charAt(0);
			item.setMnemonic(alt);
			item.setText(input.substring(0,m.start()));
			}
		else
			{
			item.setMnemonic(-1);
			item.setText(input);
			}
		}

	public void setRecentMenuEnabled(boolean enabled)
		{
		recentMenu.setEnabled(enabled);
		if (!enabled)
			{
			recentMenu.removeAll();
			}
		}

	public void updateRecentFiles()
		{
		List<String> recentList = PrefsStore.getRecentFiles();
		if (recentList.size() > 0)
			{
			setRecentMenuEnabled(true);
			}
		else
			{
			setRecentMenuEnabled(false);
			return;
			}
		recentMenu.removeAll();
		recentFiles.clear();
		for (String recentStr : recentList)
			{
			try
				{
				URI uri = new URI(recentStr);
				JMenuItem item;
				String number = Integer.toString(recentFiles.size() + 1);
				try
					{
					File file = new File(uri).getAbsoluteFile();
					item = new JMenuItem(String.format("%s %s  [%s]",number,file.getName(),file.getParent()),
							number.codePointAt(number.length() - 1));
					}
				catch (IllegalArgumentException e)
					{
					item = new JMenuItem(String.format("%s %s",number,uri),number.codePointAt(number.length() - 1));
					}
				item.setActionCommand("GmMenuBar.OPENRECENT " + recentStr); //$NON-NLS-1$
				item.addActionListener(Listener.getInstance());
				recentMenu.insert(item,recentFiles.size());
				recentFiles.add(item);
				}
			catch (URISyntaxException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			}
		recentMenu.addSeparator();
		recentMenu.addItem("GmMenuBar.CLEARRECENT");
		}

	protected static final Map<Class<? extends Resource<?,?>>,Character> MNEMONICS;
	static
		{
		MNEMONICS = new HashMap<Class<? extends Resource<?,?>>,Character>();
		for (Entry<String,Class<? extends Resource<?,?>>> k : Resource.kindsByName3.entrySet())
			MNEMONICS.put(k.getValue(),
					Messages.getString("GmMenuBar.MNEMONIC_" + k.getKey()).toUpperCase().charAt(0)); //$NON-NLS-1$
		}

	public GmMenuBar()
		{
		GmMenu menu = new GmMenu(Messages.getString("GmMenuBar.MENU_FILE")); //$NON-NLS-1$
		add(menu);

		menu.addItem("GmMenuBar.NEW"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.OPEN"); //$NON-NLS-1$
		recentMenu = (GmMenu) menu.addMenu("GmMenuBar.RECENTFILES");
		menu.addItem("GmMenuBar.SAVE"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.SAVEAS"); //$NON-NLS-1$
		menu.addSeparator();
		// JCheckBoxMenuItem check = new JCheckBoxMenuItem();
		// setTextAndAlt(check,Messages.getString("GmMenuBar.ADVANCED")); //$NON-NLS-1$
		// menu.add(check);
		menu.addItem("GmMenuBar.PREFERENCES"); //$NON-NLS-1$
		menu.addSeparator();
		menu.addItem("GmMenuBar.EXIT"); //$NON-NLS-1$
		updateRecentFiles();

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_EDIT")); //$NON-NLS-1$
		add(menu);

		GmMenu subIns = new GmMenu(Messages.getString("GmMenuBar.MENU_INSERT")); //$NON-NLS-1$
		menu.add(subIns);
		subIns.addItem("GmMenuBar.INSERT_GROUP"); //$NON-NLS-1$
		subIns.addSeparator();

		GmMenu subAdd = new GmMenu(Messages.getString("GmMenuBar.MENU_ADD")); //$NON-NLS-1$
		menu.add(subAdd);
		subAdd.addItem("GmMenuBar.ADD_GROUP"); //$NON-NLS-1$
		subAdd.addSeparator();

		for (final Class<? extends Resource<?,?>> k : Resource.kinds)
			if (InstantiableResource.class.isAssignableFrom(k))
				{
				String nodeName = Resource.kindNames.get(k);
				//				subIns.addItem("GmMenuBar.INSERT_" + name3); //$NON-NLS-1$
				//				subAdd.addItem("GmMenuBar.ADD_" + name3); //$NON-NLS-1$
				Icon icon = ResNode.ICON.get(k);
				int mnemonic = MNEMONICS.get(k);
				String insNodeName = Messages.format("GmMenuBar.INSERT",nodeName); //$NON-NLS-1$
				String addNodeName = Messages.format("GmMenuBar.ADD",nodeName); //$NON-NLS-1$

				JMenuItem item = new JMenuItem(insNodeName,icon);
				if (mnemonic != '!') item.setMnemonic(mnemonic);
				item.addActionListener(new Listener.ResourceAdder(true,k));
				subIns.add(item);

				item = new JMenuItem(addNodeName,icon);
				if (mnemonic != '!') item.setMnemonic(mnemonic);
				item.addActionListener(new Listener.ResourceAdder(false,k));
				subAdd.add(item);
				}

		menu.addSeparator();
		menu.addItem("GmMenuBar.RENAME"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.DELETE"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.DUPLICATE"); //$NON-NLS-1$
		menu.addSeparator();
		menu.addItem("GmMenuBar.PROPERTIES"); //$NON-NLS-1$

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_RESOURCES")); //$NON-NLS-1$
		add(menu);

		menu.addItem("GmMenuBar.CHECKNAMES"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.CHECKIDS"); //$NON-NLS-1$
		menu.addSeparator();
		menu.addItem("GmMenuBar.FIND"); //$NON-NLS-1$
		menu.addSeparator();
		menu.addItem("GmMenuBar.EXPAND"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.COLLAPSE"); //$NON-NLS-1$

		add(LGM.mdi.getMenu());

		menu = new GmMenu(Messages.getString("GmMenuBar.MENU_HELP")); //$NON-NLS-1$
		add(menu);
		menu.addItem("GmMenuBar.DOCUMENTATION"); //$NON-NLS-1$
		menu.addSeparator();
		menu.addItem("GmMenuBar.WEBSITE"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.COMMUNITY"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.ISSUE"); //$NON-NLS-1$
		menu.addSeparator();
		menu.addItem("GmMenuBar.EXPLORELATERALGM"); //$NON-NLS-1$
		menu.addItem("GmMenuBar.EXPLOREPROJECT"); //$NON-NLS-1$
		menu.addSeparator();
		menu.addItem("GmMenuBar.ABOUT"); //$NON-NLS-1$
		}
	}
