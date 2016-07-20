package com.hp.ccue.serviceExchange.adapter.saw;

import com.hp.ccue.serviceExchange.adapter.caseex.MemoryCaseExchangeRuleStore;
import org.springframework.stereotype.Component;

/**
 * SAW case exchange in memory rule store serves also as Case Exchange rule registrator.
 * It does not install any callbacks or actionable code into SAW.
 *
 * This rule store is shared between:
 * - poll as real CX rule store (see {@link SawCxPollingCommand})
 * - {@link com.hp.ccue.serviceExchange.adapter.saw.caseex.SawCaseExchangeAdapter}
 *          as case exchange rule registrator
 */
@Component
public class SawCaseExchangeRuleStore extends MemoryCaseExchangeRuleStore {
}
