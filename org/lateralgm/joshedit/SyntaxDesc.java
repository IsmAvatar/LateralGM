/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.util.ArrayList;
import java.util.regex.Pattern;

class SyntaxDesc
{
	public class Block
	{
		String begin;  // Begin delimiter. In Java, this is '{'.
		String end;    // End delimiter. In Java, this is '}'.
		String indent; // Indent text. This could end up uniformly being "\t".
		
		public Block(String b, String e) {
			begin = b;
			end = e;
		}
		public Block(String b, String e, String i) {
			indent = i;
			begin = b;
			end = e;
		}
	}
	public class IndentRule
	{
		Pattern rule; // A regular-expression rule for the line before this block
		Block bi; // Block Info; does not imply a sexual orientation

		public IndentRule(String r) {
			rule = Pattern.compile(r);
			bi = new Block("","");
		}
		public IndentRule(String r, Block b) {
			rule = Pattern.compile(r);
			bi = b;
		}
		public IndentRule(String r,String b,String e) {
			rule = Pattern.compile(r);
			bi = new Block(b,e);
		}
		public IndentRule(String r,String b,String e,String i) {
			rule = Pattern.compile(r);
			bi = new Block(b,e,i);
		}
	}
	
	ArrayList<IndentRule> ir;
	public SyntaxDesc()
	{
		ir = new ArrayList<IndentRule>();
	}
	public void set_language(String s)
	{
		//TODO: Read $s.properties to populate this
		// Rexex explained:   [separator]if[spaces](   (........)*  .........*   ) \n
		//ir.add(new IndentRule("(^|((.*)[\\W]+))(if|for|while)(\\s*)\\((([^\\(]*)\\)*)([^\\(\\)]*)\\)(\\s*)$","{","}","    "));
		/*System.out.println("Test1: " + (ir.get(0).rule.matcher("testString").matches()));
		System.out.println("Test2: " + (ir.get(0).rule.matcher("if (test)").matches()));
		System.out.println("Test3: " + (ir.get(0).rule.matcher("if(test)").matches()));
		System.out.println("Test4: " + (ir.get(0).rule.matcher("if test").matches()));
		System.out.println("Test5: " + (ir.get(0).rule.matcher("  if(test)").matches()));
		System.out.println("Test6: " + (ir.get(0).rule.matcher("  if (test) ").matches()));
		System.out.println("Test7: " + (ir.get(0).rule.matcher("0  if (test) ").matches()));
		System.out.println("Test1: " + (ir.get(0).rule.matcher("  0if (test) ").matches()));*/
	}
	int hasIndentAfter(String line)
	{
		for (int i = 0; i < ir.size(); i++)
		{
			if (ir.get(i).rule.matcher(line).matches())
				return i;
			System.out.println("False.");
		}
		return -1;
	}
	String getIndent(int i)
	{
		if (i > -1 && i < ir.size())
			return ir.get(i).bi.indent;
		return "INVALID:" + i;
	}
}