package com.ie.fbrss.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.ie.fbrss.viewer.RssViewer;

@Configuration
@EnableWebMvc
class WebConfig extends WebMvcConfigurerAdapter {

	@Override
	public void configureViewResolvers(final ViewResolverRegistry registry) {
		registry.enableContentNegotiation(new RssViewer());
	}

}
