package fileRes;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import resourcesRes.Background;
import resourcesRes.Constant;
import resourcesRes.Font;
import resourcesRes.GameInformation;
import resourcesRes.GmObject;
import resourcesRes.Include;
import resourcesRes.Path;
import resourcesRes.ResId;
import resourcesRes.Resource;
import resourcesRes.Room;
import resourcesRes.Script;
import resourcesRes.Sound;
import resourcesRes.Sprite;
import resourcesRes.Timeline;
import resourcesRes.subRes.Action;
import resourcesRes.subRes.Argument;
import resourcesRes.subRes.BackgroundDef;
import resourcesRes.subRes.Event;
import resourcesRes.subRes.Instance;
import resourcesRes.subRes.MainEvent;
import resourcesRes.subRes.Moment;
import resourcesRes.subRes.Point;
import resourcesRes.subRes.Tile;
import resourcesRes.subRes.View;

import componentRes.ResNode;

//TODO: LoadGm6File.ICO line 1613
public class Gm6File
	{
	private class stopWatch
		{
		private long starttime = 0;
		private long stoptime = 0;
		boolean running = false;

		public void start()
			{
			starttime = System.currentTimeMillis();
			running = true;
			}

		public void stop()
			{
			stoptime = System.currentTimeMillis();
			running = false;
			}

		public long getElapsed()
			{
			if (running)
				{
				return System.currentTimeMillis() - starttime;
				}
			else
				return stoptime - starttime;
			}
		}

	private class idStack // allows pointing to the resId of a resource even when the resource that "owns" it is
	// yet to exist
		{
		private ArrayList<ResId> ids = new ArrayList<ResId>();

		public ResId get(int id)
			{
			if (id < 0)
				{
				return null;
				}
			for (int i = 0; i < ids.size(); i++)
				{
				if (ids.get(i).value == id)
					{
					return ids.get(i);
					}
				}
			ResId newid = new ResId(id);
			ids.add(newid);
			return newid;
			}
		}

	public static int[] lastId = new int[15];
	private static final Map<Integer,List<Resource>> resMap;
	static
	{
		Map<Integer,List<Resource>> map = new HashMap<Integer,List<Resource>>();
		for (int m = 0; m < 15; m++)
			{
			lastId[m] = -1;
			if (m != 0 && m != 5 && m != 10 && m != 11)
				{
				map.put(Integer.valueOf(m),new ArrayList<Resource>());
				}
			}
		resMap = Collections.unmodifiableMap(map);
	}
	public ArrayList<Constant> constants = new ArrayList<Constant>();
	public ArrayList<Include> includes = new ArrayList<Include>();

	public Gm6File()
		{
		GameId = new Random().nextInt(100000001);
		GameIconData = new byte[0];
		try
			{
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(
					new File(Gm6File.class.getResource("default.ico").toURI())));
			ByteArrayOutputStream dat = new ByteArrayOutputStream();
			int val = in.read();
			while (val != -1)
				{
				dat.write(val);
				val = in.read();
				}
			GameIconData = dat.toByteArray();
			}
		catch (Exception ex)
			{
			GameIconData = new byte[0];
			System.err
					.println("default icon not found, any saved files will have no icon unless one is assigned manually.");
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			}
		}

	// <editor-fold defaultstate="collapsed" desc=" Constants For Resource Properties ">
	public String[] prefixes = { "","obj_","spr_","snd_","rm_","","bk_","scr_","path_","font_","","","time_" };
	//TODO add option to interface

	public static final byte COLOR_NOCHANGE = 0;
	public static final byte COLOR_16 = 1;
	public static final byte COLOR_32 = 2;
	public static final byte RES_NOCHANGE = 0;
	public static final byte RES_320X240 = 1;
	public static final byte RES_640X480 = 2;
	public static final byte RES_800X600 = 3;
	public static final byte RES_1024X768 = 4;
	public static final byte RES_1280X1024 = 5;
	public static final byte RES_1600X1200 = 6;
	public static final byte FREQ_NOCHANGE = 0;
	public static final byte FREQ_60 = 1;
	public static final byte FREQ_70 = 2;
	public static final byte FREQ_85 = 3;
	public static final byte FREQ_100 = 4;
	public static final byte FREQ_120 = 5;
	public static final byte PRIORITY_NORMAL = 0;
	public static final byte PRIORITY_HIGH = 1;
	public static final byte PRIORITY_HIGHEST = 2;
	public static final byte LOADBAR_NONE = 0;
	public static final byte LOADBAR_DEFAULT = 1;
	public static final byte LOADBAR_CUSTOM = 2;
	public static final byte INCLUDE_MAIN = 0;
	public static final byte INCLUDE_TEMP = 1;

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" File Properties ">
	public int GameId;// randomized in constructor
	public boolean StartFullscreen = false;
	public boolean Interpolate = false;
	public boolean DontDrawBorder = false;
	public boolean DisplayCursor = true;
	public int Scaling = -1;
	public boolean AllowWindowResize = false;
	public boolean AlwaysOnTop = false;
	public int ColorOutsideRoom = 0;
	public boolean SetResolution = false;
	public byte ColorDepth = COLOR_NOCHANGE;
	public byte Resolution = RES_NOCHANGE;
	public byte Frequency = FREQ_NOCHANGE;
	public boolean DontShowButtons = false;
	public boolean UseSynchronization = false;
	public boolean LetF4SwitchFullscreen = true;
	public boolean LetF1ShowGameInfo = true;
	public boolean LetEscEndGame = true;
	public boolean LetF5SaveF6Load = true;
	public byte GamePriority = PRIORITY_NORMAL;
	public boolean FreezeOnLoseFocus = false;
	public byte LoadBarMode = LOADBAR_DEFAULT;
	public BufferedImage FrontLoadBar = null;
	public BufferedImage BackLoadBar = null;
	public boolean ShowCustomLoadImage = false;
	public BufferedImage LoadingImage = null;
	public boolean ImagePartiallyTransparent = false;
	public int LoadImageAlpha = 255;
	public boolean ScaleProgressBar = true;
	public boolean DisplayErrors = true;
	public boolean WriteToLog = false;
	public boolean AbortOnError = false;
	public boolean TreatUninitializedAs0 = false;
	public String Author = "";
	public int Version = 100;
	public double LastChanged = 0;
	public String Information = "";
	public int IncludeFolder = INCLUDE_MAIN;
	public boolean OverwriteExisting = false;
	public boolean RemoveAtGameEnd = false;
	public int LastInstanceId = 100000;
	public int LastTileId = 10000000;
	public byte[] GameIconData;// actual data is stored to be written on resave (no reason to re-encode)
	public BufferedImage GameIcon;// icon as image for display purposes

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Sprite Functions ">

	public int getCount(int res)
		{
		return resMap.get(res).size();
		}
	public Resource add(int res)
		{
		Resource r = null;
		switch (res)
			{
			case Resource.SPRITE: r = new Sprite(); break;
			case Resource.SOUND: r = new Sound(); break;
			case Resource.BACKGROUND: r = new Background(); break;
			case Resource.PATH: r = new Path(); break;
			case Resource.SCRIPT: r = new Script(); break;
			case Resource.FONT: r = new Font(); break;
			case Resource.TIMELINE: r = new Timeline(); break;
			case Resource.GMOBJECT: r = new GmObject(); break;
			case Resource.ROOM: r = new Room(); break;
			}
		r.Id.value = lastId[res]++;
		resMap.get(res).add(r);
		return r;
		}
	public Resource getUnsafe(int res, int id)
		{
		for (Resource r : resMap.get(res))
			if (r.Id.value == id)
				return r;
		return null;
		}
	public Resource get(int res, ResId id)
		{
		int listIndex = getIndex(res,id);
		if (listIndex != -1) return resMap.get(res).get(listIndex);
		return null;
		}
	public Resource get(int res, String name)
		{
		int listIndex = getIndex(res,name);
		if (listIndex != -1) return resMap.get(res).get(listIndex);
		return null;
		}
/*	public Resource getList(int res, int listIndex)
		{
		if (listIndex >= 0 && listIndex < getCount(res)) return resMap.get(res).get(listIndex);
		return null;
		}*/
	public void remove(int res, ResId id)
		{
		int listIndex = getIndex(res,id);
		if (listIndex != -1) resMap.get(res).remove(listIndex);
		}
	public void remove(int res, String name)
		{
		int listIndex = getIndex(res,name);
		if (listIndex != -1) resMap.get(res).remove(listIndex);
		}
	public int getIndex(int res, ResId id)
		{
		for (int i = 0; i < getCount(res); i++)
			{
			if (resMap.get(res).get(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}
	public int getIndex(int res, String name)
		{
		for (int i = 0; i < getCount(res); i++)
			{
			if (resMap.get(res).get(i).name.equals(name))
				{
				return i;
				}
			}
		return -1;
		}
	public void clear(int res)
		{
		resMap.get(res).clear();
		}
	public void clearAll()
		{
		for (List l : resMap.values())
			l.clear();
		}
	public void sort(int res)
		{
		Collections.sort(resMap.get(res));
		}
	public void replaceSprite(int res, ResId srcId, Sprite replacement)
		{
		int ind = getIndex(res,srcId);
		if (replacement != null && ind >= 0)
			{
			resMap.get(res).set(ind,replacement);
			}
		}

	public Resource duplicate(int res, ResId id, boolean update)
		{
		Resource r2 = null;
		Resource r = get(res,id);
		if (r == null) return r2;
		
		return r2;
		}

	public Sprite duplicateSprite(ResId id,boolean update)
		{
		Sprite spr2 = null;
		Sprite spr = (Sprite)get(Resource.SPRITE,id);
		if (spr != null)
			{
			spr2 = new Sprite();
			spr2.Width = spr.Width;
			spr2.Height = spr.Height;
			spr2.Transparent = spr.Transparent;
			spr2.PreciseCC = spr.PreciseCC;
			spr2.SmoothEdges = spr.SmoothEdges;
			spr2.Preload = spr.Preload;
			spr2.OriginX = spr.OriginX;
			spr2.OriginY = spr.OriginY;
			spr2.BoundingBoxMode = spr.BoundingBoxMode;
			spr2.BoundingBoxLeft = spr.BoundingBoxLeft;
			spr2.BoundingBoxRight = spr.BoundingBoxRight;
			spr2.BoundingBoxTop = spr.BoundingBoxTop;
			spr2.BoundingBoxBottom = spr.BoundingBoxBottom;
			for (int j = 0; j < spr.NoSubImages(); j++)
				{
				spr2.addSubImage(spr.copySubImage(j));
				}
			if (update)
				{
				lastId[Resource.SPRITE]++;
				spr2.Id.value = lastId[Resource.SPRITE];
				spr2.name = prefixes[Resource.SPRITE] + lastId[Resource.SPRITE];
				resMap.get(Resource.SPRITE).add(spr2);
				}
			else
				{
				spr2.Id = spr.Id;
				spr2.name = spr.name;
				}
			}
		return spr2;
		}

	public Sound duplicateSound(ResId id,boolean update)
		{
		Sound snd2 = null;
		Sound snd = (Sound)get(Resource.SOUND,id);
		if (snd != null)
			{
			snd2 = new Sound();
			snd2.Type = snd.Type;
			snd2.FileType = snd.FileType;
			snd2.FileName = snd.FileName;
			snd2.Chorus = snd.Chorus;
			snd2.Echo = snd.Echo;
			snd2.Flanger = snd.Flanger;
			snd2.Gargle = snd.Gargle;
			snd2.Reverb = snd.Reverb;
			snd2.Volume = snd.Volume;
			snd2.Pan = snd.Pan;
			snd2.Preload = snd.Preload;
			snd2.Data = new byte[snd.Data.length];
			System.arraycopy(snd.Data,0,snd2.Data,0,snd.Data.length);
			if (update)
				{
				lastId[Resource.SOUND]++;
				snd2.Id.value = lastId[Resource.SOUND];
				snd2.name = prefixes[Resource.SOUND] + lastId[Resource.SOUND];
				resMap.get(Resource.SOUND).add(snd2);
				}
			else
				{
				snd2.Id = snd.Id;
				snd2.name = snd.name;
				}
			}
		return snd2;
		}

	public Background duplicateBackground(ResId id,boolean update)
		{
		Background back2 = null;
		Background back = (Background)get(Resource.BACKGROUND,id);
		if (back != null)
			{
			back2 = new Background();
			back2.Width = back.Width;
			back2.Height = back.Height;
			back2.Transparent = back.Transparent;
			back2.SmoothEdges = back.SmoothEdges;
			back2.Preload = back.Preload;
			back2.UseAsTileSet = back.UseAsTileSet;
			back2.TileWidth = back.TileWidth;
			back2.TileHeight = back.TileHeight;
			back2.HorizOffset = back.HorizOffset;
			back2.VertOffset = back.VertOffset;
			back2.HorizSep = back.HorizSep;
			back2.VertSep = back.VertSep;
			back2.BackgroundImage = back.copyBackgroundImage();
			if (update)
				{
				lastId[Resource.BACKGROUND]++;
				back2.Id.value = lastId[Resource.BACKGROUND];
				back2.name = prefixes[Resource.BACKGROUND] + lastId[Resource.BACKGROUND];
				resMap.get(Resource.BACKGROUND).add(back2);
				}
			else
				{
				back2.Id = back.Id;
				back2.name = back.name;
				}
			}
		return back2;
		}

	public Path duplicatePath(ResId id,boolean update)
		{
		Path path2 = null;
		Path path = (Path)get(Resource.PATH,id);
		if (path != null)
			{
			path2 = new Path();
			path2.Smooth = path.Smooth;
			path2.Closed = path.Closed;
			path2.Precision = path.Precision;
			path2.BackgroundRoom = path.BackgroundRoom;
			path2.SnapX = path.SnapX;
			path2.SnapY = path.SnapY;
			for (int i = 0; i < path.NoPoints(); i++)
				{
				Point point2 = path2.addPoint();
				Point point = path.getPoint(i);
				point2.X = point.X;
				point2.Y = point.Y;
				point2.Speed = point.Speed;
				}
			if (update)
				{
				lastId[Resource.PATH]++;
				path2.Id.value = lastId[Resource.PATH];
				path2.name = prefixes[Resource.PATH] + lastId[Resource.PATH];
				resMap.get(Resource.PATH).add(path2);
				}
			else
				{
				path2.Id = path.Id;
				path2.name = path.name;
				}
			}
		return path2;
		}
	public Script duplicateScript(ResId id,boolean update)
		{
		Script scr2 = null;
		Script scr = (Script)get(Resource.SCRIPT,id);
		if (scr != null)
			{
			scr2 = new Script();
			scr2.ScriptStr = scr.ScriptStr;
			if (update)
				{
				lastId[Resource.SCRIPT]++;
				scr2.Id.value = lastId[Resource.SCRIPT];
				scr2.name = prefixes[Resource.SCRIPT] + lastId[Resource.SCRIPT];
				resMap.get(Resource.SCRIPT).add(scr2);
				}
			else
				{
				scr2.Id = scr.Id;
				scr2.name = scr.name;
				}
			}
		return scr2;
		}
	public Font duplicateFont(ResId id,boolean update)
		{
		Font font2 = null;
		Font font = (Font)get(Resource.FONT,id);
		if (font != null)
			{
			font2 = new Font();
			font2.FontName = font.FontName;
			font2.Size = font.Size;
			font2.Bold = font.Bold;
			font2.Italic = font.Italic;
			font2.CharRangeMin = font.CharRangeMin;
			font2.CharRangeMax = font.CharRangeMax;
			if (update)
				{
				lastId[Resource.FONT]++;
				font2.Id.value = lastId[Resource.FONT];
				font2.name = prefixes[Resource.FONT] + lastId[Resource.FONT];
				resMap.get(Resource.FONT).add(font2);
				}
			else
				{
				font2.Id = font.Id;
				font2.name = font.name;
				}
			}
		return font2;
		}
	public Timeline duplicateTimeline(ResId id,boolean update)
		{
		Timeline time2 = null;
		Timeline time = (Timeline)get(Resource.TIMELINE,id);
		if (time != null)
			{
			time2 = new Timeline();
			for (int i = 0; i < time.NoMoments(); i++)
				{
				Moment mom = time.getMomentList(i);
				Moment mom2 = time2.addMoment();
				mom2.stepNo = mom.stepNo;
				for (int j = 0; j < mom.NoActions(); j++)
					{
					Action act = mom.getAction(j);
					Action act2 = mom2.addAction();
					act2.LibraryId = act.LibraryId;
					act2.LibActionId = act.LibActionId;
					act2.ActionKind = act.ActionKind;
					act2.AllowRelative = act.AllowRelative;
					act2.Question = act.Question;
					act2.CanApplyTo = act.CanApplyTo;
					act2.ExecType = act.ExecType;
					act2.ExecFunction = act.ExecFunction;
					act2.ExecCode = act.ExecCode;
					act2.Relative = act.Relative;
					act2.Not = act.Not;
					act2.AppliesTo = act.AppliesTo;
					act2.NoArguments = act.NoArguments;
					for (int k = 0; k < act.NoArguments; k++)
						{
						act2.Arguments[k].Kind = act.Arguments[k].Kind;
						act2.Arguments[k].Res = act.Arguments[k].Res;
						act2.Arguments[k].Val = act.Arguments[k].Val;
						}
					}
				}
			if (update)
				{
				lastId[Resource.TIMELINE]++;
				time2.Id.value = lastId[Resource.TIMELINE];
				time2.name = prefixes[Resource.TIMELINE] + lastId[Resource.TIMELINE];
				resMap.get(Resource.TIMELINE).add(time2);
				}
			else
				{
				time2.Id = time.Id;
				time2.name = time.name;
				}
			}
		return time2;
		}
	public GmObject duplicateGmObject(ResId id,boolean update)
		{
		GmObject obj2 = null;
		GmObject obj = (GmObject)get(Resource.GMOBJECT,id);
		if (obj != null)
			{
			obj2 = new GmObject();
			obj2.Sprite = obj.Sprite;
			obj2.Solid = obj.Solid;
			obj2.Visible = obj.Visible;
			obj2.Depth = obj.Depth;
			obj2.Persistent = obj.Persistent;
			obj2.Parent = obj.Parent;
			obj2.Mask = obj.Mask;
			for (int i = 0; i < 11; i++)
				{
				MainEvent mev = obj.MainEvents[i];
				MainEvent mev2 = obj2.MainEvents[i];
				for (int j = 0; j < mev.NoEvents(); j++)
					{
					Event ev = mev.getEventList(j);
					Event ev2 = mev2.addEvent();
					ev2.Id = ev.Id;
					for (int k = 0; k < ev.NoActions(); k++)
						{
						Action act = ev.getAction(k);
						Action act2 = ev2.addAction();
						act2.LibraryId = act.LibraryId;
						act2.LibActionId = act.LibActionId;
						act2.ActionKind = act.ActionKind;
						act2.AllowRelative = act.AllowRelative;
						act2.Question = act.Question;
						act2.CanApplyTo = act.CanApplyTo;
						act2.ExecType = act.ExecType;
						act2.ExecFunction = act.ExecFunction;
						act2.ExecCode = act.ExecCode;
						act2.Relative = act.Relative;
						act2.Not = act.Not;
						act2.AppliesTo = act.AppliesTo;
						act2.NoArguments = act.NoArguments;
						for (int l = 0; l < act.NoArguments; l++)
							{
							act2.Arguments[k].Kind = act.Arguments[k].Kind;
							act2.Arguments[k].Res = act.Arguments[k].Res;
							act2.Arguments[k].Val = act.Arguments[k].Val;
							}
						}
					}
				}
			if (update)
				{
				lastId[Resource.GMOBJECT]++;
				obj2.Id.value = lastId[Resource.GMOBJECT];
				obj2.name = prefixes[Resource.GMOBJECT] + lastId[Resource.GMOBJECT];
				resMap.get(Resource.GMOBJECT).add(obj2);
				}
			else
				{
				obj2.Id = obj.Id;
				obj2.name = obj.name;
				}
			}
		return obj2;
		}
	public Room duplicateRoom(ResId id,boolean update)
		{
		Room rm2 = null;
		Room rm = (Room)get(Resource.ROOM,id);
		if (rm != null)
			{
			rm2 = new Room();
			rm2.Caption = rm.Caption;
			rm2.Width = rm.Width;
			rm2.Height = rm.Height;
			rm2.SnapX = rm.SnapX;
			rm2.SnapY = rm.SnapY;
			rm2.IsometricGrid = rm.IsometricGrid;
			rm2.Speed = rm.Speed;
			rm2.Persistent = rm.Persistent;
			rm2.BackgroundColor = rm.BackgroundColor;
			rm2.DrawBackgroundColor = rm.DrawBackgroundColor;
			rm2.CreationCode = rm.CreationCode;
			rm2.RememberWindowSize = rm.RememberWindowSize;
			rm2.EditorWidth = rm.EditorWidth;
			rm2.EditorHeight = rm.EditorHeight;
			rm2.ShowGrid = rm.ShowGrid;
			rm2.ShowObjects = rm.ShowObjects;
			rm2.ShowTiles = rm.ShowTiles;
			rm2.ShowBackgrounds = rm.ShowBackgrounds;
			rm2.ShowForegrounds = rm.ShowForegrounds;
			rm2.ShowViews = rm.ShowViews;
			rm2.DeleteUnderlyingObjects = rm.DeleteUnderlyingObjects;
			rm2.DeleteUnderlyingTiles = rm.DeleteUnderlyingTiles;
			rm2.CurrentTab = rm.CurrentTab;
			rm2.ScrollBarX = rm.ScrollBarX;
			rm2.ScrollBarY = rm.ScrollBarY;
			rm2.EnableViews = rm.EnableViews;
			for (int i = 0; i < rm.NoInstances(); i++)
				{
				Instance inst = rm.getInstanceList(i);
				Instance inst2 = rm2.addInstance();
				inst2.CreationCode = inst.CreationCode;
				inst2.Locked = inst.Locked;
				inst2.GmObjectId = inst.GmObjectId;
				inst2.X = inst.X;
				inst2.Y = inst.Y;
				}
			for (int i = 0; i < rm.NoTiles(); i++)
				{
				Tile tile = rm.getTileList(i);
				Tile tile2 = rm2.addTile();
				tile2.BackgroundId = tile.BackgroundId;
				tile2.Depth = tile.Depth;
				tile2.Height = tile.Height;
				tile2.Locked = tile.Locked;
				tile2.TileId = LastTileId;
				tile2.TileX = tile.TileX;
				tile2.TileY = tile.TileY;
				tile2.Width = tile.Width;
				tile2.X = tile.X;
				tile2.Y = tile.Y;
				}
			for (int i = 0; i < 8; i++)
				{
				View view = rm.Views[i];
				View view2 = rm2.Views[i];
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
				BackgroundDef back = rm.BackgroundDefs[i];
				BackgroundDef back2 = rm2.BackgroundDefs[i];
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
				lastId[Resource.ROOM]++;
				rm2.Id.value = lastId[Resource.ROOM];
				rm2.name = prefixes[Resource.ROOM] + lastId[Resource.ROOM];
				resMap.get(Resource.ROOM).add(rm2);
				}
			else
				{
				rm2.Id = rm.Id;
				rm2.name = rm.name;
				}
			}
		return rm2;
		}

	public static GameInformation GameInfo = new GameInformation();

	public void ReadGm6File(String FileName,ResNode root) throws Gm6FormatException
		{
		clearAll();
		GmStreamDecoder in = null;
		try
			{
			stopWatch clock = new stopWatch();
			clock.start();
			in = new GmStreamDecoder(FileName);
			idStack timeids = new idStack(); // timeline ids
			idStack objids = new idStack(); // object ids
			idStack rmids = new idStack(); // room ids
			int identifier = in.readi();
			if (identifier != 1234321)
				throw new Gm6FormatException(FileName + " is not a valid gm6 file, initial identifier is invalid: "
						+ identifier);
			int ver = in.readi();
			if (ver != 600) throw new Gm6FormatException("GM version unsupported or file corrupt: " + ver);
			GameId = in.readi();
			in.skip(16);// unknown bytes following game id
			ver = in.readi();
			if (ver != 600)
				throw new Gm6FormatException("GM version unsupported or file corrupt: " + ver);
			StartFullscreen = in.readBool();
			Interpolate = in.readBool();
			DontDrawBorder = in.readBool();
			DisplayCursor = in.readBool();
			Scaling = in.readi();
			AllowWindowResize = in.readBool();
			AlwaysOnTop = in.readBool();
			ColorOutsideRoom = in.readi();
			SetResolution = in.readBool();
			ColorDepth = (byte) in.readi();
			Resolution = (byte) in.readi();
			Frequency = (byte) in.readi();
			DontShowButtons = in.readBool();
			UseSynchronization = in.readBool();
			LetF4SwitchFullscreen = in.readBool();
			LetF1ShowGameInfo = in.readBool();
			LetEscEndGame = in.readBool();
			LetF5SaveF6Load = in.readBool();
			GamePriority = (byte) in.readi();
			FreezeOnLoseFocus = in.readBool();
			LoadBarMode = (byte) in.readi();
			if (LoadBarMode == LOADBAR_CUSTOM)
				{
				if (in.readi() != -1)
					BackLoadBar = in.readImage();
				if (in.readi() != -1)
					FrontLoadBar = in.readImage();
				}
			ShowCustomLoadImage = in.readBool();
			if (ShowCustomLoadImage)
				if (in.readi() != -1)
					LoadingImage = in.readImage();
			ImagePartiallyTransparent = in.readBool();
			LoadImageAlpha = in.readi();
			ScaleProgressBar = in.readBool();

			int length = in.readi();
			GameIconData = new byte[length];
			in.read(GameIconData,0,length);
			// GameIcon=(BufferedImage)new ICOFile(new
			// ByteArrayInputStream(GameIconData)).getDescriptor(0).getImageRGB();

			DisplayErrors = in.readBool();
			WriteToLog = in.readBool();
			AbortOnError = in.readBool();
			TreatUninitializedAs0 = in.readBool();
			Author = in.readStr();
			Version = in.readi();
			LastChanged = in.readD();
			Information = in.readStr();
			int no = in.readi();
			for (int i = 0; i < no; i++)
				{
				Constant con = new Constant();
				constants.add(con);
				con.name = in.readStr();
				con.value = in.readStr();
				}
			no = in.readi();
			for (int i = 0; i < no; i++)
				{
				Include inc = new Include();
				inc.filePath = in.readStr();
				}
			IncludeFolder = in.readi();
			OverwriteExisting = in.readBool();
			RemoveAtGameEnd = in.readBool();
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before sounds - GM version unsupported or file corrupt: " + ver);
			// SOUNDS
			no = in.readi();
			lastId[Resource.SOUND] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Sound snd = (Sound)add(Resource.SOUND);
					snd.name = in.readStr();
					ver = in.readi();
					if (ver != 600)
						throw new Gm6FormatException("In sound " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					snd.Type = (byte) in.readi();
					snd.FileType = in.readStr();
					snd.FileName = in.readStr();
					if (in.readBool())
						snd.Data = in.decompress(in.readi());
					int effects = in.readi();
					snd.Chorus = Sound.getChorus(effects);
					snd.Echo = Sound.getEcho(effects);
					snd.Flanger = Sound.getFlanger(effects);
					snd.Gargle = Sound.getGargle(effects);
					snd.Reverb = Sound.getReverb(effects);
					snd.Volume = in.readD();
					snd.Pan = in.readD();
					snd.Preload = in.readBool();
					}
				else
					lastId[Resource.SOUND]++;
				}

			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before Sprites - GM version unsupported or file corrupt: " + ver);
			// SPRITES
			no = in.readi();
			lastId[Resource.SPRITE] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Sprite spr = (Sprite)add(Resource.SPRITE);
					spr.name = in.readStr();
					ver = in.readi();
					if (ver != 542)
						throw new Gm6FormatException("In sprite " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					spr.Width = in.readi();
					spr.Height = in.readi();
					spr.BoundingBoxLeft = in.readi();
					spr.BoundingBoxRight = in.readi();
					spr.BoundingBoxBottom = in.readi();
					spr.BoundingBoxTop = in.readi();
					spr.Transparent = in.readBool();
					spr.SmoothEdges = in.readBool();
					spr.Preload = in.readBool();
					spr.BoundingBoxMode = (byte) in.readi();
					spr.PreciseCC = in.readBool();
					spr.OriginX = in.readi();
					spr.OriginY = in.readi();
					int nosub = in.readi();
					for (int j = 0; j < nosub; j++)
						{
						in.skip(4);
						spr.addSubImage(ImageIO.read(new ByteArrayInputStream(in.decompress(in.readi()))));
						}
					}
				else
					lastId[Resource.SPRITE]++;
				}

			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before Backgrounds - GM version unsupported or file corrupt: " + ver);
			// BACKGROUNDS
			no = in.readi();
			lastId[Resource.BACKGROUND] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Background back = (Background)add(Resource.BACKGROUND);
					back.name = in.readStr();
					ver = in.readi();
					if (ver != 543)
						throw new Gm6FormatException("In Background " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					back.Width = in.readi();
					back.Height = in.readi();
					back.Transparent = in.readBool();
					back.SmoothEdges = in.readBool();
					back.Preload = in.readBool();
					back.UseAsTileSet = in.readBool();
					back.TileWidth = in.readi();
					back.TileHeight = in.readi();
					back.HorizOffset = in.readi();
					back.VertOffset = in.readi();
					back.HorizSep = in.readi();
					back.VertSep = in.readi();
					if (in.readBool())
						{
						in.skip(4);// 0A
						back.BackgroundImage = ImageIO.read(new ByteArrayInputStream(in.decompress(in.readi())));
						}
					}
				else
					lastId[Resource.BACKGROUND]++;
				}
			ver = in.readi();
			if (ver != 420)
				throw new Gm6FormatException("Before Paths - GM version unsupported or file corrupt: " + ver);
			// PATHS
			no = in.readi();
			lastId[Resource.PATH] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Path path = (Path)add(Resource.PATH);
					path.name = in.readStr();
					ver = in.readi();
					if (ver != 530)
						throw new Gm6FormatException("In Path " + i + " - GM version unsupported or file corrupt: "
								+ ver);
					path.Smooth = in.readBool();
					path.Closed = in.readBool();
					path.Precision = in.readi();
					path.BackgroundRoom = rmids.get(in.readi());
					path.SnapX = in.readi();
					path.SnapY = in.readi();
					int nopoints = in.readi();
					for (int j = 0; j < nopoints; j++)
						{
						Point point = path.addPoint();
						point.X = (int) in.readD();
						point.Y = (int) in.readD();
						point.Speed = (int) in.readD();
						}
					}
				else
					lastId[Resource.PATH]++;
				}
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before Scripts - GM version unsupported or file corrupt: " + ver);
			// SCRIPTS
			no = in.readi();
			lastId[Resource.SCRIPT] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Script scr = (Script)add(Resource.SCRIPT);
					scr.name = in.readStr();
					ver = in.readi();
					if (ver != 400)
						throw new Gm6FormatException("In script " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					scr.ScriptStr = in.readStr();
					}
				else
					lastId[Resource.SCRIPT]++;
				}

			ver = in.readi();
			if (ver != 540)
				throw new Gm6FormatException("Before Fonts - GM version unsupported or file corrupt: " + ver);
			// FONTS
			no = in.readi();
			lastId[Resource.FONT] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Font font = (Font)add(Resource.FONT);
					font.name = in.readStr();
					ver = in.readi();
					if (ver != 540)
						throw new Gm6FormatException("In Font " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					font.FontName = in.readStr();
					font.Size = in.readi();
					font.Bold = in.readBool();
					font.Italic = in.readBool();
					font.CharRangeMin = in.readi();
					font.CharRangeMax = in.readi();
					}
				else
					lastId[Resource.FONT]++;
				}

			ver = in.readi();
			if (ver != 500)
				throw new Gm6FormatException("Before Timelines - GM version unsupported or file corrupt: " + ver);
			// TIMELINES
			no = in.readi();
			lastId[Resource.TIMELINE] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Timeline time = (Timeline)add(Resource.TIMELINE);
					time.Id = timeids.get(i);
					time.name = in.readStr();
					ver = in.readi();
					if (ver != 500)
						throw new Gm6FormatException("In Timeline " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					int nomoms = in.readi();
					for (int j = 0; j < nomoms; j++)
						{
						Moment mom = time.addMoment();
						mom.stepNo = in.readi();
						ver = in.readi();
						if (ver != 400)
							throw new Gm6FormatException("In Object " + i + ", Main event type " + j
									+ " - GM version unsupported or file corrupt: " + ver);
						int noacts = in.readi();
						for (int k = 0; k < noacts; k++)
							{
							in.skip(4);
							Action act = mom.addAction();
							act.LibraryId = in.readi();
							act.LibActionId = in.readi();
							act.ActionKind = (byte) in.readi();
							act.AllowRelative = in.readBool();
							act.Question = in.readBool();
							act.CanApplyTo = in.readBool();
							act.ExecType = (byte) in.readi();
							act.ExecFunction = in.readStr();
							act.ExecCode = in.readStr();
							act.NoArguments = in.readi();
							int[] argkinds = new int[in.readi()];
							for (int l = 0; l < argkinds.length; l++)
								argkinds[l] = in.readi();
							int id = in.readi();
							switch (id)
								{
								case -1:
									act.AppliesTo = GmObject.OBJECT_SELF;
									break;
								case -2:
									act.AppliesTo = GmObject.OBJECT_OTHER;
									break;
								default:
									act.AppliesTo = objids.get(id);
								}
							act.Relative = in.readBool();
							int actualnoargs = in.readi();
							for (int l = 0; l < actualnoargs; l++)
								{
								if (l < act.NoArguments)
									{
									act.Arguments[l].Kind = (byte) argkinds[l];
									String strval = in.readStr();
									switch (argkinds[l])
										{
										case Argument.ARG_SPRITE:
											act.Arguments[l].Res = getUnsafe(Resource.SPRITE,Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_SOUND:
											act.Arguments[l].Res = getUnsafe(Resource.SOUND,Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_BACKGROUND:
											act.Arguments[l].Res = getUnsafe(Resource.BACKGROUND,Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_PATH:
											act.Arguments[l].Res = getUnsafe(Resource.PATH,Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_SCRIPT:
											act.Arguments[l].Res = getUnsafe(Resource.SCRIPT,Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_GMOBJECT:
											act.Arguments[l].Res = objids.get(Integer.parseInt(strval));
											break;
										case Argument.ARG_ROOM:
											act.Arguments[l].Res = rmids.get(Integer.parseInt(strval));
											break;
										case Argument.ARG_FONT:
											act.Arguments[l].Res = getUnsafe(Resource.FONT,Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_TIMELINE:
											act.Arguments[l].Res = timeids.get(Integer.parseInt(strval));
											break;
										default:
											act.Arguments[l].Val = strval;
											break;
										}
									}
								else
									{
									length = in.readi();
									in.skip(length);
									}
								}
							act.Not = in.readBool();
							}
						}
					}
				else
					lastId[Resource.TIMELINE]++;
				}
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before Objects - GM version unsupported or file corrupt: " + ver);
			// OBJECTS
			no = in.readi();
			lastId[Resource.GMOBJECT] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					GmObject obj = (GmObject)add(Resource.GMOBJECT);
					obj.Id = objids.get(i);
					obj.name = in.readStr();
					ver = in.readi();
					if (ver != 430)
						throw new Gm6FormatException("In Object " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					int temp = in.readi();
					if (getUnsafe(Resource.SPRITE,temp) != null)
						obj.Sprite = getUnsafe(Resource.SPRITE,temp).Id;
					obj.Solid = in.readBool();
					obj.Visible = in.readBool();
					obj.Depth = in.readi();
					obj.Persistent = in.readBool();
					obj.Parent = objids.get(in.readi());
					temp = in.readi();
					if (getUnsafe(Resource.SPRITE,temp) != null)
						obj.Mask = getUnsafe(Resource.SPRITE,temp).Id;
					in.skip(4);
					for (int j = 0; j < 11; j++)
						{
						boolean done = false;
						while (!done)
							{
							int first = in.readi();
							if (first != -1)
								{
								Event ev = obj.MainEvents[j].addEvent();
								if (j == MainEvent.EV_COLLISION)
									ev.Other = objids.get(first);
								else
									ev.Id = first;
								ver = in.readi();
								if (ver != 400)
									throw new Gm6FormatException("In Object " + i + ", Main event type " + j
											+ " - GM version unsupported or file corrupt: " + ver);
								int noacts = in.readi();
								for (int k = 0; k < noacts; k++)
									{
									in.skip(4);
									Action act = ev.addAction();
									act.LibraryId = in.readi();
									act.LibActionId = in.readi();
									act.ActionKind = (byte) in.readi();
									act.AllowRelative = in.readBool();
									act.Question = in.readBool();
									act.CanApplyTo = in.readBool();
									act.ExecType = (byte) in.readi();
									act.ExecFunction = in.readStr();
									act.ExecCode = in.readStr();
									act.NoArguments = in.readi();
									int[] argkinds = new int[in.readi()];
									for (int l = 0; l < argkinds.length; l++)
										argkinds[l] = in.readi();
									int id = in.readi();
									switch (id)
										{
										case -1:
											act.AppliesTo = GmObject.OBJECT_SELF;
											break;
										case -2:
											act.AppliesTo = GmObject.OBJECT_OTHER;
											break;
										default:
											act.AppliesTo = objids.get(id);
										}
									act.Relative = in.readBool();
									int actualnoargs = in.readi();
									for (int l = 0; l < actualnoargs; l++)
										{
										if (l < act.NoArguments)
											{
											act.Arguments[l].Kind = (byte) argkinds[l];
											String strval = in.readStr();
											switch (argkinds[l])
												{
												case Argument.ARG_SPRITE:
													act.Arguments[l].Res = getUnsafe(Resource.SPRITE,Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_SOUND:
													act.Arguments[l].Res = getUnsafe(Resource.SOUND,Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_BACKGROUND:
													act.Arguments[l].Res = getUnsafe(Resource.BACKGROUND,Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_PATH:
													act.Arguments[l].Res = getUnsafe(Resource.PATH,Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_SCRIPT:
													act.Arguments[l].Res = getUnsafe(Resource.SCRIPT,Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_GMOBJECT:
													act.Arguments[l].Res = objids.get(Integer.parseInt(strval));
													break;
												case Argument.ARG_ROOM:
													act.Arguments[l].Res = rmids.get(Integer.parseInt(strval));
													break;
												case Argument.ARG_FONT:
													act.Arguments[l].Res = getUnsafe(Resource.FONT,Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_TIMELINE:
													act.Arguments[l].Res = timeids.get(Integer.parseInt(strval));
													break;
												default:
													act.Arguments[l].Val = strval;
													break;
												}
											}
										else
											{
											length = in.readi();
											in.skip(length);
											}
										}
									act.Not = in.readBool();
									}
								}
							else
								done = true;
							}
						}
					}
				else
					lastId[Resource.GMOBJECT]++;
				}
			ver = in.readi();
			if (ver != 420)
				throw new Gm6FormatException("Before Rooms - GM version unsupported or file corrupt: " + ver);
			// ROOMS
			no = in.readi();
			lastId[Resource.ROOM] = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Room rm = (Room)add(Resource.ROOM);
					rm.Id = rmids.get(i);
					rm.name = in.readStr();
					ver = in.readi();
					if (ver != 541)
						throw new Gm6FormatException("In Room " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					rm.Caption = in.readStr();
					rm.Width = in.readi();
					rm.Height = in.readi();
					rm.SnapY = in.readi();
					rm.SnapX = in.readi();
					rm.IsometricGrid = in.readBool();
					rm.Speed = in.readi();
					rm.Persistent = in.readBool();
					rm.BackgroundColor = in.readi();
					rm.DrawBackgroundColor = in.readBool();
					rm.CreationCode = in.readStr();
					int nobackgrounds = in.readi();
					for (int j = 0; j < nobackgrounds; j++)
						{
						BackgroundDef bk = rm.BackgroundDefs[j];
						bk.Visible = in.readBool();
						bk.Foreground = in.readBool();
						Background temp = (Background)getUnsafe(Resource.BACKGROUND,in.readi());
						if (temp != null)
							bk.BackgroundId = temp.Id;
						bk.X = in.readi();
						bk.Y = in.readi();
						bk.TileHoriz = in.readBool();
						bk.TileVert = in.readBool();
						bk.HorizSpeed = in.readi();
						bk.VertSpeed = in.readi();
						bk.Stretch = in.readBool();
						}
					rm.EnableViews = in.readBool();
					int noviews = in.readi();
					for (int j = 0; j < noviews; j++)
						{
						View vw = rm.Views[j];
						vw.Enabled = in.readBool();
						vw.ViewX = in.readi();
						vw.ViewY = in.readi();
						vw.ViewW = in.readi();
						vw.ViewH = in.readi();
						vw.PortX = in.readi();
						vw.PortY = in.readi();
						vw.PortW = in.readi();
						vw.PortH = in.readi();
						vw.Hbor = in.readi();
						vw.VBor = in.readi();
						vw.HSpeed = in.readi();
						vw.VSpeed = in.readi();
						GmObject temp = (GmObject)getUnsafe(Resource.GMOBJECT,in.readi());
						if (temp != null)
							vw.ObjectFollowing = temp.Id;
						}
					int noinstances = in.readi();
					for (int j = 0; j < noinstances; j++)
						{
						Instance inst = rm.addInstance();
						inst.X = in.readi();
						inst.Y = in.readi();
						GmObject temp = (GmObject)getUnsafe(Resource.GMOBJECT,in.readi());
						if (temp != null)
							inst.GmObjectId = temp.Id;
						inst.InstanceId = in.readi();
						inst.CreationCode = in.readStr();
						inst.Locked = in.readBool();
						}
					int notiles = in.readi();
					for (int j = 0; j < notiles; j++)
						{
						Tile ti = rm.addTile();
						ti.X = in.readi();
						ti.Y = in.readi();
						Background temp = (Background)getUnsafe(Resource.BACKGROUND,in.readi());
						if (temp != null)
							ti.BackgroundId = temp.Id;
						ti.TileX = in.readi();
						ti.TileY = in.readi();
						ti.Width = in.readi();
						ti.Height = in.readi();
						ti.Depth = in.readi();
						ti.TileId = in.readi();
						ti.Locked = in.readBool();
						}
					rm.RememberWindowSize = in.readBool();
					rm.EditorWidth = in.readi();
					rm.EditorHeight = in.readi();
					rm.ShowGrid = in.readBool();
					rm.ShowObjects = in.readBool();
					rm.ShowTiles = in.readBool();
					rm.ShowBackgrounds = in.readBool();
					rm.ShowForegrounds = in.readBool();
					rm.ShowViews = in.readBool();
					rm.DeleteUnderlyingObjects = in.readBool();
					rm.DeleteUnderlyingTiles = in.readBool();
					rm.CurrentTab = in.readi();
					rm.ScrollBarX = in.readi();
					rm.ScrollBarY = in.readi();
					}
				else
					lastId[Resource.ROOM]++;
				}
			LastInstanceId = in.readi();
			LastTileId = in.readi();
			ver = in.readi();
			if (ver != 600)
				throw new Gm6FormatException("Before Game Information - GM version unsupported or file corrupt: "
						+ ver);
			GameInfo.BackgroundColor = in.readi();
			GameInfo.MimicGameWindow = in.readBool();
			GameInfo.FormCaption = in.readStr();
			GameInfo.Left = in.readi();
			GameInfo.Top = in.readi();
			GameInfo.Width = in.readi();
			GameInfo.Height = in.readi();
			GameInfo.ShowBorder = in.readBool();
			GameInfo.AllowResize = in.readBool();
			GameInfo.StayOnTop = in.readBool();
			GameInfo.PauseGame = in.readBool();
			GameInfo.GameInfoStr = in.readStr();
			ver = in.readi();
			if (ver != 500)
				throw new Gm6FormatException("After Game Information - GM version unsupported or file corrupt: "
						+ ver);
			no = in.readi();
			for (int j = 0; j < no; j++)
				{
				length = in.readi();
				in.skip(length);
				}
			ver = in.readi();
			if (ver != 540)
				throw new Gm6FormatException(
						"In the second version after Game Information - GM version unsupported or file corrupt: "
								+ ver);
			in.skip(in.readi() * 4);// room indexes in tree order;
			in.readTree(root);
			clock.stop();
			System.out.println("time taken to load file: " + clock.getElapsed() + " ms");
			}
		catch (Exception ex)
			{
			ex.printStackTrace();
			//throw new Gm6FormatException(ex.getMessage());
			}
		finally
			{
			try
				{
				if (in != null)
					in.close();
				}
			catch (IOException ex)
				{
				throw new Gm6FormatException("For some reason, the file closing failsafe has failed");
				}
			}
		}

	public void WriteGm6File(String FileName,ResNode root)
		{
		long savetime = System.currentTimeMillis();
		GmStreamEncoder out = null;
		try
			{
			out = new GmStreamEncoder(FileName);
			out.writei(1234321);
			out.writei(600);
			out.writei(GameId);
			out.fill(4);
			out.writei(600);
			out.writeBool(StartFullscreen);
			out.writeBool(Interpolate);
			out.writeBool(DontDrawBorder);
			out.writeBool(DisplayCursor);
			out.writei(Scaling);
			out.writeBool(AllowWindowResize);
			out.writeBool(AlwaysOnTop);
			out.writei(ColorOutsideRoom);
			out.writeBool(SetResolution);
			out.writei(ColorDepth);
			out.writei(Resolution);
			out.writei(Frequency);
			out.writeBool(DontShowButtons);
			out.writeBool(UseSynchronization);
			out.writeBool(LetF4SwitchFullscreen);
			out.writeBool(LetF1ShowGameInfo);
			out.writeBool(LetEscEndGame);
			out.writeBool(LetF5SaveF6Load);
			out.writei(GamePriority);
			out.writeBool(FreezeOnLoseFocus);
			out.writei(LoadBarMode);
			if (LoadBarMode == LOADBAR_CUSTOM)
				{
				if (BackLoadBar != null)
					{
					out.writei(10);
					out.writeImage(BackLoadBar);
					}
				else
					out.writei(-1);
				if (FrontLoadBar != null)
					{
					out.writei(10);
					out.writeImage(FrontLoadBar);
					}
				else
					out.writei(-1);
				}
			out.writeBool(this.ShowCustomLoadImage);
			if (this.ShowCustomLoadImage)
				{
				if (this.LoadingImage != null)
					{
					out.writei(10);
					out.writeImage(LoadingImage);
					}
				else
					out.writei(-1);
				}
			out.writeBool(this.ImagePartiallyTransparent);
			out.writei(this.LoadImageAlpha);
			out.writeBool(this.ScaleProgressBar);
			out.write(this.GameIconData);
			out.writeBool(this.DisplayErrors);
			out.writeBool(this.WriteToLog);
			out.writeBool(this.AbortOnError);
			out.writeBool(this.TreatUninitializedAs0);
			out.writeStr(this.Author);
			out.writei(this.Version);

			Calendar then = Calendar.getInstance();
			then.set(1899,11,29,23,59,59);
			out.writeD((savetime - then.getTimeInMillis()) / 86400000.0);

			out.writeStr(this.Information);
			out.writei(this.constants.size());
			for (int i = 0; i < this.constants.size(); i++)
				{
				out.writeStr(this.constants.get(i).name);
				out.writeStr(this.constants.get(i).value);
				}
			out.writei(this.includes.size());
			for (int i = 0; i < this.includes.size(); i++)
				out.writeStr(this.includes.get(i).filePath);
			out.writei(this.IncludeFolder);
			out.writeBool(this.OverwriteExisting);
			out.writeBool(this.RemoveAtGameEnd);

			// SOUNDS
			out.writei(400);
			out.writei(lastId[Resource.SOUND] + 1);
			for (int i = 0; i <= lastId[Resource.SOUND]; i++)
				{
				Sound snd = (Sound)getUnsafe(Resource.SOUND,i);
				out.writeBool(snd != null);
				if (snd != null)
					{
					out.writeStr(snd.name);
					out.writei(600);
					out.writei(snd.Type);
					out.writeStr(snd.FileType);
					out.writeStr(snd.FileName);
					if (snd.Data != null)
						{
						out.writeBool(true);
						out.compress(snd.Data);
						}
					else
						out.writeBool(false);
					out.writei(Sound.makeEffects(snd.Chorus,snd.Echo,snd.Flanger,snd.Gargle,snd.Reverb));
					out.writeD(snd.Volume);
					out.writeD(snd.Pan);
					out.writeBool(snd.Preload);
					}
				}

			// SPRITES
			out.writei(400);
			out.writei(lastId[Resource.SPRITE] + 1);
			for (int i = 0; i <= lastId[Resource.SPRITE]; i++)
				{
				Sprite spr = (Sprite)getUnsafe(Resource.SPRITE,i);
				out.writeBool(spr != null);
				if (spr != null)
					{
					out.writeStr(spr.name);
					out.writei(542);
					out.writei(spr.Width);
					out.writei(spr.Height);
					out.writei(spr.BoundingBoxLeft);
					out.writei(spr.BoundingBoxRight);
					out.writei(spr.BoundingBoxBottom);
					out.writei(spr.BoundingBoxTop);
					out.writeBool(spr.Transparent);
					out.writeBool(spr.SmoothEdges);
					out.writeBool(spr.Preload);
					out.writei(spr.BoundingBoxMode);
					out.writeBool(spr.PreciseCC);
					out.writei(spr.OriginX);
					out.writei(spr.OriginY);
					out.writei(spr.NoSubImages());
					for (int j = 0; j < spr.NoSubImages(); j++)
						{
						BufferedImage sub = spr.getSubImage(j);
						out.writei(10);
						out.writeImage(sub);
						}
					}
				}

			// BACKGROUNDS
			out.writei(400);
			out.writei(lastId[Resource.BACKGROUND] + 1);
			for (int i = 0; i <= lastId[Resource.BACKGROUND]; i++)
				{
				Background back = (Background)getUnsafe(Resource.BACKGROUND,i);
				out.writeBool(back != null);
				if (back != null)
					{
					out.writeStr(back.name);
					out.writei(543);
					out.writei(back.Width);
					out.writei(back.Height);
					out.writeBool(back.Transparent);
					out.writeBool(back.SmoothEdges);
					out.writeBool(back.Preload);
					out.writeBool(back.UseAsTileSet);
					out.writei(back.TileWidth);
					out.writei(back.TileHeight);
					out.writei(back.HorizOffset);
					out.writei(back.VertOffset);
					out.writei(back.HorizSep);
					out.writei(back.VertSep);
					if (back.BackgroundImage != null)
						{
						out.writeBool(true);
						out.writei(10);
						out.writeImage(back.BackgroundImage);
						}
					else
						out.writeBool(false);
					}
				}

			// PATHS
			out.writei(420);
			out.writei(lastId[Resource.PATH] + 1);
			for (int i = 0; i <= lastId[Resource.PATH]; i++)
				{
				Path path = (Path)getUnsafe(Resource.PATH,i);
				out.writeBool(path != null);
				if (path != null)
					{
					out.writeStr(path.name);
					out.writei(530);
					out.writeBool(path.Smooth);
					out.writeBool(path.Closed);
					out.writei(path.Precision);
					out.writeId(path.BackgroundRoom,Resource.ROOM,this);
					out.writei(path.SnapX);
					out.writei(path.SnapY);
					out.writei(path.NoPoints());
					for (int j = 0; j < path.NoPoints(); j++)
						{
						out.writeD(path.getPoint(j).X);
						out.writeD(path.getPoint(j).Y);
						out.writeD(path.getPoint(j).Speed);
						}
					}
				}

			// SCRIPTS
			out.writei(400);
			out.writei(lastId[Resource.SCRIPT] + 1);
			for (int i = 0; i <= lastId[Resource.SCRIPT]; i++)
				{
				Script scr = (Script)getUnsafe(Resource.SCRIPT,i);
				out.writeBool(scr != null);
				if (scr != null)
					{
					out.writeStr(scr.name);
					out.writei(400);
					out.writeStr(scr.ScriptStr);
					}
				}

			// FONTS
			out.writei(540);
			out.writei(lastId[Resource.FONT] + 1);
			for (int i = 0; i <= lastId[Resource.FONT]; i++)
				{
				Font font = (Font)getUnsafe(Resource.FONT,i);
				out.writeBool(font != null);
				if (font != null)
					{
					out.writeStr(font.name);
					out.writei(540);
					out.writeStr(font.FontName);
					out.writei(font.Size);
					out.writeBool(font.Bold);
					out.writeBool(font.Italic);
					out.writei(font.CharRangeMin);
					out.writei(font.CharRangeMax);
					}
				}

			// TIMELINES
			out.writei(500);
			out.writei(lastId[Resource.TIMELINE] + 1);
			for (int i = 0; i <= lastId[Resource.TIMELINE]; i++)
				{
				Timeline time = (Timeline)getUnsafe(Resource.TIMELINE,i);
				out.writeBool(time != null);
				if (time != null)
					{
					out.writeStr(time.name);
					out.writei(500);
					out.writei(time.NoMoments());
					for (int j = 0; j < time.NoMoments(); j++)
						{
						Moment mom = time.getMomentList(j);
						out.writei(mom.stepNo);
						out.writei(400);
						out.writei(mom.NoActions());
						for (int k = 0; k < mom.NoActions(); k++)
							{
							Action act = mom.getAction(k);
							out.writei(440);
							out.writei(act.LibraryId);
							out.writei(act.LibActionId);
							out.writei(act.ActionKind);
							out.writeBool(act.AllowRelative);
							out.writeBool(act.Question);
							out.writeBool(act.CanApplyTo);
							out.writei(act.ExecType);
							out.writeStr(act.ExecFunction);
							out.writeStr(act.ExecCode);
							out.writei(act.NoArguments);
							out.writei(8);
							for (int l = 0; l < 8; l++)
								{
								if (l < act.NoArguments)
									out.writei(act.Arguments[l].Kind);
								else
									out.writei(0);
								}
							if (act.AppliesTo != null)
								{
								if (act.AppliesTo.value >= 0)
									out.writeId(act.AppliesTo,Resource.GMOBJECT,-100,this);
								else //self/other are exceptions to the system
									out.writei(act.AppliesTo.value);
								}
							else
								out.writei(-100);
							out.writeBool(act.Relative);
							out.writei(8);
							for (int l = 0; l < 8; l++)
								{
								if (l < act.NoArguments)
									{
									switch (act.Arguments[l].Kind)
										{
										case Argument.ARG_SPRITE:
										case Argument.ARG_SOUND:
										case Argument.ARG_BACKGROUND:
										case Argument.ARG_PATH:
										case Argument.ARG_SCRIPT:
										case Argument.ARG_GMOBJECT:
										case Argument.ARG_ROOM:
										case Argument.ARG_FONT:
										case Argument.ARG_TIMELINE:
											out.writeIdStr(act.Arguments[l].Res,act.Arguments[l].Kind,this);
											break;
										default:
											out.writeStr(act.Arguments[l].Val);
											break;
										}
									}
								else
									out.writeStr("");
								}
							out.writeBool(act.Not);
							}
						}
					}
				}

			// (GM)OBJECTS
			out.writei(400);
			out.writei(lastId[Resource.GMOBJECT] + 1);
			for (int i = 0; i <= lastId[Resource.GMOBJECT]; i++)
				{
				GmObject obj = (GmObject)getUnsafe(Resource.GMOBJECT,i);
				out.writeBool(obj != null);
				if (obj != null)
					{
					out.writeStr(obj.name);
					out.writei(430);
					out.writeId(obj.Sprite,Resource.SPRITE,this);
					out.writeBool(obj.Solid);
					out.writeBool(obj.Visible);
					out.writei(obj.Depth);
					out.writeBool(obj.Persistent);
					out.writeId(obj.Parent,Resource.GMOBJECT,-100,this);
					out.writeId(obj.Mask,Resource.SPRITE,this);
					out.writei(10);
					for (int j = 0; j < 11; j++)
						{
						for (int k = 0; k < obj.MainEvents[j].NoEvents(); k++)
							{
							Event ev = obj.MainEvents[j].getEventList(k);
							if (j == MainEvent.EV_COLLISION)
								out.writeId(ev.Other,Resource.GMOBJECT,this);
							else
								out.writei(ev.Id);
							out.writei(400);
							out.writei(ev.NoActions());
							for (int l = 0; l < ev.NoActions(); l++)
								{
								Action act = ev.getAction(l);
								out.writei(440);
								out.writei(act.LibraryId);
								out.writei(act.LibActionId);
								out.writei(act.ActionKind);
								out.writeBool(act.AllowRelative);
								out.writeBool(act.Question);
								out.writeBool(act.CanApplyTo);
								out.writei(act.ExecType);
								out.writeStr(act.ExecFunction);
								out.writeStr(act.ExecCode);
								out.writei(act.NoArguments);
								out.writei(8);
								for (int m = 0; m < 8; m++)
									{
									if (m < act.NoArguments)
										out.writei(act.Arguments[m].Kind);
									else
										out.writei(0);
									}
								if (act.AppliesTo != null)
									{
									if (act.AppliesTo.value >= 0)
										out.writeId(act.AppliesTo,Resource.GMOBJECT,-100,this);
									else //self/other are exceptions to the system
										out.writei(act.AppliesTo.value);
									}
								else
									out.writei(-100);
								out.writeBool(act.Relative);
								out.writei(8);
								for (int m = 0; m < 8; m++)
									{
									if (m < act.NoArguments)
										{
										switch (act.Arguments[m].Kind)
											{
											case Argument.ARG_SPRITE:
											case Argument.ARG_SOUND:
											case Argument.ARG_BACKGROUND:
											case Argument.ARG_PATH:
											case Argument.ARG_SCRIPT:
											case Argument.ARG_GMOBJECT:
											case Argument.ARG_ROOM:
											case Argument.ARG_FONT:
											case Argument.ARG_TIMELINE:
												out.writeIdStr(act.Arguments[m].Res,act.Arguments[m].Kind,this);
												break;
											default:
												out.writeStr(act.Arguments[m].Val);
												break;
											}
										}
									else
										out.writeStr("");
									}
								out.writeBool(act.Not);
								}
							}
						out.writei(-1);
						}
					}
				}

			// ROOMS
			out.writei(420);
			out.writei(lastId[Resource.ROOM] + 1);
			for (int i = 0; i <= lastId[Resource.ROOM]; i++)
				{
				Room rm = (Room)getUnsafe(Resource.ROOM,i);
				out.writeBool(rm != null);
				if (rm != null)
					{
					out.writeStr(rm.name);
					out.writei(541);
					out.writeStr(rm.Caption);
					out.writei(rm.Width);
					out.writei(rm.Height);
					out.writei(rm.SnapY);
					out.writei(rm.SnapX);
					out.writeBool(rm.IsometricGrid);
					out.writei(rm.Speed);
					out.writeBool(rm.Persistent);
					out.writei(rm.BackgroundColor);
					out.writeBool(rm.DrawBackgroundColor);
					out.writeStr(rm.CreationCode);
					out.writei(8);
					for (int j = 0; j < 8; j++)
						{
						BackgroundDef back = rm.BackgroundDefs[j];
						out.writeBool(back.Visible);
						out.writeBool(back.Foreground);
						out.writeId(back.BackgroundId,Resource.BACKGROUND,this);
						out.writei(back.X);
						out.writei(back.Y);
						out.writeBool(back.TileHoriz);
						out.writeBool(back.TileVert);
						out.writei(back.HorizSpeed);
						out.writei(back.VertSpeed);
						out.writeBool(back.Stretch);
						}
					out.writeBool(rm.EnableViews);
					out.writei(8);
					for (int j = 0; j < 8; j++)
						{
						View view = rm.Views[j];
						out.writeBool(view.Enabled);
						out.writei(view.ViewX);
						out.writei(view.ViewY);
						out.writei(view.ViewW);
						out.writei(view.ViewH);
						out.writei(view.PortX);
						out.writei(view.PortY);
						out.writei(view.PortW);
						out.writei(view.PortH);
						out.writei(view.Hbor);
						out.writei(view.VBor);
						out.writei(view.HSpeed);
						out.writei(view.VSpeed);
						out.writeId(view.ObjectFollowing,Resource.GMOBJECT,this);
						}
					out.writei(rm.NoInstances());
					for (int j = 0; j < rm.NoInstances(); j++)
						{
						Instance in = rm.getInstanceList(j);
						out.writei(in.X);
						out.writei(in.Y);
						out.writeId(in.GmObjectId,Resource.GMOBJECT,this);
						out.writei(in.InstanceId);
						out.writeStr(in.CreationCode);
						out.writeBool(in.Locked);
						}
					out.writei(rm.NoTiles());
					for (int j = 0; j < rm.NoTiles(); j++)
						{
						Tile tile = rm.getTileList(j);
						out.writei(tile.X);
						out.writei(tile.Y);
						out.writeId(tile.BackgroundId,Resource.BACKGROUND,this);
						out.writei(tile.TileX);
						out.writei(tile.TileY);
						out.writei(tile.Width);
						out.writei(tile.Height);
						out.writei(tile.Depth);
						out.writei(tile.TileId);
						out.writeBool(tile.Locked);
						}
					out.writeBool(rm.RememberWindowSize);
					out.writei(rm.EditorWidth);
					out.writei(rm.EditorHeight);
					out.writeBool(rm.ShowGrid);
					out.writeBool(rm.ShowObjects);
					out.writeBool(rm.ShowTiles);
					out.writeBool(rm.ShowBackgrounds);
					out.writeBool(rm.ShowForegrounds);
					out.writeBool(rm.ShowViews);
					out.writeBool(rm.DeleteUnderlyingObjects);
					out.writeBool(rm.DeleteUnderlyingTiles);
					out.writei(rm.CurrentTab);
					out.writei(rm.ScrollBarX);
					out.writei(rm.ScrollBarY);
					}
				}
			out.writei(this.LastInstanceId);
			out.writei(this.LastTileId);

			// GAME SETTINGS
			out.writei(600);
			out.writei(GameInfo.BackgroundColor);
			out.writeBool(GameInfo.MimicGameWindow);
			out.writeStr(GameInfo.FormCaption);
			out.writei(GameInfo.Left);
			out.writei(GameInfo.Top);
			out.writei(GameInfo.Width);
			out.writei(GameInfo.Height);
			out.writeBool(GameInfo.ShowBorder);
			out.writeBool(GameInfo.AllowResize);
			out.writeBool(GameInfo.StayOnTop);
			out.writeBool(GameInfo.PauseGame);
			out.writeStr(GameInfo.GameInfoStr);
			out.writei(500);

			out.writei(0);// "how many longints will follow it"

			out.writei(540);
			out.writei(0);// room indexes in tree order

			out.writeTree(root);
			out.close();
			}
		catch (FileNotFoundException ex)
			{
			ex.printStackTrace();
			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
		catch (NullPointerException ex)
			{
			try
				{
				out.close();
				System.err.println(ex.getMessage());
				ex.printStackTrace();
				}
			catch (IOException ex2)
				{
				System.err.println(ex.getMessage());
				ex.printStackTrace();
				System.err.println(ex2.getMessage());
				ex2.printStackTrace();
				}
			}
		}
	}