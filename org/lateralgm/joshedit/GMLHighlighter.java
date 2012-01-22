/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */
package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.Font;

public class GMLHighlighter extends GenericHighlighter
{
	public GMLHighlighter(JoshText jt)
	{
		super(jt);
		schemes.add(new BlockDescriptor("Formal Comment","/\\*!","(?<=\\*)/",true,false,(char) 0,
				new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Lazy Formal Comment","/\\*(?=\\*)","(?<=\\*)/",true,false,
				(char) 0,new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Comment","/(?=\\*)","\\*/",true,false,(char) 0,new Color(13,
				165,13),Font.ITALIC));
		schemes.add(new BlockDescriptor("Formal C++ Comment","//!","$",true,false,(char) 0,new Color(
				128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Lazy Formal C++ Comment","///","$",true,false,(char) 0,
				new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("C++ Comment","//","$",true,false,(char) 0,
				new Color(13,165,13),Font.ITALIC));
		schemes.add(new BlockDescriptor("Double Quote String","\"","\"",true,false,(char) 0,new Color(
				0,0,255),0));
		schemes.add(new BlockDescriptor("Double Quote String","'","'",true,false,(char) 0,new Color(0,
				0,255),0));
		//schemes.add(new BlockDescriptor("Double Quote String","","$",true,false,(char) 0,new Color(0,
		//		255,255)));
		KeywordSet kws = new KeywordSet("Statements",new Color(0,0,128),Font.BOLD);
		kws.words.add("if");
		kws.words.add("then");
		kws.words.add("else");
		kws.words.add("do");
		kws.words.add("while");
		kws.words.add("for");
		kws.words.add("until");
		kws.words.add("with"); //FIXME: Just remove these and make LGM add them from its properties file
		hlKeywords.add(kws);

		CharSymbolSet css = new CharSymbolSet("Operators and Separators",new Color(255,0,0),0);
		char[] ca = "{[()]}!%^&*-/+=?:~<>.,;".toCharArray();
		for (int i = 0; i < ca.length; i++)
			css.chars.add(ca[i]);
		hlChars.add(css);
		
		otherTokens.add(new SimpleToken("Numeric literal","[0-9]+",0,new Color(255,0,255)));
		otherTokens.add(new SimpleToken("Hex literal","\\$[0-9A-Fa-f]+",0,new Color(255,100,100)));
	}
}
