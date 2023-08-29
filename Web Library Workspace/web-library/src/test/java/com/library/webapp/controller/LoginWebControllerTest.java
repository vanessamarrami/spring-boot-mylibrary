package com.library.webapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.library.webapp.security.SecurityConfig;

@WebMvcTest(controllers = LoginWebController.class)
@Import(SecurityConfig.class)
class LoginWebControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
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
	void testSuccessEndpointLogin() throws Exception {
		mockMvc.perform(get("/login")).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@WithAnonymousUser
	void testReturnLoginView() throws Exception {
		ModelAndViewAssert
			.assertViewName(mockMvc.perform(get("/login"))
				.andReturn().getModelAndView(), "login" );
	}

}
