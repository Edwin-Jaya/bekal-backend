package org.edwin.bekal.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.CacheablePage;
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
import org.edwin.bekal.enums.LoanStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.edwin.bekal.common.util.LoanApplicationMapper.*;

@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final Set<Integer> ALLOWED_TENORS = Set.of(6, 8, 12, 16, 20, 24);

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final PlafondRepository plafondRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "customerLoanHistory",
            key = "#customerId.toString() + '_' + #page + '_' + #size"
    )
    public CacheablePage<LoanApplicationResponse> getLoanApplicationByCustomer(
            UUID customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return CacheablePage.from(
                loanApplicationRepository
                        .findByCustomer_Id(customerId, pageable)
                        .map(this::mapToResponse)
        );
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
    @CacheEvict(value = "customerLoanHistory", allEntries = true)
    public LoanApplicationResponse createLoanApplication(CreateLoanApplicationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomer().getId())
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        Plafond plafond = plafondRepository.findById(request.getPlafond().getId())
                .orElseThrow(() -> new IllegalArgumentException("Plafond produk tidak ditemukan"));

        Branch branch = branchRepository.findById(request.getBranch().getId())
                .orElseThrow(() -> new IllegalArgumentException("Branch tidak ditemukan"));

        // Validasi 0: Whitelist Pilihan Tenor
        if (!ALLOWED_TENORS.contains(request.getTenorMonths())) {
            throw new IllegalArgumentException("Tenor tidak valid. Pilihan tenor yang tersedia: 6, 8, 12, 16, 20, 24 bulan");
        }

        // Validasi 1: Cap Limit berdasarkan Credit Tier Customer
        CreditTier customerTier = customer.getCreditTier() != null ? customer.getCreditTier() : CreditTier.TIER_1;
        BigDecimal tierMaxCap = customerTier.getMaxCap();
        if (request.getAmountRequested().compareTo(tierMaxCap) > 0) {
            throw new IllegalArgumentException("Jumlah pengajuan melebihi batas maksimum Tier " + customerTier.name() + " (" + tierMaxCap + ")");
        }

        // Validasi 2: Sisa Plafond Produk
        BigDecimal availableAmount = plafond.getPlafondAmount().subtract(plafond.getUsedAmount());
        if (request.getAmountRequested().compareTo(availableAmount) > 0) {
            throw new IllegalArgumentException("Jumlah pengajuan melebihi sisa plafond produk");
        }

        // Validasi 3: Tenor Maksimum Produk Plafond
        if (request.getTenorMonths() > plafond.getMaxTenorMonths()) {
            throw new IllegalArgumentException("Tenor melebihi batas maksimum produk plafond (" + plafond.getMaxTenorMonths() + " bulan)");
        }

        // Kalkulasi Bunga dan Angsuran Bulanan
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

        // Buat dan Simpan Pengajuan Pinjaman
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
        application.setStatus(LoanStatus.IN_REVIEW.getValue());
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
                .branch(mapToBranchResponse(res.getBranch()))
                .customer(mapToCustomerResponse(res.getCustomer()))
                .plafond(mapToPlafondResponse(res.getPlafond()))
                .amountRequested(res.getAmountRequested())
                .tenorMonths(res.getTenorMonths())
                .purpose(res.getPurpose())
                .interestRate(res.getInterestRate())
                .monthlyInstallment(res.getMonthlyInstallment())
                .totalRepayment(res.getTotalRepayment())
                .status(res.getStatus())
                .assignedMarketing(mapToInternalUserResponse(res.getAssignedMarketing()))
                .assignedBranchManager(mapToInternalUserResponse(res.getAssignedBranchManager()))
                .submittedAt(res.getSubmittedAt())
                .build();
    }
}