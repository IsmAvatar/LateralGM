package fileRes;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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

//TODO Re-implement the icon loading code using com.ctreber.aclib.ico
public class Gm6File
	{
	private class idStack // allows pointing to the ResId of a resource even when the resource that "owns" it is
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

	private Map<Integer,ResourceList> resMap = new HashMap<Integer,ResourceList>();
	public ResourceList<Sprite> Sprites = new ResourceList<Sprite>(Sprite.class);
	public ResourceList<Sound> Sounds = new ResourceList<Sound>(Sound.class);
	public ResourceList<Background> Backgrounds = new ResourceList<Background>(Background.class);
	public ResourceList<Path> Paths = new ResourceList<Path>(Path.class);
	public ResourceList<Script> Scripts = new ResourceList<Script>(Script.class);
	public ResourceList<Font> Fonts = new ResourceList<Font>(Font.class);
	public ResourceList<Timeline> Timelines = new ResourceList<Timeline>(Timeline.class);
	public ResourceList<GmObject> GmObjects = new ResourceList<GmObject>(GmObject.class);
	public ResourceList<Room> Rooms = new ResourceList<Room>(Room.class);
	public ArrayList<Constant> constants = new ArrayList<Constant>();
	public ArrayList<Include> includes = new ArrayList<Include>();

	public Gm6File()
		{
		resMap.put(new Integer(Resource.SPRITE),Sprites);
		resMap.put(new Integer(Resource.SOUND),Sounds);
		resMap.put(new Integer(Resource.BACKGROUND),Backgrounds);
		resMap.put(new Integer(Resource.PATH),Paths);
		resMap.put(new Integer(Resource.SCRIPT),Scripts);
		resMap.put(new Integer(Resource.FONT),Fonts);
		resMap.put(new Integer(Resource.TIMELINE),Timelines);
		resMap.put(new Integer(Resource.GMOBJECT),GmObjects);
		resMap.put(new Integer(Resource.ROOM),Rooms);
		GameId = new Random().nextInt(100000001);
		GameIconData = new byte[0];
		try
			{
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(Gm6File.class
					.getResource("default.ico").toURI()))); //$NON-NLS-1$
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
			System.err.println(Messages.getString("Gm6File.NOICON")); //$NON-NLS-1$
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			}
		}

	public static Calendar gmBaseTime()
		{
		Calendar res = Calendar.getInstance();
		res.set(1899,11,29,23,59,59);
		return res;
		}

	public static double longTimeToGmTime(long time)
		{
		return (time - gmBaseTime().getTimeInMillis()) / 86400000d;
		}

	public static String gmTimeToString(double time)
		{
		Calendar base = gmBaseTime();
		base.setTimeInMillis(base.getTimeInMillis() + ((long) (time * 86400000)));
		return DateFormat.getDateTimeInstance().format(base.getTime());
		}

	// Constants For Resource Properties
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

	// File Properties
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
	public String Author = ""; //$NON-NLS-1$
	public int Version = 100;
	public double LastChanged = longTimeToGmTime(System.currentTimeMillis());
	public String Information = ""; //$NON-NLS-1$
	public int IncludeFolder = INCLUDE_MAIN;
	public boolean OverwriteExisting = false;
	public boolean RemoveAtGameEnd = false;
	public int LastInstanceId = 100000;
	public int LastTileId = 10000000;
	public byte[] GameIconData;// actual data is stored to be written on resave (no reason to re-encode)
	public BufferedImage GameIcon;// icon as image for display purposes

	// Returns the ResourceList corresponding to given Resource constant
	public ResourceList getList(int res)
		{
		return resMap.get(res);
		}

	public void clearAll()
		{
		for (ResourceList l : resMap.values())
			l.clear();
		constants.clear();
		includes.clear();
		}

	public GameInformation GameInfo = new GameInformation();

	// TODO externalise the file IO?
	public void ReadGm6File(String FileName, ResNode root) throws Gm6FormatException
		{
		clearAll();
		GmStreamDecoder in = null;
		try
			{
			long startTime = System.currentTimeMillis();
			in = new GmStreamDecoder(FileName);
			idStack timeids = new idStack(); // timeline ids
			idStack objids = new idStack(); // object ids
			idStack rmids = new idStack(); // room ids
			int identifier = in.readi();
			if (identifier != 1234321)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_INVALID"),FileName,identifier)); //$NON-NLS-1$
			int ver = in.readi();
			if (ver != 600)
				throw new Gm6FormatException(String.format(Messages.getString("Gm6File.ERROR_UNSUPPORTED"),ver)); //$NON-NLS-1$
			GameId = in.readi();
			in.skip(16);// unknown bytes following game id
			ver = in.readi();
			if (ver != 600)
				throw new Gm6FormatException(String.format(Messages.getString("Gm6File.ERROR_UNSUPPORTED"),ver)); //$NON-NLS-1$
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
				if (in.readi() != -1) BackLoadBar = in.readImage();
				if (in.readi() != -1) FrontLoadBar = in.readImage();
				}
			ShowCustomLoadImage = in.readBool();
			if (ShowCustomLoadImage) if (in.readi() != -1) LoadingImage = in.readImage();
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
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESOUNDS"),ver)); //$NON-NLS-1$
			// SOUNDS
			no = in.readi();
			Sprites.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Sound snd = Sounds.add();
					snd.name = in.readStr();
					ver = in.readi();
					if (ver != 600)
						throw new Gm6FormatException(String.format(Messages
								.getString("Gm6File.ERROR_UNSUPPORTED_INSOUND"),i,ver)); //$NON-NLS-1$
					snd.kind = (byte) in.readi();
					snd.FileType = in.readStr();
					snd.FileName = in.readStr();
					if (in.readBool()) snd.Data = in.decompress(in.readi());
					int effects = in.readi();
					snd.setEffects(effects);
					snd.volume = in.readD();
					snd.pan = in.readD();
					snd.preload = in.readBool();
					}
				else
					Sounds.LastId++;
				}

			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESPRITES"),ver)); //$NON-NLS-1$
			// SPRITES
			no = in.readi();
			Sprites.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Sprite spr = Sprites.add();
					spr.name = in.readStr();
					ver = in.readi();
					if (ver != 542)
						throw new Gm6FormatException(String.format(Messages
								.getString("Gm6File.ERROR_UNSUPPORTED_INSPRITE"),i,ver)); //$NON-NLS-1$
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
					Sprites.LastId++;
				}

			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREBACKGROUNDS"),ver)); //$NON-NLS-1$
			// BACKGROUNDS
			no = in.readi();
			Backgrounds.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Background back = Backgrounds.add();
					back.name = in.readStr();
					ver = in.readi();
					if (ver != 543)
						throw new Gm6FormatException(String.format(Messages
								.getString("Gm6File.ERROR_UNSUPPORTED_INBACKGROUND"),i,ver)); //$NON-NLS-1$
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
					Backgrounds.LastId++;
				}
			ver = in.readi();
			if (ver != 420)
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREPATHS"),ver)); //$NON-NLS-1$
			// PATHS
			no = in.readi();
			Paths.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Path path = Paths.add();
					path.name = in.readStr();
					ver = in.readi();
					if (ver != 530)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INPATH"),i,ver)); //$NON-NLS-1$
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
					Paths.LastId++;
				}
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESCRIPTS"),ver)); //$NON-NLS-1$
			// SCRIPTS
			no = in.readi();
			Scripts.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Script scr = Scripts.add();
					scr.name = in.readStr();
					ver = in.readi();
					if (ver != 400)
						throw new Gm6FormatException(String.format(Messages
								.getString("Gm6File.ERROR_UNSUPPORTED_INSCRIPT"),i,ver)); //$NON-NLS-1$
					scr.ScriptStr = in.readStr();
					}
				else
					Scripts.LastId++;
				}

			ver = in.readi();
			if (ver != 540)
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREFONTS"),ver)); //$NON-NLS-1$
			// FONTS
			no = in.readi();
			Fonts.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Font font = Fonts.add();
					font.name = in.readStr();
					ver = in.readi();
					if (ver != 540)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INFONT"),i,ver)); //$NON-NLS-1$
					font.FontName = in.readStr();
					font.Size = in.readi();
					font.Bold = in.readBool();
					font.Italic = in.readBool();
					font.CharRangeMin = in.readi();
					font.CharRangeMax = in.readi();
					}
				else
					Fonts.LastId++;
				}

			ver = in.readi();
			if (ver != 500)
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFORETIMELINES"),ver)); //$NON-NLS-1$
			// TIMELINES
			Resource tag = new Script(); // workaround for the case statement below (declared here to prevent
			// repeated instantiation)
			no = in.readi();
			Timelines.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Timeline time = Timelines.add();
					time.Id = timeids.get(i);
					time.name = in.readStr();
					ver = in.readi();
					if (ver != 500)
						throw new Gm6FormatException(String.format(Messages
								.getString("Gm6File.ERROR_UNSUPPORTED_INTIMELINE"),i,ver)); //$NON-NLS-1$
					int nomoms = in.readi();
					for (int j = 0; j < nomoms; j++)
						{
						Moment mom = time.addMoment();
						mom.stepNo = in.readi();
						ver = in.readi();
						if (ver != 400)
							throw new Gm6FormatException(String.format(Messages
									.getString("Gm6File.ERROR_UNSUPPORTED_INTIMELINEMOMENT"),i,j,ver)); //$NON-NLS-1$
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
									Resource res = tag;
									switch (argkinds[l])
										{
										case Argument.ARG_SPRITE:
											res = Sprites.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_SOUND:
											res = Sounds.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_BACKGROUND:
											res = Backgrounds.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_PATH:
											res = Paths.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_SCRIPT:
											res = Scripts.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_GMOBJECT:
											act.Arguments[l].Res = objids.get(Integer.parseInt(strval));
											break;
										case Argument.ARG_ROOM:
											act.Arguments[l].Res = rmids.get(Integer.parseInt(strval));
											break;
										case Argument.ARG_FONT:
											res = Fonts.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_TIMELINE:
											act.Arguments[l].Res = timeids.get(Integer.parseInt(strval));
											break;
										default:
											act.Arguments[l].Val = strval;
											break;
										}
									if (res != null && res != tag)
										{
										act.Arguments[l].Res = res.Id;
										}
									}
								else
									{
									in.skip(in.readi());
									}
								}
							act.Not = in.readBool();
							}
						}
					}
				else
					Timelines.LastId++;
				}
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREOBJECTS"),ver)); //$NON-NLS-1$
			// OBJECTS
			no = in.readi();
			GmObjects.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					GmObject obj = GmObjects.add();
					obj.Id = objids.get(i);
					obj.name = in.readStr();
					ver = in.readi();
					if (ver != 430)
						throw new Gm6FormatException(String.format(Messages
								.getString("Gm6File.ERROR_UNSUPPORTED_INOBJECT"),i,ver)); //$NON-NLS-1$
					Sprite temp = Sprites.getUnsafe(in.readi());
					if (temp != null) obj.Sprite = temp.Id;
					obj.Solid = in.readBool();
					obj.Visible = in.readBool();
					obj.Depth = in.readi();
					obj.Persistent = in.readBool();
					obj.Parent = objids.get(in.readi());
					temp = Sprites.getUnsafe(in.readi());
					if (temp != null) obj.Mask = temp.Id;
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
									throw new Gm6FormatException(String.format(Messages
											.getString("Gm6File.ERROR_UNSUPPORTED_INOBJECTEVENT"),i,j,ver)); //$NON-NLS-1$
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
											Resource res = tag; // see before Timeline
											switch (argkinds[l])
												{
												case Argument.ARG_SPRITE:
													res = Sprites.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_SOUND:
													res = Sounds.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_BACKGROUND:
													res = Backgrounds.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_PATH:
													res = Paths.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_SCRIPT:
													res = Scripts.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_GMOBJECT:
													act.Arguments[l].Res = objids.get(Integer.parseInt(strval));
													break;
												case Argument.ARG_ROOM:
													act.Arguments[l].Res = rmids.get(Integer.parseInt(strval));
													break;
												case Argument.ARG_FONT:
													res = Fonts.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_TIMELINE:
													act.Arguments[l].Res = timeids.get(Integer.parseInt(strval));
													break;
												default:
													act.Arguments[l].Val = strval;
													break;
												}
											if (res != null && res != tag)
												{
												act.Arguments[l].Res = res.Id;
												}
											}
										else
											in.skip(in.readi());
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
					GmObjects.LastId++;
				}
			ver = in.readi();
			if (ver != 420)
				throw new Gm6FormatException(String.format(Messages
						.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREROOMS"),ver)); //$NON-NLS-1$
			// ROOMS
			no = in.readi();
			Rooms.LastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Room rm = Rooms.add(new Room(this));
					rm.Id = rmids.get(i);
					rm.name = in.readStr();
					ver = in.readi();
					if (ver != 541)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INROOM"),i,ver)); //$NON-NLS-1$
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
						Background temp = Backgrounds.getUnsafe(in.readi());
						if (temp != null) bk.BackgroundId = temp.Id;
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
						GmObject temp = GmObjects.getUnsafe(in.readi());
						if (temp != null) vw.ObjectFollowing = temp.Id;
						}
					int noinstances = in.readi();
					for (int j = 0; j < noinstances; j++)
						{
						Instance inst = rm.addInstance();
						inst.X = in.readi();
						inst.Y = in.readi();
						GmObject temp = GmObjects.getUnsafe(in.readi());
						if (temp != null) inst.GmObjectId = temp.Id;
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
						Background temp = Backgrounds.getUnsafe(in.readi());
						if (temp != null) ti.BackgroundId = temp.Id;
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
					Rooms.LastId++;
				}
			LastInstanceId = in.readi();
			LastTileId = in.readi();
			ver = in.readi();
			if (ver != 600)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREINFO"),ver)); //$NON-NLS-1$
			int bc = in.readi();
			if (bc >= 0) GameInfo.BackgroundColor = new Color(bc);
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
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_AFTERINFO"),ver)); //$NON-NLS-1$
			no = in.readi();
			for (int j = 0; j < no; j++)
				{
				length = in.readi();
				in.skip(length);
				}
			ver = in.readi();
			if (ver != 540)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_AFTERINFO2"),ver)); //$NON-NLS-1$
			in.skip(in.readi() * 4);// room indexes in tree order;
			in.readTree(root,this);
			System.out.printf(Messages.getString("Gm6File.LOADTIME"),System.currentTimeMillis() - startTime); //$NON-NLS-1$
			System.out.println();
			}
		catch (Exception ex)
			{
			ex.printStackTrace();
			// throw new Gm6FormatException(ex.getMessage());
			}
		finally
			{
			try
				{
				if (in != null) in.close();
				}
			catch (IOException ex)
				{
				throw new Gm6FormatException(Messages.getString("Gm6File.ERROR_CLOSEFAILED")); //$NON-NLS-1$
				}
			}
		}

	public void WriteGm6File(String FileName, ResNode root)
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
			out.writeBool(ShowCustomLoadImage);
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
			out.writeBool(ImagePartiallyTransparent);
			out.writei(LoadImageAlpha);
			out.writeBool(ScaleProgressBar);
			out.write(GameIconData);
			out.writeBool(DisplayErrors);
			out.writeBool(WriteToLog);
			out.writeBool(AbortOnError);
			out.writeBool(TreatUninitializedAs0);
			out.writeStr(Author);
			out.writei(Version);
			LastChanged = longTimeToGmTime(savetime);
			out.writeD(LastChanged);

			out.writeStr(Information);
			out.writei(constants.size());
			for (int i = 0; i < constants.size(); i++)
				{
				out.writeStr(constants.get(i).name);
				out.writeStr(constants.get(i).value);
				}
			out.writei(includes.size());
			for (int i = 0; i < includes.size(); i++)
				out.writeStr(includes.get(i).filePath);
			out.writei(IncludeFolder);
			out.writeBool(OverwriteExisting);
			out.writeBool(RemoveAtGameEnd);

			// SOUNDS
			out.writei(400);
			out.writei(Sounds.LastId + 1);
			for (int i = 0; i <= Sounds.LastId; i++)
				{
				Sound snd = Sounds.getUnsafe(i);
				out.writeBool(snd != null);
				if (snd != null)
					{
					out.writeStr(snd.name);
					out.writei(600);
					out.writei(snd.kind);
					out.writeStr(snd.FileType);
					out.writeStr(snd.FileName);
					if (snd.Data != null)
						{
						out.writeBool(true);
						out.compress(snd.Data);
						}
					else
						out.writeBool(false);
					out.writei(snd.getEffects());
					out.writeD(snd.volume);
					out.writeD(snd.pan);
					out.writeBool(snd.preload);
					}
				}

			// SPRITES
			out.writei(400);
			out.writei(Sprites.LastId + 1);
			for (int i = 0; i <= Sprites.LastId; i++)
				{
				Sprite spr = Sprites.getUnsafe(i);
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
			out.writei(Backgrounds.LastId + 1);
			for (int i = 0; i <= Backgrounds.LastId; i++)
				{
				Background back = Backgrounds.getUnsafe(i);
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
			out.writei(Paths.LastId + 1);
			for (int i = 0; i <= Paths.LastId; i++)
				{
				Path path = Paths.getUnsafe(i);
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
			out.writei(Scripts.LastId + 1);
			for (int i = 0; i <= Scripts.LastId; i++)
				{
				Script scr = Scripts.getUnsafe(i);
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
			out.writei(Fonts.LastId + 1);
			for (int i = 0; i <= Fonts.LastId; i++)
				{
				Font font = Fonts.getUnsafe(i);
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
			out.writei(Timelines.LastId + 1);
			for (int i = 0; i <= Timelines.LastId; i++)
				{
				Timeline time = Timelines.getUnsafe(i);
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
								else
									// self/other are exceptions to the system
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
									out.writeStr(""); //$NON-NLS-1$
								}
							out.writeBool(act.Not);
							}
						}
					}
				}

			// (GM)OBJECTS
			out.writei(400);
			out.writei(GmObjects.LastId + 1);
			for (int i = 0; i <= GmObjects.LastId; i++)
				{
				GmObject obj = GmObjects.getUnsafe(i);
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
									else
										// self/other are exceptions to the system
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
										out.writeStr(""); //$NON-NLS-1$
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
			out.writei(Rooms.LastId + 1);
			for (int i = 0; i <= Rooms.LastId; i++)
				{
				Room rm = Rooms.getUnsafe(i);
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
			out.writei(LastInstanceId);
			out.writei(LastTileId);

			// GAME SETTINGS
			out.writei(600);
			out.writei(GameInfo.BackgroundColor.getRGB());
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

	public void DefragIds()
		{
		Iterator<ResourceList> iter = resMap.values().iterator();
		while (iter.hasNext())
			iter.next().defragIds();
		LastInstanceId = 100000;
		LastTileId = 100000;
		for (int i = 0; i < Rooms.count(); i++)
			{
			for (int j = 0; j < Rooms.getList(i).NoInstances(); j++)
				Rooms.getList(i).getInstanceList(j).InstanceId = ++LastInstanceId;
			for (int j = 0; j < Rooms.getList(i).NoTiles(); j++)
				Rooms.getList(i).getTileList(j).TileId = ++LastTileId;
			}
		}
	}