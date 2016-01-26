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

	static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	static DocumentBuilder documentBuilder;

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

	public static void writeProjectFile(OutputStream os, ProjectFile f, ResNode rootRes, int ver)
			throws IOException,GmFormatException,TransformerException
		{
		f.format = ProjectFile.FormatFlavor.getVersionFlavor(ver);
		long savetime = System.currentTimeMillis();

		Document dom = null;
		try
			{
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			dom = documentBuilder.newDocument();
			}
		catch (ParserConfigurationException pce)
			{
			throw new GmFormatException(f,pce);
			}

		JProgressBar progressBar = LGM.getProgressDialogBar();
		progressBar.setMaximum(160);
		LGM.setProgressTitle(Messages.getString("ProgressDialog.GMX_SAVING"));

		ProjectFileContext c = new ProjectFileContext(f,dom);
		Element root = dom.createElement("assets");
		LGM.setProgress(0,Messages.getString("ProgressDialog.SETTINGS"));
		writeConfigurations(c,root);

		LGM.setProgress(10,Messages.getString("ProgressDialog.SPRITES"));
		writeSprites(c,root);
		LGM.setProgress(20,Messages.getString("ProgressDialog.SOUNDS"));
		writeSounds(c,root);
		LGM.setProgress(30,Messages.getString("ProgressDialog.BACKGROUNDS"));
		writeBackgrounds(c,root);
		LGM.setProgress(40,Messages.getString("ProgressDialog.PATHS"));
		writePaths(c,root);
		LGM.setProgress(50,Messages.getString("ProgressDialog.SCRIPTS"));
		writeScripts(c,root);
		LGM.setProgress(60,Messages.getString("ProgressDialog.SHADERS"));
		writeShaders(c,root);
		LGM.setProgress(70,Messages.getString("ProgressDialog.FONTS"));
		writeFonts(c,root);
		LGM.setProgress(80,Messages.getString("ProgressDialog.TIMELINES"));
		writeTimelines(c,root);
		LGM.setProgress(90,Messages.getString("ProgressDialog.OBJECTS"));
		writeGmObjects(c,root);
		LGM.setProgress(100,Messages.getString("ProgressDialog.ROOMS"));
		writeRooms(c,root);
		LGM.setProgress(110,Messages.getString("ProgressDialog.INCLUDEFILES"));
		//writeIncludedFiles(c, root);
		LGM.setProgress(120,Messages.getString("ProgressDialog.PACKAGES"));
		//writePackages(c, root);
		LGM.setProgress(130,Messages.getString("ProgressDialog.CONSTANTS"));
		writeDefaultConstants(c, root);
		LGM.setProgress(140,Messages.getString("ProgressDialog.EXTENSIONS"));
		//writeExtensions(c, root);
		LGM.setProgress(150,Messages.getString("ProgressDialog.GAMEINFORMATION"));
		writeGameInformation(c,root);

		dom.appendChild(root);

		// Now take the serialized XML data and format and write it to the actual file
		LGM.setProgress(150,Messages.getString("ProgressDialog.DOCUMENT"));
		try
			{
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT,"yes");
			tr.setOutputProperty(OutputKeys.METHOD,"xml");
			;
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

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
		LGM.setProgress(160,Messages.getString("ProgressDialog.FINISHED"));
		return;
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
			return bool ? "-1" : "0";
		}

	public static void writeConfigurations(ProjectFileContext c, Element root) throws IOException,
			TransformerException
		{
		Document mdom = c.dom;
		ProjectFile f = c.f;

		Element conNode = mdom.createElement("Configs");
		conNode.setAttribute("name","configs");
		root.appendChild(conNode);

		for (GameSettings gs : LGM.currentFile.gameSettings) {
			Element setNode = mdom.createElement("Config");
			setNode.setTextContent("Configs\\" + gs.getName());
			conNode.appendChild(setNode);

			Document dom = documentBuilder.newDocument();
			Element nconNode = dom.createElement("Config");
			dom.appendChild(nconNode);
			Element optNode = dom.createElement("Options");
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
			optNode.appendChild(createElement(dom,"option_sync_vertex",Long.toString(syncvertex)));

			optNode.appendChild(createElement(dom,"option_fullscreen",
					gs.get(PGameSettings.START_FULLSCREEN).toString()));
			optNode.appendChild(createElement(dom,"option_sizeable",
					gs.get(PGameSettings.ALLOW_WINDOW_RESIZE).toString()));
			optNode.appendChild(createElement(dom,"option_stayontop",
					gs.get(PGameSettings.ALWAYS_ON_TOP).toString()));
			optNode.appendChild(createElement(dom,"option_aborterrors",
					gs.get(PGameSettings.ABORT_ON_ERROR).toString()));

			optNode.appendChild(createElement(dom,"option_noscreensaver",
					gs.get(PGameSettings.DISABLE_SCREENSAVERS).toString()));
			optNode.appendChild(createElement(dom,"option_showcursor",
					gs.get(PGameSettings.DISPLAY_CURSOR).toString()));
			optNode.appendChild(createElement(dom,"option_displayerrors",
					gs.get(PGameSettings.DISPLAY_ERRORS).toString()));
			optNode.appendChild(createElement(dom,"option_noborder",
					gs.get(PGameSettings.DONT_DRAW_BORDER).toString()));
			optNode.appendChild(createElement(dom,"option_nobuttons",
					gs.get(PGameSettings.DONT_SHOW_BUTTONS).toString()));
			optNode.appendChild(createElement(dom,"option_argumenterrors",
					gs.get(PGameSettings.ERROR_ON_ARGS).toString()));
			optNode.appendChild(createElement(dom,"option_freeze",
					gs.get(PGameSettings.FREEZE_ON_LOSE_FOCUS).toString()));

			optNode.appendChild(createElement(dom,"option_colordepth",
					ProjectFile.GS_DEPTH_CODE.get(gs.get(PGameSettings.COLOR_DEPTH)).toString()));

			optNode.appendChild(createElement(dom,"option_frequency",
					ProjectFile.GS_FREQ_CODE.get(gs.get(PGameSettings.FREQUENCY)).toString()));
			optNode.appendChild(createElement(dom,"option_resolution",
					ProjectFile.GS_RESOL_CODE.get(gs.get(PGameSettings.RESOLUTION)).toString()));
			optNode.appendChild(createElement(dom,"option_changeresolution",
					gs.get(PGameSettings.SET_RESOLUTION).toString()));
			optNode.appendChild(createElement(
					dom,
					"option_priority",
					ProjectFile.GS_PRIORITY_CODE.get(gs.get(PGameSettings.GAME_PRIORITY)).toString()));

			optNode.appendChild(createElement(dom,"option_closeesc",
					gs.get(PGameSettings.LET_ESC_END_GAME).toString()));
			optNode.appendChild(createElement(dom,"option_interpolate",
					gs.get(PGameSettings.INTERPOLATE).toString()));
			optNode.appendChild(createElement(dom,"option_scale",
					gs.get(PGameSettings.SCALING).toString()));
			optNode.appendChild(createElement(dom,"option_closeesc",
					gs.get(PGameSettings.TREAT_CLOSE_AS_ESCAPE).toString()));
			optNode.appendChild(createElement(dom,"option_lastchanged",
					gs.get(PGameSettings.LAST_CHANGED).toString()));

			optNode.appendChild(createElement(dom,"option_gameid",
					gs.get(PGameSettings.GAME_ID).toString()));
			String guid = HexBin.encode((byte[]) gs.get(PGameSettings.GAME_GUID));
			optNode.appendChild(createElement(dom,"option_gameguid",
					"{" + guid.substring(0,8) + "-" + guid.substring(8,12) + "-" + guid.substring(12,16) + "-"
							+ guid.substring(16,20) + "-" + guid.substring(20,32) + "}"));

			optNode.appendChild(createElement(dom,"option_author",
					(String) gs.get(PGameSettings.AUTHOR)));
			optNode.appendChild(createElement(dom,"option_version_company",
					(String) gs.get(PGameSettings.COMPANY)));
			optNode.appendChild(createElement(dom,"option_version_copyright",
					(String) gs.get(PGameSettings.COPYRIGHT)));
			optNode.appendChild(createElement(dom,"option_version_description",
					(String) gs.get(PGameSettings.DESCRIPTION)));
			optNode.appendChild(createElement(dom,"option_version_product",
					(String) gs.get(PGameSettings.PRODUCT)));
			optNode.appendChild(createElement(dom,"option_information",
					(String) gs.get(PGameSettings.INFORMATION)));
			optNode.appendChild(createElement(dom,"option_version",
					gs.get(PGameSettings.VERSION).toString()));
			optNode.appendChild(createElement(dom,"option_version_build",
					gs.get(PGameSettings.VERSION_BUILD).toString()));
			optNode.appendChild(createElement(dom,"option_version_major",
					gs.get(PGameSettings.VERSION_MAJOR).toString()));
			optNode.appendChild(createElement(dom,"option_version_minor",
					gs.get(PGameSettings.VERSION_MINOR).toString()));
			optNode.appendChild(createElement(dom,"option_version_release",
					gs.get(PGameSettings.VERSION_RELEASE).toString()));

			Element cce = dom.createElement("ConfigConstants");
			writeConstants(gs.constants, dom, cce);
			nconNode.appendChild(cce);

			String icoPath = "Configs\\Default\\windows\\runner_icon.ico";
			optNode.appendChild(createElement(dom,"option_windows_game_icon",icoPath));

			icoPath = f.getDirectory() + "\\" + icoPath;
			File file = new File(Util.getPOSIXPath(icoPath)).getParentFile();
			file.mkdirs();

			FileOutputStream fos = new FileOutputStream(Util.getPOSIXPath(icoPath));
			((ICOFile) gs.get(PGameSettings.GAME_ICON)).write(fos);
			fos.close();

			fos = null;
			try
				{
				Transformer tr = TransformerFactory.newInstance().newTransformer();
				tr.setOutputProperty(OutputKeys.INDENT,"yes");
				tr.setOutputProperty(OutputKeys.METHOD,"xml");
				;
				tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");

				file = new File(Util.getPOSIXPath(f.getDirectory() + "/Configs"));
				file.mkdir();

				// send DOM to file
				fos = new FileOutputStream(Util.getPOSIXPath(f.getDirectory() + "/Configs/" + gs.getName() + ".config.gmx"));
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
			Element base = dom.createElement("constants");
			base.setAttribute("number",Integer.toString(cnsts.constants.size()));
			for (Constant cnst : cnsts.constants) {
				Element celement = dom.createElement("constant");
				celement.setAttribute("name",cnst.name);
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
					res = dom.createElement("sprites");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateSprites(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("sprites");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateSprites(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Sprite spr = (Sprite) resNode.getRes().get();
					res = dom.createElement("sprite");
					String fname = f.getDirectory() + "\\sprites\\";
					res.setTextContent("sprites\\" + spr.getName());
					File file = new File(Util.getPOSIXPath(fname + "\\images"));
					file.mkdirs();

					Document doc = documentBuilder.newDocument();

					Element sprroot = doc.createElement("sprite");
					doc.appendChild(sprroot);

					sprroot.appendChild(createElement(doc,"xorig",spr.get(PSprite.ORIGIN_X).toString()));
					sprroot.appendChild(createElement(doc,"yorigin",spr.get(PSprite.ORIGIN_Y).toString()));
					sprroot.appendChild(createElement(doc,"colkind",
							ProjectFile.SPRITE_MASK_CODE.get(spr.get(PSprite.SHAPE)).toString()));
					sprroot.appendChild(createElement(doc,"sepmasks",
							boolToString((Boolean) spr.get(PSprite.SEPARATE_MASK))));
					sprroot.appendChild(createElement(doc,"bbox_left",spr.get(PSprite.BB_LEFT).toString()));
					sprroot.appendChild(createElement(doc,"bbox_right",spr.get(PSprite.BB_RIGHT).toString()));
					sprroot.appendChild(createElement(doc,"bbox_top",spr.get(PSprite.BB_TOP).toString()));
					sprroot.appendChild(createElement(doc,"bbox_bottom",spr.get(PSprite.BB_BOTTOM).toString()));
					sprroot.appendChild(createElement(doc,"bboxmode",
							ProjectFile.SPRITE_BB_CODE.get(spr.get(PSprite.BB_MODE)).toString()));
					sprroot.appendChild(createElement(doc,"coltolerance",
							spr.get(PSprite.ALPHA_TOLERANCE).toString()));

					sprroot.appendChild(createElement(doc,"HTile",
							boolToString((Boolean) spr.get(PSprite.TILE_HORIZONTALLY))));
					sprroot.appendChild(createElement(doc,"VTile",
							boolToString((Boolean) spr.get(PSprite.TILE_VERTICALLY))));

					// TODO: Write texture groups

					sprroot.appendChild(createElement(doc,"For3D",
							boolToString((Boolean) spr.get(PSprite.FOR3D))));

					int width = spr.getWidth(),
					height = spr.getHeight();

					sprroot.appendChild(createElement(doc,"width",Integer.toString(width)));
					sprroot.appendChild(createElement(doc,"height",Integer.toString(height)));

					Element frameroot = doc.createElement("frames");
					for (int j = 0; j < spr.subImages.size(); j++)
						{
						String framefname = "images\\" + spr.getName() + "_" + j + ".png";
						File outputfile = new File(Util.getPOSIXPath(fname + framefname));
						Element frameNode = createElement(doc,"frame",framefname);
						frameNode.setAttribute("index",Integer.toString(j));
						frameroot.appendChild(frameNode);
						BufferedImage sub = spr.subImages.get(j);
						ImageIO.write(sub,"png",outputfile);
						}
					sprroot.appendChild(frameroot);

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes");
						tr.setOutputProperty(OutputKeys.METHOD,"xml");
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + spr.getName() + ".sprite.gmx"));
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

		Element node = dom.createElement("sprites");
		node.setAttribute("name","sprites");
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
					res = dom.createElement("sounds");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateSounds(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("sounds");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateSounds(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Sound snd = (Sound) resNode.getRes().get();
					res = dom.createElement("sound");
					String fname = f.getDirectory() + "\\sound\\";
					res.setTextContent("sound\\" + snd.getName());
					File file = new File(Util.getPOSIXPath(fname + "\\audio"));
					file.mkdirs();

					Document doc = documentBuilder.newDocument();

					Element sndroot = doc.createElement("sound");
					doc.appendChild(sndroot);

					// GMX uses double nested tags for volume, bit rate, sample rate, type, and bit depth
					// There is a special clause here, every one of those tags after volume, the nested
					// tag is singular, where its parent is plural.
					String ftype = snd.get(PSound.FILE_TYPE).toString();
					sndroot.appendChild(createElement(doc,"extension",ftype));
					sndroot.appendChild(createElement(doc,"origname",snd.get(PSound.FILE_NAME).toString()));
					sndroot.appendChild(createElement(doc,"kind",
							ProjectFile.SOUND_KIND_CODE.get(snd.get(PSound.KIND)).toString()));

					Element volumeRoot = doc.createElement("volume");
					volumeRoot.appendChild(createElement(doc,"volume", snd.get(PSound.VOLUME).toString()));
					sndroot.appendChild(volumeRoot);

					Element bitRateRoot = doc.createElement("bitRates");
					bitRateRoot.appendChild(createElement(doc,"bitRate",
						snd.get(PSound.BIT_RATE).toString()));
					sndroot.appendChild(bitRateRoot);

					Element sampleRateRoot = doc.createElement("sampleRates");
					sampleRateRoot.appendChild(createElement(doc,"sampleRate",
							snd.get(PSound.SAMPLE_RATE).toString()));
					sndroot.appendChild(sampleRateRoot);

					Element typesRoot = doc.createElement("types");
					typesRoot.appendChild(createElement(doc,"type",
						ProjectFile.SOUND_TYPE_CODE.get(snd.get(PSound.TYPE)).toString()));
					sndroot.appendChild(typesRoot);

					Element bitDepthRoot = doc.createElement("bitDepths");
					bitDepthRoot.appendChild(createElement(doc,"bitDepth",
							snd.get(PSound.BIT_DEPTH).toString()));
					sndroot.appendChild(bitDepthRoot);

					sndroot.appendChild(createElement(doc,"pan",snd.get(PSound.PAN).toString()));
					sndroot.appendChild(createElement(doc,"preload",
							boolToString((Boolean) snd.get(PSound.PRELOAD))));
					sndroot.appendChild(createElement(doc,"compressed",
							boolToString((Boolean) snd.get(PSound.COMPRESSED))));
					sndroot.appendChild(createElement(doc,"streamed",
							boolToString((Boolean) snd.get(PSound.STREAMED))));
					sndroot.appendChild(createElement(doc,"uncompressOnLoad",
							boolToString((Boolean) snd.get(PSound.DECOMPRESS_ON_LOAD))));
					int effects = 0;
					int n = 1;
					for (PSound k : ProjectFile.SOUND_FX_FLAGS)
						{
						if (snd.get(k)) effects |= n;
						n <<= 1;
						}
					sndroot.appendChild(createElement(doc,"effects",Integer.toString(effects)));

					sndroot.appendChild(createElement(doc,"data",snd.getName() + ftype));
					Util.writeBinaryFile(fname + "audio\\" + snd.getName() + ftype,snd.data);

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes");
						tr.setOutputProperty(OutputKeys.METHOD,"xml");
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + resNode.getUserObject().toString()
								+ ".sound.gmx"));
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

		Element node = dom.createElement("sounds");
		node.setAttribute("name","sound");
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
					res = dom.createElement("backgrounds");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateBackgrounds(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("backgrounds");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateBackgrounds(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Background bkg = (Background) resNode.getRes().get();
					res = dom.createElement("background");
					String fname = f.getDirectory() + "\\background\\";
					res.setTextContent("background\\" + bkg.getName());
					File file = new File(Util.getPOSIXPath(fname + "\\images"));
					file.mkdirs();

					Document doc = documentBuilder.newDocument();

					Element bkgroot = doc.createElement("background");
					doc.appendChild(bkgroot);

					bkgroot.appendChild(createElement(doc,"istileset",
							boolToString((Boolean) bkg.get(PBackground.USE_AS_TILESET))));
					bkgroot.appendChild(createElement(doc,"tilewidth",
							bkg.get(PBackground.TILE_WIDTH).toString()));
					bkgroot.appendChild(createElement(doc,"tileheight",
							bkg.get(PBackground.TILE_HEIGHT).toString()));
					bkgroot.appendChild(createElement(doc,"tilexoff",bkg.get(PBackground.H_OFFSET).toString()));
					bkgroot.appendChild(createElement(doc,"tileyoff",bkg.get(PBackground.V_OFFSET).toString()));
					bkgroot.appendChild(createElement(doc,"tilehsep",bkg.get(PBackground.H_SEP).toString()));
					bkgroot.appendChild(createElement(doc,"tilevsep",bkg.get(PBackground.V_SEP).toString()));
					bkgroot.appendChild(createElement(doc,"HTile",
							boolToString((Boolean) bkg.get(PBackground.TILE_HORIZONTALLY))));
					bkgroot.appendChild(createElement(doc,"VTile",
							boolToString((Boolean) bkg.get(PBackground.TILE_VERTICALLY))));

					// TODO: Write texture groups

					bkgroot.appendChild(createElement(doc,"For3D",
							boolToString((Boolean) bkg.get(PBackground.FOR3D))));

					int width = bkg.getWidth(),
					height = bkg.getHeight();

					bkgroot.appendChild(createElement(doc,"width",Integer.toString(width)));
					bkgroot.appendChild(createElement(doc,"height",Integer.toString(height)));

					bkgroot.appendChild(createElement(doc,"data","images\\" + bkg.getName() + ".png"));
					if (width > 0 && height > 0)
						{
						File outputfile = new File(Util.getPOSIXPath(fname + "images\\" + bkg.getName() + ".png"));
						ImageIO.write(bkg.getBackgroundImage(),"png",outputfile);
						}

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes");
						tr.setOutputProperty(OutputKeys.METHOD,"xml");
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + resNode.getUserObject().toString()
								+ ".background.gmx"));
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

		Element node = dom.createElement("backgrounds");
		node.setAttribute("name","background");
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
					res = dom.createElement("paths");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iteratePaths(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("paths");
					res.setAttribute("name",resNode.getUserObject().toString());
					iteratePaths(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Path path = (Path) resNode.getRes().get();
					res = dom.createElement("path");
					String fname = f.getDirectory() + "\\paths\\";
					res.setTextContent("paths\\" + path.getName());
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/paths"));
					file.mkdir();

					Document doc = documentBuilder.newDocument();

					Element pathroot = doc.createElement("path");
					doc.appendChild(pathroot);

					int kind = path.get(PPath.SMOOTH) ? 1 : 0;
					pathroot.appendChild(createElement(doc,"kind",Integer.toString(kind)));
					int closed = path.get(PPath.CLOSED) ? -1 : 0;
					pathroot.appendChild(createElement(doc,"closed",Integer.toString(closed)));
					pathroot.appendChild(createElement(doc,"precision",path.get(PPath.PRECISION).toString()));

					ResourceReference<Room> bgroom = path.get(PPath.BACKGROUND_ROOM);
					if (bgroom != null)
						{
						pathroot.appendChild(createElement(doc,"backroom",bgroom.get().getName()));
						}
					else
						{
						pathroot.appendChild(createElement(doc,"backroom","-1"));
						}

					pathroot.appendChild(createElement(doc,"hsnap",path.get(PPath.SNAP_X).toString()));
					pathroot.appendChild(createElement(doc,"vsnap",path.get(PPath.SNAP_Y).toString()));

					Element rootpoint = doc.createElement("points");
					pathroot.appendChild(rootpoint);
					for (PathPoint p : path.points)
						{
						rootpoint.appendChild(createElement(doc,"point",
								p.getX() + "," + p.getY() + "," + p.getSpeed()));
						}

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes");
						tr.setOutputProperty(OutputKeys.METHOD,"xml");
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + path.getName() + ".path.gmx"));
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

		Element node = dom.createElement("paths");
		node.setAttribute("name","paths");
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
					res = dom.createElement("scripts");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateScripts(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("scripts");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateScripts(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Script scr = (Script) resNode.getRes().get();
					res = dom.createElement("script");
					String fname = "scripts\\" + scr.getName() + ".gml";
					res.setTextContent(fname);
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/scripts"));
					file.mkdir();
					Writer out = null;
					try
						{
						out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
								Util.getPOSIXPath(f.getDirectory() + "/" + Util.getPOSIXPath(fname))),"UTF-8"));
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

		Element node = dom.createElement("scripts");
		node.setAttribute("name","scripts");
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
					res = dom.createElement("shaders");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateShaders(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("shaders");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateShaders(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Shader shr = (Shader) resNode.getRes().get();
					res = dom.createElement("shader" + "");
					String fname = "shaders\\" + shr.getName() + ".shader";
					res.setTextContent(fname);
					res.setAttribute("type",shr.properties.get(PShader.TYPE).toString());
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/shaders"));
					file.mkdir();
					Writer out = null;
					try
						{
						out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
								Util.getPOSIXPath(f.getDirectory() + "/" + fname)),"UTF-8"));
						String code = shr.properties.get(PShader.VERTEX)
								+ "\n//######################_==_YOYO_SHADER_MARKER_==_######################@~"
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

		Element node = dom.createElement("shaders");
		node.setAttribute("name","shaders");
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
					res = dom.createElement("fonts");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateFonts(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("fonts");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateFonts(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Font fnt = (Font) resNode.getRes().get();
					res = dom.createElement("font");
					String fname = f.getDirectory() + "\\fonts\\";
					res.setTextContent("fonts\\" + fnt.getName());
					File file = new File(Util.getPOSIXPath(fname));
					file.mkdirs();

					Document doc = documentBuilder.newDocument();

					Element fntroot = doc.createElement("font");
					doc.appendChild(fntroot);

					fntroot.appendChild(createElement(doc,"name",fnt.get(PFont.FONT_NAME).toString()));
					fntroot.appendChild(createElement(doc,"size",fnt.get(PFont.SIZE).toString()));
					fntroot.appendChild(createElement(doc,"bold",boolToString((Boolean) fnt.get(PFont.BOLD))));
					fntroot.appendChild(createElement(doc,"italic",
							boolToString((Boolean) fnt.get(PFont.ITALIC))));
					fntroot.appendChild(createElement(doc,"charset",fnt.get(PFont.CHARSET).toString()));
					fntroot.appendChild(createElement(doc,"aa",fnt.get(PFont.ANTIALIAS).toString()));

					Element rangeroot = doc.createElement("ranges");
					fntroot.appendChild(rangeroot);
					for (CharacterRange cr : fnt.characterRanges)
						{
						rangeroot.appendChild(createElement(
								doc,
								"range0",
								cr.properties.get(PCharacterRange.RANGE_MIN).toString() + ","
										+ cr.properties.get(PCharacterRange.RANGE_MAX).toString()));
						}

					Element glyphroot = doc.createElement("glyphs");
					fntroot.appendChild(glyphroot);
					for (GlyphMetric gm : fnt.glyphMetrics)
						{
						Element gelement = doc.createElement("glyph");
						gelement.setAttribute("character",gm.properties.get(PGlyphMetric.CHARACTER).toString());
						gelement.setAttribute("x",gm.properties.get(PGlyphMetric.X).toString());
						gelement.setAttribute("y",gm.properties.get(PGlyphMetric.Y).toString());
						gelement.setAttribute("w",gm.properties.get(PGlyphMetric.W).toString());
						gelement.setAttribute("h",gm.properties.get(PGlyphMetric.H).toString());
						gelement.setAttribute("shift",gm.properties.get(PGlyphMetric.SHIFT).toString());
						gelement.setAttribute("offset",gm.properties.get(PGlyphMetric.OFFSET).toString());
						glyphroot.appendChild(gelement);
						}

					// TODO: Move glyph renderer from the plugin to LGM and write glyphs here
					fntroot.appendChild(createElement(doc,"image",fnt.getName() + ".png"));
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
						tr.setOutputProperty(OutputKeys.INDENT,"yes");
						tr.setOutputProperty(OutputKeys.METHOD,"xml");
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + fnt.getName() + ".font.gmx"));
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

		Element node = dom.createElement("fonts");
		node.setAttribute("name","fonts");
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
					res = dom.createElement("timelines");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateTimelines(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("timelines");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateTimelines(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Timeline timeline = (Timeline) resNode.getRes().get();
					res = dom.createElement("timeline");
					String fname = f.getDirectory() + "\\timelines\\";
					res.setTextContent("timelines\\" + timeline.getName());
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/timelines"));
					file.mkdir();

					Document doc = documentBuilder.newDocument();

					Element tmlroot = doc.createElement("timeline");
					doc.appendChild(tmlroot);

					for (Moment mom : timeline.moments)
						{
						Element entroot = doc.createElement("entry");
						tmlroot.appendChild(entroot);
						entroot.appendChild(createElement(doc,"step",Integer.toString(mom.stepNo)));
						Element evtroot = doc.createElement("event");
						entroot.appendChild(evtroot);
						writeActions(doc,evtroot,mom);
						}

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes");
						tr.setOutputProperty(OutputKeys.METHOD,"xml");
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + timeline.getName() + ".timeline.gmx"));
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

		Element node = dom.createElement("timelines");
		node.setAttribute("name","timelines");
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
					res = dom.createElement("objects");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateGmObjects(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("objects");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateGmObjects(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					GmObject object = (GmObject) resNode.getRes().get();
					res = dom.createElement("object");
					String fname = f.getDirectory() + "\\objects\\";
					res.setTextContent("objects\\" + object.getName());
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/objects"));
					file.mkdir();

					Document doc = documentBuilder.newDocument();

					Element objroot = doc.createElement("object");
					doc.appendChild(objroot);

					ResourceReference<?> spr = object.get(PGmObject.SPRITE);
					if (spr != null)
						{
						objroot.appendChild(createElement(doc,"spriteName",spr.get().getName()));
						}
					else
						{
						objroot.appendChild(createElement(doc,"spriteName","<undefined>"));
						}
					objroot.appendChild(createElement(doc,"solid",
							boolToString((Boolean) object.get(PGmObject.SOLID))));
					objroot.appendChild(createElement(doc,"visible",
							boolToString((Boolean) object.get(PGmObject.VISIBLE))));
					objroot.appendChild(createElement(doc,"depth",object.get(PGmObject.DEPTH).toString()));
					objroot.appendChild(createElement(doc,"persistent",
							boolToString((Boolean) object.get(PGmObject.PERSISTENT))));
					spr = object.get(PGmObject.MASK);
					if (spr != null)
						{
						objroot.appendChild(createElement(doc,"maskName",spr.get().getName()));
						}
					else
						{
						objroot.appendChild(createElement(doc,"maskName","<undefined>"));
						}

					ResourceReference<?> par = object.get(PGmObject.PARENT);
					if (par != null)
						{
						objroot.appendChild(createElement(doc,"parentName",par.get().getName()));
						}
					else
						{
						objroot.appendChild(createElement(doc,"parentName","<undefined>"));
						}

					Element evtroot = doc.createElement("events");
					for (int i = 0; i < object.mainEvents.size(); i++)
						{
						MainEvent me = object.mainEvents.get(i);
						for (int k = me.events.size(); k > 0; k--)
							{
							Event ev = me.events.get(k - 1);
							Element evtelement = doc.createElement("event");
							evtelement.setAttribute("eventtype",Integer.toString(ev.mainId));
							if (ev.mainId == MainEvent.EV_COLLISION)
								{
								ResourceReference<GmObject> other = ev.other;
								if (other != null)
									{
									evtelement.setAttribute("ename",other.get().getName());
									}
								else
									{
									evtelement.setAttribute("ename","<undefined>");
									}
								}
							else
								{
								evtelement.setAttribute("enumb",Integer.toString(ev.id));
								}
							evtroot.appendChild(evtelement);
							writeActions(doc,evtelement,ev);
							}
						}
					objroot.appendChild(evtroot);

					// Physics Properties
					objroot.appendChild(createElement(doc,"PhysicsObject",
							boolToString((Boolean) object.get(PGmObject.PHYSICS_OBJECT))));
					objroot.appendChild(createElement(doc,"PhysicsObjectSensor",
							boolToString((Boolean) object.get(PGmObject.PHYSICS_SENSOR))));
					objroot.appendChild(createElement(doc,"PhysicsObjectShape",
							ProjectFile.SHAPE_CODE.get(object.get(PGmObject.PHYSICS_SHAPE)).toString()));
					objroot.appendChild(createElement(doc,"PhysicsObjectDensity",
							Double.toString((Double) object.get(PGmObject.PHYSICS_DENSITY))));
					objroot.appendChild(createElement(doc,"PhysicsObjectRestitution",
							Double.toString((Double) object.get(PGmObject.PHYSICS_RESTITUTION))));
					objroot.appendChild(createElement(doc,"PhysicsObjectGroup",
							Integer.toString((Integer) object.get(PGmObject.PHYSICS_GROUP))));
					objroot.appendChild(createElement(doc,"PhysicsObjectLinearDamping",
							Double.toString((Double) object.get(PGmObject.PHYSICS_DAMPING_LINEAR))));
					objroot.appendChild(createElement(doc,"PhysicsObjectAngularDamping",
							Double.toString((Double) object.get(PGmObject.PHYSICS_DAMPING_ANGULAR))));
					objroot.appendChild(createElement(doc,"PhysicsObjectFriction",
							Double.toString((Double) object.get(PGmObject.PHYSICS_FRICTION))));
					objroot.appendChild(createElement(doc,"PhysicsObjectAwake",
							boolToString((Boolean) object.get(PGmObject.PHYSICS_AWAKE))));
					objroot.appendChild(createElement(doc,"PhysicsObjectKinematic",
							boolToString((Boolean) object.get(PGmObject.PHYSICS_KINEMATIC))));

					Element pointsroot = doc.createElement("PhysicsShapePoints");
					for (ShapePoint point : object.shapePoints)
						{
						pointsroot.appendChild(createElement(doc,"point",point.getX() + "," + point.getY()));
						}
					objroot.appendChild(pointsroot);

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes");
						tr.setOutputProperty(OutputKeys.METHOD,"xml");
						;
						tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + object.getName() + ".object.gmx"));
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

		Element node = dom.createElement("objects");
		node.setAttribute("name","objects");
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
					res = dom.createElement("rooms");
					res.setAttribute("name",resNode.getUserObject().toString().toLowerCase());
					iterateRooms(c,resNode,res);
					break;
				case ResNode.STATUS_GROUP:
					res = dom.createElement("rooms");
					res.setAttribute("name",resNode.getUserObject().toString());
					iterateRooms(c,resNode,res);
					break;
				case ResNode.STATUS_SECONDARY:
					Room room = (Room) resNode.getRes().get();
					res = dom.createElement("room");
					String fname = f.getDirectory() + "\\rooms\\";
					res.setTextContent("rooms\\" + room.getName());
					File file = new File(Util.getPOSIXPath(f.getDirectory() + "/rooms"));
					file.mkdir();

					Document doc = documentBuilder.newDocument();

					Element roomroot = doc.createElement("room");
					doc.appendChild(roomroot);

					roomroot.appendChild(createElement(doc,"caption",room.get(PRoom.CAPTION).toString()));
					roomroot.appendChild(createElement(doc,"width",room.get(PRoom.WIDTH).toString()));
					roomroot.appendChild(createElement(doc,"height",room.get(PRoom.HEIGHT).toString()));
					roomroot.appendChild(createElement(doc,"hsnap",room.get(PRoom.SNAP_X).toString()));
					roomroot.appendChild(createElement(doc,"vsnap",room.get(PRoom.SNAP_Y).toString()));
					roomroot.appendChild(createElement(doc,"isometric",
							boolToString((Boolean) room.get(PRoom.ISOMETRIC))));
					roomroot.appendChild(createElement(doc,"speed",room.get(PRoom.SPEED).toString()));
					roomroot.appendChild(createElement(doc,"persistent",
							boolToString((Boolean) room.get(PRoom.PERSISTENT))));
					roomroot.appendChild(createElement(doc,"colour",
							Integer.toString(Util.getGmColor((Color) room.get(PRoom.BACKGROUND_COLOR)))));
					roomroot.appendChild(createElement(doc,"showcolour",
							boolToString((Boolean) room.get(PRoom.DRAW_BACKGROUND_COLOR))));
					roomroot.appendChild(createElement(doc,"code",room.get(PRoom.CREATION_CODE).toString()));
					roomroot.appendChild(createElement(doc,"enableViews",
							boolToString((Boolean) room.get(PRoom.VIEWS_ENABLED))));
					roomroot.appendChild(createElement(doc,"clearViewBackground",
							boolToString((Boolean) room.get(PRoom.VIEWS_CLEAR))));

					// Write the maker settings, or basically the settings of the editor.
					Element mkeroot = doc.createElement("makerSettings");
					mkeroot.appendChild(createElement(doc,"isSet",
							boolToString((Boolean) room.get(PRoom.REMEMBER_WINDOW_SIZE))));
					mkeroot.appendChild(createElement(doc,"w",room.get(PRoom.EDITOR_WIDTH).toString()));
					mkeroot.appendChild(createElement(doc,"h",room.get(PRoom.EDITOR_HEIGHT).toString()));
					mkeroot.appendChild(createElement(doc,"showGrid",
							boolToString((Boolean) room.get(PRoom.SHOW_GRID))));
					mkeroot.appendChild(createElement(doc,"showObjects",
							boolToString((Boolean) room.get(PRoom.SHOW_OBJECTS))));
					mkeroot.appendChild(createElement(doc,"showTiles",
							boolToString((Boolean) room.get(PRoom.SHOW_TILES))));
					mkeroot.appendChild(createElement(doc,"showBackgrounds",
							boolToString((Boolean) room.get(PRoom.SHOW_BACKGROUNDS))));
					mkeroot.appendChild(createElement(doc,"showForegrounds",
							boolToString((Boolean) room.get(PRoom.SHOW_FOREGROUNDS))));
					mkeroot.appendChild(createElement(doc,"showViews",
							boolToString((Boolean) room.get(PRoom.SHOW_VIEWS))));
					mkeroot.appendChild(createElement(doc,"deleteUnderlyingObj",
							boolToString((Boolean) room.get(PRoom.DELETE_UNDERLYING_OBJECTS))));
					mkeroot.appendChild(createElement(doc,"deleteUnderlyingTiles",
							boolToString((Boolean) room.get(PRoom.DELETE_UNDERLYING_TILES))));
					mkeroot.appendChild(createElement(doc,"page",room.get(PRoom.CURRENT_TAB).toString()));
					mkeroot.appendChild(createElement(doc,"xoffset",room.get(PRoom.SCROLL_BAR_X).toString()));
					mkeroot.appendChild(createElement(doc,"yoffset",room.get(PRoom.SCROLL_BAR_Y).toString()));
					roomroot.appendChild(mkeroot);

					// Write Backgrounds
					Element backroot = doc.createElement("backgrounds");
					roomroot.appendChild(backroot);
					for (BackgroundDef back : room.backgroundDefs)
						{
						PropertyMap<PBackgroundDef> props = back.properties;
						Element bckelement = doc.createElement("background");
						backroot.appendChild(bckelement);

						bckelement.setAttribute("visible",
								boolToString((Boolean) props.get(PBackgroundDef.VISIBLE)));
						bckelement.setAttribute("foreground",
								boolToString((Boolean) props.get(PBackgroundDef.FOREGROUND)));
						ResourceReference<?> br = props.get(PBackgroundDef.BACKGROUND);
						if (br != null)
							{
							bckelement.setAttribute("name",br.get().getName());
							}
						else
							{
							bckelement.setAttribute("name","");
							}
						bckelement.setAttribute("x",Integer.toString((Integer) props.get(PBackgroundDef.X)));
						bckelement.setAttribute("y",Integer.toString((Integer) props.get(PBackgroundDef.Y)));
						bckelement.setAttribute("htiled",
								boolToString((Boolean) props.get(PBackgroundDef.TILE_HORIZ)));
						bckelement.setAttribute("vtiled",
								boolToString((Boolean) props.get(PBackgroundDef.TILE_VERT)));
						bckelement.setAttribute("hspeed",
								Integer.toString((Integer) props.get(PBackgroundDef.H_SPEED)));
						bckelement.setAttribute("vspeed",
								Integer.toString((Integer) props.get(PBackgroundDef.V_SPEED)));
						bckelement.setAttribute("stretch",
								boolToString((Boolean) props.get(PBackgroundDef.STRETCH)));
						}

					// Write Views
					Element viewroot = doc.createElement("views");
					roomroot.appendChild(viewroot);
					for (View view : room.views)
						{
						PropertyMap<PView> props = view.properties;
						Element vwelement = doc.createElement("view");
						viewroot.appendChild(vwelement);

						vwelement.setAttribute("visible",boolToString((Boolean) props.get(PView.VISIBLE)));
						ResourceReference<?> or = (ResourceReference<?>) props.get(PView.OBJECT);
						if (or != null)
							{
							vwelement.setAttribute("objName",or.get().getName());
							}
						else
							{
							vwelement.setAttribute("objName","<undefined>");
							}
						vwelement.setAttribute("xview",Integer.toString((Integer) props.get(PView.VIEW_X)));
						vwelement.setAttribute("yview",Integer.toString((Integer) props.get(PView.VIEW_Y)));
						vwelement.setAttribute("wview",Integer.toString((Integer) props.get(PView.VIEW_W)));
						vwelement.setAttribute("hview",Integer.toString((Integer) props.get(PView.VIEW_H)));
						vwelement.setAttribute("xport",Integer.toString((Integer) props.get(PView.PORT_X)));
						vwelement.setAttribute("yport",Integer.toString((Integer) props.get(PView.PORT_Y)));
						vwelement.setAttribute("wport",Integer.toString((Integer) props.get(PView.PORT_W)));
						vwelement.setAttribute("hport",Integer.toString((Integer) props.get(PView.PORT_H)));
						vwelement.setAttribute("hborder",Integer.toString((Integer) props.get(PView.BORDER_H)));
						vwelement.setAttribute("vborder",Integer.toString((Integer) props.get(PView.BORDER_V)));
						vwelement.setAttribute("hspeed",Integer.toString((Integer) props.get(PView.SPEED_H)));
						vwelement.setAttribute("vspeed",Integer.toString((Integer) props.get(PView.SPEED_V)));
						}

					// Write instances
					Element insroot = doc.createElement("instances");
					roomroot.appendChild(insroot);
					for (Instance in : room.instances)
						{
						Element inselement = doc.createElement("instance");
						insroot.appendChild(inselement);
						ResourceReference<GmObject> or = in.properties.get(PInstance.OBJECT);
						if (or != null)
							{
							inselement.setAttribute("objName",or.get().getName());
							}
						else
							{
							inselement.setAttribute("objName","<undefined>");
							}
						inselement.setAttribute("x",Integer.toString(in.getPosition().x));
						inselement.setAttribute("y",Integer.toString(in.getPosition().y));
						inselement.setAttribute("name",in.getName());
						inselement.setAttribute("id",Integer.toString(in.getID()));
						inselement.setAttribute("locked",boolToString(in.isLocked()));
						inselement.setAttribute("code",in.getCreationCode());
						inselement.setAttribute("scaleX",Double.toString(in.getScale().getX()));
						inselement.setAttribute("scaleY",Double.toString(in.getScale().getY()));
						String color = Long.toString(Util.getInstanceColorWithAlpha(in.getColor(),in.getAlpha()));
						inselement.setAttribute("colour",color);// default white
						inselement.setAttribute("rotation",Double.toString(in.getRotation()));
						}

					// Write Tiles
					Element tileroot = doc.createElement("tiles");
					roomroot.appendChild(tileroot);
					for (Tile tile : room.tiles)
						{
						PropertyMap<PTile> props = tile.properties;
						Element tileelement = doc.createElement("tile");
						tileroot.appendChild(tileelement);

						ResourceReference<?> br = props.get(PTile.BACKGROUND);
						if (br != null)
							{
							tileelement.setAttribute("bgName",br.get().getName());
							}
						else
							{
							tileelement.setAttribute("bgName","");
							}
						tileelement.setAttribute("x",Integer.toString((Integer) props.get(PTile.ROOM_X)));
						tileelement.setAttribute("y",Integer.toString((Integer) props.get(PTile.ROOM_Y)));
						tileelement.setAttribute("w",Integer.toString((Integer) props.get(PTile.WIDTH)));
						tileelement.setAttribute("h",Integer.toString((Integer) props.get(PTile.HEIGHT)));
						tileelement.setAttribute("xo",Integer.toString((Integer) props.get(PTile.BG_X)));
						tileelement.setAttribute("yo",Integer.toString((Integer) props.get(PTile.BG_Y)));
						tileelement.setAttribute("id",Integer.toString((Integer) props.get(PTile.ID)));
						tileelement.setAttribute("name",(String) props.get(PTile.NAME));
						tileelement.setAttribute("depth",Integer.toString(tile.getDepth()));
						tileelement.setAttribute("locked",boolToString(tile.isLocked()));
						Point2D scale = tile.getScale();
						tileelement.setAttribute("scaleX",Double.toString(scale.getX()));
						tileelement.setAttribute("scaleY",Double.toString(scale.getY()));
						tileelement.setAttribute("colour",Long.toString(tile.getColor()));
						}

					// Physics properties
					roomroot.appendChild(createElement(doc,"PhysicsWorld",
						boolToString((Boolean) room.get(PRoom.PHYSICS_WORLD))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldTop",
						Integer.toString((Integer) room.get(PRoom.PHYSICS_TOP))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldLeft",
						Integer.toString((Integer) room.get(PRoom.PHYSICS_LEFT))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldRight",
						Integer.toString((Integer) room.get(PRoom.PHYSICS_RIGHT))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldBottom",
						Integer.toString((Integer) room.get(PRoom.PHYSICS_BOTTOM))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldGravityX",
						Double.toString((Double) room.get(PRoom.PHYSICS_GRAVITY_X))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldGravityY",
						Double.toString((Double) room.get(PRoom.PHYSICS_GRAVITY_Y))));
					roomroot.appendChild(createElement(doc,"PhysicsWorldPixToMeters",
						Double.toString((Double) room.get(PRoom.PHYSICS_PIXTOMETERS))));

					FileOutputStream fos = null;
					try
						{
						Transformer tr = TransformerFactory.newInstance().newTransformer();
						tr.setOutputProperty(OutputKeys.INDENT,"yes");
						tr.setOutputProperty(OutputKeys.METHOD,"xml");
						;
						tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

						// send DOM to file
						fos = new FileOutputStream(Util.getPOSIXPath(fname + room.getName() + ".room.gmx"));
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

		Element node = dom.createElement("rooms");
		node.setAttribute("name","rooms");
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

		Element helpNode = dom.createElement("help");
		Element rtfNode = dom.createElement("rtf");
		rtfNode.setTextContent("help.rtf");
		helpNode.appendChild(rtfNode);

		PrintWriter out = null;
		try
			{
			out = new PrintWriter(Util.getPOSIXPath(f.getDirectory() + "/help.rtf"));
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
			Element actelement = doc.createElement("action");
			root.appendChild(actelement);
			LibAction la = act.getLibAction();

			actelement.appendChild(createElement(doc,"libid",
					Integer.toString(la.parent != null ? la.parent.id : la.parentId)));
			actelement.appendChild(createElement(doc,"id",Integer.toString(la.id)));
			actelement.appendChild(createElement(doc,"kind",Integer.toString(la.actionKind)));
			actelement.appendChild(createElement(doc,"userelative",boolToString(la.allowRelative)));
			actelement.appendChild(createElement(doc,"useapplyto",boolToString(la.canApplyTo)));
			actelement.appendChild(createElement(doc,"isquestion",boolToString(la.question)));
			actelement.appendChild(createElement(doc,"exetype",Integer.toString(la.execType)));
			String execinfo = "";
			if (la.execType == Action.EXEC_FUNCTION)
				{
				execinfo = la.execInfo;
				}
			actelement.appendChild(createElement(doc,"functionname",execinfo));
			execinfo = "";
			if (la.execType == Action.EXEC_CODE)
				{
				execinfo = la.execInfo;
				}
			actelement.appendChild(createElement(doc,"codestring",execinfo));

			ResourceReference<GmObject> at = act.getAppliesTo();
			if (at != null)
				{
				if (at == GmObject.OBJECT_OTHER)
					actelement.appendChild(createElement(doc,"whoName","other"));
				else if (at == GmObject.OBJECT_SELF)
					actelement.appendChild(createElement(doc,"whoName","self"));
				else
					actelement.appendChild(createElement(doc,"whoName",at.get().getName()));
				}
			else
				actelement.appendChild(createElement(doc,"whoName","self"));

			actelement.appendChild(createElement(doc,"relative",boolToString(act.isRelative())));
			actelement.appendChild(createElement(doc,"isnot",boolToString(act.isNot())));

			// Now we write the arguments
			Element argsroot = doc.createElement("arguments");
			actelement.appendChild(argsroot);

			List<Argument> args = act.getArguments();
			for (Argument arg : args)
				{
				Element argelement = doc.createElement("argument");
				argsroot.appendChild(argelement);

				argelement.appendChild(createElement(doc,"kind",Integer.toString(arg.kind)));
				Class<? extends Resource<?,?>> kind = Argument.getResourceKind(arg.kind);
				if (kind != null && InstantiableResource.class.isAssignableFrom(kind))
					{
					Resource<?,?> r = deRef((ResourceReference<?>) arg.getRes());
					String name = "<undefined>";
					if (r != null && r instanceof InstantiableResource<?,?>)
						{
						name = ((InstantiableResource<?,?>) r).getName();
						}
					argelement.appendChild(createElement(doc,Resource.kindNames.get(kind).toLowerCase(),name));
					}
				else
					{
					argelement.appendChild(createElement(doc,"string",arg.getVal()));
					}
				}
			}
		}

	}
