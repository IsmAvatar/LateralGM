/*
 * Copyright (C) 2010, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Random;
import java.util.UUID;

import org.lateralgm.file.ProjectFile;
import org.lateralgm.file.iconio.ICOFile;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.sub.TextureGroup;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.PropertyMap;

public class GameSettings extends Resource<GameSettings,GameSettings.PGameSettings>
	{
	public static ICOFile DEFAULT_ICON = null;
	static
		{
		try
			{
			String loc = "org/lateralgm/file/default.ico"; //$NON-NLS-1$
			InputStream filein;
			File file = new File(loc);
			if (!file.exists())
				filein = LGM.class.getClassLoader().getResourceAsStream(loc);
			else
				filein = new FileInputStream(file);
			DEFAULT_ICON = new ICOFile(filein);
			}
		catch (Exception ex)
			{
			System.err.println(Messages.getString("GmFile.NOICON")); //$NON-NLS-1$
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			}
		}

	public Constants constants = new Constants();
	public final ActiveArrayList<TextureGroup> textureGroups = new ActiveArrayList<>();

	public GameSettings()
		{
		textureGroups.add(new TextureGroup());
		}

	public enum ColorDepth
		{
		NO_CHANGE,BIT_16,BIT_32
		}

	public enum Resolution
		{
		NO_CHANGE,RES_320X240,RES_640X480,RES_800X600,RES_1024X768,RES_1280X1024,RES_1600X1200
		}

	public enum Frequency
		{
		NO_CHANGE,FREQ_60,FREQ_70,FREQ_85,FREQ_100,FREQ_120
		}

	public enum Priority
		{
		NORMAL,HIGH,HIGHEST
		}

	public enum ProgressBar
		{
		NONE,DEFAULT,CUSTOM
		}

	//FIXME: Includes information moved
	public enum IncludeFolder
		{
		MAIN,TEMP
		}

	public enum PGameSettings
		{
		GAME_ID,GAME_GUID,START_FULLSCREEN,INTERPOLATE,FORCE_SOFTWARE_VERTEX_PROCESSING,
		DONT_DRAW_BORDER,DISPLAY_CURSOR,SCALING,ALLOW_WINDOW_RESIZE,ALWAYS_ON_TOP,COLOR_OUTSIDE_ROOM,/**/
		SET_RESOLUTION,COLOR_DEPTH,RESOLUTION,FREQUENCY,/**/
		DONT_SHOW_BUTTONS,USE_SYNCHRONIZATION,DISABLE_SCREENSAVERS,LET_F4_SWITCH_FULLSCREEN,
		LET_F1_SHOW_GAME_INFO,LET_ESC_END_GAME,LET_F5_SAVE_F6_LOAD,LET_F9_SCREENSHOT,
		TREAT_CLOSE_AS_ESCAPE,GAME_PRIORITY,/**/
		FREEZE_ON_LOSE_FOCUS,LOAD_BAR_MODE,FRONT_LOAD_BAR,BACK_LOAD_BAR,SHOW_CUSTOM_LOAD_IMAGE,
		LOADING_IMAGE,IMAGE_PARTIALLY_TRANSPARENTY,LOAD_IMAGE_ALPHA,SCALE_PROGRESS_BAR,/**/
		DISPLAY_ERRORS,WRITE_TO_LOG,ABORT_ON_ERROR,TREAT_UNINIT_AS_0,ERROR_ON_ARGS,AUTHOR,VERSION,
		LAST_CHANGED,INFORMATION,/**/
		INCLUDE_FOLDER,OVERWRITE_EXISTING,REMOVE_AT_GAME_END,VERSION_MAJOR,VERSION_MINOR,
		VERSION_RELEASE,VERSION_BUILD,COMPANY,PRODUCT,COPYRIGHT,DESCRIPTION,GAME_ICON,
		USE_NEW_AUDIO,SHORT_CIRCUIT_EVAL,USE_FAST_COLLISION,FAST_COLLISION_COMPAT,
		// Steam
		WINDOWS_STEAM_ID,MAC_STEAM_ID,LINUX_STEAM_ID,
		WINDOWS_STEAM_ENABLE,MAC_STEAM_ENABLE,LINUX_STEAM_ENABLE
		}

	//GAME_ID and GAME_GUID (DirectPlay) randomized in ProjectFile factory constructor
	private static final EnumMap<PGameSettings,Object> DEFS = PropertyMap.makeDefaultMap(
			PGameSettings.class,-1,new byte[16],false,false,false,false,true,-1,false,false,Color.BLACK,/**/
			false,ColorDepth.NO_CHANGE,Resolution.NO_CHANGE,Frequency.NO_CHANGE,/**/
			false,false,true,true,true,true,false,false,true,Priority.NORMAL,/**/
			true,ProgressBar.DEFAULT,null,null,false,null,false,255,true,/**/
			true,false,false,false,true,
			"","100",ProjectFile.longTimeToGmTime(System.currentTimeMillis()),"",/**///$NON-NLS-1$ //$NON-NLS-3$
			IncludeFolder.MAIN,false,false,1,0,0,0,"","","","",DEFAULT_ICON, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			true,true,false,false,
			// Steam
			0,0,0,false,false,false); 

	@Override
	public GameSettings makeInstance(ResourceReference<GameSettings> ref)
		{
		return new GameSettings();
		}

	@Override
	protected PropertyMap<PGameSettings> makePropertyMap()
		{
		return new PropertyMap<PGameSettings>(PGameSettings.class,this,DEFS);
		}

	@Override
	protected void postCopy(GameSettings dest)
		{ //Nothing else to copy
		}

	public Object validate(PGameSettings k, Object v)
		{
		return v;
		}

	public void put(PGameSettings key, Object value)
		{
		properties.put(key,value);
		}

	public <V>V get(PGameSettings key)
		{
		return properties.get(key);
		}

	public Double getLastChanged()
		{
		return (Double) properties.get(PGameSettings.LAST_CHANGED);
		}

	public void randomizeGameIds()
		{
		Random random = new Random();
		// generate new Id to be 9 digits like GameMaker
		int newId = 100000000 + random.nextInt(900000000);
		put(PGameSettings.GAME_ID,newId);
		// generate version 4 GUID just like GMSv1.4
		UUID uuid = UUID.randomUUID();
		setGUID(UUIDtoGUID(uuid));
		}

	/**
	 * Convert a Java UUID instance to a .NET/COM mixed-endian GUID byte array.
	 * 
	 * @param uuid The Java UUID instance to convert.
	 * @return A 16 byte array of the mixed-endian GUID.
	 */
	public static byte[] UUIDtoGUID(UUID uuid)
		{
		ByteBuffer source = ByteBuffer.allocate(16).
			putLong(uuid.getMostSignificantBits()).
			putLong(uuid.getLeastSignificantBits());
		// JDK 9+ overrides Buffer.rewind() in ByteBuffer
		// Use cast to maintain Java SE 7/8 Binary Compatibility
		((Buffer)source).rewind();
		return ByteBuffer.allocate(16).
			order(ByteOrder.LITTLE_ENDIAN).
			putInt(source.getInt()).
			putShort(source.getShort()).
			putShort(source.getShort()).
			order(ByteOrder.BIG_ENDIAN).
			putLong(source.getLong()).
			array();
		}

	/**
	 * Set the GUID from the text of the given GUID string. Characters are accepted in both
	 * uppercase and lowercase. Braces are stripped first if the GUID is enclosed by them.
	 * 
	 * @param guid String formatted GUID optionally enclosed by braces.
	 */
	public void setGUID(String guid)
		{
		guid = guid.replaceAll("[{}]",""); //$NON-NLS-1$ //$NON-NLS-2$
		UUID uuid = UUID.fromString(guid);
		put(PGameSettings.GAME_GUID, UUIDtoGUID(uuid));
		}

	/**
	 * Set the GUID from the given array of 16 bytes.
	 * 
	 * @param bytes Mixed endian array of 16 bytes representing the serialized GUID.
	 */
	public void setGUID(byte[] guid)
		{
		put(PGameSettings.GAME_GUID, guid);
		}

	/**
	 * This method serializes the game's GUID to Microsoft's string format, Uppercase letters
	 * are used and the GUID is enclosed by braces.
	 * 
	 * @return The GUID as a string in Microsoft's format.
	 */
	public String getGUIDAsString()
		{
		byte[] guid = get(PGameSettings.GAME_GUID);
		ByteBuffer source = ByteBuffer.wrap(guid);
		StringBuilder sb = new StringBuilder();
		source.order(ByteOrder.LITTLE_ENDIAN);
		sb.append('{');
		sb.append(String.format("%04X", source.getInt())); //$NON-NLS-1$
		sb.append('-');
		sb.append(String.format("%02X", source.getShort())); //$NON-NLS-1$
		sb.append('-');
		sb.append(String.format("%02X", source.getShort())); //$NON-NLS-1$
		sb.append('-');
		source.order(ByteOrder.BIG_ENDIAN);
		sb.append(String.format("%02X", source.getShort())); //$NON-NLS-1$
		sb.append('-');
		sb.append(String.format("%04X", source.getInt())); //$NON-NLS-1$
		sb.append(String.format("%02X", source.getShort())); //$NON-NLS-1$
		sb.append('}');
		return sb.toString();
		}

	/**
	 * This method serializes the game's GUID to Microsoft's mixed-endian format.
	 * https://docs.microsoft.com/en-us/dotnet/api/system.guid.tobytearray?view=net-5.0
	 * 
	 * @return The GUID serialized as an array of 16 bytes in Microsoft's mixed-endian format.
	 */
	public byte[] getGUID()
		{
		return get(PGameSettings.GAME_GUID);
		}
	}
