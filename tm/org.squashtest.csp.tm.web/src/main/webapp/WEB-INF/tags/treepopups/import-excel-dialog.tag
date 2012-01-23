<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag body-content="empty" description="javascript handling the copy and paste of nodes in the tree"%>
	

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>	
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>

	
<%@ attribute name="treeSelector" 		required="true" description="jquery selector of the tree instance" %>
<%@ attribute name="treeNodeButton"		required="true" description="the javascript button that will open the dialog" %>
<%@ attribute name="workspace" 			required="true" description="the workspace (or nature) of the elements to import." %>
<%@ attribute name="targetLibraries" 	required="true" description="the potential target libraries for upload." type="java.lang.Object" %>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.form.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/FeedbackMultipartPopup.js"></script> 
<%--  <script type="text/javascript" src="http://localhost/scripts/FeedbackMultipartPopup.js"></script> --%>  


<s:url var="importUrl" value="/${workspace}-browser/import/upload"/>

<%-- 
	Note : as long as this popup is open if and only if exactly one drive node is selected, the following code is safe.
	if not, consider checking the results of $(tree).jstree("get_selected");

 --%>


<pop:popup id="import-excel-dialog" titleKey="dialog.import-excel.title" isContextual="false"  closeOnSuccess="false">
	<jsp:attribute name="buttonsArray">	
		<f:message var="confirmLabel" key="dialog.import.confirm.label" />	
		<f:message var="cancelLabel" key="dialog.button.cancel.label"/>
		<f:message var="okLabel" key="dialog.button.ok.label"/>
		{
			text : "${confirmLabel}",
			"class" : FeedbackMultipartPopup.PARAMETRIZATION,
			click : function(){	importExcelFeedbackPopup.validate();}
		},
		{		
			text : "${okLabel}",
			"class" : FeedbackMultipartPopup.SUMMARY,
			click : function(){
				var thisDialog = $("#import-excel-dialog");
				thisDialog.dialog("close");
				
				var jqTree = $("${treeSelector}");
				var projectId = thisDialog.find("select[name='projectId']").val();
				var node =jqTree.find("li:library[resid='"+projectId+"']");
				
				if (node.size()>0){
					jqTree.jstree("refresh", node);
				}
			}
		},
		{
			text : "${okLabel}",
			"class" : FeedbackMultipartPopup.CONFIRM,
			click : function(){
				importExcelFeedbackPopup.submit();
			}
		
		},
		{
			text : "${cancelLabel}",
			"class" : FeedbackMultipartPopup.PROGRESSION+" "+FeedbackMultipartPopup.PARAMETRIZATION+" "+FeedbackMultipartPopup.CONFIRM,
			click : function(){importExcelFeedbackPopup.cancel();}			
		}
	</jsp:attribute>
	
	<jsp:attribute name="additionalSetup">
		open : function(){
			importExcelFeedbackPopup.reset();
		}	
	</jsp:attribute>

	<jsp:attribute name="body">
		<div class="parametrization">
			
			<div style="margin-top:1em;margin-bottom:1em;">
				<form action="${importUrl}" method="POST" enctype="multipart/form-data" class="display-table">

					<div class="display-table-row">
						<div class="display-table-cell"><label><f:message key="dialog.import.project.message"/></label></div>
						<div class="display-table-cell">
							<select name="projectId" 
								onchange="var projectname = $(':selected', this).text(); $('#import-excel-dialog .confirm-project').text(projectname);">
								<c:forEach items="${targetLibraries}" var="lib" varStatus="status" >
								<%-- warning : c tag nested in another c tag --%>
								<option value="${lib.id}" <c:if test="${status.first}">selected="yes"</c:if>>${lib.project.name}</option>
								</c:forEach>
							</select>
						
						</div>
					</div>
					<div class="display-table-row">
						<div class="display-table-cell"><label><f:message key="dialog.import.filetype.message"/></label></div>
						<div class="display-table-cell">
							<input type="file" name="archive" size="20" accept="application/zip" 
							onchange="var filename = /([^\\]+)$/.exec(this.value)[1]; $('#import-excel-dialog .confirm-file').text(filename);"/>
						</div>
					</div>
					<div class="display-table-row">
						<div class="display-table-cell"><label><f:message key="dialog.import.encoding.label"/></label></div>
						<div class="display-table-cell"><select name="zipEncoding">
							<option value="Cp858">Windows <f:message key="dialog.import.encoding.default"/></option>
							<option value="UTF8">UTF-8</option>
						</select></div>
					</div>

				</form>
			
			</div>
		</div>
		
		<div class="confirm">
			<div class="confirm-div">
				<label class="confirm-label"><f:message key="dialog.import.file.confirm"/></label>
				<span class="confirm-span confirm-file"></span>
			</div>
			<div class="confirm-div">
				<label class="confirm-label"><f:message key="dialog.import.project.confirm"/></label>
				<span class="confirm-span confirm-project"><c:out value="${targetLibraries[0].project.name}"/></span>
			</div>
			
			<span style="display:block"><f:message key="dialog.import.confirm.message"/></span>
		</div>
		
		<div class="summary">
			<div>
				<span><f:message key="dialog.import-excel.test-case.total"/></span><span class="total-import span-bold"></span>
			</div>
			<div>
				<span><f:message key="dialog.import-excel.test-case.success"/></span><span class="success-import span-bold span-green"></span>
			</div>
			<div>
				<span><f:message key="dialog.import-excel.test-case.failed"/></span><span class="failures-import span-bold"></span>
			</div>			
			
			
			<div class="import-excel-dialog-note">
				
				<hr/>
				<span><f:message key="dialog.import.summary.notes.label"/></span>
				<ul>
					<li class="import-excel-dialog-renamed"><span><f:message key="dialog.import-excel.test-case.warnings.renamed"/></span></li>
					<li class="import-excel-dialog-modified"><span><f:message key="dialog.import-excel.test-case.warnings.modified"/></span></li>	
				</ul>		
			</div>
		</div>
		
	</jsp:attribute>	

</pop:popup>


<f:message var="wrongFileMessage" key="dialog.import.wrongfile" />
<script type="text/javascript">


	var importExcelFeedbackPopup = null;
	
	
	function importSummaryBuilder(response){
			
		var panel = $("#import-excel-dialog .summary");
		
		//basic infos			
		$(".total-import", panel).text(response.total);
		$(".success-import", panel).text(response.success);
		
		var failSpan = $(".failures-import", panel).text(response.failures);
		if (response.failures==0){ failSpan.removeClass("span-red"); }else{	failSpan.addClass("span-red"); }
		
		//notes
		if ((response.renamed==0) && (response.modified==0)){
			$(".import-excel-dialog-note", panel).hide();
		}else{
			$(".import-excel-dialog-note", panel).show();
			
			var renamedDialog = $(".import-excel-dialog-renamed", panel);
			if (response.renamed>0) { renamedDialog.show(); } else { renamedDialog.hide(); }

			var modifiedDialog = $(".import-excel-dialog-modified", panel);
			if (response.modified>0) { modifiedDialog.show(); } else { modifiedDialog.hide(); }
			
		}
		
	}
	
	
	
	$(function(){		
		
		var settings = {
				
			popup : $("#import-excel-dialog"),
			
			parametrization : {
				submitUrl : "${importUrl}",
				extensions : [ "zip" ],
				errorMessage : "${wrongFileMessage} zip"
			},
			
			summary : {
				builder : importSummaryBuilder
			}
				
		};
		
		importExcelFeedbackPopup = new FeedbackMultipartPopup(settings);
		
		
		${treeNodeButton}.click(function(){
			$('#import-excel-dialog').dialog('open');
			return false;		
		});		
	});
	
	
</script>
