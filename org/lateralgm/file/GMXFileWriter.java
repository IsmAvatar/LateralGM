/**
* @file  GMXFileWriter.java
* @brief Class implementing a GMX file writer.
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

import static org.lateralgm.main.Util.deRef;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JProgressBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.Constants;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.resources.GameInformation.PGameInformation;
import org.lateralgm.resources.GameSettings;
import org.lateralgm.resources.GameSettings.PGameSettings;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Path.PPath;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
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
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.ActionContainer;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
import org.lateralgm.resources.sub.CharacterRange;
import org.lateralgm.resources.sub.CharacterRange.PCharacterRange;
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
import org.w3c.dom.Element;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

public final class GMXFileWriter
	{
	private static DocumentBuilderFactory documentBuilderFactory;
	private static DocumentBuilder documentBuilder;

	private GMXFileWriter()
		{
		}

	// Workaround for Parameter limit
	private static class ProjectFileContext
		{
		ProjectFile f;
		Document dom;

		public ProjectFileContext(ProjectFile f, Document d)
			{
			this.f = f;
			this.dom = d;
			}

		public ProjectFileContext copy()
			{
			return new ProjectFileContext(f,dom);
			}
		}

	public static void writeProjectFile(OutputStream os, ProjectFile f, ResNode rootRes)
			throws IOException,GmFormatException,TransformerException
		{
		f.format = ProjectFile.FormatFlavor.GMX;
		long savetime = System.currentTimeMillis();

		if (documentBuilderFactory == null)
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
		if (documentBuilder == null)
			try
				{
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				}
			catch (ParserConfigurationException pce)
				{
				throw new GmFormatException(f,pce);
				}
		Document dom = documentBuilder.newDocument();

		JProgressBar progressBar = LGM.getProgressDialogBar();
		progressBar.setMaximum(160);
		LGM.setProgressTitle(Messages.getString("ProgressDialog.GMX_SAVING")); //$NON-NLS-1$

		ProjectFileContext c = new ProjectFileContext(f,dom);
		Element root = dom.createElement("assets"); //$NON-NLS-1$
		LGM.setProgress(0,Messages.getString("ProgressDialog.SETTINGS")); //$NON-NLS-1$
		writeConfigurations(c,root,savetime);

		LGM.setProgress(10,Messages.getString("ProgressDialog.SPRITES")); //$NON-NLS-1$
		writeSprites(c,root);
		LGM.setProgress(20,Messages.getString("ProgressDialog.SOUNDS")); //$NON-NLS-1$
		writeSounds(c,root);
		LGM.setProgress(30,Messages.getString("ProgressDialog.BACKGROUNDS")); //$NON-NLS-1$
		writeBackgrounds(c,root);
		LGM.setProgress(40,Messages.getString("ProgressDialog.PATHS")); //$NON-NLS-1$
		writePaths(c,root);
		LGM.setProgress(50,Messages.getString("ProgressDialog.SCRIPTS")); //$NON-NLS-1$
		writeScripts(c,root);
		LGM.setProgress(60,Messages.getString("ProgressDialog.SHADERS")); //$NON-NLS-1$
		writeShaders(c,root);
		LGM.setProgress(70,Messages.getString("ProgressDialog.FONTS")); //$NON-NLS-1$
		writeFonts(c,root);
		LGM.setProgress(80,Messages.getString("ProgressDialog.TIMELINES")); //$NON-NLS-1$
		writeTimelines(c,root);
		LGM.setProgress(90,Messages.getString("ProgressDialog.OBJECTS")); //$NON-NLS-1$
		writeGmObjects(c,root);
		LGM.setProgress(100,Messages.getString("ProgressDialog.ROOMS")); //$NON-NLS-1$
		writeRooms(c,root);
		LGM.setProgress(110,Messages.getString("ProgressDialog.INCLUDEFILES")); //$NON-NLS-1$
		//writeIncludedFiles(c, root);
		LGM.setProgress(120,Messages.getString("ProgressDialog.PACKAGES")); //$NON-NLS-1$
		//writePackages(c, root);
		LGM.setProgress(130,Messages.getString("ProgressDialog.CONSTANTS")); //$NON-NLS-1$
		writeDefaultConstants(c, root);
		LGM.setProgress(140,Messages.getString("ProgressDialog.EXTENSIONS")); //$NON-NLS-1$
		//writeExtensions(c, root);
		LGM.setProgress(150,Messages.getString("ProgressDialog.GAMEINFORMATION")); //$NON-NLS-1$
		writeGameInformation(c,root);

		dom.appendChild(root);

		// Now take the serialized XML data and format and write it to the actual file
		LGM.setProgress(150,Messages.getString("ProgressDialog.DOCUMENT")); //$NON-NLS-1$
		try
			{
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
			tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

			// send DOM to file
			tr.transform(new DOMSource(dom),new StreamResult(os));
			}
		catch (TransformerException te)
			{
			throw new GmFormatException(f,te);
			}
		finally
			{
			// close up the stream and release the lock on the file
			os.close();
			}
		LGM.setProgress(160,Messages.getString("ProgressDialog.FINISHED")); //$NON-NLS-1$
		}

	private static Element createElement(Document dom, String name, String value)
		{
		Element ret = dom.createElement(name);
		ret.setTextContent(value);
		return ret;
		}

	// This is used to obtain the primary node for a resource type.
	// TODO: This is rather ugly and doesn't allow multiple primary nodes.
	private static ResNode getPrimaryNode(ResNode first)
		{
		while (first.status != ResNode.STATUS_PRIMARY)
			first = (ResNode) first.getParent();
		return first;
		}

	// This is used to store booleans since GMX uses -1 and 0 and other times false and true
	private static String boolToString(boolean bool)
		{
			return bool ? "-1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
		}

	public static <R extends Resource<R,?>> String getName(ResourceReference<R> ref)
		{
		return getName(ref,"<undefined>"); //$NON-NLS-1$
		}

	public static <R extends Resource<R,?>> String getName(ResourceReference<R> ref, String noneval)
		{
		Resource<?,?> res = deRef(ref);
		if (res != null && res instanceof InstantiableResource<?,?>)
			return ((InstantiableResource<?,?>) res).getName();
		else
			return noneval;
		}

	public static <R extends Resource<R,?>> int getId(ResourceReference<R> ref)
		{
		return getId(ref,-1);
		}

	public static <R extends Resource<R,?>> int getId(ResourceReference<R> ref, int noneval)
		{
		Resource<?,?> res = deRef(ref);
		if (res != null && res instanceof InstantiableResource<?,?>)
			return ((InstantiableResource<?,?>) res).getId();
		else
			return noneval;
		}

	public static void writeConfigurations(ProjectFileContext c, Element root, long savetime) throws IOException,
			TransformerException
		{
		Document mdom = c.dom;
		ProjectFile f = c.f;

		Element conNode = mdom.createElement("Configs"); //$NON-NLS-1$
		conNode.setAttribute("name","configs");  //$NON-NLS-1$//$NON-NLS-2$
		root.appendChild(conNode);

		for (GameSettings gs : LGM.currentFile.gameSettings) {
			Element setNode = mdom.createElement("Config"); //$NON-NLS-1$
			String configDir = "Configs\\" + gs.getName();
			setNode.setTextContent(configDir); //$NON-NLS-1$
			conNode.appendChild(setNode);

			Document dom = documentBuilder.newDocument();
			Element nconNode = dom.createElement("Config"); //$NON-NLS-1$
			dom.appendChild(nconNode);
			Element optNode = dom.createElement("Options"); //$NON-NLS-1$
			nconNode.appendChild(optNode);

			// For some odd reason these two settings are combined together.
			// 2147483649 - Both
			// 2147483648 - Software Vertex Processing only
			// 1 - Synchronization Only
			// 0 - None

			long syncvertex = 0;
			if (gs.get(PGameSettings.USE_SYNCHRONIZATION))
				{
				syncvertex += 1;
				}
			if (gs.get(PGameSettings.FORCE_SOFTWARE_VERTEX_PROCESSING))
				{
				syncvertex += 2147483648L;
				}
			optNode.appendChild(createElement(dom,"option_sync_vertex",Long.toString(syncvertex))); //$NON-NLS-1$

			optNode.appendChild(createElement(dom,"option_fullscreen", //$NON-NLS-1$
					gs.get(PGameSettings.START_FULLSCREEN).toString()));
			optNode.appendChild(createElement(dom,"option_sizeable", //$NON-NLS-1$
					gs.get(PGameSettings.ALLOW_WINDOW_RESIZE).toString()));
			optNode.appendChild(createElement(dom,"option_stayontop", //$NON-NLS-1$
					gs.get(PGameSettings.ALWAYS_ON_TOP).toString()));
			optNode.appendChild(createElement(dom,"option_aborterrors", //$NON-NLS-1$
					gs.get(PGameSettings.ABORT_ON_ERROR).toString()));

			optNode.appendChild(createElement(dom,"option_noscreensaver", //$NON-NLS-1$
					gs.get(PGameSettings.DISABLE_SCREENSAVERS).toString()));
			optNode.appendChild(createElement(dom,"option_showcursor", //$NON-NLS-1$
					gs.get(PGameSettings.DISPLAY_CURSOR).toString()));
			optNode.appendChild(createElement(dom,"option_displayerrors", //$NON-NLS-1$
					gs.get(PGameSettings.DISPLAY_ERRORS).toString()));
			optNode.appendChild(createElement(dom,"option_noborder", //$NON-NLS-1$
					gs.get(PGameSettings.DONT_DRAW_BORDER).toString()));
			optNode.appendChild(createElement(dom,"option_nobuttons", //$NON-NLS-1$
					gs.get(PGameSettings.DONT_SHOW_BUTTONS).toString()));
			optNode.appendChild(createElement(dom,"option_argumenterrors", //$NON-NLS-1$
					gs.get(PGameSettings.ERROR_ON_ARGS).toString()));
			optNode.appendChild(createElement(dom,"option_freeze", //$NON-NLS-1$
					gs.get(PGameSettings.FREEZE_ON_LOSE_FOCUS).toString()));

			optNode.appendChild(createElement(dom,"option_colordepth", //$NON-NLS-1$
					ProjectFile.GS_DEPTH_CODE.get(gs.get(PGameSettings.COLOR_DEPTH)).toString()));

			optNode.appendChild(createElement(dom,"option_frequency", //$NON-NLS-1$
					ProjectFile.GS_FREQ_CODE.get(gs.get(PGameSettings.FREQUENCY)).toString()));
			optNode.appendChild(createElement(dom,"option_resolution", //$NON-NLS-1$
					ProjectFile.GS_RESOL_CODE.get(gs.get(PGameSettings.RESOLUTION)).toString()));
			optNode.appendChild(createElement(dom,"option_changeresolution", //$NON-NLS-1$
					gs.get(PGameSettings.SET_RESOLUTION).toString()));
			optNode.appendChild(createElement(
					dom,
					"option_priority", //$NON-NLS-1$
					ProjectFile.GS_PRIORITY_CODE.get(gs.get(PGameSettings.GAME_PRIORITY)).toString()));

			optNode.appendChild(createElement(dom,"option_closeesc", //$NON-NLS-1$
					gs.get(PGameSettings.LET_ESC_END_GAME).toString()));
			optNode.appendChild(createElement(dom,"option_interpolate", //$NON-NLS-1$
					gs.get(PGameSettings.INTERPOLATE).toString()));
			optNode.appendChild(createElement(dom,"option_scale", //$NON-NLS-1$
					gs.get(PGameSettings.SCALING).toString()));
			optNode.appendChild(createElement(dom,"option_closeesc", //$NON-NLS-1$
					gs.get(PGameSettings.TREAT_CLOSE_AS_ESCAPE).toString()));
			gs.put(PGameSettings.LAST_CHANGED,ProjectFile.longTimeToGmTime(savetime));
			optNode.appendChild(createElement(dom,"option_lastchanged", //$NON-NLS-1$
					gs.get(PGameSettings.LAST_CHANGED).toString()));

			optNode.appendChild(createElement(dom,"option_gameid", //$NON-NLS-1$
					gs.get(PGameSettings.GAME_ID).toString()));
			String guid = HexBin.encode((byte[]) gs.get(PGameSettings.GAME_GUID));
			optNode.appendChild(createElement(dom,"option_gameguid", //$NON-NLS-1$
					'{' + guid.substring(0,8) + '-' + guid.substring(8,12) + '-' + guid.substring(12,16) + '-'
							+ guid.substring(16,20) + '-' + guid.substring(20,32) + '}'));

			optNode.appendChild(createElement(dom,"option_author", //$NON-NLS-1$
					(String) gs.get(PGameSettings.AUTHOR)));
			optNode.appendChild(createElement(dom,"option_version_company", //$NON-NLS-1$
					(String) gs.get(PGameSettings.COMPANY)));
			optNode.appendChild(createElement(dom,"option_version_copyright", //$NON-NLS-1$
					(String) gs.get(PGameSettings.COPYRIGHT)));
			optNode.appendChild(createElement(dom,"option_version_description", //$NON-NLS-1$
					(String) gs.get(PGameSettings.DESCRIPTION)));
			optNode.appendChild(createElement(dom,"option_version_product", //$NON-NLS-1$
					(String) gs.get(PGameSettings.PRODUCT)));
			optNode.appendChild(createElement(dom,"option_information", //$NON-NLS-1$
					(String) gs.get(PGameSettings.INFORMATION)));
			optNode.appendChild(createElement(dom,"option_version", //$NON-NLS-1$
					gs.get(PGameSettings.VERSION).toString()));
			optNode.appendChild(createElement(dom,"option_version_build", //$NON-NLS-1$
					gs.get(PGameSettings.VERSION_BUILD).toString()));
			optNode.appendChild(createElement(dom,"option_version_major", //$NON-NLS-1$
					gs.get(PGameSettings.VERSION_MAJOR).toString()));
			optNode.appendChild(createElement(dom,"option_version_minor", //$NON-NLS-1$
					gs.get(PGameSettings.VERSION_MINOR).toString()));
			optNode.appendChild(createElement(dom,"option_version_release", //$NON-NLS-1$
					gs.get(PGameSettings.VERSION_RELEASE).toString()));

			Element cce = dom.createElement("ConfigConstants"); //$NON-NLS-1$
			writeConstants(gs.constants, dom, cce);
			nconNode.appendChild(cce);

			String icoPath = configDir + "\\windows\\runner_icon.ico"; //$NON-NLS-1$
			optNode.appendChild(createElement(dom,"option_windows_game_icon",icoPath)); //$NON-NLS-1$

			icoPath = f.getDirectory() + '\\' + icoPath;
			File file = new File(Util.getPOSIXPath(icoPath)).getParentFile();
			file.mkdirs();

			FileOutputStream fos = new FileOutputStream(Util.getPOSIXPath(icoPath));
			((ICOFile) gs.get(PGameSettings.GAME_ICON)).write(fos);
			fos.close();

			fos = null;
			try
				{
				Transformer tr = TransformerFactory.newInstance().newTransformer();
				tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
				tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
				;
				tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

				file = new File(Util.getPOSIXPath(f.getDirectory() + "/Configs")); //$NON-NLS-1$
				file.mkdir();

				// send DOM to file
				fos = new FileOutputStream(Util.getPOSIXPath(f.getDirectory() + "/Configs/" + gs.getName() + ".config.gmx")); //$NON-NLS-1$ //$NON-NLS-2$
				tr.transform(new DOMSource(dom),new StreamResult(fos));
				}
			finally
				{
				fos.close();
				}
		}
		return;
		}

	public static void writeTriggers(ProjectFile f, ResNode root, int ver) throws IOException
		{
		// TODO: Implement
		}

	public static void writeConstants(Constants cnsts, Document dom, Element node) throws IOException
		{
			Element base = dom.createElement("constants"); //$NON-NLS-1$
			base.setAttribute("number",Integer.toString(cnsts.constants.size())); //$NON-NLS-1$
			for (Constant cnst : cnsts.constants) {
				Element celement = dom.createElement("constant"); //$NON-NLS-1$
				celement.setAttribute("name",cnst.name); //$NON-NLS-1$
				celement.setTextContent(cnst.value);;
				base.appendChild(celement);
			}
			node.appendChild(base);
		}

	public static void writeDefaultConstants(ProjectFileContext c, Element root) throws IOException
		{
			writeConstants(c.f.defaultConstants, c.dom, root);
		}

	private static void iterateSprites(ProjectFileContext c, ResNode root, Element node)
			throws IOException,TransformerException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("sprites"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateSprites(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("sprites"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateSprites(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Sprite spr = (Sprite) resNode.getRes().get();
					res = dom.createElement("sprite"); //$NON-NLS-1$
					String fname = f.getDirectory() + "\\sprites\\"; //$NON-NLS-1$
					res.setTextContent("sprites\\" + spr.getName()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(fname + "\\images")); //$NON-NLS-1$
					file.mkdirs();

					Document doc = documentBuilder.newDocument();

					Element sprroot = doc.createElement("sprite"); //$NON-NLS-1$
					doc.appendChild(sprroot);

					sprroot.appendChild(createElement(doc,"xorig",spr.get(PSprite.ORIGIN_X).toString())); //$NON-NLS-1$
					sprroot.appendChild(createElement(doc,"yorigin",spr.get(PSprite.ORIGIN_Y).toString())); //$NON-NLS-1$
					sprroot.appendChild(createElement(doc,"colkind", //$NON-NLS-1$
							ProjectFile.SPRITE_MASK_CODE.get(spr.get(PSprite.SHAPE)).toString()));
					sprroot.appendChild(createElement(doc,"sepmasks", //$NON-NLS-1$
							boolToString((Boolean) spr.get(PSprite.SEPARATE_MASK))));
					sprroot.appendChild(createElement(doc,"bbox_left",spr.get(PSprite.BB_LEFT).toString())); //$NON-NLS-1$
					sprroot.appendChild(createElement(doc,"bbox_right",spr.get(PSprite.BB_RIGHT).toString())); //$NON-NLS-1$
					sprroot.appendChild(createElement(doc,"bbox_top",spr.get(PSprite.BB_TOP).toString())); //$NON-NLS-1$
					sprroot.appendChild(createElement(doc,"bbox_bottom",spr.get(PSprite.BB_BOTTOM).toString())); //$NON-NLS-1$
					sprroot.appendChild(createElement(doc,"bboxmode", //$NON-NLS-1$
							ProjectFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)).toString()));
					sprroot.appendChild(createElement(doc,"coltolerance", //$NON-NLS-1$
							spr.get(PSprite.ALPHA_TOLERANCE).toString()));

					sprroot.appendChild(createElement(doc,"HTile", //$NON-NLS-1$
							boolToString((Boolean) spr.get(PSprite.TILE_HORIZONTALLY))));
					sprroot.appendChild(createElement(doc,"VTile", //$NON-NLS-1$
							boolToString((Boolean) spr.get(PSprite.TILE_VERTICALLY))));

					// TODO: Write texture groups

					sprroot.appendChild(createElement(doc,"For3D", //$NON-NLS-1$
							boolToString((Boolean) spr.get(PSprite.FOR3D))));

					int width = spr.getWidth(),
					height = spr.getHeight();

					sprroot.appendChild(createElement(doc,"width",Integer.toString(width))); //$NON-NLS-1$
					sprroot.appendChild(createElement(doc,"height",Integer.toString(height))); //$NON-NLS-1$

					Element frameroot = doc.createElement("frames"); //$NON-NLS-1$
					for (int j = 0; j < spr.subImages.size(); j++)
						{
						String framefname = "images\\" + spr.getName() + '_' + j + ".png";  //$NON-NLS-1$//$NON-NLS-2$
						File outputfile = new File(Util.getPOSIXPath(fname + framefname));
						Element frameNode = createElement(doc,"frame",framefname); //$NON-NLS-1$
						frameNode.setAttribute("index",Integer.toString(j)); //$NON-NLS-1$
						frameroot.appendChild(frameNode);
						BufferedImage sub = spr.subImages.get(j);
						// GMX does have a backwards compatibility property for transparency pixel so we write
						// the image with the transparency removed when that setting is checked
						ImageIO.write(
								(Boolean) spr.get(PSprite.TRANSPARENT) ? Util.getTransparentImage(sub) : sub,
								"png",outputfile); //$NON-NLS-1$
						}
					sprroot.appendChild(frameroot);

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
						tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + spr.getName() + ".sprite.gmx")); //$NON-NLS-1$
						tr.transform(new DOMSource(doc),new StreamResult(fos));
						}
					finally
						{
						fos.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeSprites(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document dom = c.dom;

		Element node = dom.createElement("sprites"); //$NON-NLS-1$
		node.setAttribute("name","sprites");  //$NON-NLS-1$//$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Sprite> sprList = c.f.resMap.getList(Sprite.class);
		if (sprList.size() == 0)
			{
			return;
			}

		iterateSprites(c,getPrimaryNode(sprList.first().getNode()),node);
		}

	private static void iterateSounds(ProjectFileContext c, ResNode root, Element node)
			throws IOException,TransformerException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("sounds"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateSounds(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("sounds"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateSounds(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Sound snd = (Sound) resNode.getRes().get();
					res = dom.createElement("sound"); //$NON-NLS-1$
					String fname = f.getDirectory() + "\\sound\\"; //$NON-NLS-1$
					res.setTextContent("sound\\" + snd.getName()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(fname + "\\audio")); //$NON-NLS-1$
					file.mkdirs();

					Document doc = documentBuilder.newDocument();

					Element sndroot = doc.createElement("sound"); //$NON-NLS-1$
					doc.appendChild(sndroot);

					// GMX uses double nested tags for volume, bit rate, sample rate, type, and bit depth
					// There is an exception to this however. In every one of those tags after volume the
					// nested tag is singular, where its parent is plural.
					String fileType = snd.get(PSound.FILE_TYPE).toString();
					String fileName = snd.getName() + fileType;
					sndroot.appendChild(createElement(doc,"extension",fileType)); //$NON-NLS-1$
					sndroot.appendChild(createElement(doc,"origname","sound\\audio\\" + fileName)); //$NON-NLS-1$
					sndroot.appendChild(createElement(doc,"kind", //$NON-NLS-1$
							ProjectFile.SOUND_KIND_CODE.get(snd.get(PSound.KIND)).toString()));

					Element volumeRoot = doc.createElement("volume"); //$NON-NLS-1$
					volumeRoot.appendChild(createElement(doc,"volume", snd.get(PSound.VOLUME).toString())); //$NON-NLS-1$
					sndroot.appendChild(volumeRoot);

					Element bitRateRoot = doc.createElement("bitRates"); //$NON-NLS-1$
					bitRateRoot.appendChild(createElement(doc,"bitRate", //$NON-NLS-1$
						snd.get(PSound.BIT_RATE).toString()));
					sndroot.appendChild(bitRateRoot);

					Element sampleRateRoot = doc.createElement("sampleRates"); //$NON-NLS-1$
					sampleRateRoot.appendChild(createElement(doc,"sampleRate", //$NON-NLS-1$
							snd.get(PSound.SAMPLE_RATE).toString()));
					sndroot.appendChild(sampleRateRoot);

					Element typesRoot = doc.createElement("types"); //$NON-NLS-1$
					typesRoot.appendChild(createElement(doc,"type", //$NON-NLS-1$
						ProjectFile.SOUND_TYPE_CODE.get(snd.get(PSound.TYPE)).toString()));
					sndroot.appendChild(typesRoot);

					Element bitDepthRoot = doc.createElement("bitDepths"); //$NON-NLS-1$
					bitDepthRoot.appendChild(createElement(doc,"bitDepth", //$NON-NLS-1$
							snd.get(PSound.BIT_DEPTH).toString()));
					sndroot.appendChild(bitDepthRoot);

					sndroot.appendChild(createElement(doc,"pan",snd.get(PSound.PAN).toString())); //$NON-NLS-1$
					sndroot.appendChild(createElement(doc,"preload", //$NON-NLS-1$
							boolToString((Boolean) snd.get(PSound.PRELOAD))));
					sndroot.appendChild(createElement(doc,"compressed", //$NON-NLS-1$
							boolToString((Boolean) snd.get(PSound.COMPRESSED))));
					sndroot.appendChild(createElement(doc,"streamed", //$NON-NLS-1$
							boolToString((Boolean) snd.get(PSound.STREAMED))));
					sndroot.appendChild(createElement(doc,"uncompressOnLoad", //$NON-NLS-1$
							boolToString((Boolean) snd.get(PSound.DECOMPRESS_ON_LOAD))));
					int effects = 0;
					int n = 1;
					for (PSound k : ProjectFile.SOUND_FX_FLAGS)
						{
						if (snd.get(k)) effects |= n;
						n <<= 1;
						}
					sndroot.appendChild(createElement(doc,"effects",Integer.toString(effects))); //$NON-NLS-1$

					sndroot.appendChild(createElement(doc,"data",fileName)); //$NON-NLS-1$
					Util.writeFully(Util.getPOSIXPath(fname + "audio/" + fileName),snd.data); //$NON-NLS-1$

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
						tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + resNode.getUserObject().toString()
								+ ".sound.gmx")); //$NON-NLS-1$
						tr.transform(new DOMSource(doc),new StreamResult(fos));
						}
					finally
						{
						fos.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeSounds(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document dom = c.dom;

		Element node = dom.createElement("sounds"); //$NON-NLS-1$
		node.setAttribute("name","sound"); //$NON-NLS-1$ //$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Sound> sndList = c.f.resMap.getList(Sound.class);
		if (sndList.size() == 0)
			{
			return;
			}
		iterateSounds(c,getPrimaryNode(sndList.first().getNode()),node);
		}

	private static void iterateBackgrounds(ProjectFileContext c, ResNode root, Element node)
			throws IOException,TransformerException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("backgrounds"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateBackgrounds(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("backgrounds"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateBackgrounds(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Background bkg = (Background) resNode.getRes().get();
					res = dom.createElement("background"); //$NON-NLS-1$
					String fname = f.getDirectory() + "\\background\\"; //$NON-NLS-1$
					res.setTextContent("background\\" + bkg.getName()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(fname + "\\images")); //$NON-NLS-1$
					file.mkdirs();

					Document doc = documentBuilder.newDocument();

					Element bkgroot = doc.createElement("background"); //$NON-NLS-1$
					doc.appendChild(bkgroot);

					bkgroot.appendChild(createElement(doc,"istileset", //$NON-NLS-1$
							boolToString((Boolean) bkg.get(PBackground.USE_AS_TILESET))));
					bkgroot.appendChild(createElement(doc,"tilewidth", //$NON-NLS-1$
							bkg.get(PBackground.TILE_WIDTH).toString()));
					bkgroot.appendChild(createElement(doc,"tileheight", //$NON-NLS-1$
							bkg.get(PBackground.TILE_HEIGHT).toString()));
					bkgroot.appendChild(createElement(doc,"tilexoff",bkg.get(PBackground.H_OFFSET).toString())); //$NON-NLS-1$
					bkgroot.appendChild(createElement(doc,"tileyoff",bkg.get(PBackground.V_OFFSET).toString())); //$NON-NLS-1$
					bkgroot.appendChild(createElement(doc,"tilehsep",bkg.get(PBackground.H_SEP).toString())); //$NON-NLS-1$
					bkgroot.appendChild(createElement(doc,"tilevsep",bkg.get(PBackground.V_SEP).toString())); //$NON-NLS-1$
					bkgroot.appendChild(createElement(doc,"HTile", //$NON-NLS-1$
							boolToString((Boolean) bkg.get(PBackground.TILE_HORIZONTALLY))));
					bkgroot.appendChild(createElement(doc,"VTile", //$NON-NLS-1$
							boolToString((Boolean) bkg.get(PBackground.TILE_VERTICALLY))));

					// TODO: Write texture groups

					bkgroot.appendChild(createElement(doc,"For3D", //$NON-NLS-1$
							boolToString((Boolean) bkg.get(PBackground.FOR3D))));

					int width = bkg.getWidth(),
					height = bkg.getHeight();

					bkgroot.appendChild(createElement(doc,"width",Integer.toString(width))); //$NON-NLS-1$
					bkgroot.appendChild(createElement(doc,"height",Integer.toString(height))); //$NON-NLS-1$

					bkgroot.appendChild(createElement(doc,"data","images\\" + bkg.getName() + ".png")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if (width > 0 && height > 0)
						{
						File outputfile = new File(Util.getPOSIXPath(fname + "images\\" + bkg.getName() + ".png")); //$NON-NLS-1$ //$NON-NLS-2$
						ImageIO.write(bkg.getBackgroundImage(),"png",outputfile); //$NON-NLS-1$
						}

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
						tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + resNode.getUserObject().toString()
								+ ".background.gmx")); //$NON-NLS-1$
						tr.transform(new DOMSource(doc),new StreamResult(fos));
						}
					finally
						{
						fos.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeBackgrounds(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document dom = c.dom;

		Element node = dom.createElement("backgrounds"); //$NON-NLS-1$
		node.setAttribute("name","background"); //$NON-NLS-1$ //$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Background> bkgList = c.f.resMap.getList(Background.class);
		if (bkgList.size() == 0)
			{
			return;
			}

		iterateBackgrounds(c,getPrimaryNode(bkgList.first().getNode()),node);
		}

	private static void iteratePaths(ProjectFileContext c, ResNode root, Element node)
			throws TransformerException,IOException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("paths"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iteratePaths(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("paths"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iteratePaths(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Path path = (Path) resNode.getRes().get();
					res = dom.createElement("path"); //$NON-NLS-1$
					String fname = f.getDirectory() + "\\paths\\"; //$NON-NLS-1$
					res.setTextContent("paths\\" + path.getName()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/paths")); //$NON-NLS-1$
					file.mkdir();

					Document doc = documentBuilder.newDocument();

					Element pathroot = doc.createElement("path"); //$NON-NLS-1$
					doc.appendChild(pathroot);

					int kind = path.get(PPath.SMOOTH) ? 1 : 0;
					pathroot.appendChild(createElement(doc,"kind",Integer.toString(kind))); //$NON-NLS-1$
					int closed = path.get(PPath.CLOSED) ? -1 : 0;
					pathroot.appendChild(createElement(doc,"closed",Integer.toString(closed))); //$NON-NLS-1$
					pathroot.appendChild(createElement(doc,"precision",path.get(PPath.PRECISION).toString())); //$NON-NLS-1$
					pathroot.appendChild(createElement(doc,"backroom", //$NON-NLS-1$
							Integer.toString(getId((ResourceReference<?>)path.get(PPath.BACKGROUND_ROOM)))));
					pathroot.appendChild(createElement(doc,"hsnap",path.get(PPath.SNAP_X).toString())); //$NON-NLS-1$
					pathroot.appendChild(createElement(doc,"vsnap",path.get(PPath.SNAP_Y).toString())); //$NON-NLS-1$

					Element rootpoint = doc.createElement("points"); //$NON-NLS-1$
					pathroot.appendChild(rootpoint);
					for (PathPoint p : path.points)
						{
						rootpoint.appendChild(createElement(doc,"point", //$NON-NLS-1$
								p.getX() + "," + p.getY() + ',' + p.getSpeed())); //$NON-NLS-1$
						}

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
						tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + path.getName() + ".path.gmx")); //$NON-NLS-1$
						tr.transform(new DOMSource(doc),new StreamResult(fos));
						}
					finally
						{
						fos.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writePaths(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document dom = c.dom;

		Element node = dom.createElement("paths"); //$NON-NLS-1$
		node.setAttribute("name","paths"); //$NON-NLS-1$ //$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Path> pthList = c.f.resMap.getList(Path.class);
		if (pthList.size() == 0)
			{
			return;
			}

		iteratePaths(c,getPrimaryNode(pthList.first().getNode()),node);
		}

	private static void iterateScripts(ProjectFileContext c, ResNode root, Element node)
			throws IOException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("scripts"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateScripts(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("scripts"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateScripts(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Script scr = (Script) resNode.getRes().get();
					res = dom.createElement("script"); //$NON-NLS-1$
					String fname = "scripts\\" + scr.getName() + ".gml"; //$NON-NLS-1$ //$NON-NLS-2$
					res.setTextContent(fname);
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/scripts")); //$NON-NLS-1$
					file.mkdir();
					Writer out = null;
					try
						{
						out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
								Util.getPOSIXPath(f.getDirectory() + '/' + Util.getPOSIXPath(fname))),"UTF-8")); //$NON-NLS-1$
						out.write((String) scr.properties.get(PScript.CODE));
						}
					finally
						{
						out.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeScripts(ProjectFileContext c, Element root) throws IOException
		{
		Document dom = c.dom;

		Element node = dom.createElement("scripts"); //$NON-NLS-1$
		node.setAttribute("name","scripts"); //$NON-NLS-1$ //$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Script> scrList = c.f.resMap.getList(Script.class);
		if (scrList.size() == 0)
			{
			return;
			}

		iterateScripts(c,getPrimaryNode(scrList.first().getNode()),node);
		}

	private static void iterateShaders(ProjectFileContext c, ResNode root, Element node)
			throws IOException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("shaders"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateShaders(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("shaders"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateShaders(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Shader shr = (Shader) resNode.getRes().get();
					res = dom.createElement("shader"); //$NON-NLS-1$
					String fname = "shaders\\" + shr.getName() + ".shader"; //$NON-NLS-1$ //$NON-NLS-2$
					res.setTextContent(fname);
					res.setAttribute("type",shr.properties.get(PShader.TYPE).toString()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/shaders")); //$NON-NLS-1$
					file.mkdir();
					Writer out = null;
					try
						{
						out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
								Util.getPOSIXPath(f.getDirectory() + '/' + fname)),"UTF-8"));
						String code = shr.properties.get(PShader.VERTEX)
								+ ('\n' + GMXFileReader.STUPID_SHADER_MARKER)
								+ shr.properties.get(PShader.FRAGMENT);
						out.write(code);
						}
					finally
						{
						out.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeShaders(ProjectFileContext c, Element root) throws IOException
		{
		Document dom = c.dom;

		Element node = dom.createElement("shaders"); //$NON-NLS-1$
		node.setAttribute("name","shaders"); //$NON-NLS-1$ //$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Shader> shrList = c.f.resMap.getList(Shader.class);
		if (shrList.size() == 0)
			{
			return;
			}
		iterateShaders(c,getPrimaryNode(shrList.first().getNode()),node);
		}

	private static void iterateFonts(ProjectFileContext c, ResNode root, Element node)
			throws IOException,TransformerException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("fonts"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateFonts(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("fonts"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateFonts(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Font fnt = (Font) resNode.getRes().get();
					res = dom.createElement("font"); //$NON-NLS-1$
					String fname = f.getDirectory() + "\\fonts\\"; //$NON-NLS-1$
					res.setTextContent("fonts\\" + fnt.getName()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(fname));
					file.mkdirs();

					Document doc = documentBuilder.newDocument();

					Element fntroot = doc.createElement("font"); //$NON-NLS-1$
					doc.appendChild(fntroot);

					fntroot.appendChild(createElement(doc,"name",fnt.get(PFont.FONT_NAME).toString())); //$NON-NLS-1$
					fntroot.appendChild(createElement(doc,"size",fnt.get(PFont.SIZE).toString())); //$NON-NLS-1$
					fntroot.appendChild(createElement(doc,"bold",boolToString((Boolean) fnt.get(PFont.BOLD)))); //$NON-NLS-1$
					fntroot.appendChild(createElement(doc,"italic", //$NON-NLS-1$
							boolToString((Boolean) fnt.get(PFont.ITALIC))));
					fntroot.appendChild(createElement(doc,"charset",fnt.get(PFont.CHARSET).toString())); //$NON-NLS-1$
					fntroot.appendChild(createElement(doc,"aa",fnt.get(PFont.ANTIALIAS).toString())); //$NON-NLS-1$

					Element rangeroot = doc.createElement("ranges"); //$NON-NLS-1$
					fntroot.appendChild(rangeroot);
					for (CharacterRange cr : fnt.characterRanges)
						{
						rangeroot.appendChild(createElement(
								doc,
								"range0", //$NON-NLS-1$
								cr.properties.get(PCharacterRange.RANGE_MIN) + "," //$NON-NLS-1$
										+ cr.properties.get(PCharacterRange.RANGE_MAX)));
						}

					Element glyphroot = doc.createElement("glyphs"); //$NON-NLS-1$
					fntroot.appendChild(glyphroot);
					for (GlyphMetric gm : fnt.glyphMetrics)
						{
						Element gelement = doc.createElement("glyph"); //$NON-NLS-1$
						gelement.setAttribute("character",gm.properties.get(PGlyphMetric.CHARACTER).toString()); //$NON-NLS-1$
						gelement.setAttribute("x",gm.properties.get(PGlyphMetric.X).toString()); //$NON-NLS-1$
						gelement.setAttribute("y",gm.properties.get(PGlyphMetric.Y).toString()); //$NON-NLS-1$
						gelement.setAttribute("w",gm.properties.get(PGlyphMetric.W).toString()); //$NON-NLS-1$
						gelement.setAttribute("h",gm.properties.get(PGlyphMetric.H).toString()); //$NON-NLS-1$
						gelement.setAttribute("shift",gm.properties.get(PGlyphMetric.SHIFT).toString()); //$NON-NLS-1$
						gelement.setAttribute("offset",gm.properties.get(PGlyphMetric.OFFSET).toString()); //$NON-NLS-1$
						glyphroot.appendChild(gelement);
						}

					// TODO: Move glyph renderer from the plugin to LGM and write glyphs here
					fntroot.appendChild(createElement(doc,"image",fnt.getName() + ".png")); //$NON-NLS-1$ //$NON-NLS-2$
					//File outputfile = new File(getUnixPath(fname + fnt.getName() + ".png"));
					/*
					try
						{
						ImageIO.write(fnt.getBackgroundImage(), "png", outputfile);
						}
					catch (IOException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
					*/

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
						tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + fnt.getName() + ".font.gmx")); //$NON-NLS-1$
						tr.transform(new DOMSource(doc),new StreamResult(fos));
						}
					finally
						{
						fos.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeFonts(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document dom = c.dom;

		Element node = dom.createElement("fonts"); //$NON-NLS-1$
		node.setAttribute("name","fonts"); //$NON-NLS-1$ //$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Font> fntList = c.f.resMap.getList(Font.class);
		if (fntList.size() == 0)
			{
			return;
			}
		iterateFonts(c,getPrimaryNode(fntList.first().getNode()),node);
		}

	private static void iterateTimelines(ProjectFileContext c, ResNode root, Element node)
			throws IOException,TransformerException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("timelines"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateTimelines(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("timelines"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateTimelines(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Timeline timeline = (Timeline) resNode.getRes().get();
					res = dom.createElement("timeline"); //$NON-NLS-1$
					String fname = f.getDirectory() + "\\timelines\\"; //$NON-NLS-1$
					res.setTextContent("timelines\\" + timeline.getName()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/timelines")); //$NON-NLS-1$
					file.mkdir();

					Document doc = documentBuilder.newDocument();

					Element tmlroot = doc.createElement("timeline"); //$NON-NLS-1$
					doc.appendChild(tmlroot);

					for (Moment mom : timeline.moments)
						{
						Element entroot = doc.createElement("entry"); //$NON-NLS-1$
						tmlroot.appendChild(entroot);
						entroot.appendChild(createElement(doc,"step",Integer.toString(mom.stepNo))); //$NON-NLS-1$
						Element evtroot = doc.createElement("event"); //$NON-NLS-1$
						entroot.appendChild(evtroot);
						writeActions(doc,evtroot,mom);
						}

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
						tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + timeline.getName() + ".timeline.gmx")); //$NON-NLS-1$
						tr.transform(new DOMSource(doc),new StreamResult(fos));
						}
					finally
						{
						fos.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeTimelines(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document dom = c.dom;

		Element node = dom.createElement("timelines"); //$NON-NLS-1$
		node.setAttribute("name","timelines"); //$NON-NLS-1$//$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Timeline> tmlList = c.f.resMap.getList(Timeline.class);
		if (tmlList.size() == 0)
			{
			return;
			}

		iterateTimelines(c,getPrimaryNode(tmlList.first().getNode()),node);
		}

	private static void iterateGmObjects(ProjectFileContext c, ResNode root, Element node)
			throws IOException,TransformerException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("objects"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateGmObjects(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("objects"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateGmObjects(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					GmObject object = (GmObject) resNode.getRes().get();
					res = dom.createElement("object"); //$NON-NLS-1$
					String fname = f.getDirectory() + "\\objects\\"; //$NON-NLS-1$
					res.setTextContent("objects\\" + object.getName()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/objects")); //$NON-NLS-1$
					file.mkdir();

					Document doc = documentBuilder.newDocument();

					Element objroot = doc.createElement("object"); //$NON-NLS-1$
					doc.appendChild(objroot);
					objroot.appendChild(createElement(doc,"spriteName", //$NON-NLS-1$
							getName((ResourceReference<?>)object.get(PGmObject.SPRITE))));
					objroot.appendChild(createElement(doc,"solid", //$NON-NLS-1$
							boolToString((Boolean) object.get(PGmObject.SOLID))));
					objroot.appendChild(createElement(doc,"visible", //$NON-NLS-1$
							boolToString((Boolean) object.get(PGmObject.VISIBLE))));
					objroot.appendChild(createElement(doc,"depth",object.get(PGmObject.DEPTH).toString())); //$NON-NLS-1$
					objroot.appendChild(createElement(doc,"persistent", //$NON-NLS-1$
							boolToString((Boolean) object.get(PGmObject.PERSISTENT))));
					objroot.appendChild(createElement(doc,"maskName", //$NON-NLS-1$
							getName((ResourceReference<?>)object.get(PGmObject.MASK))));
					objroot.appendChild(createElement(doc,"parentName", //$NON-NLS-1$
							getName((ResourceReference<?>)object.get(PGmObject.PARENT))));

					Element evtroot = doc.createElement("events"); //$NON-NLS-1$
					for (int i = 0; i < object.mainEvents.size(); i++)
						{
						MainEvent me = object.mainEvents.get(i);
						for (int k = me.events.size(); k > 0; k--)
							{
							Event ev = me.events.get(k - 1);
							Element evtelement = doc.createElement("event"); //$NON-NLS-1$
							evtelement.setAttribute("eventtype",Integer.toString(ev.mainId)); //$NON-NLS-1$
							if (ev.mainId == MainEvent.EV_COLLISION)
								{
								evtelement.setAttribute("ename", //$NON-NLS-1$
										getName((ResourceReference<GmObject>)ev.other));
								}
							else
								{
								evtelement.setAttribute("enumb",Integer.toString(ev.id)); //$NON-NLS-1$
								}
							evtroot.appendChild(evtelement);
							writeActions(doc,evtelement,ev);
							}
						}
					objroot.appendChild(evtroot);

					// Physics Properties
					objroot.appendChild(createElement(doc,"PhysicsObject", //$NON-NLS-1$
							boolToString((Boolean) object.get(PGmObject.PHYSICS_OBJECT))));
					objroot.appendChild(createElement(doc,"PhysicsObjectSensor", //$NON-NLS-1$
							boolToString((Boolean) object.get(PGmObject.PHYSICS_SENSOR))));
					objroot.appendChild(createElement(doc,"PhysicsObjectShape", //$NON-NLS-1$
							ProjectFile.SHAPE_CODE.get(object.get(PGmObject.PHYSICS_SHAPE)).toString()));
					objroot.appendChild(createElement(doc,"PhysicsObjectDensity", //$NON-NLS-1$
							Double.toString((Double) object.get(PGmObject.PHYSICS_DENSITY))));
					objroot.appendChild(createElement(doc,"PhysicsObjectRestitution", //$NON-NLS-1$
							Double.toString((Double) object.get(PGmObject.PHYSICS_RESTITUTION))));
					objroot.appendChild(createElement(doc,"PhysicsObjectGroup", //$NON-NLS-1$
							Integer.toString((Integer) object.get(PGmObject.PHYSICS_GROUP))));
					objroot.appendChild(createElement(doc,"PhysicsObjectLinearDamping", //$NON-NLS-1$
							Double.toString((Double) object.get(PGmObject.PHYSICS_DAMPING_LINEAR))));
					objroot.appendChild(createElement(doc,"PhysicsObjectAngularDamping", //$NON-NLS-1$
							Double.toString((Double) object.get(PGmObject.PHYSICS_DAMPING_ANGULAR))));
					objroot.appendChild(createElement(doc,"PhysicsObjectFriction", //$NON-NLS-1$
							Double.toString((Double) object.get(PGmObject.PHYSICS_FRICTION))));
					objroot.appendChild(createElement(doc,"PhysicsObjectAwake", //$NON-NLS-1$
							boolToString((Boolean) object.get(PGmObject.PHYSICS_AWAKE))));
					objroot.appendChild(createElement(doc,"PhysicsObjectKinematic", //$NON-NLS-1$
							boolToString((Boolean) object.get(PGmObject.PHYSICS_KINEMATIC))));

					Element pointsroot = doc.createElement("PhysicsShapePoints"); //$NON-NLS-1$
					for (ShapePoint point : object.shapePoints)
						{
						pointsroot.appendChild(createElement(doc,"point",point.getX() + "," + point.getY())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					objroot.appendChild(pointsroot);

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
						tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
						;
						tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8"); //$NON-NLS-1$
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + object.getName() + ".object.gmx")); //$NON-NLS-1$
						tr.transform(new DOMSource(doc),new StreamResult(fos));
						}
					finally
						{
						fos.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeGmObjects(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document dom = c.dom;

		Element node = dom.createElement("objects"); //$NON-NLS-1$
		node.setAttribute("name","objects"); //$NON-NLS-1$ //$NON-NLS-2$
		root.appendChild(node);

		ResourceList<GmObject> objList = c.f.resMap.getList(GmObject.class);
		if (objList.size() == 0)
			{
			return;
			}

		iterateGmObjects(c,getPrimaryNode(objList.first().getNode()),node);
		}

	private static void iterateRooms(ProjectFileContext c, ResNode root, Element node)
			throws IOException,TransformerException
		{
		ProjectFile f = c.f;
		Document dom = c.dom;
		Vector<ResNode> children = root.getChildren();
		if (children == null)
			{
			return;
			}
		for (Object obj : children)
			{
			if (!(obj instanceof ResNode))
				{
				continue;
				}
			ResNode resNode = (ResNode) obj;
			Element res = null;
			switch (resNode.status)
				{
				case ResNode.STATUS_PRIMARY:
					res = dom.createElement("rooms"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase()); //$NON-NLS-1$
					iterateRooms(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("rooms"); //$NON-NLS-1$
					res.setAttribute("name",resNode.getUserObject().toString()); //$NON-NLS-1$
					iterateRooms(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Room room = (Room) resNode.getRes().get();
					res = dom.createElement("room"); //$NON-NLS-1$
					String fname = f.getDirectory() + "\\rooms\\"; //$NON-NLS-1$
					res.setTextContent("rooms\\" + room.getName()); //$NON-NLS-1$
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/rooms")); //$NON-NLS-1$
					file.mkdir();

					Document doc = documentBuilder.newDocument();

					Element roomroot = doc.createElement("room"); //$NON-NLS-1$
					doc.appendChild(roomroot);

					roomroot.appendChild(createElement(doc,"caption",room.get(PRoom.CAPTION).toString())); //$NON-NLS-1$
					roomroot.appendChild(createElement(doc,"width",room.get(PRoom.WIDTH).toString())); //$NON-NLS-1$
					roomroot.appendChild(createElement(doc,"height",room.get(PRoom.HEIGHT).toString())); //$NON-NLS-1$
					roomroot.appendChild(createElement(doc,"hsnap",room.get(PRoom.SNAP_X).toString())); //$NON-NLS-1$
					roomroot.appendChild(createElement(doc,"vsnap",room.get(PRoom.SNAP_Y).toString())); //$NON-NLS-1$
					roomroot.appendChild(createElement(doc,"isometric", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.ISOMETRIC))));
					roomroot.appendChild(createElement(doc,"speed",room.get(PRoom.SPEED).toString())); //$NON-NLS-1$
					roomroot.appendChild(createElement(doc,"persistent", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.PERSISTENT))));
					roomroot.appendChild(createElement(doc,"colour", //$NON-NLS-1$
							Integer.toString(Util.getGmColor((Color) room.get(PRoom.BACKGROUND_COLOR)))));
					roomroot.appendChild(createElement(doc,"showcolour", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.DRAW_BACKGROUND_COLOR))));
					roomroot.appendChild(createElement(doc,"code",room.get(PRoom.CREATION_CODE).toString())); //$NON-NLS-1$
					roomroot.appendChild(createElement(doc,"enableViews", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.VIEWS_ENABLED))));
					roomroot.appendChild(createElement(doc,"clearViewBackground", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.VIEWS_CLEAR))));

					// Write the maker settings, or basically the settings of the editor.
					Element mkeroot = doc.createElement("makerSettings"); //$NON-NLS-1$
					mkeroot.appendChild(createElement(doc,"isSet", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.REMEMBER_WINDOW_SIZE))));
					mkeroot.appendChild(createElement(doc,"w",room.get(PRoom.EDITOR_WIDTH).toString())); //$NON-NLS-1$
					mkeroot.appendChild(createElement(doc,"h",room.get(PRoom.EDITOR_HEIGHT).toString())); //$NON-NLS-1$
					mkeroot.appendChild(createElement(doc,"showGrid", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.SHOW_GRID))));
					mkeroot.appendChild(createElement(doc,"showObjects", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.SHOW_OBJECTS))));
					mkeroot.appendChild(createElement(doc,"showTiles", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.SHOW_TILES))));
					mkeroot.appendChild(createElement(doc,"showBackgrounds", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.SHOW_BACKGROUNDS))));
					mkeroot.appendChild(createElement(doc,"showForegrounds", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.SHOW_FOREGROUNDS))));
					mkeroot.appendChild(createElement(doc,"showViews", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.SHOW_VIEWS))));
					mkeroot.appendChild(createElement(doc,"deleteUnderlyingObj", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.DELETE_UNDERLYING_OBJECTS))));
					mkeroot.appendChild(createElement(doc,"deleteUnderlyingTiles", //$NON-NLS-1$
							boolToString((Boolean) room.get(PRoom.DELETE_UNDERLYING_TILES))));
					mkeroot.appendChild(createElement(doc,"page",room.get(PRoom.CURRENT_TAB).toString())); //$NON-NLS-1$
					mkeroot.appendChild(createElement(doc,"xoffset",room.get(PRoom.SCROLL_BAR_X).toString())); //$NON-NLS-1$
					mkeroot.appendChild(createElement(doc,"yoffset",room.get(PRoom.SCROLL_BAR_Y).toString())); //$NON-NLS-1$
					roomroot.appendChild(mkeroot);

					// Write Backgrounds
					Element backroot = doc.createElement("backgrounds"); //$NON-NLS-1$
					roomroot.appendChild(backroot);
					for (BackgroundDef back : room.backgroundDefs) 
						{
						PropertyMap<PBackgroundDef> props = back.properties;
						Element bckelement = doc.createElement("background"); //$NON-NLS-1$
						backroot.appendChild(bckelement);

						bckelement.setAttribute("visible", //$NON-NLS-1$
								boolToString((Boolean) props.get(PBackgroundDef.VISIBLE)));
						bckelement.setAttribute("foreground", //$NON-NLS-1$
								boolToString((Boolean) props.get(PBackgroundDef.FOREGROUND)));
						bckelement.setAttribute("name", //$NON-NLS-1$
								getName((ResourceReference<?>)props.get(PBackgroundDef.BACKGROUND),""));
						bckelement.setAttribute("x",Integer.toString((Integer) props.get(PBackgroundDef.X))); //$NON-NLS-1$
						bckelement.setAttribute("y",Integer.toString((Integer) props.get(PBackgroundDef.Y))); //$NON-NLS-1$
						bckelement.setAttribute("htiled", //$NON-NLS-1$
								boolToString((Boolean) props.get(PBackgroundDef.TILE_HORIZ)));
						bckelement.setAttribute("vtiled", //$NON-NLS-1$
								boolToString((Boolean) props.get(PBackgroundDef.TILE_VERT)));
						bckelement.setAttribute("hspeed", //$NON-NLS-1$
								Integer.toString((Integer) props.get(PBackgroundDef.H_SPEED)));
						bckelement.setAttribute("vspeed", //$NON-NLS-1$
								Integer.toString((Integer) props.get(PBackgroundDef.V_SPEED)));
						bckelement.setAttribute("stretch", //$NON-NLS-1$
								boolToString((Boolean) props.get(PBackgroundDef.STRETCH)));
						}

					// Write Views
					Element viewroot = doc.createElement("views"); //$NON-NLS-1$
					roomroot.appendChild(viewroot);
					for (View view : room.views)
						{
						PropertyMap<PView> props = view.properties;
						Element vwelement = doc.createElement("view"); //$NON-NLS-1$
						viewroot.appendChild(vwelement);

						vwelement.setAttribute("visible",boolToString((Boolean) props.get(PView.VISIBLE))); //$NON-NLS-1$
						vwelement.setAttribute("objName", //$NON-NLS-1$
								getName((ResourceReference<?>) props.get(PView.OBJECT)));
						vwelement.setAttribute("xview",Integer.toString((Integer) props.get(PView.VIEW_X))); //$NON-NLS-1$
						vwelement.setAttribute("yview",Integer.toString((Integer) props.get(PView.VIEW_Y))); //$NON-NLS-1$
						vwelement.setAttribute("wview",Integer.toString((Integer) props.get(PView.VIEW_W))); //$NON-NLS-1$
						vwelement.setAttribute("hview",Integer.toString((Integer) props.get(PView.VIEW_H))); //$NON-NLS-1$
						vwelement.setAttribute("xport",Integer.toString((Integer) props.get(PView.PORT_X))); //$NON-NLS-1$
						vwelement.setAttribute("yport",Integer.toString((Integer) props.get(PView.PORT_Y))); //$NON-NLS-1$
						vwelement.setAttribute("wport",Integer.toString((Integer) props.get(PView.PORT_W))); //$NON-NLS-1$
						vwelement.setAttribute("hport",Integer.toString((Integer) props.get(PView.PORT_H))); //$NON-NLS-1$
						vwelement.setAttribute("hborder",Integer.toString((Integer) props.get(PView.BORDER_H))); //$NON-NLS-1$
						vwelement.setAttribute("vborder",Integer.toString((Integer) props.get(PView.BORDER_V))); //$NON-NLS-1$
						vwelement.setAttribute("hspeed",Integer.toString((Integer) props.get(PView.SPEED_H))); //$NON-NLS-1$
						vwelement.setAttribute("vspeed",Integer.toString((Integer) props.get(PView.SPEED_V))); //$NON-NLS-1$
						}

					// Write instances
					Element insroot = doc.createElement("instances"); //$NON-NLS-1$
					roomroot.appendChild(insroot);
					for (Instance in : room.instances)
						{
						Element inselement = doc.createElement("instance"); //$NON-NLS-1$
						insroot.appendChild(inselement);
						inselement.setAttribute("objName", //$NON-NLS-1$
								getName((ResourceReference<?>) in.properties.get(PInstance.OBJECT)));
						inselement.setAttribute("x",Integer.toString(in.getPosition().x)); //$NON-NLS-1$
						inselement.setAttribute("y",Integer.toString(in.getPosition().y)); //$NON-NLS-1$
						inselement.setAttribute("name",in.getName()); //$NON-NLS-1$
						inselement.setAttribute("id",Integer.toString(in.getID())); //$NON-NLS-1$
						inselement.setAttribute("locked",boolToString(in.isLocked())); //$NON-NLS-1$
						inselement.setAttribute("code",in.getCreationCode()); //$NON-NLS-1$
						inselement.setAttribute("scaleX",Double.toString(in.getScale().getX())); //$NON-NLS-1$
						inselement.setAttribute("scaleY",Double.toString(in.getScale().getY())); //$NON-NLS-1$
						String color = Long.toString(Util.getInstanceColorWithAlpha(in.getColor(),in.getAlpha()));
						inselement.setAttribute("colour",color); // default white //$NON-NLS-1$
						inselement.setAttribute("rotation",Double.toString(in.getRotation())); //$NON-NLS-1$
						}

					// Write Tiles
					Element tileroot = doc.createElement("tiles"); //$NON-NLS-1$
					roomroot.appendChild(tileroot);
					for (Tile tile : room.tiles)
						{
						PropertyMap<PTile> props = tile.properties;
						Element tileelement = doc.createElement("tile"); //$NON-NLS-1$
						tileroot.appendChild(tileelement);

						tileelement.setAttribute("bgName", //$NON-NLS-1$
								getName((ResourceReference<?>) props.get(PTile.BACKGROUND),""));
						tileelement.setAttribute("x",Integer.toString((Integer) props.get(PTile.ROOM_X))); //$NON-NLS-1$
						tileelement.setAttribute("y",Integer.toString((Integer) props.get(PTile.ROOM_Y))); //$NON-NLS-1$
						tileelement.setAttribute("w",Integer.toString((Integer) props.get(PTile.WIDTH))); //$NON-NLS-1$
						tileelement.setAttribute("h",Integer.toString((Integer) props.get(PTile.HEIGHT))); //$NON-NLS-1$
						tileelement.setAttribute("xo",Integer.toString((Integer) props.get(PTile.BG_X))); //$NON-NLS-1$
						tileelement.setAttribute("yo",Integer.toString((Integer) props.get(PTile.BG_Y))); //$NON-NLS-1$
						tileelement.setAttribute("id",Integer.toString((Integer) props.get(PTile.ID))); //$NON-NLS-1$
						tileelement.setAttribute("name",(String) props.get(PTile.NAME)); //$NON-NLS-1$
						tileelement.setAttribute("depth",Integer.toString(tile.getDepth())); //$NON-NLS-1$
						tileelement.setAttribute("locked",boolToString(tile.isLocked())); //$NON-NLS-1$
						Point2D scale = tile.getScale();
						tileelement.setAttribute("scaleX",Double.toString(scale.getX())); //$NON-NLS-1$
						tileelement.setAttribute("scaleY",Double.toString(scale.getY())); //$NON-NLS-1$
						tileelement.setAttribute("colour",Long.toString(tile.getColor())); //$NON-NLS-1$
						}

					// Physics properties
					roomroot.appendChild(createElement(doc,"PhysicsWorld", //$NON-NLS-1$
						boolToString((Boolean) room.get(PRoom.PHYSICS_WORLD))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldTop", //$NON-NLS-1$
						Integer.toString((Integer) room.get(PRoom.PHYSICS_TOP))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldLeft", //$NON-NLS-1$
						Integer.toString((Integer) room.get(PRoom.PHYSICS_LEFT))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldRight", //$NON-NLS-1$
						Integer.toString((Integer) room.get(PRoom.PHYSICS_RIGHT))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldBottom", //$NON-NLS-1$
						Integer.toString((Integer) room.get(PRoom.PHYSICS_BOTTOM))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldGravityX", //$NON-NLS-1$
						Double.toString((Double) room.get(PRoom.PHYSICS_GRAVITY_X))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldGravityY", //$NON-NLS-1$
						Double.toString((Double) room.get(PRoom.PHYSICS_GRAVITY_Y))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldPixToMeters", //$NON-NLS-1$
						Double.toString((Double) room.get(PRoom.PHYSICS_PIXTOMETERS))));

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes"); //$NON-NLS-1$
						tr.setOutputProperty(OutputKeys.METHOD,"xml"); //$NON-NLS-1$
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //$NON-NLS-1$ //$NON-NLS-2$

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + room.getName() + ".room.gmx")); //$NON-NLS-1$
						tr.transform(new DOMSource(doc),new StreamResult(fos));
						}
					finally
						{
						fos.close();
						}
					break;
				}
			node.appendChild(res);
			}
		}

	public static void writeRooms(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document dom = c.dom;

		Element node = dom.createElement("rooms"); //$NON-NLS-1$
		node.setAttribute("name","rooms"); //$NON-NLS-1$ //$NON-NLS-2$
		root.appendChild(node);

		ResourceList<Room> rmnList = c.f.resMap.getList(Room.class);
		if (rmnList.size() == 0)
			{
			return;
			}
		iterateRooms(c,getPrimaryNode(rmnList.first().getNode()),node);
		}

	public static void writeIncludedFiles(ProjectFileContext c, Element root) throws IOException
		{
		// TODO: Implement
		}

	public static void writePackages(ProjectFileContext c, Element root) throws IOException
		{
		// TODO: Implement
		}

	public static void writeGameInformation(ProjectFileContext c, Element root)
			throws FileNotFoundException
		{
		Document dom = c.dom;
		ProjectFile f = c.f;

		Element helpNode = dom.createElement("help"); //$NON-NLS-1$
		Element rtfNode = dom.createElement("rtf"); //$NON-NLS-1$
		rtfNode.setTextContent("help.rtf"); //$NON-NLS-1$
		helpNode.appendChild(rtfNode);

		PrintWriter out = null;
		try
			{
			out = new PrintWriter(Util.getPOSIXPath(f.getDirectory() + "/help.rtf")); //$NON-NLS-1$
			out.println(f.gameInfo.properties.get(PGameInformation.TEXT));
			}
		finally
			{
			out.close();
			}

		root.appendChild(helpNode);
		}

	public static void writeActions(Document doc, Element root, ActionContainer container)
		{
		for (Action act : container.actions)
			{
			Element actelement = doc.createElement("action"); //$NON-NLS-1$
			root.appendChild(actelement);
			LibAction la = act.getLibAction();

			actelement.appendChild(createElement(doc,"libid", //$NON-NLS-1$
					Integer.toString(la.parent != null ? la.parent.id : la.parentId)));
			actelement.appendChild(createElement(doc,"id",Integer.toString(la.id))); //$NON-NLS-1$
			actelement.appendChild(createElement(doc,"kind",Integer.toString(la.actionKind))); //$NON-NLS-1$
			actelement.appendChild(createElement(doc,"userelative",boolToString(la.allowRelative))); //$NON-NLS-1$
			actelement.appendChild(createElement(doc,"useapplyto",boolToString(la.canApplyTo))); //$NON-NLS-1$
			actelement.appendChild(createElement(doc,"isquestion",boolToString(la.question))); //$NON-NLS-1$
			actelement.appendChild(createElement(doc,"exetype",Integer.toString(la.execType))); //$NON-NLS-1$
			String execinfo = "";
			if (la.execType == Action.EXEC_FUNCTION)
				{
				execinfo = la.execInfo;
				}
			actelement.appendChild(createElement(doc,"functionname",execinfo)); //$NON-NLS-1$
			execinfo = ""; //$NON-NLS-1$
			if (la.execType == Action.EXEC_CODE)
				{
				execinfo = la.execInfo;
				}
			actelement.appendChild(createElement(doc,"codestring",execinfo)); //$NON-NLS-1$

			ResourceReference<GmObject> at = act.getAppliesTo();
			if (at != null)
				{
				if (at == GmObject.OBJECT_OTHER)
					actelement.appendChild(createElement(doc,"whoName","other")); //$NON-NLS-1$ //$NON-NLS-2$
				else if (at == GmObject.OBJECT_SELF)
					actelement.appendChild(createElement(doc,"whoName","self")); //$NON-NLS-1$ //$NON-NLS-2$
				else
					actelement.appendChild(createElement(doc,"whoName",getName(at))); //$NON-NLS-1$
				}
			else
				actelement.appendChild(createElement(doc,"whoName","self")); //$NON-NLS-1$ //$NON-NLS-2$

			actelement.appendChild(createElement(doc,"relative",boolToString(act.isRelative()))); //$NON-NLS-1$
			actelement.appendChild(createElement(doc,"isnot",boolToString(act.isNot()))); //$NON-NLS-1$

			// Now we write the arguments
			Element argsroot = doc.createElement("arguments"); //$NON-NLS-1$
			actelement.appendChild(argsroot);

			List<Argument> args = act.getArguments();
			for (Argument arg : args)
				{
				Element argelement = doc.createElement("argument"); //$NON-NLS-1$
				argsroot.appendChild(argelement);

				argelement.appendChild(createElement(doc,"kind",Integer.toString(arg.kind))); //$NON-NLS-1$
				Class<? extends Resource<?,?>> kind = Argument.getResourceKind(arg.kind);
				if (kind != null && InstantiableResource.class.isAssignableFrom(kind))
					{
					argelement.appendChild(createElement(doc,Resource.kindNames.get(kind).toLowerCase(),
							getName((ResourceReference<?>)arg.getRes())));
					}
				else
					{
					argelement.appendChild(createElement(doc,"string",arg.getVal())); //$NON-NLS-1$
					}
				}
			}
		}

	}
