/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.erlide.ui.dialogs.fields;

/**
 * Change listener used by <code>ListDialogField</code> and
 * <code>CheckedListDialogField</code>
 */
public interface IListAdapter<Element> {

    /**
     * A button from the button bar has been pressed.
     */
    void customButtonPressed(ListDialogField<Element> field, int index);

    /**
     * The selection of the list has changed.
     */
    void selectionChanged(ListDialogField<Element> field);

    /**
     * En entry in the list has been double clicked
     */
    void doubleClicked(ListDialogField<Element> field);

}
