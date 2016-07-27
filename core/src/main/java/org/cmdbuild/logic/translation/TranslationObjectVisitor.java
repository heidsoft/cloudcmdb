package org.cmdbuild.logic.translation;

import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;
import org.cmdbuild.logic.translation.object.ClassDescription;
import org.cmdbuild.logic.translation.object.DomainAttributeDescription;
import org.cmdbuild.logic.translation.object.DomainDescription;
import org.cmdbuild.logic.translation.object.DomainDirectDescription;
import org.cmdbuild.logic.translation.object.DomainInverseDescription;
import org.cmdbuild.logic.translation.object.DomainMasterDetailLabel;
import org.cmdbuild.logic.translation.object.FilterDescription;
import org.cmdbuild.logic.translation.object.InstanceName;
import org.cmdbuild.logic.translation.object.LookupDescription;
import org.cmdbuild.logic.translation.object.MenuItemDescription;
import org.cmdbuild.logic.translation.object.ReportDescription;
import org.cmdbuild.logic.translation.object.ViewDescription;
import org.cmdbuild.logic.translation.object.WidgetLabel;

public interface TranslationObjectVisitor {

	void visit(ClassAttributeDescription classAttributeDescription);

	void visit(ClassAttributeGroup classAttributeGroup);

	void visit(ClassDescription classDescription);

	void visit(DomainAttributeDescription domainAttributeDescription);

	void visit(DomainDescription domainDescription);

	void visit(DomainDirectDescription domainDirectDescription);

	void visit(DomainInverseDescription domainInverseDescription);

	void visit(DomainMasterDetailLabel domainMasterDetailDescription);

	void visit(FilterDescription filterDescription);

	void visit(InstanceName instanceNameTranslation);

	void visit(LookupDescription lookupDescription);

	void visit(MenuItemDescription menuItemDescription);

	void visit(NullTranslationObject translationObject);

	void visit(ReportDescription reportDescription);

	void visit(ViewDescription viewDescription);

	void visit(WidgetLabel widgetLabel);

}
