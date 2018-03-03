package com.ie.fbrss.viewer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import com.ie.fbrss.data.AtomContent;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;

@Component("atomFeedView")
public final class AtomViewer extends AbstractAtomFeedView {

    private static Entry mapToAtom(AtomContent element) {
        final Entry entry = new Entry();

        entry.setTitle(element.getTitle());

        Content summary = toContent(element.getSummary());
        entry.setSummary(summary);

        Link link = toLink(element.getUrl());
        entry.setAlternateLinks(Collections.singletonList(link));

        entry.setPublished(element.getCreatedDate());

        return entry;
    }

    private static Link toLink(String url) {
        Link link = new Link();
        link.setHref(url);
        return link;
    }

    private static Content toContent(String summary) {
        final Content bodyText = new Content();
        bodyText.setValue(summary);
        return bodyText;
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
        final AtomContent feedMetadata = (AtomContent) model.get("feedMetadata");
        feed.setTitle(feedMetadata.getTitle());

        Link link = toLink(feedMetadata.getUrl());
        feed.setAlternateLinks(Collections.singletonList(link));
    }

    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
        @SuppressWarnings("unchecked") final List<AtomContent> elements = (List<AtomContent>) model.get("feedContent");

        return elements.stream()
                .map(AtomViewer::mapToAtom)
                .collect(Collectors.toList());
    }
}
