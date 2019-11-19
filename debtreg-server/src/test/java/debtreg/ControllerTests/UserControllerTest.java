package debtreg.ControllerTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import debtreg.App;
import debtreg.Controllers.UserController;
import debtreg.Entities.AuthProvider;
import debtreg.Entities.User;
import debtreg.Entities.UserRole;
import debtreg.Repositories.UserRepository;
import debtreg.Security.TokenProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc 
@ContextConfiguration
@WebAppConfiguration
public class UserControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @After
    public void resetDb(){
        userRepository.deleteAll();
    }

    @Test
    public void whenValidInput_thenCreateUser() throws Exception, IOException{
        User user = new User(new Long(123), "Jonas", "jonas@mail.com", "12345", new Date(), "", "123", UserRole.ROLE_USER);
        user.setProvider(AuthProvider.local);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User user1 = new User(new Long(123), "Petras", "petras@mail.com", "12345", new Date(), "", "123", UserRole.ROLE_ADMIN);
        user1.setProvider(AuthProvider.local);
        user1.setPassword(passwordEncoder.encode(user1.getPassword()));
        userRepository.save(user1);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user1.getEmail(),
                        "12345"
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.createToken(authentication);
        MvcResult result = mvc.perform(post("/user")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtil.toJson(user)))
        .andReturn();
        List<User> found = userRepository.findAll();
        assertThat(found).extracting(User::getUsername).contains("Jonas");
    }

    @Test
    public void givenUser_whenGetUsers_thenStatus200() throws Exception {
        createTestUser("Jonas");
        createTestUser("Lukas");

        mvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
        .andExpect(jsonPath("$[0].username", is("Jonas")))
        .andExpect(jsonPath("$[1].username", is("Lukas")));
    }

    public void giveUser_whenGetUser_thenStatus200() throws Exception {
        createTestUser("Jonas");

        mvc.perform(get("/user/123").contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].username", is("Jonas")));
    }

    private User createTestUser(String username){
        User user = new User(new Long(123), username, "jonas@mail.com", "12345", new Date(), "", "123");
        userRepository.saveAndFlush(user);
        return user;
    }
}