/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        
        node1 = new ManagementNode(UUID.randomUUID(), "node1", new ArrayList<>(), org2.getId());
        node2 = new ManagementNode(UUID.randomUUID(), "node2", new ArrayList<>(), org2.getId());
        node3 = new ManagementNode(UUID.randomUUID(), "node3", new ArrayList<>(), node2.getId());
        node4 = new ManagementNode(UUID.randomUUID(), "node4", new ArrayList<>(), node2.getId());
        

        project1 = new Project(UUID.randomUUID(), "project1", new ArrayList<>(), org2.getId());
        project2 = new Project(UUID.randomUUID(), "project2", new ArrayList<>(), node1.getId());
        project3 = new Project(UUID.randomUUID(), "project3", new ArrayList<>(), node1.getId());
        project4 = new Project(UUID.randomUUID(), "project4", new ArrayList<>(), node3.getId());
        project5 = new Project(UUID.randomUUID(), "project5", new ArrayList<>(), node2.getId());
        
        node1.getPathToRoot().add(node1.getId());
        node2.getPathToRoot().add(node2.getId());
        node3.getPathToRoot().add(node3.getId());
        node4.getPathToRoot().add(node4.getId());

        project1.getPathToRoot().add(project1.getId());
        project2.getPathToRoot().add(project2.getId());
        project3.getPathToRoot().add(project3.getId());
        project4.getPathToRoot().add(project4.getId());
        project5.getPathToRoot().add(project5.getId());
        
        node1.getPathToRoot().addAll(org2.getPathToRoot());
        node2.getPathToRoot().addAll(org2.getPathToRoot());
        node3.getPathToRoot().addAll(node2.getPathToRoot());
        node4.getPathToRoot().addAll(node2.getPathToRoot());
        

        project1.getPathToRoot().addAll(org2.getPathToRoot());
        project2.getPathToRoot().addAll(node1.getPathToRoot());
        project3.getPathToRoot().addAll(node1.getPathToRoot());
        project4.getPathToRoot().addAll(node3.getPathToRoot());
        project5.getPathToRoot().addAll(node2.getPathToRoot());

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
    
}