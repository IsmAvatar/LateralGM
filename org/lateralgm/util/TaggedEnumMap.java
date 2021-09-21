package org.lateralgm.util;

import java.util.EnumMap;
import java.util.Map;

public class TaggedEnumMap<K extends Enum<K>,V> extends EnumMap<K,V>
	{
	/**
	 * Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = -6742801350379436367L;

	private final Class<K> keyType;

	private int numEnums = 0;//PTile.values().length;
	private final int FIELD_SIZE = 8;
	byte[] types = new byte[numEnums];
	Object[][] references = new Object[numEnums][];
	byte[][] primitives = new byte[numEnums][];

	public TaggedEnumMap(Class<K> keyType)
		{
		super(keyType);
		this.keyType = keyType;
		K[] enums = keyType.getEnumConstants();
		numEnums = enums.length;
		types = new byte[numEnums];
		int wtf = (int) Math.ceil((double)numEnums/FIELD_SIZE);
		references = new Object[wtf][];
		primitives = new byte[wtf][];
		}

	public TaggedEnumMap(TaggedEnumMap<K, ? extends V> m)
		{
		this(m.keyType);
		for (Entry<K,? extends V> e : m.entrySet())
			putImpl(e.getKey(),e.getValue());
		}

	public TaggedEnumMap(Map<K, ? extends V> m)
		{
		this(m.keySet().iterator().next().getDeclaringClass());
		for (Entry<K,? extends V> e : m.entrySet())
			putImpl(e.getKey(),e.getValue());
		}

	@Override
  public TaggedEnumMap<K, V> clone() {
		TaggedEnumMap<K, V> result = null;
		result = (TaggedEnumMap<K, V>) super.clone();
		result.types = result.types.clone();
		result.references = result.references.clone();
		for (int i = 0; i < result.references.length; ++i)
			result.references[i] = result.references[i].clone();
		result.primitives = result.primitives.clone();
		for (int i = 0; i < result.primitives.length; ++i)
			result.primitives[i] = result.primitives[i].clone();
		return result;
	}

	private boolean isValidKey(Object key)
		{
		if (key == null) return false;

		// Cheaper than instanceof Enum followed by getDeclaringClass
		Class<?> keyClass = key.getClass();
		return keyClass == keyType || keyClass.getSuperclass() == keyType;
		}

	private V removeImpl(Object key)
		{
		V oldValue = this.get(key);
		if (!isValidKey(key)) return null;
		int ko = ((Enum<K>) key).ordinal();
		int bucket = ko / FIELD_SIZE;
		int cell = ko % FIELD_SIZE;
		types[ko] = 0;
		Object[] bucketArray = references[bucket];
		if (bucketArray != null)
			bucketArray[cell] = null;
		return oldValue;
		}

	@Override
	public V remove(Object key)
		{
		return removeImpl(key);
		}

	@Override
	public boolean containsKey(Object key)
		{
		if (!isValidKey(key)) return false;
		int ko = ((Enum<K>)key).ordinal();
		if (types[ko] == 0 && get(key) == null) return false;
		return true;
		}

	private V putImpl(K key, V value)
		{
		V oldValue = get(key);
		int ko = key.ordinal();
		int bucket = ko / FIELD_SIZE;
		int cell = ko % FIELD_SIZE;
		removeImpl(key);
		types[ko] = 0;
		int typeSize = 8;
		long newValue = 0;
		if (value instanceof Byte)
			{
			types[ko] = 1; typeSize = 1; newValue = (byte) value;
			}
		else if (value instanceof Character)
			{
			types[ko] = 2; typeSize = 2; newValue = (char) value;
			}
		else if (value instanceof Short)
			{
			types[ko] = 3; typeSize = 2; newValue = (short) value;
			}
		else if (value instanceof Integer)
			{
			types[ko] = 4; typeSize = 4; newValue = (int) value;
			}
		else if (value instanceof Long)
			{
			types[ko] = 5; typeSize = 8; newValue = (long) value;
			}
		else if (value instanceof Boolean)
			{
			types[ko] = 6; typeSize = 1; newValue = ((boolean) value ? 1 : 0);
			}
		else if (value instanceof Float)
			{
			types[ko] = 7; typeSize = 4; newValue = Float.floatToIntBits((float) value);
			}
		else if (value instanceof Double)
			{
			types[ko] = 8; typeSize = 8; newValue = Double.doubleToLongBits((double) value);
			}
		else
			{
			Object[] bucketArray = references[bucket];
			if (bucketArray == null)
				bucketArray = references[bucket] = new Object[FIELD_SIZE];
			bucketArray[cell] = value;
			return oldValue;
			}

		byte[] primitiveArray = primitives[bucket];
		int oldTypeSize = 0;
		if (primitiveArray != null)
			oldTypeSize = primitiveArray.length/FIELD_SIZE;
		if (typeSize > oldTypeSize)
			{
			primitives[bucket] = new byte[FIELD_SIZE*typeSize];
			if (primitiveArray != null)
				{
				for (int i = 0; i < FIELD_SIZE; ++i)
					for (int b = 0; b < oldTypeSize; ++b)
						primitives[bucket][i*typeSize+b] = primitiveArray[i*oldTypeSize+b];
				}
			primitiveArray = primitives[bucket];
			oldTypeSize = typeSize;
			}
		for (int i = 0; i < typeSize; ++i)
			primitives[bucket][cell*oldTypeSize+(oldTypeSize-i-1)] = (byte) ((newValue >> (8*i)) & 0xff);

		return oldValue;
		}

	@Override
	public V put(K key, V value)
		{
		return putImpl(key, value);
		}

	@Override
	public V get(Object key)
		{
		if (!isValidKey(key)) return null;
		int ko = ((Enum<K>) key).ordinal();
		int bucket = ko / FIELD_SIZE;
		int cell = ko % FIELD_SIZE;
		byte[] valueArray = primitives[bucket];
		byte type = types[ko];
		long value = 0;
		if (type != 0)
			{
			//if (valueArray == null)
					//return null;
			//else
				//{
				final int bucketSize = valueArray.length/FIELD_SIZE;
				for (int i = 0; i < bucketSize; ++i)
					value |= ((valueArray[cell*bucketSize+i] & 0xFF) << (7-i)*8);
				//}
			}
		switch (type)
			{
			case 1: return (V)(Byte)(byte) value;
			case 2: return (V)(Character)(char) value;
			case 3: return (V)(Short)(short) value;
			case 4: return (V)(Integer)(int) value;
			case 5: return (V)(Long)(long) value;
			case 6: return (V)(Boolean)(boolean) (value != 0);
			case 7: return (V)(Float)(float) Float.intBitsToFloat((int)value);
			case 8: return (V)(Double)(double) Double.longBitsToDouble(value);
			}
		Object[] referenceArray = references[bucket];
		if (referenceArray == null) return null;
		return (V) referenceArray[cell];
		}
	}
