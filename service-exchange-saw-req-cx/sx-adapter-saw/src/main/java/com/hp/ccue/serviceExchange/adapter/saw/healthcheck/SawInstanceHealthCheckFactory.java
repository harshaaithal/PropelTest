package com.hp.ccue.serviceExchange.adapter.saw.healthcheck;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.hp.ccue.serviceExchange.SXConstants;
import com.hp.ccue.serviceExchange.adapter.monitoring.ConfigurationCheck;
import com.hp.ccue.serviceExchange.adapter.monitoring.InstanceHealthCheck;
import com.hp.ccue.serviceExchange.adapter.monitoring.InstanceHealthChecksFactory;
import com.hp.ccue.serviceExchange.adapter.saw.SawConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SawInstanceHealthCheckFactory implements InstanceHealthChecksFactory {
    @Value("${skipCertificateValidation}")
    private Boolean skipCertificateValidationObj;

    @Override
    public Optional<InstanceHealthCheck> buildInstanceHealthCheck(Map<String, Object> instanceConfiguration, SXConstants.InstanceHealthCheckType checkType) {
        boolean skipCertificateValidation = Objects.firstNonNull(skipCertificateValidationObj, false);
        switch (checkType) {
            case PING:
                return Optional.of(new SawInstancePingHealthCheck(instanceConfiguration, skipCertificateValidation));
            case LOGIN:
                return Optional.of(new SawInstanceLoginHealthCheck(instanceConfiguration, skipCertificateValidation));
        }
        return Optional.empty();
    }

    @Override
    public List<ConfigurationCheck> buildConfigurationCheck(Map<String, Object> instanceConfiguration) {
        return null;
    }

    @Override
    public String getSystemType() {
        return SawConstants.SAW_TYPE;
    }
}
