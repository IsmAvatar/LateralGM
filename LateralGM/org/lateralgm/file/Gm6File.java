/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2006, 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * 
 * Lateral GM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Lateral GM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lateral GM; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.lateralgm.file;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.lateralgm.components.ResNode;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Constant;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.Point;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

//TODO Re-implement the icon loading code using com.ctreber.aclib.ico
public class Gm6File
	{
	/*
	 * allows pointing to the ResId of a resource
	 * even when the resource that "owns" it is yet to exist
	 */
	private class IdStack
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
				if (ids.get(i).getValue() == id)
					{
					return ids.get(i);
					}
				}
			ResId newid = new ResId(id);
			ids.add(newid);
			return newid;
			}
		}

	private Map<Integer,ResourceList<?>> resMap = new HashMap<Integer,ResourceList<?>>();
	public ResourceList<Sprite> sprites = new ResourceList<Sprite>(Sprite.class);
	public ResourceList<Sound> sounds = new ResourceList<Sound>(Sound.class);
	public ResourceList<Background> backgrounds = new ResourceList<Background>(Background.class);
	public ResourceList<Path> paths = new ResourceList<Path>(Path.class);
	public ResourceList<Script> scripts = new ResourceList<Script>(Script.class);
	public ResourceList<Font> fonts = new ResourceList<Font>(Font.class);
	public ResourceList<Timeline> timelines = new ResourceList<Timeline>(Timeline.class);
	public ResourceList<GmObject> gmObjects = new ResourceList<GmObject>(GmObject.class);
	public ResourceList<Room> rooms = new ResourceList<Room>(Room.class);
	public ArrayList<Constant> constants = new ArrayList<Constant>();
	public ArrayList<Include> includes = new ArrayList<Include>();

	private final ResourceChangeListener rcl = new ResourceChangeListener();

	private EventListenerList listenerList = new EventListenerList();
	private ChangeEvent changeEvent = null;

	public Gm6File()
		{
		resMap.put(new Integer(Resource.SPRITE),sprites);
		resMap.put(new Integer(Resource.SOUND),sounds);
		resMap.put(new Integer(Resource.BACKGROUND),backgrounds);
		resMap.put(new Integer(Resource.PATH),paths);
		resMap.put(new Integer(Resource.SCRIPT),scripts);
		resMap.put(new Integer(Resource.FONT),fonts);
		resMap.put(new Integer(Resource.TIMELINE),timelines);
		resMap.put(new Integer(Resource.GMOBJECT),gmObjects);
		resMap.put(new Integer(Resource.ROOM),rooms);
		for (ResourceList<?> rl : resMap.values())
			{
			rl.addChangeListener(rcl);
			}
		gameId = new Random().nextInt(100000001);
		gameIconData = new byte[0];
		try
			{
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(
					Gm6File.class.getResource("default.ico").toURI()))); //$NON-NLS-1$
			ByteArrayOutputStream dat = new ByteArrayOutputStream();
			int val = in.read();
			while (val != -1)
				{
				dat.write(val);
				val = in.read();
				}
			gameIconData = dat.toByteArray();
			}
		catch (Exception ex)
			{
			gameIconData = new byte[0];
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
	public int gameId; // randomized in constructor
	public boolean startFullscreen = false;
	public boolean interpolate = false;
	public boolean dontDrawBorder = false;
	public boolean displayCursor = true;
	public int scaling = -1;
	public boolean allowWindowResize = false;
	public boolean alwaysOnTop = false;
	public int colorOutsideRoom = 0;
	public boolean setResolution = false;
	public byte colorDepth = COLOR_NOCHANGE;
	public byte resolution = RES_NOCHANGE;
	public byte frequency = FREQ_NOCHANGE;
	public boolean dontShowButtons = false;
	public boolean useSynchronization = false;
	public boolean letF4SwitchFullscreen = true;
	public boolean letF1ShowGameInfo = true;
	public boolean letEscEndGame = true;
	public boolean letF5SaveF6Load = true;
	public byte gamePriority = PRIORITY_NORMAL;
	public boolean freezeOnLoseFocus = false;
	public byte loadBarMode = LOADBAR_DEFAULT;
	public BufferedImage frontLoadBar = null;
	public BufferedImage backLoadBar = null;
	public boolean showCustomLoadImage = false;
	public BufferedImage loadingImage = null;
	public boolean imagePartiallyTransparent = false;
	public int loadImageAlpha = 255;
	public boolean scaleProgressBar = true;
	public boolean displayErrors = true;
	public boolean writeToLog = false;
	public boolean abortOnError = false;
	public boolean treatUninitializedAs0 = false;
	public String author = ""; //$NON-NLS-1$
	public int version = 100;
	public double lastChanged = longTimeToGmTime(System.currentTimeMillis());
	public String information = ""; //$NON-NLS-1$
	public int includeFolder = INCLUDE_MAIN;
	public boolean overwriteExisting = false;
	public boolean removeAtGameEnd = false;
	public int lastInstanceId = 100000;
	public int lastTileId = 10000000;

	// actual data is stored to be written on resave (no reason to re-encode)
	public byte[] gameIconData;
	public BufferedImage gameIcon; // icon as image for display purposes

	// Returns the ResourceList corresponding to given Resource constant
	public ResourceList<?> getList(int res)
		{
		return resMap.get(res);
		}

	public void clearAll()
		{
		for (ResourceList<?> l : resMap.values())
			l.clear();
		constants.clear();
		includes.clear();
		}

	public GameInformation gameInfo = new GameInformation();

	// TODO externalise the file IO?
	public void readGm6File(String fileName, ResNode root) throws Gm6FormatException
		{
		clearAll();
		GmStreamDecoder in = null;
		try
			{
			long startTime = System.currentTimeMillis();
			in = new GmStreamDecoder(fileName);
			IdStack timeids = new IdStack(); // timeline ids
			IdStack objids = new IdStack(); // object ids
			IdStack rmids = new IdStack(); // room ids
			int identifier = in.readi();
			if (identifier != 1234321)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_INVALID"),fileName,identifier)); //$NON-NLS-1$
			int ver = in.readi();
			if (ver != 600)
				{
				String msg = Messages.getString("Gm6File.ERROR_UNSUPPORTED"); //$NON-NLS-1$
				throw new Gm6FormatException(String.format(msg,ver));
				}
			gameId = in.readi();
			in.skip(16); // unknown bytes following game id
			ver = in.readi();
			if (ver != 600)
				{
				String msg = Messages.getString("Gm6File.ERROR_UNSUPPORTED"); //$NON-NLS-1$
				throw new Gm6FormatException(String.format(msg,ver));
				}
			startFullscreen = in.readBool();
			interpolate = in.readBool();
			dontDrawBorder = in.readBool();
			displayCursor = in.readBool();
			scaling = in.readi();
			allowWindowResize = in.readBool();
			alwaysOnTop = in.readBool();
			colorOutsideRoom = in.readi();
			setResolution = in.readBool();
			colorDepth = (byte) in.readi();
			resolution = (byte) in.readi();
			frequency = (byte) in.readi();
			dontShowButtons = in.readBool();
			useSynchronization = in.readBool();
			letF4SwitchFullscreen = in.readBool();
			letF1ShowGameInfo = in.readBool();
			letEscEndGame = in.readBool();
			letF5SaveF6Load = in.readBool();
			gamePriority = (byte) in.readi();
			freezeOnLoseFocus = in.readBool();
			loadBarMode = (byte) in.readi();
			if (loadBarMode == LOADBAR_CUSTOM)
				{
				if (in.readi() != -1) backLoadBar = in.readImage();
				if (in.readi() != -1) frontLoadBar = in.readImage();
				}
			showCustomLoadImage = in.readBool();
			if (showCustomLoadImage) if (in.readi() != -1) loadingImage = in.readImage();
			imagePartiallyTransparent = in.readBool();
			loadImageAlpha = in.readi();
			scaleProgressBar = in.readBool();

			int length = in.readi();
			gameIconData = new byte[length];
			in.read(gameIconData,0,length);
			// GameIcon=(BufferedImage)new ICOFile(new
			// ByteArrayInputStream(GameIconData)).getDescriptor(0).getImageRGB();

			displayErrors = in.readBool();
			writeToLog = in.readBool();
			abortOnError = in.readBool();
			treatUninitializedAs0 = in.readBool();
			author = in.readStr();
			version = in.readi();
			lastChanged = in.readD();
			information = in.readStr();
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
			includeFolder = in.readi();
			overwriteExisting = in.readBool();
			removeAtGameEnd = in.readBool();
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESOUNDS"),ver)); //$NON-NLS-1$
			// SOUNDS
			no = in.readi();
			sounds.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Sound snd = sounds.add();
					snd.setName(in.readStr());
					ver = in.readi();
					if (ver != 600)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INSOUND"),i,ver)); //$NON-NLS-1$
					snd.kind = (byte) in.readi();
					snd.fileType = in.readStr();
					snd.fileName = in.readStr();
					if (in.readBool()) snd.data = in.decompress(in.readi());
					int effects = in.readi();
					snd.setEffects(effects);
					snd.volume = in.readD();
					snd.pan = in.readD();
					snd.preload = in.readBool();
					}
				else
					sounds.lastId++;
				}

			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESPRITES"),ver)); //$NON-NLS-1$
			// SPRITES
			no = in.readi();
			sprites.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Sprite spr = sprites.add();
					spr.setName(in.readStr());
					ver = in.readi();
					if (ver != 542)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INSPRITE"),i,ver)); //$NON-NLS-1$
					spr.width = in.readi();
					spr.height = in.readi();
					spr.boundingBoxLeft = in.readi();
					spr.boundingBoxRight = in.readi();
					spr.boundingBoxBottom = in.readi();
					spr.boundingBoxTop = in.readi();
					spr.transparent = in.readBool();
					spr.smoothEdges = in.readBool();
					spr.preload = in.readBool();
					spr.boundingBoxMode = (byte) in.readi();
					spr.preciseCC = in.readBool();
					spr.originX = in.readi();
					spr.originY = in.readi();
					int nosub = in.readi();
					for (int j = 0; j < nosub; j++)
						{
						in.skip(4);
						spr.addSubImage(ImageIO.read(new ByteArrayInputStream(in.decompress(in.readi()))));
						}
					}
				else
					sprites.lastId++;
				}

			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREBACKGROUNDS"),ver)); //$NON-NLS-1$
			// BACKGROUNDS
			no = in.readi();
			backgrounds.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Background back = backgrounds.add();
					back.setName(in.readStr());
					ver = in.readi();
					if (ver != 543)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INBACKGROUND"),i,ver)); //$NON-NLS-1$
					back.width = in.readi();
					back.height = in.readi();
					back.transparent = in.readBool();
					back.smoothEdges = in.readBool();
					back.preload = in.readBool();
					back.useAsTileSet = in.readBool();
					back.tileWidth = in.readi();
					back.tileHeight = in.readi();
					back.horizOffset = in.readi();
					back.vertOffset = in.readi();
					back.horizSep = in.readi();
					back.vertSep = in.readi();
					if (in.readBool())
						{
						in.skip(4); // 0A
						ByteArrayInputStream is = new ByteArrayInputStream(in.decompress(in.readi()));
						back.backgroundImage = ImageIO.read(is);
						}
					}
				else
					backgrounds.lastId++;
				}
			ver = in.readi();
			if (ver != 420)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREPATHS"),ver)); //$NON-NLS-1$
			// PATHS
			no = in.readi();
			paths.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Path path = paths.add();
					path.setName(in.readStr());
					ver = in.readi();
					if (ver != 530)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INPATH"),i,ver)); //$NON-NLS-1$
					path.smooth = in.readBool();
					path.closed = in.readBool();
					path.precision = in.readi();
					path.backgroundRoom = rmids.get(in.readi());
					path.snapX = in.readi();
					path.snapY = in.readi();
					int nopoints = in.readi();
					for (int j = 0; j < nopoints; j++)
						{
						Point point = path.addPoint();
						point.x = (int) in.readD();
						point.y = (int) in.readD();
						point.speed = (int) in.readD();
						}
					}
				else
					paths.lastId++;
				}
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESCRIPTS"),ver)); //$NON-NLS-1$
			// SCRIPTS
			no = in.readi();
			scripts.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Script scr = scripts.add();
					scr.setName(in.readStr());
					ver = in.readi();
					if (ver != 400)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INSCRIPT"),i,ver)); //$NON-NLS-1$
					scr.scriptStr = in.readStr();
					}
				else
					scripts.lastId++;
				}

			ver = in.readi();
			if (ver != 540)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREFONTS"),ver)); //$NON-NLS-1$
			// FONTS
			no = in.readi();
			fonts.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Font font = fonts.add();
					font.setName(in.readStr());
					ver = in.readi();
					if (ver != 540)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INFONT"),i,ver)); //$NON-NLS-1$
					font.fontName = in.readStr();
					font.size = in.readi();
					font.bold = in.readBool();
					font.italic = in.readBool();
					font.charRangeMin = in.readi();
					font.charRangeMax = in.readi();
					}
				else
					fonts.lastId++;
				}

			ver = in.readi();
			if (ver != 500)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFORETIMELINES"),ver)); //$NON-NLS-1$
			// TIMELINES
			// workaround for the case statement below (declared here to prevent repeated instantiation)
			Resource tag = new Script();
			no = in.readi();
			timelines.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Timeline time = timelines.add();
					time.setId(timeids.get(i));
					time.setName(in.readStr());
					ver = in.readi();
					if (ver != 500)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INTIMELINE"),i,ver)); //$NON-NLS-1$
					int nomoms = in.readi();
					for (int j = 0; j < nomoms; j++)
						{
						Moment mom = time.addMoment();
						mom.stepNo = in.readi();
						ver = in.readi();
						if (ver != 400)
							{
							String msg;
							msg = Messages.getString("Gm6File.ERROR_UNSUPPORTED_INTIMELINEMOMENT"); //$NON-NLS-1$
							throw new Gm6FormatException(String.format(msg,i,j,ver));
							}
						int noacts = in.readi();
						for (int k = 0; k < noacts; k++)
							{
							in.skip(4);
							Action act = mom.addAction();
							int libid = in.readi();
							int actid = in.readi();
							act.libAction = LibManager.getLibAction(libid,actid);
							//The libAction will have a null parent, among other things
							if (act.libAction == null)
								{
								act.libAction = new LibAction();
								act.libAction.id = actid;
								act.libAction.parentId = libid;
								act.libAction.actionKind = (byte) in.readi();
								act.libAction.allowRelative = in.readBool();
								act.libAction.question = in.readBool();
								act.libAction.canApplyTo = in.readBool();
								act.libAction.execType = (byte) in.readi();
								act.libAction.execFunction = in.readStr();
								act.libAction.execCode = in.readStr();
								}
							else
								{
								in.skip(20);
								in.skip(in.readi());
								in.skip(in.readi());
								}
							act.arguments = new Argument[in.readi()];
							int[] argkinds = new int[in.readi()];
							for (int x : argkinds)
								x = in.readi();
							int appliesTo = in.readi();
							switch (appliesTo)
								{
								case -1:
									act.appliesTo = GmObject.OBJECT_SELF;
									break;
								case -2:
									act.appliesTo = GmObject.OBJECT_OTHER;
									break;
								default:
									act.appliesTo = objids.get(appliesTo);
								}
							act.relative = in.readBool();
							int actualnoargs = in.readi();

							for (int l = 0; l < actualnoargs; l++)
								{
								if (l < act.arguments.length)
									{
									act.arguments[l] = new Argument();
									act.arguments[l].kind = (byte) argkinds[l];

									String strval = in.readStr();
									Resource res = tag;
									switch (argkinds[l])
										{
										case Argument.ARG_SPRITE:
											res = sprites.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_SOUND:
											res = sounds.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_BACKGROUND:
											res = backgrounds.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_PATH:
											res = paths.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_SCRIPT:
											res = scripts.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_GMOBJECT:
											act.arguments[l].res = objids.get(Integer.parseInt(strval));
											break;
										case Argument.ARG_ROOM:
											act.arguments[l].res = rmids.get(Integer.parseInt(strval));
											break;
										case Argument.ARG_FONT:
											res = fonts.getUnsafe(Integer.parseInt(strval));
											break;
										case Argument.ARG_TIMELINE:
											act.arguments[l].res = timeids.get(Integer.parseInt(strval));
											break;
										default:
											act.arguments[l].val = strval;
											break;
										}
									if (res != null && res != tag)
										{
										act.arguments[l].res = res.getId();
										}
									}
								else
									{
									in.skip(in.readi());
									}
								}
							act.not = in.readBool();
							}
						}
					}
				else
					timelines.lastId++;
				}
			ver = in.readi();
			if (ver != 400)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREOBJECTS"),ver)); //$NON-NLS-1$
			// OBJECTS
			no = in.readi();
			gmObjects.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					GmObject obj = gmObjects.add();
					obj.setId(objids.get(i));
					obj.setName(in.readStr());
					ver = in.readi();
					if (ver != 430)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INOBJECT"),i,ver)); //$NON-NLS-1$
					Sprite temp = sprites.getUnsafe(in.readi());
					if (temp != null) obj.sprite = temp.getId();
					obj.solid = in.readBool();
					obj.visible = in.readBool();
					obj.depth = in.readi();
					obj.persistent = in.readBool();
					obj.parent = objids.get(in.readi());
					temp = sprites.getUnsafe(in.readi());
					if (temp != null) obj.mask = temp.getId();
					in.skip(4);
					for (int j = 0; j < 11; j++)
						{
						boolean done = false;
						while (!done)
							{
							int first = in.readi();
							if (first != -1)
								{
								Event ev = obj.mainEvents[j].addEvent();
								if (j == MainEvent.EV_COLLISION)
									ev.other = objids.get(first);
								else
									ev.id = first;
								ver = in.readi();
								if (ver != 400)
									throw new Gm6FormatException(String.format(
											Messages.getString("Gm6File.ERROR_UNSUPPORTED_INOBJECTEVENT"), //$NON-NLS-1$
											i,j,ver));
								int noacts = in.readi();
								for (int k = 0; k < noacts; k++)
									{
									in.skip(4);
									Action act = ev.addAction();
									int libid = in.readi();
									int actid = in.readi();
									act.libAction = LibManager.getLibAction(libid,actid);
									//The libAction will have a null parent, among other things
									if (act.libAction == null)
										{
										act.libAction = new LibAction();
										act.libAction.id = actid;
										act.libAction.parentId = libid;
										act.libAction.actionKind = (byte) in.readi();
										act.libAction.allowRelative = in.readBool();
										act.libAction.question = in.readBool();
										act.libAction.canApplyTo = in.readBool();
										act.libAction.execType = (byte) in.readi();
										act.libAction.execFunction = in.readStr();
										act.libAction.execCode = in.readStr();
										}
									else
										{
										in.skip(20);
										in.skip(in.readi());
										in.skip(in.readi());
										}
									act.arguments = new Argument[in.readi()];
									int[] argkinds = new int[in.readi()];
									for (int l = 0; l < argkinds.length; l++)
										argkinds[l] = in.readi();
									int id = in.readi();
									switch (id)
										{
										case -1:
											act.appliesTo = GmObject.OBJECT_SELF;
											break;
										case -2:
											act.appliesTo = GmObject.OBJECT_OTHER;
											break;
										default:
											act.appliesTo = objids.get(id);
										}
									act.relative = in.readBool();
									int actualnoargs = in.readi();
									for (int l = 0; l < actualnoargs; l++)
										{
										if (l < act.arguments.length)
											{
											act.arguments[l] = new Argument();
											act.arguments[l].kind = (byte) argkinds[l];
											String strval = in.readStr();
											Resource res = tag; // see before Timeline
											switch (argkinds[l])
												{
												case Argument.ARG_SPRITE:
													res = sprites.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_SOUND:
													res = sounds.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_BACKGROUND:
													res = backgrounds.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_PATH:
													res = paths.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_SCRIPT:
													res = scripts.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_GMOBJECT:
													act.arguments[l].res = objids.get(Integer.parseInt(strval));
													break;
												case Argument.ARG_ROOM:
													act.arguments[l].res = rmids.get(Integer.parseInt(strval));
													break;
												case Argument.ARG_FONT:
													res = fonts.getUnsafe(Integer.parseInt(strval));
													break;
												case Argument.ARG_TIMELINE:
													act.arguments[l].res = timeids.get(Integer.parseInt(strval));
													break;
												default:
													act.arguments[l].val = strval;
													break;
												}
											if (res != null && res != tag)
												{
												act.arguments[l].res = res.getId();
												}
											}
										else
											in.skip(in.readi());
										}
									act.not = in.readBool();
									}
								}
							else
								done = true;
							}
						}
					}
				else
					gmObjects.lastId++;
				}
			ver = in.readi();
			if (ver != 420)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREROOMS"),ver)); //$NON-NLS-1$
			// ROOMS
			no = in.readi();
			rooms.lastId = -1;
			for (int i = 0; i < no; i++)
				{
				if (in.readBool())
					{
					Room rm = rooms.add(new Room(this));
					rm.setId(rmids.get(i));
					rm.setName(in.readStr());
					ver = in.readi();
					if (ver != 541)
						throw new Gm6FormatException(String.format(
								Messages.getString("Gm6File.ERROR_UNSUPPORTED_INROOM"),i,ver)); //$NON-NLS-1$
					rm.caption = in.readStr();
					rm.width = in.readi();
					rm.height = in.readi();
					rm.snapY = in.readi();
					rm.snapX = in.readi();
					rm.isometricGrid = in.readBool();
					rm.speed = in.readi();
					rm.persistent = in.readBool();
					rm.backgroundColor = in.readi();
					rm.drawBackgroundColor = in.readBool();
					rm.creationCode = in.readStr();
					int nobackgrounds = in.readi();
					for (int j = 0; j < nobackgrounds; j++)
						{
						BackgroundDef bk = rm.backgroundDefs[j];
						bk.visible = in.readBool();
						bk.foreground = in.readBool();
						Background temp = backgrounds.getUnsafe(in.readi());
						if (temp != null) bk.backgroundId = temp.getId();
						bk.x = in.readi();
						bk.y = in.readi();
						bk.tileHoriz = in.readBool();
						bk.tileVert = in.readBool();
						bk.horizSpeed = in.readi();
						bk.vertSpeed = in.readi();
						bk.stretch = in.readBool();
						}
					rm.enableViews = in.readBool();
					int noviews = in.readi();
					for (int j = 0; j < noviews; j++)
						{
						View vw = rm.views[j];
						vw.enabled = in.readBool();
						vw.viewX = in.readi();
						vw.viewY = in.readi();
						vw.viewW = in.readi();
						vw.viewH = in.readi();
						vw.portX = in.readi();
						vw.portY = in.readi();
						vw.portW = in.readi();
						vw.portH = in.readi();
						vw.hbor = in.readi();
						vw.vbor = in.readi();
						vw.hspeed = in.readi();
						vw.vspeed = in.readi();
						GmObject temp = gmObjects.getUnsafe(in.readi());
						if (temp != null) vw.objectFollowing = temp.getId();
						}
					int noinstances = in.readi();
					for (int j = 0; j < noinstances; j++)
						{
						Instance inst = rm.addInstance();
						inst.x = in.readi();
						inst.y = in.readi();
						GmObject temp = gmObjects.getUnsafe(in.readi());
						if (temp != null) inst.gmObjectId = temp.getId();
						inst.instanceId = in.readi();
						inst.creationCode = in.readStr();
						inst.locked = in.readBool();
						}
					int notiles = in.readi();
					for (int j = 0; j < notiles; j++)
						{
						Tile ti = rm.addTile();
						ti.x = in.readi();
						ti.y = in.readi();
						Background temp = backgrounds.getUnsafe(in.readi());
						if (temp != null) ti.backgroundId = temp.getId();
						ti.tileX = in.readi();
						ti.tileY = in.readi();
						ti.width = in.readi();
						ti.height = in.readi();
						ti.depth = in.readi();
						ti.tileId = in.readi();
						ti.locked = in.readBool();
						}
					rm.rememberWindowSize = in.readBool();
					rm.editorWidth = in.readi();
					rm.editorHeight = in.readi();
					rm.showGrid = in.readBool();
					rm.showObjects = in.readBool();
					rm.showTiles = in.readBool();
					rm.showBackgrounds = in.readBool();
					rm.showForegrounds = in.readBool();
					rm.showViews = in.readBool();
					rm.deleteUnderlyingObjects = in.readBool();
					rm.deleteUnderlyingTiles = in.readBool();
					rm.currentTab = in.readi();
					rm.scrollBarX = in.readi();
					rm.scrollBarY = in.readi();
					}
				else
					rooms.lastId++;
				}
			lastInstanceId = in.readi();
			lastTileId = in.readi();
			ver = in.readi();
			if (ver != 600)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREINFO"),ver)); //$NON-NLS-1$
			int bc = in.readi();
			if (bc >= 0) gameInfo.backgroundColor = new Color(bc);
			gameInfo.mimicGameWindow = in.readBool();
			gameInfo.formCaption = in.readStr();
			gameInfo.left = in.readi();
			gameInfo.top = in.readi();
			gameInfo.width = in.readi();
			gameInfo.height = in.readi();
			gameInfo.showBorder = in.readBool();
			gameInfo.allowResize = in.readBool();
			gameInfo.stayOnTop = in.readBool();
			gameInfo.pauseGame = in.readBool();
			gameInfo.gameInfoStr = in.readStr();
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
			in.skip(in.readi() * 4); // room indexes in tree order;
			in.readTree(root,this);
			System.out.printf(Messages.getString("Gm6File.LOADTIME"), //$NON-NLS-1$
					System.currentTimeMillis() - startTime);
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

	public void writeGm6File(String fileName, ResNode root)
		{
		long savetime = System.currentTimeMillis();
		GmStreamEncoder out = null;
		try
			{
			out = new GmStreamEncoder(fileName);
			out.writei(1234321);
			out.writei(600);
			out.writei(gameId);
			out.fill(4);
			out.writei(600);
			out.writeBool(startFullscreen);
			out.writeBool(interpolate);
			out.writeBool(dontDrawBorder);
			out.writeBool(displayCursor);
			out.writei(scaling);
			out.writeBool(allowWindowResize);
			out.writeBool(alwaysOnTop);
			out.writei(colorOutsideRoom);
			out.writeBool(setResolution);
			out.writei(colorDepth);
			out.writei(resolution);
			out.writei(frequency);
			out.writeBool(dontShowButtons);
			out.writeBool(useSynchronization);
			out.writeBool(letF4SwitchFullscreen);
			out.writeBool(letF1ShowGameInfo);
			out.writeBool(letEscEndGame);
			out.writeBool(letF5SaveF6Load);
			out.writei(gamePriority);
			out.writeBool(freezeOnLoseFocus);
			out.writei(loadBarMode);
			if (loadBarMode == LOADBAR_CUSTOM)
				{
				if (backLoadBar != null)
					{
					out.writei(10);
					out.writeImage(backLoadBar);
					}
				else
					out.writei(-1);
				if (frontLoadBar != null)
					{
					out.writei(10);
					out.writeImage(frontLoadBar);
					}
				else
					out.writei(-1);
				}
			out.writeBool(showCustomLoadImage);
			if (this.showCustomLoadImage)
				{
				if (this.loadingImage != null)
					{
					out.writei(10);
					out.writeImage(loadingImage);
					}
				else
					out.writei(-1);
				}
			out.writeBool(imagePartiallyTransparent);
			out.writei(loadImageAlpha);
			out.writeBool(scaleProgressBar);
			out.write(gameIconData);
			out.writeBool(displayErrors);
			out.writeBool(writeToLog);
			out.writeBool(abortOnError);
			out.writeBool(treatUninitializedAs0);
			out.writeStr(author);
			out.writei(version);
			lastChanged = longTimeToGmTime(savetime);
			out.writeD(lastChanged);

			out.writeStr(information);
			out.writei(constants.size());
			for (int i = 0; i < constants.size(); i++)
				{
				out.writeStr(constants.get(i).name);
				out.writeStr(constants.get(i).value);
				}
			out.writei(includes.size());
			for (int i = 0; i < includes.size(); i++)
				out.writeStr(includes.get(i).filePath);
			out.writei(includeFolder);
			out.writeBool(overwriteExisting);
			out.writeBool(removeAtGameEnd);

			// SOUNDS
			out.writei(400);
			out.writei(sounds.lastId + 1);
			for (int i = 0; i <= sounds.lastId; i++)
				{
				Sound snd = sounds.getUnsafe(i);
				out.writeBool(snd != null);
				if (snd != null)
					{
					out.writeStr(snd.getName());
					out.writei(600);
					out.writei(snd.kind);
					out.writeStr(snd.fileType);
					out.writeStr(snd.fileName);
					if (snd.data != null)
						{
						out.writeBool(true);
						out.compress(snd.data);
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
			out.writei(sprites.lastId + 1);
			for (int i = 0; i <= sprites.lastId; i++)
				{
				Sprite spr = sprites.getUnsafe(i);
				out.writeBool(spr != null);
				if (spr != null)
					{
					out.writeStr(spr.getName());
					out.writei(542);
					out.writei(spr.width);
					out.writei(spr.height);
					out.writei(spr.boundingBoxLeft);
					out.writei(spr.boundingBoxRight);
					out.writei(spr.boundingBoxBottom);
					out.writei(spr.boundingBoxTop);
					out.writeBool(spr.transparent);
					out.writeBool(spr.smoothEdges);
					out.writeBool(spr.preload);
					out.writei(spr.boundingBoxMode);
					out.writeBool(spr.preciseCC);
					out.writei(spr.originX);
					out.writei(spr.originY);
					out.writei(spr.noSubImages());
					for (int j = 0; j < spr.noSubImages(); j++)
						{
						BufferedImage sub = spr.getSubImage(j);
						out.writei(10);
						out.writeImage(sub);
						}
					}
				}

			// BACKGROUNDS
			out.writei(400);
			out.writei(backgrounds.lastId + 1);
			for (int i = 0; i <= backgrounds.lastId; i++)
				{
				Background back = backgrounds.getUnsafe(i);
				out.writeBool(back != null);
				if (back != null)
					{
					out.writeStr(back.getName());
					out.writei(543);
					out.writei(back.width);
					out.writei(back.height);
					out.writeBool(back.transparent);
					out.writeBool(back.smoothEdges);
					out.writeBool(back.preload);
					out.writeBool(back.useAsTileSet);
					out.writei(back.tileWidth);
					out.writei(back.tileHeight);
					out.writei(back.horizOffset);
					out.writei(back.vertOffset);
					out.writei(back.horizSep);
					out.writei(back.vertSep);
					if (back.backgroundImage != null)
						{
						out.writeBool(true);
						out.writei(10);
						out.writeImage(back.backgroundImage);
						}
					else
						out.writeBool(false);
					}
				}

			// PATHS
			out.writei(420);
			out.writei(paths.lastId + 1);
			for (int i = 0; i <= paths.lastId; i++)
				{
				Path path = paths.getUnsafe(i);
				out.writeBool(path != null);
				if (path != null)
					{
					out.writeStr(path.getName());
					out.writei(530);
					out.writeBool(path.smooth);
					out.writeBool(path.closed);
					out.writei(path.precision);
					out.writeId(path.backgroundRoom,Resource.ROOM,this);
					out.writei(path.snapX);
					out.writei(path.snapY);
					out.writei(path.noPoints());
					for (int j = 0; j < path.noPoints(); j++)
						{
						out.writeD(path.getPoint(j).x);
						out.writeD(path.getPoint(j).y);
						out.writeD(path.getPoint(j).speed);
						}
					}
				}

			// SCRIPTS
			out.writei(400);
			out.writei(scripts.lastId + 1);
			for (int i = 0; i <= scripts.lastId; i++)
				{
				Script scr = scripts.getUnsafe(i);
				out.writeBool(scr != null);
				if (scr != null)
					{
					out.writeStr(scr.getName());
					out.writei(400);
					out.writeStr(scr.scriptStr);
					}
				}

			// FONTS
			out.writei(540);
			out.writei(fonts.lastId + 1);
			for (int i = 0; i <= fonts.lastId; i++)
				{
				Font font = fonts.getUnsafe(i);
				out.writeBool(font != null);
				if (font != null)
					{
					out.writeStr(font.getName());
					out.writei(540);
					out.writeStr(font.fontName);
					out.writei(font.size);
					out.writeBool(font.bold);
					out.writeBool(font.italic);
					out.writei(font.charRangeMin);
					out.writei(font.charRangeMax);
					}
				}

			// TIMELINES
			out.writei(500);
			out.writei(timelines.lastId + 1);
			for (int i = 0; i <= timelines.lastId; i++)
				{
				Timeline time = timelines.getUnsafe(i);
				out.writeBool(time != null);
				if (time != null)
					{
					out.writeStr(time.getName());
					out.writei(500);
					out.writei(time.noMoments());
					for (int j = 0; j < time.noMoments(); j++)
						{
						Moment mom = time.getMomentList(j);
						out.writei(mom.stepNo);
						out.writei(400);
						out.writei(mom.noActions());
						for (int k = 0; k < mom.noActions(); k++)
							{
							Action act = mom.getAction(k);
							out.writei(440);
							out.writei(act.libAction.parent != null ? act.libAction.parent.id
									: act.libAction.parentId);
							out.writei(act.libAction.id);
							out.writei(act.libAction.actionKind);
							out.writeBool(act.libAction.allowRelative);
							out.writeBool(act.libAction.question);
							out.writeBool(act.libAction.canApplyTo);
							out.writei(act.libAction.execType);
							out.writeStr(act.libAction.execFunction);
							out.writeStr(act.libAction.execCode);
							out.writei(act.arguments.length);

							out.writei(act.arguments.length);
							for (Argument arg : act.arguments)
								out.writei(arg.kind);

							if (act.appliesTo != null)
								{
								if (act.appliesTo.getValue() >= 0)
									out.writeId(act.appliesTo,Resource.GMOBJECT,-100,this);
								else
									// self/other are exceptions to the system
									out.writei(act.appliesTo.getValue());
								}
							else
								out.writei(-100);
							out.writeBool(act.relative);

							out.writei(act.arguments.length);
							for (Argument arg : act.arguments)
								switch (arg.kind)

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
										out.writeIdStr(arg.res,arg.kind,this);
										break;
									default:
										out.writeStr(arg.val);
										break;
									}
							out.writeBool(act.not);
							}
						}
					}
				}

			// (GM)OBJECTS
			out.writei(400);
			out.writei(gmObjects.lastId + 1);
			for (int i = 0; i <= gmObjects.lastId; i++)
				{
				GmObject obj = gmObjects.getUnsafe(i);
				out.writeBool(obj != null);
				if (obj != null)
					{
					out.writeStr(obj.getName());
					out.writei(430);
					out.writeId(obj.sprite,Resource.SPRITE,this);
					out.writeBool(obj.solid);
					out.writeBool(obj.visible);
					out.writei(obj.depth);
					out.writeBool(obj.persistent);
					out.writeId(obj.parent,Resource.GMOBJECT,-100,this);
					out.writeId(obj.mask,Resource.SPRITE,this);
					out.writei(10);
					for (int j = 0; j < 11; j++)
						{
						for (int k = 0; k < obj.mainEvents[j].noEvents(); k++)
							{
							Event ev = obj.mainEvents[j].getEventList(k);
							if (j == MainEvent.EV_COLLISION)
								out.writeId(ev.other,Resource.GMOBJECT,this);
							else
								out.writei(ev.id);
							out.writei(400);
							out.writei(ev.noActions());
							for (int l = 0; l < ev.noActions(); l++)
								{
								Action act = ev.getAction(l);
								out.writei(440);
								out.writei(act.libAction.parent != null ? act.libAction.parent.id
										: act.libAction.parentId);
								out.writei(act.libAction.id);
								out.writei(act.libAction.actionKind);
								out.writeBool(act.libAction.allowRelative);
								out.writeBool(act.libAction.question);
								out.writeBool(act.libAction.canApplyTo);
								out.writei(act.libAction.execType);
								out.writeStr(act.libAction.execFunction);
								out.writeStr(act.libAction.execCode);
								out.writei(act.arguments.length);
								
								out.writei(act.arguments.length);
								for (Argument arg : act.arguments)
									out.writei(arg.kind);

								if (act.appliesTo != null)
									{
									if (act.appliesTo.getValue() >= 0)
										out.writeId(act.appliesTo,Resource.GMOBJECT,-100,this);
									else
										// self/other are exceptions to the system
										out.writei(act.appliesTo.getValue());
									}
								else
									out.writei(-100);
								out.writeBool(act.relative);
								out.writei(act.arguments.length);
								for (Argument arg : act.arguments)
									switch (arg.kind)

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
											out.writeIdStr(arg.res,arg.kind,this);
											break;
										default:
											out.writeStr(arg.val);
											break;
										}
								out.writeBool(act.not);
								}
							}
						out.writei(-1);
						}
					}
				}

			// ROOMS
			out.writei(420);
			out.writei(rooms.lastId + 1);
			for (int i = 0; i <= rooms.lastId; i++)
				{
				Room rm = rooms.getUnsafe(i);
				out.writeBool(rm != null);
				if (rm != null)
					{
					out.writeStr(rm.getName());
					out.writei(541);
					out.writeStr(rm.caption);
					out.writei(rm.width);
					out.writei(rm.height);
					out.writei(rm.snapY);
					out.writei(rm.snapX);
					out.writeBool(rm.isometricGrid);
					out.writei(rm.speed);
					out.writeBool(rm.persistent);
					out.writei(rm.backgroundColor);
					out.writeBool(rm.drawBackgroundColor);
					out.writeStr(rm.creationCode);
					out.writei(8);
					for (int j = 0; j < 8; j++)
						{
						BackgroundDef back = rm.backgroundDefs[j];
						out.writeBool(back.visible);
						out.writeBool(back.foreground);
						out.writeId(back.backgroundId,Resource.BACKGROUND,this);
						out.writei(back.x);
						out.writei(back.y);
						out.writeBool(back.tileHoriz);
						out.writeBool(back.tileVert);
						out.writei(back.horizSpeed);
						out.writei(back.vertSpeed);
						out.writeBool(back.stretch);
						}
					out.writeBool(rm.enableViews);
					out.writei(8);
					for (int j = 0; j < 8; j++)
						{
						View view = rm.views[j];
						out.writeBool(view.enabled);
						out.writei(view.viewX);
						out.writei(view.viewY);
						out.writei(view.viewW);
						out.writei(view.viewH);
						out.writei(view.portX);
						out.writei(view.portY);
						out.writei(view.portW);
						out.writei(view.portH);
						out.writei(view.hbor);
						out.writei(view.vbor);
						out.writei(view.hspeed);
						out.writei(view.vspeed);
						out.writeId(view.objectFollowing,Resource.GMOBJECT,this);
						}
					out.writei(rm.noInstances());
					for (int j = 0; j < rm.noInstances(); j++)
						{
						Instance in = rm.getInstanceList(j);
						out.writei(in.x);
						out.writei(in.y);
						out.writeId(in.gmObjectId,Resource.GMOBJECT,this);
						out.writei(in.instanceId);
						out.writeStr(in.creationCode);
						out.writeBool(in.locked);
						}
					out.writei(rm.noTiles());
					for (int j = 0; j < rm.noTiles(); j++)
						{
						Tile tile = rm.getTileList(j);
						out.writei(tile.x);
						out.writei(tile.y);
						out.writeId(tile.backgroundId,Resource.BACKGROUND,this);
						out.writei(tile.tileX);
						out.writei(tile.tileY);
						out.writei(tile.width);
						out.writei(tile.height);
						out.writei(tile.depth);
						out.writei(tile.tileId);
						out.writeBool(tile.locked);
						}
					out.writeBool(rm.rememberWindowSize);
					out.writei(rm.editorWidth);
					out.writei(rm.editorHeight);
					out.writeBool(rm.showGrid);
					out.writeBool(rm.showObjects);
					out.writeBool(rm.showTiles);
					out.writeBool(rm.showBackgrounds);
					out.writeBool(rm.showForegrounds);
					out.writeBool(rm.showViews);
					out.writeBool(rm.deleteUnderlyingObjects);
					out.writeBool(rm.deleteUnderlyingTiles);
					out.writei(rm.currentTab);
					out.writei(rm.scrollBarX);
					out.writei(rm.scrollBarY);
					}
				}
			out.writei(lastInstanceId);
			out.writei(lastTileId);

			// GAME SETTINGS
			out.writei(600);
			out.writei(gameInfo.backgroundColor.getRGB());
			out.writeBool(gameInfo.mimicGameWindow);
			out.writeStr(gameInfo.formCaption);
			out.writei(gameInfo.left);
			out.writei(gameInfo.top);
			out.writei(gameInfo.width);
			out.writei(gameInfo.height);
			out.writeBool(gameInfo.showBorder);
			out.writeBool(gameInfo.allowResize);
			out.writeBool(gameInfo.stayOnTop);
			out.writeBool(gameInfo.pauseGame);
			out.writeStr(gameInfo.gameInfoStr);
			out.writei(500);

			out.writei(0); // "how many longints will follow it"

			out.writei(540);
			out.writei(0); // room indexes in tree order

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

	public void defragIds()
		{
		Iterator<ResourceList<?>> iter = resMap.values().iterator();
		while (iter.hasNext())
			iter.next().defragIds();
		lastInstanceId = 100000;
		lastTileId = 100000;
		for (int i = 0; i < rooms.count(); i++)
			{
			for (int j = 0; j < rooms.getList(i).noInstances(); j++)
				rooms.getList(i).getInstanceList(j).instanceId = ++lastInstanceId;
			for (int j = 0; j < rooms.getList(i).noTiles(); j++)
				rooms.getList(i).getTileList(j).tileId = ++lastTileId;
			}
		}

	public void addChangeListener(ChangeListener l)
		{
		listenerList.add(ChangeListener.class,l);
		}

	public void removeChangeListener(ChangeListener l)
		{
		listenerList.remove(ChangeListener.class,l);
		}

	protected void fireStateChanged(ChangeEvent e)
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
			if (listeners[i] == ChangeListener.class)
				{
				((ChangeListener) listeners[i + 1]).stateChanged(e);
				}
			}
		}

	protected void fireStateChanged()
		{
		// Lazily create the event:
		if (changeEvent == null) changeEvent = new ChangeEvent(this);
		fireStateChanged(changeEvent);
		}

	private class ResourceChangeListener implements ChangeListener
		{
		public void stateChanged(ChangeEvent e)
			{
			fireStateChanged(e);
			}
		}
	}
