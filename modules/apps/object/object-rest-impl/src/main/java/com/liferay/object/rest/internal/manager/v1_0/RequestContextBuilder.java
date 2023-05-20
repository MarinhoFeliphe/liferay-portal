/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.object.rest.internal.manager.v1_0;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.Locale;

import javax.ws.rs.core.UriInfo;

/**
 * @author Feliphe Marinho
 */
public class RequestContextBuilder {

	public RequestContextBuilder aggregation(Aggregation aggregation) {
		_requestContext.setAggregation(aggregation);

		return this;
	}

	public JSONObject buildJSONObject() throws JSONException {
		return JSONFactoryUtil.createJSONObject(
			JSONFactoryUtil.looseSerialize(_requestContext));
	}

	public RequestContextBuilder companyId(long companyId) {
		_requestContext.setCompanyId(companyId);

		return this;
	}

	public RequestContextBuilder externalReferenceCode(
		String externalReferenceCode) {

		_requestContext.setExternalReferenceCode(externalReferenceCode);

		return this;
	}

	public RequestContextBuilder filterString(String filterString) {
		_requestContext.setFilterString(filterString);

		return this;
	}

	public RequestContextBuilder locale(Locale locale) {
		_requestContext.setLocale(locale);

		return this;
	}

	public RequestContextBuilder objectDefinition(
		ObjectDefinition objectDefinition) {

		_requestContext.setObjectDefinition(objectDefinition);

		return this;
	}

	public RequestContextBuilder objectEntry(ObjectEntry objectEntry) {
		_requestContext.setObjectEntry(objectEntry);

		return this;
	}

	public RequestContextBuilder pagination(Pagination pagination) {
		_requestContext.setPagination(pagination);

		return this;
	}

	public RequestContextBuilder scopeKey(String scopeKey) {
		_requestContext.setScopeKey(scopeKey);

		return this;
	}

	public RequestContextBuilder search(String search) {
		_requestContext.setSearch(search);

		return this;
	}

	public RequestContextBuilder sorts(Sort[] sorts) {
		_requestContext.setSorts(sorts);

		return this;
	}

	public RequestContextBuilder uriInfo(UriInfo uriInfo) {
		_requestContext.setUriInfo(uriInfo);

		return this;
	}

	public RequestContextBuilder userId(long userId) {
		_requestContext.setUserId(userId);

		return this;
	}

	private final RequestContext _requestContext = new RequestContext();

}