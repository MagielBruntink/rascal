/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:

 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.library.experiments.RascalTutor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.interpreter.result.Result;

@SuppressWarnings("serial")
public class Edit extends TutorHttpServlet {

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.err.println("Edit, doGet: " + request.getRequestURI() + "?" + request.getQueryString());
		String concept = getStringParameter(request, "concept");
		boolean newConcept = getStringParameter(request, "new").equals("true");
		boolean check = getStringParameter(request, "check").equals("true");
		
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		
		try {
			Result<IValue> result = evaluator.eval(null, "edit(\"" + concept + "\"," + newConcept + "," + check + ")", URI.create("stdin:///"));
			String resp = ((IString) result.getValue()).getValue();
			out.println(resp);
		}
		catch (Throwable e) {
			out.println(escapeForHtml(e.getMessage()));
			e.printStackTrace(out);
		}
		finally {
			out.close();
		}
	}

}
