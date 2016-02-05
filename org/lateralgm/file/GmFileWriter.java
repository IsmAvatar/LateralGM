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
import java.nio.charset.Charset;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JProgressBar;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Constants;
import org.lateralgm.resources.Extension;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Shader;
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
import org.lateralgm.resources.sub.CharacterRange.PCharacterRange;
import org.lateralgm.resources.sub.CharacterRange;
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

	public static void writeProjectFile(OutputStream os, ProjectFile f, ResNode root, int ver)
			throws IOException
		{
		f.format = ProjectFile.FormatFlavor.getVersionFlavor(ver);
		long savetime = System.currentTimeMillis();
		GmStreamEncoder out = new GmStreamEncoder(os);

		JProgressBar progressBar = LGM.getProgressDialogBar();
		progressBar.setMaximum(200);
		LGM.setProgressTitle(Messages.getString("ProgressDialog.GMK_SAVING"));

		GameSettings gs = f.gameSettings.get(0);

		LGM.setProgress(0,Messages.getString("ProgressDialog.SETTINGS"));
		if (ver >= 810)
			out.setCharset(Charset.forName("UTF-8"));
		else
			out.setCharset(Charset.defaultCharset());
		out.write4(1234321);
		out.write4(ver);
		if (ver == 530) out.write4(0);
		int gameId = gs.get(PGameSettings.GAME_ID);
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
		out.write((byte[]) gs.get(PGameSettings.GAME_GUID)); //16 bytes

		writeSettings(f,out,ver,savetime,gs);

		if (ver >= 800)
			{
			LGM.setProgress(10,Messages.getString("ProgressDialog.TRIGGERS"));
			writeTriggers(f,out,ver,gs);
			LGM.setProgress(20,Messages.getString("ProgressDialog.CONSTANTS"));
			writeConstants(f,out,ver,gs);
			}

		LGM.setProgress(30,Messages.getString("ProgressDialog.SOUNDS"));
		writeSounds(f,out,ver,gs);
		LGM.setProgress(40,Messages.getString("ProgressDialog.SPRITES"));
		writeSprites(f,out,ver,gs);
		LGM.setProgress(50,Messages.getString("ProgressDialog.BACKGROUNDS"));
		writeBackgrounds(f,out,ver,gs);
		LGM.setProgress(60,Messages.getString("ProgressDialog.PATHS"));
		writePaths(f,out,ver,gs);
		LGM.setProgress(70,Messages.getString("ProgressDialog.SCRIPTS"));
		writeScripts(f,out,ver,gs);
		LGM.setProgress(80,Messages.getString("ProgressDialog.FONTS"));
		writeFonts(f,out,ver,gs);
		LGM.setProgress(90,Messages.getString("ProgressDialog.TIMELINES"));
		writeTimelines(f,out,ver,gs);
		LGM.setProgress(100,Messages.getString("ProgressDialog.OBJECTS"));
		writeGmObjects(f,out,ver,gs);
		LGM.setProgress(110,Messages.getString("ProgressDialog.ROOMS"));
		writeRooms(f,out,ver,gs);

		out.write4(f.lastInstanceId);
		out.write4(f.lastTileId);

		if (ver >= 700)
			{
			LGM.setProgress(120,Messages.getString("ProgressDialog.INCLUDEFILES"));
			writeIncludedFiles(f,out,ver,gs);
			LGM.setProgress(130,Messages.getString("ProgressDialog.PACKAGES"));
			writePackages(f,out,ver);
			}

		LGM.setProgress(140,Messages.getString("ProgressDialog.GAMEINFORMATION"));
		writeGameInformation(f,out,ver,gs);

		LGM.setProgress(150,Messages.getString("ProgressDialog.LIBRARYCREATION"));
		//Library Creation Code
		out.write4(500);
		out.write4(0);

		LGM.setProgress(160,Messages.getString("ProgressDialog.ROOMEXECUTION"));
		//Room Execution Order
		out.write4(ver >= 700 ? 700 : 540);
		out.write4(0);

		LGM.setProgress(170,Messages.getString("ProgressDialog.FILETREE"));
		writeTree(out,root);
		out.close();
		LGM.setProgress(200,Messages.getString("ProgressDialog.FINISHED"));
		}

	public static void writeSettings(ProjectFile f, GmStreamEncoder out, int ver, long savetime, GameSettings g)
			throws IOException
		{
		ver = ver >= 810 ? 810 : ver >= 800 ? 800 : ver >= 701 ? 702 : ver;
		out.write4(ver >= 800 ? 800 : ver);
		if (ver >= 800) out.beginDeflate();
		PropertyMap<PGameSettings> p = g.properties;
		out.writeBool(p,PGameSettings.START_FULLSCREEN);
		if (ver >= 600) out.writeBool(p,PGameSettings.INTERPOLATE);
		out.writeBool(p,PGameSettings.DONT_DRAW_BORDER,PGameSettings.DISPLAY_CURSOR);
		out.write4(p,PGameSettings.SCALING);
		out.writeBool(p,PGameSettings.ALLOW_WINDOW_RESIZE,PGameSettings.ALWAYS_ON_TOP);
		out.write4(Util.getGmColor((Color) p.get(PGameSettings.COLOR_OUTSIDE_ROOM)));
		out.writeBool(p,PGameSettings.SET_RESOLUTION);
		out.write4(ProjectFile.GS_DEPTH_CODE.get(p.get(PGameSettings.COLOR_DEPTH)));
		out.write4(ProjectFile.GS_RESOL_CODE.get(p.get(PGameSettings.RESOLUTION)));
		out.write4(ProjectFile.GS_FREQ_CODE.get(p.get(PGameSettings.FREQUENCY)));
		out.writeBool(p,PGameSettings.DONT_SHOW_BUTTONS,PGameSettings.USE_SYNCHRONIZATION);
		if (ver >= 800) out.writeBool(p,PGameSettings.DISABLE_SCREENSAVERS);
		out.writeBool(p,PGameSettings.LET_F4_SWITCH_FULLSCREEN,PGameSettings.LET_F1_SHOW_GAME_INFO,
				PGameSettings.LET_ESC_END_GAME,PGameSettings.LET_F5_SAVE_F6_LOAD);
		if (ver >= 702)
			out.writeBool(p,PGameSettings.LET_F9_SCREENSHOT,PGameSettings.TREAT_CLOSE_AS_ESCAPE);
		out.write4(ProjectFile.GS_PRIORITY_CODE.get(p.get(PGameSettings.GAME_PRIORITY)));
		out.writeBool(p,PGameSettings.FREEZE_ON_LOSE_FOCUS);
		out.write4(ProjectFile.GS_PROGBAR_CODE.get(p.get(PGameSettings.LOAD_BAR_MODE)));
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
		Util.fixIcon((ICOFile) g.get(PGameSettings.GAME_ICON),ver);
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
		p.put(PGameSettings.LAST_CHANGED,ProjectFile.longTimeToGmTime(savetime));
		out.writeD(p,PGameSettings.LAST_CHANGED);
		out.writeStr(p,PGameSettings.INFORMATION);
		if (ver < 800)
			{
			out.write4(g.constants.constants.size());
			for (Constant con : g.constants.constants)
				{
				out.writeStr(con.name);
				out.writeStr(con.value);
				}
			if (ver == 542 || ver == 600)
				{
				ResourceList<Include> includes = f.resMap.getList(Include.class);
				out.write4(includes.size());
				for (Include inc : includes)
					out.writeStr(inc.filepath);
				out.write4(ProjectFile.GS_INCFOLDER_CODE.get(p.get(PGameSettings.INCLUDE_FOLDER)));
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

	public static void writeTriggers(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		if (ver < 800) return;

		out.write4(800);
		int no = f.triggers.isEmpty() ? 0 : (f.triggers.lastKey() + 1);
		out.write4(no);
		for (Integer i = 0; i < no; i++)
			{
			out.beginDeflate();
			Trigger t = f.triggers.get(i);
			if (t == null)
				{
				out.writeBool(false); // Trigger does not exist
				}
			else
				{
				out.writeBool(true); // Trigger exists
				out.write4(800);
				out.writeStr(t.name);
				out.writeStr(t.condition);
				out.write4(t.checkStep);
				out.writeStr(t.constant);
				}
			out.endDeflate();
			}
		out.writeD(gs.getLastChanged());
		}

	public static void writeConstants(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		if (ver < 800) return;

		out.write4(800);
		out.write4(gs.constants.constants.size());
		for (Constant c : gs.constants.constants)
			{
			out.writeStr(c.name);
			out.writeStr(c.value);
			}
		out.writeD(gs.getLastChanged());
		}

	public static void writeSounds(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		ver = ver >= 800 ? 800 : ver >= 600 ? 600 : 440;
		out.write4(ver == 800 ? 800 : 400);
		out.write4(f.resMap.getList(Sound.class).lastId + 1);
		for (int i = 0; i <= f.resMap.getList(Sound.class).lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Sound snd = f.resMap.getList(Sound.class).getUnsafe(i);
			out.writeBool(snd != null);
			if (snd != null)
				{
				out.writeStr(snd.getName());
				if (ver == 800) out.writeD(gs.getLastChanged());
				out.write4(ver);
				out.write4(ProjectFile.SOUND_KIND_CODE.get(snd.get(PSound.KIND)));
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
				for (PSound k : ProjectFile.SOUND_FX_FLAGS)
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

	public static void writeSprites(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		ver = ver >= 800 ? 800 : ver >= 542 ? 542 : 400;
		out.write4(ver == 800 ? 800 : 400);
		out.write4(f.resMap.getList(Sprite.class).lastId + 1);
		for (int i = 0; i <= f.resMap.getList(Sprite.class).lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Sprite spr = f.resMap.getList(Sprite.class).getUnsafe(i);
			out.writeBool(spr != null);
			if (spr != null)
				{
				out.writeStr(spr.getName());
				if (ver == 800) out.writeD(gs.getLastChanged());
				out.write4(ver);
				if (ver < 800)
					{
					out.write4(spr.subImages.getWidth());
					out.write4(spr.subImages.getHeight());
					out.write4(spr.properties,PSprite.BB_LEFT,PSprite.BB_RIGHT,PSprite.BB_BOTTOM,
							PSprite.BB_TOP);
					out.writeBool(spr.properties,PSprite.TRANSPARENT,PSprite.SMOOTH_EDGES,PSprite.PRELOAD);
					out.write4(ProjectFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)));
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
					out.write4(ProjectFile.SPRITE_MASK_CODE.get(spr.get(PSprite.SHAPE)));
					out.write4(spr.properties,PSprite.ALPHA_TOLERANCE);
					out.writeBool(spr.properties,PSprite.SEPARATE_MASK);
					out.write4(ProjectFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)));
					out.write4(spr.properties,PSprite.BB_LEFT,PSprite.BB_RIGHT,PSprite.BB_BOTTOM,
							PSprite.BB_TOP);
					}
				}
			out.endDeflate();
			}
		}

	public static void writeBackgrounds(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs)
			throws IOException
		{
		ver = ver >= 710 ? 710 : ver >= 543 ? 543 : 400;
		out.write4(ver == 710 ? 800 : 400);
		out.write4(f.resMap.getList(Background.class).lastId + 1);

		for (int i = 0; i <= f.resMap.getList(Background.class).lastId; i++)
			{
			if (ver == 710) out.beginDeflate();
			Background back = f.resMap.getList(Background.class).getUnsafe(i);
			out.writeBool(back != null);
			if (back != null)
				{
				out.writeStr(back.getName());
				if (ver == 710) out.writeD(gs.getLastChanged());
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

	public static void writePaths(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		if (ver > 800) ver = 800;
		out.write4(ver == 800 ? 800 : 420);
		out.write4(f.resMap.getList(Path.class).lastId + 1);
		for (int i = 0; i <= f.resMap.getList(Path.class).lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Path path = f.resMap.getList(Path.class).getUnsafe(i);
			out.writeBool(path != null);
			if (path != null)
				{
				out.writeStr(path.getName());
				if (ver == 800) out.writeD(gs.getLastChanged());
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

	public static void writeScripts(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		ver = ver >= 800 ? 800 : 400;
		out.write4(ver);
		out.write4(f.resMap.getList(Script.class).lastId + 1);
		for (int i = 0; i <= f.resMap.getList(Script.class).lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Script scr = f.resMap.getList(Script.class).getUnsafe(i);
			out.writeBool(scr != null);
			if (scr != null)
				{
				out.writeStr(scr.getName());
				if (ver == 800) out.writeD(gs.getLastChanged());
				out.write4(ver);
				out.writeStr(scr.properties,PScript.CODE);
				}
			out.endDeflate();
			}
		}

	public static void writeFonts(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		out.write4(ver >= 800 ? 800 : 540);
		out.write4(f.resMap.getList(Font.class).lastId + 1);
		for (int i = 0; i <= f.resMap.getList(Font.class).lastId; i++)
			{
			if (ver >= 800) out.beginDeflate();
			Font font = f.resMap.getList(Font.class).getUnsafe(i);
			out.writeBool(font != null);
			if (font != null)
				{
				out.writeStr(font.getName());
				if (ver >= 800) out.writeD(gs.getLastChanged());
				out.write4(ver >= 800 ? 800 : 540);
				out.writeStr(font.properties,PFont.FONT_NAME);
				out.write4(font.properties,PFont.SIZE);
				out.writeBool(font.properties,PFont.BOLD,PFont.ITALIC);
				int rangemin = 0, rangemax = 0;
				if (font.characterRanges.size() > 0)
					{
					CharacterRange cr = font.characterRanges.get(0);
					if (cr != null)
						{
						rangemin = cr.properties.get(PCharacterRange.RANGE_MIN);
						rangemax = cr.properties.get(PCharacterRange.RANGE_MAX);
						}
					}
				if (ver >= 810)
					{
					out.write2(rangemin);
					out.write((Integer) font.get(PFont.CHARSET));
					out.write((Integer) font.get(PFont.ANTIALIAS));
					}
				else
					out.write4(rangemin);
				out.write4(rangemax);
				}
			out.endDeflate();
			}
		}

	public static void writeTimelines(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		if (ver > 800) ver = 800;
		out.write4(ver == 800 ? 800 : 500);
		out.write4(f.resMap.getList(Timeline.class).lastId + 1);
		for (int i = 0; i <= f.resMap.getList(Timeline.class).lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Timeline time = f.resMap.getList(Timeline.class).getUnsafe(i);
			out.writeBool(time != null);
			if (time != null)
				{
				out.writeStr(time.getName());
				if (ver == 800) out.writeD(gs.getLastChanged());
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

	public static void writeGmObjects(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		if (ver > 800) ver = 800;
		out.write4(ver == 800 ? 800 : 400);
		out.write4(f.resMap.getList(GmObject.class).lastId + 1);
		for (int i = 0; i <= f.resMap.getList(GmObject.class).lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			GmObject obj = f.resMap.getList(GmObject.class).getUnsafe(i);
			out.writeBool(obj != null);
			if (obj != null)
				{
				out.writeStr(obj.getName());
				if (ver == 800) out.writeD(gs.getLastChanged());
				out.write4(430);
				out.writeId((ResourceReference<?>) obj.get(PGmObject.SPRITE));
				out.writeBool(obj.properties,PGmObject.SOLID,PGmObject.VISIBLE);
				out.write4(obj.properties,PGmObject.DEPTH);
				out.writeBool(obj.properties,PGmObject.PERSISTENT);
				out.writeId((ResourceReference<?>) obj.get(PGmObject.PARENT),-100);
				out.writeId((ResourceReference<?>) obj.get(PGmObject.MASK));
				int numMainEvents = ver == 800 ? 12 : 11;
				out.write4(numMainEvents - 1);
				for (int j = 0; j < numMainEvents; j++)
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

	public static void writeRooms(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs) throws IOException
		{
		if (ver > 800) ver = 800;
		out.write4(ver == 800 ? 800 : 420);
		out.write4(f.resMap.getList(Room.class).lastId + 1);
		for (int i = 0; i <= f.resMap.getList(Room.class).lastId; i++)
			{
			if (ver == 800) out.beginDeflate();
			Room rm = f.resMap.getList(Room.class).getUnsafe(i);
			out.writeBool(rm != null);
			if (rm != null)
				{
				out.writeStr(rm.getName());
				if (ver == 800) out.writeD(gs.getLastChanged());
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
				out.writeBool(rm.properties,PRoom.VIEWS_ENABLED);
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
					out.write4(tile.getPosition().x);
					out.write4(tile.getPosition().y);
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

	public static void writeIncludedFiles(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs)
			throws IOException
		{
		ver = ver >= 800 ? 800 : ver >= 620 ? 620 : 0;
		if (ver < 620) return;

		out.write4(ver);
		ResourceList<Include> includes = f.resMap.getList(Include.class);
		out.write4(includes.size());
		for (Include i : includes)
			{
			if (ver >= 800)
				{
				out.beginDeflate();
				out.writeD(gs.getLastChanged());
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

	public static void writePackages(ProjectFile f, GmStreamEncoder out, int ver) throws IOException
		{
		if (ver < 700) return;

		out.write4(700);
		out.write4(f.packages.size());
		for (String s : f.packages)
			out.writeStr(s);
		}

	public static void writeGameInformation(ProjectFile f, GmStreamEncoder out, int ver, GameSettings gs)
			throws IOException
		{
		ver = ver >= 800 ? 800 : /*ver >= 620 ? 620 : */ver >= 600 ? 600 : 430;
		out.write4(ver);
		if (ver == 800) out.beginDeflate();
		GameInformation g = f.gameInfo;
		PropertyMap<PGameInformation> p = g.properties;
		out.write4(Util.getGmColor((Color) p.get(PGameInformation.BACKGROUND_COLOR)));
		if (ver < 800)
			out.writeBool(p,PGameInformation.EMBED_GAME_WINDOW);
		else
			out.writeBool(!(Boolean) p.get(PGameInformation.EMBED_GAME_WINDOW));
		out.writeStr(p,PGameInformation.FORM_CAPTION);
		out.write4(p,PGameInformation.LEFT,PGameInformation.TOP,PGameInformation.WIDTH,
				PGameInformation.HEIGHT);
		out.writeBool(p,PGameInformation.SHOW_BORDER,PGameInformation.ALLOW_RESIZE,
				PGameInformation.STAY_ON_TOP,PGameInformation.PAUSE_GAME);
		if (ver >= 800) out.writeD(gs.getLastChanged());

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
			if (node.kind == Shader.class || node.kind == Include.class || node.kind == Extension.class
					|| node.kind == Constants.class)
				{
				continue;
				}
			out.write4(node.status);
			if (ProjectFile.RESOURCE_CODE.containsKey(node.kind))
				out.write4(ProjectFile.RESOURCE_CODE.get(node.kind));
			else
				out.write4(0);
			Resource<?,?> res = deRef((ResourceReference<?>) node.getRes());
			if (res != null && res instanceof InstantiableResource<?,?>)
				out.write4(((InstantiableResource<?,?>) res).getId());
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
				{
				Class<? extends Resource<?,?>> kind = Argument.getResourceKind(arg.kind);
				if (kind != null && InstantiableResource.class.isAssignableFrom(kind))
					{
					Resource<?,?> r = deRef((ResourceReference<?>) arg.getRes());
					if (r != null && r instanceof InstantiableResource<?,?>)
						out.writeStr(Integer.toString(((InstantiableResource<?,?>) r).getId()));
					else
						out.writeStr("-1");
					}
				else
					out.writeStr(arg.getVal());
				}
			out.writeBool(act.isNot());
			}
		}
	}
