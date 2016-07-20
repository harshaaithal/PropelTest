package com.hp.ccue.serviceExchange.adapter.saw.healthcheck;

import com.hp.ccue.serviceExchange.SXConstants.InstanceHealthCheckType;
import com.hp.ccue.serviceExchange.adapter.monitoring.InstanceHealthCheck;
import com.hp.ccue.serviceExchange.adapter.monitoring.JsonHealthCheckHelper;
import com.hp.ccue.serviceExchange.monitoring.EndpointCheckerHelper;
import com.hp.ccue.serviceExchange.utils.LoggerFactory;
import org.slf4j.Logger;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hp.ccue.serviceExchange.SXConstants.INSTANCES_ENDPOINT;
import static com.hp.ccue.serviceExchange.adapter.monitoring.JsonHealthCheckHelper.getProxyDefinition;
import static com.hp.ccue.serviceExchange.adapter.saw.util.SXSAWImplProperties.getMessage;
import static com.hp.ccue.serviceExchange.monitoring.EndpointCheckerHelper.checkHttpGet;
import static com.hp.ccue.serviceExchange.monitoring.SelfTestCheckerHelper.getBestMessage;

public class SawInstancePingHealthCheck extends InstanceHealthCheck {

    private static final Logger log = LoggerFactory.getLogger(SawInstancePingHealthCheck.class);
    private final boolean skipCertificateValidation;

    public SawInstancePingHealthCheck(Map<String, Object> instanceConfiguration, boolean skipCertificateValidation) {
        super(instanceConfiguration);
        this.skipCertificateValidation = skipCertificateValidation;
    }

    @Override
    public InstanceHealthCheckType getType() {
        return InstanceHealthCheckType.PING;
    }

    @Override
    protected Result check() throws Exception {
        List<String> errorMessages = new ArrayList<>();
        final String endpoint = JsonHealthCheckHelper.getStrField(getInstanceConfiguration(), INSTANCES_ENDPOINT, true, errorMessages);
        Proxy proxy = getProxyDefinition(instanceConfiguration, errorMessages);

        if (!errorMessages.isEmpty()) {
            return Result.unhealthy(String.join("\n", errorMessages));
        }

        try {
            final String pingUri = UriBuilder.fromUri(endpoint).path("auth/authentication-endpoint/authenticate/login").build().toString();
            EndpointCheckerHelper.Result result = checkHttpGet(pingUri, null, null, skipCertificateValidation, null, proxy);
            if (result.statusCode != Status.UNAUTHORIZED.getStatusCode()) {
                log.warn("SAW instance endpoint '{}' is not accessible. Response status code: {}. Response content : {}", pingUri, result.statusCode, result.content);
                return Result.unhealthy(getMessage("SawInstancePingHealthCheck.failed.status", result.statusCode, pingUri));
            }
            return Result.healthy();
        } catch (Exception e) {
            log.warn(String.format("SAW instance endpoint '%s' is not accessible.", endpoint), e);
            return Result.unhealthy(getMessage("SawInstancePingHealthCheck.failed.exception", getBestMessage(e)));
        }
    }
}
