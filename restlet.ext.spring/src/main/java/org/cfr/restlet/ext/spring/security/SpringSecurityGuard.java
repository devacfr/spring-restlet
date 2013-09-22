package org.cfr.restlet.ext.spring.security;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.security.Guard;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.util.Assert;

public class SpringSecurityGuard extends Guard implements ApplicationContextAware, InitializingBean {

    private AuthenticationManager authentificationManager;

    private ApplicationContext applicationContext;

    private AccessDecisionManager accessDecisionManager;

    private WebInvocationPrivilegeEvaluator invocationPrivilegeEvaluator;

    private FilterChainProxy filterChainProxy;

    public WebInvocationPrivilegeEvaluator getInvocationPrivilegeEvaluator() {
        return invocationPrivilegeEvaluator;
    }

    public void setInvocationPrivilegeEvaluator(WebInvocationPrivilegeEvaluator invocationPrivilegeEvaluator) {
        this.invocationPrivilegeEvaluator = invocationPrivilegeEvaluator;
    }

    public SpringSecurityGuard() {
        this(null);
    }

    public SpringSecurityGuard(Context context) {
        super(context, ChallengeScheme.HTTP_BASIC, "Spring Security");
    }

    public AuthenticationManager getAuthentificationManager() {
        return authentificationManager;
    }

    public void setAuthentificationManager(AuthenticationManager authentificationManager) {
        this.authentificationManager = authentificationManager;
    }

    public AccessDecisionManager getAccessDecisionManager() {
        return accessDecisionManager;
    }

    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
        this.accessDecisionManager = accessDecisionManager;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.applicationContext, "applicationContext is null");

        if (null == authentificationManager) {
            setAuthentificationManager((AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER));
        }
        filterChainProxy = applicationContext.getBean(BeanIds.FILTER_CHAIN_PROXY, FilterChainProxy.class);
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        if (!checkSecurity())
            return true;
        try {
            Authentication auth = authentificationManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, new String(secret)));
            if (auth.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            return auth.isAuthenticated();
        } catch (AuthenticationException e) {
            SecurityContextHolder.getContext().setAuthentication(null);
            return false;
        }
    }

    @Override
    public boolean authorize(Request request) {
        if (!checkSecurity())
            return true;
        try {
            String url = getRelativePath(request);
            if (StringUtils.isEmpty(url)) {
                url = "/"; // bugfix spring security
            }
            // no filter belongs to url
            if (filterChainProxy != null && filterChainProxy.getFilters(url) != null && filterChainProxy.getFilters(url).isEmpty()) {
                return true;
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return invocationPrivilegeEvaluator.isAllowed(null, url, request.getMethod().getName(), auth);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        } catch (InsufficientAuthenticationException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkSecurity() {
        return this.accessDecisionManager != null && this.authentificationManager != null && invocationPrivilegeEvaluator != null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int authenticate(Request request) {
        String url = getRelativePath(request);
        // no filter belongs to url
        if (filterChainProxy != null && filterChainProxy.getFilters(url) != null && filterChainProxy.getFilters(url).isEmpty()) {
            return Guard.AUTHENTICATION_VALID;
        }
        int result = Guard.AUTHENTICATION_MISSING;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (this.getScheme() != null) {
            // An authentication scheme has been defined,
            // the request must be authenticated
            final ChallengeResponse cr = request.getChallengeResponse();

            if (cr != null) {
                if (this.getScheme().equals(cr.getScheme())) {
                    if (auth != null) {
                        if (auth.isAuthenticated()) {
                            return Guard.AUTHENTICATION_VALID;
                        }
                    } else {
                        throw new IllegalArgumentException("Challenge scheme " + this.getScheme() + " not supported by the Restlet engine.");
                    }
                } else {
                    // The challenge schemes are incompatible, we need to
                    // challenge the client
                }
            } else {
                if (auth != null) {
                    ChallengeResponse challengeResponse = new ChallengeResponse(this.getScheme(), auth.getName(), "");
                    if (auth.getDetails() instanceof User) {
                        User user = (User) auth.getDetails();
                        challengeResponse.setRawValue(user.getPassword());
                        challengeResponse.setRealm(this.getRealm());
                        org.restlet.security.User u = new org.restlet.security.User(user.getUsername());
                        if (user.getPassword() != null)
                            u.setSecret(user.getPassword().toCharArray());
                        request.getClientInfo().setUser(u);
                    }
                    challengeResponse.setAuthenticated(auth.isAuthenticated());
                    result = Guard.AUTHENTICATION_VALID;
                    request.setChallengeResponse(challengeResponse);
                }
                // No challenge response found, we need to challenge the client
            }
        }

        if (request.getChallengeResponse() != null) {
            // Update the challenge response accordingly
            request.getChallengeResponse().setAuthenticated(result == Guard.AUTHENTICATION_VALID);
        }

        // Update the client info accordingly
        request.getClientInfo().setAuthenticated(result == Guard.AUTHENTICATION_VALID);

        return result;
    }

    protected String getRelativePath(Request request) {
        Reference baseRef = request.getRootRef();
        String baseUrl = baseRef.toString();
        String url = null;
        if (baseRef == null) {
            url = baseUrl;
        } else {
            url = request.getResourceRef().toString();
            if (url.startsWith(baseUrl)) {
                url = url.substring(baseUrl.length());
            } else {
                url = request.getResourceRef().getPath();
            }
        }
        return url;
    }

}