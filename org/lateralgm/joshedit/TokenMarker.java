/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.util.ArrayList;

import org.lateralgm.joshedit.JoshText.LineChangeListener;

/**
 * An interface for getting marker styles for a given line.
 */
public interface TokenMarker extends LineChangeListener {
	/**
	 * A storage class for information about a given marked block.
	 * 
	 * @author Josh Ventura
	 */
	class TokenMarkerInfo {
		/** The font style flags to use, such as BOLD or ITALIC. */
		int fontStyle;
		/** The font color to use. */
		Color color;
		/** Where this block ends. */
		public int endPos;
		/** Where this block starts. */
		public int startPos;
		/** A unique hash for open block types. Implementation specific. */
		public int blockHash;

		/**
		 * @param fs
		 *            The font style to use when rendering this block.
		 * @param col
		 *            The font color to use when rendering this block.
		 * @param start
		 *            The starting position of this block on the current line.
		 * @param end
		 *            The ending position of this block on the current line.
		 * @param hash
		 *            The block hash to assign.
		 */
		public TokenMarkerInfo(int fs, Color col, int start, int end, int hash) {
			fontStyle = fs;
			color = col;
			startPos = start;
			endPos = end;
			blockHash = hash;
		}

		/**
		 * Convenience constructor for simple information.
		 * 
		 * @param fs
		 *            The font style to use when rendering this block.
		 * @param col
		 *            The font color to use when rendering this block.
		 */
		public TokenMarkerInfo(int fs, Color col) {
			this(fs, col, 0, 0, 0);
		}
	}

	/**
	 * Return an array of line mark colors with their positions
	 * 
	 * @param jline
	 *            The JoshText line to parse.
	 * @return An array of line mark colors with their positions
	 */
	ArrayList<TokenMarkerInfo> getStyles(Line jline);

	/**
	 * Format the code according to some specification in some grammar.
	 * 
	 * @param code
	 *            The code to format.
	 */
	void formatCode(Code code);
}
