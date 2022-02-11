/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile;

import java.util.Arrays;

class Query {
	private String   table;
	private String[] columns;
	private String   selection;
	private String[] selectionArgs;
	private String   groupBy;
	private String   having;
	private String   orderBy;
	private String   limit;

	private Query() {
	}

	public static class Builder {
		private Query query;

		/**
		 * Create a {@code Query.Builder} object with required Query parameters.
		 * <p>
		 * If null is passed for {@code columns}, all the columns for the rows shall be returned in query result.
		 *
		 * @param table {@link String} containing the table name to compile the query against
		 * @param columns {@code String[]} containing the list of columns to return
		 */
		public Builder(final String table, final String[] columns) {
			query = new Query();
			query.table = table;
			query.columns = columns;
		}

		/**
		 * Query selection.
		 * <p>
		 * If null value is passed in {@code selection}, all the table rows shall be returned in query result.
		 *
		 * @param selection {@link String} containing filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself)
		 * @param selectionArgs {@code String[]} array containing values which will replace ?s in selection, in order that they appear in the selection
		 *
		 * @return this {@link Query.Builder} object
		 */
		Builder selection(final String selection, final String[] selectionArgs) {
			query.selection = selection;
			query.selectionArgs = selectionArgs;
			return this;
		}

		/**
		 * Group query result.
		 * <p>
		 * If null value is passed in {@code groupBy}, the table rows shall not be grouped in query result.
		 *
		 * @param groupBy {@link String} containing filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding
		 *                      the GROUP BY itself)
		 *
		 * @return this {@link Query.Builder} object
		 */
		Builder groupBy(final String groupBy) {
			query.groupBy = groupBy;
			return this;
		}

		/**
		 * Having clause for this query.
		 * <p>
		 * If null value is passed in {@code having}, all row groups will be included in the query result.
		 *
		 * @param having {@link String} containing filter declaring which row groups to include in the query result, if row grouping is being
		 *                     used, formatted as an SQL HAVING clause (excluding the HAVING itself)
		 *
		 * @return this {@link Query.Builder} object
		 */
		Builder having(final String having) {
			query.having = having;
			return this;
		}

		/**
		 * Order result rows for this query's result.
		 * <p>
		 * If null value is passed in {@code orderBy}, default sort order will be used for the query result, which may be unordered.
		 *
		 * @param orderBy {@link String} describing how to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY
		 *                      itself)
		 *
		 * @return this {@link Query.Builder} object
		 */
		Builder orderBy(final String orderBy) {
			query.orderBy = orderBy;
			return this;
		}

		/**
		 * Limit the number of rows in the query result.
		 * <p>
		 * Passing null value in {@code limit}, denotes absence of LIMIT clause.
		 *
		 * @param limit {@link String} indicating the number of rows returned by the query, formatted as LIMIT clause (excluding
		 *                     the LIMIT itself)
		 *
		 * @return this {@link Query.Builder} object
		 */
		Builder limit(final String limit) {
			query.limit = limit;
			return this;
		}

		/**
		 * Build the {@code Query} object
		 *
		 * @return the {@link Query} object
		 */
		public Query build() {
			return query;
		}
	}

	/**
	 * Returns the table name in this {@code Query}.
	 *
	 * @return {@link String} containing the table name
	 */
	String getTable() {
		return table;
	}

	/**
	 * Returns the table column names in this {@code Query}.
	 *
	 * @return {@code String[]} array containing the column names
	 */
	String[] getColumns() {
		return columns != null ? Arrays.copyOf(columns, columns.length) : null;
	}


	/**
	 * Returns the SELECT clause in this {@code Query}.
	 *
	 * @return {@link String} specifying the SELECT clause
	 */
	String getSelection() {
		return selection;
	}

	/**
	 * Returns the SELECT clause arguments in this {@code Query}.
	 *
	 * @return {@code String[]} array containing the SELECT clause arguments
	 */
	String[] getSelectionArgs() {
		return selectionArgs != null ? Arrays.copyOf(selectionArgs, selectionArgs.length) : null;
	}

	/**
	 * Returns the GROUP BY clause in this {@code Query}.
	 *
	 * @return {@link String} specifying the GROUP BY clause
	 */
	String getGroupBy() {
		return groupBy;
	}

	/**
	 * Returns the HAVING clause in this {@code Query}.
	 *
	 * @return {@link String} specifying the HAVING clause
	 */
	String getHaving() {
		return having;
	}

	/**
	 * Returns the ORDER BY clause in this {@code Query}.
	 *
	 * @return {@link String} specifying the ORDER BY clause
	 */
	String getOrderBy() {
		return orderBy;
	}

	/**
	 * Returns the LIMIT clause in this {@code Query}.
	 *
	 * @return {@link String} specifying the LIMIT clause
	 */
	String getLimit() {
		return limit;
	}

}
