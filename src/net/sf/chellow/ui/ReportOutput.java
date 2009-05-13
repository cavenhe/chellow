/*******************************************************************************
 * 
 *  Copyright (c) 2005, 2009 Wessex Water Services Limited
 *  
 *  This file is part of Chellow.
 * 
 *  Chellow is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Chellow is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Chellow.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
package net.sf.chellow.ui;

import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import net.sf.chellow.monad.Invocation;
import net.sf.chellow.monad.MethodNotAllowedException;
import net.sf.chellow.monad.MonadUtils;
import net.sf.chellow.monad.NotFoundException;
import net.sf.chellow.monad.Urlable;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.XmlDescriber;
import net.sf.chellow.monad.XmlTree;

import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.monad.types.UriPathElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReportOutput implements Urlable, XmlDescriber {
	public static final UriPathElement URI_ID;

	static {
		try {
			URI_ID = new UriPathElement("output");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Report report;

	public ReportOutput(Report report) throws HttpException {
		this.report = report;
	}

	public Report getReport() {
		return report;
	}

	public Urlable getChild(UriPathElement uriId) throws HttpException {
		throw new NotFoundException();
	}

	public MonadUri getUri() throws HttpException {
		return report.getUri().resolve(URI_ID).append("/");
	}

	public void httpGet(Invocation inv) throws HttpException {
		try {
			Document doc = MonadUtils.newSourceDocument();
			Element source = doc.getDocumentElement();
			Element reportElement = toXml(doc);
			source.appendChild(reportElement);
			report.run(inv, doc);
			String template = report.getTemplate();
			if (template != null) {
				inv.sendOk(doc, new StreamSource(new StringReader(template)));
			}
		} catch (HttpException e) {
			e.setDocument(document());
			throw e;
		}
	}

	private Document document() throws HttpException {
		Document doc = MonadUtils.newSourceDocument();
		Element source = doc.getDocumentElement();
		Element outputElement = toXml(doc);
		source.appendChild(outputElement);
		Element reportElement = getReport().toXml(doc);
		outputElement.appendChild(reportElement);
		return doc;
	}

	public void httpPost(Invocation inv) throws HttpException {
		throw new MethodNotAllowedException();
	}

	public void httpDelete(Invocation inv) throws HttpException {
		throw new MethodNotAllowedException();
	}

	public Element toXml(Document doc) throws HttpException {
		Element element = doc.createElement("report-output");
		return element;
	}

	public Node toXml(Document doc, XmlTree tree) throws HttpException {
		return null;
	}
	/*
	 * 
	 * public void run(Invocation inv, Document doc) throws InternalException,
	 * DesignerException, HttpException { /* PythonInterpreter interp = new
	 * PythonInterpreter(); Element source = doc.getDocumentElement();
	 * interp.set("doc", doc); interp.set("source", source);
	 * interp.set("organization", report.getReports().getOrganization());
	 * interp.set("inv", inv); StringWriter out = new StringWriter();
	 * interp.setOut(out); StringWriter err = new StringWriter();
	 * interp.setErr(err); try { interp.execfile(reportScript.getInputStream()); }
	 * catch (PyException e) { inv.getResponse().setContentType("text/html");
	 * Object obj = e.value.__tojava__(UserException.class); if (obj instanceof
	 * UserException) { throw (UserException) obj; } else { throw
	 * UserException.newInvalidParameter(e.toString()); } } if
	 * (out.toString().length() > 0) { source.appendChild(new
	 * VFMessage(out.toString()).toXML(doc)); } if (err.toString().length() > 0) {
	 * source.appendChild(new VFMessage(err.toString()).toXML(doc)); }
	 *  }
	 */
}
