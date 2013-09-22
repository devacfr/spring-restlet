package org.cfr.restlet.ext.spring;

/**
 * Allows to define security permission according to restlet spring resource.
 * @author cfriedri
 *
 */
public class PathProtectionDescriptor {

    private String pathPattern;

    private String filterExpression;

    /**
     * Default constructor.
     * @param pathPattern define the ant pattern associated to a resource
     * @param filterExpression contains of type of security and permission to apply. sample "OAuth,permission[accessUser]"
     */
    public PathProtectionDescriptor(String pathPattern, String filterExpression) {
        this.pathPattern = pathPattern;

        this.filterExpression = filterExpression;
    }

    /**
     * Gets the ant pattern of path.
     * @return Returns a string representing the ant pattern of path. 
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * Sets the ant pattern of path. 
     * @param pathPattern a string representing the ant pattern of path.
     */
    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    /**
     * Gets the security expression.
     * @return Returns a string representing the security expression.
     */
    public String getFilterExpression() {
        return filterExpression;
    }

    /**
     * Sets the security expression.
     * @param filterExpression a string representing the security expression.
     */
    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

}
