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
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
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

	public byte kind;
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
		this(kind,new String(),null);
		}

	public static Class<? extends Resource<?,?>> getResourceKind(byte argumentKind)
		{
		switch (argumentKind)
			{
			case ARG_SPRITE:
				return Sprite.class;
			case ARG_SOUND:
				return Sound.class;
			case ARG_BACKGROUND:
				return Background.class;
			case ARG_PATH:
				return Path.class;
			case ARG_SCRIPT:
				return Script.class;
			case ARG_GMOBJECT:
				return GmObject.class;
			case ARG_ROOM:
				return Room.class;
			case ARG_FONT:
				return Font.class;
			case ARG_TIMELINE:
				return Timeline.class;
			default:
				return null;
			}
		}

	public String toString(LibArgument la)
		{
		Class<? extends Resource<?,?>> rk = Argument.getResourceKind(kind);
		switch (kind)
			{
			case Argument.ARG_BOOLEAN:
				return Boolean.toString(!val.equals("0"));
			case Argument.ARG_MENU:
				String[] sam = la.menu.split("\\|");
				try
					{
					return sam[Integer.parseInt(val)];
					}
				catch (NumberFormatException nfe)
					{ //Silly user. Return default val
					}
				catch (IndexOutOfBoundsException be)
					{ //Silly user. Return default val
					}
				return val;
			case Argument.ARG_COLOR:
				try
					{
					return String.format("%06X",Integer.parseInt(val));
					}
				catch (NumberFormatException e)
					{ //Silly user. Return default val
					}
				return val;
			default:
				if (rk == null) return val;
				Resource<?,?> resource = res == null ? null : res.get();
				return resource == null ? "<none>" : resource.getName();
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

	@Override
	public int hashCode()
		{
		final int prime = 31;
		int result = 1;
		result = prime * result + kind;
		result = prime * result + ((res == null) ? 0 : res.hashCode());
		result = prime * result + ((val == null) ? 0 : val.hashCode());
		return result;
		}

	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Argument)) return false;
		Argument other = (Argument) obj;
		if (kind != other.kind) return false;
		if (res == null)
			{
			if (other.res != null) return false;
			}
		else if (!res.equals(other.res)) return false;
		if (val == null)
			{
			if (other.val != null) return false;
			}
		else if (!val.equals(other.val)) return false;
		return true;
		}
	}
