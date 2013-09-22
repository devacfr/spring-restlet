package com.pmi.restlet.request.extjs;

import java.util.Iterator;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.Request;
import org.restlet.data.Form;
import org.springframework.util.StringUtils;

import com.pmi.restlet.request.extjs.Query.SortDirection;

@XmlRootElement()
public class QueryRequest {

    public static Query createQuery(QueryRequest queryRequest) {
        Query query = new Query();
        query.setQuery(queryRequest.getQuery());
        query.setStart(queryRequest.getStart());
        query.setLimit(queryRequest.getLimit());
        if (queryRequest.getSortDirection() != null) {
            if ("ASC".equals(queryRequest.getSortDirection())) {
                query.setSortDirection(SortDirection.Ascending);
            } else {
                query.setSortDirection(SortDirection.Descending);
            }
        }
        query.setSortProperty(queryRequest.getSortProperty());
        return query;
    }

    /**
     * Field from.
     */
    private int start = 0;

    /**
     * Field count.
     */
    private int limit = 0;

    private String query;

    private String sortProperty;

    private String sortDirection;

    private Filter[] filters;

    public QueryRequest(Request request) {
        Form form = request.getResourceRef().getQueryAsForm();
        query = form.getFirstValue("query");
        if (form.getFirstValue("start") != null)
            start = Integer.parseInt(form.getFirstValue("start"));
        if (form.getFirstValue("limit") != null)
            limit = Integer.parseInt(form.getFirstValue("limit"));
        if (form.getFirstValue("filter") != null) {

            JSONArray ar = JSONArray.fromObject(form.getFirstValue("filter"));
            Filter[] fs = new Filter[ar.size()];
            int x = 0;
            for (@SuppressWarnings("unchecked")
            Iterator<JSONObject> i = ar.iterator(); i.hasNext();) {
                JSONObject jsonObject = i.next();
                Filter f = (Filter) JSONObject.toBean(jsonObject, Filter.class);
                fs[x++] = f;
            }
            filters = fs;
        }
        sortProperty = form.getFirstValue("sort");
        sortDirection = form.getFirstValue("dir");
    }

    public Filter[] getFilters() {
        return filters;
    }

    public Filter getFilter(String propertyName) {
        if (filters == null || filters.length == 0 || !StringUtils.hasText(propertyName))
            return null;
        for (Filter f : filters) {
            if (f.getProperty().equals(propertyName))
                return f;
        }
        return null;
    }

    public int getLimit() {
        return limit;
    }

    public String getQuery() {
        return query;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public String getSortProperty() {
        return sortProperty;
    }

    public int getStart() {
        return start;
    }

}
