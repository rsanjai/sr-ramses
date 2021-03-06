/**
 * AADL-RAMSES
 * 
 * Copyright © 2012 TELECOM ParisTech and CNRS
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

import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.InputStreamReader ;

/**
 * This class is meant to display messages of external processes launched
 * from RAMSES (output or error messages).
 */
public class StdProcessTraceDisplay extends AbstractProcessTraceDisplay
{
  // Singleton pattern.
  public static final StdProcessTraceDisplay INSTANCE = new StdProcessTraceDisplay() ;
  
  private StdProcessTraceDisplay() {} ;
  
  protected void display(InputStream is, boolean isError) throws IOException
  {
    BufferedReader in = new BufferedReader(new InputStreamReader(is));
    String line = null;
    while ((line = in.readLine()) != null)
    {
      if(isError)
      {
        System.err.println(line);
      }
      else
      {
        System.out.println(line) ;
      }
    }
  }
}