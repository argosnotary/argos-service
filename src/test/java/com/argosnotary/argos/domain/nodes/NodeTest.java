/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.domain.nodes;

import static com.argosnotary.argos.domain.nodes.NodeTest.TestVisitor.VISIT_END_POINT;
import static com.argosnotary.argos.domain.nodes.NodeTest.TestVisitor.VISIT_ENTER;
import static com.argosnotary.argos.domain.nodes.NodeTest.TestVisitor.VISIT_EXIT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.NodeVisitor;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class NodeTest {
    private Organization org1, org2;
    private Project project1, project2, project3, project4, project5;
    private ManagementNode node1, node2, node3, node4;

	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

    @BeforeEach
    void setUp() {

        org1 = new Organization(UUID.randomUUID(), "org1", null);
        org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        node1 = new ManagementNode(UUID.randomUUID(), "node1", org2);
        node2 = new ManagementNode(UUID.randomUUID(), "node2", org2);
        node3 = new ManagementNode(UUID.randomUUID(), "node3", node2);
        node4 = new ManagementNode(UUID.randomUUID(), "node4", node2);
        

        project1 = new Project(UUID.randomUUID(), "project1", org2);
        project2 = new Project(UUID.randomUUID(), "project2", node1);
        project3 = new Project(UUID.randomUUID(), "project3", node1);
        project4 = new Project(UUID.randomUUID(), "project4", node3);
        project5 = new Project(UUID.randomUUID(), "project5", node2);

    }
    
    @Test
    void validate() {
    	Set<ConstraintViolation<Project>> violations3 = validator.validate(project1);
		assertThat(violations3.size(), is(0));
    	Set<ConstraintViolation<Project>> violations4 = validator.validate(project2);
		assertThat(violations4.size(), is(0));
    	//Set<ConstraintViolation<Project>> violations5 = validator.validate(project3);
    	//violations3.addAll(violations5);
    	Set<ConstraintViolation<Organization>> violations1 = validator.validate(org1);
    	Set<ConstraintViolation<Organization>> violations2 = validator.validate(org2);
		assertThat(violations1.size(), is(1));
		assertThat(violations2.size(), is(1));
    }
    
    @Test
    void testPathToRoot() {
		assertEquals(org1.getPathToRoot(), List.of(org1.getId()));
		assertEquals(org2.getPathToRoot(), List.of(org2.getId()));
		assertEquals(project1.getPathToRoot(), List.of(project1.getId(),org2.getId()));
		assertEquals(project2.getPathToRoot(), List.of(project2.getId(),node1.getId(),org2.getId()));
		assertEquals(project3.getPathToRoot(), List.of(project3.getId(),node1.getId(),org2.getId()));
		assertEquals(node4.getPathToRoot(), List.of(node4.getId(),node2.getId(),org2.getId()));
		assertEquals(project5.getPathToRoot(), List.of(project5.getId(),node2.getId(),org2.getId()));
    }

    @Test
    void visitOnlyRoot() {
        NodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        org1.visit(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(1));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(1));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(0));
    }
    
    @Test
    void visitTree() {
        NodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        org2.visit(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(10));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(10));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(0));
    }
    
    @Test
    void visitDown4Nodes() {
        NodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        project4.visitDown(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(3));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(3));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(1));
    }
    
    @Test
    void visitDownMultiProj() {
        NodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        project3.visitDown(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(2));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(2));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(1));
    }
    
    @Test
    void visitDownOnlyRoot() {
        NodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        org1.visitDown(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(0));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(0));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(1));
    }
    
    @Test
    void typeTest() {
        assertThat(project1.isLeaf(), is(true));
        assertThat(project2.isLeaf(), is(true));
        assertThat(org1.isLeaf(), is(true));
        assertThat(org2.isLeaf(), is(false));
        assertThat(node2.isLeaf(), is(false));
        assertThat(node4.isLeaf(), is(true));
        
        assertTrue(org1.getChildren().isEmpty());
        assertThat(org2.getChildren().size(), is(3));
        
        assertThat(node1.getChildren().size(), is(2));
        assertTrue(node4.getChildren().isEmpty());
        
    }

	
	@Test
	void testGetParent() {
		assertThat(project1.getParent().get(), is(org2));
		assertThat(node3.getParent().get(), is(node2));
	}
	
	@Test
	void testDefaultNullResult() {
		NodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitorNullResult();
		assertNull(treeNodeVisitor.result());
	}

    static class TestVisitor implements NodeVisitor<Map<String, Integer>> {
        protected static final String VISIT_ENTER = "visitEnter";
        protected static final String VISIT_EXIT = "visitExit";
        protected static final String VISIT_END_POINT = "visitEndPoint";
        private Map<String, Integer> visits = new HashMap<>();

        public TestVisitor() {
            visits.put(VISIT_ENTER, 0);
            visits.put(VISIT_EXIT, 0);
            visits.put(VISIT_END_POINT, 0);
        }

        @Override
        public void visitEnter(Node node) {
            visits.put(VISIT_ENTER, visits.get(VISIT_ENTER) + 1);
        }

        @Override
        public void visitExit(Node node) {
            visits.put(VISIT_EXIT, visits.get(VISIT_EXIT) + 1);
        }

        @Override
        public Map<String, Integer> result() {
            return visits;
        }

		@Override
		public void visitEndPoint(Node node) {
            visits.put(VISIT_END_POINT, visits.get(VISIT_END_POINT) + 1);
			
		}
    }
    
    static class TestVisitorNullResult implements NodeVisitor<Map<String, Integer>> {

		@Override
		public void visitEnter(Node node) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitExit(Node node) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitEndPoint(Node node) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    
}