/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.definition.directed.acyclic.graph.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.frontend.taglib.servlet.taglib.ScreenNavigationCategory;
import com.liferay.frontend.taglib.servlet.taglib.ScreenNavigationRegistryUtil;
import com.liferay.object.constants.ObjectActionExecutorConstants;
import com.liferay.object.constants.ObjectActionTriggerConstants;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.definition.directed.acyclic.graph.ObjectDefinitionDirectedAcyclicGraph;
import com.liferay.object.exception.ObjectRelationshipEdgeException;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.related.models.test.util.ObjectEntryTestUtil;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.object.test.util.ObjectRelationshipTestUtil;
import com.liferay.object.test.util.TreeTestUtil;
import com.liferay.object.tree.Node;
import com.liferay.object.tree.ObjectDefinitionTreeFactory;
import com.liferay.object.tree.Tree;
import com.liferay.petra.function.UnsafeBiConsumer;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.WorkflowDefinitionLink;
import com.liferay.portal.kernel.security.permission.ResourceActions;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.CoreMatchers;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Feliphe Marinho
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@FeatureFlag("LPD-34594")
@RunWith(Arquillian.class)
public class ObjectDefinitionDirectedAcyclicGraphTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_objectDefinitionTreeFactory = new ObjectDefinitionTreeFactory(
			_objectDefinitionLocalService, _objectRelationshipLocalService);
	}

	@After
	public void tearDown() {
		ObjectDefinitionDirectedAcyclicGraph.invalidate();
	}

	@Test
	public void testAddEdgeBetweenDraftNodes() throws Exception {

		// Bind a draft object definition as a child node in a draft object
		// definition tree

		TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			false,
			LinkedHashMapBuilder.put(
				"AA", new String[] {"AAA"}
			).put(
				"AAA", new String[0]
			).build());

		ObjectDefinition objectDefinitionAAA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAA");

		_testAddEdge(
			objectDefinitionAAA,
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AAAA"),
			(objectDefinition1, objectDefinition2) ->
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AA", new String[] {"AAA"}
					).put(
						"AAA", new String[] {"AAAA"}
					).put(
						"AAAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getRootObjectDefinitionId()),
					_objectDefinitionLocalService));

		// Bind a draft object definition as a parent node in a draft object
		// definition tree

		ObjectDefinition objectDefinitionAA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AA");

		_testAddEdge(
			ObjectDefinitionTestUtil.addCustomObjectDefinition("A"),
			objectDefinitionAA,
			(objectDefinition1, objectDefinition2) ->
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[] {"AA"}
					).put(
						"AA", new String[] {"AAA"}
					).put(
						"AAA", new String[] {"AAAA"}
					).put(
						"AAAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getObjectDefinitionId()),
					_objectDefinitionLocalService));

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind two draft object definition trees

		ObjectDefinition objectDefinitionA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("A");
		objectDefinitionAA = ObjectDefinitionTestUtil.addCustomObjectDefinition(
			"AA");

		_addEdge(
			objectDefinitionA.getObjectDefinitionId(),
			objectDefinitionAA.getObjectDefinitionId());

		objectDefinitionAAA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AAA");
		ObjectDefinition objectDefinitionAAAA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AAAA");

		_addEdge(
			objectDefinitionAAA.getObjectDefinitionId(),
			objectDefinitionAAAA.getObjectDefinitionId());

		_testAddEdge(
			objectDefinitionAA, objectDefinitionAAA,
			(objectDefinition1, objectDefinition2) ->
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[] {"AA"}
					).put(
						"AA", new String[] {"AAA"}
					).put(
						"AAA", new String[] {"AAAA"}
					).put(
						"AAAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getRootObjectDefinitionId()),
					_objectDefinitionLocalService));

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind two draft object definitions

		_testAddEdge(
			ObjectDefinitionTestUtil.addCustomObjectDefinition("A"),
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AA"),
			(objectDefinition1, objectDefinition2) ->
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[] {"AA"}
					).put(
						"AA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getObjectDefinitionId()),
					_objectDefinitionLocalService));

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService, new String[] {"C_A", "C_AA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind two nonroot draft object definitions from different object
		// definition trees

		Tree treeA = TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			false,
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[0]
			).build());
		Tree treeB = TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			false,
			LinkedHashMapBuilder.put(
				"B", new String[] {"BB"}
			).put(
				"BB", new String[] {"BBB"}
			).put(
				"BBB", new String[0]
			).build());

		TreeTestUtil.bind(
			_objectRelationshipLocalService,
			List.of(
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService,
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_AA"),
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_BB"))));

		Node rootNodeA = treeA.getRootNode();

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[] {"BB"}
			).put(
				"BB", new String[] {"BBB"}
			).put(
				"BBB", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(rootNodeA.getPrimaryKey()),
			_objectDefinitionLocalService);

		Node rootNodeB = treeB.getRootNode();

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"B", new String[] {"BB"}
			).put(
				"BB", new String[] {"BBB"}
			).put(
				"BBB", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(rootNodeB.getPrimaryKey()),
			_objectDefinitionLocalService);
	}

	@Test
	public void testAddEdgeBetweenNodesWithGreaterThanTreeMaxHeight()
		throws Exception {

		// Bind an object definition to a tree that has reached the maximum
		// height

		TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			false,
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[] {"AAA"}
			).put(
				"AAA", new String[] {"AAAA"}
			).put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAAA", new String[0]
			).build());

		ObjectDefinition objectDefinitionAAAAA =
			_objectDefinitionLocalService.fetchObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAAAA");
		ObjectDefinition objectDefinitionAAAAAA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AAAAAA");

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			"The object relationship cannot be an edge in the root context " +
				"because it would exceed the tree's maximum height",
			() -> _bindObjectDefinitions(
				objectDefinitionAAAAA.getObjectDefinitionId(),
				objectDefinitionAAAAAA.getObjectDefinitionId()));

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA", "C_AAAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		_objectDefinitionLocalService.deleteObjectDefinition(
			objectDefinitionAAAAAA);

		// Bind two object definition trees into one so that the height
		// of the new tree exceeds the maximum height

		TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			false,
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[] {"AAA"}
			).put(
				"AAA", new String[0]
			).build());

		TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			false,
			LinkedHashMapBuilder.put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAAA", new String[] {"AAAAAA"}
			).put(
				"AAAAAA", new String[0]
			).build());

		ObjectDefinition objectDefinitionAAA =
			_objectDefinitionLocalService.fetchObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAA");
		ObjectDefinition objectDefinitionAAAA =
			_objectDefinitionLocalService.fetchObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAAA");

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			"The object relationship cannot be an edge in the root context " +
				"because it would exceed the tree's maximum height",
			() -> _bindObjectDefinitions(
				objectDefinitionAAA.getObjectDefinitionId(),
				objectDefinitionAAAA.getObjectDefinitionId()));

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {
				"C_A", "C_AA", "C_AAA", "C_AAAA", "C_AAAAA", "C_AAAAAA"
			},
			_objectEntryLocalService, _objectRelationshipLocalService);
	}

	@Test
	public void testAddEdgeBetweenNodesWithObjectEntries() throws Exception {
		ObjectDefinition objectDefinitionAA =
			_addAndPublishCustomObjectDefinition("AA");
		ObjectDefinition objectDefinitionAAA =
			_addAndPublishCustomObjectDefinition("AAA");

		ObjectRelationship objectRelationship1 =
			_objectRelationshipLocalService.addObjectRelationship(
				StringUtil.randomId(), TestPropsValues.getUserId(),
				objectDefinitionAA.getObjectDefinitionId(),
				objectDefinitionAAA.getObjectDefinitionId(), 0,
				ObjectRelationshipConstants.DELETION_TYPE_PREVENT, false,
				LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
				StringUtil.randomId(), false,
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY, null);

		ObjectEntry objectDefinitionAAObjectEntry1 =
			ObjectEntryTestUtil.addObjectEntry(
				0, objectDefinitionAA.getObjectDefinitionId(),
				Collections.emptyMap());
		ObjectEntry objectDefinitionAAAObjectEntry1 =
			ObjectEntryTestUtil.addObjectEntry(
				0, objectDefinitionAAA.getObjectDefinitionId(),
				Collections.emptyMap());

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			StringBundler.concat(
				"There must be no unrelated object entries when both object ",
				"definitions are published so that the object relationship ",
				"can be an edge to a root context"),
			() -> TreeTestUtil.bind(
				_objectRelationshipLocalService, List.of(objectRelationship1)));

		ObjectField objectField1 = _objectFieldLocalService.getObjectField(
			objectRelationship1.getObjectFieldId2());

		objectDefinitionAAAObjectEntry1 =
			_objectEntryLocalService.updateObjectEntry(
				objectDefinitionAAAObjectEntry1.getUserId(),
				objectDefinitionAAAObjectEntry1.getObjectEntryId(),
				Collections.singletonMap(
					objectField1.getName(),
					objectDefinitionAAObjectEntry1.getObjectEntryId()),
				ServiceContextTestUtil.getServiceContext());

		Assert.assertEquals(
			0, objectDefinitionAAObjectEntry1.getRootObjectEntryId());
		Assert.assertEquals(
			0, objectDefinitionAAAObjectEntry1.getRootObjectEntryId());

		TreeTestUtil.bind(
			_objectRelationshipLocalService, List.of(objectRelationship1));

		objectDefinitionAAObjectEntry1 =
			_objectEntryLocalService.getObjectEntry(
				objectDefinitionAAObjectEntry1.getObjectEntryId());
		objectDefinitionAAAObjectEntry1 =
			_objectEntryLocalService.getObjectEntry(
				objectDefinitionAAAObjectEntry1.getObjectEntryId());

		long expectedRootObjectEntryId =
			objectDefinitionAAObjectEntry1.getRootObjectEntryId();

		Assert.assertEquals(
			expectedRootObjectEntryId,
			objectDefinitionAAObjectEntry1.getRootObjectEntryId());
		Assert.assertEquals(
			expectedRootObjectEntryId,
			objectDefinitionAAAObjectEntry1.getRootObjectEntryId());

		ObjectDefinition objectDefinitionA =
			_addAndPublishCustomObjectDefinition("A");

		ObjectRelationship objectRelationship2 =
			_objectRelationshipLocalService.addObjectRelationship(
				StringUtil.randomId(), TestPropsValues.getUserId(),
				objectDefinitionA.getObjectDefinitionId(),
				objectDefinitionAA.getObjectDefinitionId(), 0,
				ObjectRelationshipConstants.DELETION_TYPE_PREVENT, false,
				LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
				StringUtil.randomId(), false,
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY, null);

		ObjectField objectField2 = _objectFieldLocalService.getObjectField(
			objectRelationship2.getObjectFieldId2());

		ObjectEntry objectDefinitionAObjectEntry1 =
			ObjectEntryTestUtil.addObjectEntry(
				0, objectDefinitionA.getObjectDefinitionId(),
				Collections.emptyMap());

		_objectEntryLocalService.updateObjectEntry(
			objectDefinitionAAObjectEntry1.getUserId(),
			objectDefinitionAAObjectEntry1.getObjectEntryId(),
			Collections.singletonMap(
				objectField2.getName(),
				objectDefinitionAObjectEntry1.getObjectEntryId()),
			ServiceContextTestUtil.getServiceContext());

		Assert.assertEquals(
			0, objectDefinitionAObjectEntry1.getRootObjectEntryId());

		TreeTestUtil.bind(
			_objectRelationshipLocalService, List.of(objectRelationship2));

		objectDefinitionAObjectEntry1 = _objectEntryLocalService.getObjectEntry(
			objectDefinitionAObjectEntry1.getObjectEntryId());
		objectDefinitionAAObjectEntry1 =
			_objectEntryLocalService.getObjectEntry(
				objectDefinitionAAObjectEntry1.getObjectEntryId());
		objectDefinitionAAAObjectEntry1 =
			_objectEntryLocalService.getObjectEntry(
				objectDefinitionAAAObjectEntry1.getObjectEntryId());

		expectedRootObjectEntryId =
			objectDefinitionAObjectEntry1.getRootObjectEntryId();

		Assert.assertEquals(
			expectedRootObjectEntryId,
			objectDefinitionAObjectEntry1.getRootObjectEntryId());
		Assert.assertEquals(
			expectedRootObjectEntryId,
			objectDefinitionAAObjectEntry1.getRootObjectEntryId());
		Assert.assertEquals(
			expectedRootObjectEntryId,
			objectDefinitionAAAObjectEntry1.getRootObjectEntryId());

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {
				objectDefinitionA.getName(), objectDefinitionAA.getName(),
				objectDefinitionAAA.getName()
			},
			_objectEntryLocalService, _objectRelationshipLocalService);
	}

	@Test
	public void testAddEdgeBetweenNodesWithOngoingWorkflowInstances()
		throws Exception {

		ObjectDefinition objectDefinitionA =
			_addAndPublishCustomObjectDefinition();
		ObjectDefinition objectDefinitionAA =
			_addAndPublishCustomObjectDefinition();

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.addObjectRelationship(
				StringUtil.randomId(), TestPropsValues.getUserId(),
				objectDefinitionA.getObjectDefinitionId(),
				objectDefinitionAA.getObjectDefinitionId(), 0,
				ObjectRelationshipConstants.DELETION_TYPE_PREVENT, false,
				LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
				StringUtil.randomId(), false,
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY, null);

		_workflowDefinitionLinkLocalService.updateWorkflowDefinitionLink(
			TestPropsValues.getUserId(), TestPropsValues.getCompanyId(), 0,
			objectDefinitionA.getClassName(), 0, 0, "Single Approver", 1);

		ObjectEntry objectEntryA1 = ObjectEntryTestUtil.addObjectEntry(
			0, objectDefinitionA.getObjectDefinitionId(),
			Collections.emptyMap());
		ObjectEntry objectEntryA2 = ObjectEntryTestUtil.addObjectEntry(
			0, objectDefinitionA.getObjectDefinitionId(),
			Collections.emptyMap());

		_workflowDefinitionLinkLocalService.updateWorkflowDefinitionLink(
			TestPropsValues.getUserId(), TestPropsValues.getCompanyId(), 0,
			objectDefinitionAA.getClassName(), 0, 0, "Single Approver", 1);

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		ObjectEntry objectEntryAA = ObjectEntryTestUtil.addObjectEntry(
			0, objectDefinitionAA.getObjectDefinitionId(),
			Collections.singletonMap(
				objectField.getName(), objectEntryA1.getObjectEntryId()));

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			String.format(
				"These ongoing workflow instances must be completed to " +
					"enable inheritance: \"%s\" (\"%s\" object entries) and " +
						"\"%s\" (\"%s\" object entries)",
				objectDefinitionA.getLabel(LocaleUtil.US), 2,
				objectDefinitionAA.getLabel(LocaleUtil.US), 1),
			() -> TreeTestUtil.bind(
				_objectRelationshipLocalService, List.of(objectRelationship)));

		_completeWorkflowTask(
			objectDefinitionA.getClassName(), objectEntryA1.getObjectEntryId());

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			String.format(
				"These ongoing workflow instances must be completed to " +
					"enable inheritance: \"%s\" (\"%s\" object entries) and " +
						"\"%s\" (\"%s\" object entries)",
				objectDefinitionA.getLabel(LocaleUtil.US), 1,
				objectDefinitionAA.getLabel(LocaleUtil.US), 1),
			() -> TreeTestUtil.bind(
				_objectRelationshipLocalService, List.of(objectRelationship)));

		_completeWorkflowTask(
			objectDefinitionA.getClassName(), objectEntryA2.getObjectEntryId());

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			String.format(
				"These ongoing workflow instances must be completed to " +
					"enable inheritance: \"%s\" (\"%s\" object entries)",
				objectDefinitionAA.getLabel(LocaleUtil.US), 1),
			() -> TreeTestUtil.bind(
				_objectRelationshipLocalService, List.of(objectRelationship)));

		_completeWorkflowTask(
			objectDefinitionAA.getClassName(),
			objectEntryAA.getObjectEntryId());

		TreeTestUtil.bind(
			_objectRelationshipLocalService, List.of(objectRelationship));

		objectEntryAA = _objectEntryLocalService.getObjectEntry(
			objectEntryAA.getObjectEntryId());

		Assert.assertEquals(
			objectEntryA1.getObjectEntryId(),
			objectEntryAA.getRootObjectEntryId());

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {
				objectDefinitionA.getName(), objectDefinitionAA.getName()
			},
			_objectEntryLocalService, _objectRelationshipLocalService);
	}

	@Test
	public void testAddEdgeBetweenPublishedNodes() throws Exception {

		// Bind a published object definition as a child node in a published
		// object definition tree

		ObjectDefinition objectDefinitionAA =
			_addAndPublishCustomObjectDefinition("AA");
		ObjectDefinition objectDefinitionAAA =
			_addAndPublishCustomObjectDefinition("AAA");

		_addEdge(
			objectDefinitionAA.getObjectDefinitionId(),
			objectDefinitionAAA.getObjectDefinitionId());

		ObjectDefinition objectDefinitionAAAA =
			_addAndPublishCustomObjectDefinition("AAAA");

		_testAddEdge(
			objectDefinitionAAA, objectDefinitionAAAA,
			(objectDefinition1, objectDefinition2) ->
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AA", new String[] {"AAA"}
					).put(
						"AAA", new String[] {"AAAA"}
					).put(
						"AAAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getRootObjectDefinitionId()),
					_objectDefinitionLocalService));

		_asserScreenNavigationCategories(2, "C_AA");
		_asserScreenNavigationCategories(2, "C_AAA");
		_asserScreenNavigationCategories(1, "C_AAAA");

		// Bind a published object definition as a parent node in a published
		// object definition tree

		_testAddEdge(
			_addAndPublishCustomObjectDefinition("A"), objectDefinitionAA,
			(objectDefinition1, objectDefinition2) ->
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[] {"AA"}
					).put(
						"AA", new String[] {"AAA"}
					).put(
						"AAA", new String[] {"AAAA"}
					).put(
						"AAAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getObjectDefinitionId()),
					_objectDefinitionLocalService));

		_asserScreenNavigationCategories(2, "C_A");
		_asserScreenNavigationCategories(2, "C_AA");
		_asserScreenNavigationCategories(2, "C_AAA");
		_asserScreenNavigationCategories(1, "C_AAAA");

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind two published object definition trees

		ObjectDefinition objectDefinitionA =
			_addAndPublishCustomObjectDefinition("A");
		objectDefinitionAA = _addAndPublishCustomObjectDefinition("AA");

		_addEdge(
			objectDefinitionA.getObjectDefinitionId(),
			objectDefinitionAA.getObjectDefinitionId());

		objectDefinitionAAA = _addAndPublishCustomObjectDefinition("AAA");
		objectDefinitionAAAA = _addAndPublishCustomObjectDefinition("AAAA");

		_addEdge(
			objectDefinitionAAA.getObjectDefinitionId(),
			objectDefinitionAAAA.getObjectDefinitionId());

		_testAddEdge(
			objectDefinitionAA, objectDefinitionAAA,
			(objectDefinition1, objectDefinition2) ->
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[] {"AA"}
					).put(
						"AA", new String[] {"AAA"}
					).put(
						"AAA", new String[] {"AAAA"}
					).put(
						"AAAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getRootObjectDefinitionId()),
					_objectDefinitionLocalService));

		_asserScreenNavigationCategories(2, "C_A");
		_asserScreenNavigationCategories(2, "C_AA");
		_asserScreenNavigationCategories(2, "C_AAA");
		_asserScreenNavigationCategories(1, "C_AAAA");

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind two published object definitions

		ObjectDefinition objectDefinitionB =
			_addAndPublishCustomObjectDefinition("B");
		ObjectDefinition objectDefinitionBB =
			_addAndPublishCustomObjectDefinition("BB");

		_testAddEdge(
			objectDefinitionB, objectDefinitionBB,
			(objectDefinition1, objectDefinition2) ->
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"B", new String[] {"BB"}
					).put(
						"BB", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getObjectDefinitionId()),
					_objectDefinitionLocalService));

		_asserScreenNavigationCategories(2, "C_B");
		_asserScreenNavigationCategories(1, "C_BB");

		// Object definitions must have the same scope to enable inheritance

		ObjectDefinition siteObjectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition(
				Collections.singletonList(
					new TextObjectFieldBuilder(
					).labelMap(
						LocalizedMapUtil.getLocalizedMap(
							RandomTestUtil.randomString())
					).name(
						"a" + RandomTestUtil.randomString()
					).build()),
				ObjectDefinitionConstants.SCOPE_SITE);

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			String.format(
				"The scope of \"%s\" is not the same as \"%s\". To enable " +
					"inheritance, the object definitions must have the same " +
						"scope",
				objectDefinitionB.getShortName(),
				siteObjectDefinition.getShortName()),
			() -> _addEdge(
				objectDefinitionB.getObjectDefinitionId(),
				siteObjectDefinition.getObjectDefinitionId()));

		// Unable to bind the object definitions because the object relationship
		// must not create a circular reference in a root context

		ObjectDefinition objectDefinitionBBB =
			_addAndPublishCustomObjectDefinition("BBB");

		_addEdge(
			objectDefinitionBB.getObjectDefinitionId(),
			objectDefinitionBBB.getObjectDefinitionId());

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			"The object relationship must not create a circular reference in " +
				"a root context",
			() -> _addEdge(
				objectDefinitionBBB.getObjectDefinitionId(),
				objectDefinitionB.getObjectDefinitionId()));

		ObjectDefinition objectDefinitionC =
			_addAndPublishCustomObjectDefinition("C");
		ObjectDefinition objectDefinitionCC =
			_addAndPublishCustomObjectDefinition("CC");

		_addEdge(
			objectDefinitionC.getObjectDefinitionId(),
			objectDefinitionCC.getObjectDefinitionId());

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_B", "C_BB", "C_BBB"}, _objectEntryLocalService,
			_objectRelationshipLocalService);
		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService, new String[] {"C_C", "C_CC"},
			_objectEntryLocalService, _objectRelationshipLocalService);
	}

	@Test
	public void testAddEdgeFromDraftNodeToPublishedNode() throws Exception {

		// Bind a draft object definition as a child node in a published object
		// definition tree

		ObjectDefinition objectDefinitionAA =
			_addAndPublishCustomObjectDefinition("AA");
		ObjectDefinition objectDefinitionAAA =
			_addAndPublishCustomObjectDefinition("AAA");

		_addEdge(
			objectDefinitionAA.getObjectDefinitionId(),
			objectDefinitionAAA.getObjectDefinitionId());

		_testAddEdge(
			objectDefinitionAAA,
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AAAA"),
			(objectDefinition1, objectDefinition2) -> {
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AA", new String[] {"AAA"}
					).put(
						"AAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getRootObjectDefinitionId()),
					_objectDefinitionLocalService);
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AAAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition2.getObjectDefinitionId()),
					_objectDefinitionLocalService);
			});

		_asserScreenNavigationCategories(2, "C_AA");
		_asserScreenNavigationCategories(1, "C_AAA");
		_asserScreenNavigationCategories(0, "C_AAAA");

		// Bind a draft object definition as a parent node in a published
		// object definition tree

		_testAddEdge(
			ObjectDefinitionTestUtil.addCustomObjectDefinition("A"),
			objectDefinitionAA,
			(objectDefinition1, objectDefinition2) -> {
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getObjectDefinitionId()),
					_objectDefinitionLocalService);
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AA", new String[] {"AAA"}
					).put(
						"AAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition2.getObjectDefinitionId()),
					_objectDefinitionLocalService);
			});

		_asserScreenNavigationCategories(0, "C_A");
		_asserScreenNavigationCategories(2, "C_AA");
		_asserScreenNavigationCategories(1, "C_AAA");

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind a draft object definition to a published object definition

		_testAddEdge(
			ObjectDefinitionTestUtil.addCustomObjectDefinition("A"),
			_addAndPublishCustomObjectDefinition("AA"),
			(objectDefinition1, objectDefinition2) -> {
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getObjectDefinitionId()),
					_objectDefinitionLocalService);
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition2.getObjectDefinitionId()),
					_objectDefinitionLocalService);
			});

		_asserScreenNavigationCategories(0, "C_A");
		_asserScreenNavigationCategories(1, "C_AA");

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService, new String[] {"C_A", "C_AA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind a draft object definition to a published object definition
		// with object entries

		objectDefinitionAA = _addAndPublishCustomObjectDefinition("AA");

		_addObjectEntry(objectDefinitionAA, Collections.emptyMap());

		ObjectDefinition objectDefinitionA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("A");

		_testAddEdge(
			objectDefinitionA, objectDefinitionAA,
			(objectDefinition1, objectDefinition2) -> {
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getObjectDefinitionId()),
					_objectDefinitionLocalService);
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition2.getObjectDefinitionId()),
					_objectDefinitionLocalService);
			});

		_asserScreenNavigationCategories(0, "C_A");
		_asserScreenNavigationCategories(1, "C_AA");

		long objectDefinitionId = objectDefinitionA.getObjectDefinitionId();

		AssertUtils.assertFailure(
			ObjectRelationshipEdgeException.class,
			StringBundler.concat(
				"There must be no unrelated object entries when both object ",
				"definitions are published so that the object relationship ",
				"can be an edge to a root context"),
			() -> _objectDefinitionLocalService.publishCustomObjectDefinition(
				TestPropsValues.getUserId(), objectDefinitionId));

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService, new String[] {"C_A", "C_AA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind a draft object definition tree to a published object definition
		// tree

		objectDefinitionA = ObjectDefinitionTestUtil.addCustomObjectDefinition(
			"A");
		objectDefinitionAA = ObjectDefinitionTestUtil.addCustomObjectDefinition(
			"AA");

		_addEdge(
			objectDefinitionA.getObjectDefinitionId(),
			objectDefinitionAA.getObjectDefinitionId());

		objectDefinitionAAA = _addAndPublishCustomObjectDefinition("AAA");
		ObjectDefinition objectDefinitionAAAA =
			_addAndPublishCustomObjectDefinition("AAAA");

		_addEdge(
			objectDefinitionAAA.getObjectDefinitionId(),
			objectDefinitionAAAA.getObjectDefinitionId());

		_testAddEdge(
			objectDefinitionAA, objectDefinitionAAA,
			(objectDefinition1, objectDefinition2) -> {
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[] {"AA"}
					).put(
						"AA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getRootObjectDefinitionId()),
					_objectDefinitionLocalService);
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AAA", new String[] {"AAAA"}
					).put(
						"AAAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition2.getRootObjectDefinitionId()),
					_objectDefinitionLocalService);
			});

		_asserScreenNavigationCategories(0, "C_A");
		_asserScreenNavigationCategories(0, "C_AA");
		_asserScreenNavigationCategories(2, "C_AAA");
		_asserScreenNavigationCategories(1, "C_AAAA");

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Bind a published object definition to a draft object definition tree

		objectDefinitionA = ObjectDefinitionTestUtil.addCustomObjectDefinition(
			"A");
		objectDefinitionAA = ObjectDefinitionTestUtil.addCustomObjectDefinition(
			"AA");

		_addEdge(
			objectDefinitionA.getObjectDefinitionId(),
			objectDefinitionAA.getObjectDefinitionId());

		_testAddEdge(
			objectDefinitionAA, _addAndPublishCustomObjectDefinition("AAA"),
			(objectDefinition1, objectDefinition2) -> {
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"A", new String[] {"AA"}
					).put(
						"AA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition1.getRootObjectDefinitionId()),
					_objectDefinitionLocalService);
				TreeTestUtil.assertObjectDefinitionTree(
					LinkedHashMapBuilder.put(
						"AAA", new String[0]
					).build(),
					_objectDefinitionTreeFactory.create(
						objectDefinition2.getObjectDefinitionId()),
					_objectDefinitionLocalService);
			});

		_asserScreenNavigationCategories(0, "C_A");
		_asserScreenNavigationCategories(0, "C_AA");
		_asserScreenNavigationCategories(1, "C_AAA");

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA"}, _objectEntryLocalService,
			_objectRelationshipLocalService);
	}

	@Test
	public void testShiftNodeDescendants() throws Exception {
		ObjectDefinition objectDefinitionA =
			_addAndPublishCustomObjectDefinition("A");

		ObjectDefinition objectDefinitionAA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AA");

		TreeTestUtil.bind(
			_objectRelationshipLocalService,
			Arrays.asList(
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService, objectDefinitionA,
					objectDefinitionAA)));

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		_objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(),
			objectDefinitionAA.getObjectDefinitionId());

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService, new String[] {"C_A", "C_AA"},
			_objectEntryLocalService, _objectRelationshipLocalService);
	}

	@Test
	public void testShiftPublishedNodeWithDraftNodeDescendants()
		throws Exception {

		// Shift a published node with draft descendant nodes

		_testCreateObjectDefinitionTree(
			true,
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[0]
			).build());
		_testCreateObjectDefinitionTree(
			false,
			LinkedHashMapBuilder.put(
				"AAA", new String[] {"AAAA", "AAAB"}
			).put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAB", new String[] {"AAABA"}
			).put(
				"AAAAA", new String[0]
			).put(
				"AAABA", new String[0]
			).build());

		ObjectDefinition objectDefinitionAAA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAA");

		TreeTestUtil.bind(
			_objectRelationshipLocalService,
			Collections.singletonList(
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService,
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_AA"),
					objectDefinitionAAA)));

		ObjectDefinition objectDefinitionA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_A");

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"AAA", new String[] {"AAAA", "AAAB"}
			).put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAB", new String[] {"AAABA"}
			).put(
				"AAAAA", new String[0]
			).put(
				"AAABA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionAAA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		_objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(),
			objectDefinitionAAA.getObjectDefinitionId());

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[] {"AAA"}
			).put(
				"AAA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		ObjectDefinition objectDefinitionAAAA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAAA");

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAAA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionAAAA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		ObjectDefinition objectDefinitionAAAB =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAAB");

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"AAAB", new String[] {"AAABA"}
			).put(
				"AAABA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionAAAB.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {
				"C_A", "C_AA", "C_AAA", "C_AAAA", "C_AAAB", "C_AAAAA", "C_AAABA"
			},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// Shift a published node with draft/published descendant nodes

		Tree treeA = TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			true,
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[0]
			).build());
		Tree treeB = TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			true,
			LinkedHashMapBuilder.put(
				"B", new String[] {"BB"}
			).put(
				"BB", new String[0]
			).build());
		Tree treeC = TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			false,
			LinkedHashMapBuilder.put(
				"C", new String[] {"CC"}
			).put(
				"CC", new String[0]
			).build());
		Tree treeD = TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			false,
			LinkedHashMapBuilder.put(
				"D", new String[] {"DD"}
			).put(
				"DD", new String[0]
			).build());

		ObjectDefinition objectDefinitionDD =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_DD");

		_objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(),
			objectDefinitionDD.getObjectDefinitionId());

		Node rootNodeD = treeD.getRootNode();

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"D", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(rootNodeD.getPrimaryKey()),
			_objectDefinitionLocalService);

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"DD", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionDD.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		TreeTestUtil.bind(
			_objectRelationshipLocalService,
			List.of(
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService,
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_AA"),
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_D")),
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService,
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_BB"),
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_D")),
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService,
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_CC"),
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_D"))));

		_objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(), rootNodeD.getPrimaryKey());

		Node rootNodeA = treeA.getRootNode();

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[] {"D"}
			).put(
				"D", new String[] {"DD"}
			).put(
				"DD", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(rootNodeA.getPrimaryKey()),
			_objectDefinitionLocalService);

		Node rootNodeB = treeB.getRootNode();

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"B", new String[] {"BB"}
			).put(
				"BB", new String[] {"D"}
			).put(
				"D", new String[] {"DD"}
			).put(
				"DD", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(rootNodeB.getPrimaryKey()),
			_objectDefinitionLocalService);

		Node rootNodeC = treeC.getRootNode();

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"C", new String[] {"CC"}
			).put(
				"CC", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(rootNodeC.getPrimaryKey()),
			_objectDefinitionLocalService);
	}

	@Test
	public void testShiftPublishedNodeWithPublishedNodeDescendants()
		throws Exception {

		_testCreateObjectDefinitionTree(
			true,
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[0]
			).build());

		_addObjectAction("C_AA");

		_assertModelResourceNames(ListUtil.fromArray("C_A", "C_AA"));

		_testCreateObjectDefinitionTree(
			true,
			LinkedHashMapBuilder.put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAAA", new String[0]
			).build());

		_updateWorkflowDefinitionLink("C_AAAA", "Single Approver");

		_addObjectAction("C_AAAAA");

		_assertModelResourceNames(ListUtil.fromArray("C_AAAA", "C_AAAAA"));

		ObjectDefinition objectDefinitionAAA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AAA");

		TreeTestUtil.bind(
			_objectRelationshipLocalService,
			Arrays.asList(
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService,
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_AA"),
					objectDefinitionAAA),
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService, objectDefinitionAAA,
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_AAAA"))));

		_objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(),
			objectDefinitionAAA.getObjectDefinitionId());

		_assertModelResourceNames(ListUtil.fromArray("C_A", "C_AA", "C_AAAAA"));

		ObjectDefinition objectDefinitionA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_A");

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[] {"AAA"}
			).put(
				"AAA", new String[] {"AAAA"}
			).put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAAA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		_updateWorkflowDefinitionLink("C_A", "Single Approver");

		TreeTestUtil.unbind(
			objectDefinitionA.getObjectDefinitionId(),
			_objectRelationshipLocalService);

		_assertModelResourceNames(ListUtil.fromArray("C_A"));
		_assertModelResourceNames(ListUtil.fromArray("C_AA", "C_AAAAA"));
		_assertWorkflowDefinitionLink("C_AA", "Single Approver");

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA", "C_AAAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);
	}

	@Test
	public void testShiftRootNode() throws Exception {

		// publish a draft object definition

		ObjectDefinition objectDefinitionA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("A");

		ObjectDefinition objectDefinitionAA =
			ObjectDefinitionTestUtil.addCustomObjectDefinition("AA");

		_objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(),
			objectDefinitionAA.getObjectDefinitionId());

		TreeTestUtil.bind(
			_objectRelationshipLocalService,
			Collections.singletonList(
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService, objectDefinitionA,
					objectDefinitionAA)));

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		_objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(),
			objectDefinitionA.getObjectDefinitionId());

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService, new String[] {"C_A", "C_AA"},
			_objectEntryLocalService, _objectRelationshipLocalService);

		// publish a draft object definition from a draft object definition tree

		_testCreateObjectDefinitionTree(
			false,
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[] {"AAA"}
			).put(
				"AAA", new String[0]
			).build());
		_testCreateObjectDefinitionTree(
			true,
			LinkedHashMapBuilder.put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAAA", new String[0]
			).build());

		TreeTestUtil.bind(
			_objectRelationshipLocalService,
			Collections.singletonList(
				ObjectRelationshipTestUtil.addObjectRelationship(
					_objectRelationshipLocalService,
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_AAA"),
					_objectDefinitionLocalService.getObjectDefinition(
						TestPropsValues.getCompanyId(), "C_AAAA"))));

		objectDefinitionA = _objectDefinitionLocalService.getObjectDefinition(
			TestPropsValues.getCompanyId(), "C_A");

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA"}
			).put(
				"AA", new String[] {"AAA"}
			).put(
				"AAA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		ObjectDefinition objectDefinitionAAAA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAAA");

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAAA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionAAAA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		ObjectDefinition objectDefinitionAAA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAA");

		_objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(),
			objectDefinitionAAA.getObjectDefinitionId());

		TreeTestUtil.assertObjectDefinitionTree(
			LinkedHashMapBuilder.put(
				"AAA", new String[] {"AAAA"}
			).put(
				"AAAA", new String[] {"AAAAA"}
			).put(
				"AAAAA", new String[0]
			).build(),
			_objectDefinitionTreeFactory.create(
				objectDefinitionAAA.getObjectDefinitionId()),
			_objectDefinitionLocalService);

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AAA", "C_AAAA", "C_AAAAA"},
			_objectEntryLocalService, _objectRelationshipLocalService);
	}

	private ObjectDefinition _addAndPublishCustomObjectDefinition()
		throws Exception {

		return _addAndPublishCustomObjectDefinition(
			ObjectDefinitionTestUtil.getRandomName());
	}

	private ObjectDefinition _addAndPublishCustomObjectDefinition(String name)
		throws Exception {

		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.addCustomObjectDefinition(
				0, false, name,
				List.of(
					new TextObjectFieldBuilder(
					).labelMap(
						RandomTestUtil.randomLocaleStringMap()
					).name(
						StringUtil.randomId()
					).build()));

		return _objectDefinitionLocalService.publishCustomObjectDefinition(
			TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId());
	}

	private ObjectRelationship _addEdge(
			long objectDefinitionId1, long objectDefinitionId2)
		throws Exception {

		return _objectRelationshipLocalService.addObjectRelationship(
			StringUtil.randomId(), TestPropsValues.getUserId(),
			objectDefinitionId1, objectDefinitionId2, 0,
			ObjectRelationshipConstants.DELETION_TYPE_CASCADE, true,
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			StringUtil.randomId(), false,
			ObjectRelationshipConstants.TYPE_ONE_TO_MANY, null);
	}

	private void _addObjectAction(String objectDefinitionName)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionName);

		_objectActionLocalService.addObjectAction(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), true, null,
			RandomTestUtil.randomString(),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			RandomTestUtil.randomString(),
			ObjectActionExecutorConstants.KEY_WEBHOOK,
			ObjectActionTriggerConstants.KEY_STANDALONE,
			UnicodePropertiesBuilder.put(
				"secret", "standalone"
			).put(
				"url", "https://standalone.com"
			).build(),
			false);
	}

	private ObjectEntry _addObjectEntry(
			ObjectDefinition objectDefinition, Map<String, Serializable> values)
		throws Exception {

		return _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null, values, ServiceContextTestUtil.getServiceContext());
	}

	private void _asserScreenNavigationCategories(
			int expectedSize, String objectDefinitionName)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionName);

		List<ScreenNavigationCategory> screenNavigationCategories =
			ScreenNavigationRegistryUtil.getScreenNavigationCategories(
				objectDefinition.getClassName(), TestPropsValues.getUser(),
				null);

		Assert.assertEquals(
			screenNavigationCategories.toString(), expectedSize,
			screenNavigationCategories.size());
	}

	private void _assertModelResourceNames(List<String> objectDefinitionNames)
		throws Exception {

		Map<String, Set<String>> resourceReferences =
			ReflectionTestUtil.getFieldValue(
				_resourceActions, "_resourceReferences");

		ObjectDefinition rootObjectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionNames.get(0));

		List<String> modelResourceNames = ListUtil.filter(
			new ArrayList<>(
				resourceReferences.get(rootObjectDefinition.getPortletId())),
			resourceName -> StringUtil.startsWith(
				resourceName,
				ObjectDefinitionConstants.
					CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION));

		Assert.assertEquals(
			modelResourceNames.toString(), objectDefinitionNames.size(),
			modelResourceNames.size());

		for (String objectDefinitionName : objectDefinitionNames) {
			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.getObjectDefinition(
					TestPropsValues.getCompanyId(), objectDefinitionName);

			Assert.assertThat(
				modelResourceNames,
				CoreMatchers.hasItem(objectDefinition.getClassName()));
		}
	}

	private void _assertWorkflowDefinitionLink(
			String objectDefinitionName, String workflowDefinitionName)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionName);

		List<WorkflowDefinitionLink> workflowDefinitionLinks =
			_workflowDefinitionLinkLocalService.getWorkflowDefinitionLinks(
				objectDefinition.getCompanyId(),
				objectDefinition.getClassName());

		Assert.assertEquals(
			workflowDefinitionLinks.toString(), 1,
			workflowDefinitionLinks.size());

		WorkflowDefinitionLink workflowDefinitionLink =
			workflowDefinitionLinks.get(0);

		Assert.assertEquals(
			workflowDefinitionName,
			workflowDefinitionLink.getWorkflowDefinitionName());
	}

	private ObjectRelationship _bindObjectDefinitions(
			long objectDefinitionId1, long objectDefinitionId2)
		throws Exception {

		return _objectRelationshipLocalService.addObjectRelationship(
			StringUtil.randomId(), TestPropsValues.getUserId(),
			objectDefinitionId1, objectDefinitionId2, 0,
			ObjectRelationshipConstants.DELETION_TYPE_CASCADE, true,
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			StringUtil.randomId(), false,
			ObjectRelationshipConstants.TYPE_ONE_TO_MANY, null);
	}

	private void _completeWorkflowTask(String className, long classPK)
		throws Exception {

		List<WorkflowInstance> workflowInstances =
			_workflowInstanceManager.getWorkflowInstances(
				TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
				className, classPK, false, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				null);

		WorkflowInstance workflowInstance = workflowInstances.get(0);

		for (WorkflowTask workflowTask :
				_workflowTaskManager.getWorkflowTasksBySubmittingUser(
					TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
					false, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			if (workflowInstance.getWorkflowInstanceId() !=
					workflowTask.getWorkflowInstanceId()) {

				continue;
			}

			workflowTask = _workflowTaskManager.assignWorkflowTaskToUser(
				TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
				workflowTask.getWorkflowTaskId(), TestPropsValues.getUserId(),
				StringPool.BLANK, null, null);

			_workflowTaskManager.completeWorkflowTask(
				TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
				workflowTask.getWorkflowTaskId(), Constants.APPROVE,
				StringPool.BLANK, null);
		}
	}

	private void _testAddEdge(
			ObjectDefinition objectDefinition1,
			ObjectDefinition objectDefinition2,
			UnsafeBiConsumer<ObjectDefinition, ObjectDefinition, Exception>
				biConsumer)
		throws Exception {

		ObjectRelationship objectRelationship = _addEdge(
			objectDefinition1.getObjectDefinitionId(),
			objectDefinition2.getObjectDefinitionId());

		Assert.assertTrue(objectRelationship.isEdge());
		Assert.assertEquals(
			ObjectRelationshipConstants.DELETION_TYPE_CASCADE,
			objectRelationship.getDeletionType());

		biConsumer.accept(
			_objectDefinitionLocalService.getObjectDefinition(
				objectDefinition1.getObjectDefinitionId()),
			_objectDefinitionLocalService.getObjectDefinition(
				objectDefinition2.getObjectDefinitionId()));
	}

	private void _testCreateObjectDefinitionTree(
			boolean published, Map<String, String[]> treeMap)
		throws Exception {

		TreeTestUtil.assertObjectDefinitionTree(
			treeMap,
			TreeTestUtil.createObjectDefinitionTree(
				_objectDefinitionLocalService, _objectRelationshipLocalService,
				published, treeMap),
			_objectDefinitionLocalService);
	}

	private void _updateWorkflowDefinitionLink(
			String objectDefinitionName, String workflowDefinitionName)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionName);

		_workflowDefinitionLinkLocalService.updateWorkflowDefinitionLink(
			TestPropsValues.getUserId(), TestPropsValues.getCompanyId(), 0,
			objectDefinition.getClassName(), 0, 0, workflowDefinitionName, 1);

		_assertWorkflowDefinitionLink(
			objectDefinitionName, workflowDefinitionName);
	}

	@Inject
	private ObjectActionLocalService _objectActionLocalService;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private ObjectDefinitionTreeFactory _objectDefinitionTreeFactory;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ObjectFieldLocalService _objectFieldLocalService;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Inject
	private ResourceActions _resourceActions;

	@Inject
	private WorkflowDefinitionLinkLocalService
		_workflowDefinitionLinkLocalService;

	@Inject
	private WorkflowInstanceManager _workflowInstanceManager;

	@Inject
	private WorkflowTaskManager _workflowTaskManager;

}