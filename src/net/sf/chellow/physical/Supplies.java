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

package net.sf.chellow.physical;

import java.util.List;

import net.sf.chellow.monad.Hiber;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.Invocation;
import net.sf.chellow.monad.MethodNotAllowedException;
import net.sf.chellow.monad.MonadUtils;
import net.sf.chellow.monad.XmlTree;
import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.monad.types.UriPathElement;
import net.sf.chellow.ui.Chellow;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Supplies extends EntityList {
	// private static final int PAGE_SIZE = 25;

	public static final UriPathElement URI_ID;

	static {
		try {
			URI_ID = new UriPathElement("supplies");
		} catch (HttpException e) {
			throw new RuntimeException(e);
		}
	}

	public Supplies() {
	}

	public MonadUri getUrlPath() throws HttpException {
		return Chellow.ROOT_URI.resolve(getUriId()).append("/");
	}

	public UriPathElement getUriId() {
		return URI_ID;
	}

	@SuppressWarnings("unchecked")
	public void httpGet(Invocation inv) throws HttpException {
		Document doc = MonadUtils.newSourceDocument();
		Element source = doc.getDocumentElement();
		if (inv.hasParameter("search-pattern")) {
			String pattern = inv.getString("search-pattern");
			pattern = pattern.replace(" ", "");
			for (Object[] array : (List<Object[]>) Hiber
					.session()
					.createQuery(
							"select distinct mpanCore, mpanCore.dso.code, mpanCore.uniquePart, mpanCore.checkDigit from MpanCore mpanCore where lower(mpanCore.dso.code || mpanCore.uniquePart || mpanCore.checkDigit) like lower(:term) order by mpanCore.dso.code, mpanCore.uniquePart, mpanCore.checkDigit")
					.setString("term",
							"%" + pattern + "%").setMaxResults(50)
					.list()) {
				source.appendChild(((MpanCore) array[0]).toXml(doc, new XmlTree(
								"supply").put("dso")));
			}
		}
		inv.sendOk(doc);
	}

	public void httpPost(Invocation inv) throws HttpException {
		throw new MethodNotAllowedException();
	}

	/*
	 * @SuppressWarnings("unchecked") public List<Supply> findSupplies() {
	 * return (List<Supply>) Hiber.session().createQuery( "from Supply supply
	 * where supply.organization = :organization") .setEntity("organization",
	 * organization).list(); }
	 * 
	 * @SuppressWarnings("unchecked") public List<Supply> findSupplies(int
	 * page) { return (List<Supply>) Hiber .session() .createQuery( "select
	 * supply from Supply supply join supply.generations generation join
	 * generation.siteSupplyGenerations siteSupplyGeneration where
	 * siteSupplyGeneration.site.organization = :organization")
	 * .setEntity("organization", organization).setFirstResult( page *
	 * PAGE_SIZE).setMaxResults(PAGE_SIZE).list(); }
	 */
	public Supply getChild(UriPathElement uriId) throws HttpException {
		return Supply.getSupply(Long.parseLong(uriId.getString()));
	}

	public MonadUri getUri() throws HttpException {
		return Chellow.ROOT_URI.resolve(getUriId());
	}

	public Element toXml(Document doc) throws HttpException {
		Element element = doc.createElement("supplies");
		return element;
	}
}
