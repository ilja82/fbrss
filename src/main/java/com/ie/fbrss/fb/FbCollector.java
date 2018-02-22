package com.ie.fbrss.fb;

import com.ie.fbrss.data.FbPage;
import com.ie.fbrss.data.RssContent;
import org.springframework.social.facebook.api.*;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public final class FbCollector {

    private static final String FACEBOOK_GRAPH_URL = "https://graph.facebook.com/";

    private static final String FACEBOOK_URL = "https://www.facebook.com/";

    private static final PagingParameters PAGING_PARAMETERS = new PagingParameters(5, 0, null, null);

    private final Facebook facebook;

    private final ConcurrentMap<String, FbPage> fbSiteCache = new ConcurrentHashMap<>();

    private FbCollector() {
        String fbAppId = System.getenv()
                .get("spring.social.facebook.appId");
        String fbAppSecret = System.getenv()
                .get("spring.social.facebook.appSecret");

        if (fbAppId == null || fbAppSecret == null) {
            fbAppId = System.getProperty("spring.social.facebook.appId");
            fbAppSecret = System.getProperty("spring.social.facebook.appSecret");
        }

        if (fbAppId != null && fbAppSecret != null) {
            facebook = connectToFacebook(fbAppId + "|" + fbAppSecret);
        } else {
            facebook = null;
        }
    }

    public void collectData(final String fbUrl, final Model model) {

        if (facebook == null) {
            final RssContent data = new RssContent();
            data.setTitle("Could not authorize to Facebook!");
            data.setUrl(fbUrl);
            data.setSummary("");
            data.setCreatedDate(new Date());
            model.addAttribute("feedContent", Collections.singletonList(data));
            model.addAttribute("feedMetadata", data);
            return;
        }

        final FbPage fbPage = getId(fbUrl);

        final RssContent feedMetadata = new RssContent();
        feedMetadata.setTitle(fbPage.getName());
        feedMetadata.setUrl(fbUrl);

        final PagedList<Post> posts = facebook.feedOperations()
                .getFeed(fbPage.getId(), PAGING_PARAMETERS);

        // Collect all posts:
        List<RssContent> feedContent = posts.stream()
                .map(this::createPostEntry)
                .collect(Collectors.toList());

        // Also collect all comments of each post:
        posts.stream()
                .map(post -> facebook.commentOperations()
                        .getComments(post.getId(), PAGING_PARAMETERS))
                .flatMap(Collection::stream)
                .map(this::createCommentContent)
                .forEachOrdered(feedContent::add);

        model.addAttribute("feedContent", feedContent);
        model.addAttribute("feedMetadata", feedMetadata);
    }

    private Facebook connectToFacebook(final String token) {
        try {
            final Facebook facebook = new FacebookTemplate(token);
            if (facebook.isAuthorized()) {
                return facebook;
            } else {
                return null;
            }
        } catch (final Exception ex) {
            return null;
        }
    }

    private RssContent createCommentContent(final Comment comment) {
        final RssContent commentContent = new RssContent();
        Reference from = comment.getFrom();
        String name = from != null ? from.getName() : "NONAME";
        commentContent.setTitle("Comment from " + name);
        commentContent.setUrl(FACEBOOK_URL + comment.getId());
        commentContent.setSummary(comment.getMessage());
        commentContent.setCreatedDate(comment.getCreatedTime());
        return commentContent;
    }

    private RssContent createPostEntry(final Post post) {
        final RssContent content = new RssContent();
        content.setTitle(post.getStory() != null ? post.getStory() : post.getMessage());
        content.setUrl(FACEBOOK_URL + post.getId());
        final String summary = post.getMessage()
                + (post.getDescription() != null ? "\nLink description: " + post.getDescription() : "")
                + (post.getPicture() != null ? "\nPicture: " + post.getPicture() : "");
        content.setSummary(summary);
        content.setCreatedDate(post.getCreatedTime());
        return content;
    }

    private FbPage getId(final String fbUrl) {
        return fbSiteCache.computeIfAbsent(fbUrl, this::retrieveFbPage);
    }

    private FbPage retrieveFbPage(final String fbUrl) {
        return facebook.restOperations()
                .getForObject(FACEBOOK_GRAPH_URL + "{https}" + fbUrl, FbPage.class, "https://");
    }
}
