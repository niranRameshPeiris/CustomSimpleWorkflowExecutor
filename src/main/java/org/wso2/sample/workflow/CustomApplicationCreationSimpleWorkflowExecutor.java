package org.wso2.sample.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.*;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;

import java.util.List;

public class CustomApplicationCreationSimpleWorkflowExecutor extends WorkflowExecutor {
    private static final Log log = LogFactory.getLog(CustomApplicationCreationSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION;
    }

    /**
     * Execute the workflow executor
     *
     * @param workFlowDTO
     *            - {@link ApplicationWorkflowDTO}
     * @throws WorkflowException
     */

    public WorkflowResponse execute(WorkflowDTO workFlowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.info("Executing Application creation Workflow..");
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////// sample code to get the login users profile details //////////////////////////////////
        try{
            UserProfileDTO userinfo = APIUtil.getUserDefaultProfile(((ApplicationWorkflowDTO) workFlowDTO).getUserName());
            for(int i = 0; i <= userinfo.getFieldValues().length ; i++ ){
                log.info("User info : "+ userinfo.getFieldValues()[i].getDisplayName() + " : " + userinfo.getFieldValues()[i].getFieldValue());
            }
        }
        catch(Exception e){}
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        workFlowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workFlowDTO);
        super.publishEvents(workFlowDTO);
        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the external process status
     * Based on the workflow status we will update the status column of the
     * Application table
     *
     * @param workFlowDTO - WorkflowDTO
     */
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.info("Complete  Application creation Workflow..");
        }

        String status = null;
        if ("CREATED".equals(workFlowDTO.getStatus().toString())) {
            status = APIConstants.ApplicationStatus.APPLICATION_CREATED;
        } else if ("REJECTED".equals(workFlowDTO.getStatus().toString())) {
            status = APIConstants.ApplicationStatus.APPLICATION_REJECTED;
        } else if ("APPROVED".equals(workFlowDTO.getStatus().toString())) {
            status = APIConstants.ApplicationStatus.APPLICATION_APPROVED;
        }

        ApiMgtDAO dao = ApiMgtDAO.getInstance();

        try {
            dao.updateApplicationStatus(Integer.parseInt(workFlowDTO.getWorkflowReference()),status);
        } catch (APIManagementException e) {
            String msg = "Error occured when updating the status of the Application creation process";
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }
}
