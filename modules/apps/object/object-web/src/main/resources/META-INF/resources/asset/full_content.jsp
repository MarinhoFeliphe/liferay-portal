<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
	ViewObjectEntryDisplayContext viewObjectEntryDisplayContext = ViewObjectEntryDisplayContextProviderUtil.getViewObjectEntryDisplayContext(request);

	ObjectEntry objectEntry = viewObjectEntryDisplayContext.getObjectEntry();
	ObjectLayoutTab objectLayoutTab = viewObjectEntryDisplayContext.getObjectLayoutTab();
%>

<clay:navigation-bar
	inverted="<%= false %>"
	navigationItems="<%= viewObjectEntryDisplayContext.getNavigationItems() %>"
/>

<c:choose>
	<c:when test="<%= (objectLayoutTab != null) && (objectLayoutTab.getObjectRelationshipId() > 0) %>">
		<clay:data-set-display
			contextParams='<%=
			HashMapBuilder.<String, String>put(
				"objectEntryId", String.valueOf(objectEntry.getObjectEntryId())
			).put(
				"objectRelationshipId", String.valueOf(objectLayoutTab.getObjectRelationshipId())
			).build()
		%>'
			creationMenu="<%= viewObjectEntryDisplayContext.getRelatedModelCreationMenu() %>"
			dataProviderKey="<%= ObjectEntriesFDSNames.RELATED_MODELS %>"
			formName="fm"
			id="<%= ObjectEntriesFDSNames.RELATED_MODELS %>"
			itemsPerPage="<%= 20 %>"
			namespace="<%= liferayPortletResponse.getNamespace() %>"
			pageNumber="<%= 1 %>"
			portletURL="<%= liferayPortletResponse.createRenderURL() %>"
			style="fluid"
		/>
	</c:when>
	<c:otherwise>
		<liferay-frontend:fieldset-group>
			<clay:sheet-section>
				<clay:row>
					<clay:col
						md="12"
					>
						<%= viewObjectEntryDisplayContext.renderDDMForm(pageContext) %>
					</clay:col>
				</clay:row>
			</clay:sheet-section>
		</liferay-frontend:fieldset-group>
	</c:otherwise>
</c:choose>