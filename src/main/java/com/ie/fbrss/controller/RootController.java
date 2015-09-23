package com.ie.fbrss.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

import com.ie.fbrss.fb.FbCollector;

@Controller
final class RootController {

	private final FbCollector facebookCollector;

	@Inject
	private RootController(final FbCollector facebookCollector) {
		this.facebookCollector = facebookCollector;
	}

	@RequestMapping(value = "**", method = RequestMethod.GET)
	public String getFeedInRss(final HttpServletRequest request, final Model model) {
		final String fbUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		facebookCollector.collectData(fbUrl.replaceFirst(".*facebook\\.com", "www.facebook.com"), model);

		return "rssViewer";
	}
}
