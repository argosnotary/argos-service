package com.argosnotary.argos.service.roles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.account.ArgosUserDetails;
import com.argosnotary.argos.service.nodes.NodeService;

@ExtendWith(MockitoExtension.class)
class PermissionCheckAdvisorTest {
	
	private UUID resourceId = UUID.randomUUID();
	
	private PermissionCheckAdvisor permissionCheckAdvisor;
	
	@Mock
	private Node node;
	
	@Mock
	private RoleAssignmentService roleAssignmentService;
	
	@Mock
	private NodeService nodeService;

    @Mock
	private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private PermissionCheck permissionCheck;
    
    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;
    
    @Mock
    private ArgosUserDetails argosUserDetails;
    
    @Mock
    private PersonalAccount personalAccount;
    
	@BeforeEach
	void setUp() throws Exception {
		permissionCheckAdvisor = new PermissionCheckAdvisor(roleAssignmentService, nodeService);
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void testNotAuthenticated() {
		when(securityContext.getAuthentication()).thenReturn(null);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getName()).thenReturn("testMethode");
		when(permissionCheck.permissions()).thenReturn(new Permission[] {Permission.READ});
		assertThrows(AccessDeniedException.class,
	            ()->{
	        		permissionCheckAdvisor.checkPermissions(joinPoint, permissionCheck);
	            });
	}

	@Test
	void testHasNoPermission() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(roleAssignmentService.findAllPermissionDownTree(node)).thenReturn(Set.of());
		when(permissionCheck.permissions()).thenReturn(new Permission[] {Permission.READ});
		when(joinPoint.getArgs()).thenReturn(new Object[] {resourceId});
		when(nodeService.findById(resourceId)).thenReturn(Optional.of(node));
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getName()).thenReturn("testMethode");
		assertThrows(AccessDeniedException.class,
	            ()->{
	        		permissionCheckAdvisor.checkPermissions(joinPoint, permissionCheck);
	            });
	}
	
	@Test
	void testHasNotAuthenticated() {
		when(securityContext.getAuthentication()).thenReturn(null);
		when(permissionCheck.permissions()).thenReturn(new Permission[] {Permission.READ});
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getName()).thenReturn("testMethode");
		assertThrows(AccessDeniedException.class,
	            ()->{
	        		permissionCheckAdvisor.checkPermissions(joinPoint, permissionCheck);
	            });
	}
	
	@Test
	void testHasNoArgs() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(permissionCheck.permissions()).thenReturn(new Permission[] {Permission.READ});
		when(joinPoint.getArgs()).thenReturn(new Object[] {});
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getName()).thenReturn("testMethode");
		assertThrows(AccessDeniedException.class,
	            ()->{
	        		permissionCheckAdvisor.checkPermissions(joinPoint, permissionCheck);
	            });
	}

	@Test
	void testHasPermission() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(roleAssignmentService.findAllPermissionDownTree(node)).thenReturn(Set.of(Permission.READ));
		when(permissionCheck.permissions()).thenReturn(new Permission[] {Permission.READ});
		when(joinPoint.getArgs()).thenReturn(new Object[] {resourceId});
		when(nodeService.findById(resourceId)).thenReturn(Optional.of(node));
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getName()).thenReturn("testMethode");
		assertDoesNotThrow(
	            ()->{
	        		permissionCheckAdvisor.checkPermissions(joinPoint, permissionCheck);
	            });
	}
	
	@Test
	void testHasPermissionWithSeveral() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(roleAssignmentService.findAllPermissionDownTree(node)).thenReturn(Set.of(Permission.RELEASE,Permission.READ));
		when(permissionCheck.permissions()).thenReturn(new Permission[] {Permission.LINK_ADD,Permission.READ});
		when(joinPoint.getArgs()).thenReturn(new Object[] {resourceId});
		when(joinPoint.getSignature()).thenReturn(signature);
		when(nodeService.findById(resourceId)).thenReturn(Optional.of(node));
		when(signature.getName()).thenReturn("testMethode");
		assertDoesNotThrow(
	            ()->{
	        		permissionCheckAdvisor.checkPermissions(joinPoint, permissionCheck);
	            });
	}

	@Test
	void testHasNoPermissionsToCheck() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(permissionCheck.permissions()).thenReturn(new Permission[] {});
		when(joinPoint.getArgs()).thenReturn(new Object[] {resourceId});
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getName()).thenReturn("testMethode");
		assertThrows(AccessDeniedException.class,
	            ()->{
	        		permissionCheckAdvisor.checkPermissions(joinPoint, permissionCheck);
	            });
	}

}
