/*
 * Copyright (C) 2006-2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import static org.lateralgm.main.Util.deRef;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.Util;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.resources.GameInformation.PGameInformation;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GameSettings.ProgressBar;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Script.PScript;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Constant;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Trigger;
import org.lateralgm.resources.sub.View;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.View.PView;
import org.lateralgm.util.PropertyMap;

public final class GmFileWriter
	{
	private GmFileWriter()
		{
		}

	public static void writeGmFile(GmFile f, ResNode root) throws IOException
		{
		int ver = f.fileVersion;
		long savetime = System.currentTimeMillis();
		GmStreamEncoder out = null;
		out = new GmStreamEncoder(f.filename);
		out.write4(1234321);
		out.write4(ver);
		if (ver == 530) out.write4(0);
		int gameId = f.gameSettings.get(PGameSettings.GAME_ID);
		if (ver == 701)
			{
			out.write4(0); //bob
			out.write4(0); //fred
			out.write4(248); //seed
			out.write(gameId & 0xFF);
			out.setSeed(248);
			out.write3(gameId >>> 8);
			}
		else
			out.write4(gameId);
		out.write((byte[]) f.gameSettings.get(PGameSettings.DPLAY_GUID)); //16 bytes

		writeSettings(f,out,savetime);

		if (ver >= 800)
			{
			writeTriggers(f,out);
			writeConstants(f,out);
			}

		writeSounds(f,out);
		writeSprites(f,out);
		writeBackgrounds(f,out);
		writePaths(f,out);
		writeScripts(f,out);
		writeFonts(f,out);
		writeTimelines(f,out);
		writeGmObjects(f,out);
		writeRooms(f,out);

		out.write4(f.lastInstanceId);
		out.write4(f.lastTileId);

		if (ver >= 700)
			{
			writeIncludedFiles(f,out);
			writePackages(f,out);
			}

		writeGameInformation(f,out);

		//Library Creation Code
		out.write4(500);
		out.write4(0);

		//Room Execution Order
		out.write4(ver >= 700 ? 700 : 540);
		out.write4(0);

		writeTree(out,root);
		out.close();
		}

	public static void writeSettings(GmFile f, GmStreamEncoder out, long savetime) throws IOException
		{
		int ver = f.fileVersion;
		ver = ver >= 810 ? 810 : ver >= 800 ? 800 : ver >= 701 ? 702 : ver;
		out.write4(ver >= 800 ? 800 : ver);
		if (ver >= 800) out.beginDeflate();
		GameSettings g = f.gameSettings;
		PropertyMap<PGameSettings> p = g.properties;
		out.writeBool(p,PGameSettings.START_FULLSCREEN);
		if (ver >= 600) out.writeBool(p,PGameSettings.INTERPOLATE);
		out.writeBool(p,PGameSettings.DONT_DRAW_BORDER,PGameSettings.DISPLAY_CURSOR);
		out.write4(p,PGameSettings.SCALING);
		out.writeBool(p,PGameSettings.ALLOW_WINDOW_RESIZE,PGameSettings.ALWAYS_ON_TOP);
		out.write4(Util.getGmColor((Color) p.get(PGameSettings.COLOR_OUTSIDE_ROOM)));
		out.writeBool(p,PGameSettings.SET_RESOLUTION);
		out.write4(GmFile.GS_DEPTH_CODE.get(p.get(PGameSettings.COLOR_DEPTH)));
		out.write4(GmFile.GS_RESOL_CODE.get(p.get(PGameSettings.RESOLUTION)));
		out.write4(GmFile.GS_FREQ_CODE.get(p.get(PGameSettings.FREQUENCY)));
		out.writeBool(p,PGameSettings.DONT_SHOW_BUTTONS,PGameSettings.USE_SYNCHRONIZATION);
		if (ver >= 800) out.writeBool(p,PGameSettings.DISABLE_SCREENSAVERS);
		out.writeBool(p,PGameSettings.LET_F4_SWITCH_FULLSCREEN,PGameSettings.LET_F1_SHOW_GAME_INFO,
				PGameSettings.LET_ESC_END_GAME,PGameSettings.LET_F5_SAVE_F6_LOAD);
		if (ver >= 702)
			out.writeBool(p,PGameSettings.LET_F9_SCREENSHOT,PGameSettings.TREAT_CLOSE_AS_ESCAPE);
		out.write4(GmFile.GS_PRIORITY_CODE.get(p.get(PGameSettings.GAME_PRIORITY)));
		out.writeBool(p,PGameSettings.FREEZE_ON_LOSE_FOCUS);
		out.write4(GmFile.GS_PROGBAR_CODE.get(p.get(PGameSettings.LOAD_BAR_MODE)));
		if (p.get(PGameSettings.LOAD_BAR_MODE) == ProgressBar.CUSTOM)
			{
			if (p.get(PGameSettings.BACK_LOAD_BAR) != null)
				{
				out.write4(ver < 800 ? 10 : 1);
				out.writeZlibImage((BufferedImage) p.get(PGameSettings.BACK_LOAD_BAR));
				}
			else
				out.write4(ver < 800 ? -1 : 0);
			if (p.get(PGameSettings.FRONT_LOAD_BAR) != null)
				{
				out.write4(ver < 800 ? 10 : 1);
				out.writeZlibImage((BufferedImage) p.get(PGameSettings.FRONT_LOAD_BAR));
				}
			else
				out.write4(ver < 800 ? -1 : 0);
			}
		out.writeBool(p,PGameSettings.SHOW_CUSTOM_LOAD_IMAGE);
		if (p.get(PGameSettings.SHOW_CUSTOM_LOAD_IMAGE))
			{
			if (p.get(PGameSettings.LOADING_IMAGE) != null)
				{
				out.write4(ver < 800 ? 10 : 1);
				out.writeZlibImage((BufferedImage) p.get(PGameSettings.LOADING_IMAGE));
				}
			else
				out.write4(ver < 800 ? -1 : 0);
			}
		out.writeBool(p,PGameSettings.IMAGE_PARTIALLY_TRANSPARENTY);
		out.write4(p,PGameSettings.LOAD_IMAGE_ALPHA);
		out.writeBool(p,PGameSettings.SCALE_PROGRESS_BAR);

		//FIXME: GM8 icons
		Util.fixIcon((ICOFile) g.get(PGameSettings.GAME_ICON),f.fileVersion);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		((ICOFile) g.get(PGameSettings.GAME_ICON)).write(baos);
		out.write4(baos.size());
		baos.writeTo(out);

		out.writeBool(p,PGameSettings.DISPLAY_ERRORS,PGameSettings.WRITE_TO_LOG,
				PGameSettings.ABORT_ON_ERROR);
		out.write4((p.get(PGameSettings.TREAT_UNINIT_AS_0) ? 1 : 0)
				| ((Boolean) p.get(PGameSettings.ERROR_ON_ARGS) && ver >= 810 ? 2 : 0));
		out.writeStr(p,PGameSettings.AUTHOR);
		if (ver <= 600)
			{
			try
				{
				out.write4(Integer.parseInt((String) p.get(PGameSettings.VERSION)));
				}
			catch (NumberFormatException e)
				{
				out.write4(100);
				}
			}
		else
			out.writeStr(p,PGameSettings.VERSION);
		p.put(PGameSettings.LAST_CHANGED,GmFile.longTimeToGmTime(savetime));
		out.writeD(p,PGameSettings.LAST_CHANGED);
		out.writeStr(p,PGameSettings.INFORMATION);
		if (ver < 800)
			{
			out.write4(f.constants.size());
			for (Constant con : f.constants)
				{
				out.writeStr(con.name);
				out.writeStr(con.value);
				}
			if (ver == 542 || ver == 600)
				{
				out.write4(f.includes.size());
				for (Include inc : f.includes)
					out.writeStr(inc.filepath);
				out.write4(GmFile.GS_INCFOLDER_CODE.get(p.get(PGameSettings.INCLUDE_FOLDER)));
				out.writeBool(p,PGameSettings.OVERWRITE_EXISTING,PGameSettings.REMOVE_AT_GAME_END);
				}
			}
		if (ver >= 702)
			{
			out.write4(p,PGameSettings.VERSION_MAJOR,PGameSettings.VERSION_MINOR,
					PGameSettings.VERSION_RELEASE,PGameSettings.VERSION_BUILD);
			out.writeStr(p,PGameSettings.COMPANY,PGameSettings.PRODUCT,PGameSettings.COPYRIGHT,
					PGameSettings.DESCRIPTION);
			if (ver >= 800) out.writeD(g.getLastChanged());
			}

		out.endDeflate();
		}

	public static void writeTriggers(GmFile f, GmStreamEncoder out) throws IOException
		{
		if (f.fileVersion < 800) return;

		out.write4(800);
		out.write4(f.triggers.size());
		for (Trigger t : f.triggers)
			{
			out.beginDeflate();
			out.writeBool(true); //trigger exists
			out.write4(800);
			out.writeStr(t.name);
			out.writeStr(t.condition);
			out.write4(t.checkStep);
			out.writeStr(t.constant);
			out.endDeflate();
			}
		out.writeD(f.gameSettings.getLastChanged());
		}

	public static void writeConstants(GmFile f, GmStreamEncoder out) throws IOException
		{
		if (f.fileVersion < 800) return;

		out.write4(800);
		out.write4(f.constants.size());
		for (Constant c : f.constants)
			{
			out.writeStr(c.name);
			out.writeStr(c.value);
			}
		out.writeD(f.gameSettings.getLastChanged());
		}

	public static void writeSounds(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		ver = ver >= 800 ? 800 : ver >= 600 ? 600 : 440;
		out.write4(ver == 800 ? 800 : 400);
		out.write4(f.sounds.lastId + 1);
		for (int i = 0; i <= f.sounds.lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Sound snd = f.sounds.getUnsafe(i);
			out.writeBool(snd != null);
			if (snd != null)
				{
				out.writeStr(snd.getName());
				if (ver == 800) out.writeD(f.gameSettings.getLastChanged());
				out.write4(ver);
				out.write4(GmFile.SOUND_CODE.get(snd.get(PSound.KIND)));
				out.writeStr(snd.properties,PSound.FILE_TYPE,PSound.FILE_NAME);
				if (snd.data != null)
					{
					out.writeBool(true);
					if (ver == 800)
						{
						out.write4(snd.data.length);
						out.write(snd.data);
						}
					else
						out.compress(snd.data);
					}
				else
					out.writeBool(false);
				int effects = 0;
				int n = 1;
				for (PSound k : GmFile.SOUND_FX_FLAGS)
					{
					if (snd.get(k)) effects |= n;
					n <<= 1;
					}
				out.write4(effects);
				out.writeD(snd.properties,PSound.VOLUME,PSound.PAN);
				out.writeBool(snd.properties,PSound.PRELOAD);
				}
			out.endDeflate();
			}
		}

	public static void writeSprites(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		ver = ver >= 800 ? 800 : ver >= 542 ? 542 : 400;
		out.write4(ver == 800 ? 800 : 400);
		out.write4(f.sprites.lastId + 1);
		for (int i = 0; i <= f.sprites.lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Sprite spr = f.sprites.getUnsafe(i);
			out.writeBool(spr != null);
			if (spr != null)
				{
				out.writeStr(spr.getName());
				if (ver == 800) out.writeD(f.gameSettings.getLastChanged());
				out.write4(ver);
				if (ver < 800)
					{
					out.write4(spr.subImages.getWidth());
					out.write4(spr.subImages.getHeight());
					out.write4(spr.properties,PSprite.BB_LEFT,PSprite.BB_RIGHT,PSprite.BB_BOTTOM,
							PSprite.BB_TOP);
					out.writeBool(spr.properties,PSprite.TRANSPARENT,PSprite.SMOOTH_EDGES,PSprite.PRELOAD);
					out.write4(GmFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)));
					out.writeBool(spr.get(PSprite.SHAPE) == Sprite.MaskShape.PRECISE);
					}
				out.write4(spr.properties,PSprite.ORIGIN_X,PSprite.ORIGIN_Y);
				out.write4(spr.subImages.size());
				for (int j = 0; j < spr.subImages.size(); j++)
					{
					BufferedImage sub = spr.subImages.get(j);
					if (ver == 800)
						{
						out.write4(800);
						int w = sub.getWidth();
						int h = sub.getHeight();
						out.write4(w);
						out.write4(h);
						if (w != 0 && h != 0) out.writeBGRAImage(sub,(Boolean) spr.get(PSprite.TRANSPARENT));
						}
					else
						{
						out.write4(10);
						out.writeZlibImage(sub);
						}
					}
				if (ver >= 800)
					{
					out.write4(GmFile.SPRITE_MASK_CODE.get(spr.get(PSprite.SHAPE)));
					out.write4(spr.properties,PSprite.ALPHA_TOLERANCE);
					out.writeBool(spr.properties,PSprite.SEPARATE_MASK);
					out.write4(GmFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)));
					out.write4(spr.properties,PSprite.BB_LEFT,PSprite.BB_RIGHT,PSprite.BB_BOTTOM,
							PSprite.BB_TOP);
					}
				}
			out.endDeflate();
			}
		}

	public static void writeBackgrounds(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		ver = ver >= 710 ? 710 : ver >= 543 ? 543 : 400;
		out.write4(ver == 710 ? 800 : 400);
		out.write4(f.backgrounds.lastId + 1);
		for (int i = 0; i <= f.backgrounds.lastId; i++)
			{
			if (ver == 710) out.beginDeflate();
			Background back = f.backgrounds.getUnsafe(i);
			out.writeBool(back != null);
			if (back != null)
				{
				out.writeStr(back.getName());
				if (ver == 710) out.writeD(f.gameSettings.getLastChanged());
				out.write4(ver);
				if (ver < 710)
					{
					out.write4(back.getWidth());
					out.write4(back.getHeight());
					out.writeBool(back.properties,PBackground.TRANSPARENT,PBackground.SMOOTH_EDGES,
							PBackground.PRELOAD,PBackground.USE_AS_TILESET);
					}
				else
					out.writeBool(back.properties,PBackground.USE_AS_TILESET);
				out.write4(back.properties,PBackground.TILE_WIDTH,PBackground.TILE_HEIGHT,
						PBackground.H_OFFSET,PBackground.V_OFFSET,PBackground.H_SEP,PBackground.V_SEP);
				BufferedImage bi = back.getBackgroundImage();
				if (ver < 710)
					{
					if (bi != null)
						{
						out.writeBool(true);
						out.write4(10);
						out.writeZlibImage(bi);
						}
					else
						out.writeBool(false);
					}
				else
					{
					out.write4(800);
					int w = bi == null ? 0 : bi.getWidth();
					int h = bi == null ? 0 : bi.getHeight();
					out.write4(w);
					out.write4(h);
					if (w != 0 && h != 0) out.writeBGRAImage(bi,(Boolean) back.get(PBackground.TRANSPARENT));
					}
				}
			out.endDeflate();
			}
		}

	public static void writePaths(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		if (ver > 800) ver = 800;
		out.write4(ver == 800 ? 800 : 420);
		out.write4(f.paths.lastId + 1);
		for (int i = 0; i <= f.paths.lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Path path = f.paths.getUnsafe(i);
			out.writeBool(path != null);
			if (path != null)
				{
				out.writeStr(path.getName());
				if (ver == 800) out.writeD(f.gameSettings.getLastChanged());
				out.write4(530);
				out.writeBool(path.properties,PPath.SMOOTH,PPath.CLOSED);
				out.write4(path.properties,PPath.PRECISION);
				out.writeId((ResourceReference<?>) path.get(PPath.BACKGROUND_ROOM));
				out.write4(path.properties,PPath.SNAP_X,PPath.SNAP_Y);
				out.write4(path.points.size());
				for (PathPoint p : path.points)
					{
					out.writeD(p.getX());
					out.writeD(p.getY());
					out.writeD(p.getSpeed());
					}
				}
			out.endDeflate();
			}
		}

	public static void writeScripts(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		ver = ver >= 800 ? 800 : 400;
		out.write4(ver);
		out.write4(f.scripts.lastId + 1);
		for (int i = 0; i <= f.scripts.lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Script scr = f.scripts.getUnsafe(i);
			out.writeBool(scr != null);
			if (scr != null)
				{
				out.writeStr(scr.getName());
				if (ver == 800) out.writeD(f.gameSettings.getLastChanged());
				out.write4(ver);
				out.writeStr(scr.properties,PScript.CODE);
				}
			out.endDeflate();
			}
		}

	public static void writeFonts(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		out.write4(ver >= 800 ? 800 : 540);
		out.write4(f.fonts.lastId + 1);
		for (int i = 0; i <= f.fonts.lastId; i++)
			{
			if (ver >= 800) out.beginDeflate();
			Font font = f.fonts.getUnsafe(i);
			out.writeBool(font != null);
			if (font != null)
				{
				out.writeStr(font.getName());
				if (ver >= 800) out.writeD(f.gameSettings.getLastChanged());
				out.write4(ver >= 800 ? 800 : 540);
				out.writeStr(font.properties,PFont.FONT_NAME);
				out.write4(font.properties,PFont.SIZE);
				out.writeBool(font.properties,PFont.BOLD,PFont.ITALIC);
				if (ver >= 810)
					{
					out.write2((Integer) font.get(PFont.RANGE_MIN));
					out.write((Integer) font.get(PFont.CHARSET));
					out.write((Integer) font.get(PFont.ANTIALIAS));
					}
				else
					out.write4(font.properties,PFont.RANGE_MIN);
				out.write4(font.properties,PFont.RANGE_MAX);
				}
			out.endDeflate();
			}
		}

	public static void writeTimelines(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		if (ver > 800) ver = 800;
		out.write4(ver == 800 ? 800 : 500);
		out.write4(f.timelines.lastId + 1);
		for (int i = 0; i <= f.timelines.lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Timeline time = f.timelines.getUnsafe(i);
			out.writeBool(time != null);
			if (time != null)
				{
				out.writeStr(time.getName());
				if (ver == 800) out.writeD(f.gameSettings.getLastChanged());
				out.write4(500);
				out.write4(time.moments.size());
				for (Moment mom : time.moments)
					{
					out.write4(mom.stepNo);
					writeActions(out,mom);
					}
				}
			out.endDeflate();
			}
		}

	public static void writeGmObjects(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		if (ver > 800) ver = 800;
		out.write4(ver == 800 ? 800 : 400);
		out.write4(f.gmObjects.lastId + 1);
		for (int i = 0; i <= f.gmObjects.lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			GmObject obj = f.gmObjects.getUnsafe(i);
			out.writeBool(obj != null);
			if (obj != null)
				{
				out.writeStr(obj.getName());
				if (ver == 800) out.writeD(f.gameSettings.getLastChanged());
				out.write4(430);
				out.writeId((ResourceReference<?>) obj.get(PGmObject.SPRITE));
				out.writeBool(obj.properties,PGmObject.SOLID,PGmObject.VISIBLE);
				out.write4(obj.properties,PGmObject.DEPTH);
				out.writeBool(obj.properties,PGmObject.PERSISTENT);
				out.writeId((ResourceReference<?>) obj.get(PGmObject.PARENT),-100);
				out.writeId((ResourceReference<?>) obj.get(PGmObject.MASK));
				out.write4(10);
				for (int j = 0; j < 11; j++)
					{
					MainEvent me = obj.mainEvents.get(j);
					for (int k = me.events.size(); k > 0; k--)
						{
						Event ev = me.events.get(k - 1);
						if (j == MainEvent.EV_COLLISION)
							out.writeId(ev.other);
						else
							out.write4(ev.id);
						writeActions(out,ev);
						}
					out.write4(-1);
					}
				}
			out.endDeflate();
			}
		}

	public static void writeRooms(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		if (ver > 800) ver = 800;
		out.write4(ver == 800 ? 800 : 420);
		out.write4(f.rooms.lastId + 1);
		for (int i = 0; i <= f.rooms.lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Room rm = f.rooms.getUnsafe(i);
			out.writeBool(rm != null);
			if (rm != null)
				{
				out.writeStr(rm.getName());
				if (ver == 800) out.writeD(f.gameSettings.getLastChanged());
				out.write4(541);
				out.writeStr(rm.properties,PRoom.CAPTION);
				out.write4(rm.properties,PRoom.WIDTH,PRoom.HEIGHT,PRoom.SNAP_Y,PRoom.SNAP_X);
				out.writeBool(rm.properties,PRoom.ISOMETRIC);
				out.write4(rm.properties,PRoom.SPEED);
				out.writeBool(rm.properties,PRoom.PERSISTENT);
				out.write4(Util.getGmColor((Color) rm.get(PRoom.BACKGROUND_COLOR)));
				out.writeBool(rm.properties,PRoom.DRAW_BACKGROUND_COLOR);
				out.writeStr(rm.properties,PRoom.CREATION_CODE);
				out.write4(rm.backgroundDefs.size());
				for (BackgroundDef back : rm.backgroundDefs)
					{
					out.writeBool(back.properties,PBackgroundDef.VISIBLE,PBackgroundDef.FOREGROUND);
					out.writeId((ResourceReference<?>) back.properties.get(PBackgroundDef.BACKGROUND));
					out.write4(back.properties,PBackgroundDef.X,PBackgroundDef.Y);
					out.writeBool(back.properties,PBackgroundDef.TILE_HORIZ,PBackgroundDef.TILE_VERT);
					out.write4(back.properties,PBackgroundDef.H_SPEED,PBackgroundDef.V_SPEED);
					out.writeBool(back.properties,PBackgroundDef.STRETCH);
					}
				out.writeBool(rm.properties,PRoom.ENABLE_VIEWS);
				out.write4(rm.views.size());
				for (View view : rm.views)
					{
					out.writeBool(view.properties,PView.VISIBLE);
					out.write4(view.properties,PView.VIEW_X,PView.VIEW_Y,PView.VIEW_W,PView.VIEW_H,
							PView.PORT_X,PView.PORT_Y,PView.PORT_W,PView.PORT_H,PView.BORDER_H,PView.BORDER_V,
							PView.SPEED_H,PView.SPEED_V);
					out.writeId((ResourceReference<?>) view.properties.get(PView.OBJECT));
					}
				out.write4(rm.instances.size());
				for (Instance in : rm.instances)
					{
					out.write4(in.getPosition().x);
					out.write4(in.getPosition().y);
					ResourceReference<GmObject> or = in.properties.get(PInstance.OBJECT);
					out.writeId(or);
					out.write4((Integer) in.properties.get(PInstance.ID));
					out.writeStr(in.getCreationCode());
					out.writeBool(in.isLocked());
					}
				out.write4(rm.tiles.size());
				for (Tile tile : rm.tiles)
					{
					out.write4(tile.getRoomPosition().x);
					out.write4(tile.getRoomPosition().y);
					ResourceReference<Background> rb = tile.properties.get(PTile.BACKGROUND);
					out.writeId(rb);
					out.write4(tile.getBackgroundPosition().x);
					out.write4(tile.getBackgroundPosition().y);
					out.write4(tile.getSize().width);
					out.write4(tile.getSize().height);
					out.write4(tile.getDepth());
					out.write4((Integer) tile.properties.get(PTile.ID));
					out.writeBool(tile.isLocked());
					}
				out.writeBool(rm.properties,PRoom.REMEMBER_WINDOW_SIZE);
				out.write4(rm.properties,PRoom.EDITOR_WIDTH,PRoom.EDITOR_HEIGHT);
				out.writeBool(rm.properties,PRoom.SHOW_GRID,PRoom.SHOW_OBJECTS,PRoom.SHOW_TILES,
						PRoom.SHOW_BACKGROUNDS,PRoom.SHOW_FOREGROUNDS,PRoom.SHOW_VIEWS,
						PRoom.DELETE_UNDERLYING_OBJECTS,PRoom.DELETE_UNDERLYING_TILES);
				out.write4(rm.properties,PRoom.CURRENT_TAB,PRoom.SCROLL_BAR_X,PRoom.SCROLL_BAR_Y);
				}
			out.endDeflate();
			}
		}

	public static void writeIncludedFiles(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		ver = ver > 800 ? 800 : ver >= 620 ? 620 : 0;
		if (ver < 620) return;

		out.write4(ver);
		out.write4(f.includes.size());
		for (Include i : f.includes)
			{
			if (ver >= 800)
				{
				out.beginDeflate();
				out.writeD(f.gameSettings.getLastChanged());
				}
			out.write4(ver);
			out.writeStr(i.filename);
			out.writeStr(i.filepath);
			out.writeBool(i.isOriginal);
			out.write4(i.size);
			if (i.data != null)
				{
				out.writeBool(true);
				out.write4(i.data.length);
				out.write(i.data);
				}
			else
				out.writeBool(false);
			out.write4(i.export);
			out.writeStr(i.exportFolder);
			out.writeBool(i.overwriteExisting);
			out.writeBool(i.freeMemAfterExport);
			out.writeBool(i.removeAtGameEnd);
			out.endDeflate();
			}
		}

	public static void writePackages(GmFile f, GmStreamEncoder out) throws IOException
		{
		if (f.fileVersion < 700) return;

		out.write4(700);
		out.write4(f.packages.size());
		for (String s : f.packages)
			out.writeStr(s);
		}

	public static void writeGameInformation(GmFile f, GmStreamEncoder out) throws IOException
		{
		int ver = f.fileVersion;
		ver = ver >= 800 ? 800 : /*ver >= 620 ? 620 : */ver >= 600 ? 600 : 430;
		out.write4(ver);
		if (ver == 800) out.beginDeflate();
		GameInformation g = f.gameInfo;
		PropertyMap<PGameInformation> p = g.properties;
		out.write4(Util.getGmColor((Color) p.get(PGameInformation.BACKGROUND_COLOR)));
		if (ver < 800)
			out.writeBool(p,PGameInformation.MIMIC_GAME_WINDOW);
		else
			out.writeBool(!(Boolean) p.get(PGameInformation.MIMIC_GAME_WINDOW));
		out.writeStr(p,PGameInformation.FORM_CAPTION);
		out.write4(p,PGameInformation.LEFT,PGameInformation.TOP,PGameInformation.WIDTH,
				PGameInformation.HEIGHT);
		out.writeBool(p,PGameInformation.SHOW_BORDER,PGameInformation.ALLOW_RESIZE,
				PGameInformation.STAY_ON_TOP,PGameInformation.PAUSE_GAME);
		if (ver >= 800) out.writeD(f.gameSettings.getLastChanged());
		out.writeStr(p,PGameInformation.TEXT);
		out.endDeflate();
		}

	public static void writeTree(GmStreamEncoder out, ResNode root) throws IOException
		{
		Enumeration<?> e = root.preorderEnumeration();
		e.nextElement();
		while (e.hasMoreElements())
			{
			ResNode node = (ResNode) e.nextElement();
			out.write4(node.status);
			out.write4(GmFile.RESOURCE_CODE.get(node.kind));
			Resource<?,?> res = deRef((ResourceReference<?>) node.getRes());
			if (res != null)
				out.write4(res.getId());
			else
				out.write4(0);
			out.writeStr((String) node.getUserObject());
			out.write4(node.getChildCount());
			}
		}

	public static void writeActions(GmStreamEncoder out, ActionContainer container)
			throws IOException
		{
		out.write4(400);
		out.write4(container.actions.size());
		for (Action act : container.actions)
			{
			LibAction la = act.getLibAction();
			out.write4(440);
			out.write4(la.parent != null ? la.parent.id : la.parentId);
			out.write4(la.id);
			out.write4(la.actionKind);
			out.writeBool(la.allowRelative);
			out.writeBool(la.question);
			out.writeBool(la.canApplyTo);
			out.write4(la.execType);
			if (la.execType == Action.EXEC_FUNCTION)
				out.writeStr(la.execInfo);
			else
				out.write4(0);
			if (la.execType == Action.EXEC_CODE)
				out.writeStr(la.execInfo);
			else
				out.write4(0);
			List<Argument> args = act.getArguments();
			out.write4(args.size());

			out.write4(args.size());
			for (Argument arg : args)
				out.write4(arg.kind);

			ResourceReference<GmObject> at = act.getAppliesTo();
			if (at != null)
				{
				if (at == GmObject.OBJECT_OTHER)
					out.write4(-2);
				else if (at == GmObject.OBJECT_SELF)
					out.write4(-1);
				else
					out.writeId(at,-100);
				}
			else
				out.write4(-100);
			out.writeBool(act.isRelative());

			out.write4(args.size());
			for (Argument arg : args)
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
						Resource<?,?> r = deRef((ResourceReference<?>) arg.getRes());
						if (r != null)
							out.writeStr(Integer.toString(r.getId()));
						else
							out.writeStr("-1"); //$NON-NLS-1$
						break;
					default:
						out.writeStr(arg.getVal());
						break;
					}
			out.writeBool(act.isNot());
			}
		}
	}
