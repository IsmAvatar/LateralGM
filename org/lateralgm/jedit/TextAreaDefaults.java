/*
 * TextAreaDefaults.java - Encapsulates default values for various settings
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */
package org.lateralgm.jedit;

import java.awt.Color;

import javax.swing.JPopupMenu;

/**
 * Encapsulates default settings for a text area. This can be passed
 * to the constructor once the necessary fields have been filled out.
 * The advantage of doing this over calling lots of set() methods after
 * creating the text area is that this method is faster.
 */
public class TextAreaDefaults
	{
	private static TextAreaDefaults defaults;

	public InputHandler inputHandler;
	public SyntaxDocument document;
	public boolean editable;

	public boolean caretVisible;
	public boolean caretBlinks;
	public boolean blockCaret;
	public int electricScroll;

	public int cols;
	public int rows;
	public SyntaxStyle[] styles;
	public Color caretColor;
	public Color selectionColor;
	public Color lineHighlightColor;
	public boolean lineHighlight;
	public Color bracketHighlightColor;
	public boolean bracketHighlight;
	public Color eolMarkerColor;
	public boolean eolMarkers;
	public boolean paintInvalid;

	public JPopupMenu popup;

	/**
	 * Returns a new TextAreaDefaults object with the default values filled
	 * in.
	 */
	public static TextAreaDefaults getDefaults()
		{
		if (defaults == null)
			{
			TextAreaDefaults newDefaults = new TextAreaDefaults();

			newDefaults.inputHandler = new DefaultInputHandler();
			newDefaults.inputHandler.addDefaultKeyBindings();
			newDefaults.document = new SyntaxDocument();
			newDefaults.editable = true;

			newDefaults.caretVisible = true;
			newDefaults.caretBlinks = true;
			newDefaults.electricScroll = 3;

			newDefaults.cols = 80;
			newDefaults.rows = 25;
			newDefaults.styles = SyntaxUtilities.getDefaultSyntaxStyles();
			newDefaults.caretColor = Color.red;
			newDefaults.selectionColor = new Color(0xccccff);
			newDefaults.lineHighlightColor = new Color(0xe0e0e0);
			newDefaults.lineHighlight = true;
			newDefaults.bracketHighlightColor = Color.black;
			newDefaults.bracketHighlight = true;
			newDefaults.eolMarkerColor = new Color(0x009999);
			newDefaults.eolMarkers = true;
			newDefaults.paintInvalid = true;
			defaults = newDefaults;
			}

		return defaults;
		}
	}
