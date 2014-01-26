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

import org.lateralgm.joshedit.DefaultTokenMarker.SchemeInfo.SchemeType;
import org.lateralgm.joshedit.Line.LINE_ATTRIBS;

/**
 * A default implementation of TokenMarker taking regular expressions for everything,
 * allowing simplified implementation of language token marking rules.
 * 
 * @author Josh Ventura
 */
public abstract class DefaultTokenMarker implements TokenMarker
{
	/** The number of lines last time we parsed; used to determine change type. */
	private int line_count;
	/** The index of the first invalid line, or -1 for all-clear */
	private int invalid_line = 0;

	/** True if this is in general a case sensitive language. */
	public boolean caseSensitive = true; // Most are.

	/** Default constructor; does nothing. */
	public DefaultTokenMarker()
	{
	}
	
	
	public abstract DefaultKeywords.Keyword[][] GetKeywords();

	/**
	 * Construct with case sensitivity option.
	 * 
	 * @param caseSens
	 *            True if this language is in general case sensitive.
	 **/
	public DefaultTokenMarker(boolean caseSens)
	{
		caseSensitive = caseSens;
	}

	// The index of the next valid line after an invalid line is determined
	// while parsing--the first line that matches up with the tags from the
	// previous line is considered valid.

	/**
	 * Class for storing information about a syntax block, such as comments or strings.
	 * These are filtered out and handled before any other token type.
	 * 
	 * @author Josh Ventura
	 */
	public class BlockDescriptor
	{
		/** Human-readable name for this scheme. */
		String name;
		/** Begin and end are not regex. If end is null, EOL is used. */
		Pattern begin;
		/** Begin and end are not regex. If end is null, EOL is used. */
		Pattern end;
		/** True if this block type is allowed to span multiple lines. Can be true only if end is non-null. */
		boolean multiline;
		/** True if we can escape the ending character. If end is null, this allows us to span multiple lines. */
		boolean escapeend;
		/** The character used to escape things, or null if no escape is allowed. */
		char escapeChar;
		/** The color with which this will be rendered, or NULL to use the default */
		Color color;
		/** Font attributes (Font.BOLD, etc) */
		int fontStyle;

		/**
		 * @param block_name  The name of this block type.
		 * @param begin_regex The regular expression matching the start of the block.
		 * @param end_regex   The regular expression matching the end of the block.
		 * @param allow_multiline   True if this block should be allowed to span multiple lines.
		 * @param escape_endmarkers True if the end block expression can be escaped.
		 * @param escape_char The escape char by which the end block might be escaped.
		 * @param markColor The font color with which this block will be rendered.
		 * @param font_style The font style with which this block will be rendered.
		 */
		public BlockDescriptor(String block_name, String begin_regex, String end_regex,
				boolean allow_multiline, boolean escape_endmarkers, char escape_char, Color markColor,
				int font_style)
		{
			name = block_name;
			begin = Pattern.compile(begin_regex);
			end = Pattern.compile(end_regex);
			multiline = allow_multiline;
			escapeend = escape_endmarkers;
			escapeChar = escape_char;
			color = markColor;
			fontStyle = font_style;
		}

		/**
		 * Convenience constructor for when multiline is true and the end marker cannot be escaped.
		 * @param block_name  The name of this block type.
		 * @param begin_regex The regular expression matching the start of the block.
		 * @param end_regex   The regular expression matching the end of the block.
		 * @param markColor The font color with which this block will be rendered.
		 * @param font_style The font style with which this block will be rendered.
		 */
		public BlockDescriptor(String block_name, String begin_regex, String end_regex,
				Color markColor, int font_style)
		{
			this(block_name,begin_regex,end_regex,true,false,(char) 0,markColor,font_style);
		}
	}

	/** Array of all block types to mark. */
	public ArrayList<BlockDescriptor> schemes = new ArrayList<BlockDescriptor>();

	/**
	 * A class for representing a set of keywords to mark.
	 * @author Josh Ventura
	 */
	public static class KeywordSet
	{
		/** The name of this group of keywords, for preferences purposes. */
		String name;
		/** A set of words marked according to this rule */
		public Set<String> words;
		/** The color with which this will be rendered, or NULL to use the default */
		Color color;
		/**Font attributes (Font.BOLD, etc)  */
		int fontStyle;
		/** Whether or not these keywords must match in case to be marked. */
		public final boolean caseSensitive;

		/** Construct a new keyword set with some basic information.
		 * @param groupName The name of this group, for preferences purposes.
		 * @param markColor The font color with which keywords in this group are rendered.
		 * @param fontStyle The font style with which keywords in this group are rendered.
		 * @param casesens Whether or not this keyword set should be matched with case sensitivity.
		 */
		public KeywordSet(String groupName, Color markColor, int fontStyle, boolean casesens)
		{
			name = groupName;
			color = markColor;
			this.fontStyle = fontStyle;
			caseSensitive = casesens;
			words = new HashSet<String>();
		}
	}

	/** All keyword sets to mark. */
	public ArrayList<KeywordSet> tmKeywords = new ArrayList<KeywordSet>();

	/** A keyword set used to specify formatting for any identifier which is not in a keyword set. */
	public KeywordSet default_kws = null; // Set this to non-null to mark regular identifiers

	/** Adds a new keyword set with some basic information. Uses global case-sensitivity.
	 * @param groupName The name of this group, for preferences purposes.
	 * @param markColor The font color with which keywords in this group are rendered.
	 * @param fontStyle The font style with which keywords in this group are rendered.
	 * @return The new keyword set, so you can populate it.
	 */
	public KeywordSet addKeywordSet(String groupName, Color markColor, int fontStyle)
	{
		return addKeywordSet(groupName,markColor,fontStyle,caseSensitive);
	}

	/** Adds a new keyword set with some basic information and specified case-sensitivity.
	 * @param groupName The name of this group, for preferences purposes.
	 * @param markColor The font color with which keywords in this group are rendered.
	 * @param fontStyle The font style with which keywords in this group are rendered.
	 * @param caseSensitiveTK Whether or not this keyword set should be matched with case sensitivity.
	 * @return The new keyword set, so you can populate it.
	 */
	public KeywordSet addKeywordSet(String groupName, Color markColor, int fontStyle,
			boolean caseSensitiveTK)
	{
		KeywordSet ks = new KeywordSet(groupName,markColor,fontStyle,caseSensitiveTK);
		tmKeywords.add(ks);
		return ks;
	}

	/**
	 * A class representing a set of symbols to mark.
	 * @author Josh Ventura
	 */
	protected class CharSymbolSet
	{
		/** The name of this group of characters */
		public String name;
		/** A set of characters marked according to this rule. */
		public Set<Character> chars;
		/** The color with which this will be rendered, or NULL to use the default. */
		public Color color;
		/** The font style with which these symbols will be rendered. (Font.BOLD, etc). */
		public int fontStyle;

		/**
		 * Construct a new symbol set to mark, with basic information.
		 * 
		 * @param group_name The name of this group of symbols.
		 * @param markColor The font color with which to render these symbols.
		 * @param font_style The font style with which to render these symbols.
		 */
		public CharSymbolSet(String group_name, Color markColor, int font_style)
		{
			name = group_name;
			color = markColor;
			fontStyle = font_style;
			chars = new HashSet<Character>();
		}
	}

	/** List of all symbol sets to mark */
	public ArrayList<CharSymbolSet> tmChars = new ArrayList<CharSymbolSet>();

	/** This is the pattern we will use to isolate identifiers. **/
	public Pattern identifier_pattern = Pattern.compile("[a-z_A-Z]([a-z_A-Z0-9]*)");

	/** This is a class for specifying anything else we want to skip or mark.
	 *  You should use this, for example, to specify how to identify numeric literals
	 *  and, at your option, how to color them differently.
	**/
	public class SimpleToken
	{
		/** The name of this token */
		public String name;
		/** The pattern that constitutes the token */
		public Pattern pattern;
		/** The font style with which the token is mark */
		public int fontStyle;
		/** The color with which the token will be marked, or NULL to use the default */
		public Color color;

		/**
		 * @param token_name The name of this token or token type.
		 * @param regex The regular expression used to test for this token.
		 * @param font_style The font style used when rendering this token.
		 * @param markColor The font color with which to render this token.
		 */
		public SimpleToken(String token_name, String regex, int font_style, Color markColor)
		{
			name = token_name;
			pattern = Pattern.compile(regex);
			fontStyle = font_style;
			color = markColor;
		}
	}

	/** A list of all other tokens to mark. */
	public ArrayList<SimpleToken> otherTokens = new ArrayList<DefaultTokenMarker.SimpleToken>();

	/** Class for storing extra info about a scheme. */
	static final class SchemeInfo
	{
		/** Enumeration of all possible scheme types. */
		enum SchemeType
		{
			/** This SchemeType is not actually a scheme. */
			NOTHING,
			/** This scheme is from a custom token. */
			TOKEN,
			/** This scheme is from a block of some sort. */
			BLOCK,
			/** This scheme is from a keyword set; an entry in a member of tmKeywords. */
			KEYWORD,
			/** This scheme is from a symbol set; an entry in a member of tmSymbols. */
			SYMBOL,
			/** This entry is a generic identifier which we are marking anyway. */
			DEFKEYWORD,
			/** This entry is a block which was not terminated before the end of the line. */
			UNTERMBLOCK;
		}

		/** The actual type of this scheme. */
		SchemeType type;
		/** The index of this scheme in its type, or zero if inapplicable. */
		int id;

		/** Construct new empty schemeInfo. */
		public SchemeInfo()
		{
			type = SchemeType.NOTHING;
			id = 0;
		}

		/** Construct new empty schemeInfo with a type. 
		 * @param scheme_type The type of this scheme; one of the {@link SchemeType} constants.
		 */
		public SchemeInfo(SchemeType scheme_type)
		{
			type = scheme_type;
			id = 0;
		}

		/** Construct new empty schemeInfo with both fields. 
		 * @param scheme_type The type of this scheme; one of the {@link SchemeType} constants.
		 * @param scheme_id The index of this scheme in its type, if applicable, or else zero.
		 */
		public SchemeInfo(SchemeType scheme_type, int scheme_id)
		{
			type = scheme_type;
			id = scheme_id;
		}
	}

	/** Class for tossing around token marker info with a schemeInfo attachment. */
	class TokenMarkerInfoEx extends TokenMarkerInfo
	{
		/** Info about the scheme for which this TokenMarkerInfo was constructed. */
		SchemeInfo schemeInfo;

		/** Construct our extended TokenMarker info with the works. 
		 * 
		 * @param fs The font style to use.
		 * @param col The color to use.
		 * @param start The start position in the current line.
		 * @param end The end position in the current line.
		 * @param hash Any attribute, probably in si as well, which much persist.
		 * @param si More information about the scheme that created this TokenMarkerInfo.
		 */
		public TokenMarkerInfoEx(int fs, Color col, int start, int end, int hash, SchemeInfo si)
		{
			super(fs,col,start,end,hash);
			schemeInfo = si;
		}
	}

	/**
	 * Mark new or invalidated lines.
	 * 
	 * @param code The code to mark.
	 */
	private void mark(Code code)
	{
		do
		{
			if (invalid_line == 0)
				code.get(invalid_line).attr = 0;
			else
				invalid_line--;
		}
		while (code.get(invalid_line).attr < 0);
		while (invalid_line < line_count - 1)
		{
			ArrayList<TokenMarkerInfo> styles = getStyles(code.get(invalid_line));
			TokenMarkerInfoEx tmi = (TokenMarkerInfoEx) styles.get(styles.size() - 1);
			invalid_line++;
			if (code.get(invalid_line).attr < 1)
				code.get(invalid_line).attr = 0;
			else
				code.get(invalid_line).attr &= ~LINE_ATTRIBS.LA_SCHEMEBLOCK; // Remove all scheme info
			if (tmi.schemeInfo.type == SchemeType.UNTERMBLOCK) // If we're in a block scheme, note so.
				code.get(invalid_line).attr |= (tmi.schemeInfo.id + 1) << LINE_ATTRIBS.LA_SCHEMEBITOFFSET;
		}
		invalid_line = -1;
	}

	/** @see TokenMarker#formatCode(Code) */
//r@Override
	public void formatCode(Code code)
	{
		return; // We can't format the code; we're only pretending to know anything about it.
	}

	/** @see TokenMarker#linesChanged(Code,int,int) */
//r@Override
	public void linesChanged(Code code, int start, int end)
	{
		line_count = code.size();
		if (start < invalid_line || invalid_line == -1) invalid_line = start;
		for (int i = start; i < end; i++)
			if (code.get(i).attr > 0)
				code.get(i).attr = -code.get(i).attr;
			else if (code.get(i).attr > 0) code.get(i).attr = -1;
		mark(code);
	}

	/** @see TokenMarker#getStyles(Line) */
//r@Override
	public ArrayList<TokenMarkerInfo> getStyles(Line jline)
	{
		ArrayList<TokenMarkerInfo> res = new ArrayList<TokenMarkerInfo>();
		StringBuilder line = jline.sbuild;
		int ischeme = (int) (((jline.attr & LINE_ATTRIBS.LA_SCHEMEBLOCK) >> LINE_ATTRIBS.LA_SCHEMEBITOFFSET) - 1);

		// Our function guarantees a block at the end of our code to keep the printer, well, printing.
		// Adding this blindly sometimes screws up other mechanisms. This tells us whether to do so or not.
		boolean pushCapstone = true;

		int i = 0; // The position from which we will parse this thing
		FindAllBlocks: for (;;) // What we're going to do is find any and all blocks up front, and move to the end of them.
		{
			int shm = -1; // Scheme Holding Minimum Match
			int mmin = line.length(); // Minimum match position
			int mminend = mmin;
			if (ischeme < 0)
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
				ischeme = -1;
			}

			if (shm == -1) break;

			// Start searching for its end.
			for (;;)
			{
				Matcher mmatcher = schemes.get(shm).end.matcher(line.toString()).region(mminend,
						line.length());
				if (!mmatcher.find()) // If there's no end in sight, or that end passed our position of interest
				{
					res.add(new TokenMarkerInfoEx(schemes.get(shm).fontStyle,schemes.get(shm).color,mmin,
							line.length(),shm,new SchemeInfo(SchemeType.UNTERMBLOCK,shm)));
					pushCapstone = false;
					break FindAllBlocks; // Then we've found all the blocks. Quit.
				}
				// Now, we have found a chunk that may be the end marker, and lies before our position in question.
				// Move to its end.
				i = mmatcher.end();

				if (!schemes.get(shm).escapeend) // If we can't escape an ending sequence,
				{
					res.add(new TokenMarkerInfoEx(schemes.get(shm).fontStyle,schemes.get(shm).color,mmin,i,
							shm,new SchemeInfo(SchemeType.BLOCK,shm)));
					break; // Then mission complete
				}

				// Otherwise, we have to verify that the end *isn't* escaped.
				char escc = schemes.get(shm).escapeChar;
				boolean end_escaped = false;

				int cp; // Check position
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

				if (!end_escaped)
				{ // If the end wasn't escaped,
					res.add(new TokenMarkerInfoEx(schemes.get(shm).fontStyle,schemes.get(shm).color,mmin,i,
							shm,new SchemeInfo(SchemeType.BLOCK,shm)));
					break; // Mission accomplished
				}
				// So, our line was escaped.
				if (cp >= line.length()) // If we're at the end of the line now,
				{ // Then the block is escaped and doesn't end on this line. Hop out. 
					res.add(new TokenMarkerInfoEx(schemes.get(shm).fontStyle,schemes.get(shm).color,mmin,
							line.length(),shm,new SchemeInfo(SchemeType.UNTERMBLOCK,shm)));
					pushCapstone = false;
					break FindAllBlocks;
				}
				// Otherwise, continue iteration
				mminend = i; // And perform the next search from the end of this escaped marker
			}
		}

		i = 0;
		if (pushCapstone)
			res.add(new TokenMarkerInfoEx(0,null,line.length(),line.length(),0,new SchemeInfo(
					SchemeType.NOTHING,0)));
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
					boolean fnd = false;
					String f = line.substring(i,lookingat.end());
					for (int sn = 0; sn < tmKeywords.size(); sn++)
						if (tmKeywords.get(sn).words.contains(tmKeywords.get(sn).caseSensitive ? f
								: f.toLowerCase()))
						{
							res.add(bi++,new TokenMarkerInfoEx(tmKeywords.get(sn).fontStyle,
									tmKeywords.get(sn).color,lookingat.start(),lookingat.end(),0,new SchemeInfo(
											SchemeType.KEYWORD,sn)));
							fnd = true;
							break;
						}
					if (!fnd)
					{
						if (default_kws != null)
							res.add(bi++,
									new TokenMarkerInfoEx(default_kws.fontStyle,default_kws.color,lookingat.start(),
											lookingat.end(),0,new SchemeInfo(SchemeType.DEFKEYWORD,0)));
					}
					i = lookingat.end();
					continue SubschemeLoop;
				}
				for (int tt = 0; tt < otherTokens.size(); tt++)
				{
					lookingat = otherTokens.get(tt).pattern.matcher(line).region(i,line.length());
					if (lookingat.lookingAt())
					{
						res.add(bi++,new TokenMarkerInfoEx(otherTokens.get(tt).fontStyle,
								otherTokens.get(tt).color,lookingat.start(),lookingat.end(),0,new SchemeInfo(
										SchemeType.TOKEN,tt)));
						i = lookingat.end();
						continue SubschemeLoop;
					}
				}
				char c = line.charAt(i);
				for (int sn = 0; sn < tmChars.size(); sn++)
					if (tmChars.get(sn).chars.contains(c))
						res.add(bi++,new TokenMarkerInfoEx(tmChars.get(sn).fontStyle,tmChars.get(sn).color,i,
								i + 1,0,new SchemeInfo(SchemeType.SYMBOL,sn)));
				i++;
			}
		}
		return res;
	}
}
