package org.edwin.bekal.domain.application.repository;

import org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse;
import org.edwin.bekal.domain.application.entity.RoleMenuAccess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleMenuAccessRepository extends JpaRepository<RoleMenuAccess, UUID> {

    Optional<RoleMenuAccess> findByRoleIdAndMenuId(UUID roleId, UUID menuId);

    @Query("SELECT rma FROM RoleMenuAccess rma " +
            "WHERE rma.role.id IN :roleIds " +
            "AND rma.menu.menuIsActive = true " +
            "ORDER BY rma.menu.menuSortOrder ASC")
    List<RoleMenuAccess> findActiveMenusByRoleIds(@Param("roleIds") List<UUID> roleIds);

    void deleteByRoleId(UUID roleId);

    boolean existsByRoleIdAndMenuId(UUID roleId, UUID menuId);

    Page<RoleMenuAccess> findByRoleId(UUID roleId, Pageable pageable);

    List<RoleMenuAccess> findByRoleIdAndMenuIdIn(UUID roleId, List<UUID> menuIds);

    @Query(
            value = "SELECT new org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse(" +
                    "m.id, m.menuName, " +
                    "COALESCE(rma.roleMenuCanView, false), " +
                    "COALESCE(rma.roleMenuCanCreate, false), " +
                    "COALESCE(rma.roleMenuCanEdit, false), " +
                    "COALESCE(rma.roleMenuCanDelete, false), " +
                    "COALESCE(rma.roleMenuCanApprove, false)) " +
                    "FROM Menu m " +
                    "LEFT JOIN RoleMenuAccess rma ON rma.menu = m AND rma.role.id = :roleId",
            countQuery = "SELECT COUNT(m) FROM Menu m"
    )
    Page<RoleMenuAccessResponse> findMatrixByRoleId(@Param("roleId") UUID roleId, Pageable pageable);

    @Query("""
        SELECT rma FROM RoleMenuAccess rma
        JOIN FETCH rma.menu m
        WHERE rma.role.id = :roleId
          AND m.menuIsActive = true
          AND rma.roleMenuCanView = true
        ORDER BY m.menuSortOrder ASC
    """)
    List<RoleMenuAccess> findAllowedMenusByRoleId(@Param("roleId") UUID roleId);
}