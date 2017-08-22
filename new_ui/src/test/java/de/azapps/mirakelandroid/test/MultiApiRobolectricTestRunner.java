package de.azapps.mirakelandroid.test;

import android.os.Build;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A test runner for Robolectric that will run a test against multiple API versions.
 * Copied from Robolectric, modified to use GradleTestRunner internaly
 */
public class MultiApiRobolectricTestRunner extends Suite {

    public static final int[] JELLY_BEAN_UP = {
        Build.VERSION_CODES.JELLY_BEAN,
        Build.VERSION_CODES.JELLY_BEAN_MR1,
        Build.VERSION_CODES.JELLY_BEAN_MR2,
        Build.VERSION_CODES.KITKAT,
        Build.VERSION_CODES.LOLLIPOP
    };

    public static final int[] JELLY_BEAN_MR1_UP = {
        Build.VERSION_CODES.JELLY_BEAN_MR1,
        Build.VERSION_CODES.JELLY_BEAN_MR2,
        Build.VERSION_CODES.KITKAT,
        Build.VERSION_CODES.LOLLIPOP
    };

    public static final int[] JELLY_BEAN_MR2_UP = {
        Build.VERSION_CODES.JELLY_BEAN_MR2,
        Build.VERSION_CODES.KITKAT,
        Build.VERSION_CODES.LOLLIPOP
    };

    public static final int[] KIT_KAT_UP = {
        Build.VERSION_CODES.KITKAT,
        Build.VERSION_CODES.LOLLIPOP
    };

    public static final int[] LOLLIPOP_UP = {
        Build.VERSION_CODES.LOLLIPOP
    };

    protected static class TestRunnerForApiVersion extends RobolectricGradleTestRunner {

        private final String name;
        private final Integer apiVersion;

        TestRunnerForApiVersion(Class<?> type, Integer apiVersion) throws InitializationError {
            super(type);
            this.apiVersion = apiVersion;
            this.name = apiVersion.toString();
        }

        @Override
        protected String getName() {
            return "[" + apiVersion + "]";
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return method.getName() + getName();
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }

        @Override
        public String toString() {
            return "TestClassRunnerForParameters " + name;
        }

        protected boolean shouldIgnore(FrameworkMethod method, Config config) {
            return super.shouldIgnore(method, config) || !shouldRunApiVersion(config);
        }

        private boolean shouldRunApiVersion(Config config) {
            if (config.sdk().length == 0) {
                return true;
            }
            for (int sdk : config.sdk()) {
                if (sdk == apiVersion) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected int pickSdkVersion(Config config, AndroidManifest appManifest) {
            return apiVersion;
        }

        @Override
        protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
            try {
                return new HelperTestRunner(bootstrappedTestClass) {
                    @Override
                    protected void validateConstructor(List<Throwable> errors) {
                        TestRunnerForApiVersion.this.validateOnlyOneConstructor(errors);
                    }

                    @Override
                    public String toString() {
                        return "HelperTestRunner for " + TestRunnerForApiVersion.this.toString();
                    }
                };
            } catch (InitializationError initializationError) {
                throw new RuntimeException(initializationError);
            }
        }
    }

    private final ArrayList<Runner> runners = new ArrayList<>();
    private int runners_num = 0;

    /*
     * Only called reflectively. Do not use programmatically.
     */
    public MultiApiRobolectricTestRunner(Class<?> klass) throws Throwable {
        super(klass, Collections.<Runner>emptyList());

        for (Integer integer : SdkConfig.getSupportedApis()) {
            runners_num++;
            if(runners_num > 3) {
                break;
            }
            System.out.println("Create Runner for API: " + integer);
            // Weird failure on 12 minute
            runners.add(createTestRunner(integer));
        }
    }

    protected TestRunnerForApiVersion createTestRunner(Integer integer) throws InitializationError {
        return new TestRunnerForApiVersion(getTestClass().getJavaClass(), integer);
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
}

