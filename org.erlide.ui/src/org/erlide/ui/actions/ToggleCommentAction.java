package org.erlide.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

public class ToggleCommentAction extends ErlangTextEditorAction {

	public ToggleCommentAction(final ResourceBundle bundle,
			final String prefix, final ITextEditor editor) {
		super(bundle, prefix, editor, "erlide_comment", "toggle_comment");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.erlide.ui.actions.ErlangTextEditorAction#getTextSelection(org.eclipse
	 * .jface.text.IDocument, org.eclipse.jface.text.ITextSelection)
	 */
	@Override
	protected ITextSelection getTextSelection(final IDocument document,
			final ITextSelection selection) {
		// We don't need context, only lines to comment
		return selection;
	}

}
