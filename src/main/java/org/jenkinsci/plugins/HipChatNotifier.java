package org.jenkinsci.plugins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.hipchat.HipChat;
import org.jenkinsci.plugins.hipchat.NotifyMessage;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: me-me-
 * Date: 2013/11/26
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class HipChatNotifier extends Notifier {

    public final String room;
    public final String successMessageFormat;
    public final String failedMessageFormat;
    public final boolean postSuccess;
    public final boolean notifySuccess;
    public final boolean postFailed;
    public final boolean notifyFailed;

    @DataBoundConstructor
    public HipChatNotifier(
            String room,
            String successMessageFormat,
            String failedMessageFormat,
            boolean postSuccess,
            boolean notifySuccess,
            boolean postFailed,
            boolean notifyFailed
    ) {
        this.room = room;
        this.postSuccess = postSuccess;
        this.notifySuccess = notifySuccess;
        this.postFailed = postFailed;
        this.notifyFailed = notifyFailed;
        this.successMessageFormat = successMessageFormat;
        this.failedMessageFormat = failedMessageFormat;
    }

    public String getRoom() {
        return room;
    }

    private boolean shouldPost(AbstractBuild build) {
        Result result = build.getResult();
        return (result.isBetterOrEqualTo(Result.SUCCESS) && this.postSuccess)
                || (result.isWorseThan(Result.SUCCESS) && this.postFailed);
    }

    private boolean shouldNotify(AbstractBuild build) {
        Result result = build.getResult();
        return (result.isBetterOrEqualTo(Result.SUCCESS) && this.notifySuccess)
                || (result.isWorseThan(Result.SUCCESS) && this.notifyFailed);
    }

    private String messageFormat(AbstractBuild build) {
        Result result = build.getResult();
        if (result.isBetterOrEqualTo(Result.SUCCESS)) {
            return this.successMessageFormat;
        } else {
            return this.failedMessageFormat;
        }
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        String token = getDescriptor().getToken();

        logger.println("HipChat Post   : " + shouldPost(build));
        logger.println("HipChat Notify : " + shouldNotify(build));

        if (token.length() > 0 && getRoom().length() > 0 && shouldPost(build)) {
            boolean notifyResult = new HipChat(token).notify(
                    getRoom(),
                    new NotifyMessage(
                            NotifyMessage.BackgroundColor.get(build.getResult().color),
                            Macro.expand(messageFormat(build), build, listener),
                            shouldNotify(build)
                    )
            );
            if (notifyResult) {
                logger.println("HipChat Notification OK");
            } else {
                logger.println("HipChat Notification Failed");
            }
        } else {
            logger.println("HipChatNotifier InvalidSettings.");
        }
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private static final String NOTIFY_TEMPLATE = "${JOB_NAME} #${BUILD_NUMBER} (${BUILD_RESULT}) ${BUILD_URL}";
        private String token;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public String getToken() {
            return token;
        }

        public String getDefaultMessageFormat() {
            return NOTIFY_TEMPLATE;
        }
        public boolean getDefaultPostSuccess() {
            return true;
        }
        public boolean getDefaultNotifySuccess() {
            return true;
        }
        public boolean getDefaultPostFailed() {
            return true;
        }
        public boolean getDefaultNotifyFailed() {
            return true;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "HipChat Notifier";
        }

        public FormValidation doCheckRoom(@QueryParameter String room) throws IOException, ServletException {
            if (room.length() == 0)
                return FormValidation.error("Please input RoomName or RoomID");
            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            this.token = json.getString("token");
            save();
            return super.configure(req, json);
        }
    }
}
