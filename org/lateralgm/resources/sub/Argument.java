/*
 * Copyright (C) 2007, 2008 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.library.LibArgument;

public class Argument
	{
	public static final byte ARG_EXPRESSION = 0;
	public static final byte ARG_STRING = 1;
	public static final byte ARG_BOTH = 2;
	public static final byte ARG_BOOLEAN = 3;
	public static final byte ARG_MENU = 4;
	public static final byte ARG_COLOR = 13;
	@Deprecated
	public static final byte ARG_FONTSTRING = 15;
	public static final byte ARG_SPRITE = 5;
	public static final byte ARG_SOUND = 6;
	public static final byte ARG_BACKGROUND = 7;
	public static final byte ARG_PATH = 8;
	public static final byte ARG_SCRIPT = 9;
	public static final byte ARG_GMOBJECT = 10;
	public static final byte ARG_ROOM = 11;
	public static final byte ARG_FONT = 12;
	public static final byte ARG_TIMELINE = 14;

	public final byte kind;
	private String val;
	private ResourceReference<? extends Resource<?,?>> res; // for references to Resources

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public Argument(byte kind, String val, ResourceReference<? extends Resource<?,?>> res)
		{
		this.kind = kind;
		this.val = val;
		this.res = res;
		}

	public Argument(byte kind)
		{
		this(kind,"",null);
		}

	public static Resource.Kind getResourceKind(byte argumentKind)
		{
		switch (argumentKind)
			{
			case ARG_SPRITE:
				return Resource.Kind.SPRITE;
			case ARG_SOUND:
				return Resource.Kind.SOUND;
			case ARG_BACKGROUND:
				return Resource.Kind.BACKGROUND;
			case ARG_PATH:
				return Resource.Kind.PATH;
			case ARG_SCRIPT:
				return Resource.Kind.SCRIPT;
			case ARG_GMOBJECT:
				return Resource.Kind.OBJECT;
			case ARG_ROOM:
				return Resource.Kind.ROOM;
			case ARG_FONT:
				return Resource.Kind.FONT;
			case ARG_TIMELINE:
				return Resource.Kind.TIMELINE;
			default:
				return null;
			}
		}

	public String toString(LibArgument la)
		{
		Resource.Kind rk = Argument.getResourceKind(kind);
		switch (kind)
			{
			case Argument.ARG_BOOLEAN:
				return Boolean.toString(val != "0");
			case Argument.ARG_MENU:
				String[] sam = la.menu.split("\\|");
				try
					{
					return sam[Integer.parseInt(val)];
					}
				catch (NumberFormatException nfe)
					{
					}
				catch (IndexOutOfBoundsException be)
					{
					}
				return val;
			case Argument.ARG_COLOR:
				try
					{
					return String.format("%06X",Integer.parseInt(val));
					}
				catch (NumberFormatException e)
					{
					}
				return val;
			default:
				if (rk == null) return val;
				try
					{
					return res.get().getName();
					}
				catch (NullPointerException e)
					{
					}
				return "<none>";
			}
		}

	protected void fireStateChanged()
		{
		updateTrigger.fire();
		}

	public String getVal()
		{
		return val;
		}

	public void setVal(String val)
		{
		this.val = val;
		fireStateChanged();
		}

	public ResourceReference<? extends Resource<?,?>> getRes()
		{
		return res;
		}

	public void setRes(ResourceReference<? extends Resource<?,?>> res)
		{
		this.res = res;
		fireStateChanged();
		}
	}
