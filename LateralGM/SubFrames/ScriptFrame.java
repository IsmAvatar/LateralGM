package SubFrames;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import mainRes.LGM;
import mainRes.Prefs;
import resourcesRes.Script;

import componentRes.ResNode;

public class ScriptFrame extends ResourceFrame<Script>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon frameIcon = LGM.findIcon("script.png"); //$NON-NLS-1$
	private static final ImageIcon saveIcon = LGM.findIcon("save.png"); //$NON-NLS-1$
	public JTextArea code;

	public ScriptFrame(Script res, ResNode node)
		{
		super(res,node);
		setSize(600,400);
		setFrameIcon(frameIcon);
		// Setup the toolbar
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add("North",tool); //$NON-NLS-1$
		// Setup the buttons
		save.setIcon(saveIcon);
		tool.add(save);
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(name);
		// the code text area
		code = new JTextArea();
		code.setFont(Prefs.codeFont);
		code.setText(res.ScriptStr);
		JScrollPane codePane = new JScrollPane(code);
		getContentPane().add(codePane,BorderLayout.CENTER);
		}

	public void revertResource()
		{
		LGM.currentFile.Scripts.replace(res.Id,resOriginal);
		}

	public void updateResource()
		{
		res.ScriptStr = code.getText();
		res.name = name.getText();
		resOriginal = (Script) res.copy(false,null);
		}

	public boolean resourceChanged()
		{
		return (!code.getText().equals(res.ScriptStr)) || (!res.name.equals(resOriginal.name));
		}
	}