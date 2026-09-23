package org.edwin.bekal.common.util;

import org.edwin.bekal.domain.application.dto.PlafondResponse;
import org.edwin.bekal.domain.application.entity.Plafond;
import org.edwin.bekal.domain.customer.dto.CustomerResponse;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.dto.BranchResponse;
import org.edwin.bekal.domain.master.dto.InternalUserResponse;
import org.edwin.bekal.domain.master.dto.RoleResponse;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.math.BigDecimal;

public final class LoanApplicationMapper {
    public static BranchResponse mapToBranchResponse(Branch branch) {
        if (branch == null) {
            return null;
        }
        return BranchResponse.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .branchAddress(branch.getBranchAddress())
                .branchCity(branch.getBranchCity())
                .branchStatus(branch.getBranchStatus() != null ? branch.getBranchStatus().name() : null)
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }

    public static CustomerResponse mapToCustomerResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerFullName(customer.getCustomerFullName())
                .customerAddress(customer.getCustomerAddress())
                .customerNik(customer.getCustomerNik())
                .customerEmail(customer.getCustomerEmail())
                .customerDateOfBirth(customer.getCustomerDateOfBirth())
                .customerGender(customer.getCustomerGender() != null ? customer.getCustomerGender().name() : null)
                .customerPhoneNumber(customer.getCustomerPhoneNumber())
                .customerStatus(customer.getCustomerStatus() != null ? customer.getCustomerStatus().name() : null)
                .customerPasswordHash(customer.getCustomerPasswordHash())
                .customerLastLoginAt(customer.getCustomerLastLoginAt())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public static PlafondResponse mapToPlafondResponse(Plafond plafond) {
        if (plafond == null) {
            return null;
        }
        BigDecimal availableAmount = plafond.getPlafondAmount() != null && plafond.getUsedAmount() != null
                ? plafond.getPlafondAmount().subtract(plafond.getUsedAmount())
                : BigDecimal.ZERO;
        if (availableAmount.compareTo(BigDecimal.ZERO) < 0) {
            availableAmount = BigDecimal.ZERO;
        }

        return PlafondResponse.builder()
                .id(plafond.getId())
                .plafondAmount(plafond.getPlafondAmount())
                .usedAmount(plafond.getUsedAmount())
                .availableAmount(availableAmount)
                .interestRate(plafond.getInterestRate())
                .maxTenorMonths(plafond.getMaxTenorMonths())
                .status(plafond.getStatus())
                .creditTier(plafond.getCustomer() != null && plafond.getCustomer().getCreditTier() != null
                        ? plafond.getCustomer().getCreditTier().name()
                        : null)
                .validFrom(plafond.getValidFrom())
                .validUntil(plafond.getValidUntil())
                .build();
    }

    public static InternalUserResponse mapToInternalUserResponse(InternalUser user) {
        if (user == null) {
            return null;
        }
        var result = InternalUserResponse.builder()
                .id(user.getId())
                .internalUserEmployeeCode(user.getInternalUserEmployeeCode())
                .internalUserFullName(user.getInternalUserFullName())
                .internalUserEmail(user.getInternalUserEmail())
                .internalUserPasswordHash(user.getInternalUserPasswordHash())
                .internalUserPhoneNumber(user.getInternalUserPhoneNumber())
                .internalUserIsActive(user.getInternalUserIsActive() != null ? user.getInternalUserIsActive() : true)
                .internalUserLastLoginAt(user.getInternalUserLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getRole() != null) {
            result = result.role(RoleResponse.builder()
                    .id(user.getRole().getId())
                    .roleName(user.getRole().getRoleName())
                    .roleDescription(user.getRole().getRoleDescription())
                    .roleIsActive(user.getRole().getRoleIsActive())
                    .createdAt(user.getRole().getCreatedAt())
                    .updatedAt(user.getRole().getUpdatedAt())
                    .build());
        }

        if (user.getBranch() != null) {
            result = result.branch(mapToBranchResponse(user.getBranch()));
        }

        return result.build();
    }
}
