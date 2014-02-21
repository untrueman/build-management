/*
 * Copyright 2002 by Shawn Stafford (sstafford@modeln.com)
 * All rights reserved.
 */
package com.modeln.build.ctrl.command.patch; 

import com.modeln.build.common.data.product.CMnBaseFix;
import com.modeln.build.common.data.product.CMnBaseFixDependency;
import com.modeln.build.common.data.product.CMnPatch;
import com.modeln.build.common.data.product.CMnPatchFix;
import com.modeln.build.ctrl.CMnControlApp;
import com.modeln.build.ctrl.database.CMnPatchTable;
import com.modeln.build.ctrl.forms.CMnBaseForm;
import com.modeln.build.ctrl.forms.IMnPatchForm;
import com.modeln.testfw.reporting.CMnBuildTable;
import com.modeln.testfw.reporting.CMnDbBuildData;


import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.SQLException;

import com.modeln.build.web.errors.ApplicationError;
import com.modeln.build.web.errors.ApplicationException;
import com.modeln.build.web.application.CommandResult;
import com.modeln.build.web.application.AdminCommand;
import com.modeln.build.web.application.WebApplication;
import com.modeln.build.web.database.RepositoryConnection;
import com.modeln.build.web.errors.ErrorMap;


/**
 * This command allows a user to add a new dependency.
 * 
 * @author             Shawn Stafford
 */
public class CMnDependencyAdd extends AdminCommand {

    /**
     * This is the primary method which will be used to perform the command
     * actions.  The application will use this method to service incoming
     * requests.  You must pass a reference to the calling application into
     * the service method to allow callback method calls to be performed.
     *
     * @param   app     Application which called the command
     * @param   req     HttpServletRequest object
     * @param   res     HttpServletResponse object
     */
    public CommandResult execute(WebApplication app, HttpServletRequest req, HttpServletResponse res) 
        throws ApplicationException
    {
        // Execute the generic actions for all commands
        CommandResult result = super.execute(app, req, res);

        // Execute the actions for the command
        if (!result.containsError()) {
            ApplicationException exApp = null;
            ApplicationError error = null;
            RepositoryConnection rc = null;
            try {
                rc = app.getRepositoryConnection();
                CMnPatchTable patchTable = CMnPatchTable.getInstance();

                // Keep track of field validation errors
                Hashtable<String,String> inputErrors = new Hashtable<String,String>();


                // Fall back to the request attributes in case the data was set by another command
                String patchId = (String) req.getParameter(IMnPatchForm.PATCH_ID_LABEL);
                if (patchId == null) {
                    patchId = (String) req.getAttribute(IMnPatchForm.PATCH_ID_LABEL);
                }

                String bugId = (String) req.getParameter(IMnPatchForm.FIX_BUG_LABEL);
                if (bugId == null) {
                    bugId = (String) req.getAttribute(IMnPatchForm.FIX_BUG_LABEL);
                }

                String dependsOn = (String) req.getParameter(IMnPatchForm.DEPENDENCY_BUG_LABEL);
                if (dependsOn == null) {
                    dependsOn = (String) req.getAttribute(IMnPatchForm.DEPENDENCY_BUG_LABEL);
                }

                String depType = (String) req.getParameter(IMnPatchForm.DEPENDENCY_TYPE_LABEL);
                if (depType == null) {
                    depType = (String) req.getAttribute(IMnPatchForm.DEPENDENCY_TYPE_LABEL);
                }


                // Validate the text input
                CMnPatch patch = null;
                CMnBaseFixDependency dependency = new CMnBaseFixDependency();
app.debug("SSDEBUG: validating user input");
                if ((patchId != null) && (patchId.length() > 0)) {
                    patch = patchTable.getRequest(rc.getConnection(), patchId, true); 
                    if (patch == null) {
                        inputErrors.put(IMnPatchForm.PATCH_ID_LABEL, "Patch ID does not exist");
                    }
                } else {
                    inputErrors.put(IMnPatchForm.PATCH_ID_LABEL, "Patch ID required"); 
                }

                CMnBaseFix sourceFix = null;
                if ((bugId != null) && (bugId.length() > 0)) {
                    try {
                        // Validate that the bug was part of the patch request
                        Integer id = Integer.parseInt(bugId);
                        sourceFix = patch.getFix(id.intValue());
                        if (sourceFix == null) {
                            inputErrors.put(IMnPatchForm.FIX_BUG_LABEL, "Bug not found in patch request.");
                        }
                    } catch (NumberFormatException nfex) {
                        inputErrors.put(IMnPatchForm.FIX_BUG_LABEL, "Invalid Bug ID.  Must be a valid number.");
                    }
                } else {
                    inputErrors.put(IMnPatchForm.FIX_BUG_LABEL, "Bug ID required");
                }  

                CMnBaseFix targetFix = null;
                if ((dependsOn != null) && (dependsOn.length() > 0)) {
                    try {
                        Integer id = Integer.parseInt(dependsOn);
                        targetFix = patch.getFix(id.intValue());
                        if (targetFix != null) {
                            dependency.setBugId(id);
                        } else {
                            inputErrors.put(IMnPatchForm.DEPENDENCY_BUG_LABEL, "Bug not found in patch request.");
                        }
                    } catch (NumberFormatException nfex) {
                        inputErrors.put(IMnPatchForm.DEPENDENCY_BUG_LABEL, "Invalid Bug ID.  Must be a valid number.");
                    }
                } else {
                    inputErrors.put(IMnPatchForm.DEPENDENCY_BUG_LABEL, "Bug ID required");
                }   

                if ((depType != null) && (depType.length() > 0)) {
                    CMnBaseFixDependency.DependencyType type = CMnBaseFixDependency.DependencyType.valueOf(depType);
                    dependency.setType(type);
                } else {
                    inputErrors.put(IMnPatchForm.DEPENDENCY_TYPE_LABEL, "Dependency type required");
                }

                // Determine if the dependency already exists
                if (inputErrors.size() == 0) {
                    CMnPatchFix fix = patch.getFix(sourceFix.getBugId());
                    if ((fix != null) && (fix.getDependency(targetFix.getBugId()) != null)) {
                        inputErrors.put(IMnPatchForm.DEPENDENCY_BUG_LABEL, "Dependency already exists.");
                    }
                }

                // Add the dependency to the database 
                boolean success = false; 
                if (inputErrors.size() == 0) {
app.debug("SSDEBUG: Adding dependency to database.");
                    success = patchTable.addDependency(rc.getConnection(), patchId, bugId, dependency);
                    if (success) {
                        // Update the patch data object
                        sourceFix.addDependency(dependency);
                    }
                } else {
app.debug("SSDEBUG: Found " + inputErrors.size() + " input errors.");
                    req.setAttribute(CMnBaseForm.INPUT_ERROR_DATA, inputErrors);
                }

                // Update the patch information in the session
                if (patch != null) {
                    req.setAttribute(IMnPatchForm.PATCH_DATA, patch);
                }

                result.setDestination("patch/dependency_update.jsp");
            } catch (ApplicationException aex) {
                exApp = aex;
            } catch (Exception ex) {
                exApp = new ApplicationException(
                    ErrorMap.APPLICATION_DISPLAY_FAILURE,
                    "Failed to process command.");
                exApp.setStackTrace(ex);
            } finally {
                app.releaseRepositoryConnection(rc);

                // Throw any exceptions once the database connections have been cleaned up
                if (exApp != null) {
                    throw exApp;
                }
            }
        } else {
            app.debug("CMnDependencyUpdate: skipping execution due to pre-existing error: " + result.getError());
        }

        return result;
    }


}
