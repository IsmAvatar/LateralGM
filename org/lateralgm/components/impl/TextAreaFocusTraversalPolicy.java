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

/**
 * This class provides a focus traversal policy for setting the default focus
 * component. It is only a convenience to set the focus without the component
 * or frame having to be realized first (e.g, by calling pack). This need
 * usually arises with JInternalFrame as a result of implementing the layout
 * in the constructor which does not immediately add the frame to the MDI
 * area. Having to wrap the focus request in a window activation event
 * listener or SwingUtilities.invokeLater would be redundant. This class is
 * especially useful for frames which have a toolbar, because Swing toolbar
 * buttons are by default focusable and part of the focus traversal cycle
 * unlike some native platforms (e.g, Win32) but should not conventionally
 * receive the initial focus.
 */
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
