package org.erlide.cover.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.erlide.engine.ErlangEngine;
import org.erlide.engine.model.ErlModelException;
import org.erlide.engine.model.root.IErlModule;
import org.erlide.engine.model.root.IErlProject;

/**
 * Basic implementation for IConfiguration. Used to tell which modules at which project
 * should be analysed
 *
 * @author Aleksandra Lipiec <aleksandra.lipiec@erlang-solutions.com>
 *
 */
public class Configuration implements IConfiguration {

    private IErlProject project;
    private final Map<String, IErlModule> modules;

    public Configuration() {
        modules = new HashMap<>();
    }

    /**
     * Set Erlang project
     *
     * @param name
     *            name of an Erlang project
     */
    public void setProject(final String name) {
        if (name == null || name.isEmpty()) {
            project = null;
        } else {
            project = ErlangEngine.getInstance().getModel().getErlangProject(
                    ResourcesPlugin.getWorkspace().getRoot().getProject(name));
        }
    }

    /**
     * Add new module to cover configuration. The coverage of added module is going to be
     * analized.
     *
     * @param name
     *            Erlang module name
     * @throws ErlModelException
     *             when there is no such module in the project
     * @throws CoverException
     *             if project is not set
     */
    public void addModule(final String name) throws ErlModelException, CoverException {

        if (project == null) {
            throw new CoverException("no project set");
        }
        IErlModule module = null;
        module = project.getModule(name);
        modules.put(name, module);
    }

    /**
     * Add new module to cover configuration. The coverage of added module is going to be
     * analized.
     *
     * @param module
     *            Erlang module
     */
    public void addModule(final IErlModule module) {
        modules.put(module.getModuleName(), module);
    }

    @Override
    public IErlProject getProject() {
        return project;
    }

    @Override
    public Collection<IErlModule> getModules() {
        return modules.values();
    }

    @Override
    public IPath getOutputDir() {
        return project.getProperties().getOutputDir();
    }

    @Override
    public Collection<IPath> getSourceDirs() {
        return project.getProperties().getSourceDirs();
    }

    @Override
    public Collection<IPath> getIncludeDirs() {
        return project.getProperties().getIncludeDirs();
    }

    @Override
    public IErlModule getModule(final String name) {
        return modules.get(name);
    }

}
