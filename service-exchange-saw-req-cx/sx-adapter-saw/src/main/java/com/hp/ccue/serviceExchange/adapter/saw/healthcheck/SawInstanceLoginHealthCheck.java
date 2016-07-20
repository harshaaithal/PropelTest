package com.hp.ccue.serviceExchange.adapter.saw.healthcheck;

import com.google.common.collect.ImmutableMap;
import com.hp.ccue.serviceExchange.SXConstants;
import com.hp.ccue.serviceExchange.SXConstants.SawInstancesCfg;
import com.hp.ccue.serviceExchange.adapter.monitoring.InstanceHealthCheck;
import com.hp.ccue.serviceExchange.monitoring.EndpointCheckerHelper;
import com.hp.ccue.serviceExchange.utils.LoggerFactory;
import org.slf4j.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hp.ccue.serviceExchange.SXConstants.INSTANCES_ENDPOINT;
import static com.hp.ccue.serviceExchange.SXConstants.INSTANCES_LOGIN_NAME;
import static com.hp.ccue.serviceExchange.SXConstants.INSTANCES_PASSWORD;
import static com.hp.ccue.serviceExchange.SXConstants.INSTANCES_USER;
import static com.hp.ccue.serviceExchange.adapter.monitoring.JsonHealthCheckHelper.getProxyDefinition;
import static com.hp.ccue.serviceExchange.adapter.monitoring.JsonHealthCheckHelper.getStrField;
import static com.hp.ccue.serviceExchange.adapter.saw.util.SXSAWImplProperties.getMessage;
import static com.hp.ccue.serviceExchange.monitoring.EndpointCheckerHelper.checkHttpGet;
import static com.hp.ccue.serviceExchange.monitoring.SelfTestCheckerHelper.getBestMessage;
import static com.hp.ccue.serviceExchange.security.PropertyValueEncryptionHelper.decrypt;

public class SawInstanceLoginHealthCheck extends InstanceHealthCheck {

    private static final Logger log = LoggerFactory.getLogger(SawInstanceLoginHealthCheck.class);
    private final boolean skipCertificateValidation;

    public SawInstanceLoginHealthCheck(Map<String, Object> instanceConfiguration, boolean skipCertificateValidation) {
        super(instanceConfiguration);
        this.skipCertificateValidation = skipCertificateValidation;
    }

    @Override
    public SXConstants.InstanceHealthCheckType getType() {
        return SXConstants.InstanceHealthCheckType.LOGIN;
    }

    @Override
    protected Result check() throws Exception {
        final Map<String, Object> cfg = getInstanceConfiguration();

        List<String> errorMessages = new ArrayList<>();
        final String endpoint = getStrField(cfg, INSTANCES_ENDPOINT, true, errorMessages);
        final String loginName = getStrField(cfg, INSTANCES_USER + '.' + INSTANCES_LOGIN_NAME, true, errorMessages);
        final String password = decrypt(getStrField(cfg, INSTANCES_USER + '.' + INSTANCES_PASSWORD, true, errorMessages));
        final String organization = getStrField(cfg, SawInstancesCfg.ORGANIZATION, true, errorMessages);
        Proxy proxy = getProxyDefinition(instanceConfiguration, errorMessages);

        if (!errorMessages.isEmpty()) {
            return Result.unhealthy(String.join("\n", errorMessages));
        }

        final String token;
        try {
            final String loginUri = UriBuilder.fromUri(endpoint).path("auth/authentication-endpoint/authenticate/login")
                    .queryParam("login", loginName)
                    .queryParam("password", password)
                    .build().toString();
            assert loginName != null;
            assert password != null;
            EndpointCheckerHelper.Result result = checkHttpGet(loginUri, null, null, skipCertificateValidation,
                    ImmutableMap.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), proxy);
            if (result.statusCode != Response.Status.OK.getStatusCode()) {
                log.warn("SAW instance endpoint '{}' cannot login. Response status code: {}. Response content : {}", endpoint, result.statusCode, result.content);
                return Result.unhealthy(getMessage("SawInstanceLoginHealthCheck.failed.status", result.statusCode, loginUri));
            }
            token = result.content;
        } catch (Exception e) {
            log.warn(String.format("SAW instance endpoint '%s' cannot login.", endpoint), e);
            return Result.unhealthy(getMessage("SawInstanceLoginHealthCheck.failed.exception", getBestMessage(e)));
        }

        try {
            final String integrationUserGetUri = UriBuilder.fromUri(endpoint).path("rest/{organization}/ems/Person")
                    .queryParam("layout", "Id")
                    .queryParam("filter", "Upn='{loginName}'")
                    .build(organization, loginName).toString();
            EndpointCheckerHelper.Result result = checkHttpGet(integrationUserGetUri, null, null, skipCertificateValidation,
                    ImmutableMap.of(HttpHeaders.COOKIE, String.format("TENANTID=%s; LWSSO_COOKIE_KEY=%s", organization, token)), proxy);
            if (result.statusCode != Response.Status.OK.getStatusCode()) {
                log.warn("SAW instance endpoint '{}' cannot get integration user. Response status code: {}. Response content : {}", endpoint, result.statusCode, result.content);
                return Result.unhealthy(getMessage("SawInstanceLoginHealthCheck.getFailed.status", result.statusCode, integrationUserGetUri));
            }
        } catch (Exception e) {
            log.warn(String.format("SAW instance endpoint '%s' cannot get integration user.", endpoint), e);
            return Result.unhealthy(getMessage("SawInstanceLoginHealthCheck.getFailed.exception", getBestMessage(e)));
        }

        return Result.healthy();
    }
}