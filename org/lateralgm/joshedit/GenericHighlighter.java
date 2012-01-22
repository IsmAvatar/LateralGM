/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lateralgm.joshedit.GenericHighlighter.SchemeInfo.SchemeType;
import org.lateralgm.joshedit.JoshText.LineChangeListener;
import org.lateralgm.joshedit.Line.LINE_ATTRIBS;

public class GenericHighlighter implements Highlighter,LineChangeListener
{
	private JoshText jt; // The owning JoshText
	private int line_count; // The number of lines last time we parsed--used to determine change type
	private int invalid_line = 0; // The index of the first invalid line, or -1 for all-clear

	// The index of the next valid line after an invalid line is determined
	// while parsing--the first line that matches up with the tags from the
	// previous line is considered valid.

	class BlockDescriptor
	{
		String name; // Human-readable name for this scheme.
		Pattern begin, end; // Begin and end are not regex. If end is null, EOL is used.
		boolean multiline; // True if this block type is allowed to span multiple lines. Can be true only if end is non-null.
		boolean escapeend; // True if we can escape the ending character. If end is null, this allows us to span multiple lines.
		char escapeChar; // The character used to escape things, or null if no escape is allowed.
		Color color; // The color with which this will be rendered, or NULL to use the default
		int fontStyle; // Font attributes (Font.BOLD, etc)

		public BlockDescriptor(String block_name, String begin_regex, String end_regex,
				boolean allow_multiline, boolean escape_endmarkers, char escape_char,
				Color highlight_color, int font_style)
		{
			name = block_name;
			begin = Pattern.compile(begin_regex);
			end = Pattern.compile(end_regex);
			multiline = allow_multiline;
			escapeend = escape_endmarkers;
			escapeChar = escape_char;
			color = highlight_color;
			fontStyle = font_style;
		}
	}

	ArrayList<BlockDescriptor> schemes = new ArrayList<BlockDescriptor>();

	public static class KeywordSet
	{
		String name; // The name of this group of keywords
		public Set<String> words; // A set of words highlighted according to this rule
		Color color; // The color with which this will be rendered, or NULL to use the default
		int fontStyle; // Font attributes (Font.BOLD, etc)

		public KeywordSet(String group_name, Color highlight_color, int font_style)
		{
			name = group_name;
			color = highlight_color;
			fontStyle = font_style;
			words = new HashSet<String>();
		}
	}

	public ArrayList<KeywordSet> hlKeywords = new ArrayList<KeywordSet>();

	class CharSymbolSet
	{
		String name; // The name of this group of characters
		Set<Character> chars; // A set of characters highlighted according to this rule
		Color color; // The color with which this will be rendered, or NULL to use the default
		int fontStyle; // Font attributes (Font.BOLD, etc)

		public CharSymbolSet(String group_name, Color highlight_color, int font_style)
		{
			name = group_name;
			color = highlight_color;
			fontStyle = font_style;
			chars = new HashSet<Character>();
		}
	}

	ArrayList<CharSymbolSet> hlChars = new ArrayList<CharSymbolSet>();

	/** This is the pattern we will use to isolate identifiers. **/
	Pattern identifier_pattern = Pattern.compile("[a-z_A-Z]([a-z_A-Z0-9]*)");

	/** This is a class for specifying anything else we want to skip or highlight.
	 *  You should use this, for example, to specify how to identify numeric literals
	 *  and, at your option, how to color them differently.
	**/
	class SimpleToken
	{
		String name; // The name of this token
		Pattern pattern; // The pattern that constitutes the token
		int fontStyle; // The font style with which the token is highlighted
		Color color; // The color with which the token will be highlighted, or NULL to use the default

		public SimpleToken(String token_name, String regex, int font_style, Color highlight_color)
		{
			name = token_name;
			pattern = Pattern.compile(regex);
			fontStyle = font_style;
			color = highlight_color;
		}
	}

	ArrayList<SimpleToken> otherTokens = new ArrayList<GenericHighlighter.SimpleToken>();

	static final class SchemeInfo
	{
		enum SchemeType
		{
			NOTHING,TOKEN,BLOCK,KEYWORD,SYMBOL;
		}

		SchemeType type;
		int id;

		public SchemeInfo()
		{
			type = SchemeType.NOTHING;
			id = 0;
		}

		public SchemeInfo(SchemeType scheme_type, int scheme_id)
		{
			type = scheme_type;
			id = scheme_id;
		}
	}

	/**
	 * Returns the scheme active at a given position, or a token type otherwise.
	 * 
	 * Requires 0 <= pos <= line.length. Returns the id of any active scheme,
	 * If no scheme is active at that point, one of the values in @c TOKEN_TYPES
	 * is returned instead in the low-order byte. The next two lowest-order bytes
	 * of the return value carry any extra information (namely an ID).
	 * 
	 * If pos >= line.length, the function will return only block schemes open after
	 * the end of the line.
	 * 
	 * You must shift this id yourself before ORing it into a flag set.
	 * 
	 * @param ischeme  Initial scheme; the scheme active at the beginning of the line. 
	 * @param line     The line to parse for schemes.
	 * @param pos      The position at which to stop parsing.
	 */
	private SchemeInfo get_scheme_at(int ischeme, StringBuilder line, int pos)
	{
		int i = 0; // The position from which we will parse this thing
		for (;;) // What we're going to do is find any and all blocks up front, and move to the end of them.
		{
			int shm = 0; // Scheme Holding Minimum Match
			int mmin = pos + 1; // Minimum match position
			int mminend = mmin;
			if (ischeme == 0)
			{
				for (int si = 0; si < schemes.size(); si++)
				{
					Matcher m = schemes.get(si).begin.matcher(line.toString()).region(i,line.length()).useTransparentBounds(
							true);
					if (!m.find()) continue;
					if (m.start() < mmin)
					{ // If this one is closer to the beginning, it can potentially consume later ones. 
						mmin = m.start(); // So we have to pay attention to it first.
						mminend = m.end();
						shm = si;
					}
				}
			}
			else
			{
				mmin = -1;
				mminend = 0;
				shm = ischeme;
				ischeme = 0;
			}
			if (mmin <= pos)
			{ // If we actually found one that starts before out position,
				// Start searching for its end.
				for (;;)
				{
					Matcher mmatcher = schemes.get(shm).end.matcher(line.toString()).region(mminend,
							line.length());
					if (!mmatcher.find() || mmatcher.end() > pos) // If there's no end in sight, or that end passed our position of interest
						return new SchemeInfo(SchemeType.BLOCK,shm); // Then our position is inside the block, so we return the block's scheme info.
					// Now, we have found a chunk that may be the end marker, and lies before our position in question.
					// Move to its end.
					i = mmatcher.end();
					if (!schemes.get(shm).escapeend) // If we can't escape an ending sequence,
						break; // Then mission complete
					// Otherwise, we have to verify that the end *isn't* escaped.
					char escc = schemes.get(shm).escapeChar;
					boolean end_escaped = false;
					int cp;
					for (cp = mminend; cp < mmatcher.start(); cp++)
					{ // So, start iterating block contents!
						if (line.charAt(cp) == escc) // If we see an escape char
						{
							if (cp + 1 < mmatcher.start()) // Check if it's at the end char we're looking at
								cp++; // It's not! Skip the next char in case it's another escape char.
							else
								end_escaped = true; // It is! The end has been escaped. Find a new end and come back.
						}
					}
					if (!end_escaped) // If the end wasn't escaped,
						break; // Mission accomplished
					// So, our line was escaped.
					if (cp >= line.length()) // If we're at the end of the line now,
						return new SchemeInfo(SchemeType.BLOCK,shm); // We're clearly in the block.
					// Otherwise, continue iteration
					mminend = i; // And perform the next search from the end of this escaped marker
				}
			}
			else
				// Otherwise, checking again won't help anything. Leave.
				break;
		}

		// Okay, so we're not in any blocks. We might be at an important symbol,
		// or in a keyword or numeral or something.
		if (pos >= line.length()) // But if we're looking for our status after the line is over,
			return new SchemeInfo(); // Then we'd better just return nothing.

		SubschemeLoop: while (i <= pos)
		{
			if (Character.isWhitespace(line.charAt(i)))
			{
				while (++i < line.length() && Character.isWhitespace(line.charAt(i)))
				{ /* Move past whitespace */
				}
				continue;
			}
			Matcher lookingat = identifier_pattern.matcher(line).region(i,line.length());
			if (lookingat.lookingAt())
			{
				if (lookingat.end() > pos)
				{
					String f = line.substring(i,lookingat.end());
					for (int sn = 0; sn < hlKeywords.size(); sn++)
						if (hlKeywords.get(sn).words.contains(f)) return new SchemeInfo(SchemeType.KEYWORD,sn);
					return new SchemeInfo();
				}
				i = lookingat.end();
				continue SubschemeLoop;
			}
			for (int tt = 0; tt < otherTokens.size(); tt++)
			{
				lookingat = otherTokens.get(tt).pattern.matcher(line).region(i,line.length());
				if (lookingat.lookingAt())
				{
					if (lookingat.end() > pos) return new SchemeInfo(SchemeType.TOKEN,tt);
					i = lookingat.end();
					continue SubschemeLoop;
				}
			}
			if (i == pos)
			{
				char c = line.charAt(i);
				for (int sn = 0; sn < hlChars.size(); sn++)
					if (hlChars.get(sn).chars.contains(c)) return new SchemeInfo(SchemeType.SYMBOL,sn);
			}
			i++;
		}
		return new SchemeInfo();
	}

	private void highlight()
	{
		do
		{
			if (invalid_line == 0)
				jt.code.get(invalid_line).attr = 0;
			else
				invalid_line--;
		}
		while (jt.code.get(invalid_line).attr < 0);
		while (invalid_line < line_count - 1)
		{
			SchemeInfo a = get_scheme_at(
					(int) ((jt.code.get(invalid_line).attr & LINE_ATTRIBS.LA_SCHEMEBLOCK) >> LINE_ATTRIBS.LA_SCHEMEBITOFFSET),
					jt.code.get(invalid_line).sbuild,jt.code.get(invalid_line).sbuild.length());
			invalid_line++;
			if (jt.code.get(invalid_line).attr < 1)
				jt.code.get(invalid_line).attr = 0;
			else
				jt.code.get(invalid_line).attr &= ~LINE_ATTRIBS.LA_SCHEMEBLOCK; // Remove all scheme info
			if (a.type == SchemeType.BLOCK) // If we're in a block scheme, note so.
				jt.code.get(invalid_line).attr |= a.id << LINE_ATTRIBS.LA_SCHEMEBITOFFSET;
		}
		invalid_line = -1;
	}

	public GenericHighlighter(JoshText joshText)
	{
		this();
		set_owner(joshText);
	}

	public GenericHighlighter()
	{
		// TODO Auto-generated constructor stub
	}

	public void set_owner(JoshText jt)
	{
		this.jt = jt;
	}

	public HighlighterInfo getStyle(int lineNum, int i)
	{
		Line line = jt.code.get(lineNum);
		if (line.attr < 0)
		{
			System.err.println("ERROR! That FUCKING highlight function didn't complete; line " + lineNum
					+ " is invalid");
			jt.code.get(lineNum).attr = 0;
		}
		SchemeInfo si = get_scheme_at(
				(int) ((line.attr & LINE_ATTRIBS.LA_SCHEMEBLOCK) >> LINE_ATTRIBS.LA_SCHEMEBITOFFSET),
				line.sbuild,i);
		switch (si.type)
		{
			case BLOCK:
				return new HighlighterInfo(schemes.get(si.id).fontStyle,schemes.get(si.id).color);
			case KEYWORD:
				return new HighlighterInfo(hlKeywords.get(si.id).fontStyle,hlKeywords.get(si.id).color);
			case NOTHING:
				break;
			case TOKEN:
				return new HighlighterInfo(otherTokens.get(si.id).fontStyle,otherTokens.get(si.id).color);
			case SYMBOL:
				return new HighlighterInfo(hlChars.get(si.id).fontStyle,hlChars.get(si.id).color);
		}
		return new HighlighterInfo(0,null);
	}

	public void formatCode()
	{
		return; // We can't format the code; we're only pretending to know anything about it.
	}

	public void linesChanged(int start, int end)
	{
		line_count = jt.code.size();
		if (start < invalid_line || invalid_line == -1) invalid_line = start;
		for (int i = start; i < end; i++)
			if (jt.code.get(i).attr > 0)
				jt.code.get(i).attr = -jt.code.get(i).attr;
			else if (jt.code.get(i).attr > 0) jt.code.get(i).attr = -1;
		highlight();
	}

	public ArrayList<HighlighterInfoEx> getStyles(int lineNum)
	{
		ArrayList<HighlighterInfoEx> res = new ArrayList<HighlighterInfoEx>();
		Line jline = jt.code.get(lineNum);
		StringBuilder line = jline.sbuild;
		int ischeme = (int) ((jline.attr & LINE_ATTRIBS.LA_SCHEMEBLOCK) >> LINE_ATTRIBS.LA_SCHEMEBITOFFSET);

		int i = 0; // The position from which we will parse this thing
		FindAllBlocks: for (;;) // What we're going to do is find any and all blocks up front, and move to the end of them.
		{
			int shm = -1; // Scheme Holding Minimum Match
			int mmin = line.length(); // Minimum match position
			int mminend = mmin;
			if (ischeme == 0)
			{
				for (int si = 0; si < schemes.size(); si++)
				{
					Matcher m = schemes.get(si).begin.matcher(line.toString()).region(i,line.length()).useTransparentBounds(
							true);
					if (!m.find()) continue;
					if (m.start() < mmin)
					{ // If this one is closer to the beginning, it can potentially consume later ones. 
						mmin = m.start(); // So we have to pay attention to it first.
						mminend = m.end();
						shm = si;
					}
				}
			}
			else
			{
				mmin = 0;
				mminend = 0;
				shm = ischeme;
				ischeme = 0;
			}
			
			if (shm == -1)
				break;

			// Start searching for its end.
			for (;;)
			{
				Matcher mmatcher = schemes.get(shm).end.matcher(line.toString()).region(mminend,
						line.length());
				if (!mmatcher.find()) // If there's no end in sight, or that end passed our position of interest
				{
					res.add(new HighlighterInfoEx(schemes.get(shm).fontStyle,schemes.get(shm).color,mmin,line.length(),shm));
					break FindAllBlocks; // Then we've found all the blocks. Quit.
				}
				// Now, we have found a chunk that may be the end marker, and lies before our position in question.
				// Move to its end.
				i = mmatcher.end();
				if (!schemes.get(shm).escapeend) // If we can't escape an ending sequence,
				{
					res.add(new HighlighterInfoEx(schemes.get(shm).fontStyle,schemes.get(shm).color,mmin,i,shm));
					break; // Then mission complete
				}
				// Otherwise, we have to verify that the end *isn't* escaped.
				char escc = schemes.get(shm).escapeChar;
				boolean end_escaped = false;
				int cp;
				for (cp = mminend; cp < mmatcher.start(); cp++)
				{ // So, start iterating block contents!
					if (line.charAt(cp) == escc) // If we see an escape char
					{
						if (cp + 1 < mmatcher.start()) // Check if it's at the end char we're looking at
							cp++; // It's not! Skip the next char in case it's another escape char.
						else
							end_escaped = true; // It is! The end has been escaped. Find a new end and come back.
					}
				}
				if (!end_escaped) // If the end wasn't escaped,
					break; // Mission accomplished
				// So, our line was escaped.
				if (cp >= line.length()) // If we're at the end of the line now,
				{ // Then the block is escaped and doesn't end on this line. Hop out. 
					res.add(new HighlighterInfoEx(schemes.get(shm).fontStyle,schemes.get(shm).color,mmin,line.length(),shm));
					break FindAllBlocks;
				}
				// Otherwise, continue iteration
				mminend = i; // And perform the next search from the end of this escaped marker
				res.add(new HighlighterInfoEx(schemes.get(shm).fontStyle,schemes.get(shm).color,mmin,i,shm));
			}
		}

		i = 0;
		res.add(new HighlighterInfoEx(0,null,line.length(),line.length(),0));
		for (int bi = 0; bi < res.size(); i = res.get(bi++).endPos)
		{
			final int sp = res.get(bi).startPos;
			SubschemeLoop: while (i < sp)
			{
				if (Character.isWhitespace(line.charAt(i)))
				{
					while (++i < line.length() && Character.isWhitespace(line.charAt(i)))
					{ /* Move past whitespace */
					}
					continue;
				}
				Matcher lookingat = identifier_pattern.matcher(line).region(i,line.length());
				if (lookingat.lookingAt())
				{
					String f = line.substring(i,lookingat.end());
					for (int sn = 0; sn < hlKeywords.size(); sn++)
						if (hlKeywords.get(sn).words.contains(f))
						{
							res.add(bi++,new HighlighterInfoEx(hlKeywords.get(sn).fontStyle,
									hlKeywords.get(sn).color,lookingat.start(),lookingat.end(),0));
							break;
						}
					i = lookingat.end();
					continue SubschemeLoop;
				}
				for (int tt = 0; tt < otherTokens.size(); tt++)
				{
					lookingat = otherTokens.get(tt).pattern.matcher(line).region(i,line.length());
					if (lookingat.lookingAt())
					{
						res.add(bi++,new HighlighterInfoEx(otherTokens.get(tt).fontStyle,otherTokens.get(tt).color,
								lookingat.start(),lookingat.end(),0));
						i = lookingat.end();
						continue SubschemeLoop;
					}
				}
				char c = line.charAt(i);
				for (int sn = 0; sn < hlChars.size(); sn++)
					if (hlChars.get(sn).chars.contains(c))
						res.add(bi++,new HighlighterInfoEx(hlChars.get(sn).fontStyle,hlChars.get(sn).color,i,i+1,0));
				i++;
			}
		}
		return res;
	}
}
