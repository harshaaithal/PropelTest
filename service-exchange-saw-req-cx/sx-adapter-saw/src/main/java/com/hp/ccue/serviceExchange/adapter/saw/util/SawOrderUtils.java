package com.hp.ccue.serviceExchange.adapter.saw.util;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.hp.ccue.serviceExchange.SXConstants;
import com.hp.ccue.serviceExchange.adapter.saw.SawConstants;
import com.hp.ccue.serviceExchange.adapter.saw.SawOperationExecutor;
import com.hp.ccue.serviceExchange.aggregation.AggregationConstants;
import com.hp.ccue.serviceExchange.config.Configuration;
import com.hp.ccue.serviceExchange.message.MessageTransformer;
import com.hp.ccue.serviceExchange.message.builder.MessageConstants;
import com.hp.ccue.serviceExchange.utils.JsonUtils;
import com.hp.ccue.serviceExchange.utils.MessageUtils;
import com.hp.ccue.serviceExchange.utils.OrderUtilsBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Objects.firstNonNull;
import static com.hp.ccue.serviceExchange.SXConstants.SawInstancesCfg;
import static com.hp.ccue.serviceExchange.utils.JsonUtils.getField;

@Component
public class SawOrderUtils extends OrderUtilsBase {
    @Autowired
    private Configuration configuration;

    @Autowired
    private MessageTransformer messageTransformer;

    @Autowired
    public SawOrderUtils(SawOperationExecutor operationExecutor) {
        super(SawConstants.SAW_TYPE, operationExecutor, SawInstancesCfg.CFG_NAME);
    }

    @Override
    public Map<String, String> getInstances() {
        Map<String, Object> allInstanceConfigs = configuration.getConfiguration(getInstancesConfigPath());
        Map<String, Object> instanceConfigs = Maps.filterValues(allInstanceConfigs, new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                @SuppressWarnings("unchecked")
                Map<String, Object> instanceConfig = (Map<String, Object>) input;
                return firstNonNull(JsonUtils.getBooleanField(instanceConfig, SawInstancesCfg.R2F_ENABLED), false);
            }
        });
        return MessageUtils.instanceNames(instanceConfigs);
    }

    @Override
    protected Map<String, String> fetchCatalogItems(String targetInstance) {
        Map<String, Object> operationOutput = executeOperationForInstance(OPERATION_GET_CATALOG_ITEMS, targetInstance);
        return getField(operationOutput, MessageConstants.RESULT);
    }

    @Override
    protected List<String> fetchContacts(String targetInstance) {
        Map<String, Object> operationOutput = executeOperationForInstance(OPERATION_GET_CONTACTS, targetInstance);
        return getField(operationOutput, MessageConstants.RESULT);
    }

    @Override
    public String getCatalogItemForm(String targetInstance, String itemId, String authToken) throws Exception {
        Map<String, Object> offeringOperationOutput = executeOperationForInstance(AggregationConstants.OPERATION_GET_OFFERING, targetInstance,
                ImmutableMap.<String, Object>of("itemID", itemId));

        Map<String, Object> offering = getField(offeringOperationOutput, MessageConstants.RESULT);

        if (offering == null) {
            return "";
        }

        Map<String, Object> infrastructure = configuration.getConfiguration(SXConstants.INFRASTRUCTURE_CONFIG);

        if (Objects.equals(JsonUtils.getStrField(offering, "summary", "type"), "SUPPORT")) {
            List<Map<String, Object>> fields = JsonUtils.getField(offering, "form", "fields");
            fields.removeIf(field -> {
                final String id = JsonUtils.getStrField(field, "id");
                return Objects.equals(id, "name") || Objects.equals(id, "description");
            });
        }

        return messageTransformer.transformMessage(ImmutableMap.of(
                "offering", offering,
                "userAuthToken", authToken,
                "infrastructure", infrastructure
        ), "ui/optionsHtmlTransform.ftl");
    }
}
