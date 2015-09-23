package com.ie.fbrss.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import com.ie.fbrss.data.RssContent;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Item;

public final class RssViewer extends AbstractRssFeedView {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.web.servlet.view.feed.AbstractRssFeedView#
	 * buildFeedItems(java.util.Map, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected List<Item> buildFeedItems(final Map<String, Object> model, final HttpServletRequest request,
			final HttpServletResponse response) {

		@SuppressWarnings("unchecked")
		final List<RssContent> elements = (List<RssContent>) model.get("feedContent");
		final List<Item> items = new ArrayList<>(elements.size());

		for (final RssContent element : elements) {

			final Item item = new Item();

			final Content bodyText = new Content();
			bodyText.setValue(element.getSummary());
			item.setContent(bodyText);
			item.setTitle(element.getTitle());
			item.setLink(element.getUrl());
			item.setPubDate(element.getCreatedDate());

			items.add(item);
		}

		return items;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.web.servlet.view.feed.AbstractFeedView#
	 * buildFeedMetadata(java.util.Map, com.rometools.rome.feed.WireFeed,
	 * javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected void buildFeedMetadata(final Map<String, Object> model, final Channel feed,
			final HttpServletRequest request) {

		final RssContent feedMetadata = (RssContent) model.get("feedMetadata");

		feed.setTitle(feedMetadata.getTitle());
		feed.setDescription(feedMetadata.getTitle());
		feed.setLink(feedMetadata.getUrl());

		super.buildFeedMetadata(model, feed, request);
	}

}
