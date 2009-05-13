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

import java.util.List;

import net.sf.chellow.monad.Hiber;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.Invocation;
import net.sf.chellow.monad.MonadUtils;
import net.sf.chellow.monad.Urlable;
import net.sf.chellow.monad.XmlTree;
import net.sf.chellow.monad.types.MonadDate;
import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.monad.types.UriPathElement;
import net.sf.chellow.physical.EntityList;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BillSnags extends EntityList {
	static private final int PAGE_SIZE = 20;

	public static final UriPathElement URI_ID;

	static {
		try {
			URI_ID = new UriPathElement("bill-snags");
		} catch (HttpException e) {
			throw new RuntimeException(e);
		}
	}

	private Contract contract;

	public BillSnags(Contract contract) {
		this.contract = contract;
	}

	public UriPathElement getUriId() {
		return URI_ID;
	}

	public MonadUri getUri() throws HttpException {
		return contract.getUri().resolve(getUriId()).append("/");
	}

	@SuppressWarnings("unchecked")
	public void httpGet(Invocation inv) throws HttpException {
		inv.sendOk(document());
	}

	@SuppressWarnings("unchecked")
	private Document document() throws HttpException {
		Document doc = MonadUtils.newSourceDocument();
		Element source = doc.getDocumentElement();
		Element snagsElement = toXml(doc);
		source.appendChild(snagsElement);
		snagsElement.appendChild(contract.toXml(doc, new XmlTree("party")));
		for (BillSnag snag : (List<BillSnag>) Hiber
				.session()
				.createQuery(
						"from BillSnag snag where snag.isIgnored is false and snag.bill.account.contract = :contract order by snag.bill.id, snag.description, snag.bill.startDate.date")
				.setEntity("contract", contract).setMaxResults(PAGE_SIZE)
				.list()) {
			snagsElement.appendChild(snag.toXml(doc, new XmlTree("bill",
					new XmlTree("account"))));
		}
		source.appendChild(MonadDate.getMonthsXml(doc));
		source.appendChild(MonadDate.getDaysXml(doc));
		source.appendChild(new MonadDate().toXml(doc));
		return doc;
	}

	@SuppressWarnings("unchecked")
	public void httpPost(Invocation inv) throws HttpException {
		if (inv.hasParameter("ignore")) {
			MonadDate ignoreDate = inv.getMonadDate("ignore-date");

			ScrollableResults snags = Hiber
					.session()
					.createQuery(
							"from BillSnag snag where snag.contract = :contract and snag.finishDate < :ignoreDate")
					.setEntity("contract", contract).setTimestamp("ignoreDate",
							ignoreDate.getDate()).scroll(
							ScrollMode.FORWARD_ONLY);
			while (snags.next()) {
				BillSnag snag = (BillSnag) snags.get(0);
				snag.setIsIgnored(true);
				Hiber.session().flush();
				Hiber.session().clear();
			}
			Hiber.commit();
			inv.sendOk(document());
		}
	}

	public Urlable getChild(UriPathElement urlId) throws HttpException {
		return BillSnag.getBillSnag(Long.parseLong(urlId.getString()));
	}

	public MonadUri getMonadUri() throws InternalException {
		// TODO Auto-generated method stub
		return null;
	}

	public Element toXml(Document doc) throws HttpException {
		return doc.createElement("bill-snags");
	}
}
