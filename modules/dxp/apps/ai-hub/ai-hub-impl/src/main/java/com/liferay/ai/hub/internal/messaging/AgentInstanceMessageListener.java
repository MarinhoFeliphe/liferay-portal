/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.messaging;

import com.liferay.ai.hub.internal.audit.AuditRouterUtil;
import com.liferay.ai.hub.internal.constants.AIHubDestinationNames;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.DestinationConfiguration;
import com.liferay.portal.kernel.messaging.DestinationFactory;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowInstance;

import java.io.Serializable;

import java.util.Date;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Feliphe Marinho
 */
@Component(
	property = "destination.name=" + AIHubDestinationNames.AI_HUB_AGENT_INSTANCE,
	service = MessageListener.class
)
public class AgentInstanceMessageListener extends BaseMessageListener {

	@Activate
	protected void activate(BundleContext bundleContext) {
		Destination destination = _destinationFactory.createDestination(
			new DestinationConfiguration(
				DestinationConfiguration.DESTINATION_TYPE_PARALLEL,
				AIHubDestinationNames.AI_HUB_AGENT_INSTANCE));

		_destinationServiceRegistration = bundleContext.registerService(
			Destination.class, destination,
			MapUtil.singletonDictionary(
				"destination.name", destination.getName()));
	}

	@Deactivate
	protected void deactivate() {
		_destinationServiceRegistration.unregister();
	}

	@Override
	protected void doReceive(Message message) throws Exception {
		JSONObject jsonObject = _jsonFactory.createJSONObject(
		).put(
			"workflowInstanceId", message.getLong("workflowInstanceId")
		);

		Map<String, Serializable> metadata =
			(Map<String, Serializable>)message.get("metadata");

		if (metadata != null) {
			for (Map.Entry<String, Serializable> entry : metadata.entrySet()) {
				jsonObject.put(entry.getKey(), entry.getValue());
			}
		}

		AuditRouterUtil.route(
			WorkflowInstance.class.getName(),
			message.getLong("workflowInstanceId"),
			message.getString("eventType"), jsonObject,
			(Date)message.get("timestamp"), message.getLong("userId"));
	}

	@Reference
	private DestinationFactory _destinationFactory;

	private ServiceRegistration<Destination> _destinationServiceRegistration;

	@Reference
	private JSONFactory _jsonFactory;

}