package com.hp.ccue.serviceExchange.adapter.saw.sawBeans;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class BulkOperationResult {
    @Valid
    @NotNull
    @JsonProperty("entity_result_list")
    private List<EntityResult> entityResultList = new ArrayList<>();

    @Valid
    @NotNull
    private Meta meta;

    public static class EntityResult {
        @Valid
        private ErrorDetails errorDetails;

        public ErrorDetails getErrorDetails() {
            return errorDetails;
        }

        public void setErrorDetails(ErrorDetails errorDetails) {
            this.errorDetails = errorDetails;
        }
    }

    public static class Meta {
        @NotNull
        @JsonProperty("completion_status")
        private String completionStatus;

        @Valid
        private ErrorDetails errorDetails;

        public String getCompletionStatus() {
            return completionStatus;
        }

        public void setCompletionStatus(String completionStatus) {
            this.completionStatus = completionStatus;
        }

        public ErrorDetails getErrorDetails() {
            return errorDetails;
        }

        public void setErrorDetails(ErrorDetails errorDetails) {
            this.errorDetails = errorDetails;
        }
    }

    public static class ErrorDetails {
        private Integer httpStatus;
        private String message;

        public Integer getHttpStatus() {
            return httpStatus;
        }

        public void setHttpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public List<EntityResult> getEntityResultList() {
        return entityResultList;
    }

    public void setEntityResultList(List<EntityResult> entityResultList) {
        if (entityResultList == null) {
            entityResultList = new ArrayList<>();
        }
        this.entityResultList = entityResultList;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public static class CompletionStatuses {
        private CompletionStatuses() {
        }

        public static final String OK = "OK";
        public static final String FAILED = "FAILED";
    }
}
