package org.jenkinsci.plugins.hipchat;

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
import org.jenkinsci.plugins.hipchat.client.HipChat;
import org.jenkinsci.plugins.hipchat.client.NotifyMessage;
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

    private final String room;

    @DataBoundConstructor
    public HipChatNotifier(String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    private static final String NOTIFY_TEMPLATE = "%s #%s (%s) %s";

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        String token = this.getDescriptor().getToken();

        if (token.length() > 0 && this.getRoom().length() > 0) {
            boolean notifyResult = new HipChat(token).notify(
                    this.getRoom(),
                    new NotifyMessage(
                            NotifyMessage.BackgroundColor.get(build.getResult().color),
                            String.format(
                                    NOTIFY_TEMPLATE,
                                    build.getProject().getName(),
                                    build.getNumber(),
                                    build.getResult().toString(),
                                    this.getDescriptor().getJenkinsUrl() + build.getProject().getUrl() + build.getNumber()
                            )
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
        private String jenkinsUrl;

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

        public String getJenkinsUrl() {
            return jenkinsUrl;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "HipChatNotifier";
        }

        public FormValidation doCheckRoom(@QueryParameter String room) throws IOException, ServletException {
            if (room.length() == 0)
                return FormValidation.error("Please input RoomName or RoomID");
            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            this.token = json.getString("token");
            this.jenkinsUrl = json.getString("jenkins_url");
            save();
            return super.configure(req, json);
        }
    }
}
