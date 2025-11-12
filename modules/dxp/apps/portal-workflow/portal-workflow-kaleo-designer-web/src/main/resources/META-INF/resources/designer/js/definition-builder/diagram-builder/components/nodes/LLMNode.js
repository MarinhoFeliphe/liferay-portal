/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import PropTypes from 'prop-types';
import React from 'react';

import {defaultLanguageId} from '../../../constants';
import BaseNode from './BaseNode';

export default function LLMNode({
	data: {description, label, newNode, prompt} = {},
	descriptionSidebar,
	id,
	...otherProps
}) {
	if (!label || !label[defaultLanguageId]) {
		label = {
			[defaultLanguageId]: Liferay.Language.get('llm-node'),
		};
	}

	return (
		<BaseNode
			description={description}
			descriptionSidebar={descriptionSidebar}
			icon="stars"
			id={id}
			label={label}
			newNode={newNode}
			nodeTypeClassName="llm-node"
			prompt={prompt}
			type="llm"
			{...otherProps}
		/>
	);
}

LLMNode.propTypes = {
	data: PropTypes.object,
	descriptionSidebar: PropTypes.string,
	id: PropTypes.string,
};
