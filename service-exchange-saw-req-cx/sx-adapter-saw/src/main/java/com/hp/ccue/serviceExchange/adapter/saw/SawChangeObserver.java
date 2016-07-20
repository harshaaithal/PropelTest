package com.hp.ccue.serviceExchange.adapter.saw;

import com.google.common.collect.ImmutableList;
import com.hp.ccue.serviceExchange.adapter.polling.CompositeChangeObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SawChangeObserver extends CompositeChangeObserver {

    @Value("${adapter.saw.change.listener.delayBeforeNextRun}")
    private int pollInterval;

    @Autowired
    public SawChangeObserver(SawSrCxPollingCommand SrCxPollingCommand, SawCxPollingCommand cxPollingCommand, SawR2fPollingCommand r2fPollingCommand,
                             SawAggregationPollingCommand aggregationPollingCommand) {
        super(ImmutableList.<Runnable>of(SrCxPollingCommand, cxPollingCommand, r2fPollingCommand, aggregationPollingCommand));
    }

    @Override
    public int getPollIntervalSec() {
        return pollInterval;
    }
}
