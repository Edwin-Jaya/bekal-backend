package org.edwin.bekal.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.CreateLoanApplicationRequest;
import org.edwin.bekal.domain.application.dto.LoanApplicationResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.Plafond;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.PlafondRepository;
import org.edwin.bekal.domain.application.service.LoanApplicationService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.enums.CreditTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final PlafondRepository plafondRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getLoanApplicationByCustomer(UUID customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<LoanApplication> loanApplicationPage = loanApplicationRepository.findByCustomer_Id(customerId, pageable);
        return loanApplicationPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public Page<LoanApplicationResponse> getLoanApplication(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<LoanApplication> loanApplicationPage = loanApplicationRepository.findAll(pageable);
        return loanApplicationPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getPendingLoanApplication(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<LoanApplication> loanApplicationPage = loanApplicationRepository.findPendingMarketingReviews(pageable);
        return loanApplicationPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getPendingLoanApplicationApproval(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<LoanApplication> loanApplicationPage = loanApplicationRepository.findPendingBranchMarketingApproval(pageable);
        return loanApplicationPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getPendingLoanApplicationDisbursement(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<LoanApplication> loanApplicationPage = loanApplicationRepository.findPendingBackOfficeDisbursement(pageable);
        return loanApplicationPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public LoanApplicationResponse createLoanApplication(CreateLoanApplicationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomer().getId())
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        Plafond plafond = plafondRepository.findById(request.getPlafond().getId())
                .orElseThrow(() -> new IllegalArgumentException("Plafond tidak ditemukan"));

        Branch branch = branchRepository.findById(request.getBranch().getId())
                .orElseThrow(() -> new IllegalArgumentException("Branch tidak ditemukan"));

        // Validasi 1: Cap Limit berdasarkan Credit Tier Customer
        CreditTier customerTier = customer.getCreditTier() != null ? customer.getCreditTier() : CreditTier.TIER_1;
        BigDecimal tierMaxCap = customerTier.getMaxCap();
        if (request.getAmountRequested().compareTo(tierMaxCap) > 0) {
            throw new IllegalArgumentException("Jumlah pengajuan melebihi batas maksimum Tier " + customerTier.name() + " (" + tierMaxCap + ")");
        }

        // Validasi 2: Sisa Plafond Produk
        BigDecimal availableAmount = plafond.getPlafondAmount().subtract(plafond.getUsedAmount());
        if (request.getAmountRequested().compareTo(availableAmount) > 0) {
            throw new IllegalArgumentException("Jumlah pengajuan melebihi sisa plafond");
        }

        // Validasi 3: Tenor Maksimum
        if (request.getTenorMonths() > plafond.getMaxTenorMonths()) {
            throw new IllegalArgumentException("Tenor melebihi batas maksimum plafond");
        }

        BigDecimal interestRate = plafond.getInterestRate();
        BigDecimal totalInterest = request.getAmountRequested()
                .multiply(interestRate)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(request.getTenorMonths()))
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal totalRepayment = request.getAmountRequested()
                .add(totalInterest)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal monthlyInstallment = totalRepayment
                .divide(BigDecimal.valueOf(request.getTenorMonths()), 2, RoundingMode.HALF_UP);

        LoanApplication application = new LoanApplication();
        application.setApplicationNumber(generateApplicationNumber());
        application.setBranch(branch);
        application.setCustomer(customer);
        application.setPlafond(plafond);
        application.setAmountRequested(request.getAmountRequested());
        application.setTenorMonths(request.getTenorMonths());
        application.setPurpose(request.getPurpose());
        application.setInterestRate(interestRate);
        application.setMonthlyInstallment(monthlyInstallment);
        application.setTotalRepayment(totalRepayment);
        application.setStatus("submitted");
        application.setSubmittedAt(Instant.now());

        LoanApplication saved = loanApplicationRepository.save(application);
        return mapToResponse(saved);
    }

    private String generateApplicationNumber() {
        return "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public LoanApplicationResponse mapToResponse(LoanApplication res) {
        return LoanApplicationResponse.builder()
                .id(res.getId())
                .applicationNumber(res.getApplicationNumber())
                .branch(res.getBranch())
                .customer(res.getCustomer())
                .plafond(res.getPlafond())
                .amountRequested(res.getAmountRequested())
                .tenorMonths(res.getTenorMonths())
                .purpose(res.getPurpose())
                .interestRate(res.getInterestRate())
                .monthlyInstallment(res.getMonthlyInstallment())
                .totalRepayment(res.getTotalRepayment())
                .status(res.getStatus())
                .assignedMarketing(res.getAssignedMarketing())
                .assignedBranchManager(res.getAssignedBranchManager())
                .submittedAt(res.getSubmittedAt())
                .build();
    }
}