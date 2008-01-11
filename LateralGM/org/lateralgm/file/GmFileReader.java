/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2006, 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Stack;
import java.util.zip.DataFormatException;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibArgument;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Constant;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.Point;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

public final class GmFileReader
	{
	private GmFileReader()
		{
		}

	//Workaround for Parameter limit
	private static class Gm6FileContext
		{
		GmFile f;
		GmStreamDecoder in;
		RefList<Timeline> timeids;
		RefList<GmObject> objids;
		RefList<Room> rmids;

		Gm6FileContext(GmFile f, GmStreamDecoder in, RefList<Timeline> timeids,
				RefList<GmObject> objids, RefList<Room> rmids)
			{
			this.f = f;
			this.in = in;
			this.timeids = timeids;
			this.objids = objids;
			this.rmids = rmids;
			}
		}

	private static GmFormatException versionError(String error, String res, int ver)
		{
		return versionError(error,res,0,ver);
		}

	private static GmFormatException versionError(String error, String res, int i, int ver)
		{
		return new GmFormatException(String.format(
				Messages.getString("GmFileReader.ERROR_UNSUPPORTED"), //$NON-NLS-1$
				String.format(Messages.getString("GmFileReader." + error), //$NON-NLS-1$
						Messages.getString("LGM." + res),i),ver)); //$NON-NLS-1$
		}

	public static GmFile readGmFile(String fileName, ResNode root) throws GmFormatException
		{
		GmFile f = new GmFile();
		f.filename = fileName;
		GmStreamDecoder in = null;
		RefList<Timeline> timeids = new RefList<Timeline>(Timeline.class,f); // timeline ids
		RefList<GmObject> objids = new RefList<GmObject>(GmObject.class,f); // object ids
		RefList<Room> rmids = new RefList<Room>(Room.class,f); // room id
		try
			{
			long startTime = System.currentTimeMillis();
			in = new GmStreamDecoder(fileName);
			Gm6FileContext c = new Gm6FileContext(f,in,timeids,objids,rmids);
			int identifier = in.read4();
			if (identifier != 1234321)
				throw new GmFormatException(String.format(
						Messages.getString("GmFileReader.ERROR_INVALID"),fileName,identifier)); //$NON-NLS-1$
			int ver = in.read4();
			if (ver != 530 && ver != 600 && ver != 701)
				{
				String msg = Messages.getString("GmFileReader.ERROR_UNSUPPORTED"); //$NON-NLS-1$
				throw new GmFormatException(String.format(msg,"",ver)); //$NON-NLS-1$
				}
			if (ver == 530) in.skip(4); //reserved 0
			if (ver == 701)
				{
				int s1 = in.read4();
				int s2 = in.read4();
				in.skip(s1 * 4);
				in.setSeed(in.read4());
				in.skip(s2 * 4);
				}
			readSettings(c);
			readSounds(c);
			readSprites(c);
			readBackgrounds(c);
			readPaths(c);
			readScripts(c);
			readFonts(c);
			readTimelines(c);
			readGmObjects(c);
			readRooms(c);

			f.lastInstanceId = in.read4();
			f.lastTileId = in.read4();
			ver = in.read4();
			if (ver != 430 && ver != 600 && ver != 620) throw versionError("BEFORE","GAMEINFO",ver); //$NON-NLS-1$ //$NON-NLS-2$
			if (ver != 620)
				readGameInformation(c,ver);
			else
				{
				int noIncludes = in.read4();
				for (int i = 0; i < noIncludes; i++)
					{
					ver = in.read4();
					if (ver != 620)
						throw new GmFormatException(String.format(
								Messages.getString("GmFileReader.ERROR_UNSUPPORTED"), //$NON-NLS-1$
								Messages.getString("GmFileReader.INGM7INCLUDES"),ver)); //$NON-NLS-1$
					Include inc = new Include();
					in.skip(in.read4()); //Filename
					inc.filePath = in.readStr();
					in.skip(4); //orig file chosen
					in.skip(4); //orig file size
					if (in.readBool()) in.skip(in.read4()); //Store in editable
					in.skip(4); //export
					in.skip(in.read4()); //folder to export to
					in.skip(12); //overwrite if exists, free mem, remove at game end
					f.gameSettings.includes.add(inc);
					}
				ver = in.read4();
				if (ver != 700) throw versionError("BEFORE","EXTENSIONS",ver); //$NON-NLS-1$ //$NON-NLS-2$
				int noPackages = in.read4();
				for (int i = 0; i < noPackages; i++)
					{
					in.skip(in.read4()); //Package name
					}
				ver = in.read4();
				if (ver != 600) throw versionError("BEFORE","GAMEINFO",ver); //$NON-NLS-1$ //$NON-NLS-2$
				readGameInformation(c,620);
				}
			ver = in.read4();
			if (ver != 500)
				throw new GmFormatException(String.format(
						Messages.getString("GmFileReader.ERROR_UNSUPPORTED"), //$NON-NLS-1$
						Messages.getString("GmFileReader.AFTERINFO"),ver)); //$NON-NLS-1$
			int no = in.read4(); //Library Creation Code
			for (int j = 0; j < no; j++)
				in.skip(in.read4());
			ver = in.read4();
			if (ver != 500 && ver != 540 && ver != 700)
				throw new GmFormatException(String.format(
						Messages.getString("GmFileReader.ERROR_UNSUPPORTED"), //$NON-NLS-1$
						Messages.getString("GmFileReader.AFTERINFO2"),ver)); //$NON-NLS-1$
			in.skip(in.read4() * 4); //Room Execution Order
			readTree(c,root,ver);
			System.out.printf(Messages.getString("GmFileReader.LOADTIME"), //$NON-NLS-1$
					System.currentTimeMillis() - startTime);
			System.out.println();
			}
		catch (Exception ex)
			{
			ex.printStackTrace();
			// throw new GmFormatException(ex.getMessage());
			}
		finally
			{
			try
				{
				if (in != null)
					{
					in.close();
					in = null;
					}
				}
			catch (IOException ex)
				{
				String key = Messages.getString("GmFileReader.ERROR_CLOSEFAILED"); //$NON-NLS-1$
				throw new GmFormatException(key);
				}
			}
		return f;
		}

	private static void readSettings(Gm6FileContext c) throws IOException,GmFormatException,
			DataFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;
		GameSettings g = f.gameSettings;

		g.gameId = in.read4();
		in.skip(16); // unknown bytes following game id
		int ver = in.read4();
		if (ver != 530 && ver != 542 && ver != 600 && ver != 702)
			{
			String msg = Messages.getString("GmFileReader.ERROR_UNSUPPORTED"); //$NON-NLS-1$
			throw new GmFormatException(String.format(msg,"",ver)); //$NON-NLS-1$
			}
		g.startFullscreen = in.readBool();
		if (ver > 542) g.interpolate = in.readBool();
		g.dontDrawBorder = in.readBool();
		g.displayCursor = in.readBool();
		g.scaling = in.read4();
		if (ver == 530)
			in.skip(8); //"fullscreen scale" & "only scale w/ hardware support" 
		else
			{
			g.allowWindowResize = in.readBool();
			g.alwaysOnTop = in.readBool();
			g.colorOutsideRoom = Util.convertGmColor(in.read4());
			}
		g.setResolution = in.readBool();
		if (ver == 530)
			{
			in.skip(8); //Color Depth, Exclusive Graphics
			byte b = (byte) in.read4();
			if (b == 6)
				g.resolution = 0;
			else if (b == 5)
				g.resolution = 1;
			else
				g.resolution = (byte) (b - 2);
			b = (byte) in.read4();
			g.frequency = (b == 4) ? 0 : (byte) (b + 1);
			in.skip(8); //vertical blank, caption in fullscreen
			}
		else
			{
			g.colorDepth = (byte) in.read4();
			g.resolution = (byte) in.read4();
			g.frequency = (byte) in.read4();
			}
		g.dontShowButtons = in.readBool();
		if (ver > 530) g.useSynchronization = in.readBool();
		g.letF4SwitchFullscreen = in.readBool();
		g.letF1ShowGameInfo = in.readBool();
		g.letEscEndGame = in.readBool();
		g.letF5SaveF6Load = in.readBool();
		if (ver == 530) in.skip(8); //unknown bytes, both 0
		if (ver == 702) in.skip(8); //F9 screenshot, Treat close as esc
		g.gamePriority = (byte) in.read4();
		g.freezeOnLoseFocus = in.readBool();
		g.loadBarMode = (byte) in.read4();
		if (g.loadBarMode == GameSettings.LOADBAR_CUSTOM)
			{
			if (in.read4() != -1) g.backLoadBar = in.readImage();
			if (in.read4() != -1) g.frontLoadBar = in.readImage();
			}
		g.showCustomLoadImage = in.readBool();
		if (g.showCustomLoadImage) if (in.read4() != -1) g.loadingImage = in.readImage();
		g.imagePartiallyTransparent = in.readBool();
		g.loadImageAlpha = in.read4();
		g.scaleProgressBar = in.readBool();

		int length = in.read4();
		g.gameIconData = new byte[length];
		in.read(g.gameIconData,0,length);
		try
			{
			ByteArrayInputStream bais = new ByteArrayInputStream(g.gameIconData);
			g.gameIcon = (BufferedImage) new ICOFile(bais).getDescriptor(0).getImageRGB();
			}
		catch (Exception e)
			{
			// hopefully this won't happen
			e.printStackTrace();
			}

		g.displayErrors = in.readBool();
		g.writeToLog = in.readBool();
		g.abortOnError = in.readBool();
		g.treatUninitializedAs0 = in.readBool();
		g.author = in.readStr();
		if (ver > 600)
			g.version = in.readStr();
		else
			g.version = Integer.toString(in.read4());
		g.lastChanged = in.readD();
		g.information = in.readStr();
		int no = in.read4();
		for (int i = 0; i < no; i++)
			{
			Constant con = new Constant();
			g.constants.add(con);
			con.name = in.readStr();
			con.value = in.readStr();
			}
		if (ver > 600)
			{
			in.skip(4); //Major
			in.skip(4); //Minor
			in.skip(4); //Release
			in.skip(4); //Build
			in.skip(in.read4()); //Company
			in.skip(in.read4()); //Product
			in.skip(in.read4()); //Copyright
			in.skip(in.read4()); //Description
			}
		else if (ver > 530)
			{
			no = in.read4();
			for (int i = 0; i < no; i++)
				{
				Include inc = new Include();
				g.includes.add(inc);
				inc.filePath = in.readStr();
				}
			g.includeFolder = in.read4();
			g.overwriteExisting = in.readBool();
			g.removeAtGameEnd = in.readBool();
			}
		}

	private static void readSounds(Gm6FileContext c) throws IOException,GmFormatException,
			DataFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		int ver = in.read4();
		if (ver != 400) throw versionError("BEFORE","SOUNDS",ver); //$NON-NLS-1$ //$NON-NLS-2$

		int noSounds = in.read4();
		for (int i = 0; i < noSounds; i++)
			{
			if (!in.readBool())
				{
				f.sounds.lastId++;
				continue;
				}
			Sound snd = f.sounds.add();
			snd.setName(in.readStr());
			ver = in.read4();
			if (ver != 440 && ver != 600) throw versionError("IN","SOUNDS",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			int kind53 = -1;
			if (ver == 440)
				kind53 = in.read4(); //kind (wav, mp3, etc)
			else
				snd.kind = (byte) in.read4(); //normal, background, etc
			snd.fileType = in.readStr();
			if (ver == 440)
				{
				//-1 = no sound
				if (kind53 != -1) snd.data = in.decompress(in.read4());
				in.skip(8);
				snd.preload = !in.readBool();
				}
			else
				{
				snd.fileName = in.readStr();
				if (in.readBool()) snd.data = in.decompress(in.read4());
				int effects = in.read4();
				snd.setEffects(effects);
				snd.volume = in.readD();
				snd.pan = in.readD();
				snd.preload = in.readBool();
				}
			}
		}

	private static void readSprites(Gm6FileContext c) throws IOException,GmFormatException,
			DataFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		int ver = in.read4();
		if (ver != 400) throw versionError("BEFORE","SPRITES",ver); //$NON-NLS-1$ //$NON-NLS-2$

		int noSprites = in.read4();
		for (int i = 0; i < noSprites; i++)
			{
			if (!in.readBool())
				{
				f.sprites.lastId++;
				continue;
				}
			Sprite spr = f.sprites.add();
			spr.setName(in.readStr());
			ver = in.read4();
			if (ver != 400 && ver != 542) throw versionError("IN","SPRITES",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			spr.width = in.read4();
			spr.height = in.read4();
			spr.boundingBoxLeft = in.read4();
			spr.boundingBoxRight = in.read4();
			spr.boundingBoxBottom = in.read4();
			spr.boundingBoxTop = in.read4();
			spr.transparent = in.readBool();
			if (ver > 400)
				{
				spr.smoothEdges = in.readBool();
				spr.preload = in.readBool();
				}
			spr.boundingBoxMode = (byte) in.read4();
			spr.preciseCC = in.readBool();
			if (ver == 400)
				{
				in.skip(4); //use video memory
				spr.preload = !in.readBool();
				}
			spr.originX = in.read4();
			spr.originY = in.read4();
			int nosub = in.read4();
			for (int j = 0; j < nosub; j++)
				{
				if (in.read4() == -1) continue;
				spr.addSubImage(in.readImage(spr.width,spr.height));
				}
			}
		}

	private static void readBackgrounds(Gm6FileContext c) throws IOException,GmFormatException,
			DataFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		int ver = in.read4();
		if (ver != 400) throw versionError("BEFORE","BACKGROUNDS",ver); //$NON-NLS-1$ //$NON-NLS-2$

		int noBackgrounds = in.read4();
		for (int i = 0; i < noBackgrounds; i++)
			{
			if (!in.readBool())
				{
				f.backgrounds.lastId++;
				continue;
				}
			Background back = f.backgrounds.add();
			back.setName(in.readStr());
			ver = in.read4();
			if (ver != 400 && ver != 543) throw versionError("IN","BACKGROUNDS",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			back.width = in.read4();
			back.height = in.read4();
			back.transparent = in.readBool();
			if (ver > 400)
				{
				back.smoothEdges = in.readBool();
				back.preload = in.readBool();
				back.useAsTileSet = in.readBool();
				back.tileWidth = in.read4();
				back.tileHeight = in.read4();
				back.horizOffset = in.read4();
				back.vertOffset = in.read4();
				back.horizSep = in.read4();
				back.vertSep = in.read4();
				}
			else
				{
				in.skip(4); //use video memory
				back.preload = !in.readBool();
				}
			if (in.readBool())
				{
				if (in.read4() == -1) continue;
				back.backgroundImage = in.readImage(back.width,back.height);
				}
			}
		}

	private static void readPaths(Gm6FileContext c) throws IOException,GmFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		int ver = in.read4();
		if (ver != 420) throw versionError("BEFORE","PATHS",ver); //$NON-NLS-1$ //$NON-NLS-2$

		int noPaths = in.read4();
		for (int i = 0; i < noPaths; i++)
			{
			if (!in.readBool())
				{
				f.paths.lastId++;
				continue;
				}
			Path path = f.paths.add();
			path.setName(in.readStr());
			ver = in.read4();
			if (ver != 530) throw versionError("IN","PATHS",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			path.smooth = in.readBool();
			path.closed = in.readBool();
			path.precision = in.read4();
			path.backgroundRoom = c.rmids.get(in.read4());
			path.snapX = in.read4();
			path.snapY = in.read4();
			int nopoints = in.read4();
			for (int j = 0; j < nopoints; j++)
				{
				Point point = path.addPoint();
				point.x = (int) in.readD();
				point.y = (int) in.readD();
				point.speed = (int) in.readD();
				}
			}
		}

	private static void readScripts(Gm6FileContext c) throws IOException,GmFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		int ver = in.read4();
		if (ver != 400) throw versionError("BEFORE","SCRIPTS",ver); //$NON-NLS-1$ //$NON-NLS-2$

		int noScripts = in.read4();
		for (int i = 0; i < noScripts; i++)
			{
			if (!in.readBool())
				{
				f.scripts.lastId++;
				continue;
				}
			Script scr = f.scripts.add();
			scr.setName(in.readStr());
			ver = in.read4();
			if (ver != 400) throw versionError("IN","SCRIPTS",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			scr.scriptStr = in.readStr();
			}
		}

	private static void readFonts(Gm6FileContext c) throws IOException,GmFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;
		GameSettings g = f.gameSettings;

		int ver = in.read4();
		if (ver != 440 && ver != 540) throw versionError("BEFORE","FONTS",ver); //$NON-NLS-1$ //$NON-NLS-2$

		if (ver == 440) //data files
			{
			int noDataFiles = in.read4();
			for (int i = 0; i < noDataFiles; i++)
				{
				if (!in.readBool()) continue;
				in.skip(in.read4());
				if (in.read4() != 440)
					throw new GmFormatException(String.format(
							Messages.getString("GmFileReader.ERROR_UNSUPPORTED"), //$NON-NLS-1$
							Messages.getString("GmFileReader.INDATAFILES"),ver)); //$NON-NLS-1$
				Include inc = new Include();
				g.includes.add(inc);
				inc.filePath = in.readStr();
				if (in.readBool()) in.skip(in.read4());
				in.skip(16);
				}
			return;
			}

		int noFonts = in.read4();
		for (int i = 0; i < noFonts; i++)
			{
			if (!in.readBool())
				{
				f.fonts.lastId++;
				continue;
				}
			Font font = f.fonts.add();
			font.setName(in.readStr());
			ver = in.read4();
			if (ver != 540) throw versionError("IN","FONTS",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			font.fontName = in.readStr();
			font.size = in.read4();
			font.bold = in.readBool();
			font.italic = in.readBool();
			font.charRangeMin = in.read4();
			font.charRangeMax = in.read4();
			}
		}

	private static void readTimelines(Gm6FileContext c) throws IOException,GmFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		int ver = in.read4();
		if (ver != 500) throw versionError("BEFORE","TIMELINES",ver); //$NON-NLS-1$ //$NON-NLS-2$

		int noTimelines = in.read4();
		for (int i = 0; i < noTimelines; i++)
			{
			if (!in.readBool())
				{
				f.timelines.lastId++;
				continue;
				}
			WeakReference<Timeline> r = c.timeids.get(i);
			Timeline time = r.get();
			f.timelines.add(time);
			time.setName(in.readStr());
			ver = in.read4();
			if (ver != 500) throw versionError("IN","TIMELINES",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			int nomoms = in.read4();
			for (int j = 0; j < nomoms; j++)
				{
				Moment mom = time.addMoment();
				mom.stepNo = in.read4();
				readActions(c,mom,"INTIMELINEACTION",i,mom.stepNo); //$NON-NLS-1$
				}
			}
		}

	private static void readGmObjects(Gm6FileContext c) throws IOException,GmFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		int ver = in.read4();
		if (ver != 400) throw versionError("BEFORE","OBJECTS",ver); //$NON-NLS-1$ //$NON-NLS-2$

		int noGmObjects = in.read4();
		for (int i = 0; i < noGmObjects; i++)
			{
			if (!in.readBool())
				{
				f.gmObjects.lastId++;
				continue;
				}
			WeakReference<GmObject> r = c.objids.get(i);
			GmObject obj = r.get();
			f.gmObjects.add(obj);
			obj.setName(in.readStr());
			ver = in.read4();
			if (ver != 430) throw versionError("IN","OBJECTS",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			Sprite temp = f.sprites.getUnsafe(in.read4());
			if (temp != null) obj.sprite = new WeakReference<Sprite>(temp);
			obj.solid = in.readBool();
			obj.visible = in.readBool();
			obj.depth = in.read4();
			obj.persistent = in.readBool();
			obj.parent = c.objids.get(in.read4());
			temp = f.sprites.getUnsafe(in.read4());
			if (temp != null) obj.mask = new WeakReference<Sprite>(temp);
			in.skip(4);
			for (int j = 0; j < 11; j++)
				{
				boolean done = false;
				while (!done)
					{
					int first = in.read4();
					if (first != -1)
						{
						Event ev = new Event();
						obj.mainEvents[j].events.add(0,ev);
						if (j == MainEvent.EV_COLLISION)
							{
							ev.other = c.objids.get(first);
							}
						else
							ev.id = first;
						ev.mainId = j;
						readActions(c,ev,"INOBJECTACTION",i,j * 1000 + ev.id); //$NON-NLS-1$
						}
					else
						done = true;
					}
				}
			}
		}

	private static void readRooms(Gm6FileContext c) throws IOException,GmFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		int ver = in.read4();
		if (ver != 420) throw versionError("BEFORE","ROOMS",ver); //$NON-NLS-1$ //$NON-NLS-2$

		int noRooms = in.read4();
		for (int i = 0; i < noRooms; i++)
			{
			if (!in.readBool())
				{
				f.rooms.lastId++;
				continue;
				}
			WeakReference<Room> r = c.rmids.get(i);
			Room rm = r.get();
			f.rooms.add(rm);
			rm.setName(in.readStr());
			ver = in.read4();
			if (ver != 520 && ver != 541) throw versionError("IN","ROOMS",i,ver); //$NON-NLS-1$ //$NON-NLS-2$
			rm.caption = in.readStr();
			rm.width = in.read4();
			rm.height = in.read4();
			rm.snapY = in.read4();
			rm.snapX = in.read4();
			rm.isometricGrid = in.readBool();
			rm.speed = in.read4();
			rm.persistent = in.readBool();
			rm.backgroundColor = Util.convertGmColor(in.read4());
			rm.drawBackgroundColor = in.readBool();
			rm.creationCode = in.readStr();
			int nobackgrounds = in.read4();
			for (int j = 0; j < nobackgrounds; j++)
				{
				BackgroundDef bk = rm.backgroundDefs[j];
				bk.visible = in.readBool();
				bk.foreground = in.readBool();
				Background temp = f.backgrounds.getUnsafe(in.read4());
				if (temp != null) bk.backgroundId = new WeakReference<Background>(temp);
				bk.x = in.read4();
				bk.y = in.read4();
				bk.tileHoriz = in.readBool();
				bk.tileVert = in.readBool();
				bk.horizSpeed = in.read4();
				bk.vertSpeed = in.read4();
				bk.stretch = in.readBool();
				}
			rm.enableViews = in.readBool();
			int noviews = in.read4();
			for (int j = 0; j < noviews; j++)
				{
				View vw = rm.views[j];
				vw.visible = in.readBool();
				vw.viewX = in.read4();
				vw.viewY = in.read4();
				vw.viewW = in.read4();
				vw.viewH = in.read4();
				vw.portX = in.read4();
				vw.portY = in.read4();
				if (ver > 520)
					{
					vw.portW = in.read4();
					vw.portH = in.read4();
					}
				vw.hbor = in.read4();
				vw.vbor = in.read4();
				vw.hspeed = in.read4();
				vw.vspeed = in.read4();
				GmObject temp = f.gmObjects.getUnsafe(in.read4());
				if (temp != null) vw.objectFollowing = new WeakReference<GmObject>(temp);
				}
			int noinstances = in.read4();
			for (int j = 0; j < noinstances; j++)
				{
				Instance inst = rm.addInstance();
				inst.setX(in.read4());
				inst.setY(in.read4());
				GmObject temp = f.gmObjects.getUnsafe(in.read4());
				if (temp != null) inst.gmObjectId = new WeakReference<GmObject>(temp);
				inst.instanceId = in.read4();
				inst.setCreationCode(in.readStr());
				inst.locked = in.readBool();
				}
			int notiles = in.read4();
			for (int j = 0; j < notiles; j++)
				{
				Tile ti = rm.addTile();
				ti.x = in.read4();
				ti.y = in.read4();
				Background temp = f.backgrounds.getUnsafe(in.read4());
				if (temp != null) ti.backgroundId = new WeakReference<Background>(temp);
				ti.tileX = in.read4();
				ti.tileY = in.read4();
				ti.width = in.read4();
				ti.height = in.read4();
				ti.depth = in.read4();
				ti.tileId = in.read4();
				ti.locked = in.readBool();
				}
			rm.rememberWindowSize = in.readBool();
			rm.editorWidth = in.read4();
			rm.editorHeight = in.read4();
			rm.showGrid = in.readBool();
			rm.showObjects = in.readBool();
			rm.showTiles = in.readBool();
			rm.showBackgrounds = in.readBool();
			rm.showForegrounds = in.readBool();
			rm.showViews = in.readBool();
			rm.deleteUnderlyingObjects = in.readBool();
			rm.deleteUnderlyingTiles = in.readBool();
			if (ver == 520) in.skip(6 * 4); //tile info
			rm.currentTab = in.read4();
			rm.scrollBarX = in.read4();
			rm.scrollBarY = in.read4();
			}
		}

	private static void readGameInformation(Gm6FileContext c, int ver) throws IOException,
			GmFormatException
		{
		GmStreamDecoder in = c.in;
		GameInformation gameInfo = c.f.gameInfo;
		int bc = in.read4();
		if (bc >= 0) gameInfo.backgroundColor = Util.convertGmColor(bc);
		gameInfo.mimicGameWindow = in.readBool();
		if (ver > 430)
			{
			gameInfo.formCaption = in.readStr();
			gameInfo.left = in.read4();
			gameInfo.top = in.read4();
			gameInfo.width = in.read4();
			gameInfo.height = in.read4();
			gameInfo.showBorder = in.readBool();
			gameInfo.allowResize = in.readBool();
			gameInfo.stayOnTop = in.readBool();
			gameInfo.pauseGame = in.readBool();
			}
		gameInfo.gameInfoStr = in.readStr();
		}

	private static void readTree(Gm6FileContext c, ResNode root, int ver) throws IOException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		Stack<ResNode> path = new Stack<ResNode>();
		Stack<Integer> left = new Stack<Integer>();
		path.push(root);
		int rootnodes = (ver > 540) ? 12 : 11;
		while (rootnodes-- > 0)
			{
			byte status = (byte) in.read4();
			byte type = (byte) in.read4();
			int ind = in.read4();
			String name = in.readStr();
			ResNode node = new ResNode(name,status,type);
			if (ver == 500 && status == ResNode.STATUS_PRIMARY && type == Resource.FONT)
				path.peek().addChild(Messages.getString("LGM.FONTS"),status,type); //$NON-NLS-1$
			else
				path.peek().add(node);
			if (status == ResNode.STATUS_SECONDARY && type != Resource.GAMEINFO
					&& type != Resource.GAMESETTINGS && type != Resource.EXTENSIONS
					&& (ver != 500 || type != Resource.FONT))
				{
				node.setRes(new WeakReference<Resource<?>>(f.getList(node.kind).getUnsafe(ind)));
				// GM actually ignores the name given in the tree data
				node.setUserObject(f.getList(node.kind).getUnsafe(ind).getName());
				}
			int contents = in.read4();
			if (contents > 0)
				{
				left.push(new Integer(rootnodes));
				rootnodes = contents;
				path.push(node);
				}
			while (rootnodes == 0 && !left.isEmpty())
				{
				rootnodes = left.pop().intValue();
				path.pop();
				}
			}
		}

	private static void readActions(Gm6FileContext c, ActionContainer container, String key,
			int format1, int format2) throws IOException,GmFormatException
		{
		GmFile f = c.f;
		GmStreamDecoder in = c.in;

		Resource<?> tag = new Script();
		int ver = in.read4();
		if (ver != 400)
			{
			throw new GmFormatException(String.format(
					Messages.getString("GmFileReader.ERROR_UNSUPPORTED"), //$NON-NLS-1$
					Messages.getString("GmFileReader." + key),format1,format2,ver)); //$NON-NLS-1$
			//		String msg = Messages.getString("GmFileReader." + key)
			//		+ Messages.getString("GmFileReader.ERROR_UNSUPPORTED");
			//			throw new GmFormatException(String.format(msg,format1,format2,ver));
			}
		int noacts = in.read4();
		for (int k = 0; k < noacts; k++)
			{
			Action act = container.addAction();
			in.skip(4);
			int libid = in.read4();
			int actid = in.read4();
			act.libAction = LibManager.getLibAction(libid,actid);
			boolean unknownLib = act.libAction == null;
			//The libAction will have a null parent, among other things
			if (unknownLib)
				{
				act.libAction = new LibAction();
				act.libAction.id = actid;
				act.libAction.parentId = libid;
				act.libAction.actionKind = (byte) in.read4();
				act.libAction.allowRelative = in.readBool();
				act.libAction.question = in.readBool();
				act.libAction.canApplyTo = in.readBool();
				act.libAction.execType = (byte) in.read4();
				if (act.libAction.execType == Action.EXEC_FUNCTION)
					act.libAction.execInfo = in.readStr();
				else
					in.skip(in.read4());
				if (act.libAction.execType == Action.EXEC_CODE)
					act.libAction.execInfo = in.readStr();
				else
					in.skip(in.read4());
				}
			else
				{
				in.skip(20);
				in.skip(in.read4());
				in.skip(in.read4());
				}
			act.arguments = new Argument[in.read4()];
			byte[] argkinds = new byte[in.read4()];
			for (int x = 0; x < argkinds.length; x++)
				argkinds[x] = (byte) in.read4();
			if (unknownLib)
				{
				act.libAction.libArguments = new LibArgument[argkinds.length];
				for (int x = 0; x < argkinds.length; x++)
					{
					act.libAction.libArguments[x] = new LibArgument();
					act.libAction.libArguments[x].kind = argkinds[x];
					}
				}
			int appliesTo = in.read4();
			switch (appliesTo)
				{
				case -1:
					act.appliesTo = GmObject.OBJECT_SELF;
					break;
				case -2:
					act.appliesTo = GmObject.OBJECT_OTHER;
					break;
				default:
					act.appliesTo = c.objids.get(appliesTo);
				}
			act.relative = in.readBool();
			int actualnoargs = in.read4();

			for (int l = 0; l < actualnoargs; l++)
				{
				if (l >= act.arguments.length)
					{
					in.skip(in.read4());
					continue;
					}
				act.arguments[l] = new Argument();
				act.arguments[l].kind = argkinds[l];

				String strval = in.readStr();
				Resource<?> res = tag;
				switch (argkinds[l])
					{
					case Argument.ARG_SPRITE:
						res = f.sprites.getUnsafe(Integer.parseInt(strval));
						break;
					case Argument.ARG_SOUND:
						res = f.sounds.getUnsafe(Integer.parseInt(strval));
						break;
					case Argument.ARG_BACKGROUND:
						res = f.backgrounds.getUnsafe(Integer.parseInt(strval));
						break;
					case Argument.ARG_PATH:
						res = f.paths.getUnsafe(Integer.parseInt(strval));
						break;
					case Argument.ARG_SCRIPT:
						res = f.scripts.getUnsafe(Integer.parseInt(strval));
						break;
					case Argument.ARG_GMOBJECT:
						act.arguments[l].res = c.objids.get(Integer.parseInt(strval));
						break;
					case Argument.ARG_ROOM:
						act.arguments[l].res = c.rmids.get(Integer.parseInt(strval));
						break;
					case Argument.ARG_FONT:
						res = f.fonts.getUnsafe(Integer.parseInt(strval));
						break;
					case Argument.ARG_TIMELINE:
						act.arguments[l].res = c.timeids.get(Integer.parseInt(strval));
						break;
					default:
						act.arguments[l].val = strval;
						break;
					}
				if (res != null && res != tag)
					{
					act.arguments[l].res = new WeakReference<Resource<?>>(res);
					}
				}
			act.not = in.readBool();
			}
		}
	}
