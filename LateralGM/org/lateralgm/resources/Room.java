/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.ArrayList;

import org.lateralgm.file.Gm6File;
import org.lateralgm.file.ResourceList;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

public class Room extends Resource
	{
	public static final byte TAB_OBJECTS = 0;
	public static final byte TAB_SETTINGS = 1;
	public static final byte TAB_TILES = 2;
	public static final byte TAB_BACKGROUNDS = 3;
	public static final byte TAB_VIEWS = 4;
	public String Caption = "";
	public int Width = 640;
	public int Height = 480;
	public int SnapX = 16;
	public int SnapY = 16;
	public boolean IsometricGrid = false;
	public int Speed = 30;
	public boolean Persistent = false;
	public int BackgroundColor = 0x000000;
	public boolean DrawBackgroundColor = true;
	public String CreationCode = "";
	public boolean RememberWindowSize = true;
	public int EditorWidth = 200;// **may not be relevant to swing, or may not produce the same effect in the
	// LGM GUI
	public int EditorHeight = 200;// **
	public boolean ShowGrid = true;
	public boolean ShowObjects = true;
	public boolean ShowTiles = true;
	public boolean ShowBackgrounds = true;
	public boolean ShowForegrounds = true;
	public boolean ShowViews = false;
	public boolean DeleteUnderlyingObjects = true;
	public boolean DeleteUnderlyingTiles = true;
	public int CurrentTab = TAB_OBJECTS;
	public int ScrollBarX = 0;// **
	public int ScrollBarY = 0;// **
	public BackgroundDef[] BackgroundDefs = new BackgroundDef[8];
	public View[] Views = new View[8];
	public boolean EnableViews = false;
	private ArrayList<Instance> Instances = new ArrayList<Instance>();
	private Gm6File Parent;

	public Room()
		{
		setName(Prefs.prefixes[Resource.ROOM]);
		Parent = LGM.currentFile;
		for (int j = 0; j < 8; j++)
			{
			Views[j] = new View();
			BackgroundDefs[j] = new BackgroundDef();
			}
		}

	public Room(Gm6File Parent)// Rooms are special - they need to know what file they belong to
		{
		this.Parent = Parent;
		for (int j = 0; j < 8; j++)
			{
			Views[j] = new View();
			BackgroundDefs[j] = new BackgroundDef();
			}
		}

	public int NoInstances()
		{
		return Instances.size();
		}

	private ArrayList<Tile> Tiles = new ArrayList<Tile>();

	public int NoTiles()
		{
		return Tiles.size();
		}

	public Instance addInstance()
		{
		Instance inst = new Instance();
		inst.InstanceId = ++Parent.LastInstanceId;
		Instances.add(inst);
		return inst;
		}

	public Instance getInstance(int InstanceId)
		{
		int Index = InstanceIndex(InstanceId);
		if (Index != -1) return Instances.get(Index);
		return null;
		}

	public int InstanceIndex(int InstanceId)
		{
		for (int i = 0; i < NoInstances(); i++)
			{
			if (getInstanceList(i).InstanceId == InstanceId) return i;
			}
		return -1;
		}

	public Instance getInstanceList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoInstances()) return Instances.get(ListIndex);
		return null;
		}

	public void removeInstance(int InstanceId)
		{
		int Index = InstanceIndex(InstanceId);
		if (Index != -1) Instances.remove(Index);
		}

	public void clearInstances()
		{
		Instances.clear();
		}

	public Tile addTile()
		{
		Tile tile = new Tile();
		tile.TileId = ++Parent.LastTileId;
		Tiles.add(tile);
		return tile;
		}

	public Tile getTile(int TileId)
		{
		int Index = TileIndex(TileId);
		if (Index != -1) return Tiles.get(Index);
		return null;
		}

	public int TileIndex(int TileId)
		{
		for (int i = 0; i < NoTiles(); i++)
			{
			if (getTileList(i).TileId == TileId) return i;
			}
		return -1;
		}

	public Tile getTileList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoTiles()) return Tiles.get(ListIndex);
		return null;
		}

	public void removeTile(int TileId)
		{
		int Index = TileIndex(TileId);
		if (Index != -1) Tiles.remove(Index);
		}

	public void clearTiles()
		{
		Tiles.clear();
		}

	@SuppressWarnings("unchecked")
	public Room copy(boolean update, ResourceList src)
		{
		Room rm = new Room();
		rm = new Room();
		rm.Caption = Caption;
		rm.Width = Width;
		rm.Height = Height;
		rm.SnapX = SnapX;
		rm.SnapY = SnapY;
		rm.IsometricGrid = IsometricGrid;
		rm.Speed = Speed;
		rm.Persistent = Persistent;
		rm.BackgroundColor = BackgroundColor;
		rm.DrawBackgroundColor = DrawBackgroundColor;
		rm.CreationCode = CreationCode;
		rm.RememberWindowSize = RememberWindowSize;
		rm.EditorWidth = EditorWidth;
		rm.EditorHeight = EditorHeight;
		rm.ShowGrid = ShowGrid;
		rm.ShowObjects = ShowObjects;
		rm.ShowTiles = ShowTiles;
		rm.ShowBackgrounds = ShowBackgrounds;
		rm.ShowForegrounds = ShowForegrounds;
		rm.ShowViews = ShowViews;
		rm.DeleteUnderlyingObjects = DeleteUnderlyingObjects;
		rm.DeleteUnderlyingTiles = DeleteUnderlyingTiles;
		rm.CurrentTab = CurrentTab;
		rm.ScrollBarX = ScrollBarX;
		rm.ScrollBarY = ScrollBarY;
		rm.EnableViews = EnableViews;
		for (int i = 0; i < this.NoInstances(); i++)
			{
			Instance inst = getInstanceList(i);
			Instance inst2 = rm.addInstance();
			inst2.CreationCode = inst.CreationCode;
			inst2.Locked = inst.Locked;
			inst2.GmObjectId = inst.GmObjectId;
			inst2.X = inst.X;
			inst2.Y = inst.Y;
			}
		for (int i = 0; i < this.NoTiles(); i++)
			{
			Tile tile = getTileList(i);
			Tile tile2 = rm.addTile();
			tile2.BackgroundId = tile.BackgroundId;
			tile2.Depth = tile.Depth;
			tile2.Height = tile.Height;
			tile2.Locked = tile.Locked;
			tile2.TileX = tile.TileX;
			tile2.TileY = tile.TileY;
			tile2.Width = tile.Width;
			tile2.X = tile.X;
			tile2.Y = tile.Y;
			}
		for (int i = 0; i < 8; i++)
			{
			View view = Views[i];
			View view2 = rm.Views[i];
			view2.Enabled = view.Enabled;
			view2.ViewX = view.ViewX;
			view2.ViewY = view.ViewY;
			view2.ViewW = view.ViewW;
			view2.ViewH = view.ViewH;
			view2.PortX = view.PortX;
			view2.PortY = view.PortY;
			view2.PortW = view.PortW;
			view2.PortH = view.PortH;
			view2.Hbor = view.Hbor;
			view2.VBor = view.VBor;
			view2.HSpeed = view.HSpeed;
			view2.VSpeed = view.VSpeed;
			view2.ObjectFollowing = view.ObjectFollowing;
			}
		for (int i = 0; i < 8; i++)
			{
			BackgroundDef back = BackgroundDefs[i];
			BackgroundDef back2 = rm.BackgroundDefs[i];
			back2.Visible = back.Visible;
			back2.Foreground = back.Foreground;
			back2.BackgroundId = back.BackgroundId;
			back2.X = back.X;
			back2.Y = back.Y;
			back2.TileHoriz = back.TileHoriz;
			back2.TileVert = back.TileVert;
			back2.HorizSpeed = back.HorizSpeed;
			back2.VertSpeed = back.VertSpeed;
			back2.Stretch = back.Stretch;
			}
		if (update)
			{
			rm.setId(new ResId(++src.LastId));
			rm.setName(Prefs.prefixes[Resource.ROOM] + src.LastId);
			src.add(rm);
			}
		else
			{
			rm.setId(getId());
			rm.setName(getName());
			}
		return rm;
		}
	}