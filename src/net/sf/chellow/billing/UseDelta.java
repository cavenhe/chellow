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

package net.sf.chellow.billing;

import net.sf.chellow.monad.DeployerException;
import net.sf.chellow.monad.DesignerException;
import net.sf.chellow.monad.Hiber;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.Invocation;
import net.sf.chellow.monad.MonadUtils;
import net.sf.chellow.monad.NotFoundException;
import net.sf.chellow.monad.Urlable;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.XmlTree;
import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.monad.types.UriPathElement;
import net.sf.chellow.physical.HhEndDate;
import net.sf.chellow.physical.PersistentEntity;
import net.sf.chellow.physical.Site;

import org.hibernate.HibernateException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class UseDelta extends PersistentEntity implements Urlable {
	public static UseDelta getUseDelta(Long id) throws HttpException {
		UseDelta useDelta = (UseDelta) Hiber.session().get(UseDelta.class, id);
		if (useDelta == null) {
			throw new UserException("There isn't an account with that id.");
		}
		return useDelta;
	}

	public static void deleteUseDelta(UseDelta useDelta)
			throws InternalException {
		try {
			Hiber.session().delete(useDelta);
			Hiber.flush();
		} catch (HibernateException e) {
			throw new InternalException(e);
		}
	}

	private Site site;

	private HhEndDate startDate;

	private float kwChange;

	public UseDelta() {
	}

	public UseDelta(Site site, HhEndDate startDate,
			int kwhPerMonth) {
		update(site, startDate, kwhPerMonth);
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public HhEndDate getStartDate() {
		return startDate;
	}

	protected void setStartDate(HhEndDate startDate) {
		this.startDate = startDate;
	}

	public float getKwChange() {
		return kwChange;
	}

	public void setKwChange(float kwChange) {
		this.kwChange = kwChange;
	}

	public void update(Site site, HhEndDate startDate, float kwChange) {
		setSite(site);
		setStartDate(startDate);
		setKwChange(kwChange);
	}

	public Node toXml(Document doc) throws HttpException {
		Element element = super.toXml(doc, "use-delta");
		startDate.setLabel("start");
		element.appendChild(startDate.toXml(doc));
		element.setAttribute("kw-change", Float.toString(kwChange));
		return element;
	}

	public void httpPost(Invocation inv) throws InternalException,
			HttpException, DesignerException, DeployerException {
		/*
		String siteCode = inv.getString("site-code");
		Date date = inv.getDate("start-date");
		int kwhPerMonth = inv.getInteger("kwh-per-month");
		if (!inv.isValid()) {
			throw new UserException(document());
		}
		update(Site.getSite(siteCode), HhEndDate
				.roundUp(date), kwhPerMonth);
		Hiber.commit();
		inv.sendOk(document());
		*/
	}

	private Document document() throws HttpException {
		Document doc = MonadUtils.newSourceDocument();
		Element source = doc.getDocumentElement();
		source
				.appendChild(toXml(doc,
						new XmlTree("organization").put("site")));
		return doc;
	}

	public void httpGet(Invocation inv) throws DesignerException,
			InternalException, HttpException, DeployerException {
		inv.sendOk(document());
	}

	public MonadUri getUri() throws InternalException, HttpException {
		//return organization.useDeltasInstance().getUri().resolve(getUriId())
		//		.append("/");
		return null;
	}

	public Urlable getChild(UriPathElement uriId) throws InternalException,
			HttpException {
		throw new NotFoundException();
	}

	public void httpDelete(Invocation inv) throws InternalException,
			DesignerException, HttpException, DeployerException {
		deleteUseDelta(this);
		inv.sendOk();
	}
}
