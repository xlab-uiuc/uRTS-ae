package org.urts.junit5Extension;

import org.urts.Ekstazi;
import org.urts.configAware.ConfigInjector;
import org.urts.configAware.ConfigMapping;
import org.urts.Config;
import org.urts.monitor.CoverageMonitor;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import java.io.File;

public class CoverageExtension implements BeforeAllCallback, AfterAllCallback {
    private String[] mURLs = null;
    private String mClassName = null;
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        mClassName = extensionContext.getRequiredTestClass().getCanonicalName();
        CoverageMonitor.clean();
        mURLs = CoverageMonitor.getURLs();
        ConfigInjector.injectConfig(ConfigMapping.getInjectConfigPairs(mClassName));
        Ekstazi.inst().beginClassCoverage(mClassName);
        //System.out.println("Start Coverage :" + mClassName);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (mURLs != null) CoverageMonitor.addURLs(mURLs);
        Ekstazi.inst().endClassCoverage(mClassName, Junit5Helper.isTestFailed);
        //System.out.println("End Coverage :" + mClassName);
        File file = new File(Config.CONFIG_INJECT_FILE_PATH_V);
        if(file.exists()){
            file.delete();
        }
    }
}
