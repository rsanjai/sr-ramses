/**
 * AADL-RAMSES
 * 
 * Copyright © 2014 TELECOM ParisTech and CNRS
 * 
 * TELECOM ParisTech/LTCI
 * 
 * Authors: see AUTHORS
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the Eclipse Public License as published by Eclipse,
 * either version 1.0 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * You should have received a copy of the Eclipse Public License
 * along with this program.  If not, see 
 * http://www.eclipse.org/org/documents/epl-v10.php
 */

package fr.tpt.aadl.ramses.control.support.reporters;


/**
 * 
 * This class doesn't encapsulate log4j in order to keep 
 * class and method name, code line informations when log4j is called.
 *
 */
public class SysErrReporter4Cli extends AbstractSystemErrReporter
{
  protected MessageReporter4Cli _msgReporter ;
  
  public SysErrReporter4Cli(MessageReporter4Cli msgReporter)
  {
    _msgReporter = msgReporter ;
  }

  @Override
  public void fatal(String msg,
                    Throwable ex)
  {
    msg = super.formatFatalMsg(msg, ex) ;
    _msgReporter.reportMessage(MessageStatus.INTERNAL_FATAL_ERROR, msg, ex);
  }

  @Override
  public void fatal(String msg)
  {
    msg = super.formatFatalMsg(msg) ;
    _msgReporter.reportMessage(MessageStatus.INTERNAL_FATAL_ERROR, msg) ;
  }

  @Override
  public void error(String msg, boolean isDelayed)
  {
    _nbErrors++ ;
    
    msg = super.formatErrorMsg(msg) ;
    
    // Never delay better visibility when executing RAMSES CLI.
    _msgReporter.reportMessage(MessageStatus.INTERNAL_ERROR, msg) ;
  }

  @Override
  public void warning(String msg, boolean isDelayed)
  {
    _nbWarnings++ ;
    
    msg = super.formatWarningMsg(msg) ;
    
    // Never delay: better visibility when executing RAMSES CLI.
    _msgReporter.reportMessage(MessageStatus.INTERNAL_WARNING, msg) ;
  }
}