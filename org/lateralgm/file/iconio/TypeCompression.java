package org.lateralgm.file.iconio;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration class for bitmap compression types.
 *
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public final class TypeCompression
	{
	/** Maps type values to TypeCompression objects. */
	private static final Map<Long,TypeCompression> TYPES;
	/** Uncompressed (any BPP). */
	public static final TypeCompression BI_RGB = new TypeCompression("BI_RGB",0,
			"Uncompressed (any BPP)");
	/** 8 Bit RLE Compression (8 BPP only). */
	public static final TypeCompression BI_RLE8 = new TypeCompression("BI_RLE8",1,
			"8 Bit RLE Compression (8 BPP only)");
	/** 4 Bit RLE Compression (4 BPP only). */
	public static final TypeCompression BI_RLE4 = new TypeCompression("BI_RLE4",2,
			"4 Bit RLE Compression (4 BPP only)");
	/** Uncompressed (16 & 32 BPP only). */
	public static final TypeCompression BI_BITFIELDS = new TypeCompression("BI_BITFIELDS",3,
			"Uncompressed (16 & 32 BPP only)");
	public static final TypeCompression BI_PNG = new TypeCompression("BI_PNG",-1,"PNG Compression");
	static
		{
		TYPES = new HashMap<Long,TypeCompression>();
		register(BI_RGB);
		register(BI_RLE8);
		register(BI_RLE4);
		register(BI_BITFIELDS);
		register(BI_PNG);
		}
	private final int value;
	private final String name;
	private final String comment;

	/**
	 * @param pName
	 * @param pValue
	 */
	// @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:29
	private TypeCompression(final String pName, final int pValue, final String pComment)
		{
		name = pName;
		value = pValue;
		comment = pComment;

		}

	/**
	 * @param pType
	 */
	private static void register(final TypeCompression pType)
		{
		TYPES.put((long) pType.getValue(),pType);
		}

	/**
	 * Returns the name of the type and a comment.
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString()
		{
		return name + " (" + comment + ")";
		}

	/**
	 * Get the symbolic name.
	 *
	 * @return Returns the name.
	 */
	public String getName()
		{
		return name;
		}

	/**
	 * Get the numerical value.
	 *
	 * @return Returns the value.
	 */
	public int getValue()
		{
		return value;
		}

	/**
	 * Get a type for the specified numerical value.
	 *
	 * @param pValue Compression type integer value.
	 * @return Type for the value specified.
	 */
	public static TypeCompression getType(final long pValue)
		{
		final TypeCompression lResult = TYPES.get(pValue);
		if (lResult == null)
			{
			throw new IllegalArgumentException("Compression type " + pValue + " unknown");
			}

		return lResult;
		}
	}
