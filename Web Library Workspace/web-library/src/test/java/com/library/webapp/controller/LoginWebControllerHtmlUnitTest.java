package com.library.webapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.library.webapp.security.SecurityConfig;

@WebMvcTest(controllers = LoginWebController.class)
@Import(SecurityConfig.class)
class LoginWebControllerHtmlUnitTest {
	
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
	void testLoginPageTitle() throws Exception  {
		HtmlPage page = webClient.getPage("/login");
		HtmlElement h1 = page.getHtmlElementById("titleH1");
		assertEquals("Login", page.getTitleText());
		assertEquals("Login", h1.asNormalizedText());
	}
	
	@Test
	@WithAnonymousUser
	void testLogin_ValidInput() throws Exception {
		HtmlPage page = webClient.getPage("/login");
		HtmlForm form = page.getFormByName("formLogin");
		form.getInputByName("userId").setValueAttribute("admin");
		form.getInputByName("password").setValueAttribute("adminPass");
		HtmlInput submit = form.getInputByName("submitlogin");
	    HtmlPage resultPage = submit.click();
	    assertThat(resultPage.getUrl().getPath()).isEqualTo("/");
	}
	
	@Test
	@WithAnonymousUser
	void testLogin_InvalidInput() throws Exception {
		HtmlPage page = webClient.getPage("/login");
		HtmlForm form = page.getFormByName("formLogin");
		form.getInputByName("userId").setValueAttribute("");
		form.getInputByName("password").setValueAttribute("");
		HtmlInput submit = form.getInputByName("submitlogin");
	    HtmlPage resultPage = submit.click();
	    assertThat(resultPage.getUrl().getPath()).isEqualTo("/login");
	}
	
	@Test
	@WithAnonymousUser
	void testLogin_wrongInput() throws Exception {
		HtmlPage page = webClient.getPage("/login");
		final HtmlForm form = page.getFormByName("formLogin");
		form.getInputByName("userId").setValueAttribute("user");
		form.getInputByName("password").setValueAttribute("user");
		HtmlInput submit = form.getInputByName("submitlogin");
	    HtmlPage resultPage = submit.click();
	    assertThat(resultPage.getUrl().getPath()).isEqualTo("/login");
	}
	
}
