/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * The color manager for the system (very simple implementation)
 *
 *
 * @author Eric Merritt [cyberlync at gmail dot com]
 */
public class ColorManager implements IColorManager {

    protected Map<String, RGB> fKeyTable = new HashMap<>(10);

    protected Map<Display, Map<RGB, Color>> fDisplayTable = new HashMap<>(2);

    /**
     * Flag which tells if the colors are automatically disposed when the current display
     * gets disposed.
     */
    private final boolean fAutoDisposeOnDisplayDispose;

    /**
     * Creates a new Erlang color manager which automatically disposes the allocated
     * colors when the current display gets disposed.
     */
    public ColorManager() {
        this(true);
    }

    /**
     * Creates a new Erlang color manager.
     *
     * @param autoDisposeOnDisplayDispose
     *            if <code>true</code> the color manager automatically disposes all
     *            managed colors when the current display gets disposed and all calls to
     *            {@link org.eclipse.jface.text.source.ISharedTextColors#dispose()} are
     *            ignored.
     *
     */
    public ColorManager(final boolean autoDisposeOnDisplayDispose) {
        fAutoDisposeOnDisplayDispose = autoDisposeOnDisplayDispose;
    }

    public void dispose(final Display display) {
        final Map<RGB, Color> colorTable = fDisplayTable.get(display);
        if (colorTable != null) {
            for (final Color color : colorTable.values()) {
                if (color != null && !color.isDisposed()) {
                    color.dispose();
                }
            }
        }
    }

    /*
     * @see IColorManager#getColor(RGB)
     */
    @Override
    public Color getColor(final RGB rgb) {

        if (rgb == null) {
            return null;
        }

        final Display display = Display.getCurrent();
        Map<RGB, Color> colorTable = fDisplayTable.get(display);
        if (colorTable == null) {
            colorTable = new HashMap<>(10);
            fDisplayTable.put(display, colorTable);
            if (fAutoDisposeOnDisplayDispose) {
                display.disposeExec(() -> dispose(display));
            }
        }

        final Color color = colorTable.computeIfAbsent(rgb,
                r -> new Color(Display.getCurrent(), r));

        return color;
    }

    /*
     * @see IColorManager#dispose
     */
    @Override
    public void dispose() {
        if (!fAutoDisposeOnDisplayDispose) {
            dispose(Display.getCurrent());
        }
    }

    /*
     * @see IColorManager#getColor(String)
     */
    @Override
    public Color getColor(final String key) {

        if (key == null) {
            return null;
        }

        final RGB rgb = fKeyTable.get(key);
        return getColor(rgb);
    }

}
