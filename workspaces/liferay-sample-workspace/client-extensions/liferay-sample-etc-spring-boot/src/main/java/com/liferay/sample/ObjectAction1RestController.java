/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Raymond Augé
 * @author Gregory Amerson
 * @author Brian Wing Shun Chan
 */
@RequestMapping("/object/action/1")
@RestController
public class ObjectAction1RestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
		@AuthenticationPrincipal Jwt jwt, @RequestBody String json) throws Exception {

		log(jwt, _log, json);

		// get book id from the payload

		JsonNode jsonNode = new ObjectMapper().readTree(json);

		jsonNode = jsonNode.get("objectEntryDTOBook");
		jsonNode = jsonNode.get("id");

		// perform a GET request to obtain the authors of that book,
		// remember to set the oauth2 scope for book object definition

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(jwt.getTokenValue());

		ResponseEntity<String> response = new RestTemplate().exchange(
				lxcDXPServerProtocol + "://" + lxcDXPMainDomain +
						"/o/c/books/" + jsonNode.asLong() + "/bookAuthor", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);

		jsonNode = new ObjectMapper().readTree(response.getBody());
		jsonNode.get("items");

		// iterate over the items that will contain the authors of that book.

		// send a notification different from user-notification

		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	private static final Log _log = LogFactory.getLog(
		ObjectAction1RestController.class);

}