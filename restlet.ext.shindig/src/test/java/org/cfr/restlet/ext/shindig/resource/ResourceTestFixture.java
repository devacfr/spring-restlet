package org.cfr.restlet.ext.shindig.resource;

import java.util.Arrays;

import org.apache.shindig.gadgets.LockedDomainService;
import org.apache.shindig.gadgets.http.RequestPipeline;
import org.apache.shindig.gadgets.rewrite.CaptureRewriter;
import org.apache.shindig.gadgets.rewrite.DefaultResponseRewriterRegistry;
import org.apache.shindig.gadgets.rewrite.ResponseRewriter;
import org.apache.shindig.gadgets.rewrite.ResponseRewriterRegistry;
import org.cfr.commons.testing.EasyMockTestCase;

public abstract class ResourceTestFixture extends EasyMockTestCase {

	public final RequestPipeline pipeline = mock(RequestPipeline.class);

	public CaptureRewriter rewriter = new CaptureRewriter();

	public ResponseRewriterRegistry rewriterRegistry = new DefaultResponseRewriterRegistry(
			Arrays.<ResponseRewriter> asList(rewriter), null);

	public final LockedDomainService lockedDomainService = mock(LockedDomainService.class);
}
