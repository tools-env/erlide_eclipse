/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation
 *******************************************************************************/
package org.erlide.core;

import java.util.logging.Level;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.erlide.backend.debug.ErlangDebugOptionsManager;
import org.erlide.core.internal.ErlangCoreLogger;
import org.erlide.core.util.EncodingUtils;
import org.erlide.runtime.rpc.RpcMonitor;
import org.erlide.util.ErlLogger;
import org.erlide.util.SystemConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;

public final class ErlangCore {
    public static final String PLUGIN_ID = "org.erlide.core";
    public static final String NATURE_ID = ErlangCore.PLUGIN_ID + ".erlnature";
    public static final String ERLIDE_GLOBAL_TRACE_OPTION = "org.erlide.backend/debug";

    private String featureVersion;

    private final Plugin plugin;
    private final IWorkspace workspace;
    private final IExtensionRegistry extensionRegistry;
    private ISaveParticipant saveParticipant;
    private final ErlangDebugOptionsManager erlangDebugOptionsManager;
    private final ErlangCoreLogger logger;

    public ErlangCore(final Plugin plugin, final IWorkspace workspace,
            final IExtensionRegistry extensionRegistry,
            final ErlangDebugOptionsManager erlangDebugOptionsManager) {
        this.plugin = plugin;
        this.workspace = workspace;
        this.extensionRegistry = extensionRegistry;
        this.erlangDebugOptionsManager = erlangDebugOptionsManager;
        featureVersion = "?";
        logger = new ErlangCoreLogger(plugin);
    }

    public void start() throws CoreException {
        final String version = retrieveAllVersions();

        ErlLogger.info("Starting CORE " + Thread.currentThread());
        String dev = "(" + EncodingUtils.getEncoding() + ")";
        if (SystemConfiguration.getInstance().isDeveloper()) {
            dev += " developer version ***";
        }
        if (SystemConfiguration.getInstance().isTest()) {
            dev += " test ***";
        }

        final String versionBanner = "*** starting Erlide v" + version + " *** " + dev;
        logger.log(Level.INFO, versionBanner);
        featureVersion = version;

        workspace.addSaveParticipant(plugin.getBundle().getSymbolicName(),
                getSaveParticipant());

        erlangDebugOptionsManager.start();
        ErlLogger.info("Started CORE");
    }

    public void stop() {
        erlangDebugOptionsManager.shutdown();
        final String location = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                .toPortableString();

        RpcMonitor.cleanupOldLogs(location, "rpc_monitor");
        ErlLogger.getInstance().dispose();
        RpcMonitor.dump(ErlLogger.getInstance().getLogFile());
    }

    public IWorkspace getWorkspace() {
        return workspace;
    }

    public IExtensionRegistry getExtensionRegistry() {
        return extensionRegistry;
    }

    public String getFeatureVersion() {
        return featureVersion;
    }

    public ISaveParticipant getSaveParticipant() {
        if (saveParticipant == null) {
            saveParticipant = new ISaveParticipant() {
                @Override
                public void doneSaving(final ISaveContext context1) {
                }

                @Override
                public void prepareToSave(final ISaveContext context1)
                        throws CoreException {
                }

                @Override
                public void rollback(final ISaveContext context1) {
                }

                @Override
                public void saving(final ISaveContext context1) throws CoreException {
                    try {
                        InstanceScope.INSTANCE
                                .getNode(plugin.getBundle().getSymbolicName()).flush();
                    } catch (final BackingStoreException e) {
                        // ignore
                    }
                }
            };
        }
        return saveParticipant;
    }

    public boolean isDebugging() {
        return plugin.isDebugging();
    }

    public Bundle getBundle() {
        return plugin.getBundle();
    }

    public IPath getStateLocation() {
        return plugin.getStateLocation();
    }

    public boolean isTracing(final String traceOption) {
        if (!Platform.inDebugMode()) {
            return false;
        }
        final String globalTraceValue = Platform
                .getDebugOption(ErlangCore.ERLIDE_GLOBAL_TRACE_OPTION);
        final String value = Platform.getDebugOption(
                ErlangCore.ERLIDE_GLOBAL_TRACE_OPTION + "/" + traceOption);
        return "true".equalsIgnoreCase(globalTraceValue)
                && "true".equalsIgnoreCase(value);
    }

    private String retrieveAllVersions() {
        String version = "?";
        String kernelVersion = "?";
        try {
            final IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
            if (providers != null) {
                version = findFeatureVersion("org.erlide", providers);
                kernelVersion = findFeatureVersion("org.erlide.kernel.feature", providers)
                        + "~"
                        + Platform.getBundle("org.erlide.kernel.common").getVersion();
            } else {
                ErlLogger.info("***: no bundle group providers");
            }
        } catch (final Exception e) {
            // ignore
        }
        final Version coreVersion = getBundle().getVersion();
        final Version modelVersion = Platform.getBundle("org.erlide.model.api")
                .getVersion();

        version = version + "~" + coreVersion.toString() + " (model api="
                + modelVersion.toString() + ")" + " (kernel=" + kernelVersion + ")";
        return version;
    }

    private String findFeatureVersion(final String key,
            final IBundleGroupProvider[] providers) {
        String version = "?";
        for (final IBundleGroupProvider provider : providers) {
            final IBundleGroup[] bundleGroups = provider.getBundleGroups();
            for (final IBundleGroup group : bundleGroups) {
                final String id = group.getIdentifier();
                if (key.equals(id)) {
                    version = group.getVersion();
                    break;
                }
            }
            if (!"?".equals(version)) {
                break;
            }
        }
        return version;
    }
}
