package com.liferay.portal.workflow.taglib.internal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalService;
import com.liferay.portal.kernel.util.ParamUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

@Component(
	immediate = true,
	property = {
		"javax.portlet.name=com_liferay_portal_workflow_taglib_TrackApprovalPortlet",
		"mvc.command.name=/workflow_status/track_approval"
	},
	service = MVCRenderCommand.class
)
public class TrackApprovalMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
		RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		long companyId = ParamUtil.getLong(renderRequest, "companyId");
		String className = ParamUtil.getString(renderRequest, "className");
		long groupId = ParamUtil.getLong(renderRequest, "groupId");
		long primaryKey = ParamUtil.getLong(renderRequest, "primaryKey");

		return "/workflow_status/page.jsp";

		/*try {
			WorkflowInstanceLink workflowInstanceLink =
				_workflowInstanceLinkLocalService.getWorkflowInstanceLink(
					companyId, groupId, className, primaryKey);

			workflowInstanceLink.getWorkflowInstanceId();

			return "/workflow_status/page.jsp";
		}
		catch (PortalException e) {
			e.printStackTrace();
		}

		return null;*/
	}

	@Reference
	private WorkflowInstanceLinkLocalService _workflowInstanceLinkLocalService;
}
