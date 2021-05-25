package org.wso2.sample.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.*;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;

import java.util.List;

public class CustomSubscriptionCreationSimpleWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(CustomSubscriptionCreationSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    /**
     * This method executes subscription creation simple workflow and return workflow response back to the caller
     *
     * @param workflowDTO The WorkflowDTO which contains workflow contextual information related to the workflow
     * @return workflow response back to the caller
     * @throws WorkflowException Thrown when the workflow execution was not fully performed
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////// sample code to get the login users profile details //////////////////////////////////
        try{
            String uuid = APIManagerFactory.getInstance().getAPIConsumer( ((SubscriptionWorkflowDTO) workflowDTO).getSubscriber()  ).getAPI("apimgt/applicationdata/provider/"+((SubscriptionWorkflowDTO) workflowDTO).getApiProvider()+"/"+((SubscriptionWorkflowDTO) workflowDTO).getApiName()+"/"+((SubscriptionWorkflowDTO) workflowDTO).getApiVersion()+"/api").getUUID();
            log.info("API UUID : "+ uuid);
            UserProfileDTO userinfo = APIUtil.getUserDefaultProfile(((SubscriptionWorkflowDTO) workflowDTO).getSubscriber());
            for(int i = 0; i <= userinfo.getFieldValues().length ; i++ ){
                log.info("User info : "+ userinfo.getFieldValues()[i].getDisplayName() + " : " + userinfo.getFieldValues()[i].getFieldValue());
            }
        }
        catch(Exception e){}
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        WorkflowResponse workflowResponse = complete(workflowDTO);
        super.publishEvents(workflowDTO);

        return workflowResponse;
    }

    /**
     * This method is responsible for creating monetuzation logic and returns the execute method.
     *
     * @param workflowDTO The WorkflowDTO which contains workflow contextual information related to the workflow
     * @return workflow response to the caller by returning the execute() method
     * @throws WorkflowException
     */
    @Override
    public WorkflowResponse monetizeSubscription(WorkflowDTO workflowDTO, API api) throws WorkflowException {
        // implemetation is not provided in this version
        return execute(workflowDTO);
    }

    @Override
    public WorkflowResponse monetizeSubscription(WorkflowDTO workflowDTO, APIProduct apiProduct) throws WorkflowException {
        // implementation is not provided in this version
        return execute(workflowDTO);
    }

    /**
     * This method completes subscription creation simple workflow and return workflow response back to the caller
     *
     * @param workflowDTO The WorkflowDTO which contains workflow contextual information related to the workflow
     * @return workflow response back to the caller
     * @throws WorkflowException
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        try {
            apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                    APIConstants.SubscriptionStatus.UNBLOCKED);
        } catch (APIManagementException e) {
            log.error("Could not complete subscription creation workflow", e);
            throw new WorkflowException("Could not complete subscription creation workflow", e);
        }
        return new GeneralWorkflowResponse();
    }
}
