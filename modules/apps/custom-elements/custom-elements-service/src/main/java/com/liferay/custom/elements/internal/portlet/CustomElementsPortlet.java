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

package com.liferay.custom.elements.internal.portlet;

import com.liferay.custom.elements.model.CustomElementsPortletDescriptor;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.Properties;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Iván Zaera Avellón
 */
public class CustomElementsPortlet extends MVCPortlet {

	public CustomElementsPortlet(
		CustomElementsPortletDescriptor customElementsPortletDescriptor) {

		_htmlElementName = customElementsPortletDescriptor.getHTMLElementName();

		String propertiesString =
			customElementsPortletDescriptor.getProperties();

		try {
			_properties.load(
				new ByteArrayInputStream(
					propertiesString.getBytes(StandardCharsets.UTF_8)));
		}
		catch (IOException ioException) {
			ReflectionUtil.throwException(ioException);
		}
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException {

		PrintWriter printWriter = renderResponse.getWriter();

		printWriter.print(StringPool.LESS_THAN);
		printWriter.print(_htmlElementName);

		for (Map.Entry<Object, Object> entry : _properties.entrySet()) {
			printWriter.print(StringPool.SPACE);
			printWriter.print(entry.getKey());
			printWriter.print("=\"");

			String value = (String)entry.getValue();

			printWriter.print(value.replaceAll(StringPool.QUOTE, "&quot;"));

			printWriter.print(StringPool.QUOTE);
		}

		printWriter.print("></");
		printWriter.print(_htmlElementName);
		printWriter.print(StringPool.GREATER_THAN);

		printWriter.flush();
	}

	private final String _htmlElementName;
	private final Properties _properties = new Properties();

}