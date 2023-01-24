package org.urts.junit;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.urts.configAware.ConfigInjector;
import org.urts.configAware.ConfigMapping;
import org.urts.log.Log;


public class JUnit4ReTestAllRunner extends Runner {

    /** Test class being run */
    private final Class<?> mClz;

    /** Wrapped runner */
    private final Runner mWrappedRunner;

    /**
     * Constructor.
     */
    public JUnit4ReTestAllRunner(Class<?> clz, Runner wrapped) {
        this.mClz = clz;
        this.mWrappedRunner = wrapped;
    }

    @Override
    public Description getDescription() {
        return mWrappedRunner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        ConfigInjector.injectConfig(ConfigMapping.getInjectConfigPairs(mClz.getName()));
        mWrappedRunner.run(notifier);
    }
}
