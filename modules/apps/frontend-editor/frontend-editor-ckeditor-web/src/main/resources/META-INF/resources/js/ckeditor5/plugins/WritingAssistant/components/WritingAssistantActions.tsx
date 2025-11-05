/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayDropDown from '@clayui/drop-down';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import React, {useEffect, useRef, useState} from 'react';

import {Action} from '../types';

export default function WritingAssistantActions({
	containerRef,
	handleActionClick,
}: {
	containerRef: HTMLElement;
	handleActionClick: (type: Action['type']) => Promise<void>;
}) {
	const [active, setActive] = useState(true);
	const [isLoading, setIsLoading] = useState<{type: Action['type'] | ''}>({
		type: '',
	});

	const actionsGroup = [
		{
			children: [
				{
					disabled: false,
					name: Liferay.Language.get('improve-writing'),
					symbolLeft: 'magic',
					type: 'Improve Writing',
				},
				{
					disabled: false,
					name: Liferay.Language.get('fix-spelling-and-grammar'),
					symbolLeft: 'check',
					type: 'Fix Spelling and Grammar',
				},
				{
					disabled: true,
					name: Liferay.Language.get('translate-to'),
					symbolLeft: 'automatic-translate',
					symbolRight: 'angle-right-small',
					type: 'Translate To',
				},
			],
			name: Liferay.Language.get('suggested'),
		},
		{type: 'divider'},
		{
			children: [
				{
					disabled: false,
					name: Liferay.Language.get('make-shorter'),
					symbolLeft: 'bars',
					type: 'Make Shorter',
				},
				{
					disabled: true,
					name: Liferay.Language.get('make-longer'),
					symbolLeft: 'align-justify',
					type: 'Make Shorter',
				},
				{
					disabled: true,
					name: Liferay.Language.get('change-tone'),
					symbolRight: 'angle-right-small',
					type: 'Change Tone',
				},
			],
			name: Liferay.Language.get('edit'),
		},
		{type: 'divider'},
		{
			children: [
				{
					disabled: true,
					name: 'Title',
					type: 'Generate Based On Title',
				},
			],
			name: Liferay.Language.get('generate-based-on'),
		},
	];

	const alignRef = useRef<HTMLElement | null>(null);
	const menuElementRef = useRef<HTMLDivElement | null>(null);

	useEffect(() => {
		alignRef.current = containerRef ?? null;
	}, [containerRef]);

	return (
		<ClayDropDown.Menu
			active={active}
			alignElementRef={alignRef}
			alignmentByViewport
			onActiveChange={() => {
				setActive(!active);
			}}
			ref={menuElementRef}
		>
			<ClayDropDown.ItemList items={actionsGroup}>
				{(group: any) => (
					<ClayDropDown.Group<Action>
						header={group.name}
						items={group.children}
						key={group.name}
					>
						{(child: Action) => (
							<ClayDropDown.Item
								disabled={child.disabled}
								key={child.name}
								onClick={() => {
									handleActionClick(child.type);
									setIsLoading({type: child.type});
								}}
								spritemap={
									Liferay.ThemeDisplay.getPathThemeImages() +
									'/clay/icons.svg'
								}
								style={{
									opacity:
										isLoading.type === child.type ? 0.5 : 1,
								}}
								symbolLeft={child.symbolLeft}
								symbolRight={child.symbolRight}
							>
								<div className="align-items-center d-flex">
									<span className="ml-4">{child.name}</span>

									{isLoading.type === child.type && (
										<ClayLoadingIndicator className="mb-0 mt-0" />
									)}
								</div>
							</ClayDropDown.Item>
						)}
					</ClayDropDown.Group>
				)}
			</ClayDropDown.ItemList>
		</ClayDropDown.Menu>
	);
}
