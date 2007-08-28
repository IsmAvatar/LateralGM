/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

/**
 * This class represents a reference to a <code>Resource</code>.
 * It is, in effect, a modified and rethought version of
 * the old <code>ResId</code> class. You may wonder why in most cases
 * it isn't a good idea to directly reference a <code>Resource</code>.
 * The simple answer is that if you want to destroy it later and
 * free the memory, you must either implement a state pattern,
 * or manually remove every reference to it.
 * Both of these solutions are undesirable, so instead we reference
 * an instance of this class, which in turn references the Resource.
 * If we want to  allow the Resource to be garbage collected, all we have to
 * do is null this class' reference, which is achieved by calling
 * <code>delete()</code>.
 * 
 * <h2>Example Usage</h2>
 * <br>
 * <b>To declare a Reference to a <code>Sprite</code>:</b><pre>Ref&ltSprite&gt ref;</pre>
 * <b>To get the <code>Sprite</code> the <code>Ref</code> refers to:</b>
 * <pre>Sprite spr = ref.getRes();</pre>
 * Note that no casting is required, as the parametrised type handles this.
 * <br><br>
 * <b>To free the <code>Sprite</code>:</b> <pre>res.delete();</pre>
 * You shouldn't need to worry about calling <code>delete()</code> most of the time.
 * <code>ResourceList</code> calls it when <code>remove()</code> is called.
 * Of course, this does leave this instance in existance, but it is a class of
 * a significantly lighter weight than <code>Resource</code>.
 */
public class Ref<R extends Resource<R>>
	{
	private R res;

	/**
	 * Constructs a <code>Ref</code>. Should only be called by <code>Resource</code>.
	 * @param res The subject of the reference
	 */
	public Ref(R res)
		{
		setRes(res);
		}

	/**
	 * Used to convert this reference to an actual reference.
	 * @return The <code>Resource</code> that is the subject of this <code>Ref</code>
	 */
	public R getRes()
		{
		return res;
		}

	/**
	 * Sets the subject of the reference. Should not usually need to be called.
	 * @param res The new subject of the reference
	 */
	public void setRes(R res)
		{
		if (this.res != null) this.res.setRef(null);
		this.res = res;
		if (res != null) res.setRef(this);
		}

	/**
	 * Frees the subject of the reference. Should not usually need to be called explicitly.
	 */
	public void delete()
		{
		res = null;
		}
	
	public boolean equals(Object o)
		{
		return o == this;
		}
	}
