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
package com.argosnotary.argos.service.rest.roles;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.openapi.rest.model.RestRoleAssignment;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentRestServiceTest {
	
	RoleAssignmentRestService roleAssignmentRestService;
    
	@Mock
    RoleAssignmentService roleAssignmentService;
	
	@Mock
    private HttpServletRequest httpServletRequest;
    
	RoleAssignmentMapper roleAssignmentMapper = Mappers.getMapper(RoleAssignmentMapper.class);
	
    private Organization org2, org3;
    private Project project211;
    private ManagementNode node21, node22, node31, node311;
	private RoleAssignment ra21, ra22, ra211, ra311;
	
	PersonalAccount pa = PersonalAccount.builder().build();

	@BeforeEach
	void setUp() throws Exception {
		roleAssignmentRestService = new RoleAssignmentRestServiceImpl(roleAssignmentService, roleAssignmentMapper);

        org2 = new Organization(UUID.randomUUID(), "org2", Domain.builder().domain("org2.com").build());
        org3 = new Organization(UUID.randomUUID(), "org3", Domain.builder().domain("org3.com").build());
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", new ArrayList<>(), org2.getId());
        node22 = new ManagementNode(UUID.randomUUID(), "node22", new ArrayList<>(), org2.getId());
        node31 = new ManagementNode(UUID.randomUUID(), "node31", new ArrayList<>(), org3.getId());
        node311 = new ManagementNode(UUID.randomUUID(), "node311", new ArrayList<>(), node31.getId());
        node21.setPathToRoot(List.of(node21.getId(), org2.getId()));
        node22.setPathToRoot(List.of(node22.getId(), org2.getId()));
        node31.setPathToRoot(List.of(node31.getId(), org3.getId()));
        node311.setPathToRoot(List.of(node311.getId(), node31.getId(), org3.getId()));

        project211 = new Project(UUID.randomUUID(), "project211", List.of(), node21.getId());
        project211.setPathToRoot(List.of(project211.getId(),node21.getId(), org2.getId()));
        
        ra21 = RoleAssignment.builder().id(UUID.randomUUID()).resourceId(node21.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra22 = RoleAssignment.builder().resourceId(node22.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra211 = RoleAssignment.builder().resourceId(project211.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra311 = RoleAssignment.builder().resourceId(node311.getId()).identityId(pa.getId()).role(new Role.LinkAdder()).build();
	}
	
	@Test
	void testCreateRoleAssignment() throws URISyntaxException {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
		RestRoleAssignment ra = roleAssignmentMapper.convertToRestRoleAssignment(ra21);
		when(roleAssignmentService.save(ra21)).thenReturn(ra21);
		ResponseEntity<RestRoleAssignment> response = roleAssignmentRestService.createRoleAssignment(ra.getResourceId(), ra);
        assertThat(response.getStatusCode(), is(HttpStatusCode.valueOf(201)));
        assertEquals(new URI(String.format("/api/roleassignments/%s", ra.getId())),response.getHeaders().getLocation());
        assertEquals(ra, response.getBody());
	}

	@Test
	void testDeleteRoleAssignemntById() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
		ResponseEntity<Void> response = roleAssignmentRestService.deleteRoleAssignemntById(ra21.getResourceId(), ra21.getId());
		verify(roleAssignmentService).delete(ra21.getId());
        assertThat(response.getStatusCode(), is(HttpStatusCode.valueOf(204)));
	}

	@Test
	void testGetRoleAssignments() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
		RestRoleAssignment ra = roleAssignmentMapper.convertToRestRoleAssignment(ra21);
		when(roleAssignmentService.findByResourceId(ra21.getResourceId())).thenReturn(List.of(ra21));
        ResponseEntity<List<RestRoleAssignment>> response = roleAssignmentRestService.getRoleAssignments(ra21.getResourceId());
        assertThat(response.getStatusCode(), is(HttpStatusCode.valueOf(200)));
        assertEquals(List.of(ra), response.getBody());
	}

}
