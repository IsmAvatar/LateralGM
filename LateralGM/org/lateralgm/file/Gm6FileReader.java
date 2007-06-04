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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;

import org.lateralgm.components.ResNode;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Constant;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
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
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.Point;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

public final class Gm6FileReader
	{
	private Gm6FileReader()
		{
		}

	public static Gm6File readGm6File(String fileName, ResNode root) throws Gm6FormatException
		{
		Gm6File f = new Gm6File();
		GmStreamDecoder in = null;
		try
			{
			long startTime = System.currentTimeMillis();
			in = new GmStreamDecoder(fileName);
			IdStack timeids = new IdStack(); // timeline ids
			IdStack objids = new IdStack(); // object ids
			IdStack rmids = new IdStack(); // room ids
			int identifier = in.read4();
			if (identifier != 1234321)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_INVALID"),fileName,identifier)); //$NON-NLS-1$
			int ver = in.read4();
			if (ver != 600)
				{
				String msg = Messages.getString("Gm6File.ERROR_UNSUPPORTED"); //$NON-NLS-1$
				throw new Gm6FormatException(String.format(msg,ver));
				}
			readSettings(f,in);
			readSounds(f,in);
			readSprites(f,in);
			readBackgrounds(f,in);
			readPaths(f,in,rmids);
			readScripts(f,in);
			readFonts(f,in);
			readTimelines(f,in,timeids,objids,rmids);
			readGmObjects(f,in,timeids,objids,rmids);
			readRooms(f,in,rmids);

			f.lastInstanceId = in.read4();
			f.lastTileId = in.read4();
			ver = in.read4();
			if (ver != 600)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREINFO"),ver)); //$NON-NLS-1$
			int bc = in.read4();
			GameInformation gameInfo = f.gameInfo;
			if (bc >= 0) gameInfo.backgroundColor = new Color(bc);
			gameInfo.mimicGameWindow = in.readBool();
			gameInfo.formCaption = in.readStr();
			gameInfo.left = in.read4();
			gameInfo.top = in.read4();
			gameInfo.width = in.read4();
			gameInfo.height = in.read4();
			gameInfo.showBorder = in.readBool();
			gameInfo.allowResize = in.readBool();
			gameInfo.stayOnTop = in.readBool();
			gameInfo.pauseGame = in.readBool();
			gameInfo.gameInfoStr = in.readStr();
			ver = in.read4();
			if (ver != 500)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_AFTERINFO"),ver)); //$NON-NLS-1$
			int no = in.read4();
			for (int j = 0; j < no; j++)
				in.skip(in.read4());
			ver = in.read4();
			if (ver != 540)
				throw new Gm6FormatException(String.format(
						Messages.getString("Gm6File.ERROR_UNSUPPORTED_AFTERINFO2"),ver)); //$NON-NLS-1$
			in.skip(in.read4() * 4); // room indexes in tree order;
			in.readTree(root,f);
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
		return f;
		}

	private static void readSettings(Gm6File f, GmStreamDecoder in) throws IOException,
			Gm6FormatException,DataFormatException
		{
		f.gameId = in.read4();
		in.skip(16); // unknown bytes following game id
		int ver = in.read4();
		if (ver != 600)
			{
			String msg = Messages.getString("Gm6File.ERROR_UNSUPPORTED"); //$NON-NLS-1$
			throw new Gm6FormatException(String.format(msg,ver));
			}
		f.startFullscreen = in.readBool();
		f.interpolate = in.readBool();
		f.dontDrawBorder = in.readBool();
		f.displayCursor = in.readBool();
		f.scaling = in.read4();
		f.allowWindowResize = in.readBool();
		f.alwaysOnTop = in.readBool();
		f.colorOutsideRoom = in.read4();
		f.setResolution = in.readBool();
		f.colorDepth = (byte) in.read4();
		f.resolution = (byte) in.read4();
		f.frequency = (byte) in.read4();
		f.dontShowButtons = in.readBool();
		f.useSynchronization = in.readBool();
		f.letF4SwitchFullscreen = in.readBool();
		f.letF1ShowGameInfo = in.readBool();
		f.letEscEndGame = in.readBool();
		f.letF5SaveF6Load = in.readBool();
		f.gamePriority = (byte) in.read4();
		f.freezeOnLoseFocus = in.readBool();
		f.loadBarMode = (byte) in.read4();
		if (f.loadBarMode == Gm6File.LOADBAR_CUSTOM)
			{
			if (in.read4() != -1) f.backLoadBar = in.readImage();
			if (in.read4() != -1) f.frontLoadBar = in.readImage();
			}
		f.showCustomLoadImage = in.readBool();
		if (f.showCustomLoadImage) if (in.read4() != -1) f.loadingImage = in.readImage();
		f.imagePartiallyTransparent = in.readBool();
		f.loadImageAlpha = in.read4();
		f.scaleProgressBar = in.readBool();

		int length = in.read4();
		f.gameIconData = new byte[length];
		in.read(f.gameIconData,0,length);
		try
			{
			ByteArrayInputStream bais = new ByteArrayInputStream(f.gameIconData);
			f.gameIcon = (BufferedImage) new ICOFile(bais).getDescriptor(0).getImageRGB();
			}
		catch (Exception e)
			{
			// hopefully this won't happen
			e.printStackTrace();
			}

		f.displayErrors = in.readBool();
		f.writeToLog = in.readBool();
		f.abortOnError = in.readBool();
		f.treatUninitializedAs0 = in.readBool();
		f.author = in.readStr();
		f.version = in.read4();
		f.lastChanged = in.readD();
		f.information = in.readStr();
		int no = in.read4();
		for (int i = 0; i < no; i++)
			{
			Constant con = new Constant();
			f.constants.add(con);
			con.name = in.readStr();
			con.value = in.readStr();
			}
		no = in.read4();
		for (int i = 0; i < no; i++)
			{
			Include inc = new Include();
			inc.filePath = in.readStr();
			}
		f.includeFolder = in.read4();
		f.overwriteExisting = in.readBool();
		f.removeAtGameEnd = in.readBool();
		}

	private static void readSounds(Gm6File f, GmStreamDecoder in) throws IOException,
			Gm6FormatException,DataFormatException
		{
		int ver = in.read4();
		if (ver != 400)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESOUNDS"),ver)); //$NON-NLS-1$

		int noSounds = in.read4();
		for (int i = 0; i < noSounds; i++)
			{
			if (in.readBool())
				{
				Sound snd = f.sounds.add();
				snd.setName(in.readStr());
				ver = in.read4();
				if (ver != 600)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INSOUND"),i,ver)); //$NON-NLS-1$
				snd.kind = (byte) in.read4();
				snd.fileType = in.readStr();
				snd.fileName = in.readStr();
				if (in.readBool()) snd.data = in.decompress(in.read4());
				int effects = in.read4();
				snd.setEffects(effects);
				snd.volume = in.readD();
				snd.pan = in.readD();
				snd.preload = in.readBool();
				}
			else
				f.sounds.lastId++;
			}
		}

	private static void readSprites(Gm6File f, GmStreamDecoder in) throws IOException,
			Gm6FormatException,DataFormatException
		{
		int ver = in.read4();
		if (ver != 400)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESPRITES"),ver)); //$NON-NLS-1$

		int noSprites = in.read4();
		for (int i = 0; i < noSprites; i++)
			{
			if (in.readBool())
				{
				Sprite spr = f.sprites.add();
				spr.setName(in.readStr());
				ver = in.read4();
				if (ver != 542)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INSPRITE"),i,ver)); //$NON-NLS-1$
				spr.width = in.read4();
				spr.height = in.read4();
				spr.boundingBoxLeft = in.read4();
				spr.boundingBoxRight = in.read4();
				spr.boundingBoxBottom = in.read4();
				spr.boundingBoxTop = in.read4();
				spr.transparent = in.readBool();
				spr.smoothEdges = in.readBool();
				spr.preload = in.readBool();
				spr.boundingBoxMode = (byte) in.read4();
				spr.preciseCC = in.readBool();
				spr.originX = in.read4();
				spr.originY = in.read4();
				int nosub = in.read4();
				for (int j = 0; j < nosub; j++)
					{
					in.skip(4);
					spr.addSubImage(ImageIO.read(new ByteArrayInputStream(in.decompress(in.read4()))));
					}
				}
			else
				f.sprites.lastId++;
			}
		}

	private static void readBackgrounds(Gm6File f, GmStreamDecoder in) throws IOException,
			Gm6FormatException,DataFormatException
		{
		int ver = in.read4();
		if (ver != 400)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREBACKGROUNDS"),ver)); //$NON-NLS-1$

		int noBackgrounds = in.read4();
		for (int i = 0; i < noBackgrounds; i++)
			{
			if (in.readBool())
				{
				Background back = f.backgrounds.add();
				back.setName(in.readStr());
				ver = in.read4();
				if (ver != 543)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INBACKGROUND"),i,ver)); //$NON-NLS-1$
				back.width = in.read4();
				back.height = in.read4();
				back.transparent = in.readBool();
				back.smoothEdges = in.readBool();
				back.preload = in.readBool();
				back.useAsTileSet = in.readBool();
				back.tileWidth = in.read4();
				back.tileHeight = in.read4();
				back.horizOffset = in.read4();
				back.vertOffset = in.read4();
				back.horizSep = in.read4();
				back.vertSep = in.read4();
				if (in.readBool())
					{
					in.skip(4); // 0A
					ByteArrayInputStream is = new ByteArrayInputStream(in.decompress(in.read4()));
					back.backgroundImage = ImageIO.read(is);
					}
				}
			else
				f.backgrounds.lastId++;
			}
		}

	private static void readPaths(Gm6File f, GmStreamDecoder in, IdStack rmids) throws IOException,
			Gm6FormatException
		{
		int ver = in.read4();
		if (ver != 420)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREPATHS"),ver)); //$NON-NLS-1$

		int noPaths = in.read4();
		for (int i = 0; i < noPaths; i++)
			{
			if (in.readBool())
				{
				Path path = f.paths.add();
				path.setName(in.readStr());
				ver = in.read4();
				if (ver != 530)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INPATH"),i,ver)); //$NON-NLS-1$
				path.smooth = in.readBool();
				path.closed = in.readBool();
				path.precision = in.read4();
				path.backgroundRoom = rmids.get(in.read4());
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
			else
				f.paths.lastId++;
			}
		}

	private static void readScripts(Gm6File f, GmStreamDecoder in) throws IOException,
			Gm6FormatException
		{
		int ver = in.read4();
		if (ver != 400)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFORESCRIPTS"),ver)); //$NON-NLS-1$

		int noScripts = in.read4();
		for (int i = 0; i < noScripts; i++)
			{
			if (in.readBool())
				{
				Script scr = f.scripts.add();
				scr.setName(in.readStr());
				ver = in.read4();
				if (ver != 400)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INSCRIPT"),i,ver)); //$NON-NLS-1$
				scr.scriptStr = in.readStr();
				}
			else
				f.scripts.lastId++;
			}
		}

	private static void readFonts(Gm6File f, GmStreamDecoder in) throws IOException,
			Gm6FormatException
		{
		int ver = in.read4();
		if (ver != 540)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREFONTS"),ver)); //$NON-NLS-1$

		int noFonts = in.read4();
		for (int i = 0; i < noFonts; i++)
			{
			if (in.readBool())
				{
				Font font = f.fonts.add();
				font.setName(in.readStr());
				ver = in.read4();
				if (ver != 540)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INFONT"),i,ver)); //$NON-NLS-1$
				font.fontName = in.readStr();
				font.size = in.read4();
				font.bold = in.readBool();
				font.italic = in.readBool();
				font.charRangeMin = in.read4();
				font.charRangeMax = in.read4();
				}
			else
				f.fonts.lastId++;
			}
		}

	private static void readTimelines(Gm6File f, GmStreamDecoder in, IdStack timeids, IdStack objids,
			IdStack rmids) throws IOException,Gm6FormatException
		{
		int ver = in.read4();
		if (ver != 500)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFORETIMELINES"),ver)); //$NON-NLS-1$

		int noTimelines = in.read4();
		for (int i = 0; i < noTimelines; i++)
			{
			if (in.readBool())
				{
				Timeline time = f.timelines.add();
				time.setId(timeids.get(i));
				time.setName(in.readStr());
				ver = in.read4();
				if (ver != 500)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INTIMELINE"),i,ver)); //$NON-NLS-1$
				int nomoms = in.read4();
				for (int j = 0; j < nomoms; j++)
					{
					Moment mom = time.addMoment();
					mom.stepNo = in.read4();
					readActions(f,in,mom,timeids,objids,rmids);
					}
				}
			else
				f.timelines.lastId++;
			}
		}

	private static void readGmObjects(Gm6File f, GmStreamDecoder in, IdStack timeids, IdStack objids,
			IdStack rmids) throws IOException,Gm6FormatException
		{
		int ver = in.read4();
		if (ver != 400)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREOBJECTS"),ver)); //$NON-NLS-1$

		int noGmObjects = in.read4();
		for (int i = 0; i < noGmObjects; i++)
			{
			if (in.readBool())
				{
				GmObject obj = f.gmObjects.add();
				obj.setId(objids.get(i));
				obj.setName(in.readStr());
				ver = in.read4();
				if (ver != 430)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INOBJECT"),i,ver)); //$NON-NLS-1$
				Sprite temp = f.sprites.getUnsafe(in.read4());
				if (temp != null) obj.sprite = temp.getId();
				obj.solid = in.readBool();
				obj.visible = in.readBool();
				obj.depth = in.read4();
				obj.persistent = in.readBool();
				obj.parent = objids.get(in.read4());
				temp = f.sprites.getUnsafe(in.read4());
				if (temp != null) obj.mask = temp.getId();
				in.skip(4);
				for (int j = 0; j < 11; j++)
					{
					boolean done = false;
					while (!done)
						{
						int first = in.read4();
						if (first != -1)
							{
							Event ev = obj.mainEvents[j].addEvent();
							if (j == MainEvent.EV_COLLISION)
								{
								ev.other = objids.get(first);
								}
							else
								ev.id = first;
							ev.mainId = j;
							readActions(f,in,ev,timeids,objids,rmids);
							}
						else
							done = true;
						}
					}
				}
			else
				f.gmObjects.lastId++;
			}
		}

	private static void readRooms(Gm6File f, GmStreamDecoder in, IdStack rmids) throws IOException,
			Gm6FormatException
		{
		int ver = in.read4();
		if (ver != 420)
			throw new Gm6FormatException(String.format(
					Messages.getString("Gm6File.ERROR_UNSUPPORTED_BEFOREROOMS"),ver)); //$NON-NLS-1$
		// ROOMS
		int noRooms = in.read4();
		for (int i = 0; i < noRooms; i++)
			{
			if (in.readBool())
				{
				Room rm = f.rooms.add(new Room(f));
				rm.setId(rmids.get(i));
				rm.setName(in.readStr());
				ver = in.read4();
				if (ver != 541)
					throw new Gm6FormatException(String.format(
							Messages.getString("Gm6File.ERROR_UNSUPPORTED_INROOM"),i,ver)); //$NON-NLS-1$
				rm.caption = in.readStr();
				rm.width = in.read4();
				rm.height = in.read4();
				rm.snapY = in.read4();
				rm.snapX = in.read4();
				rm.isometricGrid = in.readBool();
				rm.speed = in.read4();
				rm.persistent = in.readBool();
				rm.backgroundColor = in.read4();
				rm.drawBackgroundColor = in.readBool();
				rm.creationCode = in.readStr();
				int nobackgrounds = in.read4();
				for (int j = 0; j < nobackgrounds; j++)
					{
					BackgroundDef bk = rm.backgroundDefs[j];
					bk.visible = in.readBool();
					bk.foreground = in.readBool();
					Background temp = f.backgrounds.getUnsafe(in.read4());
					if (temp != null) bk.backgroundId = temp.getId();
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
					vw.enabled = in.readBool();
					vw.viewX = in.read4();
					vw.viewY = in.read4();
					vw.viewW = in.read4();
					vw.viewH = in.read4();
					vw.portX = in.read4();
					vw.portY = in.read4();
					vw.portW = in.read4();
					vw.portH = in.read4();
					vw.hbor = in.read4();
					vw.vbor = in.read4();
					vw.hspeed = in.read4();
					vw.vspeed = in.read4();
					GmObject temp = f.gmObjects.getUnsafe(in.read4());
					if (temp != null) vw.objectFollowing = temp.getId();
					}
				int noinstances = in.read4();
				for (int j = 0; j < noinstances; j++)
					{
					Instance inst = rm.addInstance();
					inst.x = in.read4();
					inst.y = in.read4();
					GmObject temp = f.gmObjects.getUnsafe(in.read4());
					if (temp != null) inst.gmObjectId = temp.getId();
					inst.instanceId = in.read4();
					inst.creationCode = in.readStr();
					inst.locked = in.readBool();
					}
				int notiles = in.read4();
				for (int j = 0; j < notiles; j++)
					{
					Tile ti = rm.addTile();
					ti.x = in.read4();
					ti.y = in.read4();
					Background temp = f.backgrounds.getUnsafe(in.read4());
					if (temp != null) ti.backgroundId = temp.getId();
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
				rm.currentTab = in.read4();
				rm.scrollBarX = in.read4();
				rm.scrollBarY = in.read4();
				}
			else
				f.rooms.lastId++;
			}
		}

	private static void readActions(Gm6File f, GmStreamDecoder in, ActionContainer container,
			IdStack timeids, IdStack objids, IdStack rmids) throws IOException,Gm6FormatException
		{
		Resource tag = new Script();
		int ver = in.read4();
		if (ver != 400)
			{
			String msg;
			msg = Messages.getString("Gm6File.ERROR_UNSUPPORTED_INTIMELINEMOMENT"); //$NON-NLS-1$
			//TODO rethrow
			throw new Gm6FormatException(String.format(msg,-9,-10,ver));
			}
		int noacts = in.read4();
		for (int k = 0; k < noacts; k++)
			{
			Action act = container.addAction();
			in.skip(4);
			int libid = in.read4();
			int actid = in.read4();
			act.libAction = LibManager.getLibAction(libid,actid);
			//The libAction will have a null parent, among other things
			if (act.libAction == null)
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
			int[] argkinds = new int[in.read4()];
			for (int x = 0; x < argkinds.length; x++)
				argkinds[x] = in.read4();
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
					act.appliesTo = objids.get(appliesTo);
				}
			act.relative = in.readBool();
			int actualnoargs = in.read4();

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
							act.arguments[l].res = objids.get(Integer.parseInt(strval));
							break;
						case Argument.ARG_ROOM:
							act.arguments[l].res = rmids.get(Integer.parseInt(strval));
							break;
						case Argument.ARG_FONT:
							res = f.fonts.getUnsafe(Integer.parseInt(strval));
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
					in.skip(in.read4());
					}
				}
			act.not = in.readBool();
			}
		}
	}
