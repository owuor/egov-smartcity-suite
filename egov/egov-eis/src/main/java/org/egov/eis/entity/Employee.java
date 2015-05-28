/**
 * eGov suite of products aim to improve the internal efficiency,transparency,
   accountability and the service delivery of the government  organizations.

    Copyright (C) <2015>  eGovernments Foundation

    The updated version of eGov suite of products as by eGovernments Foundation
    is available at http://www.egovernments.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/ or
    http://www.gnu.org/licenses/gpl.html .

    In addition to the terms of the GPL license to be adhered to in using this
    program, the following additional terms are to be complied with:

	1) All versions of this program, verbatim or modified must carry this
	   Legal Notice.

	2) Any misrepresentation of the origin of the material is prohibited. It
	   is required that all modified versions of this material be marked in
	   reasonable ways as different from the original version.

	3) This license does not grant any rights to any user of the program
	   with regards to rights under trademark law for use of the trade names
	   or trademarks of eGovernments Foundation.

  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.eis.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.egov.eis.entity.enums.EmployeeStatus;
import org.egov.infra.admin.master.entity.AbstractUser;
import org.egov.infra.admin.master.entity.enums.UserType;
import org.egov.infra.validation.regex.Constants;
import org.egov.search.domain.Searchable;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

@Entity
@Table(name = "egeis_employee")
@SecondaryTable(name = "eg_user")
public class Employee extends AbstractUser {

    private static final long serialVersionUID = -1105585841211211215L;

    public Employee() {
        setType(UserType.EMPLOYEE);
    }

    @NotNull
    @SafeHtml
    @Column(name = "code", unique = true)
    @Pattern(regexp = Constants.ALPHANUMERIC)
    private String code;

    @NotNull
    @Temporal(value = TemporalType.DATE)
    private Date dateOfAppointment;

    @NotNull
    @Temporal(value = TemporalType.DATE)
    private Date dateOfRetirement;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Searchable(group = Searchable.Group.CLAUSES)
    private EmployeeStatus employeeStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeetype")
    private EmployeeType employeeType;

    @OneToMany(mappedBy = "employee", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final Set<Assignment> assignments = new HashSet<Assignment>(0);

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public DateTime getDateOfAppointment() {
        return null == dateOfAppointment ? null : new DateTime(dateOfAppointment);
    }

    public void setDateOfAppointment(final DateTime dateOfAppointment) {
        this.dateOfAppointment = null == dateOfAppointment ? null : dateOfAppointment.toDate();
    }

    public DateTime getDateOfRetirement() {
        return null == dateOfRetirement ? null : new DateTime(dateOfRetirement);
    }

    public void setDateOfRetirement(final DateTime dateOfRetirement) {
        this.dateOfRetirement = null == dateOfRetirement ? null : dateOfRetirement.toDate();
    }

}
