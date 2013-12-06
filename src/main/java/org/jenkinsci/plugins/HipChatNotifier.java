package org.jenkinsci.plugins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
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

    private static final String NOTIFY_TEMPLATE = "${JOB_NAME} #${BUILD_NUMBER} (${BUILD_RESULT}) ${BUILD_URL}";

    public final String room;
    public final String messageFormat;
    public final boolean postSuccess;
    public final boolean notifySuccess;
    public final boolean postFailed;
    public final boolean notifyFailed;

    @DataBoundConstructor
    public HipChatNotifier(
            String room,
            String messageFormat,
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

        if (messageFormat != null && messageFormat.length() > 0) {
            this.messageFormat = messageFormat;
        } else {
            this.messageFormat = NOTIFY_TEMPLATE;
        }
    }

    public String getRoom() {
        return room;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        String token = this.getDescriptor().getToken();

        if (token.length() > 0 && this.getRoom().length() > 0) {
            boolean notifyResult = new HipChat(token).notify(
                    this.getRoom(),
                    new NotifyMessage(
                            NotifyMessage.BackgroundColor.get(build.getResult().color),
                            Macro.expand(this.messageFormat, build)
                    )
            );
            if (notifyResult) {
                logger.println("HipChat Notify OK");
            } else {
                logger.println("HipChat Notify Failed");
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
