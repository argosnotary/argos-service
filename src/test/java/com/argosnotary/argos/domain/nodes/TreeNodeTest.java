/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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

import static com.argosnotary.argos.domain.nodes.TreeNodeTest.TestVisitor.VISIT_END_POINT;
import static com.argosnotary.argos.domain.nodes.TreeNodeTest.TestVisitor.VISIT_ENTER;
import static com.argosnotary.argos.domain.nodes.TreeNodeTest.TestVisitor.VISIT_EXIT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class TreeNodeTest {
    private Organization org1, org2;
    private Project project21, project211, project212, project2211, project221;
    private ManagementNode node21, node22, node221, node222;

	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

    @BeforeEach
    void setUp() {

        org1 = new Organization(UUID.randomUUID(), "org1", null);
        org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", new ArrayList<>(), org2.getId());
        node22 = new ManagementNode(UUID.randomUUID(), "node22", new ArrayList<>(), org2.getId());
        node221 = new ManagementNode(UUID.randomUUID(), "node221", new ArrayList<>(), node22.getId());
        node222 = new ManagementNode(UUID.randomUUID(), "node222", new ArrayList<>(), node22.getId());
        

        project21 = new Project(UUID.randomUUID(), "project21", new ArrayList<>(), org2.getId());
        project211 = new Project(UUID.randomUUID(), "project211", new ArrayList<>(), node21.getId());
        project212 = new Project(UUID.randomUUID(), "project212", new ArrayList<>(), node21.getId());
        project2211 = new Project(UUID.randomUUID(), "project2211", new ArrayList<>(), node221.getId());
        project221 = new Project(UUID.randomUUID(), "project221", new ArrayList<>(), node22.getId());
        
        node21.getPathToRoot().add(node21.getId());
        node22.getPathToRoot().add(node22.getId());
        node221.getPathToRoot().add(node221.getId());
        node222.getPathToRoot().add(node222.getId());

        project21.getPathToRoot().add(project21.getId());
        project211.getPathToRoot().add(project211.getId());
        project212.getPathToRoot().add(project212.getId());
        project2211.getPathToRoot().add(project2211.getId());
        project221.getPathToRoot().add(project221.getId());
        
        node21.getPathToRoot().addAll(org2.getPathToRoot());
        node22.getPathToRoot().addAll(org2.getPathToRoot());
        node221.getPathToRoot().addAll(node22.getPathToRoot());
        node222.getPathToRoot().addAll(node22.getPathToRoot());
        

        project21.getPathToRoot().addAll(org2.getPathToRoot());
        project211.getPathToRoot().addAll(node21.getPathToRoot());
        project212.getPathToRoot().addAll(node21.getPathToRoot());
        project2211.getPathToRoot().addAll(node221.getPathToRoot());
        project221.getPathToRoot().addAll(node22.getPathToRoot());
    }
    
    @Test
    void validate() {
    	Set<ConstraintViolation<Project>> violations3 = validator.validate(project21);
		assertThat(violations3.size(), is(0));
    	Set<ConstraintViolation<Project>> violations4 = validator.validate(project211);
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
		assertEquals(project21.getPathToRoot(), List.of(project21.getId(),org2.getId()));
		assertEquals(project211.getPathToRoot(), List.of(project211.getId(),node21.getId(),org2.getId()));
		assertEquals(project212.getPathToRoot(), List.of(project212.getId(),node21.getId(),org2.getId()));
		assertEquals(node221.getPathToRoot(), List.of(node221.getId(),node22.getId(),org2.getId()));
		assertEquals(project221.getPathToRoot(), List.of(project221.getId(),node22.getId(),org2.getId()));
    }
    
    @Test
    void testConstructor() {
    	Optional<TreeNode> empty = Optional.empty();
		Throwable exception = assertThrows(ValidationException.class, () -> {
			new TreeNode(project21, empty);
          });
        assertEquals(String.format("Inconsistent creation of Treenode with node [%s] and parent [%s]",project21.toString(), "empty"), exception.getMessage());
        
        TreeNode orgNode = new TreeNode(org2, Optional.empty());
        Optional<TreeNode> optOrgNode = Optional.of(orgNode);
        
        project21.setParentId(null);
		exception = assertThrows(ValidationException.class, () -> {
			new TreeNode(project21, optOrgNode);
          });
        assertEquals(String.format("Inconsistent creation of Treenode with node [%s] and parent [%s]",project21.toString(), orgNode.toString()), exception.getMessage());
        
        project21.setParentId(UUID.randomUUID());
		exception = assertThrows(ValidationException.class, () -> {
			new TreeNode(project21, optOrgNode);
          });
        assertEquals(String.format("Inconsistent creation of Treenode with node [%s] and parent [%s]",project21.toString(), orgNode.toString()), exception.getMessage());
        
        project21.setParentId(UUID.randomUUID());
		exception = assertThrows(ValidationException.class, () -> {
			new TreeNode(project21, empty);
          });
        assertEquals(String.format("Inconsistent creation of Treenode with node [%s] and parent [%s]",project21.toString(), "empty"), exception.getMessage());
    }
    
    @Test
    void testCreateUpTree() {
		Set<Node> empty = Set.of();
    	Throwable exception = assertThrows(ArgosError.class, () -> {
	    	TreeNode.createUpTree(empty);
          });
        assertEquals("No pivot", exception.getMessage());
        
        Set<Node> orgSet = Set.of(org1, org2);
    	
        exception = assertThrows(ArgosError.class, () -> {
        	TreeNode.createUpTree(orgSet);
          });
        assertEquals("More than 1 pivot", exception.getMessage());
    }
    

    @Test
    void visitOnlyRoot() {
        TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        TreeNode orgNode = new TreeNode(org1, Optional.empty());
        orgNode.visit(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(1));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(1));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(0));
    }
    
    @Test
    void visitTree() {
        TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        TreeNode orgNode = TreeNode.createUpTree(Set.of(org2, project21, project211, project212, project2211, project221, node21, node22, node221, node222));
        orgNode.visit(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(10));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(10));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(0));
    }
    
    @Test
    void testGetPathToRoot() {
    	TreeNode leaf = TreeNode.getPathToRoot(project2211, Set.of(org2, project2211, node22, node221));
    	assertEquals(leaf.getParent().get().getNode(),node221);
    	assertTrue(leaf.getChildren().isEmpty());
    	assertEquals(leaf.getParent().get().getChildren().iterator().next().getNode(), project2211);
    }
    
    @Test
    void visitDown4Nodes() {
        TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        TreeNode projectNode2211 = TreeNode.getPathToRoot(project2211, Set.of(org2, project2211, node22, node221));
        projectNode2211.visitDown(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(3));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(3));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(1));
    }
    
    @Test
    void visitDownMultiProj() {
        TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        TreeNode projectNode212 = TreeNode.getPathToRoot(project212, Set.of(org2, project212, node21));
        projectNode212.visitDown(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(2));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(2));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(1));
    }
    
    @Test
    void visitDownOnlyRoot() {
        TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        TreeNode orgNode1 = TreeNode.getPathToRoot(org1, Set.of(org1));
        orgNode1.visitDown(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(0));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(0));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(1));
    }
    
    @Test
    void visitDownOnlyRoot2() {
        TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        TreeNode orgNode2 = TreeNode.getPathToRoot(org2, Set.of(org2));
        orgNode2.visitDown(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(0));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(0));
        assertThat(treeNodeVisitor.result().get(VISIT_END_POINT), is(1));
    }
    
    @Test
    void typeTest() {
    	TreeNode org1Node = TreeNode.createUpTree(Set.of(org1));
    	TreeNode org2Node = TreeNode.createUpTree(Set.of(org2, project21, project211, project212, project2211, project221, node21, node22, node221, node222));

    	TreeNode node21Node = TreeNode.createUpTree(Set.of(project211, project212, node21));
        
        TreeNode node22Node = new TreeNode(node22, Optional.of(org2Node));
        TreeNode node222Node = new TreeNode(node222, Optional.of(node22Node));
    	
    	TreeNode projectNode21 = new TreeNode(project21, Optional.of(org2Node));
    	TreeNode projectNode211 = new TreeNode(project211, Optional.of(node21Node));
        
    	assertThat(projectNode21.isLeaf(), is(true));
    	assertThat(projectNode211.isLeaf(), is(true));
        assertThat(org1Node.isLeaf(), is(true));
        assertThat(org2Node.isLeaf(), is(false));
    	
        assertThat(node22Node.isLeaf(), is(false));
        assertThat(node222Node.isLeaf(), is(true));
        
        assertTrue(org1Node.getChildren().isEmpty());
        assertThat(org2Node.getChildren().size(), is(3));
        
        assertThat(node21Node.getChildren().size(), is(2));
        assertTrue(node222Node.getChildren().isEmpty());
        
    }
	
	@Test
	void testGetParent() {
		TreeNode projectNode21 = TreeNode.getPathToRoot(project21, Set.of(org2, project21));
		assertThat(projectNode21.getParent().get().getNode(), is(org2));
		TreeNode nodeNode221 = TreeNode.getPathToRoot(node221, Set.of(org2, node22, node221));
		assertThat(nodeNode221.getParent().get().getNode(), is(node22));
	}
	
	@Test
	void testDefaultNullResult() {
		TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitorNullResult();
		assertNull(treeNodeVisitor.result());
	}

    static class TestVisitor implements TreeNodeVisitor<Map<String, Integer>> {
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
        public void visitEnter(TreeNode node) {
            visits.put(VISIT_ENTER, visits.get(VISIT_ENTER) + 1);
        }

        @Override
        public void visitExit(TreeNode node) {
            visits.put(VISIT_EXIT, visits.get(VISIT_EXIT) + 1);
        }

        @Override
        public Map<String, Integer> result() {
            return visits;
        }

		@Override
		public void visitEndPoint(TreeNode node) {
            visits.put(VISIT_END_POINT, visits.get(VISIT_END_POINT) + 1);
			
		}
    }
    
    static class TestVisitorNullResult implements TreeNodeVisitor<Map<String, Integer>> {

		@Override
		public void visitEnter(TreeNode node) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitExit(TreeNode node) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visitEndPoint(TreeNode node) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    
}