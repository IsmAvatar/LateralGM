/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */
package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;

/**
 * Sample GML token marker class based on the default token marker.
 */
public class GMLTokenMarker extends DefaultTokenMarker
{
	/** Construct, populating language data. */
	public GMLTokenMarker()
	{
		super();
		schemes.add(new BlockDescriptor("Javadoc","/\\*(?=\\*)","\\*/",new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Block Comment","/(?=\\*)","\\*/",new Color(13,165,13),
				Font.ITALIC));
		schemes.add(new BlockDescriptor("Doc Line Comment","///","$",new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Line Comment","//","$",new Color(13,165,13),Font.ITALIC));
		schemes.add(new BlockDescriptor("Double Quote String","\"","\"",new Color(0,0,255),0));
		schemes.add(new BlockDescriptor("Single Quote String","'","'",new Color(0,0,255),0));

		KeywordSet kws = addKeywordSet("Statements",new Color(0,0,128),Font.BOLD);
		//FIXME: Just remove these and make LGM add them from its properties file
		Collections.addAll(kws.words,new String[] { "if","then","else","do","while","for","until",
				"with","switch","case","default","break","continue","exit","return" });
		tmKeywords.add(kws);

		CharSymbolSet css = new CharSymbolSet("Operators and Separators",new Color(255,0,0),0);
		char[] ca = "{[()]}!%^&*-/+=?:~<>.,;".toCharArray();
		for (int i = 0; i < ca.length; i++)
			css.chars.add(ca[i]);
		tmChars.add(css);

		otherTokens.add(new SimpleToken("Numeric literal","[0-9]+",0,new Color(255,0,255)));
		otherTokens.add(new SimpleToken("Hex literal","\\$[0-9A-Fa-f]+",0,new Color(255,100,100)));
	}
}
