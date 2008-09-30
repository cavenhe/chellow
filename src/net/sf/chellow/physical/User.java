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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import net.sf.chellow.billing.Provider;
import net.sf.chellow.monad.Hiber;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.Invocation;
import net.sf.chellow.monad.MonadUtils;
import net.sf.chellow.monad.NotFoundException;
import net.sf.chellow.monad.Urlable;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.XmlTree;
import net.sf.chellow.monad.types.EmailAddress;
import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.monad.types.UriPathElement;
import net.sf.chellow.ui.Chellow;

import org.hibernate.exception.ConstraintViolationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.Ostermiller.util.Base64;

public class User extends PersistentEntity {
	public static final UriPathElement USERS_URI_ID;
	public static final int EDITOR = 0;
	public static final int ORG_VIEWER = 1;
	public static final int PROVIDER_VIEWER = 2;
	public static final EmailAddress BASIC_USER_EMAIL_ADDRESS;

	static {
		try {
			BASIC_USER_EMAIL_ADDRESS = new EmailAddress("basic-user@localhost");
		} catch (HttpException e) {
			throw new RuntimeException(e);
		}
	}

	static {
		try {
			USERS_URI_ID = new UriPathElement("users");
		} catch (HttpException e) {
			throw new RuntimeException(e);
		}
	}

	static public User getUser(Long id) {
		return (User) Hiber.session().get(User.class, id);
	}

	static public User findUserByEmail(EmailAddress emailAddress)
			throws InternalException {
		return (User) Hiber
				.session()
				.createQuery(
						"from User user where user.emailAddress.address = :emailAddress")
				.setString("emailAddress", emailAddress.getAddress())
				.uniqueResult();
	}

	static public User insertUser(EmailAddress emailAddress,
			String password, int role, Provider provider) throws HttpException {
		User user = null;
		try {
			user = new User(emailAddress, password, role, provider);
			Hiber.session().save(user);
			Hiber.flush();
		} catch (ConstraintViolationException e) {
			SQLException sqle = e.getSQLException();
			if (sqle != null) {
				Exception nextException = sqle.getNextException();
				if (nextException != null) {
					String message = nextException.getMessage();
					if (message != null
							&& message.contains("user_email_address_key")) {
						throw new UserException(
								"There is already a user with this email address.");
					} else {
						throw e;
					}
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
		return user;
	}

	public static String digest(String password) throws HttpException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes("UTF-8"));
			return Base64.encodeToString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new InternalException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new InternalException(e.getMessage());
		}
	}

	private EmailAddress emailAddress;

	private String passwordDigest;

	private int role;
	
	private Provider provider;
	
	//private Set<Role> roles;

	public User() {
	}

	public User(EmailAddress emailAddress, String password, int role, Provider provider)
			throws HttpException {
		update(emailAddress, password);
	}

	public void update(EmailAddress emailAddress, String password)
			throws HttpException {
		setEmailAddress(emailAddress);
		if (password.length() < 6) {
			throw new UserException(
					"The password must be at least 6 characters long.");
		}
		setPasswordDigest(digest(password));
	}

	public String getPasswordDigest() {
		return passwordDigest;
	}

	protected void setPasswordDigest(String passwordDigest) {
		this.passwordDigest = passwordDigest;
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	protected void setEmailAddress(EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}

	public int getRole() {
		return role;
	}

	protected void setRole(int role) {
		this.role = role;
	}
	
	public Provider getProvider() {
		return provider;
	}
	
	void setProvider(Provider provider) {
		this.provider = provider;
	}

	public boolean equals(Object object) {
		boolean isEqual = false;
		if (object instanceof User) {
			User user = (User) object;
			isEqual = user.getId().equals(getId());
		}
		return isEqual;
	}

	public String toString() {
		try {
			return getUriId().toString();
		} catch (HttpException e) {
			throw new RuntimeException(e);
		}
	}

	public Element toXml(Document doc) throws HttpException {
		Element element = super.toXml(doc, "user");
		element.setAttributeNode(emailAddress.toXml(doc));
		return element;
	}

	public MonadUri getUri() throws HttpException {
		return Chellow.USERS_INSTANCE.getUri().resolve(getUriId());
	}

	public Urlable getChild(UriPathElement uriId) throws HttpException {
		throw new NotFoundException();
	}

	public void httpGet(Invocation inv) throws HttpException {
		inv.sendOk(document());
	}

	private Document document() throws HttpException {
		return document(null);
	}

	private Document document(String message) throws HttpException {
		Document doc = MonadUtils.newSourceDocument();
		Element source = doc.getDocumentElement();
		source.appendChild(toXml(doc, new XmlTree("roles")));
		if (message != null) {
			// source.appendChild(new VFMessage(message).toXML(doc));
		}
		return doc;
	}

	public void httpPost(Invocation inv) throws HttpException {
		if (inv.hasParameter("delete")) {
			Hiber.session().delete(this);
			Hiber.close();
			inv.sendSeeOther(Chellow.USERS_INSTANCE.getUri());
		} else if (inv.hasParameter("current-password")) {
			String currentPassword = inv.getString("current-password");
			String newPassword = inv.getString("new-password");
			String confirmNewPassword = inv.getString("confirm-new-password");

			if (!inv.isValid()) {
				throw new UserException(document());
			}
			if (!getPasswordDigest().equals(digest(currentPassword))) {
				throw new UserException("The current password is incorrect.");
			}
			if (!newPassword.equals(confirmNewPassword)) {
				throw new UserException("The new passwords aren't the same.");
			}
			setPasswordDigest(digest(newPassword));
			Hiber.commit();
			inv.sendOk(document("New password set successfully."));
		} else {
			throw new UserException(
					"I can't really see what you're trying to do.");
		}
	}
}