package com.hp.ccue.serviceExchange.adapter.saw.caseex;

import com.hp.ccue.serviceExchange.adapter.saw.SawCaseExchangeRuleStore;
import com.hp.ccue.serviceExchange.adapter.saw.SawConstants;
import com.hp.ccue.serviceExchange.caseex.AbstractCaseExchangeAdapter;
import com.hp.ccue.serviceExchange.content.ContentStorageApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Case exchange adapter for SAW adapter
 */
@Component
public class SawCaseExchangeAdapter extends AbstractCaseExchangeAdapter {

    @Autowired
    protected SawCaseExchangeAdapter(SawCaseExchangeRuleStore sawCaseExchangeRuleStore) {
        super(SawConstants.SAW_TYPE, sawCaseExchangeRuleStore);
    }

    @Override
    protected Map<String, String> loadCanonicalToBackendEntityTypeMapping() {
        return loadCanonicalToBackendEntityTypeMapping("saw-case-exchange/saw-mappings", "entityType");
    }

}
