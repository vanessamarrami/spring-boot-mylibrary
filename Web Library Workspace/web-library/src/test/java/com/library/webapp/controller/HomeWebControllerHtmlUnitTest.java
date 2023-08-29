package com.library.webapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.library.webapp.security.SecurityConfig;

@WebMvcTest(HomeWebController.class)
@Import(SecurityConfig.class)
class HomeWebControllerHtmlUnitTest {

	@Autowired
	private WebClient webClient;
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@BeforeEach
	public void setup() {
		MockMvcBuilders
	            .webAppContextSetup(webApplicationContext)
	            .apply(springSecurity())
	            .build();
	}
	
	@Test
	@WithAnonymousUser
	void testHomePageTitle() throws Exception  {
		HtmlPage page = webClient.getPage("/");
		assertEquals("Library", page.getTitleText());
		HtmlElement h1 = page.getHtmlElementById("indexTitle");
		
		assertEquals( "Welcome to Library!", h1.asNormalizedText());
	}
	
	@Test
	@WithAnonymousUser
	void testHomePage_AllBooks() throws Exception  {
		HtmlPage page = webClient.getPage("/");
		HtmlAnchor a = page.getAnchorByText("List of Books");
		
		assertEquals("/library", a.getHrefAttribute());
	}

	@Test
	@WithAnonymousUser
	void testHomePage_AllAuthors() throws Exception  {
		HtmlPage page = webClient.getPage("/");
		HtmlAnchor a = page.getAnchorByText("List of Authors");
		
		assertEquals("/library/author", a.getHrefAttribute());
	}
	
	@Test
	@WithAnonymousUser
	void testHomePage_Login() throws Exception  {
		HtmlPage page = webClient.getPage("/");
		HtmlAnchor a = page.getAnchorByText("Login");
		
		assertEquals("/login", a.getHrefAttribute());
	}
}
