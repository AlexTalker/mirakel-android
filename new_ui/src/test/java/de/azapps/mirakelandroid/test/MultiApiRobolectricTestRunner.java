package de.azapps.mirakelandroid.test;

import android.os.Build;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.SdkPicker;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;
import org.junit.runners.model.InitializationError;
import java.util.Locale;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Properties;

/**
 * A test runner for Robolectric that will run a test against multiple API versions.
 * Copied from Robolectric, modified to use GradleTestRunner internaly
 */
public class MultiApiRobolectricTestRunner extends RobolectricTestRunner {
    public MultiApiRobolectricTestRunner(Class<?> testClass) throws InitializationError {
      super(testClass);
      Locale.setDefault(Locale.ENGLISH);
    }

    private class OurPicker extends SdkPicker {
        @Override
        public List<SdkConfig> selectSdks(Config config, AndroidManifest appManifest) {
            List<SdkConfig> result = super.selectSdks(config, appManifest);
 //           for (SdkConfig 
            return result;
        }
    }

    @Override
    protected SdkPicker createSdkPicker() {
        return new OurPicker();
    }

    @Override
    protected Config buildGlobalConfig() {
      Set<Integer> sdkSet = SdkConfig.getSupportedApis();
      Integer[] sdkArray = new Integer[sdkSet.size()];
      sdkSet.toArray(sdkArray);

      int[] sdks = new int[sdkArray.length];
      for (int i = 0; i < sdks.length; i++) {
          sdks[i] = sdkArray[i];
      }

      return new Config.Builder(super.buildGlobalConfig())
          .setSdk(sdks)
          .build();
    }
}
//public class MultiApiRobolectricTestRunner extends Suite {
//
//    public static final int[] JELLY_BEAN_UP = {
//        Build.VERSION_CODES.JELLY_BEAN,
//        Build.VERSION_CODES.JELLY_BEAN_MR1,
//        Build.VERSION_CODES.JELLY_BEAN_MR2,
//        Build.VERSION_CODES.KITKAT,
//        Build.VERSION_CODES.LOLLIPOP
//    };
//
//    public static final int[] JELLY_BEAN_MR1_UP = {
//        Build.VERSION_CODES.JELLY_BEAN_MR1,
//        Build.VERSION_CODES.JELLY_BEAN_MR2,
//        Build.VERSION_CODES.KITKAT,
//        Build.VERSION_CODES.LOLLIPOP
//    };
//
//    public static final int[] JELLY_BEAN_MR2_UP = {
//        Build.VERSION_CODES.JELLY_BEAN_MR2,
//        Build.VERSION_CODES.KITKAT,
//        Build.VERSION_CODES.LOLLIPOP
//    };
//
//    public static final int[] KIT_KAT_UP = {
//        Build.VERSION_CODES.KITKAT,
//        Build.VERSION_CODES.LOLLIPOP
//    };
//
//    public static final int[] LOLLIPOP_UP = {
//        Build.VERSION_CODES.LOLLIPOP
//    };
//
//
//    private final ArrayList<Runner> runners = new ArrayList<>();
//
//    /*
//     * Only called reflectively. Do not use programmatically.
//     */
//    public MultiApiRobolectricTestRunner(Class<?> klass) throws Throwable {
//        super(klass, Collections.<Runner>emptyList());
//
//        runners.add(new RobolectricTestRunner(getTestClass().getJavaClass()));
//    }
//
//    @Override
//    protected List<Runner> getChildren() {
//        return runners;
//    }
//}

