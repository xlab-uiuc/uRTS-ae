package org.ekstazi.junit5Extension;

import org.ekstazi.Ekstazi;
import org.ekstazi.configTest.ConfigInjector;
import org.ekstazi.configTest.ConfigMapping;
import org.ekstazi.monitor.CoverageMonitor;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CoverageExtension implements BeforeAllCallback, AfterAllCallback {
    private String[] mURLs = null;
    private String mClassName = null;
    private boolean injectConfig = true;
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        mClassName = extensionContext.getRequiredTestClass().getCanonicalName();
        CoverageMonitor.clean();
        mURLs = CoverageMonitor.getURLs();
        if (injectConfig) {
            ConfigInjector.injectConfig(ConfigMapping.getInjectConfigPairs(mClassName));
        }
        Ekstazi.inst().beginClassCoverage(mClassName);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (mURLs != null) CoverageMonitor.addURLs(mURLs);
        Ekstazi.inst().endClassCoverage(mClassName, Junit5Helper.isTestFailed);
    }
}