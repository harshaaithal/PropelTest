package com.hp.ccue.serviceExchange.adapter.saw;

import com.google.common.net.HttpHeaders;
import com.hp.ccue.serviceExchange.SXConstants.SawInstancesCfg;
import com.hp.ccue.serviceExchange.adapter.caseex.CxAwareOperationExecutor;
import com.hp.ccue.serviceExchange.adapter.saw.sawBeans.BulkOperationResult;
import com.hp.ccue.serviceExchange.adapter.saw.sawBeans.BulkOperationResult.CompletionStatuses;
import com.hp.ccue.serviceExchange.adapter.saw.sawBeans.BulkOperationResult.EntityResult;
import com.hp.ccue.serviceExchange.adapter.saw.sawBeans.BulkOperationResult.ErrorDetails;
import com.hp.ccue.serviceExchange.adapter.saw.util.SXSAWImplProperties;
import com.hp.ccue.serviceExchange.config.Configuration;
import com.hp.ccue.serviceExchange.http.HttpRequest;
import com.hp.ccue.serviceExchange.http.HttpResponse;
import com.hp.ccue.serviceExchange.http.HttpResponseWrapper;
import com.hp.ccue.serviceExchange.operation.OperationConstants;
import com.hp.ccue.serviceExchange.utils.JsonUtils;
import com.hp.ccue.serviceExchange.utils.ValidationUtils;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hp.ccue.serviceExchange.operation.BaseOperationExecutor;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Objects.firstNonNull;
import static com.hp.ccue.serviceExchange.SXConstants.SUPPORTED_LANGUAGE_TAGS;

@Component
public class SawOperationExecutor extends CxAwareOperationExecutor {
    
    private static final String LOGIN_REQUEST_URL = "/auth/authentication-endpoint/authenticate/login";
	private static final String DOWNLOAD_ATTACHMENT_URL = "/frs/file-list/";
	private static final String DOWNLOAD_IMAGES_URL = "/frs/image-list/";
    private static final String IMAGES_URL = "/js/modules/saw/resources/images/";

    @Autowired
    private SawNotificationSetupExecutor notificationSetupExecutor;

    private Set<String> operationsWithoutLogging;

    @Autowired
    private Configuration configuration;

    public void setOperationsWithoutLogging(Set<String> operationsWithoutLogging) {
        this.operationsWithoutLogging = operationsWithoutLogging;
    }

    public SawOperationExecutor() {
        super(SawConstants.SAW_TYPE, SawInstancesCfg.CFG_NAME);
        setDefaultHttpRequestContentType(MediaType.APPLICATION_JSON);
	}

    private Collection<String> getInstanceEndpoints(){
        Collection<String> instanceEndpoints = new ArrayList<>();
        Map<String, Object> allInstanceConfigs = configuration.getConfiguration(SawInstancesCfg.CFG_NAME);
        for (Object value : allInstanceConfigs.values()) {
            @SuppressWarnings("unchecked")
            Map<String,Object> instance = (Map<String, Object>) value;
            instanceEndpoints.add((String)instance.get("endpoint"));
        }
        return instanceEndpoints;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Set<String> skippedOperations = firstNonNull(operationsWithoutLogging, Collections.<String>emptySet());
        for (String operation : skippedOperations) {
            skipLoggingForOperation(operation);
        }
    }

    @Override
    protected void beforeExecuteOperation(String operationName, Map<String, Object> message, Map<String, Object> context, boolean doLogging) {
        context.put(KEY_BUNDLE, SXSAWImplProperties.getMessagesBundle());
        context.put(KEY_BUNDLES, SUPPORTED_LANGUAGE_TAGS.stream()
                .collect(Collectors.toMap(Function.identity(), tag -> SXSAWImplProperties.getMessagesBundle(Locale.forLanguageTag(tag)))));
    }

    @Override
    protected HttpResponse afterHttpResponseReceived(final HttpResponse httpResponse, final HttpRequest request) {
        // SAW HTTP responses are buggy!
        // we need to wrap them to determine proper Content-Type - the server does not send them
        // we must wrap all methods which have something to do with Content-Type handling
        HashMap<String, Object> context = new HashMap<>(); // unfortunately we don't have the context here; fixing this would required changing method signature
        SawHttpResponseWrapper result = new SawHttpResponseWrapper(httpResponse, request);
        getEffectiveStatusCode(httpResponse, context).ifPresent(result::setStatus);
        return result;
    }

    private Optional<Integer> getEffectiveStatusCode(HttpResponse httpResponse, Map<String, Object> context) {
        try {
            Optional<BulkOperationResult> bulkOperationResult = parseBulkOperationResult(httpResponse, context);
            if (bulkOperationResult.isPresent() && !Objects.equals(bulkOperationResult.get().getMeta().getCompletionStatus(), CompletionStatuses.FAILED)) {
                return Optional.empty();
            }
            return bulkOperationResult.flatMap(SawOperationExecutor::getErrorDetails).map(ErrorDetails::getHttpStatus);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private class SawHttpResponseWrapper extends HttpResponseWrapper {

        private final HttpRequest request;
        private int statusCode;
        private StatusLine statusLine;

        public SawHttpResponseWrapper(HttpResponse httpResponse, HttpRequest request) {
            super(httpResponse);
            this.request = request;
            this.statusCode = httpResponse.getStatusCode();
            this.statusLine = httpResponse.getStatusLine();
        }

        @Override
        public String getHeader(String headerName) {
            if (headerName.equals(HttpHeaders.CONTENT_TYPE) && !isUrlKnownToHaveCorrectContentType(request.getUrl())) {
                if (request.getUrl().contains(LOGIN_REQUEST_URL)) {
                    // authentication response is plaintext
                    return MediaType.TEXT_PLAIN;
                } else {
                    // other responses are JSON
                    return MediaType.APPLICATION_JSON;
                }
            }
            // else:
            return super.getHeader(headerName);
        }

        @Override
        public boolean hasStringContent() {
            //noinspection SimplifiableIfStatement
            if (!isUrlKnownToHaveCorrectContentType(request.getUrl())) {
                return true;
            }
            return super.hasStringContent();
        }

        @Override
        public boolean isJson() {
            return getHeader(HttpHeaders.CONTENT_TYPE).equals(MediaType.APPLICATION_JSON);
        }

        private boolean isUrlKnownToHaveCorrectContentType(String url) {
            for (String endpoint : getInstanceEndpoints()) {
                if (url.startsWith(endpoint)) {
                    return url.contains(DOWNLOAD_ATTACHMENT_URL) || url.contains(IMAGES_URL) || url.contains(DOWNLOAD_IMAGES_URL);
                }
            }
            return true;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public StatusLine getStatusLine() {
            return statusLine;
        }

        void setStatus(int statusCode) {
            this.statusCode = statusCode;
            Status status = Status.fromStatusCode(statusCode);
            String reasonPhrase = status != null ? status.getReasonPhrase() : String.format("HTTP %s", statusCode);
            this.statusLine = new BasicStatusLine(getDelegate().getStatusLine().getProtocolVersion(), statusCode, reasonPhrase);
        }
    }

    @Override
    protected void executeNotificationSetup(
            String entityId, Map<String,Object> entity, String checkOperation, Map<String, Object> checkOperationInputMessage, String catalogCallbackTemplate,
            EntityRegistrationMode mode, Map<String, Object> context, Map<String, Object> stepConfig) throws Exception {
        notificationSetupExecutor.executeNotificationSetup(
                entityId, entity, checkOperation, checkOperationInputMessage, catalogCallbackTemplate, mode, context, stepConfig);
    }

    @Override
    protected boolean isResponseSuccess(String operationName, Map<String, Object> stepConfig, HttpResponse httpResponse, Map<String, Object> message, Map<String, Object> context) {
        final int statusCode = httpResponse.getStatusCode();
        List<Integer> whiteListedStatuses = firstNonNull(JsonUtils.<List<Integer>>getField(stepConfig, OperationConstants.REPORT_SUCCESS_FOR_HTTP_STATUSES),
                Collections.<Integer>emptyList());
        //noinspection SimplifiableIfStatement
        if (whiteListedStatuses.contains(statusCode)) {
            return true;
        }
        return statusCode < 400;
    }

    private Optional<BulkOperationResult> parseBulkOperationResult(HttpResponse httpResponse, Map<String, Object> context) {
        if (!httpResponse.isJson()) {
            return Optional.empty();
        }
        Object jsonPayload;
        try {
            jsonPayload = parseJsonPayload(httpResponse, context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!(jsonPayload instanceof Map)) {
            return Optional.empty();
        }
        BulkOperationResult bulkOperationResult;
        try {
            //noinspection unchecked
            bulkOperationResult = JsonUtils.readBean((Map<String, Object>) jsonPayload, BulkOperationResult.class);
            ValidationUtils.validateBean(bulkOperationResult);
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.of(bulkOperationResult);
    }

    @Override
    protected String getDetailErrorMessage(String operationName, Map<String, Object> stepConfig, HttpResponse httpResponse, Map<String, Object> message, Map<String, Object> context) {
        if (!httpResponse.isJson()) {
            return null;
        }
        try {
            ErrorDetails errorDetails = parseBulkOperationResult(httpResponse, context).flatMap(SawOperationExecutor::getErrorDetails).orElse(null);
            if (errorDetails == null) {
                return null;
            }
            final Integer httpStatus = errorDetails.getHttpStatus();
            final String detailMessage = errorDetails.getMessage();
            if (httpStatus == null || detailMessage == null) {
                return null;
            }
            return String.format("%s (effective HTTP status: %s)", detailMessage, httpStatus);
        } catch (RuntimeException e) {
            log.debug("Failed to extract the detail message.", e);
            return null;
        }
    }

    private static Optional<ErrorDetails> getErrorDetails(BulkOperationResult bulkOperationResult) {
        ErrorDetails errorDetails;
        final List<EntityResult> entityResultList = bulkOperationResult.getEntityResultList();
        if (entityResultList.size() != 1) { // let's cover only the case of single entity
            errorDetails = bulkOperationResult.getMeta().getErrorDetails();
        } else {
            errorDetails = entityResultList.get(0).getErrorDetails();
        }
        return Optional.ofNullable(errorDetails);
    }
}
