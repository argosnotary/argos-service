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
package com.argosnotary.argos.service.roles;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.nodes.NodeService;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(value = 1)
public class PermissionCheckAdvisor {

    private final RoleAssignmentService roleAssignmentService;
    private final NodeService nodeService;

    @Pointcut("@annotation(permissionCheck)")
    public void permissionCheckPointCut(PermissionCheck permissionCheck) {}

    @Before(value = "permissionCheckPointCut(permissionCheck)", argNames = "joinPoint,permissionCheck")
    public void checkPermissions(JoinPoint joinPoint, PermissionCheck permissionCheck) {
        log.info("checking of method:{} with permissions {}",
                joinPoint.getSignature().getName(),
                permissionCheck.permissions()
        );
        
        Object[] args = joinPoint.getArgs();

		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication optionalAuthentication = securityContext.getAuthentication();
        Set<Permission> permissions = Set.of(permissionCheck.permissions());
        
        if (permissions.isEmpty() || args == null || args.length == 0 || optionalAuthentication == null) {
        	throw new AccessDeniedException("Access denied");
        }
        
        UUID resourceId = (UUID) args[0];
		Optional<Node> optNode = nodeService.findById(resourceId);
		if (optNode.isEmpty()) {
			throw new NotFoundException(String.format("Resource with id [%s] not found", resourceId));
		}

        if (!hasPermission(permissions, optNode.get())) {
            log.info("access denied for method:{} with permissions {}",
                    joinPoint.getSignature().getName(),
                    permissionCheck.permissions()
            );
            throw new AccessDeniedException("Access denied");
        }
    }

    private boolean hasPermission(Set<Permission> permissionsToCheck, Node node) {
    	Set<Permission> permissions = roleAssignmentService.findAllPermissionDownTree(node);
    	return permissions
    			.stream()
    			.anyMatch(permissionsToCheck::contains);
    }
}
