/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JTextField;

public class EventKeyInput extends JTextField
	{
	private static final long serialVersionUID = 1L;

	private static final ArrayList<Integer> KEYS = new ArrayList<Integer>();

	private int selectedKey;

	public static String getGmKeyName(int keyCode)
		{
		switch (keyCode)
			{
			case 13:
				keyCode = KeyEvent.VK_ENTER;
				break;
			case 46:
				keyCode = KeyEvent.VK_DELETE;
				break;
			case 45:
				keyCode = KeyEvent.VK_INSERT;
				break;
			default:
				break;
			}
		return KeyEvent.getKeyText(keyCode);
		}

	public static int getGmKeyCode(int keyCode)
		{
		switch (keyCode)
			{
			case KeyEvent.VK_ENTER:
				return 13;
			case KeyEvent.VK_DELETE:
				return 46;
			case KeyEvent.VK_INSERT:
				return 45;
			default:
				return keyCode;
			}
		}

	static
		{
		KEYS.add(37); //vk_left
		KEYS.add(39); //vk_right
		KEYS.add(38); //vk_up
		KEYS.add(40); //vk_down

		KEYS.add(17); //vk_control
		KEYS.add(18); //vk_alt
		KEYS.add(16); //vk_shift
		KEYS.add(32); //vk_space
		KEYS.add(13); //vk_enter

		KEYS.add(96); //vk_numpad0
		KEYS.add(97); //vk_numpad1
		KEYS.add(98); //vk_numpad2
		KEYS.add(99); //vk_numpad3
		KEYS.add(100); //vk_numpad4
		KEYS.add(101); //vk_numpad5
		KEYS.add(102); //vk_numpad6
		KEYS.add(103); //vk_numpad7
		KEYS.add(104); //vk_numpad8
		KEYS.add(105); //vk_numpad9

		KEYS.add(111); //vk_divide
		KEYS.add(106); //vk_multiply
		KEYS.add(109); //vk_subtract
		KEYS.add(107); //vk_add		
		KEYS.add(110); //vk_decimal

		KEYS.add(48); //0
		KEYS.add(49); //1
		KEYS.add(50); //2
		KEYS.add(51); //3
		KEYS.add(52); //4
		KEYS.add(53); //5
		KEYS.add(54); //6
		KEYS.add(55); //7
		KEYS.add(56); //8
		KEYS.add(57); //9

		KEYS.add(65); //A
		KEYS.add(66); //B
		KEYS.add(67); //C
		KEYS.add(68); //D
		KEYS.add(69); //E
		KEYS.add(70); //F
		KEYS.add(71); //G
		KEYS.add(72); //H
		KEYS.add(73); //I
		KEYS.add(74); //J
		KEYS.add(75); //K
		KEYS.add(76); //L
		KEYS.add(77); //M
		KEYS.add(78); //N
		KEYS.add(79); //O
		KEYS.add(80); //P
		KEYS.add(81); //Q
		KEYS.add(82); //R
		KEYS.add(83); //S
		KEYS.add(84); //T
		KEYS.add(85); //U
		KEYS.add(86); //V
		KEYS.add(87); //W
		KEYS.add(88); //X
		KEYS.add(89); //Y
		KEYS.add(90); //Z

		KEYS.add(112); //vk_f1
		KEYS.add(113); //vk_f2
		KEYS.add(114); //vk_f3
		KEYS.add(115); //vk_f4
		KEYS.add(116); //vk_f5
		KEYS.add(117); //vk_f6
		KEYS.add(118); //vk_f7
		KEYS.add(119); //vk_f8
		KEYS.add(120); //vk_f9
		KEYS.add(121); //vk_f10
		KEYS.add(122); //vk_f11
		KEYS.add(123); //vk_f12

		KEYS.add(8); //vk_backspace
		KEYS.add(27); //vk_escape
		KEYS.add(36); //vk_home
		KEYS.add(35); //vk_end
		KEYS.add(33); //vk_pageup
		KEYS.add(34); //vk_pagedown	
		KEYS.add(46); //vk_delete
		KEYS.add(45); //vk_insert
		}

	public EventKeyInput()
		{
		setFocusTraversalKeysEnabled(false);
		}

	public void processKeyEvent(KeyEvent e)
		{
		if (e.getID() == KeyEvent.KEY_PRESSED)
			{
			int key = getGmKeyCode(e.getKeyCode());
			if (KEYS.contains(key))
				{
				selectedKey = key;
				setText(getGmKeyName(selectedKey));
				}
			}
		}
	}
