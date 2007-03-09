package SubFrames;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import mainRes.LGM;
import mainRes.Prefs;
import resourcesRes.GameInformation;
import resourcesRes.ResId;
import resourcesRes.Resource;
import resourcesRes.Script;

import componentRes.NameDocument;
import fileRes.Gm6File;

public class ScriptFrame extends JInternalFrame
	{
	private static final long serialVersionUID = 1L;
	public static GameInformation gi = new GameInformation();
	public static JTextField name;
	public static JTextArea code;
	
	public ScriptFrame(ResId id)
		{
		super("ScriptName",true,true,true,true);
		Script scr = (Script)Gm6File.resMap.get(Resource.SCRIPT).get(1);
		this.setTitle(scr.name);
		setSize(600,400);
		// Setup the toolbar
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add("North",tool); //$NON-NLS-1$
		// Setup the buttons
		JButton but = new JButton(LGM.findIcon("save.png")); //$NON-NLS-1$
		but.setActionCommand("Save"); //$NON-NLS-1$
//		but.addActionListener(this);
		tool.add(but);
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		name = new JTextField(new NameDocument(),"",13); //$NON-NLS-1$
		name.setMaximumSize(name.getPreferredSize());
		tool.add(name);
		//the code text area
		code = new JTextArea();
		code.setFont(Prefs.codeFont);
		JScrollPane codePane = new JScrollPane(code);
		getContentPane().add(codePane,BorderLayout.CENTER);
		}

	public void setScript(Script s)
		{
		name.setText(s.name);
		code.setText(s.ScriptStr);
		}
	}