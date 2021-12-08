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

import ClayButton from '@clayui/button';
import {useModal} from '@clayui/modal';
import PropTypes from 'prop-types';
import React, {useCallback, useState} from 'react';

import ImportModal from './ImportModal';

function ImportSubmit({
	disabled,
	formDataQuerySelector,
	formImportURL,
	portletNamespace,
}) {
	const [visible, setVisible] = useState(false);
	const {observer, onClose} = useModal({
		onClose: () => setVisible(false),
	});
	const onButtonClick = useCallback(() => {
		setVisible(true);
	}, [setVisible]);

	return (
		<span className="mr-3">
			<ClayButton
				disabled={disabled}
				displayType="primary"
				id={`${portletNamespace}-import-submit`}
				onClick={onButtonClick}
				type="button"
			>
				{Liferay.Language.get('import')}
			</ClayButton>

			{visible && (
				<ImportModal
					closeModal={onClose}
					formDataQuerySelector={formDataQuerySelector}
					formSubmitURL={formImportURL}
					namespace={portletNamespace}
					observer={observer}
				/>
			)}
		</span>
	);
}

ImportSubmit.propTypes = {
	disabled: PropTypes.bool.isRequired,
	formDataQuerySelector: PropTypes.string.isRequired,
	formImportURL: PropTypes.string.isRequired,
	portletNamespace: PropTypes.string.isRequired,
};

export default ImportSubmit;
