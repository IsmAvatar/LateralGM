/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, 2014 Robert B. Colton
 *
 * This file is part of LateralGM.
 *
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.messages;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages
	{
	private static final String LANGUAGE_BUNDLE_NAME = "org.lateralgm.messages.messages"; //$NON-NLS-1$
	private static final String INPUT_BUNDLE_NAME = "org.lateralgm.messages.keyboard"; //$NON-NLS-1$

	// NOTE: See comments about locale below.
	private static ResourceBundle LANGUAGE_BUNDLE = null;
	private static ResourceBundle KEYBOARD_BUNDLE = null;

	static
	{
		updateLangPack();
	}

	private Messages()
		{
		}

	//TODO: This method is exceedingly verbose, and we also need a way for users to install their own language packages.
	public static void updateLangPack()
		{
		LANGUAGE_BUNDLE = ResourceBundle.getBundle(LANGUAGE_BUNDLE_NAME);
		KEYBOARD_BUNDLE = ResourceBundle.getBundle(INPUT_BUNDLE_NAME);
		}

	public static String getString(String key)
		{
		try
			{
			return LANGUAGE_BUNDLE.getString(key);
			}
		catch (MissingResourceException e)
			{
			return '!' + key + '!';
			}
		}

	public static String getKeyboardString(String key)
		{
		try
			{
			return KEYBOARD_BUNDLE.getString(key);
			}
		catch (MissingResourceException e)
			{
			return '!' + key + '!';
			}
		}

	public static String format(String key, Object...arguments)
		{
		try
			{
			String p = LANGUAGE_BUNDLE.getString(key);
			return MessageFormat.format(p,arguments);
			}
		catch (MissingResourceException e)
			{
			return '!' + key + '!';
			}
		}
	}
