package com.liferay.portal.workflow.taglib.internal.portlet.provider;

import com.liferay.portal.kernel.portlet.BasePortletProvider;
import com.liferay.portal.kernel.portlet.ViewPortletProvider;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	property = "model.class.name=com_liferay_portal_workflow_taglib_internal_TrackApprovalPortlet",
	service = ViewPortletProvider.class
)
public class TrackApprovalPortletProvider
	extends BasePortletProvider implements ViewPortletProvider {

	@Override
	public String getPortletName() {
		return "com_liferay_portal_workflow_taglib_TrackApprovalPortlet";
	}
}
