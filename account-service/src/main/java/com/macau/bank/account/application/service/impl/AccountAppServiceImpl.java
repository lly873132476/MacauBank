package com.macau.bank.account.application.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.account.application.assembler.AccountDomainAssembler;
import com.macau.bank.account.application.assembler.TransactionFlowDomainAssembler;
import com.macau.bank.account.application.command.*;
import com.macau.bank.account.application.query.AccountListQuery;
import com.macau.bank.account.application.query.AccountSummaryQuery;
import com.macau.bank.account.application.query.AssetSummaryQuery;
import com.macau.bank.account.application.query.TransactionFlowQuery;
import com.macau.bank.account.application.result.AccountInfoResult;
import com.macau.bank.account.application.result.AssetSummaryResult;
import com.macau.bank.account.application.result.TransactionFlowResult;
import com.macau.bank.account.application.service.AccountAppService;
import com.macau.bank.account.common.result.AccountErrorCode;
import com.macau.bank.account.domain.entity.AccountBalance;
import com.macau.bank.account.domain.entity.AccountInfo;
import com.macau.bank.account.domain.entity.AccountSubLedger;
import com.macau.bank.account.domain.model.BalanceAdjustment;
import com.macau.bank.account.domain.model.UserAssetView;
import com.macau.bank.account.domain.service.AccountDomainService;
import com.macau.bank.account.domain.service.AccountBalanceDomainService;
import com.macau.bank.api.currency.service.CurrencyRpcService;
import com.macau.bank.api.user.response.UserInfoRpcResponse;
import com.macau.bank.api.user.service.UserRpcService;
import com.macau.bank.common.core.domain.Money;
import com.macau.bank.common.core.enums.BizType;
import com.macau.bank.common.core.enums.Currency;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.common.core.util.IdGenerator;
import com.macau.bank.common.framework.lock.annotation.RedissonLock;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.macau.bank.account.infra.tcc.TccProtectionService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountAppServiceImpl implements AccountAppService {

    @Resource
    private AccountDomainService accountDomainService;

    @Resource
    private AccountBalanceDomainService accountBalanceDomainService;

    @Resource
    private TccProtectionService tccProtectionService;

    @DubboReference
    private CurrencyRpcService currencyRpcService;

    @DubboReference
    private UserRpcService userRpcService;

    @Resource
    private AccountDomainAssembler accountDomainAssembler;

    @Resource
    private TransactionFlowDomainAssembler transactionFlowDomainAssembler;

    /**
     * 管理员密钥 (从配置文件读取，支持环境变量覆盖)
     */
    @Value("${admin.secret}")
    private String adminSecret;

    @Override
    public Page<TransactionFlowResult> getTransactionFlows(TransactionFlowQuery query) {
        log.info("应用服务 - 分页查询流水: query={}", query);

        IPage<AccountSubLedger> page = accountDomainService.pageSubLedgers(
                query.getUserNo(),
                query.getAccountNo(),
                query.getCurrencyCode(),
                query.getStartDate(),
                query.getEndDate(),
                query.getDirection(),
                query.getBizType(),
                query.getPage(),
                query.getPageSize());

        // 使用 MP 的 convert 方法进行转换，或者手动构建 Page
        Page<TransactionFlowResult> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(transactionFlowDomainAssembler.toResultList(page.getRecords()));

        return resultPage;
    }

    @Override
    public AssetSummaryResult getAssetSummary(AssetSummaryQuery query) {
        String userNo = query.getUserNo();
        log.info("获取资产总览: userNo={}", userNo);

        List<AccountInfo> accounts = accountDomainService.getAccountInfosByUserNo(userNo);
        if (accounts == null || accounts.isEmpty()) {
            return AssetSummaryResult.builder().totalMopValue(BigDecimal.ZERO).accounts(new ArrayList<>()).build();
        }
        List<AccountBalance> balances = accountDomainService.getBalancesByUserNo(userNo);

        Map<String, BigDecimal> rateMap = fetchExchangeRates(balances);
        UserAssetView assetView = accountDomainService.buildUserAssetView(accounts, balances, rateMap);

        return AssetSummaryResult.builder()
                .totalMopValue(assetView.getTotalMopValue())
                .accounts(buildAccountInfoResults(assetView))
                .build();
    }

    @Override
    public List<AccountInfoResult> getAccountList(AccountListQuery query) {
        String userNo = query.getUserNo();
        log.info("获取账户列表: userNo={}", userNo);

        List<AccountInfo> accounts = accountDomainService.getAccountInfosByUserNo(userNo);
        if (accounts == null || accounts.isEmpty()) {
            return new ArrayList<>();
        }
        List<AccountBalance> balances = accountDomainService.getBalancesByUserNo(userNo);
        UserAssetView assetView = accountDomainService.buildUserAssetView(accounts, balances, new HashMap<>());

        return buildAccountInfoResults(assetView);
    }

    @Override
    public AccountInfoResult getAccountSummary(AccountSummaryQuery query) {
        String accountNo = query.getAccountNo();
        log.info("获取账户汇总信息: accountNo={}", accountNo);

        AccountInfo accountInfo = accountDomainService.getAccountInfoByAccountNo(accountNo);
        if (accountInfo == null) {
            return null;
        }

        List<AccountBalance> balances = accountDomainService.getAccountBalancesByAccountNo(accountNo);
        AccountInfoResult result = accountDomainAssembler.toResult(accountInfo);
        result.setBalances(accountDomainAssembler.toBalanceList(balances));

        UserInfoRpcResponse user = userRpcService.getUserByUserNo(accountInfo.getUserNo());
        result.setAccountName(user.getRealNameCn() + "|" + user.getRealNameEn());

        return result;
    }

    private List<AccountInfoResult> buildAccountInfoResults(UserAssetView assetView) {
        return assetView.getAccounts().stream()
                .map(info -> {
                    AccountInfoResult result = accountDomainAssembler.toResult(info);
                    List<AccountBalance> balances = assetView.getBalanceMap().getOrDefault(info.getAccountNo(),
                            new ArrayList<>());
                    result.setBalances(accountDomainAssembler.toBalanceList(balances));
                    return result;
                })
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> fetchExchangeRates(List<AccountBalance> balances) {
        List<String> currencies = balances.stream()
                .map(AccountBalance::getCurrencyCode)
                .filter(c -> !Currency.MOP.getCode().equalsIgnoreCase(c))
                .distinct()
                .toList();

        Map<String, BigDecimal> rateMap = new HashMap<>();
        rateMap.put(Currency.MOP.getCode(), BigDecimal.ONE);

        if (!currencies.isEmpty()) {
            try {
                Map<String, BigDecimal> batchRates = currencyRpcService.getBatchExchangeRates(currencies,
                        Currency.MOP.getCode());
                if (batchRates != null) {
                    rateMap.putAll(batchRates);
                }
            } catch (Exception e) {
                throw new BusinessException("资产系统维护中");
            }
        }
        return rateMap;
    }

    @Override
    public BigDecimal getAvailableBalance(String accountNo, String currencyCode) {
        AccountBalance accountBalance = accountDomainService.getAccountBalance(accountNo, currencyCode);
        return accountBalance != null ? accountBalance.getAvailableBalance() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalBalance(String accountNo, String currencyCode) {
        AccountBalance accountBalance = accountDomainService.getAccountBalance(accountNo, currencyCode);
        // 修复 Bug: 应返回总余额(balance)而非可用余额(availableBalance)
        return accountBalance != null ? accountBalance.getBalance() : BigDecimal.ZERO;
    }

    @Override
    public String getAccountNoByUserNoAndCurrency(String userNo, String currencyCode) {
        return accountDomainService.findAccountNoByUserAndCurrency(userNo, currencyCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAccount(CreateAccountCmd cmd) {
        Money initialMoney = Money.of(
                cmd.getInitialBalance() != null ? cmd.getInitialBalance() : BigDecimal.ZERO,
                cmd.getInitialCurrencyCode());
        return accountDomainService.createAccount(cmd.getUserNo(), cmd.getAccountType(), initialMoney);
    }

    @Override
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "'lock:account:' + #cmd.accountNo")
    public boolean adjustBalance(AdjustBalanceCmd cmd) {
        BalanceAdjustment adjustment = accountDomainAssembler.toBalanceAdjustment(cmd);
        return accountBalanceDomainService.adjustBalance(adjustment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "'lock:account:' + #cmd.accountNo")
    public boolean debit(DebitCmd cmd) {
        // 1. 校验金额必须为正数
        if (cmd.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(AccountErrorCode.INVALID_AMOUNT);
        }

        // 2. 组装 BalanceAdjustment (扣款为负数)
        BalanceAdjustment adjustment = BalanceAdjustment.builder()
                .accountNo(cmd.getAccountNo())
                .amount(Money.of(cmd.getAmount().negate(), cmd.getCurrencyCode()))
                .description(cmd.getDescription())
                .bizNo(cmd.getBizNo())
                .requestId(cmd.getRequestId())
                .bizType(cmd.getBizType() != null ? cmd.getBizType() : BizType.TRANSFER_OUT) // 默认为转出
                .build();

        // 3. 调用 Domain 层
        return accountBalanceDomainService.adjustBalance(adjustment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "'lock:account:' + #cmd.accountNo")
    public boolean credit(CreditCmd cmd) {
        // 1. 校验金额必须为正数
        if (cmd.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(AccountErrorCode.INVALID_AMOUNT);
        }

        // 2. 组装 BalanceAdjustment (入账为正数)
        BalanceAdjustment adjustment = BalanceAdjustment.builder()
                .accountNo(cmd.getAccountNo())
                .amount(Money.of(cmd.getAmount(), cmd.getCurrencyCode()))
                .description(cmd.getDescription())
                .bizNo(cmd.getBizNo())
                .requestId(cmd.getRequestId())
                .bizType(cmd.getBizType() != null ? cmd.getBizType() : BizType.TRANSFER_IN) // 默认为转入
                .build();

        // 3. 调用 Domain 层
        return accountBalanceDomainService.adjustBalance(adjustment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeBalance(FreezeBalanceCmd cmd) {
        // 1. TCC 协议检查：悬挂检测
        if (!tccProtectionService.checkTryAllowed(cmd.getFlowNo())) {
            return false;
        }

        // 2. 调用领域服务（纯业务逻辑）
        Money amount = Money.of(cmd.getAmount(), cmd.getCurrencyCode());
        return accountBalanceDomainService.freezeBalance(cmd.getAccountNo(), amount,
                cmd.getFlowNo(), cmd.getFreezeType(), cmd.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeBalance(UnfreezeBalanceCmd cmd) {
        Money amount = Money.of(cmd.getAmount(), cmd.getCurrencyCode());

        // 1. TCC 空回滚检测
        if (!tccProtectionService.handleEmptyRollback(cmd.getFlowNo(), cmd.getAccountNo(), amount)) {
            return true; // 空回滚已处理，直接返回成功
        }

        // 2. 调用领域服务（纯业务逻辑）
        return accountBalanceDomainService.unfreezeBalance(cmd.getAccountNo(), amount,
                cmd.getFlowNo(), cmd.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeAndDebit(UnfreezeAndDebitCmd cmd) {
        Money amount = Money.of(cmd.getAmount(), cmd.getCurrencyCode());
        return accountBalanceDomainService.unfreezeAndDebit(cmd.getAccountNo(), amount,
                cmd.getFlowNo(), cmd.getReason(), cmd.getBizType(), cmd.getRequestId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminDeposit(AdminDepositCmd cmd) {
        // 使用配置化的管理员密钥进行校验
        if (!adminSecret.equals(cmd.getAdminSecret())) {
            log.warn("管理员密钥校验失败");
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }
        String remark = cmd.getRemark() != null ? cmd.getRemark() : "后台管理员充值";
        // 传递 requestId 用于幂等性校验

        AccountBalance accountBalance = accountDomainService.getAccountBalance(cmd.getUserNo(),
                cmd.getCurrencyCode());
        if (accountBalance == null) {
            throw new BusinessException(AccountErrorCode.BALANCE_RECORD_NOT_FOUND);
        }
        // 后台充值业务单号
        String bizNo = "AD_DP_" + IdGenerator.generateId();

        BalanceAdjustment adjustment = accountDomainAssembler.toBalanceAdjustment(
                AdjustBalanceCmd.builder()
                        .accountNo(accountBalance.getAccountNo())
                        .currencyCode(cmd.getCurrencyCode())
                        .amount(cmd.getAmount())
                        .description(remark)
                        .bizNo(bizNo)
                        .requestId(cmd.getRequestId())
                        .build());
        return accountBalanceDomainService.adjustBalance(adjustment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean openCurrencyAccount(AdminOpenCurrencyAccountCmd cmd) {
        // 使用配置化的管理员密钥进行校验
        if (!adminSecret.equals(cmd.getAdminSecret())) {
            log.warn("管理员密钥校验失败");
            throw new BusinessException(AccountErrorCode.INVALID_OPERATION);
        }
        accountDomainService.openCurrencyAccount(cmd.getAccountNo(), cmd.getCurrencyCode());
        return true;
    }

    @Override
    public boolean validateAccountOwnership(String accountNo, String userNo) {
        if (accountNo == null || userNo == null) {
            return false;
        }
        AccountInfo accountInfo = accountDomainService.getAccountInfoByAccountNo(accountNo);
        if (accountInfo == null) {
            return false;
        }
        return userNo.equals(accountInfo.getUserNo());
    }
}