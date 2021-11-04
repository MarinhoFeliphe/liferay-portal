package com.liferay.portal.workflow.taglib.servlet.taglib;

import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.workflow.taglib.internal.TrackApprovalMVCRenderCommand;
import com.liferay.portal.workflow.taglib.internal.TrackApprovalPortlet;
import com.liferay.portal.workflow.taglib.internal.portlet.provider.TrackApprovalPortletProvider;
import com.liferay.portlet.configuration.kernel.util.PortletConfigurationApplicationType;

import javax.portlet.PortletURL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class TrackApprovalURLTag extends TagSupport {

	public static String doTag(
		String modelResource, HttpServletRequest httpServletRequest)
		throws Exception {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		PortletURL portletURL = PortletProviderUtil.getPortletURL(
			httpServletRequest,
			"com_liferay_portal_workflow_taglib_internal_TrackApprovalPortlet",
			PortletProvider.Action.VIEW);

		portletURL.setWindowState(LiferayWindowState.POP_UP);

		portletURL.setParameter("mvcPath", "/track_approval_kebab_item/page.jsp");

		portletURL.setParameter(
			"portletConfiguration", Boolean.TRUE.toString());

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		portletURL.setParameter("portletResource", portletDisplay.getId());
		portletURL.setParameter("modelResource", modelResource);
		portletURL.setParameter(
			"modelResourceDescription", "Track Approval");

		return portletURL.toString();
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			String portletURLToString = doTag(_modelResource,
				(HttpServletRequest)pageContext.getRequest());

			if (Validator.isNotNull(_var)) {
				pageContext.setAttribute(_var, portletURLToString);
			}
			else {
				JspWriter jspWriter = pageContext.getOut();

				jspWriter.write(portletURLToString);
			}
		}
		catch (Exception exception) {
			throw new JspException(exception);
		}

		return EVAL_PAGE;
	}

	public String getModelResource() {
		return _modelResource;
	}

	public String getVar() {
		return _var;
	}

	public void setModelResource(String modelResource) {
		_modelResource = modelResource;
	}

	public void setVar(String var) {
		_var = var;
	}

	private String _modelResource;
	private String _var;
}
