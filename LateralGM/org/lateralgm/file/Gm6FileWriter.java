package org.lateralgm.file;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.lateralgm.components.ResNode;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Constant;
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
			out.writei(1234321);
			out.writei(600);
			out.writei(f.gameId);
			out.fill(4);
			out.writei(600);

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

			out.writei(f.lastInstanceId);
			out.writei(f.lastTileId);

			// GAME SETTINGS
			out.writei(600);
			out.writei(f.gameInfo.backgroundColor.getRGB());
			out.writeBool(f.gameInfo.mimicGameWindow);
			out.writeStr(f.gameInfo.formCaption);
			out.writei(f.gameInfo.left);
			out.writei(f.gameInfo.top);
			out.writei(f.gameInfo.width);
			out.writei(f.gameInfo.height);
			out.writeBool(f.gameInfo.showBorder);
			out.writeBool(f.gameInfo.allowResize);
			out.writeBool(f.gameInfo.stayOnTop);
			out.writeBool(f.gameInfo.pauseGame);
			out.writeStr(f.gameInfo.gameInfoStr);
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
		}

	public static void writeSettings(Gm6File f, GmStreamEncoder out, long savetime)
			throws IOException
		{
		out.writeBool(f.startFullscreen);
		out.writeBool(f.interpolate);
		out.writeBool(f.dontDrawBorder);
		out.writeBool(f.displayCursor);
		out.writei(f.scaling);
		out.writeBool(f.allowWindowResize);
		out.writeBool(f.alwaysOnTop);
		out.writei(f.colorOutsideRoom);
		out.writeBool(f.setResolution);
		out.writei(f.colorDepth);
		out.writei(f.resolution);
		out.writei(f.frequency);
		out.writeBool(f.dontShowButtons);
		out.writeBool(f.useSynchronization);
		out.writeBool(f.letF4SwitchFullscreen);
		out.writeBool(f.letF1ShowGameInfo);
		out.writeBool(f.letEscEndGame);
		out.writeBool(f.letF5SaveF6Load);
		out.writei(f.gamePriority);
		out.writeBool(f.freezeOnLoseFocus);
		out.writei(f.loadBarMode);
		if (f.loadBarMode == Gm6File.LOADBAR_CUSTOM)
			{
			if (f.backLoadBar != null)
				{
				out.writei(10);
				out.writeImage(f.backLoadBar);
				}
			else
				out.writei(-1);
			if (f.frontLoadBar != null)
				{
				out.writei(10);
				out.writeImage(f.frontLoadBar);
				}
			else
				out.writei(-1);
			}
		out.writeBool(f.showCustomLoadImage);
		if (f.showCustomLoadImage)
			{
			if (f.loadingImage != null)
				{
				out.writei(10);
				out.writeImage(f.loadingImage);
				}
			else
				out.writei(-1);
			}
		out.writeBool(f.imagePartiallyTransparent);
		out.writei(f.loadImageAlpha);
		out.writeBool(f.scaleProgressBar);
		out.write(f.gameIconData);
		out.writeBool(f.displayErrors);
		out.writeBool(f.writeToLog);
		out.writeBool(f.abortOnError);
		out.writeBool(f.treatUninitializedAs0);
		out.writeStr(f.author);
		out.writei(f.version);
		f.lastChanged = Gm6File.longTimeToGmTime(savetime);
		out.writeD(f.lastChanged);

		out.writeStr(f.information);
		out.writei(f.constants.size());
		for (Constant con : f.constants)
			{
			out.writeStr(con.name);
			out.writeStr(con.value);
			}
		out.writei(f.includes.size());
		for (Include inc : f.includes)
			out.writeStr(inc.filePath);
		out.writei(f.includeFolder);
		out.writeBool(f.overwriteExisting);
		out.writeBool(f.removeAtGameEnd);
		}

	public static void writeSounds(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(400);
		out.writei(f.sounds.lastId + 1);
		for (int i = 0; i <= f.sounds.lastId; i++)
			{
			Sound snd = f.sounds.getUnsafe(i);
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
		}

	public static void writeSprites(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(400);
		out.writei(f.sprites.lastId + 1);
		for (int i = 0; i <= f.sprites.lastId; i++)
			{
			Sprite spr = f.sprites.getUnsafe(i);
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
		}

	public static void writeBackgrounds(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(400);
		out.writei(f.backgrounds.lastId + 1);
		for (int i = 0; i <= f.backgrounds.lastId; i++)
			{
			Background back = f.backgrounds.getUnsafe(i);
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
		}

	public static void writePaths(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(420);
		out.writei(f.paths.lastId + 1);
		for (int i = 0; i <= f.paths.lastId; i++)
			{
			Path path = f.paths.getUnsafe(i);
			out.writeBool(path != null);
			if (path != null)
				{
				out.writeStr(path.getName());
				out.writei(530);
				out.writeBool(path.smooth);
				out.writeBool(path.closed);
				out.writei(path.precision);
				out.writeId(path.backgroundRoom,Resource.ROOM,f);
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
		}

	public static void writeScripts(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(400);
		out.writei(f.scripts.lastId + 1);
		for (int i = 0; i <= f.scripts.lastId; i++)
			{
			Script scr = f.scripts.getUnsafe(i);
			out.writeBool(scr != null);
			if (scr != null)
				{
				out.writeStr(scr.getName());
				out.writei(400);
				out.writeStr(scr.scriptStr);
				}
			}
		}

	public static void writeFonts(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(540);
		out.writei(f.fonts.lastId + 1);
		for (int i = 0; i <= f.fonts.lastId; i++)
			{
			Font font = f.fonts.getUnsafe(i);
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
		}

	public static void writeTimelines(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(500);
		out.writei(f.timelines.lastId + 1);
		for (int i = 0; i <= f.timelines.lastId; i++)
			{
			Timeline time = f.timelines.getUnsafe(i);
			out.writeBool(time != null);
			if (time != null)
				{
				out.writeStr(time.getName());
				out.writei(500);
				out.writei(time.moments.size());
				for (Moment mom : time.moments)
					{
					out.writei(mom.stepNo);
					writeActions(f,out,mom);
					}
				}
			}
		}

	public static void writeGmObjects(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(400);
		out.writei(f.gmObjects.lastId + 1);
		for (int i = 0; i <= f.gmObjects.lastId; i++)
			{
			GmObject obj = f.gmObjects.getUnsafe(i);
			out.writeBool(obj != null);
			if (obj != null)
				{
				out.writeStr(obj.getName());
				out.writei(430);
				out.writeId(obj.sprite,Resource.SPRITE,f);
				out.writeBool(obj.solid);
				out.writeBool(obj.visible);
				out.writei(obj.depth);
				out.writeBool(obj.persistent);
				out.writeId(obj.parent,Resource.GMOBJECT,-100,f);
				out.writeId(obj.mask,Resource.SPRITE,f);
				out.writei(10);
				for (int j = 0; j < 11; j++)
					{
					for (int k = 0; k < obj.mainEvents[j].noEvents(); k++)
						{
						Event ev = obj.mainEvents[j].getEventList(k);
						if (j == MainEvent.EV_COLLISION)
							out.writeId(ev.other,Resource.GMOBJECT,f);
						else
							out.writei(ev.id);
						writeActions(f,out,ev);
						}
					out.writei(-1);
					}
				}
			}
		}

	public static void writeRooms(Gm6File f, GmStreamEncoder out) throws IOException
		{
		out.writei(420);
		out.writei(f.rooms.lastId + 1);
		for (int i = 0; i <= f.rooms.lastId; i++)
			{
			Room rm = f.rooms.getUnsafe(i);
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
					out.writeId(back.backgroundId,Resource.BACKGROUND,f);
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
					out.writeId(view.objectFollowing,Resource.GMOBJECT,f);
					}
				out.writei(rm.noInstances());
				for (int j = 0; j < rm.noInstances(); j++)
					{
					Instance in = rm.getInstanceList(j);
					out.writei(in.x);
					out.writei(in.y);
					out.writeId(in.gmObjectId,Resource.GMOBJECT,f);
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
					out.writeId(tile.backgroundId,Resource.BACKGROUND,f);
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
		}

	public static void writeActions(Gm6File f, GmStreamEncoder out, ActionContainer container)
			throws IOException
		{
		out.writei(400);
		out.writei(container.actions.size());
		for (Action act : container.actions)
			{
			out.writei(440);
			out.writei(act.libAction.parent != null ? act.libAction.parent.id : act.libAction.parentId);
			out.writei(act.libAction.id);
			out.writei(act.libAction.actionKind);
			out.writeBool(act.libAction.allowRelative);
			out.writeBool(act.libAction.question);
			out.writeBool(act.libAction.canApplyTo);
			out.writei(act.libAction.execType);
			if (act.libAction.execType == Action.EXEC_FUNCTION)
				out.writeStr(act.libAction.execInfo);
			else
				out.writei(0);
			if (act.libAction.execType == Action.EXEC_CODE)
				out.writeStr(act.libAction.execInfo);
			else
				out.writei(0);
			out.writei(act.arguments.length);

			out.writei(act.arguments.length);
			for (Argument arg : act.arguments)
				out.writei(arg.kind);

			if (act.appliesTo != null)
				{
				if (act.appliesTo.getValue() >= 0)
					out.writeId(act.appliesTo,Resource.GMOBJECT,-100,f);
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
