package com.hp.ccue.serviceExchange.adapter.saw;

import com.google.common.base.Objects;
import com.hp.ccue.serviceExchange.adapter.polling.R2fPollingCommand;
import com.hp.ccue.serviceExchange.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.hp.ccue.serviceExchange.SXConstants.SawInstancesCfg;
import static com.hp.ccue.serviceExchange.utils.JsonUtils.getField;
import static com.hp.ccue.serviceExchange.utils.JsonUtils.getStrField;

@Component
public class SawR2fPollingCommand extends R2fPollingCommand {

    private static final String KEY_ENTITY_ID = "entityId";
    private static final String KEY_REQUEST_TYPE = "requestType";
    private static final String KEY_ENTITIES = "entities";

    @Value("${adapter.saw.change.listener.delayBeforeNextRun}")
    private int pollInterval;

    /**
     * Operation for fetching changed requests.
     */
    public static final String OPERATION_GET_CHANGES = "getChangedRequests";
    @Autowired
    public SawR2fPollingCommand(SawOperationExecutor operationExecutor) {
        super(SawConstants.SAW_TYPE, SawConstants.ENTITY_REQUEST, SawInstancesCfg.CFG_NAME, OPERATION_GET_CHANGES, operationExecutor);
    }

    @Override
    protected long getInitialLastUpdatedTime() {
        return new Date().getTime() - pollInterval * 1000L; // moving one period back so as not to miss changes during startup
    }

    @Nonnull
    @Override
    protected List<Map<String, Object>> extractChangedEntities(Map<String, Object> operationResult) {
        return getField(operationResult, KEY_ENTITIES);
    }

    @Nonnull
    @Override
    protected String extractEntityId(Map<String, Object> entity) {
        return getStrField(entity, KEY_ENTITY_ID);
    }

    @Nonnull
    @Override
    protected String extractRequestType(Map<String, Object> entity) {
        return getStrField(entity, KEY_REQUEST_TYPE);
    }

    @Override
    protected boolean isInterested(Map<String, Object> instanceConfig) {
        return Objects.firstNonNull(JsonUtils.getBooleanField(instanceConfig, SawInstancesCfg.R2F_ENABLED), false)
                || Objects.firstNonNull(JsonUtils.getBooleanField(instanceConfig, SawInstancesCfg.TICKETING_ENABLED), false);
    }
}
