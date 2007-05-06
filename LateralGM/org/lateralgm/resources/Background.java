/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.image.BufferedImage;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;

public class Background extends Resource
	{
	public int Width = 0;
	public int Height = 0;
	public boolean Transparent = false;
	public boolean SmoothEdges = false;
	public boolean Preload = false;
	public boolean UseAsTileSet = false;
	public int TileWidth = 16;
	public int TileHeight = 16;
	public int HorizOffset = 0;
	public int VertOffset = 0;
	public int HorizSep = 0;
	public int VertSep = 0;
	public BufferedImage BackgroundImage = null;

	public Background()
		{
		name = Prefs.prefixes[Resource.BACKGROUND];
		}

	public BufferedImage copyBackgroundImage()
		{
		if (BackgroundImage != null)
			{
			BufferedImage bf = BackgroundImage;
			BufferedImage bf2 = new BufferedImage(bf.getWidth(),bf.getHeight(),bf.getType());
			bf2.setData(bf.getData());
			return bf2;
			}
		return null;
		}

	@SuppressWarnings("unchecked")
	public Background copy(boolean update, ResourceList src)
		{
		Background back = new Background();
		back.Width = Width;
		back.Height = Height;
		back.Transparent = Transparent;
		back.SmoothEdges = SmoothEdges;
		back.Preload = Preload;
		back.UseAsTileSet = UseAsTileSet;
		back.TileWidth = TileWidth;
		back.TileHeight = TileHeight;
		back.HorizOffset = HorizOffset;
		back.VertOffset = VertOffset;
		back.HorizSep = HorizSep;
		back.VertSep = VertSep;
		back.BackgroundImage = copyBackgroundImage();
		if (update)
			{
			back.Id.value = ++src.LastId;
			back.name = Prefs.prefixes[Resource.BACKGROUND] + src.LastId;
			src.add(back);
			}
		else
			{
			back.Id = Id;
			back.name = name;
			}
		return back;
		}
	}