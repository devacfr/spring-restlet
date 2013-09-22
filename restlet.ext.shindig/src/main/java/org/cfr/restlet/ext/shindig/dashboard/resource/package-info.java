/**
 * <p>Interfaces and classes used to implement dashboards and their gadgets
 * as REST resources.</p>
 *
 * <p>Generally, the {@code Handler} classes define
 * a resource operation in terms of domain objects. Instances of
 * {@code Handler}s are injected into {@code Resource} classes.
 * {@code Representation} classes define JAXB bindings between
 * models in this package and JSON (or XML) that is sent to clients.</p>
 */
package org.cfr.restlet.ext.shindig.dashboard.resource;