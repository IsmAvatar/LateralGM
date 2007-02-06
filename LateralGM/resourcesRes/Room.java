package resourcesRes;

import java.util.ArrayList;

import mainRes.LGM;
import resourcesRes.subRes.BackgroundDef;
import resourcesRes.subRes.Instance;
import resourcesRes.subRes.Tile;
import resourcesRes.subRes.View;

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
	public int EditorWidth = 200;// **may not be relevant to swing, or may not produce the same effect in the LGM GUI
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

	public int NoInstances()
		{
		return Instances.size();
		}

	private ArrayList<Tile> Tiles = new ArrayList<Tile>();

	public int NoTiles()
		{
		return Tiles.size();
		}

	public Room()
		{
		for (int j = 0; j < 8; j++)
			{
			Views[j] = new View();
			BackgroundDefs[j] = new BackgroundDef();
			}
		}

	public Instance addInstance()
		{
		Instance inst = new Instance();
		Instances.add(inst);
		LGM.currentFile.LastInstanceId++;
		inst.InstanceId = LGM.currentFile.LastInstanceId;
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
		Tiles.add(tile);
		LGM.currentFile.LastTileId++;
		tile.TileId = LGM.currentFile.LastTileId;
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
	}