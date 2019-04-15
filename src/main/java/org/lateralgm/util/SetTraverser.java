/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.util;

import java.util.HashMap;

public abstract class SetTraverser<E, P>
	{
	/*
	 * Maps an element to the node that comes before the element's node. The map is necessary for
	 * fast node access, and mapping to the previous node rather than directly is necessary for fast
	 * node removal in a singly-linked list.
	 */
	private HashMap<E,Node> previous;
	/*
	 *  The header node of the linked list. The first element node is header.next.
	 */
	private final Node header;

	public SetTraverser()
		{
		header = new Node();
		}

	/**
	 * Adds <code>e</code> to the set if not already added. It is safe to do this even during a
	 * traversal; the element is added to the beginning of the linked list, so ongoing traversals
	 * will not be affected.
	 *
	 * @param e The element to add
	 * @return <code>false</code> if <code>e</code> was already in the set;
	 *         <code>true</code> otherwise.
	 */
	public final boolean add(E e)
		{
		if (previous != null && previous.containsKey(e)) return false;
		new ElementNode(header,e);
		return true;
		}

	/**
	 * Removes <code>e</code> from the set. It is safe to do this even during a traversal. Any
	 * ongoing traversal will skip this element if it hasn't visited it already.
	 *
	 * @param e The element to remove
	 * @return <code>false</code> if <code>e</code> was not in the set;
	 *         <code>true</code> otherwise.
	 */
	public final boolean remove(E e)
		{
		if (previous == null) return false;
		Node p = previous.get(e);
		if (p == null) return false;
		p.removeNext();
		return true;
		}

	/**
	 * Traverses the set.
	 *
	 * @param p The parameter passed to the <code>visit</code> method.
	 */
	public final void traverse(P p)
		{
		for (ElementNode n = header.next; n != null; n = n.next)
			visit(n.element,p);
		}

	protected abstract void visit(E e, P p);

	private class Node
		{
		protected ElementNode next;

		public void removeNext()
			{
			previous.remove(next.element);
			next = next.next;
			if (next == null) return;
			previous.put(next.element,this);
			}
		}

	private class ElementNode extends Node
		{
		public final E element;

		public ElementNode(Node p, E e)
			{
			next = p.next;
			p.next = this;
			if (previous == null) previous = new HashMap<E,Node>();
			if (next != null) previous.put(next.element,this);
			previous.put(e,p);
			element = e;
			}
		}
	}
