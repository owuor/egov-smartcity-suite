<!-- #-------------------------------------------------------------------------------
# eGov suite of products aim to improve the internal efficiency,transparency,
#    accountability and the service delivery of the government  organizations.
# 
#     Copyright (C) <2015>  eGovernments Foundation
# 
#     The updated version of eGov suite of products as by eGovernments Foundation
#     is available at http://www.egovernments.org
# 
#     This program is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     any later version.
# 
#     This program is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
# 
#     You should have received a copy of the GNU General Public License
#     along with this program. If not, see http://www.gnu.org/licenses/ or
#     http://www.gnu.org/licenses/gpl.html .
# 
#     In addition to the terms of the GPL license to be adhered to in using this
#     program, the following additional terms are to be complied with:
# 
# 	1) All versions of this program, verbatim or modified must carry this
# 	   Legal Notice.
# 
# 	2) Any misrepresentation of the origin of the material is prohibited. It
# 	   is required that all modified versions of this material be marked in
# 	   reasonable ways as different from the original version.
# 
# 	3) This license does not grant any rights to any user of the program
# 	   with regards to rights under trademark law for use of the trade names
# 	   or trademarks of eGovernments Foundation.
# 
#   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
#------------------------------------------------------------------------------- -->
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
	<head>  
	    <title> <s:text name="page.title.asset.create" /></title>
	</head> 
	<body id="home">

		<s:if test="%{hasErrors()}">
			<div class="errorstyle">
				<s:actionerror />
				<s:fielderror />
			</div>
		</s:if>
		<s:if test="%{hasActionMessages()}">
			<div class="messagestyle">
				<s:actionmessage theme="simple" />
			</div>
		</s:if>
		<script>
			function enableFields(){
				for(i=0;i<document.assetForm.elements.length;i++){
				        document.assetForm.elements[i].disabled=false;
				}   
			}
		</script>
		<s:form action="asset" theme="simple" name="assetForm">
		<s:token/>		
			<div class="errorstyle" id="asset_error" style="display:none;"></div>
			<s:push value="model">
				<div class="navibarshadowwk">
				</div>
				<div class="formmainbox">
					<div class="insidecontent">
						<div class="rbroundbox2">
							<div class="rbtop2">
							</div>
							<div class="rbcontent2">
								<div class="datewk">
									<span class="bold">Today</span>
									<egov:now />
								</div>
								<s:hidden name="id" />
								<s:hidden name="rowId" id="rowid"/>
								<%@ include file='asset-form.jsp'%>
							</div>
							<div class="rbbot2">
							</div>
						</div>
					</div>
				</div>
				<s:if test="%{not id}">
					<div class="buttonholderwk" id="divButRow1" name="divButRow1">
						<s:submit cssClass="buttonfinal" value="CREATE" id="submitButton" method="create" 
							onclick="return validateFormAndSubmit();"/>
						<input type="button" class="buttonfinal" value="CLOSE"
							id="closeButton" name="button"
							onclick="window.close();" />
					</div>
				</s:if>
				<s:else>
					<div class="buttonholderwk" id="divButRow1" name="divButRow1">
						<input type="button" class="buttonfinal" value="ADD AND RETURN"
							id="saveButton" name="button"
							onclick="javascript:returnBackToParent('<s:property value="id"/>','<s:property value="code"/>','<s:property value="name"/>')" />
					</div>
				</s:else>
			</s:push>
		</s:form>
		<script type="text/javascript">
			<s:if test="%{id!=null}">
				links=document.assetForm.getElementsByTagName("a"); 
		        disableLinks(links,[]);
			</s:if>
			
			$('tdstatmain').show();
			$('tdstatalt').hide();
			
			document.getElementById('dateOfCreation').readonly=true;
			document.getElementById('dateOfCreation').disabled=true;
			
			function returnBackToParent(id,code,name) {
				var wind;
				var data = new Array();
				row_id = $('rowid').value;
				wind=window.dialogArguments;
				if(wind==undefined){
					wind=window.opener;
					data = row_id + '`~`' + id + '`~`' + code + '`~`' + name;
					window.opener.update(data);
				}
		
				else{
					wind=window.dialogArguments;
					wind.result = row_id + '`~`' + id + '`~`' + code + '`~`' + name;
				}
				window.close();
			}
		</script>
	</body>
</html>
