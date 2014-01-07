package org.jenkinsci.plugins;

import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: me-me-
 * Date: 2013/12/04
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public class Macro {
    private static final Pattern ENV_VARS_REGEX = Pattern.compile("\\$\\{(.+?)\\}");

    private static final List<ReplaceLogic> replacers = Arrays.<ReplaceLogic>asList(
//            new ReplaceLogic() {
//                public String replace(String original, AbstractBuild build, TaskListener listener) {
//                    return original.replaceAll("\\$\\{JOB_NAME\\}", build.getProject().getName());
//                }
//            },
//            new ReplaceLogic() {
//                public String replace(String original, AbstractBuild build, TaskListener listener) {
//                    return original.replaceAll("\\$\\{BUILD_NUMBER\\}", Integer.toString(build.getNumber()));
//                }
//            },
//            new ReplaceLogic() {
//                public String replace(String original, AbstractBuild build, TaskListener listener) {
//                    return original.replaceAll("\\$\\{BUILD_URL\\}", Hudson.getInstance().getRootUrl() + build.getUrl());
//                }
//            },
            new ReplaceLogic() {
                public String replace(String original, AbstractBuild build, TaskListener listener) {
                    return original.replaceAll("\\$\\{BUILD_RESULT\\}", build.getResult().toString());
                }
            },
            new ReplaceLogic() {
                public String replace(String original, AbstractBuild build, TaskListener listener) {
                    String replaced = original;
                    while (true) {
                        Matcher matcher = ENV_VARS_REGEX.matcher(replaced);
                        if (matcher.find()) {
                            String env = matcher.group(1);
                            try {
                                replaced = replaced.replaceAll(String.format("\\$\\{%s\\}", env), build.getEnvironment(listener).get(env, ""));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                    return replaced;
                }
            }
    );

    static String expand(String format, AbstractBuild build, TaskListener listener) {
        for (ReplaceLogic replacer : replacers) {
            format = replacer.replace(format, build, listener);
        }
        return format;
    }

    private static interface ReplaceLogic {
        String replace(String original, AbstractBuild build, TaskListener listener);
    }
}
