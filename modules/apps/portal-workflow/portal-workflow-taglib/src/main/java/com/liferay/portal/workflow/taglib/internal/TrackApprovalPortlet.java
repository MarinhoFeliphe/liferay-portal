package com.liferay.portal.workflow.taglib.internal;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import org.osgi.service.component.annotations.Component;

import javax.portlet.Portlet;

@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"javax.portlet.display-name=Track Approval",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/init.jsp",
		"javax.portlet.name=com_liferay_portal_workflow_taglib_TrackApprovalPortlet",
		"javax.portlet.security-role-ref=guest,power-user,user"
	},
	service = Portlet.class
)
public class TrackApprovalPortlet extends MVCPortlet {
}
