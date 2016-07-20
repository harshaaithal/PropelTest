package com.hp.ccue.serviceExchange.adapter.saw;

import com.hp.ccue.serviceExchange.adapter.polling.AggregationPollingCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.hp.ccue.serviceExchange.SXConstants.SawInstancesCfg;

@Component
public class SawAggregationPollingCommand extends AggregationPollingCommand {

    @Value("${adapter.saw.change.listener.delayBeforeNextRun}")
    private int pollInterval;

    /**
     * Operation for fetching changed offerings.
     */
    public static final String OPERATION_GET_CHANGES = "getChangedOfferings";
    @Autowired
    public SawAggregationPollingCommand(SawOperationExecutor operationExecutor) {
        super(SawConstants.SAW_TYPE, SawConstants.ENTITY_OFFERING, SawInstancesCfg.CFG_NAME, OPERATION_GET_CHANGES, operationExecutor);
    }

    @Override
    protected long getInitialLastUpdatedTime() {
        return new Date().getTime() - pollInterval * 1000L; // moving one period back so as not to miss changes during startup
    }

}
