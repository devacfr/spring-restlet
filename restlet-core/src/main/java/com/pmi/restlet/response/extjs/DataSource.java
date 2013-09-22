package com.pmi.restlet.response.extjs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.pmi.restlet.utils.Assert;

@XmlRootElement()
public class DataSource<T> {

    private final long countTotal;

    private final List<T> data;

    private final int pageSize;

    private Collection<T> results = null;

    private final int start;

    public DataSource(List<T> data, int pageSize, int start) {
        Assert.notNull(data);
        this.countTotal = data.size();
        this.data = data;
        this.pageSize = pageSize;
        this.results = null;
        this.start = start;
    }

    public DataSource(List<T> data, int pageSize, int start, long totalCount) {
        Assert.notNull(data);
        this.countTotal = totalCount;
        this.data = data;
        this.pageSize = pageSize;
        this.results = data;
        this.start = start;
    }

    public long getCountTotal() {
        return countTotal;
    }

    public Collection<T> getData() {
        return data;
    }

    public Collection<T> list() {
        return getPagingData(this.start);
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getStart() {
        return start;
    }

    public int size() {
        return data.size();
    }

    protected synchronized Collection<T> getPagingData(int start) {
        int length = data.size();

        if ((pageSize >= 0 || start >= 0) && length > 0) {
            if (results == null) {
                List<T> tmp = new ArrayList<T>(pageSize);
                int end = start + pageSize;

                if (end > length || end == 0) {
                    end = length;
                }

                for (int i = start; i < end; i++) {
                    tmp.add(data.get(i));
                }
                results = Collections.unmodifiableCollection(tmp);
            }

            return results;
        }

        return data;
    }
}
