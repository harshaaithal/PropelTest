package com.hp.ccue.serviceExchange.adapter.saw;

import com.google.common.base.Objects;
import com.hp.ccue.serviceExchange.SXConstants;
import com.hp.ccue.serviceExchange.adapter.polling.CxPollingByAliasCommand;
import com.hp.ccue.serviceExchange.caseex.CaseExchangeConstants;
import com.hp.ccue.serviceExchange.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.ccue.serviceExchange.caseex.CaseExchangeConstants.CaseExchangeMsg.EXTERNAL_INSTANCE_ALIAS;
import static com.hp.ccue.serviceExchange.caseex.CaseExchangeConstants.CaseExchangeMsg.RESPONSE;

@Component
public class SawCxPollingCommand extends CxPollingByAliasCommand {

    private static final String ID = "Id";
    private static final String PROPERTIES = "properties";
    private static final String ENTITIES = "entities";
    public static final String OPERATION_GET_CHANGES = "getChangedIncidents";

    @Value("${adapter.saw.change.listener.delayBeforeNextRun}")
    private int pollInterval;

    @Autowired
    public SawCxPollingCommand(SawOperationExecutor operationExecutor,
                               SawCaseExchangeRuleStore sawCxRuleStore,
                               SawEventFilterEvaluator eventFilterEvaluator) {
        super(SawConstants.SAW_TYPE, SawConstants.ENTITY_INCIDENT, SXConstants.SawInstancesCfg.CFG_NAME, OPERATION_GET_CHANGES,
                operationExecutor, sawCxRuleStore, eventFilterEvaluator);
    }

    @Override
    protected void customizeGetChangedEntitiesMessage(Map<String, Object> message, String targetInstance, Map<String, Object> externalSystemConfig,
                                                      Long lastUpdateTime, String externalSystemAlias) {
        // the produced message will contain: messageHeader.targetInstance, sawConfig, externalSystemConfig, lastUpdateTime
        message.put(CaseExchangeConstants.CaseExchangeMsg.EXTERNAL_SYSTEM_CONFIG, externalSystemConfig);
    }

    @Override
    protected long getInitialLastUpdatedTime() {
        return new Date().getTime() - pollInterval * 1000L; // moving one period back so as not to miss changes during startup
    }

    @Nonnull
    @Override
    protected String extractEntityId(Map<String, Object> entity) {
        final Map<String, Object> properties = JsonUtils.getSubMap(entity, PROPERTIES);
        return JsonUtils.getStrField(properties, ID);
    }

    @Override
    protected Map<String, Object> prepareMessageCustomData(Map<String, Object> entity, String externalInstanceAlias) {
        final Map<String, Object> data = new HashMap<>();
        data.put(EXTERNAL_INSTANCE_ALIAS, externalInstanceAlias);
        return data;
    }

    @Override
    @Nonnull
    protected List<Map<String, Object>> extractChangedEntities(Map<String, Object> changedEntities) {
        List<Map<String, Object>> result = JsonUtils.getField(changedEntities, ENTITIES);
        return Objects.firstNonNull(result, Collections.<Map<String, Object>>emptyList());
    }
}
