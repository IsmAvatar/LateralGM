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
import java.util.Random;

import javax.imageio.ImageIO;

import resourcesRes.Background;
import resourcesRes.Constant;
import resourcesRes.Font;
import resourcesRes.GameInformation;
import resourcesRes.GmObject;
import resourcesRes.IncludeFile;
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
	private ArrayList<Sprite> Sprites = new ArrayList<Sprite>();

	public int NoSprites()
		{
		return Sprites.size();
		}

	public int LastSpriteId = -1;

	public Sprite addSprite()
		{
		Sprite spr = new Sprite();
		spr.Id.value = LastSpriteId++;
		Sprites.add(spr);
		return spr;
		}

	public Sprite getSpriteUnsafe(int id)
		{
		for (int i = 0; i < NoSprites(); i++)
			{
			if (getSpriteList(i).Id.value == id)
				{
				return getSpriteList(i);
				}
			}
		return null;
		}

	public Sprite getSprite(ResId id)
		{
		int ListIndex = SpriteIndex(id);
		if (ListIndex != -1) return Sprites.get(ListIndex);
		return null;
		}

	public Sprite getSprite(String Name)
		{
		int ListIndex = SpriteIndex(Name);
		if (ListIndex != -1) return Sprites.get(ListIndex);
		return null;
		}

	public Sprite getSpriteList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoSprites()) return Sprites.get(ListIndex);
		return null;
		}

	public void removeSprite(ResId id)
		{
		int ListIndex = SpriteIndex(id);
		if (ListIndex != -1) Sprites.remove(ListIndex);
		}

	public void removeSprite(String Name)
		{
		int ListIndex = SpriteIndex(Name);
		if (ListIndex != -1) Sprites.remove(ListIndex);
		}

	public int SpriteIndex(ResId id)
		{
		for (int i = 0; i < NoSprites(); i++)
			{
			if (getSpriteList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int SpriteIndex(String Name)
		{
		for (int i = 0; i < NoSprites(); i++)
			{
			if (getSpriteList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearSprites()
		{
		Sprites.clear();
		}

	public void sortSprites()
		{
		Collections.sort(Sprites);
		}

	public Sprite duplicateSprite(ResId id,boolean update)
		{
		Sprite spr2 = null;
		Sprite spr = getSprite(id);
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
				LastSpriteId++;
				spr2.Id.value = LastSpriteId;
				spr2.Name = prefixes[Resource.SPRITE] + LastSpriteId;
				Sprites.add(spr2);
				}
			else
				{
				spr2.Id = spr.Id;
				spr2.Name = spr.Name;
				}
			}
		return spr2;
		}

	public void replaceSprite(ResId srcId,Sprite replacement)
		{
		int ind = SpriteIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			Sprites.set(ind,replacement);
			}
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Sound Functions ">
	private ArrayList<Sound> Sounds = new ArrayList<Sound>();

	public int NoSounds()
		{
		return Sounds.size();
		}

	public int LastSoundId = -1;

	public Sound addSound()
		{
		Sound snd = new Sound();
		snd.Id.value = LastSoundId++;
		Sounds.add(snd);
		return snd;
		}

	public Sound getSoundUnsafe(int id)
		{
		for (int i = 0; i < NoSounds(); i++)
			{
			if (getSoundList(i).Id.value == id)
				{
				return getSoundList(i);
				}
			}
		return null;
		}

	public Sound getSound(ResId id)
		{
		int ListIndex = SoundIndex(id);
		if (ListIndex != -1) return Sounds.get(ListIndex);
		return null;
		}

	public Sound getSound(String Name)
		{
		int ListIndex = SoundIndex(Name);
		if (ListIndex != -1) return Sounds.get(ListIndex);
		return null;
		}

	public Sound getSoundList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoSounds()) return Sounds.get(ListIndex);
		return null;
		}

	public void removeSound(ResId id)
		{
		int ListIndex = SoundIndex(id);
		if (ListIndex != -1) Sounds.remove(ListIndex);
		}

	public void removeSound(String Name)
		{
		int ListIndex = SoundIndex(Name);
		if (ListIndex != -1) Sounds.remove(ListIndex);
		}

	public int SoundIndex(ResId id)
		{
		for (int i = 0; i < NoSounds(); i++)
			{
			if (getSoundList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int SoundIndex(String Name)
		{
		for (int i = 0; i < NoSounds(); i++)
			{
			if (getSoundList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearSounds()
		{
		Sounds.clear();
		}

	public void sortSounds()
		{
		Collections.sort(Sounds);
		}

	public void replaceSound(ResId srcId,Sound replacement)
		{
		int ind = SoundIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			Sounds.set(ind,replacement);
			}
		}

	public Sound duplicateSound(ResId id,boolean update)
		{
		Sound snd2 = null;
		Sound snd = getSound(id);
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
				LastSoundId++;
				snd2.Id.value = LastSoundId;
				snd2.Name = prefixes[Resource.SOUND] + LastSoundId;
				Sounds.add(snd2);
				}
			else
				{
				snd2.Id = snd.Id;
				snd2.Name = snd.Name;
				}
			}
		return snd2;
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Background Functions ">
	private ArrayList<Background> Backgrounds = new ArrayList<Background>();

	public int NoBackgrounds()
		{
		return Backgrounds.size();
		}

	public int LastBackgroundId = -1;

	public Background addBackground()
		{
		Background back = new Background();
		back.Id.value = LastBackgroundId++;
		Backgrounds.add(back);
		return back;
		}

	public Background getBackgroundUnsafe(int id)
		{
		for (int i = 0; i < NoBackgrounds(); i++)
			{
			if (Backgrounds.get(i).Id.value == id) return Backgrounds.get(i);
			}
		return null;
		}

	public Background getBackground(ResId id)
		{
		int ListIndex = BackgroundIndex(id);
		if (ListIndex != -1) return Backgrounds.get(ListIndex);
		return null;
		}

	public Background getBackground(String Name)
		{
		int ListIndex = BackgroundIndex(Name);
		if (ListIndex != -1) return Backgrounds.get(ListIndex);
		return null;
		}

	public Background getBackgroundList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoBackgrounds()) return Backgrounds.get(ListIndex);
		return null;
		}

	public void removeBackground(ResId id)
		{
		int ListIndex = BackgroundIndex(id);
		if (ListIndex != -1) Backgrounds.remove(ListIndex);
		}

	public void removeBackground(String Name)
		{
		int ListIndex = BackgroundIndex(Name);
		if (ListIndex != -1) Backgrounds.remove(ListIndex);
		}

	public int BackgroundIndex(ResId id)
		{
		for (int i = 0; i < NoBackgrounds(); i++)
			{
			if (getBackgroundList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int BackgroundIndex(String Name)
		{
		for (int i = 0; i < NoBackgrounds(); i++)
			{
			if (getBackgroundList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearBackgrounds()
		{
		Backgrounds.clear();
		}

	public void sortBackgrounds()
		{
		Collections.sort(Backgrounds);
		}

	public void replaceBackground(ResId srcId,Background replacement)
		{
		int ind = BackgroundIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			Backgrounds.set(ind,replacement);
			}
		}

	public Background duplicateBackground(ResId id,boolean update)
		{
		Background back2 = null;
		Background back = getBackground(id);
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
				LastBackgroundId++;
				back2.Id.value = LastBackgroundId;
				back2.Name = prefixes[Resource.BACKGROUND] + LastBackgroundId;
				Backgrounds.add(back2);
				}
			else
				{
				back2.Id = back.Id;
				back2.Name = back.Name;
				}
			}
		return back2;
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Path Functions ">
	private ArrayList<Path> Paths = new ArrayList<Path>();

	public int NoPaths()
		{
		return Paths.size();
		}

	public int LastPathId = -1;

	public Path addPath()
		{
		Path path = new Path();
		path.Id.value = LastPathId++;
		Paths.add(path);
		return path;
		}

	public Path getPathUnsafe(int id)
		{
		for (int i = 0; i < NoPaths(); i++)
			{
			if (Paths.get(i).Id.value == id) return Paths.get(i);
			}
		return null;
		}

	public Path getPath(ResId id)
		{
		int ListIndex = PathIndex(id);
		if (ListIndex != -1) return Paths.get(ListIndex);
		return null;
		}

	public Path getPath(String Name)
		{
		int ListIndex = PathIndex(Name);
		if (ListIndex != -1) return Paths.get(ListIndex);
		return null;
		}

	public Path getPathList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoPaths()) return Paths.get(ListIndex);
		return null;
		}

	public void removePath(ResId id)
		{
		int ListIndex = PathIndex(id);
		if (ListIndex != -1) Paths.remove(ListIndex);
		}

	public void removePath(String Name)
		{
		int ListIndex = PathIndex(Name);
		if (ListIndex != -1) Paths.remove(ListIndex);
		}

	public int PathIndex(ResId id)
		{
		for (int i = 0; i < NoPaths(); i++)
			{
			if (getPathList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int PathIndex(String Name)
		{
		for (int i = 0; i < NoPaths(); i++)
			{
			if (getPathList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearPaths()
		{
		Paths.clear();
		}

	public void sortPaths()
		{
		Collections.sort(Paths);
		}

	public void replacePath(ResId srcId,Path replacement)
		{
		int ind = PathIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			Paths.set(ind,replacement);
			}
		}

	public Path duplicatePath(ResId id,boolean update)
		{
		Path path2 = null;
		Path path = getPath(id);
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
				LastPathId++;
				path2.Id.value = LastPathId;
				path2.Name = prefixes[Resource.PATH] + LastPathId;
				Paths.add(path2);
				}
			else
				{
				path2.Id = path.Id;
				path2.Name = path.Name;
				}
			}
		return path2;
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Script Functions ">
	private ArrayList<Script> Scripts = new ArrayList<Script>();

	public int NoScripts()
		{
		return Scripts.size();
		}

	public int LastScriptId = -1;

	public Script addScript()
		{
		Script scr = new Script();
		scr.Id.value = LastScriptId++;
		Scripts.add(scr);
		return scr;
		}

	public Script getScriptUnsafe(int id)
		{
		for (int i = 0; i < NoScripts(); i++)
			{
			if (Scripts.get(i).Id.value == id) return Scripts.get(i);
			}
		return null;
		}

	public Script getScript(ResId id)
		{
		int ListIndex = ScriptIndex(id);
		if (ListIndex != -1) return Scripts.get(ListIndex);
		return null;
		}

	public Script getScript(String Name)
		{
		int ListIndex = ScriptIndex(Name);
		if (ListIndex != -1) return Scripts.get(ListIndex);
		return null;
		}

	public Script getScriptList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoScripts()) return Scripts.get(ListIndex);
		return null;
		}

	public void removeScript(ResId id)
		{
		int ListIndex = ScriptIndex(id);
		if (ListIndex != -1) Scripts.remove(ListIndex);
		}

	public void removeScript(String Name)
		{
		int ListIndex = ScriptIndex(Name);
		if (ListIndex != -1) Scripts.remove(ListIndex);
		}

	public int ScriptIndex(ResId id)
		{
		for (int i = 0; i < NoScripts(); i++)
			{
			if (getScriptList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int ScriptIndex(String Name)
		{
		for (int i = 0; i < NoScripts(); i++)
			{
			if (getScriptList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearScripts()
		{
		Scripts.clear();
		}

	public void sortScripts()
		{
		Collections.sort(Scripts);
		}

	public void replaceScript(ResId srcId,Script replacement)
		{
		int ind = ScriptIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			Scripts.set(ind,replacement);
			}
		}

	public Script duplicateScript(ResId id,boolean update)
		{
		Script scr2 = null;
		Script scr = getScript(id);
		if (scr != null)
			{
			scr2 = new Script();
			scr2.ScriptStr = scr.ScriptStr;
			if (update)
				{
				LastScriptId++;
				scr2.Id.value = LastScriptId;
				scr2.Name = prefixes[Resource.SCRIPT] + LastScriptId;
				Scripts.add(scr2);
				}
			else
				{
				scr2.Id = scr.Id;
				scr2.Name = scr.Name;
				}
			}
		return scr2;
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Font Functions ">
	private ArrayList<Font> Fonts = new ArrayList<Font>();

	public int NoFonts()
		{
		return Fonts.size();
		}

	public int LastFontId = -1;

	public Font addFont()
		{
		Font font = new Font();
		font.Id.value = LastFontId++;
		Fonts.add(font);
		return font;
		}

	public Font getFontUnsafe(int id)
		{
		for (int i = 0; i < NoFonts(); i++)
			{
			if (Fonts.get(i).Id.value == id) return Fonts.get(i);
			}
		return null;
		}

	public Font getFont(ResId id)
		{
		int ListIndex = FontIndex(id);
		if (ListIndex != -1) return Fonts.get(ListIndex);
		return null;
		}

	public Font getFont(String Name)
		{
		int ListIndex = FontIndex(Name);
		if (ListIndex != -1) return Fonts.get(ListIndex);
		return null;
		}

	public Font getFontList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoFonts()) return Fonts.get(ListIndex);
		return null;
		}

	public void removeFont(ResId id)
		{
		int ListIndex = FontIndex(id);
		if (ListIndex != -1) Fonts.remove(ListIndex);
		}

	public void removeFont(String Name)
		{
		int ListIndex = FontIndex(Name);
		if (ListIndex != -1) Fonts.remove(ListIndex);
		}

	public int FontIndex(ResId id)
		{
		for (int i = 0; i < NoFonts(); i++)
			{
			if (getFontList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int FontIndex(String Name)
		{
		for (int i = 0; i < NoFonts(); i++)
			{
			if (getFontList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearFonts()
		{
		Fonts.clear();
		}

	public void sortFonts()
		{
		Collections.sort(Fonts);
		}

	public void replaceFont(ResId srcId,Font replacement)
		{
		int ind = FontIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			Fonts.set(ind,replacement);
			}
		}

	public Font duplicateFont(ResId id,boolean update)
		{
		Font font2 = null;
		Font font = getFont(id);
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
				LastFontId++;
				font2.Id.value = LastFontId;
				font2.Name = prefixes[Resource.FONT] + LastFontId;
				Fonts.add(font2);
				}
			else
				{
				font2.Id = font.Id;
				font2.Name = font.Name;
				}
			}
		return font2;
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Timeline Functions ">
	private ArrayList<Timeline> Timelines = new ArrayList<Timeline>();

	public int NoTimelines()
		{
		return Timelines.size();
		}

	public int LastTimelineId = -1;

	public Timeline addTimeline()
		{
		Timeline time = new Timeline();
		time.Id.value = LastTimelineId++;
		Timelines.add(time);
		return time;
		}

	public Timeline getTimelineUnsafe(int id)
		{
		for (int i = 0; i < NoTimelines(); i++)
			{
			if (Timelines.get(i).Id.value == id) return Timelines.get(i);
			}
		return null;
		}

	public Timeline getTimeline(ResId id)
		{
		int ListIndex = TimelineIndex(id);
		if (ListIndex != -1) return Timelines.get(ListIndex);
		return null;
		}

	public Timeline getTimeline(String Name)
		{
		int ListIndex = TimelineIndex(Name);
		if (ListIndex != -1) return Timelines.get(ListIndex);
		return null;
		}

	public Timeline getTimelineList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoTimelines()) return Timelines.get(ListIndex);
		return null;
		}

	public void removeTimeline(ResId id)
		{
		int ListIndex = TimelineIndex(id);
		if (ListIndex != -1) Timelines.remove(ListIndex);
		}

	public void removeTimeline(String Name)
		{
		int ListIndex = TimelineIndex(Name);
		if (ListIndex != -1) Timelines.remove(ListIndex);
		}

	public int TimelineIndex(ResId id)
		{
		for (int i = 0; i < NoTimelines(); i++)
			{
			if (getTimelineList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int TimelineIndex(String Name)
		{
		for (int i = 0; i < NoTimelines(); i++)
			{
			if (getTimelineList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearTimelines()
		{
		Timelines.clear();
		}

	public void sortTimelines()
		{
		Collections.sort(Timelines);
		}

	public void replaceTimeline(ResId srcId,Timeline replacement)
		{
		int ind = TimelineIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			Timelines.set(ind,replacement);
			}
		}

	public Timeline duplicateTimeline(ResId id,boolean update)
		{
		Timeline time2 = null;
		Timeline time = getTimeline(id);
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
				LastTimelineId++;
				time2.Id.value = LastTimelineId;
				time2.Name = prefixes[Resource.TIMELINE] + LastTimelineId;
				Timelines.add(time2);
				}
			else
				{
				time2.Id = time.Id;
				time2.Name = time.Name;
				}
			}
		return time2;
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" GmObject Functions ">
	private ArrayList<GmObject> GmObjects = new ArrayList<GmObject>();

	public int NoGmObjects()
		{
		return GmObjects.size();
		}

	public int LastGmObjectId = -1;

	public GmObject addGmObject()
		{
		GmObject obj = new GmObject();
		obj.Id.value = LastGmObjectId++;
		GmObjects.add(obj);
		return obj;
		}

	public GmObject getGmObjectUnsafe(int id)
		{
		for (int i = 0; i < NoGmObjects(); i++)
			{
			if (GmObjects.get(i).Id.value == id) return GmObjects.get(i);
			}
		return null;
		}

	public GmObject getGmObject(ResId id)
		{
		int ListIndex = GmObjectIndex(id);
		if (ListIndex != -1) return GmObjects.get(ListIndex);
		return null;
		}

	public GmObject getGmObject(String Name)
		{
		int ListIndex = GmObjectIndex(Name);
		if (ListIndex != -1) return GmObjects.get(ListIndex);
		return null;
		}

	public GmObject getGmObjectList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoGmObjects()) return GmObjects.get(ListIndex);
		return null;
		}

	public void removeGmObject(ResId id)
		{
		int ListIndex = GmObjectIndex(id);
		if (ListIndex != -1) GmObjects.remove(ListIndex);
		}

	public void removeGmObject(String Name)
		{
		int ListIndex = GmObjectIndex(Name);
		if (ListIndex != -1) GmObjects.remove(ListIndex);
		}

	public int GmObjectIndex(ResId id)
		{
		for (int i = 0; i < NoGmObjects(); i++)
			{
			if (getGmObjectList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int GmObjectIndex(String Name)
		{
		for (int i = 0; i < NoGmObjects(); i++)
			{
			if (getGmObjectList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearGmObjects()
		{
		GmObjects.clear();
		}

	public void sortGmObjects()
		{
		Collections.sort(GmObjects);
		}

	public void replaceGmObject(ResId srcId,GmObject replacement)
		{
		int ind = GmObjectIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			GmObjects.set(ind,replacement);
			}
		}

	public GmObject duplicateGmObject(ResId id,boolean update)
		{
		GmObject obj2 = null;
		GmObject obj = getGmObject(id);
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
				LastGmObjectId++;
				obj2.Id.value = LastGmObjectId;
				obj2.Name = prefixes[Resource.GMOBJECT] + LastGmObjectId;
				GmObjects.add(obj2);
				}
			else
				{
				obj2.Id = obj.Id;
				obj2.Name = obj.Name;
				}
			}
		return obj2;
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Room Functions ">
	private ArrayList<Room> Rooms = new ArrayList<Room>();

	public int NoRooms()
		{
		return Rooms.size();
		}

	public int LastRoomId = -1;

	public Room addRoom()
		{
		Room rm = new Room();
		rm.Container = this;
		rm.Id.value = LastRoomId++;
		Rooms.add(rm);
		return rm;
		}

	public Room getRoomUnsafe(int id)
		{
		for (int i = 0; i < NoRooms(); i++)
			{
			if (Rooms.get(i).Id.value == id) return Rooms.get(i);
			}
		return null;
		}

	public Room getRoom(ResId id)
		{
		int ListIndex = RoomIndex(id);
		if (ListIndex != -1) return Rooms.get(ListIndex);
		return null;
		}

	public Room getRoom(String Name)
		{
		int ListIndex = RoomIndex(Name);
		if (ListIndex != -1) return Rooms.get(ListIndex);
		return null;
		}

	public Room getRoomList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoRooms()) return Rooms.get(ListIndex);
		return null;
		}

	public void removeRoom(ResId id)
		{
		int ListIndex = RoomIndex(id);
		if (ListIndex != -1) Rooms.remove(ListIndex);
		}

	public void removeRoom(String Name)
		{
		int ListIndex = RoomIndex(Name);
		if (ListIndex != -1) Rooms.remove(ListIndex);
		}

	public int RoomIndex(ResId id)
		{
		for (int i = 0; i < NoRooms(); i++)
			{
			if (getRoomList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int RoomIndex(String Name)
		{
		for (int i = 0; i < NoRooms(); i++)
			{
			if (getRoomList(i).Name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clearRooms()
		{
		Rooms.clear();
		}

	public void sortRooms()
		{
		Collections.sort(Rooms);
		}

	public void replaceRoom(ResId srcId,Room replacement)
		{
		int ind = RoomIndex(srcId);
		if (replacement != null && ind >= 0)
			{
			Rooms.set(ind,replacement);
			}
		}

	public Room duplicateRoom(ResId id,boolean update)
		{
		Room rm2 = null;
		Room rm = getRoom(id);
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
				LastRoomId++;
				rm2.Id.value = LastRoomId;
				rm2.Name = prefixes[Resource.ROOM] + LastRoomId;
				Rooms.add(rm2);
				}
			else
				{
				rm2.Id = rm.Id;
				rm2.Name = rm.Name;
				}
			}
		return rm2;
		}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Constants Functions ">
	private ArrayList<Constant> Constants = new ArrayList<Constant>();

	public int NoConstants()
		{
		return Constants.size();
		}

	public Constant addConstant()
		{
		Constant con = new Constant();
		Constants.add(con);
		return con;
		}

	public Constant getConstant(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoConstants()) return Constants.get(ListIndex);
		return null;
		}

	public void removeConstant(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoConstants()) Constants.remove(ListIndex);
		}

	public void clearConstants()
		{
		Constants.clear();
		}

	// </editor-fold>\
	// <editor-fold defaultstate="collapsed" desc=" Include Files Functions ">
	private ArrayList<IncludeFile> IncludeFiles = new ArrayList<IncludeFile>();

	public int NoIncludeFiles()
		{
		return IncludeFiles.size();
		}

	public IncludeFile addIncludeFile()
		{
		IncludeFile inc = new IncludeFile();
		IncludeFiles.add(inc);
		return inc;
		}

	public IncludeFile getIncludeFile(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoIncludeFiles()) return IncludeFiles.get(ListIndex);
		return null;
		}

	public void removeIncludeFile(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoIncludeFiles()) IncludeFiles.remove(ListIndex);
		}

	public void clearIncludeFiles()
		{
		IncludeFiles.clear();
		}

	// </editor-fold>
	public GameInformation GameInfo = new GameInformation();

	public void getResUnsafe(ResNode node, int id)
		{
		switch (node.kind)
			{
			case Resource.SPRITE: node.resourceId = getSpriteUnsafe(id).Id; break;
			case Resource.SOUND: node.resourceId = getSoundUnsafe(id).Id; break;
			case Resource.BACKGROUND: node.resourceId = getBackgroundUnsafe(id).Id; break;
			case Resource.PATH: node.resourceId = getPathUnsafe(id).Id; break;
			case Resource.SCRIPT: node.resourceId = getScriptUnsafe(id).Id; break;
			case Resource.FONT: node.resourceId = getFontUnsafe(id).Id; break;
			case Resource.TIMELINE: node.resourceId = getTimelineUnsafe(id).Id; break;
			case Resource.GMOBJECT: node.resourceId = getGmObjectUnsafe(id).Id; break;
			case Resource.ROOM: node.resourceId = getRoomUnsafe(id).Id; break;
			default: node.resourceId = null; break;
			}
		}

	public void ReadGm6File(String FileName,ResNode root) throws Gm6FormatException
		{
		clearSprites();
		clearSounds();
		clearBackgrounds();
		clearPaths();
		clearScripts();
		clearFonts();
		clearTimelines();
		clearGmObjects();
		clearRooms();
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
				Constant con = addConstant();
				con.Name = in.readStr();
				con.Value = in.readStr();
				}
			no = in.readi();
			for (int i = 0; i < no; i++)
				{
				IncludeFile inc = addIncludeFile();
				inc.FilePath = in.readStr();
				}
			IncludeFolder = in.readi();
			OverwriteExisting = in.readBool();
			RemoveAtGameEnd = in.readBool();
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before sounds - GM version unsupported or file corrupt: " + ver);
			// SOUNDS
			no = in.readi();
			LastSoundId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Sound snd = addSound();
					snd.Name = in.readStr();
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
					LastSoundId++;
				}

			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before Sprites - GM version unsupported or file corrupt: " + ver);
			// SPRITES
			no = in.readi();
			LastSpriteId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Sprite spr = addSprite();
					spr.Name = in.readStr();
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
					LastSpriteId++;
				}

			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before Backgrounds - GM version unsupported or file corrupt: " + ver);
			// BACKGROUNDS
			no = in.readi();
			LastBackgroundId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Background back = addBackground();
					back.Name = in.readStr();
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
					LastBackgroundId++;
				}
			ver = in.readi();
			if (ver != 420)
				throw new Gm6FormatException("Before Paths - GM version unsupported or file corrupt: " + ver);
			// PATHS
			no = in.readi();
			LastPathId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Path path = addPath();
					path.Name = in.readStr();
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
					LastPathId++;
				}
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before Scripts - GM version unsupported or file corrupt: " + ver);
			// SCRIPTS
			no = in.readi();
			LastScriptId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Script scr = addScript();
					scr.Name = in.readStr();
					ver = in.readi();
					if (ver != 400)
						throw new Gm6FormatException("In script " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					scr.ScriptStr = in.readStr();
					}
				else
					LastScriptId++;
				}

			ver = in.readi();
			if (ver != 540)
				throw new Gm6FormatException("Before Fonts - GM version unsupported or file corrupt: " + ver);
			// FONTS
			no = in.readi();
			LastFontId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Font font = addFont();
					font.Name = in.readStr();
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
					LastFontId++;
				}

			ver = in.readi();
			if (ver != 500)
				throw new Gm6FormatException("Before Timelines - GM version unsupported or file corrupt: " + ver);
			// TIMELINES
			no = in.readi();
			LastTimelineId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Timeline time = addTimeline();
					time.Id = timeids.get(i);
					time.Name = in.readStr();
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
											act.Arguments[l].Res = getSpriteUnsafe(Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_SOUND:
											act.Arguments[l].Res = getSoundUnsafe(Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_BACKGROUND:
											act.Arguments[l].Res = getBackgroundUnsafe(Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_PATH:
											act.Arguments[l].Res = getPathUnsafe(Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_SCRIPT:
											act.Arguments[l].Res = getScriptUnsafe(Integer.parseInt(strval)).Id;
											break;
										case Argument.ARG_GMOBJECT:
											act.Arguments[l].Res = objids.get(Integer.parseInt(strval));
											break;
										case Argument.ARG_ROOM:
											act.Arguments[l].Res = rmids.get(Integer.parseInt(strval));
											break;
										case Argument.ARG_FONT:
											act.Arguments[l].Res = getFontUnsafe(Integer.parseInt(strval)).Id;
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
					LastTimelineId++;
				}
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException("Before Objects - GM version unsupported or file corrupt: " + ver);
			// OBJECTS
			no = in.readi();
			LastGmObjectId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					GmObject obj = addGmObject();
					obj.Id = objids.get(i);
					obj.Name = in.readStr();
					ver = in.readi();
					if (ver != 430)
						throw new Gm6FormatException("In Object " + i
								+ " - GM version unsupported or file corrupt: " + ver);
					int temp = in.readi();
					if (getSpriteUnsafe(temp) != null)
						obj.Sprite = getSpriteUnsafe(temp).Id;
					obj.Solid = in.readBool();
					obj.Visible = in.readBool();
					obj.Depth = in.readi();
					obj.Persistent = in.readBool();
					obj.Parent = objids.get(in.readi());
					temp = in.readi();
					if (getSpriteUnsafe(temp) != null)
						obj.Mask = getSpriteUnsafe(temp).Id;
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
													act.Arguments[l].Res = getSpriteUnsafe(Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_SOUND:
													act.Arguments[l].Res = getSoundUnsafe(Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_BACKGROUND:
													act.Arguments[l].Res = getBackgroundUnsafe(Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_PATH:
													act.Arguments[l].Res = getPathUnsafe(Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_SCRIPT:
													act.Arguments[l].Res = getScriptUnsafe(Integer.parseInt(strval)).Id;
													break;
												case Argument.ARG_GMOBJECT:
													act.Arguments[l].Res = objids.get(Integer.parseInt(strval));
													break;
												case Argument.ARG_ROOM:
													act.Arguments[l].Res = rmids.get(Integer.parseInt(strval));
													break;
												case Argument.ARG_FONT:
													act.Arguments[l].Res = getFontUnsafe(Integer.parseInt(strval)).Id;
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
					LastGmObjectId++;
				}
			ver = in.readi();
			if (ver != 420)
				throw new Gm6FormatException("Before Rooms - GM version unsupported or file corrupt: " + ver);
			// ROOMS
			no = in.readi();
			LastRoomId = 0;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Room rm = addRoom();
					rm.Id = rmids.get(i);
					rm.Name = in.readStr();
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
						Background temp = getBackgroundUnsafe(in.readi());
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
						GmObject temp = getGmObjectUnsafe(in.readi());
						if (temp != null)
							vw.ObjectFollowing = temp.Id;
						}
					int noinstances = in.readi();
					for (int j = 0; j < noinstances; j++)
						{
						Instance inst = rm.addInstance();
						inst.X = in.readi();
						inst.Y = in.readi();
						GmObject temp = getGmObjectUnsafe(in.readi());
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
						Background temp = getBackgroundUnsafe(in.readi());
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
					LastRoomId++;
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
			throw new Gm6FormatException(ex.getMessage());
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
			out.writei(this.NoConstants());
			for (int i = 0; i < this.NoConstants(); i++)
				{
				out.writeStr(this.getConstant(i).Name);
				out.writeStr(this.getConstant(i).Value);
				}
			out.writei(this.NoIncludeFiles());
			for (int i = 0; i < this.NoIncludeFiles(); i++)
				out.writeStr(this.getIncludeFile(i).FilePath);
			out.writei(this.IncludeFolder);
			out.writeBool(this.OverwriteExisting);
			out.writeBool(this.RemoveAtGameEnd);

			// SOUNDS
			out.writei(400);
			out.writei(LastSoundId + 1);
			for (int i = 0; i <= LastSoundId; i++)
				{
				Sound snd = getSoundUnsafe(i);
				out.writeBool(snd != null);
				if (snd != null)
					{
					out.writeStr(snd.Name);
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
			out.writei(LastSpriteId + 1);
			for (int i = 0; i <= LastSpriteId; i++)
				{
				Sprite spr = getSpriteUnsafe(i);
				out.writeBool(spr != null);
				if (spr != null)
					{
					out.writeStr(spr.Name);
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
			out.writei(LastBackgroundId + 1);
			for (int i = 0; i <= LastBackgroundId; i++)
				{
				Background back = getBackgroundUnsafe(i);
				out.writeBool(back != null);
				if (back != null)
					{
					out.writeStr(back.Name);
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
			out.writei(LastPathId + 1);
			for (int i = 0; i <= LastPathId; i++)
				{
				Path path = getPathUnsafe(i);
				out.writeBool(path != null);
				if (path != null)
					{
					out.writeStr(path.Name);
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
			out.writei(LastScriptId + 1);
			for (int i = 0; i <= LastScriptId; i++)
				{
				Script scr = getScriptUnsafe(i);
				out.writeBool(scr != null);
				if (scr != null)
					{
					out.writeStr(scr.Name);
					out.writei(400);
					out.writeStr(scr.ScriptStr);
					}
				}

			// FONTS
			out.writei(540);
			out.writei(LastFontId + 1);
			for (int i = 0; i <= LastFontId; i++)
				{
				Font font = getFontUnsafe(i);
				out.writeBool(font != null);
				if (font != null)
					{
					out.writeStr(font.Name);
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
			out.writei(LastTimelineId + 1);
			for (int i = 0; i <= LastTimelineId; i++)
				{
				Timeline time = getTimelineUnsafe(i);
				out.writeBool(time != null);
				if (time != null)
					{
					out.writeStr(time.Name);
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
			out.writei(LastGmObjectId + 1);
			for (int i = 0; i <= LastGmObjectId; i++)
				{
				GmObject obj = getGmObjectUnsafe(i);
				out.writeBool(obj != null);
				if (obj != null)
					{
					out.writeStr(obj.Name);
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
			out.writei(LastRoomId + 1);
			for (int i = 0; i <= LastRoomId; i++)
				{
				Room rm = getRoomUnsafe(i);
				out.writeBool(rm != null);
				if (rm != null)
					{
					out.writeStr(rm.Name);
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