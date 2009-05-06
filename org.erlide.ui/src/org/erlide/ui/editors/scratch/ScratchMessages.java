/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.erlide.ui.editors.scratch;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ScratchMessages {

	private static final String RESOURCE_BUNDLE = "org.erlide.ui.editors.scratch.ScratchMessages";//$NON-NLS-1$

	private static final ResourceBundle fgResourceBundle = ResourceBundle
			.getBundle(RESOURCE_BUNDLE);

	private ScratchMessages() {
	}

	public static String getString(final String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (final MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key
	 *            the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(final String key, Object arg) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (final MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg == null) {
			arg = ""; //$NON-NLS-1$
		}
		return MessageFormat.format(format, new Object[] { arg });
	}

	static ResourceBundle getBundle() {
		return fgResourceBundle;
	}
}
