/**
* @file  GMXFileReader.java
* @brief Class implementing a GMX file reader.
*
* @section License
*
* Copyright (C) 2013-2017 Robert B. Colton
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

import java.util.List;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.JProgressBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.ProjectFile.PostponedRef;
import org.lateralgm.file.ProjectFile.NamedPostponedRef;
import org.lateralgm.file.ProjectFile.IdMapPostponedRef;
import org.lateralgm.file.ProjectFile.NamedMapPostponedRef;
import org.lateralgm.file.ProjectFile.ResourceHolder;
import org.lateralgm.file.ProjectFile.SingletonResourceHolder;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.resources.Constants;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.Font.PFont;
import org.lateralgm.resources.GameInformation;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

public final class GMXFileReader
	{
	public static final String STUPID_SHADER_MARKER =
			"//######################_==_YOYO_SHADER_MARKER_==_######################@~"; //$NON-NLS-1$

	private static DocumentBuilderFactory documentBuilderFactory;
	private static DocumentBuilder documentBuilder;

	private GMXFileReader()
		{
		}

	private static Queue<PostponedRef<?>> postpone = new LinkedList<>();

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

	public static void readProjectFile(InputStream stream, ProjectFile file, URI uri, ResNode root)
			throws GmFormatException
		{
		readProjectFile(stream,file,uri,root,null);
		}

	public static void readProjectFile(InputStream stream, ProjectFile file, URI uri, ResNode root,
			Charset forceCharset) throws GmFormatException
		{
		file.format = ProjectFile.FormatFlavor.getVersionFlavor(1200); // GMX will have version numbers in the future
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
			progressBar.setMaximum(170);
			LGM.setProgressTitle(Messages.getString("ProgressDialog.GMX_LOADING")); //$NON-NLS-1$

			GMXFileReader.readTree(c,root);

			LGM.setProgress(160,Messages.getString("ProgressDialog.POSTPONED")); //$NON-NLS-1$
			// All resources read, now we can invoke our postponed references.
			for (PostponedRef<?> ref : postpone)
				{
				ref.invoke(file.resMap);
				}
			postpone.clear();

			LGM.setProgress(170,Messages.getString("ProgressDialog.FINISHED")); //$NON-NLS-1$
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

	// this map is used for picking the .resource.gmx file extension
	// it exists because some resources, i.e. Config, use names different from LGM
	// config also must be lowercase, as written by GMS, which we must copy
	// because operating systems like Linux are case-sensitive
	public static Map<Class<? extends Resource<?,?>>, String> gmxNames = new HashMap<>();
	static
		{
		gmxNames.put(Sprite.class,"sprite"); //$NON-NLS-1$
		gmxNames.put(Sound.class,"sound"); //$NON-NLS-1$
		gmxNames.put(Background.class,"background"); //$NON-NLS-1$
		gmxNames.put(Path.class,"path"); //$NON-NLS-1$
		//gmxNames.put(Script.class,"gml"); //$NON-NLS-1$
		//gmxNames.put(Shader.class,"shader"); //$NON-NLS-1$
		gmxNames.put(Font.class,"font"); //$NON-NLS-1$
		gmxNames.put(Timeline.class,"timeline"); //$NON-NLS-1$
		gmxNames.put(GmObject.class,"object"); //$NON-NLS-1$
		gmxNames.put(Room.class,"room"); //$NON-NLS-1$
		gmxNames.put(GameSettings.class,"config"); //$NON-NLS-1$
		//gmxNames.put(GameInformation.class,"rtf"); //$NON-NLS-1$
		}

	// this map is used to construct the tree in the appropriate order
	// it maps the GMX Node names, different from LGM, to the kinds
	public static Map<String, Class<? extends Resource<?,?>>> gmxNamesPlural = new LinkedHashMap<>();
	static
		{
		gmxNamesPlural.put("sprites",Sprite.class); //$NON-NLS-1$
		gmxNamesPlural.put("sounds",Sound.class); //$NON-NLS-1$
		gmxNamesPlural.put("backgrounds",Background.class); //$NON-NLS-1$
		gmxNamesPlural.put("paths",Path.class); //$NON-NLS-1$
		gmxNamesPlural.put("scripts",Script.class); //$NON-NLS-1$
		gmxNamesPlural.put("shaders",Shader.class); //$NON-NLS-1$
		gmxNamesPlural.put("fonts",Font.class); //$NON-NLS-1$
		gmxNamesPlural.put("timelines",Timeline.class); //$NON-NLS-1$
		gmxNamesPlural.put("objects",GmObject.class); //$NON-NLS-1$
		gmxNamesPlural.put("rooms",Room.class); //$NON-NLS-1$
		gmxNamesPlural.put("Configs",GameSettings.class); //$NON-NLS-1$
		gmxNamesPlural.put("help",GameInformation.class); //$NON-NLS-1$
		}

	private static void readTree(ProjectFileContext c, ResNode root)
		{
		Document in = c.in;
		Map<Class<? extends Resource<?,?>>, ResNode> primaryNodes = new HashMap<>();
		NodeList nodes = in.getDocumentElement().getChildNodes();

		for (int i = 0; i < nodes.getLength(); ++i)
			{
			Node node = nodes.item(i);
			if (!(node instanceof Element)) continue;
			String name = node.getNodeName();
			Class<? extends Resource<?,?>> kind = gmxNamesPlural.get(name);
			if (kind == null) continue;
			String kindName = Resource.kindNamesPlural.get(kind);
			byte status = InstantiableResource.class.isAssignableFrom(kind) ?
					ResNode.STATUS_PRIMARY : ResNode.STATUS_SECONDARY;

			LGM.setProgress(i * 10,Messages.format("ProgressDialog.LOADING",kindName)); //$NON-NLS-1$
			
			ResNode resNode = new ResNode(kindName,status,kind,null);
			GMXFileReader.readTree(c,node.getChildNodes(), resNode, kind);
			primaryNodes.put(kind, resNode);
			}

		for (Class<? extends Resource<?,?>> kind : Resource.kinds)
			{
			ResNode resNode = primaryNodes.get(kind);
			if (resNode == null)
				{
				String kindName = Resource.kindNamesPlural.get(kind);
				byte status = InstantiableResource.class.isAssignableFrom(kind) ?
						ResNode.STATUS_PRIMARY : ResNode.STATUS_SECONDARY;

				resNode = new ResNode(kindName,status,kind,null);
				}
			root.add(resNode);
			}
		}

	private static void readTree(ProjectFileContext c, NodeList nodes, ResNode root, Class kind)
		{
		ProjectFile f = c.f;
		for (int i = 0; i < nodes.getLength(); ++i)
			{
			Node node = nodes.item(i);
			if (!(node instanceof Element)) continue;
			Node nameAttribute = node.hasAttributes() ?
					node.getAttributes().getNamedItem("name") : null; //$NON-NLS-1$
			ResNode resNode = null;
			if (nameAttribute != null)
				{
				resNode = new ResNode(nameAttribute.getTextContent(),
						ResNode.STATUS_GROUP,kind,null);
				GMXFileReader.readTree(c,node.getChildNodes(),resNode,kind);
				}
			else
				{
				Resource<?,?> resource = null;
				ResourceHolder holder = f.resMap.get(kind);
				if (holder instanceof ResourceList)
					{
					ResourceList list = (ResourceList) holder;
					resource = list.add();
					}
				else if (holder instanceof SingletonResourceHolder)
					{
					resource = holder.getResource();
					}
				else
					{
					continue;
					}
				String filePath = f.getDirectory() + '/' + node.getTextContent();
				resource.setName(new File(Util.getPOSIXPath(filePath)).getName());
				String kindName = gmxNames.get(kind);
				File file = null;
				Document doc = null;
				if (kindName != null)
					{
					file = new File(filePath + '.' + gmxNames.get(kind) + ".gmx"); //$NON-NLS-1$
					doc = GMXFileReader.parseDocumentChecked(f, file.getAbsolutePath());
					}

				boolean skipNode = false;

				if (kind.equals(Sprite.class))
					{
					readSprite((Sprite)resource,doc,file);
					}
				else if (kind.equals(Sound.class))
					{
					readSound((Sound)resource,doc,file);
					}
				else if (kind.equals(Background.class))
					{
					readBackground((Background)resource,doc,file);
					}
				else if (kind.equals(Path.class))
					{
					readPath((Path)resource,doc,file);
					}
				else if (kind.equals(Script.class)
								|| kind.equals(Shader.class)
								|| kind.equals(GameInformation.class))
					{
					String text = ""; //$NON-NLS-1$
					try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) 
						{
						String line = ""; //$NON-NLS-1$

						while ((line = reader.readLine()) != null)
							{
							text += line + '\n';
							}
						}
					catch (FileNotFoundException e)
						{
						LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "file not found: " + filePath, e));
						}
					catch (IOException e)
						{
						LGM.showDefaultExceptionHandler(new GmFormatException(c.f, "unable to read file: " + filePath, e));
						}

					if (kind.equals(Script.class))
						{
						Script scr = (Script) resource;

						//TODO: String[] split = text.split(arg0);

						scr.setCode(text);
						}
					else if (kind.equals(Shader.class))
						{
						Shader shr = (Shader)resource;
						Node typeAttribute = node.hasAttributes() ?
								node.getAttributes().getNamedItem("type") : null; //$NON-NLS-1$
						shr.put(PShader.TYPE,typeAttribute.getNodeValue());

						String[] split = text.split(STUPID_SHADER_MARKER);

						shr.setVertexCode(split[0]);
						if (split.length > 0) shr.setFragmentCode(split[1]);
						}
					else if (kind.equals(GameInformation.class))
						{
						GameInformation gameInfo = (GameInformation) resource;
						gameInfo.put(PGameInformation.TEXT, text);
						}
					}
				else if (kind.equals(Font.class))
					{
					readFont((Font)resource,doc,file);
					}
				else if (kind.equals(Timeline.class))
					{
					readTimeline((Timeline)resource,doc,file);
					}
				else if (kind.equals(GmObject.class))
					{
					readGmObject((GmObject)resource,doc,file);
					}
				else if (kind.equals(Room.class))
					{
					readRoom((Room)resource,doc,file);
					}
				else if (kind.equals(GameSettings.class))
					{
					readConfig((GameSettings)resource,doc,file);
					}

				if (skipNode || kind.equals(GameInformation.class)) continue; // already has a node

				resNode = new ResNode(resource.getName(),
						ResNode.STATUS_SECONDARY,kind,resource.reference);
				}
			root.add(resNode);
			}
		}

	private static void readSprite(Sprite spr, Document doc, File file)
		{
		spr.put(PSprite.ORIGIN_X,
				Integer.parseInt(doc.getElementsByTagName("xorig").item(0).getTextContent())); //$NON-NLS-1$
		spr.put(PSprite.ORIGIN_Y,
				Integer.parseInt(doc.getElementsByTagName("yorigin").item(0).getTextContent())); //$NON-NLS-1$
		spr.put(PSprite.SHAPE,
				ProjectFile.SPRITE_MASK_SHAPE[Integer.parseInt(doc.getElementsByTagName("colkind").item( //$NON-NLS-1$
						0).getTextContent())]);
		spr.put(PSprite.SEPARATE_MASK,
				Integer.parseInt(doc.getElementsByTagName("sepmasks").item(0).getTextContent()) != 0); //$NON-NLS-1$
		spr.put(PSprite.BB_MODE,
				ProjectFile.SPRITE_BB_MODE[Integer.parseInt(doc.getElementsByTagName("bboxmode").item( //$NON-NLS-1$
						0).getTextContent())]);
		spr.put(PSprite.BB_LEFT,
				Integer.parseInt(doc.getElementsByTagName("bbox_left").item(0).getTextContent())); //$NON-NLS-1$
		spr.put(PSprite.BB_RIGHT,
				Integer.parseInt(doc.getElementsByTagName("bbox_right").item(0).getTextContent())); //$NON-NLS-1$
		spr.put(PSprite.BB_TOP,
				Integer.parseInt(doc.getElementsByTagName("bbox_top").item(0).getTextContent())); //$NON-NLS-1$
		spr.put(PSprite.BB_BOTTOM,
				Integer.parseInt(doc.getElementsByTagName("bbox_bottom").item(0).getTextContent())); //$NON-NLS-1$
		spr.put(PSprite.ALPHA_TOLERANCE,
				Integer.parseInt(doc.getElementsByTagName("coltolerance").item(0).getTextContent())); //$NON-NLS-1$

		spr.put(PSprite.TILE_HORIZONTALLY,
				Integer.parseInt(doc.getElementsByTagName("HTile").item(0).getTextContent()) != 0); //$NON-NLS-1$
		spr.put(PSprite.TILE_VERTICALLY,
				Integer.parseInt(doc.getElementsByTagName("VTile").item(0).getTextContent()) != 0); //$NON-NLS-1$

		// TODO: Read texture groups

		spr.put(PSprite.FOR3D,
				Integer.parseInt(doc.getElementsByTagName("For3D").item(0).getTextContent()) != 0); //$NON-NLS-1$

		// iterate and load the sprites subimages
		NodeList frList = doc.getElementsByTagName("frame"); //$NON-NLS-1$
		String path = file.getParent() + '/';
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
					LGM.showDefaultExceptionHandler(e);
					}
				}
			}
		}

	private static void readSound(Sound snd, Document doc, File file)
		{
		snd.put(PSound.FILE_NAME,doc.getElementsByTagName("origname").item(0).getTextContent()); //$NON-NLS-1$
		// GMX uses double nested tags for volume, bit rate, sample rate, type, and bit depth
		// There is a special clause here, every one of those tags after volume, the nested
		// tag is singular, where its parent is plural.
		NodeList nl = doc.getElementsByTagName("volume"); //$NON-NLS-1$
		snd.put(PSound.VOLUME,Double.parseDouble(nl.item(nl.getLength() - 1).getTextContent()));
		snd.put(PSound.PAN,
			Double.parseDouble(doc.getElementsByTagName("pan").item(0).getTextContent())); //$NON-NLS-1$
		snd.put(PSound.BIT_RATE,
			Integer.parseInt(doc.getElementsByTagName("bitRate").item(0).getTextContent())); //$NON-NLS-1$
		snd.put(PSound.SAMPLE_RATE,
			Integer.parseInt(doc.getElementsByTagName("sampleRate").item(0).getTextContent())); //$NON-NLS-1$
		int sndtype = Integer.parseInt(doc.getElementsByTagName("type").item(0).getTextContent()); //$NON-NLS-1$
		snd.put(PSound.TYPE, ProjectFile.SOUND_TYPE[sndtype]);
		snd.put(PSound.BIT_DEPTH,
			Integer.parseInt(doc.getElementsByTagName("bitDepth").item(0).getTextContent())); //$NON-NLS-1$
		snd.put(PSound.PRELOAD,
			Integer.parseInt(doc.getElementsByTagName("preload").item(0).getTextContent()) != 0); //$NON-NLS-1$
		snd.put(PSound.COMPRESSED,
			Integer.parseInt(doc.getElementsByTagName("compressed").item(0).getTextContent()) != 0); //$NON-NLS-1$
		snd.put(PSound.STREAMED,
			Integer.parseInt(doc.getElementsByTagName("streamed").item(0).getTextContent()) != 0); //$NON-NLS-1$
		snd.put(PSound.DECOMPRESS_ON_LOAD,
			Integer.parseInt(doc.getElementsByTagName("uncompressOnLoad").item(0).getTextContent()) != 0); //$NON-NLS-1$
		int sndkind = Integer.parseInt(doc.getElementsByTagName("kind").item(0).getTextContent()); //$NON-NLS-1$
		snd.put(PSound.KIND,ProjectFile.SOUND_KIND[sndkind]);
		snd.put(PSound.FILE_TYPE,doc.getElementsByTagName("extension").item(0).getTextContent()); //$NON-NLS-1$

		Node dataNode = doc.getElementsByTagName("data").item(0); //$NON-NLS-1$
		if (dataNode == null) return; // not an error, it didn't say it had any data

		String path = file.getParent() + "/audio/"; //$NON-NLS-1$

		snd.data = Util.readBinaryFile(path + Util.getPOSIXPath(dataNode.getTextContent()));
		}

	private static void readBackground(Background bkg, Document doc, File file)
		{
		bkg.put(PBackground.USE_AS_TILESET,
				Integer.parseInt(doc.getElementsByTagName("istileset").item(0).getTextContent()) != 0); //$NON-NLS-1$
		bkg.put(PBackground.TILE_WIDTH,
				Integer.parseInt(doc.getElementsByTagName("tilewidth").item(0).getTextContent())); //$NON-NLS-1$
		bkg.put(PBackground.TILE_HEIGHT,
				Integer.parseInt(doc.getElementsByTagName("tileheight").item(0).getTextContent())); //$NON-NLS-1$
		bkg.put(PBackground.H_OFFSET,
				Integer.parseInt(doc.getElementsByTagName("tilexoff").item(0).getTextContent())); //$NON-NLS-1$
		bkg.put(PBackground.V_OFFSET,
				Integer.parseInt(doc.getElementsByTagName("tileyoff").item(0).getTextContent())); //$NON-NLS-1$
		bkg.put(PBackground.H_SEP,
				Integer.parseInt(doc.getElementsByTagName("tilehsep").item(0).getTextContent())); //$NON-NLS-1$
		bkg.put(PBackground.V_SEP,
				Integer.parseInt(doc.getElementsByTagName("tilevsep").item(0).getTextContent())); //$NON-NLS-1$
		bkg.put(PBackground.TILE_HORIZONTALLY,
				Integer.parseInt(doc.getElementsByTagName("HTile").item(0).getTextContent()) != 0); //$NON-NLS-1$
		bkg.put(PBackground.TILE_VERTICALLY,
				Integer.parseInt(doc.getElementsByTagName("VTile").item(0).getTextContent()) != 0); //$NON-NLS-1$

		// TODO: Read texture groups

		bkg.put(PBackground.FOR3D,
				Integer.parseInt(doc.getElementsByTagName("For3D").item(0).getTextContent()) != 0); //$NON-NLS-1$

		Node dataNode = doc.getElementsByTagName("data").item(0); //$NON-NLS-1$
		if (dataNode == null) return; // not an error, it didn't say it had any data

		String path = file.getParent() + '/';
		File dataFile = new File(path + Util.getPOSIXPath(dataNode.getTextContent()));

		BufferedImage img = null;
		try
			{
			img = ImageIO.read(dataFile);
			bkg.setBackgroundImage(img);
			}
		catch (IOException e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		}

	private static void readPath(Path pth, Document pthdoc, File file)
		{
		pth.put(PPath.SMOOTH,
				Integer.parseInt(pthdoc.getElementsByTagName("kind").item(0).getTextContent()) != 0); //$NON-NLS-1$
		pth.put(PPath.PRECISION,
				Integer.parseInt(pthdoc.getElementsByTagName("precision").item(0).getTextContent())); //$NON-NLS-1$
		pth.put(PPath.CLOSED,
				Integer.parseInt(pthdoc.getElementsByTagName("closed").item(0).getTextContent()) != 0); //$NON-NLS-1$
		final int backroom = Integer.parseInt(pthdoc.getElementsByTagName("backroom").item(0).getTextContent()); //$NON-NLS-1$

		if (backroom >= 0)
			{
				postpone.add(new IdMapPostponedRef(Room.class, backroom, pth.properties, PPath.BACKGROUND_ROOM));
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

	private static void readFont(Font fnt, Document doc, File file)
		{
		fnt.put(PFont.FONT_NAME,doc.getElementsByTagName("name").item(0).getTextContent()); //$NON-NLS-1$
		fnt.put(PFont.SIZE,
				Integer.parseInt(doc.getElementsByTagName("size").item(0).getTextContent())); //$NON-NLS-1$
		fnt.put(PFont.BOLD,
				Integer.parseInt(doc.getElementsByTagName("bold").item(0).getTextContent()) != 0); //$NON-NLS-1$
		fnt.put(PFont.ITALIC,
				Integer.parseInt(doc.getElementsByTagName("italic").item(0).getTextContent()) != 0); //$NON-NLS-1$
		fnt.put(PFont.CHARSET,
				Integer.parseInt(doc.getElementsByTagName("charset").item(0).getTextContent())); //$NON-NLS-1$
		fnt.put(PFont.ANTIALIAS,
				Integer.parseInt(doc.getElementsByTagName("aa").item(0).getTextContent())); //$NON-NLS-1$
		NodeList ranges = doc.getElementsByTagName("range0"); //$NON-NLS-1$
		for (int item = 0; item < ranges.getLength(); item++)
			{
			String[] range = ranges.item(item).getTextContent().split(","); //$NON-NLS-1$
			fnt.addRange(Integer.parseInt(range[0]),Integer.parseInt(range[1]));
			}

		NodeList glyphs = doc.getElementsByTagName("glyph"); //$NON-NLS-1$
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

	private static void readTimeline(Timeline tml, Document doc, File file)
		{
		//Iterate the moments and load the actions
		NodeList frList = doc.getElementsByTagName("entry"); //$NON-NLS-1$
		for (int i = 0; i < frList.getLength(); ++i)
			{
			Node fnode = frList.item(i);
			Moment mom = tml.addMoment();

			NodeList children = fnode.getChildNodes();
			for (int x = 0; x < children.getLength(); ++x)
				{
				Node cnode = children.item(x);
				if (!(cnode instanceof Element)) continue;

				if (cnode.getNodeName().equals("step")) //$NON-NLS-1$
					{
					mom.stepNo = Integer.parseInt(cnode.getTextContent());
					}
				else if (cnode.getNodeName().equals("event")) //$NON-NLS-1$
					{
					readActions(mom,cnode.getChildNodes());
					}
				}
			}
		}

	private static void readGmObject(GmObject obj, Document doc, File file)
		{
		String sprname = doc.getElementsByTagName("spriteName").item(0).getTextContent(); //$NON-NLS-1$
		if (!sprname.equals("<undefined>"))
			{
			postpone.add(new NamedMapPostponedRef(Sprite.class, sprname, obj.properties, PGmObject.SPRITE));
			}
		else
			{
			obj.put(PGmObject.SPRITE,null);
			}

		String mskname = doc.getElementsByTagName("maskName").item(0).getTextContent(); //$NON-NLS-1$
		if (!mskname.equals("<undefined>"))
			{
			postpone.add(new NamedMapPostponedRef(Sprite.class, mskname, obj.properties, PGmObject.MASK));
			}
		else
			{
			obj.put(PGmObject.MASK,null);
			}

		String parname = doc.getElementsByTagName("parentName").item(0).getTextContent(); //$NON-NLS-1$
		if (!parname.equals("<undefined>") && !parname.equals("self"))
			{
			postpone.add(new NamedMapPostponedRef(GmObject.class, parname, obj.properties, PGmObject.PARENT));
			}
		else
			{
			obj.put(PGmObject.PARENT,null);
			}

		obj.put(PGmObject.SOLID,
				Integer.parseInt(doc.getElementsByTagName("solid").item(0).getTextContent()) != 0); //$NON-NLS-1$
		obj.put(PGmObject.VISIBLE,
				Integer.parseInt(doc.getElementsByTagName("visible").item(0).getTextContent()) != 0); //$NON-NLS-1$
		obj.put(PGmObject.DEPTH,
				Integer.parseInt(doc.getElementsByTagName("depth").item(0).getTextContent())); //$NON-NLS-1$
		obj.put(
				PGmObject.PERSISTENT,
				Integer.parseInt(doc.getElementsByTagName("persistent").item(0).getTextContent()) != 0); //$NON-NLS-1$

		// Now that properties are loaded iterate the events and load the actions
		NodeList frList = doc.getElementsByTagName("event"); //$NON-NLS-1$
		for (int i = 0; i < frList.getLength(); ++i)
			{
			Node fnode = frList.item(i);
			final Event ev = new Event();

			ev.mainId = Integer.parseInt(fnode.getAttributes().getNamedItem("eventtype").getTextContent()); //$NON-NLS-1$
			MainEvent me = obj.mainEvents.get(ev.mainId);
			me.events.add(0,ev);
			if (ev.mainId == MainEvent.EV_COLLISION)
				{
				final String colname = fnode.getAttributes().getNamedItem("ename").getTextContent(); //$NON-NLS-1$
				postpone.add(new NamedPostponedRef<GmObject>(GmObject.class, colname) {
					@Override
					public boolean set(ResourceReference<GmObject> ref)
						{
						if (ref != null) ev.other = ref;
						return ref != null;
						}
				});
				}
			else
				{
				ev.id = Integer.parseInt(fnode.getAttributes().getNamedItem("enumb").getTextContent()); //$NON-NLS-1$
				}
			readActions(ev,fnode.getChildNodes());
			}
		obj.put(
				PGmObject.PHYSICS_OBJECT,
				Integer.parseInt(doc.getElementsByTagName("PhysicsObject").item(0).getTextContent()) != 0); //$NON-NLS-1$
		obj.put(
				PGmObject.PHYSICS_SENSOR,
				Integer.parseInt(doc.getElementsByTagName("PhysicsObjectSensor").item(0).getTextContent()) != 0); //$NON-NLS-1$
		int shapekind = Integer.parseInt(doc.getElementsByTagName("PhysicsObjectShape").item(0).getTextContent()); //$NON-NLS-1$
		obj.put(PGmObject.PHYSICS_SHAPE,ProjectFile.PHYSICS_SHAPE[shapekind]);
		obj.put(
				PGmObject.PHYSICS_DENSITY,
				Double.parseDouble(doc.getElementsByTagName("PhysicsObjectDensity").item(0).getTextContent())); //$NON-NLS-1$
		obj.put(
				PGmObject.PHYSICS_RESTITUTION,
				Double.parseDouble(doc.getElementsByTagName("PhysicsObjectRestitution").item(0).getTextContent())); //$NON-NLS-1$
		obj.put(
				PGmObject.PHYSICS_GROUP,
				Integer.parseInt(doc.getElementsByTagName("PhysicsObjectGroup").item(0).getTextContent())); //$NON-NLS-1$
		obj.put(
				PGmObject.PHYSICS_DAMPING_LINEAR,
				Double.parseDouble(doc.getElementsByTagName("PhysicsObjectLinearDamping").item(0).getTextContent())); //$NON-NLS-1$
		obj.put(
				PGmObject.PHYSICS_DAMPING_ANGULAR,
				Double.parseDouble(doc.getElementsByTagName("PhysicsObjectAngularDamping").item(0).getTextContent())); //$NON-NLS-1$
		//NOTE: Some versions of the format did not have all of the physics properties.
		Node fNode = doc.getElementsByTagName("PhysicsObjectFriction").item(0); //$NON-NLS-1$
		if (fNode != null)
			{
			obj.put(PGmObject.PHYSICS_FRICTION,Double.parseDouble(fNode.getTextContent()));
			obj.put(
					PGmObject.PHYSICS_AWAKE,
					Integer.parseInt(doc.getElementsByTagName("PhysicsObjectAwake").item(0).getTextContent()) != 0); //$NON-NLS-1$
			obj.put(
					PGmObject.PHYSICS_KINEMATIC,
					Integer.parseInt(doc.getElementsByTagName("PhysicsObjectKinematic").item(0).getTextContent()) != 0); //$NON-NLS-1$
			}

		NodeList pointNodes = doc.getElementsByTagName("point"); //$NON-NLS-1$
		for (int p = 0; p < pointNodes.getLength(); p++)
			{
			String[] coords = pointNodes.item(p).getTextContent().split(","); //$NON-NLS-1$
			obj.shapePoints.add(new ShapePoint(Integer.parseInt(coords[0]),
					Integer.parseInt(coords[1])));
			}
		}

	private static void readRoom(Room rmn, Document rmndoc, File file)
		{
		String caption = rmndoc.getElementsByTagName("caption").item(0).getTextContent(); //$NON-NLS-1$
		rmn.put(PRoom.CAPTION,caption);

		NodeList cnodes = rmndoc.getElementsByTagName("room").item(0).getChildNodes(); //$NON-NLS-1$
		for (int x = 0; x < cnodes.getLength(); x++)
			{
			Node pnode = cnodes.item(x);
			if (!(pnode instanceof Element)) continue;
			String pname = pnode.getNodeName();

			if (pname.equals("caption")) //$NON-NLS-1$
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
					if (!(mnode instanceof Element)) continue;
					String mname = mnode.getNodeName();

					if (mname.equals("isSet")) //$NON-NLS-1$
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
					if (!bnode.getNodeName().equals("background")) continue; //$NON-NLS-1$

					final BackgroundDef bkg = rmn.backgroundDefs.get(bkgnum);
					bkgnum += 1;

					bkg.properties.put(
							PBackgroundDef.VISIBLE,
							Integer.parseInt(bnode.getAttributes().getNamedItem("visible").getTextContent()) != 0); //$NON-NLS-1$
					final String bkgname = bnode.getAttributes().getNamedItem("name").getTextContent(); //$NON-NLS-1$

					postpone.add(new NamedMapPostponedRef(Background.class, bkgname, bkg.properties, PBackgroundDef.BACKGROUND));

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
					if (!vnode.getNodeName().equals("view")) continue; //$NON-NLS-1$

					final View vw = rmn.views.get(viewnum);
					viewnum += 1;

					vw.properties.put(
							PView.VISIBLE,
							Integer.parseInt(vnode.getAttributes().getNamedItem("visible").getTextContent()) != 0); //$NON-NLS-1$
					final String objname = vnode.getAttributes().getNamedItem("objName").getTextContent(); //$NON-NLS-1$

					postpone.add(new NamedMapPostponedRef(GmObject.class, objname, vw.properties, PView.OBJECT));

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
					if (!inode.getNodeName().equals("instance")) continue; //$NON-NLS-1$

					Instance inst = rmn.addInstance();

					String objname = inode.getAttributes().getNamedItem("objName").getTextContent(); //$NON-NLS-1$
					postpone.add(new NamedMapPostponedRef(GmObject.class, objname, inst.properties, PInstance.OBJECT));

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
					/*
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

					inst.properties.put(PInstance.ID, instid);*/

					inst.setPosition(new Point(xx,yy));
					inst.setScale(new Point2D.Double(sx,sy));
					inst.setRotation(rot);
					inst.setCreationCode(inode.getAttributes().getNamedItem("code").getNodeValue()); //$NON-NLS-1$
					inst.setLocked(Integer.parseInt(inode.getAttributes().getNamedItem("locked").getNodeValue()) != 0); //$NON-NLS-1$
					}
				}
			else if (pname.equals("tiles")) //$NON-NLS-1$
				{
				NodeList tinodes = pnode.getChildNodes();
				for (int p = 0; p < tinodes.getLength(); p++)
					{
					Node tnode = tinodes.item(p);
					if (!tnode.getNodeName().equals("tile")) continue; //$NON-NLS-1$

					final Tile tile = new Tile(rmn);

					NamedNodeMap attribs = tnode.getAttributes();

					tile.setPosition(new Point(
							Integer.parseInt(attribs.getNamedItem("x").getTextContent()), //$NON-NLS-1$
							Integer.parseInt(attribs.getNamedItem("y").getTextContent()))); //$NON-NLS-1$

					final String bkgname = tnode.getAttributes().getNamedItem("bgName").getTextContent(); //$NON-NLS-1$
					postpone.add(new NamedMapPostponedRef(Background.class, bkgname, tile.properties, PTile.BACKGROUND));

					tile.properties.put(PTile.NAME, attribs.getNamedItem("name").getNodeValue()); //$NON-NLS-1$

					/*
					int tileid = Integer.parseInt(attribs.getNamedItem("id").getTextContent()); //$NON-NLS-1$
					if (tileid > f.lastTileId) {
						f.lastTileId = tileid;
					}
					tile.properties.put(PTile.ID,tileid);*/

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

	private static void readConfig(GameSettings cgf, Document setdoc, File file)
		{
		cgf.put(
				PGameSettings.START_FULLSCREEN,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_fullscreen").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.ALLOW_WINDOW_RESIZE,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_sizeable").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.ALWAYS_ON_TOP,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_stayontop").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.ABORT_ON_ERROR,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_aborterrors").item(0).getTextContent())); //$NON-NLS-1$
		// TODO: This value is stored using the Windows native dialog's name for the color value, ie
		// "clBlack" or "clWhite" meaning black and white respectively. If the user chooses a custom
		// defined color in the dialog, then the value is in the hexadecimal form "$HHHHHHHH" using
		// a dollar sign instead of a hash sign as a normal hex color value does in other places in
		// the same configuration file.
		// This will not be compatible if they ever try to port their IDE to other platforms.
		//gSet.put(PGameSettings.COLOR_OUTSIDE_ROOM, Integer.parseInt(setdoc.getElementsByTagName("option_windowcolor").item(0).getTextContent()));
		cgf.put(
				PGameSettings.DISABLE_SCREENSAVERS,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_noscreensaver").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.DISPLAY_CURSOR,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_showcursor").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.DISPLAY_ERRORS,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_displayerrors").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.DONT_DRAW_BORDER,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_noborder").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.DONT_SHOW_BUTTONS,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_nobuttons").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.ERROR_ON_ARGS,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_argumenterrors").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.FREEZE_ON_LOSE_FOCUS,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_freeze").item(0).getTextContent())); //$NON-NLS-1$

		cgf.put(
				PGameSettings.COLOR_DEPTH,
				ProjectFile.GS_DEPTHS[Integer.parseInt(setdoc.getElementsByTagName("option_colordepth").item( //$NON-NLS-1$
						0).getTextContent())]);
		cgf.put(
				PGameSettings.FREQUENCY,
				ProjectFile.GS_FREQS[Integer.parseInt(setdoc.getElementsByTagName("option_frequency").item( //$NON-NLS-1$
						0).getTextContent())]);
		cgf.put(
				PGameSettings.RESOLUTION,
				ProjectFile.GS_RESOLS[Integer.parseInt(setdoc.getElementsByTagName("option_resolution").item( //$NON-NLS-1$
						0).getTextContent())]);
		cgf.put(
				PGameSettings.SET_RESOLUTION,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_changeresolution").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.GAME_PRIORITY,
				ProjectFile.GS_PRIORITIES[Integer.parseInt(setdoc.getElementsByTagName(
						"option_priority").item(0).getTextContent())]); //$NON-NLS-1$

		// For some odd reason these two settings are combined together.
		// 2147483649 - Both
		// 2147483648 - Software Vertex Processing only
		// 1 - Synchronization Only
		// 0 - None
		long syncvertex = Long.parseLong(setdoc.getElementsByTagName("option_sync_vertex").item(0).getTextContent()); //$NON-NLS-1$
		cgf.put(PGameSettings.USE_SYNCHRONIZATION,(syncvertex == 2147483649L || syncvertex == 1));
		cgf.put(PGameSettings.FORCE_SOFTWARE_VERTEX_PROCESSING,
				(syncvertex == 2147483649L || syncvertex == 2147483648L));

		cgf.put(
				PGameSettings.LET_ESC_END_GAME,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_closeesc").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.INTERPOLATE,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_interpolate").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(PGameSettings.SCALING,
				Integer.parseInt(setdoc.getElementsByTagName("option_scale").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.TREAT_CLOSE_AS_ESCAPE,
				Boolean.parseBoolean(setdoc.getElementsByTagName("option_closeesc").item(0).getTextContent())); //$NON-NLS-1$
		String changed = setdoc.getElementsByTagName("option_lastchanged").item(0).getTextContent(); //$NON-NLS-1$
		if (changed != "")
			{
			cgf.put(PGameSettings.LAST_CHANGED,Double.parseDouble(changed));
			}

		// TODO: Could not find these properties in GMX
		//gSet.put(PGameSettings.BACK_LOAD_BAR,
		//	Boolean.parseBoolean(setdoc.getElementsByTagName("option_stayontop").item(0).getTextContent()));
		//gSet.put(PGameSettings.FRONT_LOAD_BAR,
		//	Boolean.parseBoolean(setdoc.getElementsByTagName("option_showcursor").item(0).getTextContent()));

		String icopath = file.getParentFile().getParent() + '/'
				+ setdoc.getElementsByTagName("option_windows_game_icon").item(0).getTextContent(); //$NON-NLS-1$
		try
			{
			cgf.put(PGameSettings.GAME_ICON,new ICOFile(icopath));
			}
		catch (IOException e)
			{
			LGM.showDefaultExceptionHandler(e);
			}

		cgf.put(PGameSettings.GAME_ID,
				Integer.parseInt(setdoc.getElementsByTagName("option_gameid").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.GAME_GUID,
				HexBin.decode(setdoc.getElementsByTagName("option_gameguid").item(0).getTextContent().replace( //$NON-NLS-1$
						"-","").replace("{","").replace("}","")));

		cgf.put(PGameSettings.AUTHOR,
				setdoc.getElementsByTagName("option_author").item(0).getTextContent()); //$NON-NLS-1$
		cgf.put(PGameSettings.VERSION,
				setdoc.getElementsByTagName("option_version").item(0).getTextContent()); //$NON-NLS-1$
		cgf.put(PGameSettings.INFORMATION,
				setdoc.getElementsByTagName("option_information").item(0).getTextContent()); //$NON-NLS-1$
		cgf.put(PGameSettings.COMPANY,
				setdoc.getElementsByTagName("option_version_company").item(0).getTextContent()); //$NON-NLS-1$
		cgf.put(PGameSettings.COPYRIGHT,
				setdoc.getElementsByTagName("option_version_copyright").item(0).getTextContent()); //$NON-NLS-1$
		cgf.put(PGameSettings.DESCRIPTION,
				setdoc.getElementsByTagName("option_version_description").item(0).getTextContent()); //$NON-NLS-1$
		cgf.put(PGameSettings.PRODUCT,
				setdoc.getElementsByTagName("option_version_product").item(0).getTextContent()); //$NON-NLS-1$
		cgf.put(
				PGameSettings.VERSION_BUILD,
				Integer.parseInt(setdoc.getElementsByTagName("option_version_build").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.VERSION_MAJOR,
				Integer.parseInt(setdoc.getElementsByTagName("option_version_major").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
				PGameSettings.VERSION_MINOR,
				Integer.parseInt(setdoc.getElementsByTagName("option_version_minor").item(0).getTextContent())); //$NON-NLS-1$
		cgf.put(
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
				if (!(cnstNode instanceof Element)) continue;
				String cnstName = cnstNode.getNodeName();

				if (cnstName.toLowerCase().equals("constants")) //$NON-NLS-1$
					{
					found = true;
					break;
					}
				}

			if (found) readConstants(cgf.constants,cnstNode);

			}
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

	private static void readActions(ActionContainer container, NodeList actList)
		{
		for (int i = 0; i < actList.getLength(); i++)
			{
			Node actNode = actList.item(i);
			if (!(actNode instanceof Element)) continue;

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
				if (!(prop instanceof Element)) continue;

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

					List<Node> argList = new ArrayList<>();
					for (int x = 0; x < targList.getLength(); x++)
						{
						Node arg = targList.item(x);
						if (!(arg instanceof Element)) continue;

						argList.add(arg);
						}

					args = new Argument[argList.size()];

					for (int x = 0; x < argList.size(); x++)
						{
						Node arg = argList.get(x);
						if (!(arg instanceof Element)) continue;

						args[x] = new Argument((byte) 0);

						NodeList argproplist = arg.getChildNodes();
						for (int xx = 0; xx < argproplist.getLength(); xx++)
							{
							Node argprop = argproplist.item(xx);
							if (!(argprop instanceof Element)) continue;

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
								if (kindc != null && Resource.class.isAssignableFrom(kindc))
									{
									postpone.add(new NamedPostponedRef(Argument.getResourceKind(argument.kind), proptext) {
										@Override
										public boolean set(ResourceReference ref)
											{
											if (ref != null)
												{
												argument.setRes(ref);
												argument.setVal(proptext);
												}
											return ref != null;
											}
									});
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
				postpone.add(new NamedPostponedRef<GmObject>(GmObject.class, objname) {
					@Override
					public boolean set(ResourceReference<GmObject> ref)
						{
						if (ref != null) act.setAppliesTo(ref);
						return ref != null;
						}
				});
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
