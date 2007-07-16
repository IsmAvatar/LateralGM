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

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.lateralgm.components.ResNode;
import org.lateralgm.main.Util;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Constant;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

public final class Gm6FileWriter
	{
	private Gm6FileWriter()
		{
		}

	public static void writeGm6File(Gm6File f, String fileName, ResNode root)
		{
		long savetime = System.currentTimeMillis();
		GmStreamEncoder out = null;
		try
			{
			out = new GmStreamEncoder(fileName);
			out.write4(1234321);
			out.write4(600);
			out.write4(f.gameId);
			out.fill(4);
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

			// GAME SETTINGS
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
		}

	public static void writeSettings(Gm6File f, GmStreamEncoder out, long savetime)
			throws IOException
		{
		out.writeBool(f.startFullscreen);
		out.writeBool(f.interpolate);
		out.writeBool(f.dontDrawBorder);
		out.writeBool(f.displayCursor);
		out.write4(f.scaling);
		out.writeBool(f.allowWindowResize);
		out.writeBool(f.alwaysOnTop);
		out.write4(Util.getGmColor(f.colorOutsideRoom));
		out.writeBool(f.setResolution);
		out.write4(f.colorDepth);
		out.write4(f.resolution);
		out.write4(f.frequency);
		out.writeBool(f.dontShowButtons);
		out.writeBool(f.useSynchronization);
		out.writeBool(f.letF4SwitchFullscreen);
		out.writeBool(f.letF1ShowGameInfo);
		out.writeBool(f.letEscEndGame);
		out.writeBool(f.letF5SaveF6Load);
		out.write4(f.gamePriority);
		out.writeBool(f.freezeOnLoseFocus);
		out.write4(f.loadBarMode);
		if (f.loadBarMode == Gm6File.LOADBAR_CUSTOM)
			{
			if (f.backLoadBar != null)
				{
				out.write4(10);
				out.writeImage(f.backLoadBar);
				}
			else
				out.write4(-1);
			if (f.frontLoadBar != null)
				{
				out.write4(10);
				out.writeImage(f.frontLoadBar);
				}
			else
				out.write4(-1);
			}
		out.writeBool(f.showCustomLoadImage);
		if (f.showCustomLoadImage)
			{
			if (f.loadingImage != null)
				{
				out.write4(10);
				out.writeImage(f.loadingImage);
				}
			else
				out.write4(-1);
			}
		out.writeBool(f.imagePartiallyTransparent);
		out.write4(f.loadImageAlpha);
		out.writeBool(f.scaleProgressBar);
		out.write(f.gameIconData);
		out.writeBool(f.displayErrors);
		out.writeBool(f.writeToLog);
		out.writeBool(f.abortOnError);
		out.writeBool(f.treatUninitializedAs0);
		out.writeStr(f.author);
		out.write4(f.version);
		f.lastChanged = Gm6File.longTimeToGmTime(savetime);
		out.writeD(f.lastChanged);

		out.writeStr(f.information);
		out.write4(f.constants.size());
		for (Constant con : f.constants)
			{
			out.writeStr(con.name);
			out.writeStr(con.value);
			}
		out.write4(f.includes.size());
		for (Include inc : f.includes)
			out.writeStr(inc.filePath);
		out.write4(f.includeFolder);
		out.writeBool(f.overwriteExisting);
		out.writeBool(f.removeAtGameEnd);
		}

	public static void writeSounds(Gm6File f, GmStreamEncoder out) throws IOException
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
				out.write4(snd.kind);
				out.writeStr(snd.fileType);
				out.writeStr(snd.fileName);
				if (snd.data != null)
					{
					out.writeBool(true);
					out.compress(snd.data);
					}
				else
					out.writeBool(false);
				out.write4(snd.getEffects());
				out.writeD(snd.volume);
				out.writeD(snd.pan);
				out.writeBool(snd.preload);
				}
			}
		}

	public static void writeSprites(Gm6File f, GmStreamEncoder out) throws IOException
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
				out.write4(spr.width);
				out.write4(spr.height);
				out.write4(spr.boundingBoxLeft);
				out.write4(spr.boundingBoxRight);
				out.write4(spr.boundingBoxBottom);
				out.write4(spr.boundingBoxTop);
				out.writeBool(spr.transparent);
				out.writeBool(spr.smoothEdges);
				out.writeBool(spr.preload);
				out.write4(spr.boundingBoxMode);
				out.writeBool(spr.preciseCC);
				out.write4(spr.originX);
				out.write4(spr.originY);
				out.write4(spr.noSubImages());
				for (int j = 0; j < spr.noSubImages(); j++)
					{
					BufferedImage sub = spr.getSubImage(j);
					out.write4(10);
					out.writeImage(sub);
					}
				}
			}
		}

	public static void writeBackgrounds(Gm6File f, GmStreamEncoder out) throws IOException
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
				out.write4(back.width);
				out.write4(back.height);
				out.writeBool(back.transparent);
				out.writeBool(back.smoothEdges);
				out.writeBool(back.preload);
				out.writeBool(back.useAsTileSet);
				out.write4(back.tileWidth);
				out.write4(back.tileHeight);
				out.write4(back.horizOffset);
				out.write4(back.vertOffset);
				out.write4(back.horizSep);
				out.write4(back.vertSep);
				if (back.backgroundImage != null)
					{
					out.writeBool(true);
					out.write4(10);
					out.writeImage(back.backgroundImage);
					}
				else
					out.writeBool(false);
				}
			}
		}

	public static void writePaths(Gm6File f, GmStreamEncoder out) throws IOException
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
				out.writeBool(path.smooth);
				out.writeBool(path.closed);
				out.write4(path.precision);
				out.writeId(path.backgroundRoom,Resource.ROOM,f);
				out.write4(path.snapX);
				out.write4(path.snapY);
				out.write4(path.noPoints());
				for (int j = 0; j < path.noPoints(); j++)
					{
					out.writeD(path.getPoint(j).x);
					out.writeD(path.getPoint(j).y);
					out.writeD(path.getPoint(j).speed);
					}
				}
			}
		}

	public static void writeScripts(Gm6File f, GmStreamEncoder out) throws IOException
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
				out.writeStr(scr.scriptStr);
				}
			}
		}

	public static void writeFonts(Gm6File f, GmStreamEncoder out) throws IOException
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
				out.writeStr(font.fontName);
				out.write4(font.size);
				out.writeBool(font.bold);
				out.writeBool(font.italic);
				out.write4(font.charRangeMin);
				out.write4(font.charRangeMax);
				}
			}
		}

	public static void writeTimelines(Gm6File f, GmStreamEncoder out) throws IOException
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
					writeActions(f,out,mom);
					}
				}
			}
		}

	public static void writeGmObjects(Gm6File f, GmStreamEncoder out) throws IOException
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
				out.writeId(obj.sprite,Resource.SPRITE,f);
				out.writeBool(obj.solid);
				out.writeBool(obj.visible);
				out.write4(obj.depth);
				out.writeBool(obj.persistent);
				out.writeId(obj.parent,Resource.GMOBJECT,-100,f);
				out.writeId(obj.mask,Resource.SPRITE,f);
				out.write4(10);
				for (int j = 0; j < 11; j++)
					{
					for (int k = 0; k < obj.mainEvents[j].noEvents(); k++)
						{
						Event ev = obj.mainEvents[j].getEventList(k);
						if (j == MainEvent.EV_COLLISION)
							out.writeId(ev.other,Resource.GMOBJECT,f);
						else
							out.write4(ev.id);
						writeActions(f,out,ev);
						}
					out.write4(-1);
					}
				}
			}
		}

	public static void writeRooms(Gm6File f, GmStreamEncoder out) throws IOException
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
				out.writeStr(rm.caption);
				out.write4(rm.width);
				out.write4(rm.height);
				out.write4(rm.snapY);
				out.write4(rm.snapX);
				out.writeBool(rm.isometricGrid);
				out.write4(rm.speed);
				out.writeBool(rm.persistent);
				out.write4(rm.backgroundColor);
				out.writeBool(rm.drawBackgroundColor);
				out.writeStr(rm.creationCode);
				out.write4(8);
				for (int j = 0; j < 8; j++)
					{
					BackgroundDef back = rm.backgroundDefs[j];
					out.writeBool(back.visible);
					out.writeBool(back.foreground);
					out.writeId(back.backgroundId,Resource.BACKGROUND,f);
					out.write4(back.x);
					out.write4(back.y);
					out.writeBool(back.tileHoriz);
					out.writeBool(back.tileVert);
					out.write4(back.horizSpeed);
					out.write4(back.vertSpeed);
					out.writeBool(back.stretch);
					}
				out.writeBool(rm.enableViews);
				out.write4(8);
				for (int j = 0; j < 8; j++)
					{
					View view = rm.views[j];
					out.writeBool(view.enabled);
					out.write4(view.viewX);
					out.write4(view.viewY);
					out.write4(view.viewW);
					out.write4(view.viewH);
					out.write4(view.portX);
					out.write4(view.portY);
					out.write4(view.portW);
					out.write4(view.portH);
					out.write4(view.hbor);
					out.write4(view.vbor);
					out.write4(view.hspeed);
					out.write4(view.vspeed);
					out.writeId(view.objectFollowing,Resource.GMOBJECT,f);
					}
				out.write4(rm.noInstances());
				for (int j = 0; j < rm.noInstances(); j++)
					{
					Instance in = rm.getInstanceList(j);
					out.write4(in.x);
					out.write4(in.y);
					out.writeId(in.gmObjectId,Resource.GMOBJECT,f);
					out.write4(in.instanceId);
					out.writeStr(in.creationCode);
					out.writeBool(in.locked);
					}
				out.write4(rm.noTiles());
				for (int j = 0; j < rm.noTiles(); j++)
					{
					Tile tile = rm.getTileList(j);
					out.write4(tile.x);
					out.write4(tile.y);
					out.writeId(tile.backgroundId,Resource.BACKGROUND,f);
					out.write4(tile.tileX);
					out.write4(tile.tileY);
					out.write4(tile.width);
					out.write4(tile.height);
					out.write4(tile.depth);
					out.write4(tile.tileId);
					out.writeBool(tile.locked);
					}
				out.writeBool(rm.rememberWindowSize);
				out.write4(rm.editorWidth);
				out.write4(rm.editorHeight);
				out.writeBool(rm.showGrid);
				out.writeBool(rm.showObjects);
				out.writeBool(rm.showTiles);
				out.writeBool(rm.showBackgrounds);
				out.writeBool(rm.showForegrounds);
				out.writeBool(rm.showViews);
				out.writeBool(rm.deleteUnderlyingObjects);
				out.writeBool(rm.deleteUnderlyingTiles);
				out.write4(rm.currentTab);
				out.write4(rm.scrollBarX);
				out.write4(rm.scrollBarY);
				}
			}
		}

	public static void writeActions(Gm6File f, GmStreamEncoder out, ActionContainer container)
			throws IOException
		{
		out.write4(400);
		out.write4(container.actions.size());
		for (Action act : container.actions)
			{
			out.write4(440);
			out.write4(act.libAction.parent != null ? act.libAction.parent.id : act.libAction.parentId);
			out.write4(act.libAction.id);
			out.write4(act.libAction.actionKind);
			out.writeBool(act.libAction.allowRelative);
			out.writeBool(act.libAction.question);
			out.writeBool(act.libAction.canApplyTo);
			out.write4(act.libAction.execType);
			if (act.libAction.execType == Action.EXEC_FUNCTION)
				out.writeStr(act.libAction.execInfo);
			else
				out.write4(0);
			if (act.libAction.execType == Action.EXEC_CODE)
				out.writeStr(act.libAction.execInfo);
			else
				out.write4(0);
			out.write4(act.arguments.length);

			out.write4(act.arguments.length);
			for (Argument arg : act.arguments)
				out.write4(arg.kind);

			if (act.appliesTo != null)
				{
				if (act.appliesTo.getValue() >= 0)
					out.writeId(act.appliesTo,Resource.GMOBJECT,-100,f);
				else
					// self/other are exceptions to the system
					out.write4(act.appliesTo.getValue());
				}
			else
				out.write4(-100);
			out.writeBool(act.relative);

			out.write4(act.arguments.length);
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
						out.writeIdStr(arg.res,arg.kind,f);
						break;
					default:
						out.writeStr(arg.val);
						break;
					}
			out.writeBool(act.not);
			}
		}
	}
