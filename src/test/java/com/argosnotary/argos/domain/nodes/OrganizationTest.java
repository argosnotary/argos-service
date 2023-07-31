/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2023 Gerard Borst <gerard.borst@argosnotary.com>
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.crypto.HashAlgorithm;
import com.argosnotary.argos.domain.crypto.KeyAlgorithm;
import com.argosnotary.argos.domain.crypto.signing.SignatureAlgorithm;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;

class OrganizationTest {
    private Organization org1, org2;
    private Project project1, project2, project3, project4, project5;
    private ManagementNode node1, node2, node3, node4;

	@BeforeEach
	void setUp() throws Exception {

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
        
//        org2.getNodes().add(node1);
//        org2.getNodes().add(node2);
//        org2.getNodes().add(node3);
//        org2.getNodes().add(node4);
//        org2.getNodes().add(project1);
//        org2.getNodes().add(project2);
//        org2.getNodes().add(project3);
//        org2.getNodes().add(project4);
//        org2.getNodes().add(project5);
		
	}
	
	@Test
	void testEquals() {
		Organization expected = new Organization(org2.getId(), "org2", null);
		assertEquals(expected, org2);
		
	}

	@Test
	void testGetChildrenNoChildren() {
		assertThat(org1.getChildren()).isEmpty();
	}
	
	@Test
	void testGetChildrenWithChildren() {
		assertThat(org2.getChildren()).size().isEqualTo(3);
	}
	
	@Test
	void testLeafNode() {
		assertThat(org1.isLeaf()).isTrue();
		assertThat(org2.isLeaf()).isFalse();
	}
	
	@Test
	void testResourceId() {
		UUID aId = UUID.randomUUID();
		Organization org1 = new Organization(aId, "org1", null);
		assertThat(org1.getId()).isEqualTo(aId);
	}
	
	@Test
	void testGetParentId() {
		Organization org1 = new Organization();
		assertThat(org1.getParent()).isEmpty();
	}
	
	@Test
	void testNoParent() {
		Organization org1 = new Organization();
		Organization org2 = new Organization();
		org2.setId(org1.getId());
		assertThrows(UnsupportedOperationException.class, () -> {
			org1.setParent(node1);
          });
		assertEquals(org2, org1);
		assertNull(org1.getParentId());
	}

}
