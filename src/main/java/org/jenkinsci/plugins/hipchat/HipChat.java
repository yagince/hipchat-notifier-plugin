package org.jenkinsci.plugins.hipchat;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created with IntelliJ IDEA.
 * User: me-me-
 * Date: 2013/11/26
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class HipChat {
    private static final String NOTIFY_URL = "https://api.hipchat.com/v2/room/%s/notification?auth_token=%s"; // TODO: config
    private final String token;

    public HipChat(String token) {
        this.token = token;
    }

    public boolean notify(String room, NotifyMessage message) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(String.format(NOTIFY_URL, encode(room), encode(this.token)));
            post.setEntity(new StringEntity(message.toJson().toString(), ContentType.create("application/json")));
            CloseableHttpResponse res = client.execute(post);

            if (res.getStatusLine().getStatusCode() > 400) {
                System.out.println(String.format("HipChat Notify Status Code : %d", res.getStatusLine().getStatusCode()));
                return false;
            }
            return true;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encode(String v) {
        try {
            return URLEncoder.encode(v, "UTF-8")
                    .replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
