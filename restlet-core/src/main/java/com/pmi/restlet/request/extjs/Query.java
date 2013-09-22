package com.pmi.restlet.request.extjs;



public class Query {

	public static enum SortDirection {

		/**
		 *
		 */
        Ascending,

        /**
         *
         */
        Descending;


        public boolean  isAscending() {
        	return SortDirection.Ascending == this;
        }

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

	private SortDirection sortDirection = SortDirection.Ascending;

	public int getStart() {
		return start;
	}

	public int getLimit() {
		return limit;
	}

	public String getQuery() {
		return query;
	}

	public String getSortProperty() {
		return sortProperty;
	}

	public SortDirection getSortDirection() {
		return sortDirection;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setSortProperty(String sortProperty) {
		this.sortProperty = sortProperty;
	}

	public void setSortDirection(SortDirection sortDirection) {
		this.sortDirection = sortDirection;
	}


}
