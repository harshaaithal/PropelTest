package com.hp.ccue.serviceExchange.adapter.saw;

import com.hp.ccue.serviceExchange.adapter.messages.EntityChangeMsg;
import com.hp.ccue.serviceExchange.adapter.provided.AdapterAbstract;
import com.hp.ccue.serviceExchange.adapter.saw.caseex.SawCaseExchangeAdapter;
import com.hp.ccue.serviceExchange.adapter.saw.healthcheck.SawInstanceHealthCheckFactory;
import com.hp.ccue.serviceExchange.amqp.MessageSubType;
import com.hp.ccue.serviceExchange.change.ChangeReasonCategory;
import com.hp.ccue.serviceExchange.message.builder.MessageConstants;
import com.hp.ccue.serviceExchange.utils.MessageUtils;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;

import static com.hp.ccue.serviceExchange.SXConstants.SawInstancesCfg;
import static com.hp.ccue.serviceExchange.utils.JsonUtils.getField;
import static com.hp.ccue.serviceExchange.utils.JsonUtils.getStrField;

@Component
public class SawAdapter extends AdapterAbstract {

    @Autowired
    public SawAdapter(SawOperationExecutor executor, SawPipelineBuilder builder,
                      SawInstanceHealthCheckFactory healthCheckFactory,
                      SawChangeObserver changeObserver,
                      SawCaseExchangeAdapter sawCaseExchangeAdapter) {
        super(SawConstants.SAW_TYPE, executor, builder, healthCheckFactory, sawCaseExchangeAdapter);
        setRequestMessageHeaderTemplate("saw-r2f/sx/templates/generateMessageHeader.ftl");
        setChangeObserver(changeObserver);
    }

    @Override
   	protected String getPipelineNameForMessage(MessageProperties properties, Map<String, Object> amqpMessage) {
   		final String subType = extractMessageSubtype(properties.getType());
   		if (MessageSubType.CHANGE.equals(subType)) {
            final String reasonCategory = getStrField(amqpMessage, EntityChangeMsg.REASON_CATEGORY);
            switch (reasonCategory) {
                case ChangeReasonCategory.REASON_CATEGORY_SX_MANAGED:
                    return SawConstants.PIPELINE_SX_MANAGED_CHANGE;
                case ChangeReasonCategory.REASON_CATEGORY_AGGREGATION:
                    return SawConstants.PIPELINE_AGGREGATION_CHANGE;
                default:
                    throw new IllegalArgumentException();
            }
        }
   		return super.getPipelineNameForMessage(properties, amqpMessage);
   	}
	
    @Override
    public void decorateRequestMessage(Map<String, Object> message) {
        super.decorateRequestMessage(message);

        Map<String, Object> messageHeader = getField(message, MessageConstants.MESSAGE_HEADER);
        Map<String, Object> instances = configuration.getConfiguration(SawInstancesCfg.CFG_NAME);
        MessageUtils.fixTargetInstanceInMessageHeader(messageHeader, instances);
    }

}
