/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
	 * 
	 * This file is part of JoshEdit. JoshEdit is free software.
	 * You can use, modify, and distribute it under the terms of
	 * the GNU General Public License, version 3 or later. 
	 */

package org.lateralgm.joshedit;

import java.awt.Color;
import java.awt.Font;

public class CPPHighlighter extends GenericHighlighter
{
	public CPPHighlighter(JoshText jt)
	{
		super(jt);
		schemes.add(new BlockDescriptor("Formal Comment","/\\*!","\\*/",true,false,(char) 0,new Color(
				128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Lazy Formal Comment","/\\*(?=\\*)","\\*/",true,false,(char) 0,
				new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Comment","/\\*","\\*/",true,false,(char) 0,new Color(13,
				165,13),Font.ITALIC));
		schemes.add(new BlockDescriptor("Formal C++ Comment","//!","$",true,false,(char) 0,new Color(
				128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Lazy Formal C++ Comment","///","$",true,false,(char) 0,
				new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("C++ Comment","//","$",true,false,(char) 0,
				new Color(13,165,13),Font.ITALIC));
		schemes.add(new BlockDescriptor("Double Quote String","\"","($|\")",true,true,'\\',new Color(
				0,0,255),0));
		schemes.add(new BlockDescriptor("Double Quote String","'","($|')",true,true,'\\',new Color(255,
				128,0),0));
		schemes.add(new BlockDescriptor("Preprocessor","^(\\s*)#","$",true,true,'\\',new Color(0,
				255,255),0));
		KeywordSet kws = new KeywordSet("Statements",new Color(0,0,128),Font.BOLD);
		String[] cppkws = {"if","else","do","while","for","new","delete","this","and","or","not"};
		for (int i = 0; i < cppkws.length; i++)
			kws.words.add(cppkws[i]);
		hlKeywords.add(kws);

		CharSymbolSet css = new CharSymbolSet("Operators and Separators",new Color(255,0,0),0);
		char[] ca = "{[()]}!%^&*-/+=?:~<>.,;".toCharArray();
		for (int i = 0; i < ca.length; i++)
			css.chars.add(ca[i]);
		hlChars.add(css);

		otherTokens.add(new SimpleToken("Hex literal","0[Xx][0-9A-Fa-f]+[FfUuLlDd]*",0,new Color(255,
				100,100)));
		otherTokens.add(new SimpleToken("Numeric literal","[0-9]+[FfUuLlDd]*",0,new Color(255,0,255)));
	}
}
