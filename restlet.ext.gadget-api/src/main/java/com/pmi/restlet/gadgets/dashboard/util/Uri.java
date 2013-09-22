package com.pmi.restlet.gadgets.dashboard.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.cfr.commons.util.Assert;

import com.pmi.restlet.gadgets.GadgetSpecUriNotAllowedException;

public class Uri {

	private Uri() {
		throw new AssertionError("noninstantiable");
	}

	public static String decodeUriComponent(String uriComponent) {
		try {
			return URLDecoder.decode(uriComponent, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error("JREs are required to support UTF-8");
		}
	}

	public static String encodeUriComponent(String uriComponent) {
		try {
			return URLEncoder.encode(uriComponent, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error("JREs are required to support UTF-8");
		}
	}

	public static boolean isValid(String gadgetUri) {
		if (StringUtils.isBlank(gadgetUri)) {
			return false;
		}
		try {
			URI uri = new URI(gadgetUri).normalize();
			if (uri.isAbsolute() && !"http".equalsIgnoreCase(uri.getScheme())
					&& !"https".equalsIgnoreCase(uri.getScheme())) {
				return false;
			}
		} catch (URISyntaxException e) {
			return false;
		}
		return true;
	}

	public static URI create(String gadgetUri) throws GadgetSpecUriNotAllowedException {
		if (!isValid(gadgetUri)) {
			throw new GadgetSpecUriNotAllowedException(
					"gadget spec url must be a valid url beginning with either http: or https:");
		} else {
			return URI.create(gadgetUri);
		}
	}

	public static String ensureTrailingSlash(String url) {
		return url.endsWith("/") ? url : new StringBuilder().append(url).append("/").toString();
	}

	public static URI resolveUriAgainstBase(String baseUrl, URI possiblyRelativeUri) {
		Assert.notNull(baseUrl, "baseUrl");
		Assert.notNull(possiblyRelativeUri, "possiblyRelativeUri");
		return possiblyRelativeUri.isAbsolute() ? possiblyRelativeUri : URI.create(ensureTrailingSlash(baseUrl))
				.resolve(possiblyRelativeUri);
	}

	public static URI resolveUriAgainstBase(String baseUrl, String possiblyRelativeUri) {
		return resolveUriAgainstBase(baseUrl, URI.create(possiblyRelativeUri));
	}

	public static URI relativizeUriAgainstBase(String baseUrl, URI possiblyAbsoluteUri) {
		Assert.notNull(baseUrl, "baseUrl");
		Assert.notNull(possiblyAbsoluteUri, "possiblyAbsoluteUri");
		return possiblyAbsoluteUri.isAbsolute() ? URI.create(ensureTrailingSlash(baseUrl)).relativize(
				possiblyAbsoluteUri) : possiblyAbsoluteUri;
	}

	public static URI relativizeUriAgainstBase(String baseUrl, String possiblyAbsoluteUri) {
		return relativizeUriAgainstBase(baseUrl, URI.create(possiblyAbsoluteUri));
	}
}