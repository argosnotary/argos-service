package com.argosnotary.argos.service.roles;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.mongodb.nodes.NodeRepository;
import com.argosnotary.argos.service.nodes.NodeDeleteService;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.NodeServiceImpl;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {
	
	@Mock
	private NodeRepository nodeRepository;

	private NodeDeleteService nodeDeleteService;
	
	@Mock
    private RoleAssignmentService roleAssignmentService;

	@Mock
	private AccountSecurityContext accountSecurityContext;
	
    @Mock
	private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
	
	private NodeService nodeService;
	
	private PersonalAccount pa;
	private ServiceAccount sa;
	
	private UUID resourceId1 = UUID.randomUUID();
	private UUID resourceId2 = UUID.randomUUID();
	private UUID resourceId3 = UUID.randomUUID();
	private UUID resourceId4 = UUID.randomUUID();
	
	private UUID paId = UUID.randomUUID();
	
	private RoleAssignment contributor = RoleAssignment.builder().identityId(paId).resourceId(resourceId4).role(new Role.Contributor()).build();
	private RoleAssignment reader = RoleAssignment.builder().identityId(paId).resourceId(resourceId2).role(new Role.Reader()).build();
	private RoleAssignment adder = RoleAssignment.builder().identityId(paId).resourceId(resourceId2).role(new Role.LinkAdder()).build();
	
	private Project proj1;

	@BeforeEach
	void setUp() throws Exception {
		//nodeService = new NodeServiceImpl(nodeRepository, nodeDeleteService, roleAssignmentService, accountSecurityContext);
		proj1 = new Project();
		proj1.setId(resourceId1);
		proj1.setPathToRoot(List.of(resourceId1, resourceId2, resourceId3));
		
		pa = PersonalAccount.builder().name("pa").id(paId).build();
		

		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void testGetAllPermissionDownTreePersonalAccount() {
//		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
//		when(nodeRepository.findById(resourceId1)).thenReturn(Optional.of(proj1));
//		when(roleAssignmentService.findByAccountId(pa.getId())).thenReturn(List.of(contributor,reader,adder));
//		Set<Permission> permissions = nodeService.getAllPermissionDownTree(resourceId1);
//		Set<Permission> expected = Set.of(Permission.LINK_ADD, Permission.READ);
//		
//		assertEquals(expected, permissions);
	}

}
