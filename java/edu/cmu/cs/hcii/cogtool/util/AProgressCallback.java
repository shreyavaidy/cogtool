/*******************************************************************************
 * CogTool Copyright Notice and Distribution Terms
 * CogTool 1.2, Copyright (c) 2005-2012 Carnegie Mellon University
 * This software is distributed under the terms of the FSF Lesser
 * Gnu Public License (see LGPL.txt). 
 * 
 * CogTool is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * CogTool is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with CogTool; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * CogTool makes use of several third-party components, with the 
 * following notices:
 * 
 * Eclipse SWT
 * Eclipse GEF Draw2D
 * 
 * Unless otherwise indicated, all Content made available by the Eclipse 
 * Foundation is provided to you under the terms and conditions of the Eclipse 
 * Public License Version 1.0 ("EPL"). A copy of the EPL is provided with this 
 * Content and is also available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * CLISP
 * 
 * Copyright (c) Sam Steingold, Bruno Haible 2001-2006
 * This software is distributed under the terms of the FSF Gnu Public License.
 * See COPYRIGHT file in clisp installation folder for more information.
 * 
 * ACT-R 6.0
 * 
 * Copyright (c) 1998-2007 Dan Bothell, Mike Byrne, Christian Lebiere & 
 *                         John R Anderson. 
 * This software is distributed under the terms of the FSF Lesser
 * Gnu Public License (see LGPL.txt).
 * 
 * Apache Jakarta Commons-Lang 2.1
 * 
 * This product contains software developed by the Apache Software Foundation
 * (http://www.apache.org/)
 * 
 * Mozilla XULRunner 1.9.0.5
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/.
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * 
 * The J2SE(TM) Java Runtime Environment
 * 
 * Copyright 2009 Sun Microsystems, Inc., 4150
 * Network Circle, Santa Clara, California 95054, U.S.A.  All
 * rights reserved. U.S.  
 * See the LICENSE file in the jre folder for more information.
 ******************************************************************************/

package edu.cmu.cs.hcii.cogtool.util;

/**
 * Abstract implementation for scheduling the progress callback feedback
 * to be run by SWT "at the next reasonable opportunity" (see
 * <code>org.eclipse.swt.widgets.Display</code>).
 */
public abstract class AProgressCallback implements ProgressCallback, Runnable
{
    /**
     * Caches the progress percentage provided by <code>updateProgress</code>
     */
    protected double progress = 0.0;

    /**
     * Caches the status provided by <code>updateProgress</code>
     */
    protected String status = "";

    /**
     * Each subclass must override <code>progressCallback</code>
     * to handle the periodic informing of progress by another
     * computation.  Executed on main UI thread.
     *
     * @param progressPct a number from 0.0 to 1.0 (inclusive) indicating
     *                    the percentage progress from 0% to 100%
     * @param progressStatus an arbitrary string that may be interpreted and/or
     *                       displayed by the progress callback object
     */
    protected abstract void progressCallback(double progressPct,
                                             String progressStatus);

    /**
     * Use the given progress (a number between 0.0 and 1.0 inclusive,
     * reflecting a percentage between 0% and 100%) and status
     * (which may be interpreted or displayed to the user) to react
     * to progress by a computation.
     * <p>
     * The notification presumably came from another thread, so
     * the reaction must be scheduled to execute on the main user interface
     * thread; thus, we schedule the invocation of the
     * <code>progressCallback</code> as overridden by the subclass.
     *
     * @param progressSoFar a number from 0.0 to 1.0 (inclusive) indicating
     *                      the percentage progress from 0% to 100%
     * @param statusAsOfProgress an arbitrary string that may be interpreted
     *                           and/or displayed
     */
    public void updateProgress(double progressSoFar, String statusAsOfProgress)
    {
        // Executes in the child thread
        synchronized(this) {
            progress = progressSoFar;
            status = statusAsOfProgress;
        }

        WindowUtil.scheduleAsynchronously(this);
    }

    /**
     * Perform any cleanup necessary to recover system resources.
     */
    public void dispose()
    {
        // Nothing to do by default
    }

    /**
     * To schedule a method to be run by SWT "at the next reasonable
     * opportunity", this object must be a <code>Runnable</code>, since
     * its <code>run</code> method will be invoked when scheduled.
     * <p>
     * The purpose of this is to execute in the main UI thread the reaction to
     * the notification of a change in progress.
     */
    public void run()
    {
        // Executes in the main UI thread
        double localProgress;
        String localStatus;

        synchronized(this) {
            localProgress = progress;
            localStatus = status;
        }

        progressCallback(localProgress, localStatus);
    }
}