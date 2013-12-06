package org.jenkinsci.plugins;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: me-me-
 * Date: 2013/12/04
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public class Macro {
    private static final List<ReplaceLogic> replacers = Arrays.<ReplaceLogic>asList(
            new ReplaceLogic() {
                public String replace(String original, AbstractBuild build) {
                    return original.replaceAll("\\$\\{JOB_NAME\\}", build.getProject().getName());
                }
            },
            new ReplaceLogic() {
                public String replace(String original, AbstractBuild build) {
                    return original.replaceAll("\\$\\{BUILD_NUMBER\\}", Integer.toString(build.getNumber()));
                }
            },
            new ReplaceLogic() {
                public String replace(String original, AbstractBuild build) {
                    return original.replaceAll("\\$\\{BUILD_RESULT\\}", build.getResult().toString());
                }
            },
            new ReplaceLogic() {
                public String replace(String original, AbstractBuild build) {
                    return original.replaceAll("\\$\\{BUILD_URL\\}", Hudson.getInstance().getRootUrl() + build.getUrl());
                }
            }
    );

    static String expand(String format, AbstractBuild build) {
        for (ReplaceLogic replacer : replacers) {
            format = replacer.replace(format, build);
        }
        return format;
    }

    private static interface ReplaceLogic {
        String replace(String original, AbstractBuild build);
    }
}
