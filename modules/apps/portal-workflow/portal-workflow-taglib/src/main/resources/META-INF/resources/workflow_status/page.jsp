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

<span class="taglib-workflow-status">
	<c:if test="<%= Validator.isNotNull(workflowStatusDisplayContext.getId(request)) %>">
		<span class="mr-2 workflow-id">
			<span class="workflow-label"><liferay-ui:message key="id" />:</span>

			<span class="workflow-value">
				<%= HtmlUtil.escape(workflowStatusDisplayContext.getId(request)) %>
			</span>
		</span>
	</c:if>

	<c:if test="<%= Validator.isNotNull(workflowStatusDisplayContext.getVersion(request)) %>">
		<span class="mr-2 workflow-version">
			<span class="workflow-label"><liferay-ui:message key="version" />:</span>

			<span class="workflow-value"><%= workflowStatusDisplayContext.getVersion(request) %></span>
		</span>
	</c:if>

	<span class="workflow-status">
		<c:if test="<%= workflowStatusDisplayContext.isShowLabel(request) %>">
			<span class="workflow-label"><liferay-ui:message key="status" />:</span>
		</c:if>

		<span class="label label-<%= WorkflowConstants.getStatusStyle(workflowStatusDisplayContext.getStatus(request)) %> status workflow-status-<%= WorkflowConstants.getStatusLabel(workflowStatusDisplayContext.getStatus(request)) %> <%= WorkflowConstants.getStatusCssClass(workflowStatusDisplayContext.getStatus(request)) %> workflow-value">
			<liferay-ui:message key="<%= workflowStatusDisplayContext.getStatusMessage(request) %>" /><%= workflowStatusDisplayContext.getInstanceStatus(request, TagResourceBundleUtil.getResourceBundle(request, locale)) %>
		</span>
	</span>

	<c:if test="<%= workflowStatusDisplayContext.isShowHelpMessage(request) && Validator.isNotNull(workflowStatusDisplayContext.getHelpMessage(request)) %>">
		<liferay-ui:icon-help message="<%= workflowStatusDisplayContext.getHelpMessage(request) %>" />
	</c:if>
</span>