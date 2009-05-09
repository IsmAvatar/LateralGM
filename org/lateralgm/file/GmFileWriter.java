/*
 * Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2006, 2007, 2008, 2009 IsmAvatar <IsmAvatar@gmail.com>
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
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.Util;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
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
import org.lateralgm.resources.sub.View;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.View.PView;

public final class GmFileWriter
	{
	private GmFileWriter()
		{
		}

	public static void writeGmFile(GmFile f, ResNode root) throws IOException
		{
		long savetime = System.currentTimeMillis();
		GmStreamEncoder out = null;
		out = new GmStreamEncoder(f.filename);
		out.write4(1234321);
		out.write4(600);

		writeSettings(f,out,savetime);
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

		// GAME INFO SETTINGS
		out.write4(600);
		out.write4(Util.getGmColor(f.gameInfo.backgroundColor));
		out.writeBool(f.gameInfo.mimicGameWindow);
		out.writeStr(f.gameInfo.formCaption);
		out.write4(f.gameInfo.left);
		out.write4(f.gameInfo.top);
		out.write4(f.gameInfo.width);
		out.write4(f.gameInfo.height);
		out.writeBool(f.gameInfo.showBorder);
		out.writeBool(f.gameInfo.allowResize);
		out.writeBool(f.gameInfo.stayOnTop);
		out.writeBool(f.gameInfo.pauseGame);
		out.writeStr(f.gameInfo.gameInfoStr);
		out.write4(500);

		out.write4(0); // "how many longints will follow it"

		out.write4(540);
		out.write4(0); // room indexes in tree order

		writeTree(out,root);
		out.close();
		}

	public static void writeSettings(GmFile f, GmStreamEncoder out, long savetime) throws IOException
		{
		GameSettings g = f.gameSettings;
		out.write4(f.gameSettings.gameId);
		out.fill(4);
		out.write4(600);

		out.writeBool(g.startFullscreen);
		out.writeBool(g.interpolate);
		out.writeBool(g.dontDrawBorder);
		out.writeBool(g.displayCursor);
		out.write4(g.scaling);
		out.writeBool(g.allowWindowResize);
		out.writeBool(g.alwaysOnTop);
		out.write4(Util.getGmColor(g.colorOutsideRoom));
		out.writeBool(g.setResolution);
		out.write4(g.colorDepth);
		out.write4(g.resolution);
		out.write4(g.frequency);
		out.writeBool(g.dontShowButtons);
		out.writeBool(g.useSynchronization);
		out.writeBool(g.letF4SwitchFullscreen);
		out.writeBool(g.letF1ShowGameInfo);
		out.writeBool(g.letEscEndGame);
		out.writeBool(g.letF5SaveF6Load);
		out.write4(g.gamePriority);
		out.writeBool(g.freezeOnLoseFocus);
		out.write4(g.loadBarMode);
		if (g.loadBarMode == GameSettings.LOADBAR_CUSTOM)
			{
			if (g.backLoadBar != null)
				{
				out.write4(10);
				out.writeImage(g.backLoadBar);
				}
			else
				out.write4(-1);
			if (g.frontLoadBar != null)
				{
				out.write4(10);
				out.writeImage(g.frontLoadBar);
				}
			else
				out.write4(-1);
			}
		out.writeBool(g.showCustomLoadImage);
		if (g.showCustomLoadImage)
			{
			if (g.loadingImage != null)
				{
				out.write4(10);
				out.writeImage(g.loadingImage);
				}
			else
				out.write4(-1);
			}
		out.writeBool(g.imagePartiallyTransparent);
		out.write4(g.loadImageAlpha);
		out.writeBool(g.scaleProgressBar);
		out.write4(g.gameIconData.length);
		out.write(g.gameIconData);
		out.writeBool(g.displayErrors);
		out.writeBool(g.writeToLog);
		out.writeBool(g.abortOnError);
		out.writeBool(g.treatUninitializedAs0);
		out.writeStr(g.author);
		try
			{
			out.write4(Integer.parseInt(g.version));
			}
		catch (NumberFormatException e)
			{
			out.write4(100);
			}
		g.lastChanged = GmFile.longTimeToGmTime(savetime);
		out.writeD(g.lastChanged);

		out.writeStr(g.information);
		out.write4(g.constants.size());
		for (Constant con : g.constants)
			{
			out.writeStr(con.name);
			out.writeStr(con.value);
			}
		out.write4(g.includes.size());
		for (Include inc : g.includes)
			out.writeStr(inc.filePath);
		out.write4(g.includeFolder);
		out.writeBool(g.overwriteExisting);
		out.writeBool(g.removeAtGameEnd);
		}

	public static void writeSounds(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(400);
		out.write4(f.sounds.lastId + 1);
		for (int i = 0; i <= f.sounds.lastId; i++)
			{
			Sound snd = f.sounds.getUnsafe(i);
			out.writeBool(snd != null);
			if (snd != null)
				{
				out.writeStr(snd.getName());
				out.write4(600);
				out.write4(GmFile.SOUND_CODE.get(snd.get(PSound.KIND)));
				out.writeStr(snd.properties,PSound.FILE_TYPE,PSound.FILE_NAME);
				if (snd.data != null)
					{
					out.writeBool(true);
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
			}
		}

	public static void writeSprites(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(400);
		out.write4(f.sprites.lastId + 1);
		for (int i = 0; i <= f.sprites.lastId; i++)
			{
			Sprite spr = f.sprites.getUnsafe(i);
			out.writeBool(spr != null);
			if (spr != null)
				{
				out.writeStr(spr.getName());
				out.write4(542);
				out.write4(spr.subImages.getWidth());
				out.write4(spr.subImages.getHeight());
				// The formatter doesn't wrap this line even though it's too long...
				out.write4(spr.properties,PSprite.BB_LEFT,PSprite.BB_RIGHT,PSprite.BB_BOTTOM,//
						PSprite.BB_TOP);
				out.writeBool(spr.properties,PSprite.TRANSPARENT,PSprite.SMOOTH_EDGES,PSprite.PRELOAD);
				out.write4(GmFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)));
				out.writeBool(spr.properties,PSprite.PRECISE);
				out.write4(spr.properties,PSprite.ORIGIN_X,PSprite.ORIGIN_Y);
				out.write4(spr.subImages.size());
				for (int j = 0; j < spr.subImages.size(); j++)
					{
					BufferedImage sub = spr.subImages.get(j);
					out.write4(10);
					out.writeImage(sub);
					}
				}
			}
		}

	public static void writeBackgrounds(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(400);
		out.write4(f.backgrounds.lastId + 1);
		for (int i = 0; i <= f.backgrounds.lastId; i++)
			{
			Background back = f.backgrounds.getUnsafe(i);
			out.writeBool(back != null);
			if (back != null)
				{
				out.writeStr(back.getName());
				out.write4(543);
				out.write4(back.getWidth());
				out.write4(back.getHeight());
				out.writeBool(back.properties,PBackground.TRANSPARENT,PBackground.SMOOTH_EDGES,
						PBackground.PRELOAD,PBackground.USE_AS_TILESET);
				out.write4(back.properties,PBackground.TILE_WIDTH,PBackground.TILE_HEIGHT,
						PBackground.H_OFFSET,PBackground.V_OFFSET,PBackground.H_SEP,PBackground.V_SEP);
				BufferedImage bi = back.getBackgroundImage();
				if (bi != null)
					{
					out.writeBool(true);
					out.write4(10);
					out.writeImage(bi);
					}
				else
					out.writeBool(false);
				}
			}
		}

	public static void writePaths(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(420);
		out.write4(f.paths.lastId + 1);
		for (int i = 0; i <= f.paths.lastId; i++)
			{
			Path path = f.paths.getUnsafe(i);
			out.writeBool(path != null);
			if (path != null)
				{
				out.writeStr(path.getName());
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
			}
		}

	public static void writeScripts(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(400);
		out.write4(f.scripts.lastId + 1);
		for (int i = 0; i <= f.scripts.lastId; i++)
			{
			Script scr = f.scripts.getUnsafe(i);
			out.writeBool(scr != null);
			if (scr != null)
				{
				out.writeStr(scr.getName());
				out.write4(400);
				out.writeStr(scr.properties,PScript.CODE);
				}
			}
		}

	public static void writeFonts(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(540);
		out.write4(f.fonts.lastId + 1);
		for (int i = 0; i <= f.fonts.lastId; i++)
			{
			Font font = f.fonts.getUnsafe(i);
			out.writeBool(font != null);
			if (font != null)
				{
				out.writeStr(font.getName());
				out.write4(540);
				out.writeStr(font.properties,PFont.FONT_NAME);
				out.write4(font.properties,PFont.SIZE);
				out.writeBool(font.properties,PFont.BOLD,PFont.ITALIC);
				out.write4(font.properties,PFont.RANGE_MIN,PFont.RANGE_MAX);
				}
			}
		}

	public static void writeTimelines(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(500);
		out.write4(f.timelines.lastId + 1);
		for (int i = 0; i <= f.timelines.lastId; i++)
			{
			Timeline time = f.timelines.getUnsafe(i);
			out.writeBool(time != null);
			if (time != null)
				{
				out.writeStr(time.getName());
				out.write4(500);
				out.write4(time.moments.size());
				for (Moment mom : time.moments)
					{
					out.write4(mom.stepNo);
					writeActions(out,mom);
					}
				}
			}
		}

	public static void writeGmObjects(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(400);
		out.write4(f.gmObjects.lastId + 1);
		for (int i = 0; i <= f.gmObjects.lastId; i++)
			{
			GmObject obj = f.gmObjects.getUnsafe(i);
			out.writeBool(obj != null);
			if (obj != null)
				{
				out.writeStr(obj.getName());
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
					for (Event ev : me.events)
						{
						if (j == MainEvent.EV_COLLISION)
							out.writeId(ev.other);
						else
							out.write4(ev.id);
						writeActions(out,ev);
						}
					out.write4(-1);
					}
				}
			}
		}

	public static void writeRooms(GmFile f, GmStreamEncoder out) throws IOException
		{
		out.write4(420);
		out.write4(f.rooms.lastId + 1);
		for (int i = 0; i <= f.rooms.lastId; i++)
			{
			Room rm = f.rooms.getUnsafe(i);
			out.writeBool(rm != null);
			if (rm != null)
				{
				out.writeStr(rm.getName());
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
			}
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
							out.writeStr("-1");
						break;
					default:
						out.writeStr(arg.getVal());
						break;
					}
			out.writeBool(act.isNot());
			}
		}
	}
