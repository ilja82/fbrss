package com.ie.fbrss.controller;

import com.ie.fbrss.fb.FbCollector;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.HandlerMapping;

@Controller
final class RootController {

    private final FbCollector facebookCollector;

    @Inject
    private RootController(final FbCollector facebookCollector) {
        this.facebookCollector = facebookCollector;
    }

    @GetMapping(value = "/favicon.ico")
    public ResponseEntity getFavicon() {
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "**")
    public String getFeedInRss(final HttpServletRequest request, final Model model) {
        final String fbUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        facebookCollector.collectData(fbUrl.replaceFirst(".*facebook\\.com", "www.facebook.com"), model);

        return "atomFeedView";
    }
}
