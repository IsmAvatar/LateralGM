/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class PrefsStore
	{
	private PrefsStore()
		{
		}

	private static final Preferences PREFS = Preferences.userRoot().node("/org/lateralgm");

	public static void resetToDefaults() {
		try
		{
			PREFS.clear();
		}
		catch (BackingStoreException e)
		{
			LGM.showDefaultExceptionHandler(e);
		}
	}

	public static void clearRecentFiles()
		{
		PREFS.remove("FILE_RECENT");
		LGM.menuBar.setRecentMenuEnabled(false);
		}

	//TODO: These are now delimited by a tab instead of a space to allow spaces in recent file paths. - Robert
	public static ArrayList<String> getRecentFiles()
		{
		String value = PREFS.get("FILE_RECENT",null);
		if (value == null) return new ArrayList<String>(0);
		String[] array = value.split(" ");
		ArrayList<String> list = new ArrayList<String>(array.length);
		for (String name : array)
			list.add(Util.urlDecode(name));
		return list;

		}

	public static void addRecentFile(String name)
		{
		int maxcount = PREFS.getInt("FILE_RECENT_COUNT",10);
		ArrayList<String> oldList = getRecentFiles();
		oldList.remove(name);
		String newList;
		newList = Util.urlEncode(name);
		for (int i = 0; i + 1 < maxcount && i < oldList.size(); i++)
			newList += " " + Util.urlEncode(oldList.get(i));
		PREFS.put("FILE_RECENT",newList);
		}

	public static Rectangle getWindowBounds(Rectangle def)
		{
		return Util.stringToRectangle(PREFS.get("WINDOW_BOUNDS",null),def);
		}

	public static void setWindowBounds(Rectangle r)
		{
		PREFS.put("WINDOW_BOUNDS",Util.rectangleToString(r));
		}

	public static boolean getWindowMaximized()
		{
		return PREFS.getBoolean("WINDOW_MAXIMIZED",true);
		}

	public static void setWindowMaximized(boolean b)
		{
		PREFS.putBoolean("WINDOW_MAXIMIZED",b);
		}

	public static void setIconPack(String s)
		{
		PREFS.put("iconPack",s);
		Prefs.iconPack = s;
		}

	public static void setUserLibraryPath(String s)
		{
		PREFS.put("userLibraryPath",s);
		Prefs.userLibraryPath = s;
		}

	public static void setSpriteExt(String s)
		{
		PREFS.put("externalSpriteExtension",s);
		Prefs.externalSpriteExtension = s;
		}

	public static void setBackgroundExt(String s)
		{
		PREFS.put("externalBackgroundExtension",s);
		Prefs.externalBackgroundExtension = s;
		}

	public static void setScriptExt(String s)
		{
		PREFS.put("externalScriptExtension",s);
		Prefs.externalScriptExtension = s;
		}

	public static void setBackgroundEditorCommand(String s)
		{
		PREFS.put("externalBackgroundEditorCommand",s);
		Prefs.externalBackgroundEditorCommand = s;
		}

	public static void setSpriteEditorCommand(String s)
		{
		PREFS.put("externalSpriteEditorCommand",s);
		Prefs.externalSpriteEditorCommand = s;
		}

	public static void setScriptEditorCommand(String s)
		{
		PREFS.put("externalScriptEditorCommand",s);
		Prefs.externalScriptEditorCommand = s;
		}

	public static void setSoundEditorCommand(String s)
		{
		PREFS.put("externalSoundEditorCommand",s);
		Prefs.externalSoundEditorCommand = s;
		}

	public static void setDecorateWindowBorders(boolean selected)
		{
		PREFS.putBoolean("decorateWindowBorders",selected);
		Prefs.decorateWindowBorders = selected;
		}

	public static void setAntialiasControlFont(String s)
		{
		PREFS.put("antialiasControlFont",s);
		Prefs.antialiasControlFont = s;
		}

	public static void setDirect3DAcceleration(String s)
		{
		PREFS.put("direct3DAcceleration",s);
		Prefs.direct3DAcceleration = s;
		}

	public static void setOpenGLAcceleration(String s)
		{
		PREFS.put("openGLAcceleration",s);
		Prefs.openGLAcceleration = s;
		}

	public static void setPrefixes(String s)
		{
		PREFS.put("prefixes",s);
		Prefs.createPrefixes(s);
		}

	public static void setIconPath(String s)
		{
		PREFS.put("iconPath",s);
		Prefs.iconPath = s;
		}

	public static void setSwingTheme(String s)
		{
		PREFS.put("swingTheme",s);
		Prefs.swingTheme = s;
		}

	public static void setSwingThemePath(String s)
		{
		PREFS.put("swingThemePath",s);
		Prefs.swingThemePath = s;
		}

	public static void setLocale(Locale s)
		{
		PREFS.put("localeTag",s.toLanguageTag());
		Prefs.locale = s;
		}

	public static void setDNDEnabled(boolean selected)
		{
		PREFS.putBoolean("enableDragAndDrop",selected);
		Prefs.enableDragAndDrop = selected;
		}

	public static void setDocumentationURI(String uri)
		{
		PREFS.put("documentationURI",uri);
		Prefs.documentationURI = uri;
		}

	public static void setWebsiteURI(String uri)
		{
		PREFS.put("websiteURI",uri);
		Prefs.websiteURI = uri;
		}

	public static void setCommunityURI(String uri)
		{
		PREFS.put("communityURI",uri);
		Prefs.communityURI = uri;
		}

	public static void setIssueURI(String uri)
		{
		PREFS.put("issueURI",uri);
		Prefs.issueURI = uri;
		}

	public static void setShowTreeFilter(boolean selected)
		{
		PREFS.putBoolean("showTreeFilter",selected);
		Prefs.showTreeFilter = selected;
		}

	public static void setExtraNodes(boolean selected)
		{
		PREFS.putBoolean("extraNodes",selected);
		Prefs.extraNodes = selected;
		}

	public static void setDockEventPanel(boolean selected)
		{
		PREFS.putBoolean("dockEventPanel",selected);
		Prefs.dockEventPanel = selected;
		}

	public static void setRightOrientation(boolean selected)
		{
		PREFS.putBoolean("rightOrientation",selected);
		Prefs.rightOrientation = selected;
		}

	public static void setUndoHistorySize(int undoHistorySize)
		{
		PREFS.putInt("undoHistorySize",undoHistorySize);
		Prefs.undoHistorySize = undoHistorySize;
		}

	public static void setFilledRectangleForViews(boolean selected)
		{
		PREFS.putBoolean("filledRectangleForViews",selected);
		Prefs.useFilledRectangleForViews = selected;
		}

	public static void setInvertedColorForViews(boolean selected)
		{
		PREFS.putBoolean("invertedColorForViews",selected);
		Prefs.useInvertedColorForViews = selected;
		}

	public static void setImagePreviewBackgroundColor(int color)
		{
		PREFS.putInt("imagePreviewBackgroundColor",color);
		Prefs.imagePreviewBackgroundColor = color;
		}

	public static void setImagePreviewForegroundColor(int color)
		{
		PREFS.putInt("imagePreviewForegroundColor",color);
		Prefs.imagePreviewForegroundColor = color;
		}

	public static void setHighlightMatchCountBackground(boolean selected)
		{
		PREFS.putBoolean("highlightMatchCountBackground",selected);
		Prefs.highlightMatchCountBackground = selected;
		}

	public static void setHighlightMatchCountForeground(boolean selected)
		{
		PREFS.putBoolean("highlightMatchCountForeground",selected);
		Prefs.highlightMatchCountForeground = selected;
		}

	public static void setMatchCountBackgroundColor(int color)
		{
		PREFS.putInt("matchCountBackgroundColor",color);
		Prefs.matchCountBackgroundColor = color;
		}

	public static void setMatchCountForegroundColor(int color)
		{
		PREFS.putInt("matchCountForegroundColor",color);
		Prefs.matchCountForegroundColor = color;
		}

	public static void setHighlightResultMatchBackground(boolean selected)
		{
		PREFS.putBoolean("highlightResultMatchBackground",selected);
		Prefs.highlightResultMatchBackground = selected;
		}

	public static void setHighlightResultMatchForeground(boolean selected)
		{
		PREFS.putBoolean("highlightResultMatchForeground",selected);
		Prefs.highlightResultMatchForeground = selected;
		}

	public static void setResultMatchBackgroundColor(int color)
		{
		PREFS.putInt("resultMatchBackgroundColor",color);
		Prefs.resultMatchBackgroundColor = color;
		}

	public static void setResultMatchForegroundColor(int color)
		{
		PREFS.putInt("resultMatchForegroundColor",color);
		Prefs.resultMatchForegroundColor = color;
		}

	public static void setViewInsideColor(int viewInsideColor)
		{
		PREFS.putInt("viewInsideColor",viewInsideColor);
		Prefs.viewInsideColor = viewInsideColor;
		}

	public static void setViewOutsideColor(int viewOutsideColor)
		{
		PREFS.putInt("viewOutsideColor",viewOutsideColor);
		Prefs.viewOutsideColor = viewOutsideColor;
		}

	public static void setFilledRectangleForSelection(boolean selected)
		{
		PREFS.putBoolean("filledRectangleForSelection",selected);
		Prefs.useFilledRectangleForSelection = selected;
		}

	public static void setInvertedColorForSelection(boolean selected)
		{
		PREFS.putBoolean("invertedColorForSelection",selected);
		Prefs.useInvertedColorForSelection = selected;
		}

	public static void setSelectionInsideColor(int selectionInsideColor)
		{
		PREFS.putInt("selectionInsideColor",selectionInsideColor);
		Prefs.selectionInsideColor = selectionInsideColor;
		}

	public static void setSelectionOutsideColor(int selectionOutsideColor)
		{
		PREFS.putInt("selectionOutsideColor",selectionOutsideColor);
		Prefs.selectionOutsideColor = selectionOutsideColor;
		}

	public static void setFilledRectangleForMultipleSelection(boolean selected)
		{
		PREFS.putBoolean("filledRectangleForMultipleSelection",selected);
		Prefs.useFilledRectangleForMultipleSelection = selected;
		}

	public static void setInvertedColorForMultipleSelection(boolean selected)
		{
		PREFS.putBoolean("invertedColorForMultipleSelection",selected);
		Prefs.useInvertedColorForMultipleSelection = selected;
		}

	public static void setMultipleSelectionInsideColor(int selectionInsideColor)
		{
		PREFS.putInt("multipleSelectionInsideColor",selectionInsideColor);
		Prefs.multipleSelectionInsideColor = selectionInsideColor;
		}

	public static void setMultipleSelectionOutsideColor(int selectionOutsideColor)
		{
		PREFS.putInt("multipleSelectionOutsideColor",selectionOutsideColor);
		Prefs.multipleSelectionOutsideColor = selectionOutsideColor;
		}

	public static int getNumberOfBackups()
		{
		return PREFS.getInt("FILE_BACKUP_COUNT",1);
		}
	}
