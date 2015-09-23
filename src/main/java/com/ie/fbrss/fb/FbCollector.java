package com.ie.fbrss.fb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.social.facebook.api.Comment;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.PagingParameters;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.ie.fbrss.data.FbPage;
import com.ie.fbrss.data.RssContent;

@Component
public final class FbCollector {

	private static final String FACEBOOK_GRAPH_URL = "https://graph.facebook.com/";

	private static final String FACEBOOK_URL = "https://www.facebook.com/";

	private static final PagingParameters PAGING_PARAMETERS = new PagingParameters(5, 0, null, null);

	private final Facebook facebook;

	private final ConcurrentMap<String, FbPage> fbSiteCache = new ConcurrentHashMap<>();

	private FbCollector() {
		String fbAppId = System.getenv().get("spring.social.facebook.appId");
		String fbAppSecret = System.getenv().get("spring.social.facebook.appSecret");

		if (fbAppId == null || fbAppSecret == null) {
			fbAppId = System.getProperty("spring.social.facebook.appId");
			fbAppSecret = System.getProperty("spring.social.facebook.appSecret");
		}

		if (fbAppId != null && fbAppSecret != null) {
			facebook = new FacebookTemplate(fbAppId + "|" + fbAppSecret);
		} else {
			facebook = null;
		}
	}

	public void collectData(final String fbUrl, final Model model) {

		final FbPage fbPage = getId(facebook, fbUrl);

		if (facebook == null || !fbAuthorized(facebook)) {
			final RssContent data = new RssContent();
			data.setTitle(fbPage.getName());
			data.setUrl(fbUrl);
			data.setSummary("Could not authorize to facebook!");
			data.setCreatedDate(new Date());
			model.addAttribute("feedContent", Arrays.asList(data));
			model.addAttribute("feedMetadata", data);
			return;
		}

		final RssContent feedMetadata = new RssContent();
		feedMetadata.setTitle(fbPage.getName());
		feedMetadata.setUrl(fbUrl);

		final ArrayList<RssContent> feedContent = new ArrayList<RssContent>();

		final PagedList<Post> posts = facebook.feedOperations().getFeed(fbPage.getId(), PAGING_PARAMETERS);
		for (final Post post : posts) {
			feedContent.add(createPostEntry(post));
			final PagedList<Comment> comments = facebook.commentOperations().getComments(post.getId(),
					PAGING_PARAMETERS);
			for (final Comment comment : comments) {
				feedContent.add(createCommentContent(comment));
			}
		}

		model.addAttribute("feedContent", feedContent);
		model.addAttribute("feedMetadata", feedMetadata);
	}

	private RssContent createCommentContent(final Comment comment) {
		final RssContent commentContent = new RssContent();
		commentContent.setTitle("Comment from " + comment.getFrom().getName());
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

	private boolean fbAuthorized(final Facebook facebook) {
		try {
			if (facebook.isAuthorized()) {
				return true;
			} else {
				return false;
			}
		} catch (final Exception ex) {
			return false;
		}
	}

	private FbPage getId(final Facebook facebook, final String fbUrl) {
		return fbSiteCache.computeIfAbsent(fbUrl, key -> retrieveFbPage(facebook, key));
	}

	private FbPage retrieveFbPage(final Facebook facebook, final String fbUrl) {
		return facebook.restOperations().getForObject(FACEBOOK_GRAPH_URL + "{https}" + fbUrl, FbPage.class, "https://");
	}
}
