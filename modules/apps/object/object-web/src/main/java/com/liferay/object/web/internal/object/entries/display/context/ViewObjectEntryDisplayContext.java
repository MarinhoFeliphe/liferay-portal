package com.liferay.object.web.internal.object.entries.display.context;

import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderer;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.NavigationItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.NavigationItemBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.NavigationItemList;
import com.liferay.item.selector.ItemSelector;
import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectLayout;
import com.liferay.object.model.ObjectLayoutTab;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectLayoutLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.ParamUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

public class ViewObjectEntryDisplayContext extends ObjectEntryDisplayContext{

	public ViewObjectEntryDisplayContext(
		DDMFormRenderer ddmFormRenderer,
		HttpServletRequest httpServletRequest,
		ItemSelector itemSelector,
		ListTypeEntryLocalService listTypeEntryLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryService objectEntryService,
		ObjectFieldLocalService objectFieldLocalService,
		ObjectLayoutLocalService objectLayoutLocalService,
		ObjectRelationshipLocalService objectRelationshipLocalService) {
		super(ddmFormRenderer, httpServletRequest, itemSelector,
			listTypeEntryLocalService, objectDefinitionLocalService,
			objectEntryService, objectFieldLocalService,
			objectLayoutLocalService,
			objectRelationshipLocalService);
	}

	@Override
	protected DDMForm getDDMForm(ObjectLayoutTab objectLayoutTab)
		throws PortalException {

		DDMForm ddmForm = new DDMForm();

		ddmForm.addAvailableLocale(objectRequestHelper.getLocale());

		ObjectDefinition objectDefinition = getObjectDefinition();

		List<ObjectField> objectFields =
			objectFieldLocalService.getObjectFields(
				objectDefinition.getObjectDefinitionId());

		if (objectLayoutTab == null) {
			for (ObjectField objectField : objectFields) {
				if (!isActive(objectField)) {
					continue;
				}

				ddmForm.addDDMFormField(
					getDDMFormField(objectField, true));
			}
		}
		else {
			addDDMFormFields(ddmForm, objectFields, objectLayoutTab, true);
		}

		ddmForm.setDefaultLocale(objectRequestHelper.getLocale());

		return ddmForm;
	}


	@Override
	public ObjectEntry getObjectEntry() throws PortalException {
		if (objectEntry != null) {
			return objectEntry;
		}

		HttpServletRequest httpServletRequest =
			objectRequestHelper.getRequest();

		long objectEntryId = ParamUtil.getLong(
			objectRequestHelper.getRequest(), "objectEntryId");
		
		if (objectEntryId == 0L) {
			objectEntryId = (long)httpServletRequest.getAttribute("objectEntryId");
		}

		objectEntry = objectEntryService.fetchObjectEntry(objectEntryId);

		return objectEntry;
	}

	public List<NavigationItem> getNavigationItems() throws PortalException {
		ObjectLayout objectLayout = getObjectLayout();

		if (objectLayout == null) {
			return Collections.emptyList();
		}

		NavigationItemList navigationItemList = new NavigationItemList();

		ObjectEntry objectEntry = getObjectEntry();

		if (objectEntry == null) {
			return navigationItemList;
		}

		ObjectLayoutTab currentObjectLayoutTab = getObjectLayoutTab();

		LiferayPortletResponse liferayPortletResponse =
			objectRequestHelper.getLiferayPortletResponse();

		HttpServletRequest httpServletRequest = objectRequestHelper.getRequest();

		for (ObjectLayoutTab objectLayoutTab :
			objectLayout.getObjectLayoutTabs()) {

			if (objectLayoutTab.getObjectRelationshipId() > 0) {
				ObjectRelationship objectRelationship =
					objectRelationshipLocalService.getObjectRelationship(
						objectLayoutTab.getObjectRelationshipId());

				ObjectDefinition objectDefinition =
					objectDefinitionLocalService.getObjectDefinition(
						objectRelationship.getObjectDefinitionId2());

				if (!objectDefinition.isActive()) {
					continue;
				}
			}

			navigationItemList.add(
				NavigationItemBuilder.setActive(
					objectLayoutTab.getObjectLayoutTabId() ==
					currentObjectLayoutTab.getObjectLayoutTabId()
				).setHref(
					PortletURLBuilder.create(
						liferayPortletResponse.createRenderURL()
					).setMVCPath(
						httpServletRequest.getParameter("mvcPath")
					).setMVCRenderCommandName(
						"/portal_workflow_task/edit_task"
					).setRedirect(
						httpServletRequest.getParameter("redirect")
					).setBackURL(
						httpServletRequest.getParameter("backURL")
					).setParameter(
						"objectEntryId", objectEntry.getObjectEntryId()
					).setParameter(
						"objectLayoutTabId",
						objectLayoutTab.getObjectLayoutTabId()
					).setParameter(
						"workflowTaskId",
						httpServletRequest.getParameter("workflowTaskId")
					).setParameter(
						"assetEntryId",
						httpServletRequest.getParameter("assetEntryId")
					).setParameter(
						"assetEntryClassPK",
						httpServletRequest.getParameter("assetEntryClassPK")
					).setParameter(
						"languageId",
						httpServletRequest.getParameter("languageId")
					).setParameter(
						"showExtraInfo",
						httpServletRequest.getParameter("showExtraInfo")
					).setParameter(
						"showHeader",
						httpServletRequest.getParameter("showHeader")
					).setParameter(
						"type",
						httpServletRequest.getParameter("type")
					).setParameter(
						"showComments",
						httpServletRequest.getParameter("showComments")
					).setParameter(
						"showEditURL",
						httpServletRequest.getParameter("showEditURL")
					).buildString()
				).setLabel(
					objectLayoutTab.getName(objectRequestHelper.getLocale())
				).build());
		}

		return navigationItemList;
	}
}
