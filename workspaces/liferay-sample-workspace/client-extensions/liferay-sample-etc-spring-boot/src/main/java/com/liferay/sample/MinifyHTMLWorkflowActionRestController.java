/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.sample;

import com.liferay.client.extension.util.spring.boot3.BaseRestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Feliphe Marinho
 * @author Mahmoud Tayem
 */
@RequestMapping("/workflow-action/minify-html")
@RestController
public class MinifyHTMLWorkflowActionRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
			@AuthenticationPrincipal Jwt jwt, @RequestBody String json)
		throws Exception {

		log(jwt, _log, json);

		JSONObject jsonObject = new JSONObject(json);

		String patchBody = "{\"context\":{\"minifiedHtml\":\"<!DOCTYPE html><html lang=\\\"en\\\"><head><title>Architect Test Page</title></head><body><nav class=\\\"site-header primary-nav\\\" role=\\\"navigation\\\"><div class=\\\"container\\\"><a href=\\\"/\\\">Logo</a><ul><li><a href=\\\"/home\\\">Home</a></li><li><a href=\\\"/about\\\">About</a></li></ul></div></nav><main><section class=\\\"hero-slider swiper-container\\\" id=\\\"main-carousel\\\"><div class=\\\"swiper-wrapper\\\"><div class=\\\"swiper-slide\\\">Slide 1</div><div class=\\\"swiper-slide\\\">Slide 2</div></div><button class=\\\"next-ctrl\\\">Next</button><button class=\\\"prev-ctrl\\\">Prev</button></section><section class=\\\"product-showcase\\\"><div class=\\\"grid-container\\\"><div class=\\\"product-card item-wrapper\\\"><h3>Product A</h3><p>$10</p></div><div class=\\\"product-card item-wrapper\\\"><h3>Product B</h3><p>$20</p></div></div></section></main><footer class=\\\"site-footer\\\"><p>&copy; 2026 Architect Corp</p></footer></body></html>\"}}";

		patch(
			"Bearer " + jwt.getTokenValue(),
			patchBody,
			UriComponentsBuilder.fromUriString(
					"/o/headless-admin-workflow/v1.0/workflow-instances/" + jsonObject.getJSONObject("kaleoInstanceToken").getLong("kaleoInstanceId")
			).build(
			).toUri());

		post(
			"Bearer " + jwt.getTokenValue(),
			"{\"transitionName\": \"analyze\"}",
			UriComponentsBuilder.fromUriString(
				jsonObject.getString("transitionURL")
			).build(
			).toUri());

		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	private static final Log _log = LogFactory.getLog(
		MinifyHTMLWorkflowActionRestController.class);

}