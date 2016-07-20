package com.hp.ccue.serviceExchange.adapter.saw;

import com.hp.ccue.serviceExchange.SXConstants;
import com.hp.ccue.serviceExchange.adapter.pipeline.ContextVariable;
import com.hp.ccue.serviceExchange.adapter.pipeline.ExecutionContext;
import com.hp.ccue.serviceExchange.adapter.provided.AggregationInvocationBlock;
import com.hp.ccue.serviceExchange.adapter.provided.CatalogNotificationBlock;
import com.hp.ccue.serviceExchange.adapter.provided.EntityChangeCleanupBlock;
import com.hp.ccue.serviceExchange.adapter.provided.EntityInfoAwareCatalogNotificationBlock;
import com.hp.ccue.serviceExchange.adapter.provided.EntityInfoOperationExecutorBlock;
import com.hp.ccue.serviceExchange.adapter.provided.OoInvocationBlock;
import com.hp.ccue.serviceExchange.adapter.provided.PrepareCatalogNotificationMessageBlock;
import com.hp.ccue.serviceExchange.adapter.provided.RetrieveEntityInfoBlock;
import com.hp.ccue.serviceExchange.adapter.storage.EntityInfo;
import com.hp.ccue.serviceExchange.aggregation.AggregationService;
import com.hp.ccue.serviceExchange.catalog.CatalogNotificationMessagePublisher;
import com.hp.ccue.serviceExchange.config.InstanceConfigService;
import com.hp.ccue.serviceExchange.message.MessageTransformer;
import com.hp.ccue.serviceExchange.message.builder.MessageConstants;
import com.hp.ccue.serviceExchange.oo.OoFlowMessagePublisher;
import com.hp.ccue.serviceExchange.storage.StorageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.ccue.serviceExchange.adapter.Adapter;
import com.hp.ccue.serviceExchange.adapter.pipeline.AdapterPipelineBuilder;
import com.hp.ccue.serviceExchange.adapter.pipeline.PipelineBuilder;
import com.hp.ccue.serviceExchange.adapter.pipeline.PipelineBuilderFactory;
import com.hp.ccue.serviceExchange.adapter.pipeline.Names;
import com.hp.ccue.serviceExchange.adapter.pipeline.Pipeline;
import com.hp.ccue.serviceExchange.adapter.provided.OperationExecutionBlock;

import java.util.Map;

import static com.hp.ccue.serviceExchange.adapter.saw.SawConstants.SAW_TYPE;

@Component
public class SawPipelineBuilder implements AdapterPipelineBuilder {

    private static final String CONDITIONAL_OO_FLOW_BLOCK_NAME_SUFFIX = "conditionalOoFlow";

    private static final String ENTITY_INFO_PROPERTY_PATH = "entityInfo";

    private static final String CATALOG_NOTIFICATION_MESSAGE_PROPERTY_PATH = "catalogNotificationMessage";

    @Autowired
    private StorageFactory storageFactory;

    @Autowired
    private CatalogNotificationMessagePublisher cnPublisher;

    @Autowired
    private OoFlowMessagePublisher ooFlowMessagePublisher;

    @Autowired
    private MessageTransformer messageTransformer;

    @Autowired
    private InstanceConfigService instanceConfigService;

    @Autowired
    private AggregationService aggregationService;

	@Override
	public Pipeline buildPipeline(Adapter adapter, PipelineBuilderFactory factory, String name) {
		switch (name) {
            case Names.PIPELINE_PLAIN:
                return buildPlainPipeline(factory);
            case Names.PIPELINE_OPERATION:
                return buildOperationPipeline(factory);
            case SawConstants.PIPELINE_SX_MANAGED_CHANGE:
                return buildSxManagedChangePipeline(factory);
            case SawConstants.PIPELINE_AGGREGATION_CHANGE:
                return buildAggregationChangePipeline(factory);
			default:
				return null;
		}
	}

    public Pipeline buildAggregationChangePipeline(PipelineBuilderFactory factory) {
        PipelineBuilder builder = factory.newBuilder(SawConstants.PIPELINE_AGGREGATION_CHANGE);
        builder.addBlock(new AggregationInvocationBlock(SAW_TYPE, aggregationService));
        return builder.build();
    }

	private Pipeline buildPlainPipeline(PipelineBuilderFactory factory) {
		final PipelineBuilder builder = factory.newBuilder(Names.PIPELINE_PLAIN);
		builder.addBlock(new OperationExecutionBlock());
		return builder.build();
	}

    private Pipeline buildOperationPipeline(PipelineBuilderFactory factory) {
        final PipelineBuilder builder = factory.newBuilder(Names.PIPELINE_OPERATION);

        builder.addBlock(new OperationExecutionBlock());

        ContextVariable<Map> catalogNotificationMessage = ContextVariable.newDataMap(CATALOG_NOTIFICATION_MESSAGE_PROPERTY_PATH);
        builder.addBlock(new PrepareCatalogNotificationMessageBlock(
                ContextVariable.newFixedValue(MessageConstants.RequestState.COMPLETED),
                catalogNotificationMessage));
        builder.addBlock(new CatalogNotificationBlock(cnPublisher,
                // notification message
                catalogNotificationMessage,
                // entity ID is 'id' in the message
                ContextVariable.newMessageString(MessageConstants.ID),
                // notification type - always request
                ContextVariable.newFixedValue(CatalogNotificationMessagePublisher.NotificationType.REQUEST)
        ));

        return builder.build();
    }

    public Pipeline buildSxManagedChangePipeline(PipelineBuilderFactory factory) {
        PipelineBuilder builder = factory.newBuilder(SawConstants.PIPELINE_SX_MANAGED_CHANGE);

        // prepare entityInfo variable
        ContextVariable<EntityInfo> entityInfo = ContextVariable.newDataValue(EntityInfo.class, ENTITY_INFO_PROPERTY_PATH);

        // retrieve entity info from database
        // keep reference to RetrieveEntityInfoBlock - we want its VAR_TARGET_INSTANCE
        RetrieveEntityInfoBlock retrieveEntityInfoBlock;
        builder.addBlock(retrieveEntityInfoBlock = new RetrieveEntityInfoBlock(SAW_TYPE, storageFactory, entityInfo));
        // operation execution from entity info
        builder.addBlock(new EntityInfoOperationExecutorBlock(
                storageFactory,
                entityInfo));
        // extract target instance (it is not in the message anymore)
        ContextVariable<String> targetInstance = retrieveEntityInfoBlock.<String>describeVariable(RetrieveEntityInfoBlock.VAR_TARGET_INSTANCE).binding;
        // catalog notification
        builder.addBlock(new EntityInfoAwareCatalogNotificationBlock(
                SAW_TYPE,
                cnPublisher,
                storageFactory,
                messageTransformer,
                instanceConfigService,
                targetInstance,
                null,
                entityInfo
        ));
        // finally if not explicitly suppressed, notify OO
        builder.addBlock(new OoInvocationBlock(CONDITIONAL_OO_FLOW_BLOCK_NAME_SUFFIX, ooFlowMessagePublisher) {

            protected boolean isInterested(ExecutionContext context) {
                return !context.message.isEmpty() && !context.message.containsKey(SXConstants.MessageDirectives.SKIP_FLOW_RUN);
            }

        });
        // entity change cleanup
        builder.addBlock(new EntityChangeCleanupBlock(
                SAW_TYPE,
                storageFactory,
                targetInstance));
        return builder.build();
    }
}
