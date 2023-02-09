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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %><%@
taglib uri="http://liferay.com/tld/frontend-data-set" prefix="frontend-data-set" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.notification.web.internal.custom.tab.CustomTabDisplayContext" %><%@
page import="com.liferay.object.model.ObjectRelationship" %><%@
page import="com.liferay.portal.kernel.util.Constants" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<%
CustomTabDisplayContext customTabDisplayContext = (CustomTabDisplayContext)request.getAttribute("displayContext");

ObjectRelationship objectRelationship = customTabDisplayContext.getObjectRelationship();
%>

<portlet:actionURL name="/object_entries/edit_object_entry_related_model" var="editObjectEntryRelatedModelActionURL" />

<aui:form action="<%= editObjectEntryRelatedModelActionURL %>" method="post" name="fm">
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.ASSIGN %>" />
	<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />
	<aui:input name="objectRelationshipId" type="hidden" value="<%= objectRelationship.getObjectRelationshipId() %>" />
	<aui:input name="objectEntryId" type="hidden" value="<%= customTabDisplayContext.getObjectEntryId() %>" />
	<aui:input name="objectRelationshipPrimaryKey2" type="hidden" value="" />

	<frontend-data-set:headless-display
		apiURL="<%= customTabDisplayContext.getAPIURL() %>"
		creationMenu="<%= customTabDisplayContext.getCreationMenu() %>"
		fdsActionDropdownItems="<%= customTabDisplayContext.getFDSActionDropdownItems() %>"
		formName="fm"
		id="customTabTableFDSViewId"
		style="fluid"
	/>
</aui:form>

<c:if test="<%= !customTabDisplayContext.isDefaultUser() %>">
	<aui:script sandbox="<%= true %>">
		const eventHandlers = [];

		const selectRelatedModelHandler = Liferay.on(
			'<portlet:namespace />selectRelatedModel',
			() => {
				Liferay.Util.openSelectionModal({
					multiple: false,
					onSelect: (selectedItem) => {
						const objectEntry = JSON.parse(selectedItem.value);

						const objectRelationshipPrimaryKey2Input = document.getElementById(
							'<portlet:namespace />objectRelationshipPrimaryKey2'
						);

						objectRelationshipPrimaryKey2Input.value = objectEntry.classPK;

						const form = document.getElementById('<portlet:namespace />fm');

						if (form) {
							submitForm(form);
						}
					},
					selectEventName: '<portlet:namespace />selectRelatedModalEntry',
					title: '<liferay-ui:message key="select" />',
					url:
						'<%= customTabDisplayContext.getRelatedObjectEntryItemSelectorURL() %>',
				});
			}
		);

		eventHandlers.push(selectRelatedModelHandler);

		Liferay.on('destroyPortlet', () => {
			eventHandlers.forEach((eventHandler) => {
				eventHandler.detach();
			});
		});
	</aui:script>
</c:if>