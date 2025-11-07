/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import PropTypes from 'prop-types';
import React from 'react';

import SidebarPanel from '../../SidebarPanel';

const PromptSummary = ({setContentName}) => {
	return (
		<SidebarPanel panelTitle={Liferay.Language.get('prompt')}>
			<CurrentActions
				actions={selectedItem.data?.actions}
				setContentName={setContentName}
			/>
		</SidebarPanel>
	);
};

PromptSummary.propTypes = {
	setContentName: PropTypes.func.isRequired,
};

export default PromptSummary;
