<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/jsp/common/taglibs.jspf"%>
<es:contentHeader/>
<div class="panel">

    <ul class="nav nav-tabs">
        <c:if test="${op eq '新增'}">
            <li <c:if test="${op eq '新增'}">class="active"</c:if>>
                <a href="${ctx}/showcase/status/audit/create?BackURL=<es:BackURL/>">
                    <i class="icon-file"></i>
                    新增
                </a>
            </li>
        </c:if>

        <c:if test="${not empty m.id}">
            <li <c:if test="${op eq '查看'}">class="active"</c:if>>
                <a href="${ctx}/showcase/status/show/${m.id}?BackURL=<es:BackURL/>">
                    <i class="icon-eye-open"></i>
                    查看
                </a>
            </li>
            <li <c:if test="${op eq '修改'}">class="active"</c:if>>
                <a href="${ctx}/showcase/status/show/update/${m.id}?BackURL=<es:BackURL/>">
                    <i class="icon-edit"></i>
                    修改
                </a>
            </li>
            <li <c:if test="${op eq '删除'}">class="active"</c:if>>
                <a href="${ctx}/showcase/status/show/delete/${m.id}?BackURL=<es:BackURL/>">
                    <i class="icon-trash"></i>
                    删除
                </a>
            </li>
        </c:if>
        <li>
            <a href="<es:BackURL/>" class="btn btn-link">
                <i class="icon-reply"></i>
                返回
            </a>
        </li>
    </ul>
    <form:form id="editForm" method="post" commandName="m" cssClass="form-horizontal">

            <es:showGlobalError commandName="m"/>

            <form:hidden path="id"/>


            <div class="control-group">
                <form:label path="name" cssClass="control-label">名称</form:label>
                <div class="controls">
                    <form:input path="name" cssClass="validate[required]"/>
                </div>
            </div>

            <div class="control-group">
                <form:label path="name" cssClass="control-label">是否显示</form:label>
                <div class  ="controls inline-radio">
                    <form:radiobuttons path="status" items="${statusList}" itemLabel="info" cssClass="validate[required]"/>
                </div>
            </div>

            <c:if test="${op eq '新增'}">
                <c:set var="icon" value="icon-file"/>
            </c:if>
            <c:if test="${op eq '修改'}">
                <c:set var="icon" value="icon-edit"/>
            </c:if>
            <c:if test="${op eq '删除'}">
                <c:set var="icon" value="icon-trash"/>
            </c:if>

            <div class="control-group">
                <div class="controls">
                    <button type="submit" class="btn btn-primary">
                        <i class="${icon}"></i>
                            ${op}
                    </button>
                    <a href="<es:BackURL/>" class="btn">
                        <i class="icon-reply"></i>
                        返回
                    </a>
                </div>
            </div>

    </form:form>
</div>
<es:contentFooter/>
<script type="text/javascript">
    $(function () {
        <c:choose>
            <c:when test="${op eq '删除'}">
                //删除时不验证 并把表单readonly
                $.app.readonlyForm($("#editForm"), false);
            </c:when>
            <c:when test="${op eq '查看'}">
                $.app.readonlyForm($("#editForm"), true);
            </c:when>
            <c:otherwise>
                var validationEngine = $("#editForm").validationEngine();
                <es:showFieldError commandName="m"/>
            </c:otherwise>
        </c:choose>
    });
</script>