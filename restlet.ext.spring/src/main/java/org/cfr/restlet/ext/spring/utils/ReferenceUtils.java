package org.cfr.restlet.ext.spring.utils;

import org.cfr.commons.web.ui.WebUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Reference;

import com.pmi.restlet.utils.Assert;

public final class ReferenceUtils {

	/**
	 * <p>
	 * Valid characters in a scheme.
	 * </p>
	 * <p>
	 * RFC 1738 says the following:
	 * </p>
	 * <blockquote> Scheme names consist of a sequence of characters. The lower
	 * case letters "a"--"z", digits, and the characters plus ("+"), period
	 * ("."), and hyphen ("-") are allowed. For resiliency, programs
	 * interpreting URLs should treat upper case letters as equivalent to lower
	 * case in scheme names (e.g., allow "HTTP" as well as "http").
	 * </blockquote>
	 * <p>
	 * We treat as absolute any URL that begins with such a scheme name,
	 * followed by a colon.
	 * </p>
	 */
	public static final String VALID_SCHEME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";

	/**
	 * create absolute path reference
	 * 
	 * @param request
	 *            the request
	 * @param relPart
	 *            the relative path
	 * @return Returns the reference representation of absolute path.
	 */
	public static Reference createRootReference(Request request, String relPart) {
		Assert.notNull(relPart, "relPart is required");
		Assert.notNull(request, "request is required");
		Assert.notNull(request.getRootRef(), "root reference in request is required");
		if (!relPart.isEmpty() && relPart.charAt(0) == '/') {
			relPart = relPart.substring(1);
		}
		Reference ref = new Reference(new Reference(WebUtils.CleanupPath(request.getRootRef().toString())), relPart);
		return ref.getTargetRef();
	}

	/**
	 * Get the context path of request
	 * 
	 * @param context
	 *            the context
	 * @param request
	 *            the request
	 * @return Returns the reference representation of context path.
	 */
	public static Reference getContextPath(Context context, Request request) {
		Reference contextPath = null;
		contextPath = new Reference(request.getHostRef(), getContextPath(request));
		return contextPath.getTargetRef();
	}

	/**
	 * Get the context path of request
	 * 
	 * @param request
	 *            the request.
	 * @return Returns the string representation of context path.
	 */
	public static String getContextPath(Request request) {
		if (request.getRootRef() == null) {
			return "";
		}
		return request.getRootRef().toString();
	}

	/**
	 * Gets a value indicating whether the url is absolute.
	 * 
	 * @param url
	 *            the url to check
	 * @return Returns <code>true</code> if the url is absolute, otherwise
	 *         <code>false</code>.
	 */
	public static boolean isAbsoluteUrl(String url) {
		// a null URL is not absolute, by our definition
		if (url == null) {
			return false;
		}

		// do a fast, simple check first
		int colonPos = url.indexOf(":");
		if (colonPos == -1) {
			return false;
		}

		// if we DO have a colon, make sure that every character
		// leading up to it is a valid scheme character
		for (int i = 0; i < colonPos; i++) {
			if (VALID_SCHEME_CHARS.indexOf(url.charAt(i)) == -1) {
				return false;
			}
		}

		// if so, we've got an absolute url
		return true;
	}

	/**
	 * 
	 * @param url
	 * @param request
	 * @return
	 */
	public static String resolveUrl(String url, Request request) {
		// don't touch absolute URLs
		if (isAbsoluteUrl(url)) {
			return url;
		}

		String contextPath = getContextPath(request);
		// normalize relative URLs against a context root

		if (url.startsWith("/")) {
			return contextPath + url;
		} else {
			return url;
		}

	}
}
