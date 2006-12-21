package org.marketcetera.photon.actions;

import org.apache.bsf.BSFManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.marketcetera.photon.scripting.IScript;
import org.marketcetera.photon.scripting.Script;
import org.marketcetera.photon.scripting.ScriptRunnable;


/**
 * Action delegate for "Run script".
 * 
 * @author andrei@lissovski.org
 */
public class RunScriptActionDelegate implements IEditorActionDelegate {

	public static final String ID = "org.marketcetera.photon.actions.RunScriptActionDelegate";
	
	private IEditorPart targetEditor;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
  		this.targetEditor = targetEditor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		//agl todo:temp
		IScript script = new Script("puts 'sample script output'");
		BSFManager manager = new BSFManager();
		ScriptRunnable runnable = new ScriptRunnable(script, manager);
		runnable.run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		//agl do nothing -- this method is invoked for text selection changes
	}

}