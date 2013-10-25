package org.erlide.core.internal.builder.external

import org.eclipse.core.resources.IProject
import org.erlide.engine.model.root.ErlangProjectProperties
import org.erlide.engine.model.root.ProjectConfigurator
import org.erlide.core.internal.builder.FileProjectConfigurationPersister

class EmakeConfigurator implements ProjectConfigurator {

    override String encodeConfig(IProject project, ErlangProjectProperties info) {
        '''
            �FOR src : info.sourceDirs�
                {'�src.toPortableString�/*',[�FOR inc : info.includeDirs�{i, "�inc.toPortableString�"},�ENDFOR�]}.
            �ENDFOR�
        '''
    }

    override decodeConfig(String config) {
        null
    }

    def getConfigFile() {
        'Emakefile'
    }
    
    override getPersister(IProject project) {
        return new FileProjectConfigurationPersister(project, this, 'Emakefile')
    }

}
