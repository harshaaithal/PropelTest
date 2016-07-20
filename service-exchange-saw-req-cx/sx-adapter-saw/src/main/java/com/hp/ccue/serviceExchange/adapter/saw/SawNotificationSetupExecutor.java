package com.hp.ccue.serviceExchange.adapter.saw;

import com.hp.ccue.serviceExchange.adapter.DefaultNotificationSetupExecutor;
import org.springframework.stereotype.Component;

@Component
public class SawNotificationSetupExecutor extends DefaultNotificationSetupExecutor {
    public SawNotificationSetupExecutor() {
        super(SawConstants.SAW_TYPE, SawConstants.ENTITY_REQUEST);
    }
}
