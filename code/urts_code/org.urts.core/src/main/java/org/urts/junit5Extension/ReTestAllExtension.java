package org.urts.junit5Extension;

import org.urts.configAware.ConfigInjector;
import org.urts.configAware.ConfigMapping;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ReTestAllExtension implements BeforeAllCallback, AfterAllCallback {
    private String[] mURLs = null;
    private String mClassName = null;
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        mClassName = extensionContext.getRequiredTestClass().getCanonicalName();
        ConfigInjector.injectConfig(ConfigMapping.getInjectConfigPairs(mClassName));
        //System.out.println("Start Coverage :" + mClassName);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        // Nothing to do
    }
}
