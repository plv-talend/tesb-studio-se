// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.bean;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PartInitException;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.util.CamelRepositoryNodeType;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: EditProcess.java 1495 2007-01-18 04:31:30Z nrousseau $
 * 
 */
public class ReadCamelBean extends AbstractBeanAction {// AbstractProcessAction

    private static final String LABEL = Messages.getString("ReadBean.label"); //$NON-NLS-1$

    public ReadCamelBean() {
        super();
        this.setText(LABEL);
        this.setToolTipText(LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.READ_ICON));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    protected void doRun() {
        if (repositoryNode == null && getSelection() != null) {
            repositoryNode = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();
        }
        BeanItem beanItem = (BeanItem) repositoryNode.getObject().getProperty().getItem();

        Property updatedProperty = null;
        try {
            updatedProperty = ProxyRepositoryFactory
                    .getInstance()
                    .getLastVersion(new Project(ProjectManager.getInstance().getProject(beanItem)),
                            beanItem.getProperty().getId()).getProperty();

            // repositoryNode.getObject().setProperty(updatedProperty);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        beanItem = (BeanItem) repositoryNode.getObject().getProperty().getItem();
        try {
            openBeanEditor(beanItem, true);
        } catch (PartInitException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (SystemException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case REPOSITORY_ELEMENT:
                if (node.getObjectType() != CamelRepositoryNodeType.repositoryBeansType) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
            }
            if (canWork && node.getObject() != null
                    && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.LOCK_BY_USER) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }
}
