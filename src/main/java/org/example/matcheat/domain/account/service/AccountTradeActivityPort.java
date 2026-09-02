package org.example.matcheat.domain.account.service;

/** Account-domain port for checking whether withdrawal would interrupt a trade. */
public interface AccountTradeActivityPort {
    boolean hasActiveTrade(long userId);
}
