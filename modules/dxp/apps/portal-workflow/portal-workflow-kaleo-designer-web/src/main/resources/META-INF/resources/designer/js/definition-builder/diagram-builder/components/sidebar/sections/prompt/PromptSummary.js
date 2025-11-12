/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayInput} from '@clayui/form';
import PropTypes from 'prop-types';
import React, {useContext} from 'react';

import {DiagramBuilderContext} from '../../../../DiagramBuilderContext';
import SidebarPanel from '../../SidebarPanel';

const PromptSummary = () => {
	const {selectedItem, setSelectedItem} = useContext(DiagramBuilderContext);

	return (
		<SidebarPanel panelTitle={Liferay.Language.get('prompt')}>
			<ClayInput
				component="textarea"
				onChange={({target}) =>
					setSelectedItem({
						...selectedItem,
						data: {
							...selectedItem.data,
							prompt: target.value,
						},
					})
				}
				type="text"
				value={selectedItem?.data.prompt ?? ''}
			/>
		</SidebarPanel>
	);
};

PromptSummary.propTypes = {
	setContentName: PropTypes.func.isRequired,
};

export default PromptSummary;
