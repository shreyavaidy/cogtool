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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation for scheduling the progress callback feedback
 * to be run by SWT "at the next reasonable opportunity" (see
 * <code>org.eclipse.swt.widgets.Display</code>).
 */
public abstract class AProcessTraceCallback extends AProgressCallback
                                            implements ProcessTraceCallback
{
    protected List<String> outputLines = new ArrayList<String>();
    protected List<String> errorLines = new ArrayList<String>();
    protected boolean progressInvoked = false;

    public void appendOutputLine(String traceOutputLine)
    {
        synchronized(this) {
            outputLines.add(traceOutputLine);
        }

        WindowUtil.scheduleAsynchronously(this);
    }

    public void appendOutputLines(List<String> traceOutputLines)
    {
        synchronized(this) {
            outputLines.addAll(traceOutputLines);
        }

        WindowUtil.scheduleAsynchronously(this);
    }

    public void appendErrorLine(String traceErrorLine)
    {
        synchronized(this) {
            errorLines.add(traceErrorLine);
        }

        WindowUtil.scheduleAsynchronously(this);
    }

    public void appendErrorLines(List<String> traceErrorLines)
    {
        synchronized(this) {
            errorLines.addAll(traceErrorLines);
        }

        WindowUtil.scheduleAsynchronously(this);
    }

    /**
     * Override to determine whether progress is invoked vs. tracing
     */
    @Override
    public void updateProgress(double progressSoFar, String statusAsOfProgress)
    {
        synchronized(this) {
            progressInvoked = true;
        }

        super.updateProgress(progressSoFar, statusAsOfProgress);
    }

    /**
     * Each subclass must override <code>outputLineCallback</code>
     * to handle the periodic informing of output tracing.
     * Executed on main UI thread.
     *
     * @param lines the output trace lines to display
     */
    protected abstract void outputLineCallback(List<String> lines);

    /**
     * Each subclass must override <code>errorLineCallback</code>
     * to handle the periodic informing of error tracing.
     * Executed on main UI thread.
     *
     * @param lines the error trace lines to display
     */
    protected abstract void errorLineCallback(List<String> lines);

    /**
     * To schedule a method to be run by SWT "at the next reasonable
     * opportunity", this object must be a <code>Runnable</code>, since
     * its <code>run</code> method will be invoked when scheduled.
     * <p>
     * The purpose of this is to execute in the main UI thread the reaction to
     * the notification of a change in progress.
     */
    @Override
    public void run()
    {
        List<String> localOutputLines = null;
        List<String> localErrorLines = null;
        boolean localProgressInvoked;

        synchronized(this) {
            if (outputLines.size() > 0) {
                localOutputLines = new ArrayList<String>(outputLines);
                outputLines.clear();
            }

            if (errorLines.size() > 0) {
                localErrorLines = new ArrayList<String>(errorLines);
                errorLines.clear();
            }

            localProgressInvoked = progressInvoked;
            progressInvoked = false;
        }

        if (localOutputLines != null) {
            outputLineCallback(localOutputLines);
        }

        if (localErrorLines != null) {
            errorLineCallback(localErrorLines);
        }

        if (localProgressInvoked) {
            super.run();
        }
    }
}