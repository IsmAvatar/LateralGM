/**
* @file  GMXFileReader.java
* @brief Class implementing a GMX file reader.
*
* @section License
*
* Copyright (C) 2013-2015 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.lateralgm.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.imageio.ImageIO;
import javax.swing.JProgressBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.ProjectFile.ResourceHolder;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.Constants;
import org.lateralgm.resources.Extension;
import org.lateralgm.resources.ExtensionPackages;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.resources.GameInformation;
import org.lateralgm.resources.GameInformation.PGameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.Include;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Room.PRoom;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Script.PScript;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Shader.PShader;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibArgument;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.Constant;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.GlyphMetric;
import org.lateralgm.resources.sub.GlyphMetric.PGlyphMetric;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.resources.sub.ShapePoint;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.Tile.PTile;
import org.lateralgm.resources.sub.View;
import org.lateralgm.resources.sub.View.PView;
import org.lateralgm.util.PropertyMap;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

// TODO: Possibly rewrite from a DOM parser to a SAX parser,
// because SAX is light weight faster and uses less memory,
// DOM reads the whole thing into memory and then parses it.
// There is a downside to SAX such as incompatibility with UTF-8
public final class GMXFileReader
	{
	public static final String STUPID_SHADER_MARKER =
			"//######################_==_YOYO_SHADER_MARKER_==_######################@~"; //$NON-NLS-1$

	private static DocumentBuilderFactory documentBuilderFactory;
	private static DocumentBuilder documentBuilder;

	private GMXFileReader()
		{
		}

	static Queue<PostponedRef> postpone = new LinkedList<PostponedRef>();

	static interface PostponedRef
		{
		boolean invoke();
		}

	static class DefaultPostponedRef<K extends Enum<K>> implements PostponedRef
		{
		ResourceList<?> list;
		String name;
		PropertyMap<K> p;
		K key;

		DefaultPostponedRef(ResourceList<?> list, PropertyMap<K> p, K key, String name)
			{
			this.list = list;
			this.p = p;
			this.key = key;
			this.name = name;
			}

		public boolean invoke()
			{
			Resource<?,?> temp = list.get(name);
			if (temp != null) p.put(key,temp.reference);
			return temp != null;
			}
		}

	private static Document parseDocumentUnchecked(ProjectFile f, String path) throws GmFormatException
		{
		Document doc = null;
		try
			{
			doc = documentBuilder.parse(path);
			}
		catch (SAXException e)
			{
			throw new GmFormatException(f, "failed to parse: " + path, e);
			}
		catch (IOException e)
			{
			throw new GmFormatException(f, "failed to read: " + path, e);
			}
		return doc;
		}

	private static Document parseDocumentChecked(ProjectFile f, String path)
		{
			Document doc = null;
			try
				{
				doc = parseDocumentUnchecked(f, path);
				}
			catch (GmFormatException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			return doc;
		}

	//Workaround for Parameter limit
	private static class ProjectFileContext
		{
		ProjectFile f;
		Document in;
		RefList<Timeline> timeids;
		RefList<GmObject> objids;
		RefList<Room> rmids;

		public ProjectFileContext(ProjectFile f, Document d, RefList<Timeline> timeids,
				RefList<GmObject> objids, RefList<Room> rmids)
			{
			this.f = f;
			this.in = d;
			this.timeids = timeids;
			this.objids = objids;
			this.rmids = rmids;
			}

		public ProjectFileContext copy()
			{
			return new ProjectFileContext(f,in,timeids,objids,rmids);
			}
		}

	private static GmFormatException versionError(ProjectFile f, String error, String res, int ver)
		{
		return versionError(f,error,res,0,ver);
		}

	private static GmFormatException versionError(ProjectFile f, String error, String res, int i,
			int ver)
		{
		return new GmFormatException(f,Messages.format(
				"ProjectFileReader.ERROR_UNSUPPORTED",Messages.format(//$NON-NLS-1$
						"ProjectFileReader." + error,Messages.getString("LGM." + res),i),ver)); //$NON-NLS-1$  //$NON-NLS-2$
		}

	public static void readProjectFile(InputStream stream, ProjectFile file, URI uri, ResNode root)
			throws GmFormatException
		{
		readProjectFile(stream,file,uri,root,null);
		}

	public static void readProjectFile(InputStream stream, ProjectFile file, URI uri, ResNode root,
			Charset forceCharset) throws GmFormatException
		{
		file.format = ProjectFile.FormatFlavor.GMX;
		if (documentBuilderFactory == null)
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
		if (documentBuilder == null)
			try
				{
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				}
			catch (ParserConfigurationException e1)
				{
				throw new GmFormatException(file,e1);
				}
		RefList<Timeline> timeids = new RefList<Timeline>(Timeline.class); // timeline ids
		RefList<GmObject> objids = new RefList<GmObject>(GmObject.class); // object ids
		RefList<Room> rmids = new RefList<Room>(Room.class); // room id
		long startTime = System.currentTimeMillis();

		try
			{
			Document document = GMXFileReader.parseDocumentUnchecked(file, uri.toString());

			ProjectFileContext c = new ProjectFileContext(file,document,timeids,objids,rmids);

			JProgressBar progressBar = LGM.getProgressDialogBar();
			progressBar.setMaximum(160);
			LGM.setProgressTitle(Messages.getString("ProgressDialog.GMX_LOADING")); //$NON-NLS-1$

			LGM.setProgress(0,Messages.getString("ProgressDialog.SPRITES")); //$NON-NLS-1$
			readSprites(c,root);
			LGM.setProgress(10,Messages.getString("ProgressDialog.SOUNDS")); //$NON-NLS-1$
			readSounds(c,root);
			LGM.setProgress(20,Messages.getString("ProgressDialog.BACKGROUNDS")); //$NON-NLS-1$
			readBackgrounds(c,root);
			LGM.setProgress(30,Messages.getString("ProgressDialog.PATHS")); //$NON-NLS-1$
			readPaths(c,root);
			LGM.setProgress(40,Messages.getString("ProgressDialog.SCRIPTS")); //$NON-NLS-1$
			readScripts(c,root);
			LGM.setProgress(50,Messages.getString("ProgressDialog.SHADERS")); //$NON-NLS-1$
			readShaders(c,root);
			LGM.setProgress(60,Messages.getString("ProgressDialog.FONTS")); //$NON-NLS-1$
			readFonts(c,root);
			LGM.setProgress(70,Messages.getString("ProgressDialog.TIMELINES")); //$NON-NLS-1$
			readTimelines(c,root);
			LGM.setProgress(80,Messages.getString("ProgressDialog.OBJECTS")); //$NON-NLS-1$
			readGmObjects(c,root);
			LGM.setProgress(90,Messages.getString("ProgressDialog.ROOMS")); //$NON-NLS-1$
			readRooms(c,root);
			LGM.setProgress(100,Messages.getString("ProgressDialog.INCLUDEFILES")); //$NON-NLS-1$
			readIncludedFiles(c,root);
			LGM.setProgress(110,Messages.getString("ProgressDialog.EXTENSIONS")); //$NON-NLS-1$
			readExtensions(c,root);
			LGM.setProgress(120,Messages.getString("ProgressDialog.CONSTANTS")); //$NON-NLS-1$
			readDefaultConstants(c,root);
			LGM.setProgress(130,Messages.getString("ProgressDialog.GAMEINFORMATION")); //$NON-NLS-1$
			readGameInformation(c,root);
			LGM.setProgress(140,Messages.getString("ProgressDialog.SETTINGS")); //$NON-NLS-1$
			readConfigurations(c,root);
			LGM.setProgress(150,Messages.getString("ProgressDialog.PACKAGES")); //$NON-NLS-1$
			readPackages(c,root);

			LGM.setProgress(160,Messages.getString("ProgressDialog.POSTPONED")); //$NON-NLS-1$
			// All resources read, now we can invoke our postponed references.
			for (PostponedRef i : postpone)
				i.invoke();

			LGM.setProgress(160,Messages.getString("ProgressDialog.FINISHED")); //$NON-NLS-1$
			System.out.println(Messages.format("ProjectFileReader.LOADTIME",System.currentTimeMillis() //$NON-NLS-1$
					- startTime));
			}
		catch (Exception e)
			{
			if ((e instanceof GmFormatException)) throw (GmFormatException) e;
			throw new GmFormatException(file,e);
			}
		finally
			{
			try
				{
				if (stream != null)
					{
					stream.close();
					stream = null;
					}
				}
			catch (IOException ex)
				{
				String key = Messages.getString("GmFileReader.ERROR_CLOSEFAILED"); //$NON-NLS-1$
				throw new GmFormatException(file,key);
				}
			}
		}

	private static void readConfigurations(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		NodeList configNodes = in.getElementsByTagName("Configs").item(0).getChildNodes(); //$NON-NLS-1$

		// clear the old/default ones
		c.f.gameSettings.clear();

		for (int i = 0; i < configNodes.getLength(); i++)
			{
			Node cNode = configNodes.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			if (cname.toLowerCase().equals("configs")) //$NON-NLS-1$
				{
				continue;
				}
			else if (cname.toLowerCase().equals("config")) //$NON-NLS-1$
				{

				GameSettings gSet = new GameSettings();
				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				gSet.setName(fileName);

				c.f.gameSettings.add(gSet);
				PropertyMap<PGameSettings> pSet = gSet.properties;

				String path = c.f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document setdoc = GMXFileReader.parseDocumentChecked(c.f, path + ".config.gmx"); //$NON-NLS-1$
				if (setdoc == null) continue;

				pSet.put(
						PGameSettings.START_FULLSCREEN,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_fullscreen").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.ALLOW_WINDOW_RESIZE,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_sizeable").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.ALWAYS_ON_TOP,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_stayontop").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.ABORT_ON_ERROR,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_aborterrors").item(0).getTextContent())); //$NON-NLS-1$
				// TODO: This value is stored using the Windows native dialog's name for the color value, ie
				// "clBlack" or "clWhite" meaning black and white respectively. If the user chooses a custom
				// defined color in the dialog, then the value is in the hexadecimal form "$HHHHHHHH" using
				// a dollar sign instead of a hash sign as a normal hex color value does in other places in
				// the same configuration file.
				// This will not be compatible if they ever try to port their IDE to other platforms.
				//gSet.put(PGameSettings.COLOR_OUTSIDE_ROOM, Integer.parseInt(setdoc.getElementsByTagName("option_windowcolor").item(0).getTextContent()));
				pSet.put(
						PGameSettings.DISABLE_SCREENSAVERS,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_noscreensaver").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.DISPLAY_CURSOR,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_showcursor").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.DISPLAY_ERRORS,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_displayerrors").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.DONT_DRAW_BORDER,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_noborder").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.DONT_SHOW_BUTTONS,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_nobuttons").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.ERROR_ON_ARGS,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_argumenterrors").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.FREEZE_ON_LOSE_FOCUS,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_freeze").item(0).getTextContent())); //$NON-NLS-1$

				pSet.put(
						PGameSettings.COLOR_DEPTH,
						ProjectFile.GS_DEPTHS[Integer.parseInt(setdoc.getElementsByTagName("option_colordepth").item( //$NON-NLS-1$
								0).getTextContent())]);
				pSet.put(
						PGameSettings.FREQUENCY,
						ProjectFile.GS_FREQS[Integer.parseInt(setdoc.getElementsByTagName("option_frequency").item( //$NON-NLS-1$
								0).getTextContent())]);
				pSet.put(
						PGameSettings.RESOLUTION,
						ProjectFile.GS_RESOLS[Integer.parseInt(setdoc.getElementsByTagName("option_resolution").item( //$NON-NLS-1$
								0).getTextContent())]);
				pSet.put(
						PGameSettings.SET_RESOLUTION,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_changeresolution").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.GAME_PRIORITY,
						ProjectFile.GS_PRIORITIES[Integer.parseInt(setdoc.getElementsByTagName(
								"option_priority").item(0).getTextContent())]); //$NON-NLS-1$

				// For some odd reason these two settings are combined together.
				// 2147483649 - Both
				// 2147483648 - Software Vertex Processing only
				// 1 - Synchronization Only
				// 0 - None
				long syncvertex = Long.parseLong(setdoc.getElementsByTagName("option_sync_vertex").item(0).getTextContent()); //$NON-NLS-1$
				gSet.put(PGameSettings.USE_SYNCHRONIZATION,(syncvertex == 2147483649L || syncvertex == 1));
				pSet.put(PGameSettings.FORCE_SOFTWARE_VERTEX_PROCESSING,
						(syncvertex == 2147483649L || syncvertex == 2147483648L));

				pSet.put(
						PGameSettings.LET_ESC_END_GAME,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_closeesc").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.INTERPOLATE,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_interpolate").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(PGameSettings.SCALING,
						Integer.parseInt(setdoc.getElementsByTagName("option_scale").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.TREAT_CLOSE_AS_ESCAPE,
						Boolean.parseBoolean(setdoc.getElementsByTagName("option_closeesc").item(0).getTextContent())); //$NON-NLS-1$
				String changed = setdoc.getElementsByTagName("option_lastchanged").item(0).getTextContent(); //$NON-NLS-1$
				if (!changed.isEmpty())
					pSet.put(PGameSettings.LAST_CHANGED,Double.parseDouble(changed));

				// TODO: Could not find these properties in GMX
				//gSet.put(PGameSettings.BACK_LOAD_BAR,
				//	Boolean.parseBoolean(setdoc.getElementsByTagName("option_stayontop").item(0).getTextContent()));
				//gSet.put(PGameSettings.FRONT_LOAD_BAR,
				//	Boolean.parseBoolean(setdoc.getElementsByTagName("option_showcursor").item(0).getTextContent()));

				String icopath = c.f.getDirectory() + '/'
						+ setdoc.getElementsByTagName("option_windows_game_icon").item(0).getTextContent(); //$NON-NLS-1$
				try
					{
					pSet.put(PGameSettings.GAME_ICON,new ICOFile(Util.getPOSIXPath(icopath)));
					}
				catch (IOException e)
					{
					LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "failed to read: " + icopath, e));
					}
				pSet.put(PGameSettings.GAME_ID,
						Integer.parseInt(setdoc.getElementsByTagName("option_gameid").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.GAME_GUID,
						HexBin.decode(setdoc.getElementsByTagName("option_gameguid").item(0).getTextContent().replace( //$NON-NLS-1$
								"-","").replace("{","").replace("}","")));

				pSet.put(PGameSettings.AUTHOR,
						setdoc.getElementsByTagName("option_author").item(0).getTextContent()); //$NON-NLS-1$
				pSet.put(PGameSettings.VERSION,
						setdoc.getElementsByTagName("option_version").item(0).getTextContent()); //$NON-NLS-1$
				pSet.put(PGameSettings.INFORMATION,
						setdoc.getElementsByTagName("option_information").item(0).getTextContent()); //$NON-NLS-1$
				pSet.put(PGameSettings.COMPANY,
						setdoc.getElementsByTagName("option_version_company").item(0).getTextContent()); //$NON-NLS-1$
				pSet.put(PGameSettings.COPYRIGHT,
						setdoc.getElementsByTagName("option_version_copyright").item(0).getTextContent()); //$NON-NLS-1$
				pSet.put(PGameSettings.DESCRIPTION,
						setdoc.getElementsByTagName("option_version_description").item(0).getTextContent()); //$NON-NLS-1$
				pSet.put(PGameSettings.PRODUCT,
						setdoc.getElementsByTagName("option_version_product").item(0).getTextContent()); //$NON-NLS-1$
				pSet.put(
						PGameSettings.VERSION_BUILD,
						Integer.parseInt(setdoc.getElementsByTagName("option_version_build").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.VERSION_MAJOR,
						Integer.parseInt(setdoc.getElementsByTagName("option_version_major").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.VERSION_MINOR,
						Integer.parseInt(setdoc.getElementsByTagName("option_version_minor").item(0).getTextContent())); //$NON-NLS-1$
				pSet.put(
						PGameSettings.VERSION_RELEASE,
						Integer.parseInt(setdoc.getElementsByTagName("option_version_release").item(0).getTextContent())); //$NON-NLS-1$

				Node cnstNode = setdoc.getElementsByTagName("ConfigConstants").item(0); //$NON-NLS-1$

				// If there is a constant section
				if (cnstNode != null)
					{
					NodeList cnstsList = cnstNode.getChildNodes();
					boolean found = false;

					for (int ic = 0; ic < cnstsList.getLength(); ic++)
						{
						cnstNode = cnstsList.item(ic);
						String cnstName = cnstNode.getNodeName();
						if (cnstName.toLowerCase().equals("#text")) //$NON-NLS-1$
							{
							continue;
							}
						else if (cnstName.toLowerCase().equals("constants")) //$NON-NLS-1$
							{
							found = true;
							break;
							}
						}

					if (found) readConstants(gSet.constants,cnstNode);

					}

				}
			}

		ResNode node = new ResNode("Game Settings",ResNode.STATUS_SECONDARY,GameSettings.class,null);
		root.add(node);
		}

	private static void iterateSprites(ProjectFileContext c, NodeList sprList, ResNode node)
		{
		ProjectFile f = c.f;

		for (int i = 0; i < sprList.getLength(); i++)
			{
			Node cNode = sprList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("sprites")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Sprite.class,null);
				node.add(rnode);
				iterateSprites(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("sprite")) //$NON-NLS-1$
				{
				Sprite spr = f.resMap.getList(Sprite.class).add();
				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				spr.setName(fileName);
				rnode = new ResNode(spr.getName(),ResNode.STATUS_SECONDARY,Sprite.class,spr.reference);
				node.add(rnode);
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document sprdoc = GMXFileReader.parseDocumentChecked(f, path + ".sprite.gmx"); //$NON-NLS-1$
				if (sprdoc == null) continue;

				spr.put(PSprite.ORIGIN_X,
						Integer.parseInt(sprdoc.getElementsByTagName("xorig").item(0).getTextContent())); //$NON-NLS-1$
				spr.put(PSprite.ORIGIN_Y,
						Integer.parseInt(sprdoc.getElementsByTagName("yorigin").item(0).getTextContent())); //$NON-NLS-1$
				spr.put(
						PSprite.SHAPE,
						ProjectFile.SPRITE_MASK_SHAPE[Integer.parseInt(sprdoc.getElementsByTagName("colkind").item( //$NON-NLS-1$
								0).getTextContent())]);
				spr.put(PSprite.SEPARATE_MASK,
						Integer.parseInt(sprdoc.getElementsByTagName("sepmasks").item(0).getTextContent()) != 0); //$NON-NLS-1$
				spr.put(
						PSprite.BB_MODE,
						ProjectFile.SPRITE_BB_MODE[Integer.parseInt(sprdoc.getElementsByTagName("bboxmode").item( //$NON-NLS-1$
								0).getTextContent())]);
				spr.put(PSprite.BB_LEFT,
						Integer.parseInt(sprdoc.getElementsByTagName("bbox_left").item(0).getTextContent())); //$NON-NLS-1$
				spr.put(PSprite.BB_RIGHT,
						Integer.parseInt(sprdoc.getElementsByTagName("bbox_right").item(0).getTextContent())); //$NON-NLS-1$
				spr.put(PSprite.BB_TOP,
						Integer.parseInt(sprdoc.getElementsByTagName("bbox_top").item(0).getTextContent())); //$NON-NLS-1$
				spr.put(PSprite.BB_BOTTOM,
						Integer.parseInt(sprdoc.getElementsByTagName("bbox_bottom").item(0).getTextContent())); //$NON-NLS-1$
				spr.put(PSprite.ALPHA_TOLERANCE,
						Integer.parseInt(sprdoc.getElementsByTagName("coltolerance").item(0).getTextContent())); //$NON-NLS-1$

				spr.put(PSprite.TILE_HORIZONTALLY,
						Integer.parseInt(sprdoc.getElementsByTagName("HTile").item(0).getTextContent()) != 0); //$NON-NLS-1$
				spr.put(PSprite.TILE_VERTICALLY,
						Integer.parseInt(sprdoc.getElementsByTagName("VTile").item(0).getTextContent()) != 0); //$NON-NLS-1$

				// TODO: Read texture groups

				spr.put(PSprite.FOR3D,
						Integer.parseInt(sprdoc.getElementsByTagName("For3D").item(0).getTextContent()) != 0); //$NON-NLS-1$

				// TODO: Just extra metadata stored in the GMX by studio
				//int width = Integer.parseInt(sprdoc.getElementsByTagName("width").item(0).getTextContent());
				//int height = Integer.parseInt(sprdoc.getElementsByTagName("height").item(0).getTextContent());

				// iterate and load the sprites subimages
				NodeList frList = sprdoc.getElementsByTagName("frame"); //$NON-NLS-1$
				path = f.getDirectory() + "/sprites/"; //$NON-NLS-1$
				for (int ii = 0; ii < frList.getLength(); ii++)
					{
					Node fnode = frList.item(ii);
					BufferedImage img = null;
					File imgfile = new File(path + Util.getPOSIXPath(fnode.getTextContent()));
					if (imgfile.exists())
						{
						try
							{
							img = ImageIO.read(imgfile);
							spr.subImages.add(img);
							}
						catch (IOException e)
							{
							LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "failed to read: " + imgfile.getAbsolutePath(), e));
							}
						}
					}
				}
			}
		}

	private static void readSprites(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Sprite.class),ResNode.STATUS_PRIMARY,
				Sprite.class,null);
		root.add(node);

		NodeList sprList = in.getElementsByTagName("sprites"); //$NON-NLS-1$
		if (sprList.getLength() > 0)
			{
			sprList = sprList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateSprites(c,sprList,node);
		}

	private static void iterateSounds(ProjectFileContext c, NodeList sndList, ResNode node)
		{
		ProjectFile f = c.f;

		for (int i = 0; i < sndList.getLength(); i++)
			{
			Node cNode = sndList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("sounds")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Sound.class,null);
				node.add(rnode);
				iterateSounds(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("sound")) //$NON-NLS-1$
				{
				Sound snd = f.resMap.getList(Sound.class).add();
				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				snd.setName(fileName);
				rnode = new ResNode(snd.getName(),ResNode.STATUS_SECONDARY,Sound.class,snd.reference);
				node.add(rnode);
				snd.setNode(rnode);
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document snddoc = GMXFileReader.parseDocumentChecked(f, path + ".sound.gmx"); //$NON-NLS-1$
				if (snddoc == null) continue;

				snd.put(PSound.FILE_NAME,snddoc.getElementsByTagName("origname").item(0).getTextContent()); //$NON-NLS-1$
				// GMX uses double nested tags for volume, bit rate, sample rate, type, and bit depth
				// There is a special clause here, every one of those tags after volume, the nested
				// tag is singular, where its parent is plural.
				NodeList nl = snddoc.getElementsByTagName("volume"); //$NON-NLS-1$
				snd.put(PSound.VOLUME,Double.parseDouble(nl.item(nl.getLength() - 1).getTextContent()));
				snd.put(PSound.PAN,
					Double.parseDouble(snddoc.getElementsByTagName("pan").item(0).getTextContent())); //$NON-NLS-1$
				snd.put(PSound.BIT_RATE,
					Integer.parseInt(snddoc.getElementsByTagName("bitRate").item(0).getTextContent())); //$NON-NLS-1$
				snd.put(PSound.SAMPLE_RATE,
					Integer.parseInt(snddoc.getElementsByTagName("sampleRate").item(0).getTextContent())); //$NON-NLS-1$
				int sndtype = Integer.parseInt(snddoc.getElementsByTagName("type").item(0).getTextContent()); //$NON-NLS-1$
				snd.put(PSound.TYPE, ProjectFile.SOUND_TYPE[sndtype]);
				snd.put(PSound.BIT_DEPTH,
					Integer.parseInt(snddoc.getElementsByTagName("bitDepth").item(0).getTextContent())); //$NON-NLS-1$
				snd.put(PSound.PRELOAD,
					Integer.parseInt(snddoc.getElementsByTagName("preload").item(0).getTextContent()) != 0); //$NON-NLS-1$
				snd.put(PSound.COMPRESSED,
					Integer.parseInt(snddoc.getElementsByTagName("compressed").item(0).getTextContent()) != 0); //$NON-NLS-1$
				snd.put(PSound.STREAMED,
					Integer.parseInt(snddoc.getElementsByTagName("streamed").item(0).getTextContent()) != 0); //$NON-NLS-1$
				snd.put(PSound.DECOMPRESS_ON_LOAD,
					Integer.parseInt(snddoc.getElementsByTagName("uncompressOnLoad").item(0).getTextContent()) != 0); //$NON-NLS-1$
				int sndkind = Integer.parseInt(snddoc.getElementsByTagName("kind").item(0).getTextContent()); //$NON-NLS-1$
				snd.put(PSound.KIND,ProjectFile.SOUND_KIND[sndkind]);
				snd.put(PSound.FILE_TYPE,snddoc.getElementsByTagName("extension").item(0).getTextContent()); //$NON-NLS-1$
				NodeList data = snddoc.getElementsByTagName("data"); //$NON-NLS-1$
				if (data.item(0) != null)
					{
					String fname = data.item(0).getTextContent();
					fname = f.getDirectory() + "/sound/audio/" + fname;
					try
						{
						snd.data = Util.readFully(fname); //$NON-NLS-1$
						}
					catch (IOException e)
						{
						LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "failed to read: " + fname, e));
						}
					}
				}
			}
		}

	private static void readSounds(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Sound.class),ResNode.STATUS_PRIMARY,
				Sound.class,null);
		root.add(node);

		NodeList sndList = in.getElementsByTagName("sounds"); //$NON-NLS-1$
		if (sndList.getLength() > 0)
			{
			sndList = sndList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateSounds(c,sndList,node);
		}

	private static void iterateBackgrounds(ProjectFileContext c, NodeList bkgList, ResNode node)
		{
		ProjectFile f = c.f;

		for (int i = 0; i < bkgList.getLength(); i++)
			{
			Node cNode = bkgList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("backgrounds")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Background.class,null);
				node.add(rnode);
				iterateBackgrounds(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("background")) //$NON-NLS-1$
				{
				Background bkg = f.resMap.getList(Background.class).add();
				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				bkg.setName(fileName);
				rnode = new ResNode(bkg.getName(),ResNode.STATUS_SECONDARY,Background.class,bkg.reference);
				node.add(rnode);
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document bkgdoc = GMXFileReader.parseDocumentChecked(f, path + ".background.gmx"); //$NON-NLS-1$
				if (bkgdoc == null) continue;

				bkg.put(PBackground.USE_AS_TILESET,
						Integer.parseInt(bkgdoc.getElementsByTagName("istileset").item(0).getTextContent()) != 0); //$NON-NLS-1$
				bkg.put(PBackground.TILE_WIDTH,
						Integer.parseInt(bkgdoc.getElementsByTagName("tilewidth").item(0).getTextContent())); //$NON-NLS-1$
				bkg.put(PBackground.TILE_HEIGHT,
						Integer.parseInt(bkgdoc.getElementsByTagName("tileheight").item(0).getTextContent())); //$NON-NLS-1$
				bkg.put(PBackground.H_OFFSET,
						Integer.parseInt(bkgdoc.getElementsByTagName("tilexoff").item(0).getTextContent())); //$NON-NLS-1$
				bkg.put(PBackground.V_OFFSET,
						Integer.parseInt(bkgdoc.getElementsByTagName("tileyoff").item(0).getTextContent())); //$NON-NLS-1$
				bkg.put(PBackground.H_SEP,
						Integer.parseInt(bkgdoc.getElementsByTagName("tilehsep").item(0).getTextContent())); //$NON-NLS-1$
				bkg.put(PBackground.V_SEP,
						Integer.parseInt(bkgdoc.getElementsByTagName("tilevsep").item(0).getTextContent())); //$NON-NLS-1$
				bkg.put(PBackground.TILE_HORIZONTALLY,
						Integer.parseInt(bkgdoc.getElementsByTagName("HTile").item(0).getTextContent()) != 0); //$NON-NLS-1$
				bkg.put(PBackground.TILE_VERTICALLY,
						Integer.parseInt(bkgdoc.getElementsByTagName("VTile").item(0).getTextContent()) != 0); //$NON-NLS-1$

				// TODO: Read texture groups

				bkg.put(PBackground.FOR3D,
						Integer.parseInt(bkgdoc.getElementsByTagName("For3D").item(0).getTextContent()) != 0); //$NON-NLS-1$

				// NOTE: Just extra metadata stored in the GMX by studio
				//int width = Integer.parseInt(bkgdoc.getElementsByTagName("width").item(0).getTextContent());
				//int height = Integer.parseInt(bkgdoc.getElementsByTagName("height").item(0).getTextContent());

				path = f.getDirectory() + "/background/"; //$NON-NLS-1$
				Node fnode = bkgdoc.getElementsByTagName("data").item(0); //$NON-NLS-1$
				BufferedImage img = null;
				File imgfile = new File(path + Util.getPOSIXPath(fnode.getTextContent()));
				if (imgfile.exists())
					{
					try
						{
						img = ImageIO.read(imgfile);
						bkg.setBackgroundImage(img);
						}
					catch (IOException e)
						{
						LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "failed to read: " + imgfile.getAbsolutePath(), e));
						}
					}
				}
			}
		}

	private static void readBackgrounds(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Background.class),
				ResNode.STATUS_PRIMARY,Background.class,null);
		root.add(node);

		NodeList bkgList = in.getElementsByTagName("backgrounds"); //$NON-NLS-1$
		if (bkgList.getLength() > 0)
			{
			bkgList = bkgList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateBackgrounds(c,bkgList,node);
		}

	private static void iteratePaths(ProjectFileContext c, NodeList pthList, ResNode node)
		{
		final ProjectFile f = c.f;

		for (int i = 0; i < pthList.getLength(); i++)
			{
			Node cNode = pthList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("paths")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Path.class,null);
				node.add(rnode);
				iteratePaths(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("path")) //$NON-NLS-1$
				{
				final Path pth = f.resMap.getList(Path.class).add();
				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				pth.setName(fileName);
				rnode = new ResNode(pth.getName(),ResNode.STATUS_SECONDARY,Path.class,pth.reference);
				node.add(rnode);
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document pthdoc = GMXFileReader.parseDocumentChecked(f, path + ".path.gmx"); //$NON-NLS-1$
				if (pthdoc == null) continue;

				pth.put(PPath.SMOOTH,
						Integer.parseInt(pthdoc.getElementsByTagName("kind").item(0).getTextContent()) != 0); //$NON-NLS-1$
				pth.put(PPath.PRECISION,
						Integer.parseInt(pthdoc.getElementsByTagName("precision").item(0).getTextContent())); //$NON-NLS-1$
				pth.put(PPath.CLOSED,
						Integer.parseInt(pthdoc.getElementsByTagName("closed").item(0).getTextContent()) != 0); //$NON-NLS-1$
				final int backroom = Integer.parseInt(pthdoc.getElementsByTagName("backroom").item(0).getTextContent()); //$NON-NLS-1$

				if (backroom >= 0)
					{
					PostponedRef pr = new PostponedRef()
						{
						public boolean invoke()
							{
							ResourceList<Room> list = f.resMap.getList(Room.class);
							if (list == null)
								{
								return false;
								}

							Room rmn = list.getUnsafe(backroom);
							if (rmn == null)
								{
								return false;
								}

							pth.put(PPath.BACKGROUND_ROOM,rmn.reference);
							return true;
							}
						};
						postpone.add(pr);
					}

				pth.put(PPath.SNAP_X,
						Integer.parseInt(pthdoc.getElementsByTagName("hsnap").item(0).getTextContent())); //$NON-NLS-1$
				pth.put(PPath.SNAP_Y,
						Integer.parseInt(pthdoc.getElementsByTagName("vsnap").item(0).getTextContent())); //$NON-NLS-1$

				// iterate and add each path point
				NodeList frList = pthdoc.getElementsByTagName("point"); //$NON-NLS-1$
				for (int ii = 0; ii < frList.getLength(); ii++)
					{
					Node fnode = frList.item(ii);
					String[] coords = fnode.getTextContent().split(","); //$NON-NLS-1$
					pth.points.add(new PathPoint(Integer.parseInt(coords[0]),Integer.parseInt(coords[1]),
							Integer.parseInt(coords[2])));
					}
				}
			}
		}

	private static void readPaths(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Path.class),ResNode.STATUS_PRIMARY,
				Path.class,null);
		root.add(node);

		NodeList pthList = in.getElementsByTagName("paths"); //$NON-NLS-1$
		if (pthList.getLength() > 0)
			{
			pthList = pthList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iteratePaths(c,pthList,node);
		}

	private static void iterateScripts(ProjectFileContext c, NodeList scrList, ResNode node)
		{
		ProjectFile f = c.f;

		for (int i = 0; i < scrList.getLength(); i++)
			{
			Node cNode = scrList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("scripts")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Script.class,null);
				node.add(rnode);
				iterateScripts(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("script")) //$NON-NLS-1$
				{
				Script scr = f.resMap.getList(Script.class).add();
				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				scr.setName(fileName.substring(0,fileName.lastIndexOf('.')));
				rnode = new ResNode(scr.getName(),ResNode.STATUS_SECONDARY,Script.class,scr.reference);
				node.add(rnode);
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				try (BufferedReader reader = new BufferedReader(new FileReader(path))) 
					{
					String code = ""; //$NON-NLS-1$
					String line = reader.readLine();
					if (line == null) continue;
					if (line.startsWith("#define")) //$NON-NLS-1$
						{
						line = reader.readLine();
						if (line == null) continue;
						}
					do 
						{
						if (line.startsWith("#define")) //$NON-NLS-1$
							{
							scr.put(PScript.CODE,code);
							code = ""; //$NON-NLS-1$

							scr = f.resMap.getList(Script.class).add();
							scr.setName(line.substring(8, line.length()));
							rnode = new ResNode(scr.getName(),ResNode.STATUS_SECONDARY,Script.class,scr.reference);
							node.add(rnode);
							}
						else
							code += line + '\n';
						}
					while ((line = reader.readLine()) != null);

					scr.put(PScript.CODE,code);
					}
				catch (FileNotFoundException e)
					{
					LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "file not found: " + path, e));
					}
				catch (IOException e)
					{
					LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "unable to read file: " + path, e));
					}
				}
			}

		}

	private static void readScripts(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Script.class),ResNode.STATUS_PRIMARY,
				Script.class,null);
		root.add(node);

		NodeList scrList = in.getElementsByTagName("scripts"); //$NON-NLS-1$
		if (scrList.getLength() > 0)
			{
			scrList = scrList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateScripts(c,scrList,node);
		}

	private static void iterateShaders(ProjectFileContext c, NodeList shrList, ResNode node)
		{
		ProjectFile f = c.f;

		for (int i = 0; i < shrList.getLength(); i++)
			{
			Node cNode = shrList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("shaders")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Shader.class,null);
				node.add(rnode);
				iterateScripts(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("shader")) //$NON-NLS-1$
				{
				Shader shr = f.resMap.getList(Shader.class).add();
				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				shr.setName(fileName.substring(0,fileName.lastIndexOf('.')));
				rnode = new ResNode(shr.getName(),ResNode.STATUS_SECONDARY,Shader.class,shr.reference);
				node.add(rnode);
				shr.put(PShader.TYPE,cNode.getAttributes().item(0).getTextContent());
				String code = ""; //$NON-NLS-1$
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				try (BufferedReader reader = new BufferedReader(new FileReader(path))) 
					{
					String line = ""; //$NON-NLS-1$
					while ((line = reader.readLine()) != null)
						{
						code += line + '\n';
						}
					}
				catch (FileNotFoundException e)
					{
					LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "file not found: " + path, e));
					}
				catch (IOException e)
					{
					LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "unable to read file: " + path, e));
					}

				String[] splitcode = code.split(STUPID_SHADER_MARKER);
				shr.put(PShader.VERTEX,splitcode[0]);
				shr.put(PShader.FRAGMENT,splitcode[1]);
				}
			}

		}

	private static void readShaders(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Shader.class),ResNode.STATUS_PRIMARY,
				Shader.class,null);
		root.add(node);

		NodeList shrList = in.getElementsByTagName("shaders"); //$NON-NLS-1$
		if (shrList.getLength() > 0)
			{
			shrList = shrList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateShaders(c,shrList,node);
		}

	private static void iterateFonts(ProjectFileContext c, NodeList fntList, ResNode node)
		{
		ProjectFile f = c.f;

		for (int i = 0; i < fntList.getLength(); i++)
			{
			Node cNode = fntList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("fonts")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Font.class,null);
				node.add(rnode);
				iterateFonts(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("font")) //$NON-NLS-1$
				{
				Font fnt = f.resMap.getList(Font.class).add();
				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				fnt.setName(fileName);
				rnode = new ResNode(fnt.getName(),ResNode.STATUS_SECONDARY,Font.class,fnt.reference);
				node.add(rnode);
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document fntdoc = GMXFileReader.parseDocumentChecked(f, path + ".font.gmx"); //$NON-NLS-1$
				if (fntdoc == null) continue;

				fnt.put(PFont.FONT_NAME,fntdoc.getElementsByTagName("name").item(0).getTextContent()); //$NON-NLS-1$
				fnt.put(PFont.SIZE,
						Integer.parseInt(fntdoc.getElementsByTagName("size").item(0).getTextContent())); //$NON-NLS-1$
				fnt.put(PFont.BOLD,
						Integer.parseInt(fntdoc.getElementsByTagName("bold").item(0).getTextContent()) != 0); //$NON-NLS-1$
				fnt.put(PFont.ITALIC,
						Integer.parseInt(fntdoc.getElementsByTagName("italic").item(0).getTextContent()) != 0); //$NON-NLS-1$
				fnt.put(PFont.CHARSET,
						Integer.parseInt(fntdoc.getElementsByTagName("charset").item(0).getTextContent())); //$NON-NLS-1$
				fnt.put(PFont.ANTIALIAS,
						Integer.parseInt(fntdoc.getElementsByTagName("aa").item(0).getTextContent())); //$NON-NLS-1$
				NodeList ranges = fntdoc.getElementsByTagName("range0"); //$NON-NLS-1$
				for (int item = 0; item < ranges.getLength(); item++)
					{
					String[] range = ranges.item(item).getTextContent().split(","); //$NON-NLS-1$
					fnt.addRange(Integer.parseInt(range[0]),Integer.parseInt(range[1]));
					}

				NodeList glyphs = fntdoc.getElementsByTagName("glyph"); //$NON-NLS-1$
				for (int item = 0; item < glyphs.getLength(); item++)
					{
					NamedNodeMap attribs = glyphs.item(item).getAttributes();
					GlyphMetric gm = fnt.addGlyph();
					gm.properties.put(PGlyphMetric.CHARACTER,
							Integer.parseInt(attribs.getNamedItem("character").getTextContent())); //$NON-NLS-1$
					gm.properties.put(PGlyphMetric.X,
							Integer.parseInt(attribs.getNamedItem("x").getTextContent())); //$NON-NLS-1$
					gm.properties.put(PGlyphMetric.Y,
							Integer.parseInt(attribs.getNamedItem("y").getTextContent())); //$NON-NLS-1$
					gm.properties.put(PGlyphMetric.W,
							Integer.parseInt(attribs.getNamedItem("w").getTextContent())); //$NON-NLS-1$
					gm.properties.put(PGlyphMetric.H,
							Integer.parseInt(attribs.getNamedItem("h").getTextContent())); //$NON-NLS-1$
					gm.properties.put(PGlyphMetric.SHIFT,
							Integer.parseInt(attribs.getNamedItem("shift").getTextContent())); //$NON-NLS-1$
					gm.properties.put(PGlyphMetric.OFFSET,
							Integer.parseInt(attribs.getNamedItem("offset").getTextContent())); //$NON-NLS-1$
					}
				}
			}

		}

	private static void readFonts(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Font.class),ResNode.STATUS_PRIMARY,
				Font.class,null);
		root.add(node);

		NodeList fntList = in.getElementsByTagName("fonts"); //$NON-NLS-1$
		if (fntList.getLength() > 0)
			{
			fntList = fntList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateFonts(c,fntList,node);
		}

	private static void iterateTimelines(ProjectFileContext c, NodeList tmlList, ResNode node)
		{
		ProjectFile f = c.f;

		for (int i = 0; i < tmlList.getLength(); i++)
			{
			Node cNode = tmlList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("timelines")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Timeline.class,null);
				node.add(rnode);
				}
			else if (cname.equals("timeline")) //$NON-NLS-1$
				{
				//ResourceReference<Timeline> r = c.timeids.get(i); //includes ID
				//Timeline tml = r.get();
				//f.resMap.getList(Timeline.class).add(tml);

				Timeline tml = f.resMap.getList(Timeline.class).add();

				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				tml.setName(fileName);
				rnode = new ResNode(tml.getName(),ResNode.STATUS_SECONDARY,Timeline.class,tml.reference);
				node.add(rnode);
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document tmldoc = GMXFileReader.parseDocumentChecked(f, path + ".timeline.gmx"); //$NON-NLS-1$
				if (tmldoc == null) continue;

				//Iterate the moments and load the actions
				NodeList frList = tmldoc.getElementsByTagName("entry"); //$NON-NLS-1$
				for (int ii = 0; ii < frList.getLength(); ii++)
					{
					Node fnode = frList.item(ii);
					Moment mom = tml.addMoment();

					NodeList children = fnode.getChildNodes();
					for (int x = 0; x < children.getLength(); x++)
						{
						Node cnode = children.item(x);
						if (cnode.getNodeName().equals("#text")) //$NON-NLS-1$
							{
							continue;
							}
						else if (cnode.getNodeName().equals("step")) //$NON-NLS-1$
							{
							mom.stepNo = Integer.parseInt(cnode.getTextContent());
							}
						else if (cnode.getNodeName().equals("event")) //$NON-NLS-1$
							{
							readActions(c,mom,"INTIMELINEACTION",i,mom.stepNo,cnode.getChildNodes()); //$NON-NLS-1$
							}
						}
					}
				}
			iterateTimelines(c,cNode.getChildNodes(),rnode);
			}

		}

	private static void readTimelines(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Timeline.class),ResNode.STATUS_PRIMARY,
				Timeline.class,null);
		root.add(node);

		NodeList tmlList = in.getElementsByTagName("timelines"); //$NON-NLS-1$
		if (tmlList.getLength() > 0)
			{
			tmlList = tmlList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateTimelines(c,tmlList,node);
		}

	private static void iterateGmObjects(ProjectFileContext c, NodeList objList, ResNode node)
		{
		final ProjectFile f = c.f;

		for (int i = 0; i < objList.getLength(); i++)
			{
			Node cNode = objList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}
			ResNode rnode = null;

			if (cname.equals("objects")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						GmObject.class,null);
				node.add(rnode);
				iterateGmObjects(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("object")) //$NON-NLS-1$
				{
				//ResourceReference<GmObject> r = c.objids.get(int); //includes ID
				//final GmObject obj = r.get();
				//f.resMap.getList(GmObject.class).add(obj);

				final GmObject obj = f.resMap.getList(GmObject.class).add();

				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				obj.setName(fileName);

				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document objdoc = GMXFileReader.parseDocumentChecked(f, path + ".object.gmx"); //$NON-NLS-1$
				if (objdoc == null) continue;

				final String sprname = objdoc.getElementsByTagName("spriteName").item(0).getTextContent(); //$NON-NLS-1$
				if (!sprname.equals("<undefined>"))
					{
					postpone.add(new DefaultPostponedRef(f.resMap.getList(Sprite.class), obj.properties, PGmObject.SPRITE, sprname));
					}
				else
					{
					obj.put(PGmObject.SPRITE,null);
					}

				final String mskname = objdoc.getElementsByTagName("maskName").item(0).getTextContent(); //$NON-NLS-1$
				if (!mskname.equals("<undefined>"))
					{
					postpone.add(new DefaultPostponedRef(f.resMap.getList(Sprite.class), obj.properties, PGmObject.MASK, mskname));
					}
				else
					{
					obj.put(PGmObject.MASK,null);
					}

				final String parname = objdoc.getElementsByTagName("parentName").item(0).getTextContent(); //$NON-NLS-1$
				if (!parname.equals("<undefined>") && !parname.equals("self"))
					{
					postpone.add(new DefaultPostponedRef(f.resMap.getList(GmObject.class), obj.properties, PGmObject.PARENT, parname));
					}
				else
					{
					obj.put(PGmObject.PARENT,null);
					}

				obj.put(PGmObject.SOLID,
						Integer.parseInt(objdoc.getElementsByTagName("solid").item(0).getTextContent()) != 0); //$NON-NLS-1$
				obj.put(PGmObject.VISIBLE,
						Integer.parseInt(objdoc.getElementsByTagName("visible").item(0).getTextContent()) != 0); //$NON-NLS-1$
				obj.put(PGmObject.DEPTH,
						Integer.parseInt(objdoc.getElementsByTagName("depth").item(0).getTextContent())); //$NON-NLS-1$
				obj.put(
						PGmObject.PERSISTENT,
						Integer.parseInt(objdoc.getElementsByTagName("persistent").item(0).getTextContent()) != 0); //$NON-NLS-1$

				// Now that properties are loaded iterate the events and load the actions
				NodeList frList = objdoc.getElementsByTagName("event"); //$NON-NLS-1$
				for (int ii = 0; ii < frList.getLength(); ii++)
					{
					Node fnode = frList.item(ii);
					final Event ev = new Event();

					ev.mainId = Integer.parseInt(fnode.getAttributes().getNamedItem("eventtype").getTextContent()); //$NON-NLS-1$
					MainEvent me = obj.mainEvents.get(ev.mainId);
					me.events.add(0,ev);
					if (ev.mainId == MainEvent.EV_COLLISION)
						{
						final String colname = fnode.getAttributes().getNamedItem("ename").getTextContent(); //$NON-NLS-1$
						PostponedRef pr = new PostponedRef()
							{
								public boolean invoke()
									{
									ResourceList<GmObject> list = f.resMap.getList(GmObject.class);
									if (list == null)
										{
										return false;
										}
									GmObject col = list.get(colname);
									if (col == null)
										{
										return false;
										}
									ev.other = col.reference;
									return true;
									}
							};
						postpone.add(pr);
						}
					else
						{
						ev.id = Integer.parseInt(fnode.getAttributes().getNamedItem("enumb").getTextContent()); //$NON-NLS-1$
						}
					readActions(c,ev,"INOBJECTACTION",i,ii * 1000 + ev.id,fnode.getChildNodes()); //$NON-NLS-1$
					}
				obj.put(
						PGmObject.PHYSICS_OBJECT,
						Integer.parseInt(objdoc.getElementsByTagName("PhysicsObject").item(0).getTextContent()) != 0); //$NON-NLS-1$
				obj.put(
						PGmObject.PHYSICS_SENSOR,
						Integer.parseInt(objdoc.getElementsByTagName("PhysicsObjectSensor").item(0).getTextContent()) != 0); //$NON-NLS-1$
				int shapekind = Integer.parseInt(objdoc.getElementsByTagName("PhysicsObjectShape").item(0).getTextContent()); //$NON-NLS-1$
				obj.put(PGmObject.PHYSICS_SHAPE,ProjectFile.PHYSICS_SHAPE[shapekind]);
				obj.put(
						PGmObject.PHYSICS_DENSITY,
						Double.parseDouble(objdoc.getElementsByTagName("PhysicsObjectDensity").item(0).getTextContent())); //$NON-NLS-1$
				obj.put(
						PGmObject.PHYSICS_RESTITUTION,
						Double.parseDouble(objdoc.getElementsByTagName("PhysicsObjectRestitution").item(0).getTextContent())); //$NON-NLS-1$
				obj.put(
						PGmObject.PHYSICS_GROUP,
						Integer.parseInt(objdoc.getElementsByTagName("PhysicsObjectGroup").item(0).getTextContent())); //$NON-NLS-1$
				obj.put(
						PGmObject.PHYSICS_DAMPING_LINEAR,
						Double.parseDouble(objdoc.getElementsByTagName("PhysicsObjectLinearDamping").item(0).getTextContent())); //$NON-NLS-1$
				obj.put(
						PGmObject.PHYSICS_DAMPING_ANGULAR,
						Double.parseDouble(objdoc.getElementsByTagName("PhysicsObjectAngularDamping").item(0).getTextContent())); //$NON-NLS-1$
				// TODO: Some versions of the format did not have all of the physics properties.
				Node fNode = objdoc.getElementsByTagName("PhysicsObjectFriction").item(0); //$NON-NLS-1$
				if (fNode != null)
					{
					obj.put(PGmObject.PHYSICS_FRICTION,Double.parseDouble(fNode.getTextContent()));
					obj.put(
							PGmObject.PHYSICS_AWAKE,
							Integer.parseInt(objdoc.getElementsByTagName("PhysicsObjectAwake").item(0).getTextContent()) != 0); //$NON-NLS-1$
					obj.put(
							PGmObject.PHYSICS_KINEMATIC,
							Integer.parseInt(objdoc.getElementsByTagName("PhysicsObjectKinematic").item(0).getTextContent()) != 0); //$NON-NLS-1$
					}

				NodeList pointNodes = objdoc.getElementsByTagName("point"); //$NON-NLS-1$
				for (int p = 0; p < pointNodes.getLength(); p++)
					{
					String[] coords = pointNodes.item(p).getTextContent().split(","); //$NON-NLS-1$
					obj.shapePoints.add(new ShapePoint(Integer.parseInt(coords[0]),
							Integer.parseInt(coords[1])));
					}

				rnode = new ResNode(obj.getName(),ResNode.STATUS_SECONDARY,GmObject.class,obj.reference);
				node.add(rnode);
				}
			}
		}

	private static void readGmObjects(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;
		ProjectFile f = c.f;
		ResNode node = new ResNode("Objects",ResNode.STATUS_PRIMARY,GmObject.class,null); //$NON-NLS-1$
		root.add(node);

		NodeList objList = in.getElementsByTagName("objects"); //$NON-NLS-1$
		if (objList.getLength() > 0)
			{
			objList = objList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateGmObjects(c,objList,node);

		f.resMap.getList(GmObject.class).lastId = objList.getLength() - 1;
		}

	private static void iterateRooms(ProjectFileContext c, NodeList rmnList, ResNode node)
		{
		final ProjectFile f = c.f;

		for (int i = 0; i < rmnList.getLength(); i++)
			{
			Node cNode = rmnList.item(i);
			String cname = cNode.getNodeName();
			if (cname.equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			ResNode rnode = null;

			if (cname.equals("rooms")) //$NON-NLS-1$
				{
				rnode = new ResNode(cNode.getAttributes().item(0).getTextContent(),ResNode.STATUS_GROUP,
						Room.class,null);
				node.add(rnode);
				iterateRooms(c,cNode.getChildNodes(),rnode);
				}
			else if (cname.equals("room")) //$NON-NLS-1$
				{
				//ResourceReference<Room> r = c.rmids.get(i); //includes ID
				//Room rmn = r.get();
				//f.resMap.getList(Room.class).add(rmn);
				Room rmn = f.resMap.getList(Room.class).add();

				String fileName = new File(Util.getPOSIXPath(cNode.getTextContent())).getName();
				rmn.setName(fileName);
				rnode = new ResNode(rmn.getName(),ResNode.STATUS_SECONDARY,Room.class,rmn.reference);
				node.add(rnode);
				String path = f.getDirectory() + '/' + Util.getPOSIXPath(cNode.getTextContent());

				Document rmndoc = GMXFileReader.parseDocumentChecked(f, path + ".room.gmx"); //$NON-NLS-1$
				if (rmndoc == null) continue;

				String caption = rmndoc.getElementsByTagName("caption").item(0).getTextContent(); //$NON-NLS-1$
				rmn.put(PRoom.CAPTION,caption);

				NodeList cnodes = rmndoc.getElementsByTagName("room").item(0).getChildNodes(); //$NON-NLS-1$
				for (int x = 0; x < cnodes.getLength(); x++)
					{
					Node pnode = cnodes.item(x);
					String pname = pnode.getNodeName();
					if (pname.equals("#text")) //$NON-NLS-1$
						{
						continue;
						}
					else if (pname.equals("caption")) //$NON-NLS-1$
						{
						rmn.put(PRoom.CAPTION,pnode.getTextContent());
						}
					else if (pname.equals("width")) //$NON-NLS-1$
						{
						rmn.put(PRoom.WIDTH,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("height")) //$NON-NLS-1$
						{
						rmn.put(PRoom.HEIGHT,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("vsnap")) //$NON-NLS-1$
						{
						rmn.put(PRoom.SNAP_Y,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("hsnap")) //$NON-NLS-1$
						{
						rmn.put(PRoom.SNAP_X,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("isometric")) //$NON-NLS-1$
						{
						rmn.put(PRoom.ISOMETRIC,Integer.parseInt(pnode.getTextContent()) != 0);
						}
					else if (pname.equals("speed")) //$NON-NLS-1$
						{
						rmn.put(PRoom.SPEED,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("persistent")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PERSISTENT,Integer.parseInt(pnode.getTextContent()) != 0);
						}
					else if (pname.equals("colour")) //$NON-NLS-1$
						{
						int col = Integer.parseInt(pnode.getTextContent());
						rmn.put(PRoom.BACKGROUND_COLOR,Util.convertGmColor(col));
						}
					else if (pname.equals("showcolour")) //$NON-NLS-1$
						{
						rmn.put(PRoom.DRAW_BACKGROUND_COLOR,Integer.parseInt(pnode.getTextContent()) != 0);
						}
					else if (pname.equals("code")) //$NON-NLS-1$
						{
						rmn.put(PRoom.CREATION_CODE,pnode.getTextContent());
						}
					else if (pname.equals("enableViews")) //$NON-NLS-1$
						{
						rmn.put(PRoom.VIEWS_ENABLED,Integer.parseInt(pnode.getTextContent()) != 0);
						}
					else if (pname.equals("clearViewBackground")) //$NON-NLS-1$
						{
						rmn.put(PRoom.VIEWS_CLEAR,Integer.parseInt(pnode.getTextContent()) != 0);
						}
					else if (pname.equals("makerSettings")) //$NON-NLS-1$
						{
						NodeList msnodes = pnode.getChildNodes();
						for (int y = 0; y < msnodes.getLength(); y++)
							{
							Node mnode = msnodes.item(y);
							String mname = mnode.getNodeName();
							if (mname.equals("#text")) //$NON-NLS-1$
								{
								continue;
								}
							else if (mname.equals("isSet")) //$NON-NLS-1$
								{
								rmn.put(PRoom.REMEMBER_WINDOW_SIZE,Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("w")) //$NON-NLS-1$
								{
								rmn.put(PRoom.EDITOR_WIDTH,Integer.parseInt(mnode.getTextContent()));
								}
							else if (mname.equals("h")) //$NON-NLS-1$
								{
								rmn.put(PRoom.EDITOR_HEIGHT,Integer.parseInt(mnode.getTextContent()));
								}
							else if (mname.equals("showGrid")) //$NON-NLS-1$
								{
								rmn.put(PRoom.SHOW_GRID,Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("showObjects")) //$NON-NLS-1$
								{
								rmn.put(PRoom.SHOW_OBJECTS,Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("showTiles")) //$NON-NLS-1$
								{
								rmn.put(PRoom.SHOW_TILES,Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("showBackgrounds")) //$NON-NLS-1$
								{
								rmn.put(PRoom.SHOW_BACKGROUNDS,Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("showForegrounds")) //$NON-NLS-1$
								{
								rmn.put(PRoom.SHOW_FOREGROUNDS,Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("showViews")) //$NON-NLS-1$
								{
								rmn.put(PRoom.SHOW_VIEWS,Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("deleteUnderlyingObj")) //$NON-NLS-1$
								{
								rmn.put(PRoom.DELETE_UNDERLYING_OBJECTS,
										Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("deleteUnderlyingTiles")) //$NON-NLS-1$
								{
								rmn.put(PRoom.DELETE_UNDERLYING_TILES,Integer.parseInt(mnode.getTextContent()) != 0);
								}
							else if (mname.equals("page")) //$NON-NLS-1$
								{
								rmn.put(PRoom.CURRENT_TAB,Integer.parseInt(mnode.getTextContent()));
								}
							else if (mname.equals("xoffset")) //$NON-NLS-1$
								{
								rmn.put(PRoom.SCROLL_BAR_X,Integer.parseInt(mnode.getTextContent()));
								}
							else if (mname.equals("yoffset")) //$NON-NLS-1$
								{
								rmn.put(PRoom.SCROLL_BAR_Y,Integer.parseInt(mnode.getTextContent()));
								}
							}
						}
					else if (pname.equals("backgrounds")) //$NON-NLS-1$
						{
						NodeList bgnodes = pnode.getChildNodes();
						int bkgnum = 0;
						for (int y = 0; y < bgnodes.getLength(); y++)
							{
							Node bnode = bgnodes.item(y);
							String bname = bnode.getNodeName();
							if (bname.equals("#text")) //$NON-NLS-1$
								{
								continue;
								}
							final BackgroundDef bkg = rmn.backgroundDefs.get(bkgnum);
							bkgnum += 1;

							bkg.properties.put(
									PBackgroundDef.VISIBLE,
									Integer.parseInt(bnode.getAttributes().getNamedItem("visible").getTextContent()) != 0); //$NON-NLS-1$
							final String bkgname = bnode.getAttributes().getNamedItem("name").getTextContent(); //$NON-NLS-1$

							postpone.add(new DefaultPostponedRef(f.resMap.getList(Background.class), bkg.properties, PBackgroundDef.BACKGROUND, bkgname));

							bkg.properties.put(
									PBackgroundDef.FOREGROUND,
									Integer.parseInt(bnode.getAttributes().getNamedItem("foreground").getTextContent()) != 0); //$NON-NLS-1$
							bkg.properties.put(
									PBackgroundDef.TILE_HORIZ,
									Integer.parseInt(bnode.getAttributes().getNamedItem("htiled").getTextContent()) != 0); //$NON-NLS-1$
							bkg.properties.put(
									PBackgroundDef.TILE_VERT,
									Integer.parseInt(bnode.getAttributes().getNamedItem("vtiled").getTextContent()) != 0); //$NON-NLS-1$
							bkg.properties.put(
									PBackgroundDef.STRETCH,
									Integer.parseInt(bnode.getAttributes().getNamedItem("stretch").getTextContent()) != 0); //$NON-NLS-1$
							bkg.properties.put(PBackgroundDef.H_SPEED,
									Integer.parseInt(bnode.getAttributes().getNamedItem("hspeed").getTextContent())); //$NON-NLS-1$
							bkg.properties.put(PBackgroundDef.V_SPEED,
									Integer.parseInt(bnode.getAttributes().getNamedItem("vspeed").getTextContent())); //$NON-NLS-1$
							bkg.properties.put(PBackgroundDef.X,
									Integer.parseInt(bnode.getAttributes().getNamedItem("x").getTextContent())); //$NON-NLS-1$
							bkg.properties.put(PBackgroundDef.Y,
									Integer.parseInt(bnode.getAttributes().getNamedItem("y").getTextContent())); //$NON-NLS-1$

							}
						}
					else if (pname.equals("views")) //$NON-NLS-1$
						{
						NodeList vinodes = pnode.getChildNodes();
						int viewnum = 0;
						for (int y = 0; y < vinodes.getLength(); y++)
							{
							Node vnode = vinodes.item(y);
							String vname = vnode.getNodeName();
							if (vname.equals("#text")) //$NON-NLS-1$
								{
								continue;
								}
							final View vw = rmn.views.get(viewnum);
							viewnum += 1;

							vw.properties.put(
									PView.VISIBLE,
									Integer.parseInt(vnode.getAttributes().getNamedItem("visible").getTextContent()) != 0); //$NON-NLS-1$
							final String objname = vnode.getAttributes().getNamedItem("objName").getTextContent(); //$NON-NLS-1$

							postpone.add(new DefaultPostponedRef(f.resMap.getList(GmObject.class), vw.properties, PView.OBJECT, objname));

							vw.properties.put(PView.SPEED_H,
									Integer.parseInt(vnode.getAttributes().getNamedItem("hspeed").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.SPEED_V,
									Integer.parseInt(vnode.getAttributes().getNamedItem("vspeed").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.BORDER_H,
									Integer.parseInt(vnode.getAttributes().getNamedItem("hborder").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.BORDER_V,
									Integer.parseInt(vnode.getAttributes().getNamedItem("vborder").getTextContent())); //$NON-NLS-1$

							vw.properties.put(PView.PORT_H,
									Integer.parseInt(vnode.getAttributes().getNamedItem("hport").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.PORT_W,
									Integer.parseInt(vnode.getAttributes().getNamedItem("wport").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.PORT_X,
									Integer.parseInt(vnode.getAttributes().getNamedItem("xport").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.PORT_Y,
									Integer.parseInt(vnode.getAttributes().getNamedItem("yport").getTextContent())); //$NON-NLS-1$

							vw.properties.put(PView.VIEW_H,
									Integer.parseInt(vnode.getAttributes().getNamedItem("hview").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.VIEW_W,
									Integer.parseInt(vnode.getAttributes().getNamedItem("wview").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.VIEW_X,
									Integer.parseInt(vnode.getAttributes().getNamedItem("xview").getTextContent())); //$NON-NLS-1$
							vw.properties.put(PView.VIEW_Y,
									Integer.parseInt(vnode.getAttributes().getNamedItem("yview").getTextContent())); //$NON-NLS-1$
							}
						}
					else if (pname.equals("instances")) //$NON-NLS-1$
						{
						NodeList insnodes = pnode.getChildNodes();
						for (int y = 0; y < insnodes.getLength(); y++)
							{
							Node inode = insnodes.item(y);
							String iname = inode.getNodeName();
							if (iname.equals("#text")) //$NON-NLS-1$
								{
								continue;
								}
							else if (iname.equals("instance") && inode.getAttributes().getLength() > 0) //$NON-NLS-1$
								{
								Instance inst = rmn.addInstance();

								// TODO: Replace this with DelayedRef
								String objname = inode.getAttributes().getNamedItem("objName").getTextContent(); //$NON-NLS-1$

								// because of the way this is set up, sprites must be loaded before objects
								GmObject temp = f.resMap.getList(GmObject.class).get(objname);
								if (temp != null) inst.properties.put(PInstance.OBJECT,temp.reference);
								NamedNodeMap attribs = inode.getAttributes();
								int xx = Integer.parseInt(attribs.getNamedItem("x").getNodeValue()); //$NON-NLS-1$
								int yy = Integer.parseInt(attribs.getNamedItem("y").getNodeValue()); //$NON-NLS-1$
								double sx = Double.parseDouble(attribs.getNamedItem("scaleX").getNodeValue()); //$NON-NLS-1$
								double sy = Double.parseDouble(attribs.getNamedItem("scaleY").getNodeValue()); //$NON-NLS-1$

								// Read the color blending
								if (attribs.getNamedItem("colour") != null) //$NON-NLS-1$
									{
									long col = Long.parseLong(attribs.getNamedItem("colour").getNodeValue()); //$NON-NLS-1$
									Color color = Util.convertInstanceColorWithAlpha((int) col);
									inst.setColor(color);
									inst.setAlpha(color.getAlpha());
									}

								double rot = Double.parseDouble(attribs.getNamedItem("rotation").getNodeValue()); //$NON-NLS-1$
								inst.properties.put(PInstance.NAME, inode.getAttributes().getNamedItem("name").getNodeValue()); //$NON-NLS-1$

								// NOTE: Because LGM still supports GMK, we attempt to preserve the ID which Studio
								// will remove if it saves over the GMX, so see if the "id" attribute we added is
								// there otherwise make up a new ID.
								Node idNode = inode.getAttributes().getNamedItem("id"); //$NON-NLS-1$
								int instid;
								if (idNode != null) {
									instid = Integer.parseInt(idNode.getNodeValue());
									if (instid > f.lastInstanceId) {
										f.lastInstanceId = instid;
									}
								} else {
									instid = ++f.lastInstanceId;
								}

								inst.properties.put(PInstance.ID, instid);

								inst.setPosition(new Point(xx,yy));
								inst.setScale(new Point2D.Double(sx,sy));
								inst.setRotation(rot);
								inst.setCreationCode(inode.getAttributes().getNamedItem("code").getNodeValue()); //$NON-NLS-1$
								inst.setLocked(Integer.parseInt(inode.getAttributes().getNamedItem("locked").getNodeValue()) != 0); //$NON-NLS-1$
								}
							}
						}
					else if (pname.equals("tiles")) //$NON-NLS-1$
						{
						NodeList tinodes = pnode.getChildNodes();
						for (int p = 0; p < tinodes.getLength(); p++)
							{
							Node tnode = tinodes.item(p);
							String tname = tnode.getNodeName();
							if (tname.equals("#text")) //$NON-NLS-1$
								{
								continue;
								}
							final Tile tile = new Tile(rmn);

							NamedNodeMap attribs = tnode.getAttributes();

							tile.setPosition(new Point(
									Integer.parseInt(attribs.getNamedItem("x").getTextContent()), //$NON-NLS-1$
									Integer.parseInt(attribs.getNamedItem("y").getTextContent()))); //$NON-NLS-1$

							final String bkgname = tnode.getAttributes().getNamedItem("bgName").getTextContent(); //$NON-NLS-1$
							postpone.add(new DefaultPostponedRef(f.resMap.getList(Background.class), tile.properties, PTile.BACKGROUND, bkgname));

							tile.properties.put(PTile.NAME, attribs.getNamedItem("name").getNodeValue()); //$NON-NLS-1$

							int tileid = Integer.parseInt(attribs.getNamedItem("id").getTextContent()); //$NON-NLS-1$
							if (tileid > f.lastTileId) {
								f.lastTileId = tileid;
							}
							tile.properties.put(PTile.ID,tileid);

							tile.setBackgroundPosition(new Point(
									Integer.parseInt(attribs.getNamedItem("xo").getTextContent()), //$NON-NLS-1$
									Integer.parseInt(attribs.getNamedItem("yo").getTextContent()))); //$NON-NLS-1$
							tile.setSize(new Dimension(
									Integer.parseInt(attribs.getNamedItem("w").getTextContent()), //$NON-NLS-1$
									Integer.parseInt(attribs.getNamedItem("h").getTextContent()))); //$NON-NLS-1$
							tile.setDepth(Integer.parseInt(attribs.getNamedItem("depth").getTextContent())); //$NON-NLS-1$

							tile.setLocked(Integer.parseInt(attribs.getNamedItem("locked").getTextContent()) != 0); //$NON-NLS-1$

							double sx = Double.parseDouble(attribs.getNamedItem("scaleX").getNodeValue()); //$NON-NLS-1$
							double sy = Double.parseDouble(attribs.getNamedItem("scaleY").getNodeValue()); //$NON-NLS-1$
							tile.setScale(new Point2D.Double(sx,sy));
							tile.setColor(Long.parseLong(attribs.getNamedItem("colour").getNodeValue())); //$NON-NLS-1$

							rmn.tiles.add(tile);
							}
						}
					else if (pname.equals("PhysicsWorld")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PHYSICS_WORLD,Integer.parseInt(pnode.getTextContent()) != 0);
						}
					else if (pname.equals("PhysicsWorldTop")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PHYSICS_TOP,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("PhysicsWorldLeft")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PHYSICS_LEFT,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("PhysicsWorldRight")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PHYSICS_RIGHT,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("PhysicsWorldBottom")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PHYSICS_BOTTOM,Integer.parseInt(pnode.getTextContent()));
						}
					else if (pname.equals("PhysicsWorldGravityX")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PHYSICS_GRAVITY_X,Double.parseDouble(pnode.getTextContent()));
						}
					else if (pname.equals("PhysicsWorldGravityY")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PHYSICS_GRAVITY_Y,Double.parseDouble(pnode.getTextContent()));
						}
					else if (pname.equals("PhysicsWorldPixToMeters")) //$NON-NLS-1$
						{
						rmn.put(PRoom.PHYSICS_PIXTOMETERS,Double.parseDouble(pnode.getTextContent()));
						}
					}
				}
			}

		}

	private static void readRooms(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Room.class), ResNode.STATUS_PRIMARY,
			Room.class, null);
		root.add(node);

		NodeList rmnList = in.getElementsByTagName("rooms"); //$NON-NLS-1$
		if (rmnList.getLength() > 0)
			{
			rmnList = rmnList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		iterateRooms(c,rmnList,node);
		}

	private static void readIncludedFiles(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Include.class), ResNode.STATUS_PRIMARY,
			Include.class, null);
		root.add(node);

		NodeList incList = in.getElementsByTagName("includes"); //$NON-NLS-1$
		if (incList.getLength() > 0)
			{
			incList = incList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		//iterateIncludes(c, incList, node);
		}

	private static void readPackages(ProjectFileContext c, ResNode root)
		{

		//iteratePackages(c, extList, node);
		ExtensionPackages extpkgs = c.f.extPackages;
		ResNode node = new ResNode(Resource.kindNamesPlural.get(ExtensionPackages.class),
				ResNode.STATUS_SECONDARY,ExtensionPackages.class,extpkgs.reference);
		root.add(node);
		}

	private static void readExtensions(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		ResNode node = new ResNode(Resource.kindNamesPlural.get(Extension.class),
				ResNode.STATUS_PRIMARY,ExtensionPackages.class,null);
		root.add(node);

		NodeList extList = in.getElementsByTagName("extensions"); //$NON-NLS-1$
		if (extList.getLength() > 0)
			{
			extList = extList.item(0).getChildNodes();
			}
		else
			{
			return;
			}
		//iterateExtensions(c, extList, node);
		}

	private static void readConstants(Constants cnsts, Node node)
		{
		if (node == null) return;
		int count = Integer.valueOf(node.getAttributes().getNamedItem("number").getNodeValue()); //$NON-NLS-1$
		List<Constant> newList = new ArrayList<Constant>(count);
		NodeList cnstNodes = node.getChildNodes();
		for (int i = 0; i < cnstNodes.getLength(); i++)
			{
			Node cnstNode = cnstNodes.item(i);
			if (!cnstNode.getNodeName().equals("constant")) //$NON-NLS-1$
				{
				continue;
				}
			String name = cnstNode.getAttributes().getNamedItem("name").getTextContent(); //$NON-NLS-1$
			String value = cnstNode.getTextContent();
			newList.add(new Constant(name,value));
			}
		cnsts.constants = newList;
		}

	private static void readDefaultConstants(ProjectFileContext c, ResNode root)
		{
		readConstants(c.f.defaultConstants,c.in.getElementsByTagName("constants").item(0)); //$NON-NLS-1$
		ResNode node = new ResNode("Constants",ResNode.STATUS_SECONDARY,Constants.class,null); //$NON-NLS-1$
		root.add(node);
		}

	private static void readGameInformation(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;

		GameInformation gameInfo = c.f.gameInfo;

		NodeList rtfNodes = in.getElementsByTagName("rtf"); //$NON-NLS-1$
		if (rtfNodes.getLength() == 0)
			{
			return;
			}
		Node rtfNode = rtfNodes.item(rtfNodes.getLength() - 1);

		String path = c.f.getDirectory() + '/' + Util.getPOSIXPath(rtfNode.getTextContent());

		String text = ""; //$NON-NLS-1$

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) 
			{
			String line = ""; //$NON-NLS-1$
			while ((line = reader.readLine()) != null)
				{
				text += line + '\n';
				}
			}
		catch (FileNotFoundException e)
			{
			LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "file not found: " + path, e));
			}
		catch (IOException e)
			{
			LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "unable to read file: " + path, e));
			}

		gameInfo.put(PGameInformation.TEXT,text);

		ResNode node = new ResNode(Resource.kindNamesPlural.get(GameInformation.class),
				ResNode.STATUS_SECONDARY,GameInformation.class,gameInfo.reference);
		root.add(node);
		}

	private static void readActions(ProjectFileContext c, ActionContainer container, String errorKey,
			int format1, int format2, NodeList actList)
		{
		final ProjectFile f = c.f;

		for (int i = 0; i < actList.getLength(); i++)
			{
			Node actNode = actList.item(i);

			if (actNode.getNodeName().equals("#text")) //$NON-NLS-1$
				{
				continue;
				}

			int libid = 0;
			int actid = 0;
			byte kind = 0;
			boolean userelative = false;
			boolean isquestion = false;
			boolean isquestiontrue = false;
			boolean isrelative = false;
			boolean useapplyto = false;
			byte exectype = 0;

			String appliesto = ""; //$NON-NLS-1$
			String functionname = ""; // execInfo for if the action just calls an action function //$NON-NLS-1$
			String codestring = ""; // execInfo for if the action executes code //$NON-NLS-1$

			Argument[] args = null;

			LibAction la = null;

			NodeList propList = actNode.getChildNodes();
			for (int ii = 0; ii < propList.getLength(); ii++)
				{
				Node prop = propList.item(ii);

				if (prop.getNodeName().equals("#text")) //$NON-NLS-1$
					{
					continue;
					}

				if (prop.getNodeName().equals("libid")) //$NON-NLS-1$
					{
					libid = Integer.parseInt(prop.getTextContent());
					}
				else if (prop.getNodeName().equals("id")) //$NON-NLS-1$
					{
					actid = Integer.parseInt(prop.getTextContent());
					}
				else if (prop.getNodeName().equals("kind")) //$NON-NLS-1$
					{
					kind = Byte.parseByte(prop.getTextContent());
					}
				else if (prop.getNodeName().equals("userelative")) //$NON-NLS-1$
					{
					userelative = Integer.parseInt(prop.getTextContent()) != 0;
					}
				else if (prop.getNodeName().equals("relative")) //$NON-NLS-1$
					{
					isrelative = Integer.parseInt(prop.getTextContent()) != 0;
					}
				else if (prop.getNodeName().equals("isquestion")) //$NON-NLS-1$
					{
					isquestion = Integer.parseInt(prop.getTextContent()) != 0;
					}
				else if (prop.getNodeName().equals("isnot")) //$NON-NLS-1$
					{
					isquestiontrue = Integer.parseInt(prop.getTextContent()) != 0;
					}
				else if (prop.getNodeName().equals("useapplyto")) //$NON-NLS-1$
					{
					useapplyto = Integer.parseInt(prop.getTextContent()) != 0;
					}
				else if (prop.getNodeName().equals("exetype")) //$NON-NLS-1$
					{
					exectype = Byte.parseByte(prop.getTextContent());
					}
				else if (prop.getNodeName().equals("whoName")) //$NON-NLS-1$
					{
					appliesto = prop.getTextContent();
					}
				else if (prop.getNodeName().equals("functionname")) //$NON-NLS-1$
					{
					functionname = prop.getTextContent();
					}
				else if (prop.getNodeName().equals("codestring")) //$NON-NLS-1$
					{
					codestring = prop.getTextContent();
					}
				else if (prop.getNodeName().equals("arguments")) //$NON-NLS-1$
					{
					NodeList targList = prop.getChildNodes();

					List<Node> argList = new ArrayList<Node>();
					for (int x = 0; x < targList.getLength(); x++)
						{
						Node arg = targList.item(x);
						if (!arg.getNodeName().equals("#text")) //$NON-NLS-1$
							{
							argList.add(arg);
							}
						}

					args = new Argument[argList.size()];

					for (int x = 0; x < argList.size(); x++)
						{
						Node arg = argList.get(x);

						if (arg.getNodeName().equals("#text")) //$NON-NLS-1$
							{
							continue;
							}

						args[x] = new Argument((byte) 0);

						NodeList argproplist = arg.getChildNodes();
						for (int xx = 0; xx < argproplist.getLength(); xx++)
							{
							Node argprop = argproplist.item(xx);

							if (prop.getNodeName().equals("#text")) //$NON-NLS-1$
								{
								continue;
								}

							final String proptext = argprop.getTextContent();
							final Argument argument = args[x];
							if (argprop.getNodeName().equals("kind")) //$NON-NLS-1$
								{
								argument.kind = Byte.parseByte(argprop.getTextContent());
								}
							else if (argprop.getNodeName().equals("string")) //$NON-NLS-1$
								{
								argument.setVal(proptext);
								}
							else
								{

								Class<? extends Resource<?,?>> kindc = Argument.getResourceKind(argument.kind);
								if (kindc != null && Resource.class.isAssignableFrom(kindc)) try
									{
									PostponedRef pr = new PostponedRef()
										{
											public boolean invoke()
												{
												ResourceHolder<?> rh = f.resMap.get(Argument.getResourceKind(argument.kind));
												if (rh == null)
													{
													return false;
													}
												Resource<?,?> temp = null;
												if (rh instanceof ResourceList<?>)
													temp = ((ResourceList<?>) rh).get(proptext);
												else
													temp = rh.getResource();
												if (temp != null) argument.setRes(temp.reference);
												argument.setVal(proptext);
												return temp != null;
												}
										};
									postpone.add(pr);
									}
								catch (NumberFormatException e)
									{
									// Trying to ref a resource without a valid id number?
									// Fallback to strval (already set)
									}
								}
							}
						}
					}
				}

			la = LibManager.getLibAction(libid,actid);
			boolean unknownLib = la == null;
			// The libAction will have a null parent, among other things
			if (unknownLib)
				{
				la = new LibAction();
				la.id = actid;
				la.parentId = libid;
				la.actionKind = kind;
				// TODO: Maybe make this more agnostic?
				if (la.actionKind == Action.ACT_CODE)
					{
					la = LibManager.codeAction;
					}
				else
					{
					la.allowRelative = userelative;
					la.question = isquestion;
					la.canApplyTo = useapplyto;
					la.execType = exectype;
					if (la.execType == Action.EXEC_FUNCTION) la.execInfo = functionname;
					if (la.execType == Action.EXEC_CODE) la.execInfo = codestring;
					}
				if (args != null)
					{
					la.libArguments = new LibArgument[args.length];
					for (int b = 0; b < args.length; b++)
						{
						LibArgument argument = new LibArgument();
						argument.kind = args[b].kind;
						la.libArguments[b] = argument;
						}
					}
				}

			final Action act = container.addAction(la);
			if (appliesto.equals("self")) //$NON-NLS-1$
				{
				act.setAppliesTo(GmObject.OBJECT_SELF);
				}
			else if (appliesto.equals("other")) //$NON-NLS-1$
				{
				act.setAppliesTo(GmObject.OBJECT_OTHER);
				}
			else
				{
				final String objname = appliesto;
				PostponedRef pr = new PostponedRef()
					{
						public boolean invoke()
							{
							ResourceList<GmObject> list = f.resMap.getList(GmObject.class);
							if (list == null)
								{
								return false;
								}
							GmObject obj = list.get(objname);
							if (obj == null)
								{
								return false;
								}
							act.setAppliesTo(obj.reference);
							return true;
							}
					};
				postpone.add(pr);
				}

			act.setRelative(isrelative);
			if (args != null && args.length > 0)
				{
				act.setArguments(args);
				}
			act.setNot(isquestiontrue);
			}

		}

	}
