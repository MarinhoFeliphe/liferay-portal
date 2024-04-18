/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.formats.Json;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Feliphe Marinho
 */
@RequestMapping("/notification-service/telegram-bot")
@RestController
public class TelegramNotificationTypeRestController extends BaseRestController {

	@PostMapping("/send-message")
	public ResponseEntity<String> post(
			@AuthenticationPrincipal Jwt jwt, @RequestBody String json)
		throws JsonProcessingException {

		log(jwt, _log, json);

		String url = _baseURL + _botAPIToken + "/sendMessage?chat_id=" + _chatId + "&text=" + _getText(json, jwt);

		new RestTemplate().exchange(url, HttpMethod.GET, null, String.class);

		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	private String _getText(String json, Jwt jwt) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode jsonNode = objectMapper.readTree(json);

		JsonNode notificationTemplateJsonNode = jsonNode.get("notificationTemplate");

		JsonNode bodyCurrentValueJsonNode = notificationTemplateJsonNode.get("bodyCurrentValue");

		JsonNode termsValueJsonNode = jsonNode.get("termValues");

		JsonNode orderStatusJsonNode = termsValueJsonNode.get("orderStatus");

		String bodyCurrentValueJsonNodeString = bodyCurrentValueJsonNode.asText();

		return bodyCurrentValueJsonNodeString.replaceAll(
					"\\[%COMMERCEORDER_ORDERSTATUS%]",
					_statusMap.get(orderStatusJsonNode.asInt())
		).replaceAll(
				"\\[%COMMERCEORDER_AUTHOR_FIRST_NAME%]",
				_getUserName(termsValueJsonNode.get("creator").asLong(), jwt.getTokenValue())
		);
	}

	private String _getUserName(long userId, String token) throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		ResponseEntity<String> response = new RestTemplate().exchange(
				"http://localhost:8080/o/headless-admin-user/v1.0/user-accounts?filter=id eq '" + userId + "'",
				HttpMethod.GET, new HttpEntity<>(headers), String.class);

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode jsonNode = objectMapper.readTree(response.getBody());

		return jsonNode.get("items").get(0).get("name").asText();
	}

	private String _getCommerceOrderAuthorName(String commerceOrderId, String token) throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		ResponseEntity<String> response = new RestTemplate().exchange(
				"http://localhost:8080/o/headless-commerce-admin-order/v1.0/orders/" + commerceOrderId,
				HttpMethod.GET, new HttpEntity<>(headers), String.class);

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode jsonNode = objectMapper.readTree(response.getBody());

		return "Test Test";
	}

	@Value("${telegram.bot.api.token}")
	private String _botAPIToken;

	@Value("${telegram.chat.id}")
	private String _chatId;

	private static final String _baseURL = "https://api.telegram.org/bot";

	private static final Log _log = LogFactory.getLog(
		TelegramNotificationTypeRestController.class);

	private static final Map<Integer, String> _statusMap = new HashMap<Integer, String>() {
		{
			put(0, "Completado");
			put(1, "Pendente");
			put(10, "Processando");
			put(14, "Parcialmente Entregue");
		}
	};
}