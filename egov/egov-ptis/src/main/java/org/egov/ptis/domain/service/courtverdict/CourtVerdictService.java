/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2017  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 *
 */
package org.egov.ptis.domain.service.courtverdict;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.egov.ptis.constants.PropertyTaxConstants.ADMIN_HIERARCHY_TYPE;
import static org.egov.ptis.constants.PropertyTaxConstants.APPLICATION_TYPE_COURT_VERDICT;
import static org.egov.ptis.constants.PropertyTaxConstants.BUILT_UP_PROPERTY;
import static org.egov.ptis.constants.PropertyTaxConstants.CATEGORY_MIXED;
import static org.egov.ptis.constants.PropertyTaxConstants.CATEGORY_NON_RESIDENTIAL;
import static org.egov.ptis.constants.PropertyTaxConstants.CATEGORY_RESIDENTIAL;
import static org.egov.ptis.constants.PropertyTaxConstants.COMMISSIONER_DESGN;
import static org.egov.ptis.constants.PropertyTaxConstants.COMMISSIONER_DESIGNATIONS;
import static org.egov.ptis.constants.PropertyTaxConstants.CURRENTYEAR_SECOND_HALF;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_CODE_ADVANCE;
import static org.egov.ptis.constants.PropertyTaxConstants.FLOOR_MAP;
import static org.egov.ptis.constants.PropertyTaxConstants.LOCALITY;
import static org.egov.ptis.constants.PropertyTaxConstants.LOCATION_HIERARCHY_TYPE;
import static org.egov.ptis.constants.PropertyTaxConstants.NATURE_COURT_VERDICT;
import static org.egov.ptis.constants.PropertyTaxConstants.OWNERSHIP_TYPE_VAC_LAND;
import static org.egov.ptis.constants.PropertyTaxConstants.OWNERSHIP_TYPE_VAC_LAND_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.REVENUE_HIERARCHY_TYPE;
import static org.egov.ptis.constants.PropertyTaxConstants.REVENUE_OFFICER_DESGN;
import static org.egov.ptis.constants.PropertyTaxConstants.SOURCE_ONLINE;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_CANCELLED;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_ISACTIVE;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_ISHISTORY;
import static org.egov.ptis.constants.PropertyTaxConstants.STATUS_WORKFLOW;
import static org.egov.ptis.constants.PropertyTaxConstants.VACANTLAND_MIN_CUR_CAPITALVALUE;
import static org.egov.ptis.constants.PropertyTaxConstants.VACANT_PROPERTY;
import static org.egov.ptis.constants.PropertyTaxConstants.WARD;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_APPROVE;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_FORWARD;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_REJECT;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_COMMISSIONER_APPROVAL_PENDING;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_REJECTED;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_REVENUE_OFFICER_APPROVAL_PENDING;
import static org.egov.ptis.constants.PropertyTaxConstants.ZONE;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.egov.commons.Area;
import org.egov.commons.Installment;
import org.egov.commons.dao.InstallmentHibDao;
import org.egov.demand.model.EgDemandDetails;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.BoundaryService;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.notification.service.NotificationService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.egov.infra.utils.DateUtils;
import org.egov.infra.workflow.matrix.entity.WorkFlowMatrix;
import org.egov.infra.workflow.service.SimpleWorkflowService;
import org.egov.infstr.services.PersistenceService;
import org.egov.pims.commons.Position;
import org.egov.ptis.bean.demand.DemandDetail;
import org.egov.ptis.client.bill.PTBillServiceImpl;
import org.egov.ptis.client.util.PropertyTaxUtil;
import org.egov.ptis.domain.dao.demand.PtDemandDao;
import org.egov.ptis.domain.dao.property.PropertyTypeMasterDAO;
import org.egov.ptis.domain.entity.demand.Ptdemand;
import org.egov.ptis.domain.entity.property.BasicPropertyImpl;
import org.egov.ptis.domain.entity.property.CourtVerdict;
import org.egov.ptis.domain.entity.property.Floor;
import org.egov.ptis.domain.entity.property.PropertyDetail;
import org.egov.ptis.domain.entity.property.PropertyID;
import org.egov.ptis.domain.entity.property.PropertyImpl;
import org.egov.ptis.domain.entity.property.PropertyOccupation;
import org.egov.ptis.domain.entity.property.PropertyOwnerInfo;
import org.egov.ptis.domain.entity.property.PropertyTypeMaster;
import org.egov.ptis.domain.entity.property.PropertyUsage;
import org.egov.ptis.domain.entity.property.StructureClassification;
import org.egov.ptis.domain.entity.property.vacantland.LayoutApprovalAuthority;
import org.egov.ptis.domain.entity.property.vacantland.VacantLandPlotArea;
import org.egov.ptis.domain.repository.courtverdict.CourtVerdictRepository;
import org.egov.ptis.domain.repository.master.occupation.PropertyOccupationRepository;
import org.egov.ptis.domain.repository.master.structureclassification.StructureClassificationRepository;
import org.egov.ptis.domain.repository.master.vacantland.LayoutApprovalAuthorityRepository;
import org.egov.ptis.domain.repository.master.vacantland.VacantLandPlotAreaRepository;
import org.egov.ptis.domain.service.property.PropertyService;
import org.egov.ptis.exceptions.TaxCalculatorExeption;
import org.egov.ptis.master.service.PropertyUsageService;
import org.egov.ptis.service.utils.PropertyTaxCommonUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Service
public class CourtVerdictService {

    @Autowired
    private CourtVerdictRepository courtVerdictRepo;
    @Autowired
    private PropertyTaxUtil propertyTaxUtil;
    @Autowired
    @Qualifier("parentMessageSource")
    private MessageSource ptisMessageSource;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PropertyTaxCommonUtils propertyTaxCommonUtils;
    @Autowired
    private BoundaryService boundaryService;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private PropertyTypeMasterDAO propTypeMasterDAO;
    @Autowired
    private VacantLandPlotAreaRepository vacantLandPlotAreaRepo;
    @Autowired
    private LayoutApprovalAuthorityRepository layoutApprovalAuthorityRepo;
    @Autowired
    private PropertyOccupationRepository propOccRepo;
    @Autowired
    StructureClassificationRepository structureDAO;
    @Autowired
    PropertyUsageService propertyUsageService;
    @Autowired
    private PositionMasterService positionMasterService;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private PersistenceService<T, Serializable> persistenceService;
    @Autowired
    @Qualifier("workflowService")
    private SimpleWorkflowService<PropertyImpl> propertyWorkflowService;
    @Autowired
    private ApplicationNumberGenerator applicationNo;
    @Autowired
    private InstallmentHibDao installmentDao;
    @Autowired
    private PtDemandDao ptDemandDAO;
    @Autowired
    private PTBillServiceImpl ptBillServiceImpl;
    private static final Logger LOGGER = Logger.getLogger(CourtVerdictService.class);

    private String propertyCategory;

    public void addModelAttributes(final Model model, final PropertyImpl property,
            final HttpServletRequest request) {

        List<Map<String, Object>> wcDetails = propertyService.getWCDetails(property.getBasicProperty().getUpicNo(), request);
        List<Map<String, Object>> sewConnDetails = propertyTaxCommonUtils.getSewConnDetails(
                property.getBasicProperty().getUpicNo(),
                request);
        List<Floor> floor = property.getPropertyDetail().getFloorDetails();
        property.getPropertyDetail().setFloorDetailsProxy(floor);

        List<PropertyUsage> usageList;
        TreeMap<Integer, String> flrNoMap = new TreeMap<>();
        flrNoMap.putAll(FLOOR_MAP);
        final List<Boundary> localityList = boundaryService
                .getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(LOCALITY, LOCATION_HIERARCHY_TYPE);
        final List<Boundary> zones = boundaryService.getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(ZONE,
                REVENUE_HIERARCHY_TYPE);
        final List<Boundary> electionWardList = boundaryService
                .getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(WARD, ADMIN_HIERARCHY_TYPE);

        final List<PropertyTypeMaster> propTypeList = propTypeMasterDAO.findAllExcludeEWSHS();
        final List<PropertyOccupation> propOccList = propOccRepo.findAll();
        final List<StructureClassification> structureList = structureDAO.findByIsActiveTrueOrderByTypeName();

        usageList = populateUsages(
                isNotBlank(propertyCategory) ? propertyCategory : property.getPropertyDetail().getCategoryType());

        List<VacantLandPlotArea> plotAreaList = vacantLandPlotAreaRepo.findAll();
        List<LayoutApprovalAuthority> layoutApprovalList = layoutApprovalAuthorityRepo.findAll();

        model.addAttribute("floor", floor);
        model.addAttribute("wcDetails", wcDetails);
        model.addAttribute("sewConnDetails", sewConnDetails);
        model.addAttribute("localityList", localityList);
        model.addAttribute("zones", zones);
        model.addAttribute("electionWardList", electionWardList);
        model.addAttribute("propTypeList", propTypeList);
        model.addAttribute("propOccList", propOccList);
        model.addAttribute("flrNoMap", flrNoMap);
        model.addAttribute("structureList", structureList);

        model.addAttribute("usageList", usageList);
        model.addAttribute("plotAreaList", plotAreaList);
        model.addAttribute("layoutApprovalList", layoutApprovalList);

    }

    private List<PropertyUsage> populateUsages(final String propertyCategory) {
        List<PropertyUsage> usageList = propertyUsageService.getAllActiveMixedPropertyUsages();
        // Loading property usages based on property category
        if (StringUtils.isNoneBlank(propertyCategory))
            if (propertyCategory.equals(CATEGORY_MIXED))
                usageList = propertyUsageService.getAllActiveMixedPropertyUsages();
            else if (propertyCategory.equals(CATEGORY_RESIDENTIAL))
                usageList = propertyUsageService.getResidentialPropertyUsages();
            else if (propertyCategory.equals(CATEGORY_NON_RESIDENTIAL))
                usageList = propertyUsageService.getNonResidentialPropertyUsages();
        return usageList;
    }

    public Map<String, String> validate(CourtVerdict courtVerdict) {
        HashMap<String, String> errorMessages = new HashMap<>();
        Date propCompletionDate = propertyTaxUtil
                .getLowestInstallmentForProperty(courtVerdict.getBasicProperty().getActiveProperty());
        if (StringUtils.isBlank(courtVerdict.getBasicProperty().getAddress().getAreaLocalitySector()))
            errorMessages.put("areaLocalitySector", "areaLocalitySector.required");
        if (StringUtils.isBlank(courtVerdict.getBasicProperty().getPropertyID().getArea().getName()))
            errorMessages.put("block", "blockId.required");
        if (courtVerdict.getBasicProperty().getPropertyID().getZone().getName() == null)
            errorMessages.put("zone", "zone.required");
        if (StringUtils.isBlank(courtVerdict.getBasicProperty().getPropertyID().getWard().getName()))
            errorMessages.put("ward", "ward.required");
        if (courtVerdict.getBasicProperty().getPropertyID().getElectionBoundary().getName() == null)
            errorMessages.put("electionWard", "electionWard.required");
        if (StringUtils.isBlank(courtVerdict.getProperty().getPropertyDetail()
                .getPropertyTypeMaster().getType()))
            errorMessages.put("categoryType", "categoryType.required");
        else if (StringUtils.isBlank(courtVerdict.getProperty().getPropertyDetail().getCategoryType()))
            errorMessages.put("propertyType", "propertyType.required");
        else
            validateProperty(courtVerdict.getProperty(), courtVerdict.getProperty().getPropertyDetail().getSitalArea(),
                    courtVerdict.getBasicProperty().getPropertyID().getEastBoundary(),
                    courtVerdict.getBasicProperty().getPropertyID().getWestBoundary(),
                    courtVerdict.getBasicProperty().getPropertyID().getSouthBoundary(),
                    courtVerdict.getBasicProperty().getPropertyID().getNorthBoundary(),
                    courtVerdict.getProperty().getPropertyDetail().getVacantLandPlotArea().getId(),
                    courtVerdict.getProperty().getPropertyDetail().getLayoutApprovalAuthority().getId(), errorMessages,
                    propCompletionDate);
        return errorMessages;
    }

    private void validateProperty(PropertyImpl property, Area sitalArea, String eastBoundary, String westBoundary,
            String southBoundary, String northBoundary, Long vacantLandPlotAreaId,
            Long layoutApprovalAuthorityId, HashMap<String, String> errorMessages, Date propCompletionDate) {

        PropertyTypeMaster propertyTypeMaster = propTypeMasterDAO
                .getPropertyTypeMasterByCode(property.getPropertyDetail().getPropertyTypeMaster().getCode());

        if (propertyTypeMaster.getType().equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND_STR)) {
            if (null != property.getPropertyDetail())
                validateVacantProperty(property.getPropertyDetail(), eastBoundary, westBoundary, southBoundary,
                        northBoundary, vacantLandPlotAreaId, layoutApprovalAuthorityId,
                        errorMessages, propCompletionDate);
        } else if (null == property.getId() && TRUE.equals(property.getPropertyDetail().isAppurtenantLandChecked())) {
            validateVacantProperty(property.getPropertyDetail(), eastBoundary, westBoundary, southBoundary, northBoundary,
                    vacantLandPlotAreaId, layoutApprovalAuthorityId, errorMessages,
                    propCompletionDate);
            validateBuiltUpProperty(property.getPropertyDetail(), sitalArea, errorMessages);
            validateFloor(propertyTypeMaster, property.getPropertyDetail().getFloorDetailsProxy(), property, sitalArea,
                    errorMessages);
        } else {
            validateBuiltUpProperty(property.getPropertyDetail(), sitalArea, errorMessages);
            validateFloor(propertyTypeMaster, property.getPropertyDetail().getFloorDetailsProxy(), property, sitalArea,
                    errorMessages);
        }

    }

    private void validateFloor(PropertyTypeMaster propertyTypeMaster, List<Floor> floorDetailsProxy, PropertyImpl property,
            Area sitalArea, HashMap<String, String> errorMessages) {

        if (!propertyTypeMaster.getCode().equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND) && !floorDetailsProxy.isEmpty()) {
            for (Floor floor : floorDetailsProxy) {
                if (floor.getFloorNo() == null || floor.getFloorNo().equals(-10))
                    errorMessages.put("floorNo", "floorNo.required");

                if (floor.getStructureClassification() == null
                        || floor.getStructureClassification().getId() == null
                        || "-1".equals(floor.getStructureClassification().getId().toString()))
                    errorMessages.put("structureClassif", "structuralclassification.required");

                if (!floor.getUnstructuredLand()) {
                    if (floor.getBuiltUpArea() == null || floor.getBuiltUpArea().getLength() == null)
                        errorMessages.put("length", "length.required");
                    if (floor.getBuiltUpArea() == null || floor.getBuiltUpArea().getBreadth() == null)
                        errorMessages.put("breadth", "breadth.required");
                }
                if (floor.getPropertyUsage() == null || null == floor.getPropertyUsage().getId()
                        || "-1".equals(floor.getPropertyUsage().getId().toString()))
                    errorMessages.put("usage", "usage.required");

                if (StringUtils.isNotBlank(floor.getBuildingPermissionNo())) {
                    if (floor.getBuildingPermissionDate() == null) {
                        errorMessages.put("buildPermissionDate", "buildpermissiondate.required");
                    }
                    if (floor.getBuildingPlanPlinthArea().getArea() == null) {
                        errorMessages.put("buildPlinthArea", "buildplintharea.required");
                    }
                }
                if (floor.getBuildingPermissionDate() != null) {
                    if (isBlank(floor.getBuildingPermissionNo())) {
                        errorMessages.put("buildPermission", "buildpermission.required");
                    }
                    if (floor.getBuildingPlanPlinthArea().getArea() == null)
                        errorMessages.put("buildPlinthArea", "buildplintharea.required");
                }
                if (floor.getBuildingPlanPlinthArea().getArea() != null) {
                    if (floor.getBuildingPermissionDate() == null)
                        errorMessages.put("buildPermissionDate", "buildpermissiondate.required");
                    if (isBlank(floor.getBuildingPermissionNo()))
                        errorMessages.put("buildPermission", "buildpermission.required");
                }
                if (floor.getPropertyOccupation() == null || null == floor.getPropertyOccupation().getId()
                        || "-1".equals(floor.getPropertyOccupation().getId().toString()))
                    errorMessages.put("occupation", "occupancy.required");

                if (floor.getConstructionDate() == null)
                    errorMessages.put("constructDate", "constructiondate.required");

                final Date effDate = propertyTaxUtil.getEffectiveDateForProperty(property);
                if (floor.getOccupancyDate() == null)
                    errorMessages.put("occupancyDate", "occupancydate.required");
                if (floor.getOccupancyDate() != null) {
                    if (floor.getOccupancyDate().after(new Date()))
                        errorMessages.put("occBeforeNewDate", "dtFlrBeforeCurr.error");
                    if (floor.getOccupancyDate().before(effDate))
                        errorMessages.put("occBeforeEffectDate", "constrDate.before.6inst");
                }
                if (floor.getOccupancyDate() != null && floor.getConstructionDate() != null
                        && floor.getOccupancyDate().before(floor.getConstructionDate()))
                    errorMessages.put("effDateBeforeConstrDate", "effectiveDate.before.constrDate.error");

                if (floor.getBuiltUpArea() == null || floor.getBuiltUpArea().getArea() == null)
                    errorMessages.put("builtupArea", "builtuparea.required");
                else if (sitalArea != null
                        && floor.getBuiltUpArea().getArea() > sitalArea.getArea())
                    errorMessages.put("builtAreaGtSitalArea", "builtupareavalid.required");

            }
        }
    }

    private void validateBuiltUpProperty(PropertyDetail propertyDetail, Area sitalArea, HashMap<String, String> errorMessages) {

    }

    private void validateVacantProperty(PropertyDetail propertyDetail, String eastBoundary, String westBoundary,
            String southBoundary, String northBoundary, Long vacantLandPlotAreaId,
            Long layoutApprovalAuthorityId, HashMap<String, String> errorMessages,
            Date propCompletionDate) {

        if (isBlank(propertyDetail.getSurveyNumber()))
            errorMessages.put("surveyNo", "mandatory.surveyNo");
        if (isBlank(propertyDetail.getPattaNumber()))
            errorMessages.put("pattaNo", "mandatory.pattaNum");
        if (null == propertyDetail.getSitalArea().getArea())
            errorMessages.put("sitalArea", "mandatory.vacantLandArea");
        if (null == propertyDetail.getDateOfCompletion())
            errorMessages.put("dateOfCompletion", "mandatory.dtOfCmpln");
        if (null == propertyDetail.getCurrentCapitalValue())
            errorMessages.put("currCapitalValue", "mandatory.capitalValue");
        if (null == propertyDetail.getMarketValue())
            errorMessages.put("marrketValue", "mandatory.marketValue");
        if (propertyDetail.getCurrentCapitalValue() != null
                && propertyDetail.getCurrentCapitalValue().compareTo(new BigDecimal(VACANTLAND_MIN_CUR_CAPITALVALUE)) == -1)
            errorMessages.put("minCapitalValue", "minvalue.capitalValue");
        if (isBlank(eastBoundary))
            errorMessages.put("eastBoundary", "mandatory.eastBoundary");
        if (isBlank(westBoundary))
            errorMessages.put("westBoundary", "mandatory.westBoundary");
        if (isBlank(southBoundary))
            errorMessages.put("southBoundary", "mandatory.southBoundary");
        if (isBlank(northBoundary))
            errorMessages.put("northBoundary", "mandatory.northBoundary");
        if (vacantLandPlotAreaId == null || Long.valueOf(-1).equals(vacantLandPlotAreaId))
            errorMessages.put("vacantLandPlotArea", "mandatory.vacanland.plotarea");
        if (layoutApprovalAuthorityId == null || Long.valueOf(-1).equals(layoutApprovalAuthorityId))
            errorMessages.put("layoutApprovalAuthority", "mandatory.layout.authority");
        if (!(layoutApprovalAuthorityId == null || Long.valueOf(-1).equals(layoutApprovalAuthorityId)) && !"No Approval"
                .equals(layoutApprovalAuthorityRepo.findOne(layoutApprovalAuthorityId).getName())) {
            if (isBlank(propertyDetail.getLayoutPermitNo()))
                errorMessages.put("layoutPermitNo", "mandatory.layout.permitno");
            if (propertyDetail.getLayoutPermitDate() == null)
                errorMessages.put("layoutPermitDate", "mandatory.layout.permitdate");
        }
        if (null != propCompletionDate && propertyDetail.getDateOfCompletion() != null
                && !DateUtils.compareDates(propertyDetail.getDateOfCompletion(), propCompletionDate))
            errorMessages.put("dtOfCompletionValid", "modify.vacant.completiondate.validate");
    }

    @Transactional
    public CourtVerdict saveCourtVerdict(CourtVerdict courtVerdict, Long approvalPosition, final String approvalComent,
            final String additionalRule, final String workFlowAction, final Boolean propertyByEmployee) {
        final User user = securityUtils.getCurrentUser();
        final DateTime currentDate = new DateTime();
        Position pos = null;
        Assignment wfInitiator = null;
        String currentState;
        Assignment assignment = null;
        String approverDesignation = "";
        String nextAction = null;
        String loggedInUserDesignation = "";
        String loggedInUserDesig = "";
        List<Assignment> loggedInUserAssign;
        if (courtVerdict.getState() != null) {
            loggedInUserAssign = assignmentService.getAssignmentByPositionAndUserAsOnDate(
                    courtVerdict.getCurrentState().getOwnerPosition().getId(), user.getId(), new Date());
            loggedInUserDesig = !loggedInUserAssign.isEmpty() ? loggedInUserAssign.get(0).getDesignation().getName()
                    : "";
        } else {
            assignment = propertyTaxCommonUtils.getWorkflowInitiatorAsRO(user.getId());
            wfInitiator = assignment;
            loggedInUserDesig = assignment != null ? assignment.getDesignation().getName()
                    : "";
        }
        if (SOURCE_ONLINE.equalsIgnoreCase(courtVerdict.getSource()) && ApplicationThreadLocals.getUserId() == null)
            ApplicationThreadLocals.setUserId(securityUtils.getCurrentUser().getId());

        if (propertyByEmployee && loggedInUserDesig.contains(REVENUE_OFFICER_DESGN)
                && !workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_REJECT)) {
            currentState = "Created";
            assignment = assignmentService.getAssignmentsForPosition(approvalPosition, new Date()).get(0);
            approverDesignation = assignment.getDesignation().getName();
            if (null != assignment)
                approvalPosition = assignment.getPosition().getId();
        } else {
            currentState = null;
            if (null != approvalPosition && approvalPosition != 0) {
                assignment = assignmentService.getAssignmentsForPosition(approvalPosition, new Date()).get(0);
                assignment.getEmployee().getName().concat("~").concat(assignment.getPosition().getName());
                approverDesignation = assignment.getDesignation().getName();
            }
        }
        if (courtVerdict.getState() != null)
            loggedInUserDesignation = getLoggedInUserDesignation(
                    courtVerdict.getCurrentState().getOwnerPosition().getId(), securityUtils.getCurrentUser());
        if (WFLOW_ACTION_STEP_FORWARD.equalsIgnoreCase(workFlowAction)
                && (COMMISSIONER_DESIGNATIONS.contains(approverDesignation))) {

            final String designation = approverDesignation.split(" ")[0];
            nextAction = getWorkflowNextAction(designation);
        }

        if (courtVerdict.getId() != null && courtVerdict.getState() != null)
            wfInitiator = assignmentService.getAssignmentsForPosition(courtVerdict.getState().getInitiatorPosition().getId())
                    .get(0);
        else if (wfInitiator == null)
            wfInitiator = propertyTaxCommonUtils.getWorkflowInitiatorAsRO(user.getId());

        if (WFLOW_ACTION_STEP_REJECT.equalsIgnoreCase(workFlowAction)) {
            if (wfInitiator.getPosition().equals(courtVerdict.getState().getOwnerPosition())) {
                courtVerdict.transition().end().withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withDateInfo(currentDate.toDate()).withNextAction(null)
                        .withOwner(courtVerdict.getState().getOwnerPosition());
                courtVerdict.setStatus(STATUS_CANCELLED);
                courtVerdict.getBasicProperty().setUnderWorkflow(FALSE);
            } else {
                final Assignment assignmentOnreject = getUserAssignmentOnReject(courtVerdict);
                if (assignmentOnreject != null) {
                    nextAction = "Revenue Officer Approval Pending";
                    wfInitiator = assignmentOnreject;
                } else
                    nextAction = WF_STATE_REVENUE_OFFICER_APPROVAL_PENDING;
                final String stateValue = WF_STATE_REJECTED;
                courtVerdict.transition().progressWithStateCopy()
                        .withSenderName(user.getUsername() + "::" + user.getName()).withComments(approvalComent)
                        .withStateValue(stateValue).withDateInfo(currentDate.toDate())
                        .withOwner(wfInitiator.getPosition()).withNextAction(nextAction);
                buildSMS(courtVerdict, workFlowAction);
            }

        } else {
            if (WFLOW_ACTION_STEP_APPROVE.equalsIgnoreCase(workFlowAction)) {
                pos = courtVerdict.getCurrentState().getOwnerPosition();
            } else if (null != approvalPosition && approvalPosition != -1 && !approvalPosition.equals(Long.valueOf(0)))
                pos = positionMasterService.getPositionById(approvalPosition);
            WorkFlowMatrix wfmatrix;
            if (null == courtVerdict.getState()) {
                wfmatrix = propertyWorkflowService.getWfMatrix(courtVerdict.getStateType(), null, null, additionalRule,
                        currentState, null);
                courtVerdict.transition().start().withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withStateValue(wfmatrix.getNextState()).withDateInfo(new Date())
                        .withOwner(pos)
                        .withNextAction(nextAction)
                        .withNatureOfTask(NATURE_COURT_VERDICT)
                        .withInitiator(wfInitiator != null ? wfInitiator.getPosition() : null)
                        .withSLA(propertyService.getSlaValue(APPLICATION_TYPE_COURT_VERDICT));

            }

            else if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_APPROVE)) {
                courtVerdict.getProperty().setStatus(STATUS_ISACTIVE);
                courtVerdict.getBasicProperty().getActiveProperty().setStatus(STATUS_ISHISTORY);
                courtVerdict.getBasicProperty().addProperty(courtVerdict.getProperty());
                courtVerdict.getBasicProperty().setUnderWorkflow(false);
                courtVerdict.transition().end().withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withDateInfo(currentDate.toDate()).withNextAction(null)
                        .withOwner(courtVerdict.getCurrentState().getOwnerPosition());
            } else {

                wfmatrix = propertyWorkflowService.getWfMatrix(courtVerdict.getStateType(), null, null, additionalRule,
                        courtVerdict.getCurrentState().getValue(), courtVerdict.getCurrentState().getNextAction(), null,
                        loggedInUserDesignation);
                courtVerdict.transition().progressWithStateCopy()
                        .withSenderName(user.getUsername() + "::" + user.getName()).withComments(approvalComent)
                        .withStateValue(wfmatrix.getNextState()).withDateInfo(currentDate.toDate()).withOwner(pos)
                        .withNextAction(StringUtils.isNotBlank(nextAction)
                                ? getNextAction(approverDesignation, workFlowAction)
                                : wfmatrix.getNextAction());

                if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_APPROVE))
                    buildSMS(courtVerdict, workFlowAction);
            }
        }

        return courtVerdictRepo.save(courtVerdict);
    }

    private String getNextAction(final String approverDesignation, String workFlowAction) {
        String nextAction = "";
        if (WFLOW_ACTION_STEP_FORWARD.equalsIgnoreCase(workFlowAction)
                && COMMISSIONER_DESIGNATIONS.contains(approverDesignation)) {

            final String designation = approverDesignation.split(" ")[0];
            if (designation.equalsIgnoreCase(COMMISSIONER_DESGN))
                nextAction = WF_STATE_COMMISSIONER_APPROVAL_PENDING;
            else if (REVENUE_OFFICER_DESGN.equalsIgnoreCase(approverDesignation))
                nextAction = WF_STATE_REVENUE_OFFICER_APPROVAL_PENDING;
            else
                nextAction = new StringBuilder().append(designation).append(" ")
                        .append(WF_STATE_COMMISSIONER_APPROVAL_PENDING).toString();
        }
        return nextAction;
    }

    public String getLoggedInUserDesignation(final Long posId, final User user) {
        final List<Assignment> loggedInUserAssign = assignmentService.getAssignmentByPositionAndUserAsOnDate(posId,
                user.getId(), new Date());
        return !loggedInUserAssign.isEmpty() ? loggedInUserAssign.get(0).getDesignation().getName() : null;
    }

    private String getWorkflowNextAction(final String designation) {
        String nextAction;
        if (designation.equalsIgnoreCase(COMMISSIONER_DESGN))
            nextAction = WF_STATE_COMMISSIONER_APPROVAL_PENDING;
        else
            nextAction = new StringBuilder().append(designation).append(" ")
                    .append(WF_STATE_COMMISSIONER_APPROVAL_PENDING).toString();
        return nextAction;
    }

    public Assignment getUserAssignmentOnReject(final CourtVerdict courtVerdict) {
        Assignment assignmentOnreject = null;
        assignmentOnreject = assignmentService
                .getAssignmentsForPosition(courtVerdict.getCurrentState().getInitiatorPosition().getId()).get(0);

        return assignmentOnreject;

    }

    public void buildSMS(final CourtVerdict courtVerdict, final String workFlowAction) {
        for (final PropertyOwnerInfo ownerInfo : courtVerdict.getBasicProperty().getPropertyOwnerInfo())
            if (StringUtils.isNotBlank(ownerInfo.getOwner().getMobileNumber()))
                buildSms(courtVerdict, ownerInfo.getOwner(), workFlowAction);
    }

    private void buildSms(final CourtVerdict courtVerdict, final User user, final String workFlowAction) {
        final String assessmentNo = courtVerdict.getBasicProperty().getUpicNo();
        final String mobileNumber = user.getMobileNumber();
        final String applicantName = user.getName();
        String smsMsg = "";
        if (workFlowAction.equals(WFLOW_ACTION_STEP_FORWARD)) {
            // to be enabled once acknowledgement feature is developed
            /*
             * smsMsg = messageSource.getMessage("msg.initiateexemption.sms", new String[] { applicantName, assessmentNo }, null);
             */
        } else if (workFlowAction.equals(WFLOW_ACTION_STEP_REJECT))
            smsMsg = ptisMessageSource.getMessage("msg.rejectexemption.sms",
                    new String[] { applicantName, assessmentNo, ApplicationThreadLocals.getMunicipalityName() }, null);
        else if (workFlowAction.equals(WFLOW_ACTION_STEP_APPROVE)) {
            final Installment installment = propertyTaxUtil.getInstallmentListByStartDate(new Date()).get(0);
            final Date effectiveDate = org.apache.commons.lang3.time.DateUtils.addDays(installment.getToDate(), 1);
            smsMsg = ptisMessageSource.getMessage("msg.approveexemption.sms",
                    new String[] { applicantName, assessmentNo,
                            new SimpleDateFormat("dd/MM/yyyy").format(effectiveDate),
                            ApplicationThreadLocals.getMunicipalityName() },
                    null);
        }

        if (StringUtils.isNotBlank(mobileNumber))
            notificationService.sendSMS(mobileNumber, smsMsg);

    }

    public CourtVerdict updatePropertyDetails(CourtVerdict courtVerdict) {
        final Character status = STATUS_WORKFLOW;
        courtVerdict.getBasicProperty().setUnderWorkflow(true);
        courtVerdict.getProperty().setPropertyModifyReason("COURTVERDICT");
        Date propCompletionDate;
        PropertyImpl newProperty;
        newProperty = courtVerdict.getProperty();
        PropertyTypeMaster propTypeMaster = propTypeMasterDAO.getPropertyTypeMasterByCode(
                courtVerdict.getProperty().getPropertyDetail().getPropertyTypeMaster().getCode());
        newProperty.getPropertyDetail().setPropertyTypeMaster(propTypeMaster);

        if (propTypeMaster.getType().equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND_STR))
            newProperty.getPropertyDetail().setPropertyType(VACANT_PROPERTY);
        else
            newProperty.getPropertyDetail().setPropertyType(BUILT_UP_PROPERTY);

        setPropertyID(newProperty);

        newProperty = propertyService.createProperty(newProperty,
                newProperty.getPropertyDetail().getSitalArea().getArea().toString(),
                newProperty.getPropertyDetail().getPropertyMutationMaster().getCode(),
                propTypeMaster.getId().toString(), null, null, status, newProperty.getPropertyDetail().getDocNumber(),
                null,
                newProperty.getPropertyDetail().getFloorType() != null
                        ? newProperty.getPropertyDetail().getFloorType().getId() : null,
                newProperty.getPropertyDetail().getRoofType() != null
                        ? newProperty.getPropertyDetail().getRoofType().getId() : null,
                newProperty.getPropertyDetail().getWallType() != null
                        ? newProperty.getPropertyDetail().getWallType().getId() : null,
                newProperty.getPropertyDetail().getWoodType() != null
                        ? newProperty.getPropertyDetail().getWoodType().getId() : null,
                null, null, newProperty.getPropertyDetail().getVacantLandPlotArea().getId(),
                newProperty.getPropertyDetail().getLayoutApprovalAuthority().getId(), Boolean.FALSE);

        if (!newProperty.getPropertyDetail().getPropertyTypeMaster().getCode()
                .equalsIgnoreCase(OWNERSHIP_TYPE_VAC_LAND))
            propCompletionDate = propertyService
                    .getLowestDtOfCompFloorWise(newProperty.getPropertyDetail().getFloorDetails());
        else
            propCompletionDate = newProperty.getPropertyDetail().getDateOfCompletion();
        newProperty.getBasicProperty().setPropOccupationDate(propCompletionDate);

        if (newProperty != null && !newProperty.getDocuments().isEmpty())
            propertyService.processAndStoreDocument(newProperty.getDocuments());
        if (propTypeMaster != null && propTypeMaster.getCode().equals(OWNERSHIP_TYPE_VAC_LAND))
            newProperty.setPropertyDetail(propertyService
                    .changePropertyDetail(newProperty, newProperty.getPropertyDetail(), 0).getPropertyDetail());
        if (newProperty.getPropertyDetail().getLayoutApprovalAuthority() != null
                && "No Approval".equals(newProperty.getPropertyDetail().getLayoutApprovalAuthority().getName())) {
            newProperty.getPropertyDetail().setLayoutPermitNo(null);
            newProperty.getPropertyDetail().setLayoutPermitDate(null);
        }
        newProperty.getPropertyDetail().setStructure(false);

        courtVerdict.setBasicProperty((BasicPropertyImpl) newProperty.getBasicProperty());
        courtVerdict.setProperty(newProperty);
        courtVerdict.setApplicationNumber(applicationNo.generate());

        return courtVerdict;

    }

    private void setPropertyID(PropertyImpl property) {
        PropertyID propertyID = property.getBasicProperty().getPropertyID();
        if (propertyID != null) {
            if (propertyID.getZone().getId() != null)
                property.getBasicProperty().getPropertyID()
                        .setZone(boundaryService.getBoundaryById(propertyID.getZone().getId()));
            if (propertyID.getWard().getId() != null)
                property.getBasicProperty().getPropertyID()
                        .setWard(boundaryService.getBoundaryById(propertyID.getWard().getId()));
            if (propertyID.getLocality().getId() != null)
                property.getBasicProperty().getPropertyID()
                        .setLocality(boundaryService.getBoundaryById(propertyID.getLocality().getId()));
            if (propertyID.getArea().getId() != null)
                property.getBasicProperty().getPropertyID()
                        .setArea(boundaryService.getBoundaryById(propertyID.getArea().getId()));
            if (propertyID.getElectionBoundary().getId() != null)
                property.getBasicProperty().getPropertyID()
                        .setElectionBoundary(boundaryService.getBoundaryById(propertyID.getElectionBoundary().getId()));
            if (propertyID.getNorthBoundary() != null)
                property.getBasicProperty().getPropertyID().setNorthBoundary(propertyID.getNorthBoundary());
            if (propertyID.getSouthBoundary() != null)
                property.getBasicProperty().getPropertyID().setSouthBoundary(propertyID.getSouthBoundary());
            if (propertyID.getEastBoundary() != null)
                property.getBasicProperty().getPropertyID().setEastBoundary(propertyID.getEastBoundary());
            if (propertyID.getWestBoundary() != null)
                property.getBasicProperty().getPropertyID().setWestBoundary(propertyID.getWestBoundary());

        }
    }

    public PropertyImpl modifyDemand(PropertyImpl newProperty, PropertyImpl oldProperty) {
        PropertyImpl modProperty = null;

        try {
            modProperty = (PropertyImpl) propertyService.modifyDemand(newProperty, oldProperty);
        } catch (final TaxCalculatorExeption e) {

            LOGGER.error("forward : There are no Unit rates defined for chosen combinations", e);
            return newProperty;
        }
        return modProperty;
    }

    public CourtVerdict getCourtVerdictById(final Long id) {
        return courtVerdictRepo.findOne(id);
    }

    public void updateDemandDetails(CourtVerdict courtVerdict) {

        Set<EgDemandDetails> demandDetails = propertyService.getCurrrentDemand(courtVerdict.getProperty()).getEgDemandDetails();

        for (final EgDemandDetails dmdDetails : demandDetails)
            for (final DemandDetail dmdDetailBean : courtVerdict.getDemandDetailBeanList()) {
                Boolean isUpdateAmount = Boolean.FALSE;
                Boolean isUpdateCollection = Boolean.FALSE;
                dmdDetailBean.setInstallment(installmentDao.findById(dmdDetailBean.getInstallment().getId(), false));
                if (dmdDetailBean.getRevisedAmount() != null
                        && dmdDetailBean.getInstallment()
                                .equals(dmdDetails.getEgDemandReason().getEgInstallmentMaster())
                        && dmdDetails.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster()
                                .equalsIgnoreCase(dmdDetailBean.getReasonMaster()))
                    isUpdateAmount = true;

                if (dmdDetailBean.getRevisedCollection() != null
                        && dmdDetails.getEgDemand().getEgInstallmentMaster()
                                .equals(propertyTaxCommonUtils.getCurrentInstallment())
                        && dmdDetails.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster()
                                .equalsIgnoreCase(dmdDetailBean.getReasonMaster())
                        && dmdDetails.getEgDemandReason().getEgInstallmentMaster()
                                .equals(dmdDetailBean.getInstallment()))
                    isUpdateCollection = true;

                if (isUpdateAmount)
                    dmdDetails.setAmount(dmdDetailBean.getRevisedAmount() != null
                            ? dmdDetailBean.getActualAmount().subtract(dmdDetailBean.getRevisedAmount())
                            : BigDecimal.ZERO);
                if (isUpdateCollection)
                    dmdDetails.setAmtCollected(
                            dmdDetailBean.getRevisedCollection() != null ? dmdDetailBean.getRevisedCollection()
                                    : BigDecimal.ZERO);

                if (isUpdateAmount || isUpdateCollection) {
                    dmdDetails.setModifiedDate(new Date());
                    break;
                }
            }
        final List<Ptdemand> currPtdemand;
        final javax.persistence.Query qry = entityManager.createNamedQuery("QUERY_CURRENT_PTDEMAND");
        qry.setParameter("basicProperty", courtVerdict.getProperty().getBasicProperty());
        qry.setParameter("installment", propertyTaxCommonUtils.getCurrentInstallment());
        currPtdemand = qry.getResultList();

        if (currPtdemand != null) {
            final Ptdemand ptdemand = (Ptdemand) currPtdemand.get(0).clone();
            ptdemand.setBaseDemand(getTotalDemand(demandDetails));
            ptdemand.setEgDemandDetails(demandDetails);
            ptdemand.setEgptProperty(courtVerdict.getProperty());
            ptdemand.getDmdCalculations().setCreatedDate(new Date());
            persistenceService.applyAuditing(ptdemand.getDmdCalculations());
            courtVerdict.getProperty().getPtDemandSet().clear();
            courtVerdict.getProperty().getPtDemandSet().add(ptdemand);
        }
    }

    private BigDecimal getTotalDemand(Set<EgDemandDetails> dmndDetails) {
        BigDecimal totalDmd = BigDecimal.ZERO;
        for (EgDemandDetails newDemandDetails : dmndDetails) {
            totalDmd = totalDmd.add(newDemandDetails.getAmount());
        }
        return totalDmd;
    }

    public Map<String, String> validateDemand(List<DemandDetail> demandDetailBeanList) {

        HashMap<String, String> errors = new HashMap<>();

        for (final DemandDetail dd : demandDetailBeanList) {
            dd.setInstallment(installmentDao.findById(dd.getInstallment().getId(), false));
            if (dd.getRevisedCollection().compareTo(dd.getActualAmount().subtract(dd.getRevisedAmount())) > 0) {
                errors.put("revisedCollection",
                        "revised.collection.greater");
            }
        }
        return errors;
    }

    public void addDemandDetails(CourtVerdict courtVerdict) {

        List<DemandDetail> demandDetailList = getDemandDetails(courtVerdict);
        courtVerdict.setDemandDetailBeanList(demandDetailList);

    }

    private List<DemandDetail> setDemandBeanList(List<EgDemandDetails> newDmndDetails, List<EgDemandDetails> oldDmndDetails) {

        List<DemandDetail> demandDetailList = new ArrayList<>();

        int i = 0;
        for (final EgDemandDetails demandDetail : newDmndDetails) {
            for (final EgDemandDetails oldDemandDetail : oldDmndDetails) {
                if (oldDemandDetail.getEgDemandReason().getEgInstallmentMaster()
                        .equals(demandDetail.getEgDemandReason().getEgInstallmentMaster())
                        && oldDemandDetail.getEgDemandReason().getEgDemandReasonMaster()
                                .equals(demandDetail.getEgDemandReason().getEgDemandReasonMaster())) {
                    final Installment installment = demandDetail.getEgDemandReason().getEgInstallmentMaster();
                    final String reasonMaster = demandDetail.getEgDemandReason().getEgDemandReasonMaster()
                            .getReasonMaster();
                    final BigDecimal revisedAmount = oldDemandDetail.getAmount().subtract(demandDetail.getAmount());
                    final BigDecimal revisedCollection = demandDetail.getAmtCollected();
                    final DemandDetail dmdDtl = createDemandDetailBean(installment, reasonMaster, oldDemandDetail.getAmount(),
                            revisedAmount,
                            oldDemandDetail.getAmtCollected(), revisedCollection);
                    demandDetailList.add(i, dmdDtl);

                    break;
                }
            }
            i++;
        }
        return demandDetailList;
    }

    private DemandDetail createDemandDetailBean(final Installment installment, final String reasonMaster,
            final BigDecimal amount, final BigDecimal revisedAmount, final BigDecimal amountCollected,
            final BigDecimal revisedCollection) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Entered into createDemandDetailBean");
            LOGGER.debug("createDemandDetailBean - installment=" + installment + ", reasonMaster=" + reasonMaster
                    + ", amount=" + amount + ", amountCollected=" + amountCollected);
        }

        final DemandDetail demandDetail = new DemandDetail();
        demandDetail.setInstallment(installment);
        demandDetail.setReasonMaster(reasonMaster);
        demandDetail.setActualAmount(amount);
        demandDetail.setRevisedAmount(revisedAmount);
        demandDetail.setActualCollection(amountCollected);
        demandDetail.setRevisedCollection(revisedCollection);
        demandDetail.setIsCollectionEditable(true);
        return demandDetail;
    }

    public CourtVerdict updateDemand(CourtVerdict courtVerdict) {

        List<DemandDetail> demandDetailList = getDemandDetails(courtVerdict);
        BigDecimal totalCollectionAmt = BigDecimal.ZERO;
        for (DemandDetail demandDetail : demandDetailList) {
            if (demandDetail.getActualCollection().compareTo(demandDetail.getRevisedCollection()) >= 0)
                totalCollectionAmt = totalCollectionAmt.add(demandDetail.getActualCollection());
            else
                totalCollectionAmt = totalCollectionAmt.add(demandDetail.getRevisedCollection());
        }

        Ptdemand ptDemandNew = propertyService.getCurrrentDemand(courtVerdict.getProperty());

        if (ptDemandNew.getEgDemandDetails() != null) {
            for (EgDemandDetails egDemandDetails : ptDemandNew.getEgDemandDetails()) {

                totalCollectionAmt = updateCollection(totalCollectionAmt, egDemandDetails);
            }

            if (totalCollectionAmt.compareTo(BigDecimal.ZERO) > 0) {
                final Installment currSecondHalf = propertyTaxUtil.getInstallmentsForCurrYear(new Date())
                        .get(CURRENTYEAR_SECOND_HALF);
                final EgDemandDetails advanceDemandDetails = ptBillServiceImpl.getDemandDetail(ptDemandNew, currSecondHalf,
                        DEMANDRSN_CODE_ADVANCE);
                if (advanceDemandDetails == null) {
                    final EgDemandDetails dmdDetails = ptBillServiceImpl.insertDemandDetails(DEMANDRSN_CODE_ADVANCE,
                            totalCollectionAmt, currSecondHalf);
                    ptDemandNew.getEgDemandDetails().add(dmdDetails);
                } else
                    advanceDemandDetails.getAmtCollected().add(totalCollectionAmt);
            }
        }

        return courtVerdict;
    }

    private BigDecimal updateCollection(BigDecimal totalColl, EgDemandDetails newDemandDetail) {
        BigDecimal remaining = totalColl;
        if (newDemandDetail != null) {
            newDemandDetail.setAmtCollected(ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                if (remaining.compareTo(newDemandDetail.getAmount()) <= 0) {
                    newDemandDetail.setAmtCollected(remaining);
                    newDemandDetail.setModifiedDate(new Date());
                    remaining = BigDecimal.ZERO;
                } else {
                    newDemandDetail.setAmtCollected(newDemandDetail.getAmount());
                    newDemandDetail.setModifiedDate(new Date());
                    remaining = remaining.subtract(newDemandDetail.getAmount());
                }
            }
        }
        return remaining;
    }

    public List<DemandDetail> getDemandDetails(CourtVerdict courtVerdict) {
        Set<EgDemandDetails> newDemandDetails = (ptDemandDAO.getNonHistoryCurrDmdForProperty(courtVerdict.getProperty()))
                .getEgDemandDetails();
        Set<EgDemandDetails> oldDemandDetails = (ptDemandDAO
                .getNonHistoryCurrDmdForProperty(courtVerdict.getBasicProperty().getProperty()))
                        .getEgDemandDetails();
        List<EgDemandDetails> newDmndDetails = new ArrayList<>(newDemandDetails);
        List<EgDemandDetails> oldDmndDetails = new ArrayList<>(oldDemandDetails);

        if (!newDmndDetails.isEmpty())
            newDmndDetails = sortDemandDetails(newDmndDetails);

        if (!oldDmndDetails.isEmpty())
            oldDmndDetails = sortDemandDetails(oldDmndDetails);

        return setDemandBeanList(newDmndDetails, oldDmndDetails);
    }

    public List<DemandDetail> setDemandBeanList(List<EgDemandDetails> demandDetails) {

        List<DemandDetail> demandDetailList = new ArrayList<>();

        for (final EgDemandDetails demandDetail : demandDetails) {
            final Installment installment = demandDetail.getEgDemandReason().getEgInstallmentMaster();
            final String reasonMaster = demandDetail.getEgDemandReason().getEgDemandReasonMaster()
                    .getReasonMaster();
            final DemandDetail dmdDtl = createDemandDetailBean(installment, reasonMaster, demandDetail.getAmount(),
                    demandDetail.getAmtCollected());
            demandDetailList.add(dmdDtl);
        }
        return demandDetailList;
    }

    private DemandDetail createDemandDetailBean(final Installment installment, final String reasonMaster,
            final BigDecimal amount, final BigDecimal amountCollected) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Entered into createDemandDetailBean");
            LOGGER.debug("createDemandDetailBean - installment=" + installment + ", reasonMaster=" + reasonMaster
                    + ", amount=" + amount + ", amountCollected=" + amountCollected);
        }

        final DemandDetail demandDetail = new DemandDetail();
        demandDetail.setInstallment(installment);
        demandDetail.setReasonMaster(reasonMaster);
        demandDetail.setActualAmount(amount);
        demandDetail.setActualCollection(amountCollected);
        demandDetail.setIsCollectionEditable(true);
        return demandDetail;
    }

    public List<EgDemandDetails> sortDemandDetails(List<EgDemandDetails> demandDetails) {
        Collections.sort(demandDetails, new Comparator<EgDemandDetails>() {

            @Override
            public int compare(EgDemandDetails dmdDtl1, EgDemandDetails dmdDtl2) {
                return dmdDtl1.getEgDemandReason().getEgInstallmentMaster()
                        .compareTo(dmdDtl2.getEgDemandReason().getEgInstallmentMaster());
            }

        }.thenComparing(new Comparator<EgDemandDetails>() {

            @Override
            public int compare(EgDemandDetails dmdDtl1, EgDemandDetails dmdDtl2) {
                return dmdDtl1.getEgDemandReason().getEgDemandReasonMaster().getOrderId()
                        .compareTo(dmdDtl2.getEgDemandReason().getEgDemandReasonMaster().getOrderId());
            }
        }));
        return demandDetails;
    }
}