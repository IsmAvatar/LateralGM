package org.lateralgm.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class TaggedEnumMap<K extends Enum<K>,V> extends HashMap<K,V>
	{
	/**
	 * Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = -6742801350379436367L;

	private static class HiddenEnumMap implements Cloneable
		{
		int length = 0;
		Map<Integer,Integer> offsets = new HashMap<>();
		Map<Integer,Byte> types = new HashMap<>();
		Map<Long,HiddenEnumMap> transitions = new HashMap<>();

		public HiddenEnumMap clone()
			{
			HiddenEnumMap result = null;
			try
				{
				result = (HiddenEnumMap) super.clone();
				}
			catch (CloneNotSupportedException e)
				{
				throw new AssertionError();
				}
			result.offsets = (Map<Integer,Integer>) ((HashMap<Integer,Integer>)result.offsets).clone();
			result.types = (Map<Integer,Byte>) ((HashMap<Integer,Byte>)result.types).clone();
			result.transitions = new HashMap<>();
			return result;
			}

		public HiddenEnumMap transition(int key, byte type)
			{
			if (Byte.valueOf(type).equals(types.get(key))) return this;
			long composite = key;
			composite |= (type << 16);
			if (transitions.containsKey(composite)) 
				return transitions.get(composite);
			HiddenEnumMap next = this.clone();
			next.offsets.put(key,length);
			next.types.put(key,type);
			transitions.put(composite,next);
			return next;
			}
		}
	private static final HiddenEnumMap EMPTY_ROOT = new HiddenEnumMap();

	private HiddenEnumMap hiddenEnumMap = EMPTY_ROOT;
	private final Class<K> keyType;

	private int numEnums = 0;//PTile.values().length;

	BitSet has = null;
	ByteBuffer bytes = null;
	Object[] refs;

	public TaggedEnumMap(Class<K> keyType)
		{
		super();
		this.keyType = keyType;
		K[] enums = keyType.getEnumConstants();
		numEnums = enums.length;
		has = new BitSet(numEnums);
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
  public TaggedEnumMap<K, V> clone()
		{
		TaggedEnumMap<K, V> result = null;
		result = (TaggedEnumMap<K, V>) super.clone();
		ByteBuffer original = result.bytes;
		result.bytes = ByteBuffer.allocate(result.bytes.capacity());
		original.rewind(); // copy from the beginning
		result.bytes.put(original);
		original.rewind();
		//result.bytes.flip();
		result.refs = result.refs.clone();
		result.has = (BitSet) result.has.clone();
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
		has.clear(ko);
		int offset = hiddenEnumMap.offsets.get(ko);
		int type = hiddenEnumMap.types.get(ko);
		if (type == 0) refs[offset] = null;
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
		return has.get(ko);
		}

	private V putImpl(K key, V value)
		{
		V oldValue = get(key);
		int ko = key.ordinal();
		if (has.get(ko))
			{
			int offset = hiddenEnumMap.offsets.get(ko);
			int type = hiddenEnumMap.types.get(ko);
			if (type == 0) refs[offset] = null;
			}

		byte type = 0, typeSize = 0;
		long newValue = 0;
		if (value == null || value.getClass().isArray())
			{} // fall through
		else if (value instanceof Byte)
			{
			type = 1; typeSize = 1; newValue = (byte) value;
			}
		else if (value instanceof Character)
			{
			type = 2; typeSize = 2; newValue = (char) value;
			}
		else if (value instanceof Short)
			{
			type = 3; typeSize = 2; newValue = (short) value;
			}
		else if (value instanceof Integer)
			{
			type = 4; typeSize = 4; newValue = (int) value;
			}
		else if (value instanceof Long)
			{
			type = 5; typeSize = 8; newValue = (long) value;
			}
		else if (value instanceof Boolean)
			{
			type = 6; typeSize = 1; newValue = ((boolean) value ? 1 : 0);
			}
		else if (value instanceof Float)
			{
			type = 7; typeSize = 4; newValue = Float.floatToIntBits((float) value);
			}
		else if (value instanceof Double)
			{
			type = 8; typeSize = 8; newValue = Double.doubleToLongBits((double) value);
			}

		HiddenEnumMap oldMap = hiddenEnumMap;
		hiddenEnumMap = hiddenEnumMap.transition(ko,type);
		if (type == 0) 
			{
			int offset = 0;
			if (refs == null)
				refs = new Object[1];
			else if (!has.get(ko) || oldMap.types.get(ko) != 0)
				{
				offset = refs.length;
				refs = Arrays.copyOf(refs,refs.length+1);
				}
			else
				offset = oldMap.offsets.get(ko);
			hiddenEnumMap.offsets.put(ko,offset);
			refs[offset] = value;
			}
		else
			{
			int offset = 0;
			if (bytes == null)
				{
				bytes = ByteBuffer.allocate(typeSize);
				hiddenEnumMap.length += typeSize;
				}
			else if (!has.get(ko) || oldMap.types.get(ko) != type)
				{
				offset = bytes.capacity();
				hiddenEnumMap.length += typeSize;
				ByteBuffer original = bytes;
				bytes = ByteBuffer.allocate(bytes.capacity() + typeSize);
				original.rewind(); // copy from the beginning
				bytes.put(original);
				//bytes.flip();
				}
			else
				offset = oldMap.offsets.get(ko);
			hiddenEnumMap.offsets.put(ko,offset);
			switch (typeSize)
				{
				case 1: bytes.put(offset, (byte) newValue); break;
				case 2: bytes.putShort(offset, (short) newValue); break;
				case 4: bytes.putInt(offset, (int) newValue); break;
				case 8: default: bytes.putLong(offset, newValue); break;
				}
			}
		hiddenEnumMap.types.put(ko,type);
		has.set(ko);
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
		if (!has.get(ko)) return null;

		int offset = hiddenEnumMap.offsets.get(ko);
		byte type = hiddenEnumMap.types.get(ko);
		long value = 0;
		switch (type)
			{
			case 1: case 6: value = bytes.get(offset); break;
			case 2: case 3: value = bytes.getShort(offset); break;
			case 4: case 7: value = bytes.getInt(offset); break;
			case 5: case 8: value = bytes.getLong(offset); break;
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
		return (V) refs[offset];
		}
	}
