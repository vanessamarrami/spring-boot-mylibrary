package com.library.webapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookSlimDto;
import com.library.webapp.exceptions.NotFoundException;
import com.library.webapp.security.SecurityConfig;
import com.library.webapp.service.LibraryService;


@WebMvcTest(AuthorWebController.class)
@Import(SecurityConfig.class)
class AuthorWebControllerHtmlUnitTest {
	
	@Autowired
	private WebClient webClient;
	
	@MockBean
	LibraryService service;
	
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
	void testTitlePage() throws Exception {
		HtmlPage page = webClient.getPage("/library/author");
		assertEquals("Authors", page.getTitleText());
		
	}

	@Test
	@WithAnonymousUser
	void testLinkNavBar() throws Exception{
		HtmlPage page = webClient.getPage("/library/author");
		HtmlAnchor homelink = page.getAnchorByText("Home");
		HtmlAnchor bookslink = page.getAnchorByText("List of Books");
		HtmlAnchor loginlink = page.getAnchorByText("Login");
		assertEquals("/", homelink.getHrefAttribute());
		assertEquals("/library", bookslink.getHrefAttribute());
		assertEquals("/login", loginlink.getHrefAttribute());
	}
	
	@Test
	@WithUserDetails("admin")
	void testNavBarLinks_authenticated() throws Exception{
		HtmlPage page = webClient.getPage("/library/author");
		assertThat(page.getBody().getTextContent()).contains("Logout");
	}
	
	@Test
	@WithAnonymousUser
	void testTableAuthors() throws Exception{
		List<AuthorDto> allAuthors = List.of(new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "title"))));
		when(service.selAllAuthors()).thenReturn(allAuthors);
		
		HtmlPage page = webClient.getPage("/library/author");
		HtmlTable table = (HtmlTable) page.getElementById("authorsTable");
		assertEquals(table.asNormalizedText(), 
				"Id	First Name	Last Name	Books\n"
				+ "1	FirstName	LastName	 978-88-111-1111-1 title");
		assertThat(page.getBody().getTextContent()).doesNotContain("Back To All Authors");
	}
	
	@Test
	@WithAnonymousUser
	void testTableAuthors_empty_Anonymous() throws Exception{
		when(service.selAllAuthors())
			.thenThrow(new NotFoundException("There is no author!"));
		
		HtmlPage page = webClient.getPage("/library/author");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("There is no author!", page.getHtmlElementById("errMessage").getTextContent());
		
		assertThat(page.getBody().getTextContent()).doesNotContain("Go Back", "Insert your first book!");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Login");
	}
	
	@Test
	@WithUserDetails("admin")
	void testTableAuthors_empty_Admin() throws Exception{
		when(service.selAllAuthors())
			.thenThrow(new NotFoundException("There is no author!"));
		
		HtmlPage page = webClient.getPage("/library/author");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("There is no author!", page.getHtmlElementById("errMessage").getTextContent());
		
		assertThat(page.getBody().getTextContent()).doesNotContain("Go Back", "Login");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Insert your first book!");
	}
	
	@Test
	@WithUserDetails("admin")
	void testExistenceButtonEdit_AuthorsTable() throws Exception{
		List<AuthorDto> allAuthors = List.of(new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "title"))));
		when(service.selAllAuthors()).thenReturn(allAuthors);
		
		HtmlPage page = webClient.getPage("/library/author");
		HtmlAnchor edit = page.getAnchorByHref("/library/author/update/1");
		assertNotNull(edit);
	}
	
	@Test
	@WithUserDetails("admin")
	void testUpdateAuthor_WhenClickEditInTable() throws Exception {
		AuthorDto author = new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "title")));
		when(service.selAllAuthors()).thenReturn(List.of(author));
		when(service.selAuthorById(1L)).thenReturn(author);
		
		HtmlPage page = webClient.getPage("/library/author");
		HtmlAnchor edit = page.getAnchorByHref("/library/author/update/1");
		HtmlPage pageEditBook = edit.click();
		assertEquals("Edit Author", pageEditBook.getTitleText());
		final HtmlForm form = pageEditBook.getFormByName("authorForm");
		assertThat(form.getInputByName("id").getValueAttribute()).isNotNull();
		assertThat(form.getInputByName("firstName").getValueAttribute()).isNotNull();
		assertThat(form.getInputByName("lastName").getValueAttribute()).isNotNull();
		
	}
	
	@Test
	@WithAnonymousUser
	void testButtonUpdateAuthorNotVisible_Anonymous() throws Exception {
		AuthorDto author = new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "title")));
		when(service.selAllAuthors()).thenReturn(List.of(author));
		
		HtmlPage page = webClient.getPage("/library/author");
		assertThat(page.getBody().getTextContent()).doesNotContain("/library/author/update/1");
		
	}
	
	@Test
	@WithUserDetails("admin")
	void testEdit_ValidInput() throws Exception {
		AuthorDto author = new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "title")));
		AuthorSlimDto authorSlim = new AuthorSlimDto(1L, "FirstName", "LastName");
		when(service.selAuthorById(1L)).thenReturn(author);
		when(service.saveAuthor(authorSlim)).thenReturn(true);
		
		HtmlPage page = webClient.getPage("/library/author/update/1");
		final HtmlForm form = page.getFormByName("authorForm");
		form.getInputByValue("FirstName").setValueAttribute("NewFirstName");
		HtmlInput submit = form.getInputByName("submitButton");
	    HtmlPage resultPage = submit.click();
	    assertThat(resultPage.getTitleText()).isEqualTo("Authors");
	}
	
	@Test
	@WithUserDetails("admin")
	void testEdit_InvalidInput() throws Exception {
		AuthorDto author = new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "title")));
		when(service.selAuthorById(1L)).thenReturn(author);
		
		HtmlPage page = webClient.getPage("/library/author/update/1");
		final HtmlForm form = page.getFormByName("authorForm");
		form.getInputByValue("FirstName").setValueAttribute("");
		HtmlInput submit = form.getInputByName("submitButton");
	    HtmlPage resultPage = submit.click();
	    assertThat(resultPage.getTitleText()).isEqualTo("Edit Author");
	}
	
	@Test
	@WithUserDetails("admin")
	void testElement_EditPage() throws Exception {
		AuthorDto author = new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "title")));
		when(service.selAuthorById(1L)).thenReturn(author);
		HtmlPage page = webClient.getPage("/library/author/update/1");
		HtmlAnchor homelink = page.getAnchorByText("Home");
		HtmlAnchor bookslink = page.getAnchorByText("List of Books");
		HtmlAnchor authorslink = page.getAnchorByText("List of Authors");
		assertEquals("/", homelink.getHrefAttribute());
		assertEquals("/library", bookslink.getHrefAttribute());
		assertEquals("/library/author", authorslink.getHrefAttribute());
	}
	
	@Test
	@WithAnonymousUser
	void testFindByName() throws Exception {
		AuthorDto author = new AuthorDto(1L , "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "title")));
		when(service.findAuthorByNameAndSurname("FirstName", "LastName")).thenReturn(author);
		
		HtmlPage page = webClient.getPage("/library/author");
		HtmlTextInput firstnameInput = page.getElementByName("firstNameFound");
		firstnameInput.setValue("FirstName");
		HtmlTextInput lastnameInput = page.getElementByName("lastNameFound");
		lastnameInput.setValue("LastName");
		HtmlButton searchButton = (HtmlButton) page.getElementById("buttonName");
		HtmlPage result = searchButton.click();
		
		HtmlTable table = (HtmlTable) result.getElementById("authorsTable");
		assertThat(result.getUrl().getPath()).isEqualTo("/library/author/name/FirstName/LastName");
		assertEquals(table.asNormalizedText(), 
				"Id	First Name	Last Name	Books\n"
				+ "1	FirstName	LastName	 978-88-111-1111-1 title");
		
		assertThat(result.getBody().getTextContent()).contains("Back To All Authors");
	}
	
	@Test
	@WithAnonymousUser
	void testFindByName_errorPage() throws Exception {
		when(service.findAuthorByNameAndSurname("FirstName", "LastName" ))
		.thenThrow(new NotFoundException("There is no author with the specified name."));
		
		HtmlPage page = webClient.getPage("/library/author/name/FirstName/LastName");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("There is no author with the specified name.", page.getHtmlElementById("errMessage").getTextContent());

		assertThat(page.getBody().getTextContent()).doesNotContain("Insert your first book!");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Go Back");
	}
	
	@Test
	@WithUserDetails("admin")
	void testlogout() throws Exception {
		HtmlPage page = webClient.getPage("/library/author");
		HtmlButton logout = (HtmlButton) page.getElementById("buttonLogout");
		HtmlPage home = logout.click();
		assertEquals(home.getTitleText(), "Library");
	}
}
