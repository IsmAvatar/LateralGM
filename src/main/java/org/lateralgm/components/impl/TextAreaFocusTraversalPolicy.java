/*
 * Copyright (C) 2008 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.ContainerOrderFocusTraversalPolicy;

public class TextAreaFocusTraversalPolicy extends ContainerOrderFocusTraversalPolicy
	{
	private static final long serialVersionUID = 1L;
	Component comp;

	public TextAreaFocusTraversalPolicy(Component comp)
		{
		this.comp = comp;
		}

	public Component getDefaultComponent(Container container)
		{
		return comp;
		}

	public Component getFirstComponent(Container container)
		{
		return comp;
		}
	}
