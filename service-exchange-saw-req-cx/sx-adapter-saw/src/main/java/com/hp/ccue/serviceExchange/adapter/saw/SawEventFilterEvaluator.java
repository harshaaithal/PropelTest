package com.hp.ccue.serviceExchange.adapter.saw;

import java.util.Map;

import org.springframework.stereotype.Component;
import com.hp.ccue.serviceExchange.adapter.caseex.eval.EventFilterEvaluator;

import static com.hp.ccue.serviceExchange.utils.JsonUtils.getField;

@Component
public class SawEventFilterEvaluator extends EventFilterEvaluator {

	private static final String PROPERTIES = "properties";
	private static final String EXT_PROPERTIES = "ext_properties";
	private static final String OPERATION = "Operation";

	@Override
	protected Map<String, Object> extractProperties(Map<String, Object> entityJson) {
		return getField(entityJson, PROPERTIES);
	}

	@Override
	protected String extractOperation(Map<String, Object> entityJson) {
		return getField(entityJson, EXT_PROPERTIES, OPERATION);
	}
}
