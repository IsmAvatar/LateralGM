/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.jedit;

import javax.swing.text.Segment;

public class GMLTokenMarker extends TokenMarker
	{
	private static KeywordMap gmlKeywords;

	private KeywordMap stdKeywords, customKeywords;
	private int lastOffset;
	private int lastKeyword;
	private static final byte DQSTRING = Token.INTERNAL_FIRST;
	private static final byte SQSTRING = Token.INTERNAL_FIRST + 1;

	public GMLTokenMarker()
		{
		stdKeywords = getKeywords();
		customKeywords = null;
		}

	@Override
	protected byte markTokensImpl(byte token, Segment line, int lineIndex)
		{
		char[] array = line.array;
		int offset = line.offset;
		lastOffset = offset;
		lastKeyword = offset;
		int length = line.count + offset;
		loop: for (int i = offset; i < length; i++)
			{
			int i1 = (i + 1);
			char c = array[i];
			switch (token)
				{
				case Token.NULL:
					switch (c)
						{
						case '"':
							doKeyword(line,i);
							addToken(i - lastOffset,token);
							token = DQSTRING;
							lastOffset = i;
							lastKeyword = i;
							break;
						case '\'':
							doKeyword(line,i);
							addToken(i - lastOffset,token);
							token = SQSTRING;
							lastOffset = i;
							lastKeyword = i;
							break;
						case '/':
							doKeyword(line,i);
							if (length - i > 1)
								{
								switch (array[i1])
									{
									case '*':
										addToken(i - lastOffset,token);
										lastOffset = i;
										lastKeyword = i;
										if (length - i > 2 && array[i + 2] == '*')
											token = Token.COMMENT2;
										else
											token = Token.COMMENT1;
										break;
									case '/':
										addToken(i - lastOffset,token);
										addToken(length - i,Token.COMMENT1);
										lastOffset = length;
										lastKeyword = length;
										break loop;
									}
								}
							break;
						case '{':
						case '}':
							addToken(i - lastOffset,token);
							addToken(1,Token.KEYWORD1);
							token = Token.NULL;
							lastOffset = i1;
							lastKeyword = i1;
							break;
						default:
							if (!Character.isLetterOrDigit(c) && c != '_') doKeyword(line,i);
							break;
						}
				case Token.COMMENT1:
				case Token.COMMENT2:
					if (c == '*' && length - i > 1)
						{
						if (array[i1] == '/')
							{
							i++;
							addToken((i + 1) - lastOffset,token);
							token = Token.NULL;
							lastOffset = i + 1;
							lastKeyword = i + 1;
							}
						}
					break;
				case DQSTRING:
					if (c == '"')
						{
						addToken(i1 - lastOffset,Token.LITERAL1);
						token = Token.NULL;
						lastOffset = i1;
						lastKeyword = i1;
						}
					break;
				case SQSTRING:
					if (c == '\'')
						{
						addToken(i1 - lastOffset,Token.LITERAL1);
						token = Token.NULL;
						lastOffset = i1;
						lastKeyword = i1;
						}
					break;
				default:
					throw new InternalError("Invalid state: " + token);
				}
			}
		if (token == Token.NULL) doKeyword(line,length);

		switch (token)
			{
			case DQSTRING:
			case SQSTRING:
				addToken(length - lastOffset,Token.LITERAL1);
				break;
			default:
				addToken(length - lastOffset,token);
				break;
			}
		return token;
		}

	public void setCustomKeywords(KeywordMap km)
		{
		customKeywords = km;
		}

	public static KeywordMap getKeywords()

		{
		if (gmlKeywords == null)
			{
			gmlKeywords = new KeywordMap(false);
			for (GMLKeywords.Construct keyword : GMLKeywords.CONSTRUCTS)
				gmlKeywords.add(keyword.getName(),Token.KEYWORD1);
			for (GMLKeywords.Operator keyword : GMLKeywords.OPERATORS)
				gmlKeywords.add(keyword.getName(),Token.OPERATOR);
			for (GMLKeywords.Constant keyword : GMLKeywords.CONSTANTS)
				gmlKeywords.add(keyword.getName(),Token.LITERAL2);
			for (GMLKeywords.Variable keyword : GMLKeywords.VARIABLES)
				gmlKeywords.add(keyword.getName(),Token.KEYWORD2);
			for (GMLKeywords.Function keyword : GMLKeywords.FUNCTIONS)
				gmlKeywords.add(keyword.getName(),Token.LABEL);
			}
		return gmlKeywords;
		}

	private boolean doKeyword(Segment line, int i)
		{
		int i1 = i + 1;

		int len = i - lastKeyword;
		byte id = stdKeywords.lookup(line,lastKeyword,len);
		if (id == Token.NULL && customKeywords != null)
			id = customKeywords.lookup(line,lastKeyword,len);
		if (id != Token.NULL)
			{
			if (lastKeyword != lastOffset) addToken(lastKeyword - lastOffset,Token.NULL);
			addToken(len,id);
			lastOffset = i;
			}
		lastKeyword = i1;
		return false;
		}
	}
