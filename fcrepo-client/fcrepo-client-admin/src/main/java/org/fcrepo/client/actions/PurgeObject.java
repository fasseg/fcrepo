/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.client.actions;

import org.fcrepo.client.Administrator;
import org.fcrepo.client.objecteditor.ObjectEditorFrame;
import org.fcrepo.client.utility.AutoPurger;
import org.fcrepo.server.utilities.StringUtility;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Chris Wilper
 */
public class PurgeObject
        extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private Set<String> m_pids;

    private boolean m_prompt;

    private ObjectEditorFrame m_parent;

    public PurgeObject() {
        super("Purge Object...");
        m_prompt = true;
    }

    public PurgeObject(ObjectEditorFrame parent, String pid) {
        super("Purge Object");
        m_pids = new HashSet<String>();
        m_pids.add(pid);
        m_parent = parent;
    }

    public PurgeObject(Set<String> pids) {
        super("Purge Objects");
        m_pids = pids;
    }

    public PurgeObject(String pid) {
        super("Purge Object");
        m_pids = new HashSet<String>();
        m_pids.add(pid);
    }

    public void actionPerformed(ActionEvent ae) {
        boolean failed = false;
        if (m_prompt) {
            String pid = JOptionPane.showInputDialog("Enter the PID.");
            if (pid == null) {
                return;
            }
            m_pids = new HashSet<String>();
            m_pids.add(pid);
        }
        AutoPurger purger = null;
        try {
            purger = new AutoPurger(Administrator.APIM);
        } catch (Exception e) {
            Administrator
                    .showErrorDialog(Administrator.getDesktop(),
                                     "Purge Failure",
                                     StringUtility.prettyPrint(e.getClass()
                                             .getName()
                                                               + ": " + e.getMessage(), 70, null),
                                     e);
        }
        if (purger != null) {
            Iterator<String> pidIter = m_pids.iterator();
            if (m_pids.size() == 1) {
                String pid = pidIter.next();
                // just purge one
                String reason =
                        JOptionPane
                                .showInputDialog("Why are you permanently removing "
                                                 + pid + "?");
                if (reason != null) {
                    try {
                        purger.purge(pid, reason);
                    } catch (Exception e) {
                        Administrator.showErrorDialog(Administrator
                                .getDesktop(), "Purge Failure", StringUtility
                                .prettyPrint(e.getClass().getName() + ": "
                                             + e.getMessage(), 70, null), e);
                        failed = true;
                    }
                    if (!failed) {
                        JOptionPane.showMessageDialog(Administrator
                                .getDesktop(), "Purge succeeded.");
                        if (m_parent != null) {
                            m_parent.dispose();
                        }
                    }
                }
            } else {
                // purge multiple
                String reason =
                        JOptionPane
                                .showInputDialog("Why are you permanently removing these objects?");
                if (reason != null) {
                    while (pidIter.hasNext()) {
                        try {
                            String pid = pidIter.next();
                            purger.purge(pid, reason);
                        } catch (Exception e) {
                            Administrator
                                    .showErrorDialog(Administrator.getDesktop(),
                                                     "Purge Failure",
                                                     StringUtility
                                                             .prettyPrint(e
                                                             .getClass()
                                                             .getName()
                                                                          + ": "
                                                                          + e
                                                             .getMessage(),
                                                                          70,
                                                                          null),
                                                     e);
                            failed = true;
                        }
                    }
                    if (!failed) {
                        JOptionPane.showMessageDialog(Administrator
                                .getDesktop(), "Purge of " + m_pids.size()
                                               + " objects succeeded.");
                    }
                }
            }
        }
    }

}
