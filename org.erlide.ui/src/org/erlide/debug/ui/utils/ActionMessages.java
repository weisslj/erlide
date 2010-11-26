package org.erlide.debug.ui.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ActionMessages {
    private static final String BUNDLE_NAME = "org.erlide.debug.ui.views.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private ActionMessages() {
    }

    public static String getString(final String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (final MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
