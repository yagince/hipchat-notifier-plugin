package org.jenkinsci.plugins.hipchat;

import hudson.model.BallColor;
import net.sf.json.JSONObject;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: me-me-
 * Date: 2013/11/26
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class NotifyMessage {

    private final BackgroundColor bgColor;
    private final String message;
    private final boolean notify;

    public NotifyMessage(BackgroundColor bgColor, String message, boolean notify) {
        this.bgColor = bgColor;
        this.message = message;
        this.notify = notify;

    }

    public NotifyMessage(String message) {
        this(BackgroundColor.red, message, true);
    }

    public BackgroundColor getBackgroundColor() {
        return bgColor;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject toJson() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("color", this.getBackgroundColor().name());
        map.put("message", this.getMessage());
        map.put("notify", Boolean.valueOf(this.notify));
        map.put("message_format", "text");
        return JSONObject.fromObject(map);
    }

    public enum BackgroundColor {
        red, yellow, green, purple, gray, random;

        public static BackgroundColor get(BallColor ball) {
            switch (ball) {
                case BLUE:
                case BLUE_ANIME:
                    return green;
                case RED:
                case RED_ANIME:
                    return red;
                default:
                    return gray;
            }
        }
    }
}
