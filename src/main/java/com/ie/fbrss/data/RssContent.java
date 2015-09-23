package com.ie.fbrss.data;

import java.util.Date;

public final class RssContent {
	private String title;
	private String url;
	private String summary;
	private Date createdDate;

	public Date getCreatedDate() {
		return createdDate;
	}

	public String getSummary() {
		return summary;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public void setCreatedDate(final Date createdDate) {
		this.createdDate = createdDate;
	}

	public void setSummary(final String summary) {
		this.summary = summary;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public void setUrl(final String url) {
		this.url = url;
	}
}