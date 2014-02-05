/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.thinclient.ui;

import java.io.IOException;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.localization.msgs.GetServersResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.comm.ConnectivityManager;
import com.raytheon.uf.viz.core.comm.ConnectivityManager.ConnectivityResult;
import com.raytheon.uf.viz.core.comm.IConnectivityCallback;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.ConnectivityPreferenceDialog;
import com.raytheon.uf.viz.core.localization.LocalizationConstants;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.thinclient.Activator;
import com.raytheon.uf.viz.thinclient.ThinClientUriUtil;
import com.raytheon.uf.viz.thinclient.preferences.ThinClientPreferenceConstants;

/**
 * Connectivity dialog for launching thinclient or thinalertviz. Contains extra
 * options not available when connecting with a normal CAVE.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 23, 2011            bsteffen    Initial creation
 * Aug 02, 2013 2202       bsteffen    Add edex specific connectivity checking.
 * Feb 04, 2014 2704       njensen     Refactored
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class ThinClientConnectivityDialog extends ConnectivityPreferenceDialog {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ThinClientConnectivityDialog.class, "CAVE");

    private class ServicesCallback implements IConnectivityCallback {

        @Override
        public void connectionChecked(ConnectivityResult results) {
            servicesGood = results.hasConnectivity;
            appendDetails(buildDetails(results));
            if (!results.hasConnectivity && status == null) {
                status = buildErrorMessage(results);
            }
        }
    }

    private class PypiesCallback implements IConnectivityCallback {

        @Override
        public void connectionChecked(ConnectivityResult results) {
            pypiesGood = results.hasConnectivity;
            appendDetails(buildDetails(results));
            if (!results.hasConnectivity && status == null) {
                status = buildErrorMessage(results);
            }
        }
    }

    private class JmsCallback implements IConnectivityCallback {

        @Override
        public void connectionChecked(ConnectivityResult results) {
            jmsGood = results.hasConnectivity;
            appendDetails(buildDetails(results));
            if (!results.hasConnectivity && status == null) {
                status = buildErrorMessage(results);
            }
        }

    }

    private boolean servicesGood = false;

    private IConnectivityCallback servicesCallback = new ServicesCallback();

    private boolean pypiesGood = false;

    private IConnectivityCallback pypiesCallback = new PypiesCallback();

    private Button useProxyCheck;

    private boolean useProxy = false;

    private Button disableJmsCheck;

    private boolean disableJms = false;

    private boolean jmsGood = false;

    private Label jmsErrorLabel;

    private IConnectivityCallback jmsCallback = new JmsCallback();

    private Text proxyText;

    private String proxyAddress;

    public ThinClientConnectivityDialog(boolean checkAlertViz) {
        super(checkAlertViz, "Thin Client Connectivity Preferences");
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        useProxy = store
                .getBoolean(ThinClientPreferenceConstants.P_USE_PROXIES);
        disableJms = store
                .getBoolean(ThinClientPreferenceConstants.P_DISABLE_JMS);
        proxyAddress = store
                .getString(ThinClientPreferenceConstants.P_PROXY_ADDRESS);
    }

    @Override
    protected void createTextBoxes(Composite textBoxComp) {
        super.createTextBoxes(textBoxComp);

        Label label = new Label(textBoxComp, SWT.RIGHT);
        label.setText("Disable JMS:");
        GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.widthHint = 150;
        label.setLayoutData(gd);

        Composite jmsComp = new Composite(textBoxComp, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        jmsComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        jmsComp.setLayoutData(gd);

        disableJmsCheck = new Button(jmsComp, SWT.CHECK | SWT.LEFT);
        disableJmsCheck.setSelection(disableJms);
        disableJmsCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                disableJms = disableJmsCheck.getSelection();
                validate();
            }
        });
        jmsErrorLabel = new Label(jmsComp, SWT.LEFT);
        jmsErrorLabel.setText("Error connecting to JMS");
        jmsErrorLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
        jmsErrorLabel.setVisible(false);

        label = new Label(textBoxComp, SWT.RIGHT);
        label.setText("Use Proxy Server:");
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.widthHint = 150;
        label.setLayoutData(gd);

        Composite proxyComp = new Composite(textBoxComp, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        proxyComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        proxyComp.setLayoutData(gd);

        useProxyCheck = new Button(proxyComp, SWT.CHECK | SWT.LEFT);
        useProxyCheck.setSelection(useProxy);
        useProxyCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateProxyEnabled();
            }
        });

        proxyText = new Text(proxyComp, SWT.NONE | SWT.BORDER);
        gd = new GridData(SWT.FILL, SWT.None, true, true);
        proxyText.setLayoutData(gd);
        proxyText.setText(proxyAddress == null ? "" : proxyAddress);
        proxyText.setBackground(getTextColor(servicesGood && pypiesGood));

        updateProxyEnabled();
    }

    @Override
    protected void applySettings() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setValue(ThinClientPreferenceConstants.P_DISABLE_JMS, disableJms);
        if (useProxy) {
            store.setValue(ThinClientPreferenceConstants.P_USE_PROXIES,
                    useProxy);
            store.setValue(ThinClientPreferenceConstants.P_PROXY_ADDRESS,
                    proxyAddress);

            if (getAlertVizServer() != null) {
                LocalizationManager
                        .getInstance()
                        .getLocalizationStore()
                        .setValue(LocalizationConstants.P_ALERT_SERVER,
                                getAlertVizServer());
            }
            // setting the site will save the preference store
            LocalizationManager.getInstance().setCurrentSite(getSite());

            try {
                ((IPersistentPreferenceStore) store).save();
            } catch (IOException e) {
                statusHandler.handle(Priority.SIGNIFICANT,
                        "Unable to persist localization preference store", e);
            }
        } else {
            super.applySettings();
        }

    }

    @Override
    public boolean validate() {
        if (!useProxy) {
            return super.validate();
        }

        status = null;
        details = null;

        // validate proxy
        if (proxyText != null && !proxyText.isDisposed()
                && proxyText.isEnabled()) {
            proxyAddress = proxyText.getText();
        }
        if (proxyAddress != null && proxyAddress.length() > 0) {
            validateServices();
            validatePypies();
        } else {
            status = "Please enter a thin client proxy server address";
        }
        if (proxyText != null && !proxyText.isDisposed()) {
            proxyText.setBackground(getTextColor(servicesGood && pypiesGood));
        }

        // only check Jms if it's enabled and we can connect to the services
        if (!disableJms) {
            if (servicesGood) {
                try {
                    GetServersResponse response = ConnectivityManager
                            .checkLocalizationServer(ThinClientUriUtil
                                    .getServicesAddress(proxyAddress), false);
                    ConnectivityManager.checkJmsServer(
                            response.getJmsConnectionString(), jmsCallback);
                } catch (VizException e) {
                    if (status == null) {
                        status = "Error connecting to JMS";
                    }
                    appendDetails(buildDetails(new ConnectivityResult(false,
                            null, e)));
                    jmsGood = false;
                }
            } else {
                // JMS can't be good if services fail cause then we don't
                // even know where to connect JMS to
                jmsGood = false;
            }
        }
        jmsGood = (jmsGood || disableJms);
        if (jmsErrorLabel != null && !jmsErrorLabel.isDisposed()) {
            jmsErrorLabel.setVisible(!jmsGood);
        }

        // validate site
        if (siteText != null && !siteText.isDisposed()) {
            super.setSite(siteText.getText());
        }
        super.validateSite();
        if (siteText != null && !siteText.isDisposed()) {
            siteText.setBackground(getTextColor(isSiteGood()));
        }

        boolean everythingGood = servicesGood && pypiesGood && isSiteGood()
                && isAlertVizGood() && jmsGood;
        updateStatus(everythingGood, status, details);

        return everythingGood;
    }

    private void validateServices() {
        ConnectivityManager.checkLocalizationServer(
                ThinClientUriUtil.getServicesAddress(proxyAddress),
                servicesCallback);
    }

    private void validatePypies() {
        ConnectivityManager.checkHttpServer(
                ThinClientUriUtil.getPypiesAddress(proxyAddress),
                pypiesCallback);
    }

    private void updateProxyEnabled() {
        useProxy = useProxyCheck.getSelection();
        proxyText.setEnabled(useProxy);
        super.setLocalizationEnabled(!useProxy);
        validate();
    }

}
