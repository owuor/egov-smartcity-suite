/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
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
 */

package org.egov.works.web.controller.abstractestimate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.egov.works.abstractestimate.entity.AbstractEstimate;
import org.egov.works.abstractestimate.entity.Activity;
import org.egov.works.abstractestimate.service.EstimateService;
import org.egov.works.utils.WorksUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/abstractestimate")
public class AbstractEstimatePDFController {

    @Autowired
    private EstimateService estimateService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private WorksUtils worksUtils;

    public static final String ABSTRACTESTIMATEPDF = "abstractEstimatePDF";
    private final Map<String, Object> reportParams = new HashMap<String, Object>();
    private ReportRequest reportInput = null;
    private ReportOutput reportOutput = null;

    @RequestMapping(value = "/abstractEstimatePDF/{estimateId}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> generateAbstractEstimatePDF(final HttpServletRequest request,
            @PathVariable("estimateId") final Long id, final HttpSession session) throws IOException {
        final AbstractEstimate abstractEstimate = estimateService.getAbstractEstimateById(id);
        return generateReport(abstractEstimate, request, session);
    }

    private ResponseEntity<byte[]> generateReport(final AbstractEstimate abstractEstimate,
            final HttpServletRequest request, final HttpSession session) {
        final List<Activity> activities = new ArrayList<Activity>();
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        final String cityName = request.getSession().getAttribute("citymunicipalityname").toString();
        reportParams.put("cityName", cityName);
        if (abstractEstimate != null) {
            reportParams.put("estimateDate", formatter.format(abstractEstimate.getEstimateDate()));
            activities.addAll(abstractEstimate.getSORActivities());
            activities.addAll(abstractEstimate.getNonSORActivities());
            if (abstractEstimate.getState() != null)
                reportParams.put("workflowdetails",
                        worksUtils.getWorkFlowHistory(abstractEstimate.getState(), abstractEstimate.getStateHistory()));

            reportParams.put("activities", activities);
        }
        reportParams.put("currDate", formatter.format(new Date()));
        reportInput = new ReportRequest(ABSTRACTESTIMATEPDF, abstractEstimate, reportParams);
        final HttpHeaders headers = new HttpHeaders();
        ;
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.add("content-disposition",
                "inline;filename=AbstractEstimate_" + abstractEstimate.getEstimateNumber() + ".pdf");
        reportOutput = reportService.createReport(reportInput);
        return new ResponseEntity<byte[]>(reportOutput.getReportOutputData(), headers, HttpStatus.CREATED);

    }

}
