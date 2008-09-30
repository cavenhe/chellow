/*
 
 Copyright 2005, 2008 Meniscus Systems Ltd
 
 This file is part of Chellow.

 Chellow is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Chellow is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Chellow; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */

package net.sf.chellow.physical;

import java.util.List;

import net.sf.chellow.billing.Provider;
import net.sf.chellow.monad.Hiber;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.Invocation;
import net.sf.chellow.monad.MonadUtils;
import net.sf.chellow.monad.Urlable;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.XmlDescriber;
import net.sf.chellow.monad.XmlTree;
import net.sf.chellow.monad.types.EmailAddress;
import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.monad.types.UriPathElement;
import net.sf.chellow.ui.Chellow;
import net.sf.chellow.ui.Me;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Users implements Urlable, XmlDescriber {
	public static final UriPathElement URI_ID;

	static {
		try {
			URI_ID = new UriPathElement("users");
		} catch (HttpException e) {
			throw new RuntimeException(e);
		}
	}

	public Users() {
	}

	public MonadUri getUri() throws HttpException {
		return Chellow.ROOT_URI.resolve(getUriId()).append("/");
	}

	public void httpPost(Invocation inv) throws HttpException {
		EmailAddress emailAddress = inv.getEmailAddress("email-address");
		String password = inv.getString("password");
		Integer userRole = inv.getInteger("role");
		String participantCode = inv.getString("participant-code");
		Character roleCode = inv.getCharacter("role-code");

		if (!inv.isValid()) {
			throw new UserException();
		}
		Provider provider = null;
		if (userRole == User.PROVIDER_VIEWER) {
			provider = Provider.getProvider(participantCode, roleCode);
		}
		User user = User.insertUser(emailAddress, password, userRole, provider);
		Hiber.commit();
		inv.sendCreated(user.getUri());
	}

	@SuppressWarnings("unchecked")
	public void httpGet(Invocation inv) throws HttpException {
		Document doc = MonadUtils.newSourceDocument();
		Element source = doc.getDocumentElement();
		Element usersElement = (Element) toXml(doc);
		source.appendChild(usersElement);
		for (User user : (List<User>) Hiber.session().createQuery(
				"from User user").list()) {
			usersElement.appendChild(user.toXml(doc));
		}
		inv.sendOk(doc);
	}

	public UriPathElement getUriId() {
		return URI_ID;
	}

	public Urlable getChild(UriPathElement uriId) throws HttpException {
		if (Me.URI_ID.equals(uriId)) {
			return new Me();
		} else {
			return (User) Hiber.session().createQuery(
					"from User user where user.id = :userId").setLong("userId",
					Long.parseLong(uriId.getString())).uniqueResult();
		}
	}

	public User findUser(EmailAddress emailAddress) throws InternalException {
		return (User) Hiber
				.session()
				.createQuery(
						"from User user where user.emailAddress.address = :emailAddress")
				.setString("emailAddress", emailAddress.toString())
				.uniqueResult();
	}

	public void httpDelete(Invocation inv) throws HttpException {
		// TODO Auto-generated method stub

	}

	public UriPathElement setUriId(UriPathElement uriId)
			throws InternalException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node toXml(Document doc) throws HttpException {
		return doc.createElement("users");
	}

	public Node toXml(Document doc, XmlTree tree) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}
}