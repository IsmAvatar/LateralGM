/**
* @file  EffectsFrame.java
* @brief Class implementing an effect chooser for sprites and backgrounds.
*
* @section License
*
* Copyright (C) 2013 Robert B. Colton
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

package org.lateralgm.subframes;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;

public class EffectsFrame extends JFrame
	{

	private BufferedImage image = null;
	
	public EffectsFrame(BufferedImage source) {
  	setAlwaysOnTop(true);
  	setDefaultCloseOperation(HIDE_ON_CLOSE);
  	setSize(600,400);
  	setLocationRelativeTo(LGM.frame);
  	setTitle(Messages.getString("EffectsFrame.TITLE"));
  	setIconImage(LGM.getIconForKey("EffectsFrame.TITLE").getImage());
  	setResizable(true);
  	
		image = source;
	}
	
	}
