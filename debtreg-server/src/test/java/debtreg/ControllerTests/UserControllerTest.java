package debtreg.ControllerTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

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

    /**
     * Unit test to validate input and then create user.
     * JWT is gathered and atached to authorization header in order to mock API call.
     * @throws Exception
     * @throws IOException
     */
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
        mvc.perform(post("/user")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtil.toJson(user)));
        List<User> found = userRepository.findAll();
        assertThat(found).extracting(User::getUsername).contains("Jonas");
    }

    /**
     * adds two users to repository and then gets users by calling API
     * checks if json contains same names as user objects. Then checks HTTP status code. 
     * @throws Exception
     */
    @Test
    public void givenUser_whenGetUsers_thenStatus200() throws Exception {
        String jonasToken = createTestUser("Jonas", "jonas@mail.com");
        createTestUser("Lukas", "petras@mail.com");

        mvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
        .andExpect(jsonPath("$[0].username", is("Jonas")))
        .andExpect(jsonPath("$[1].username", is("Lukas")));
    }


    @Test
    public void giveUser_whenGetUser_thenStatus200() throws Exception {
        String name = "Jonas";
        String email = "jonas@mail.com";
        String jonasToken = createTestUser(name , email);
        User user = userRepository.findByEmail(email).get();
        mvc.perform(get("/user/"+user.getId()).contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.username", is(name)));
    }

    @Test(expected = Exception.class)
    public void getUser_thenUserNotFound() throws Exception {
        String name = "Jonas";
        String email = "jonas@mail.com";
        String jonasToken = createTestUser(name , email);
        mvc.perform(get("/user/789946112133168").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken));
    }

    @Test
    public void getCurrentUser_thenStatus200() throws Exception{
        String name = "Jonas";
        String email = "jonas@mail.com";
        String jonasToken = createTestUser(name , email);
        mvc.perform(get("/user/me").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.username", is(name)))
        .andExpect(status().isOk());
    }

    @Test
    public void deleteUser_thenStatus204() throws Exception{
        String name = "Jonas";
        String email = "jonas@mail.com";
        String jonasToken = createTestUser(name , email);
        User user = userRepository.findByEmail(email).get();
        mvc.perform(delete("/user/"+user.getId()).contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(status().isNoContent());
    }

    @Test
    public void patchUser_thenStatus204() throws Exception {
        String name = "Jonas";
        String email = "jonas@mail.com";
        String name1 = "Petras";
        String email1 = "Petras@mail.com";
        String jonasToken = createTestUser(name , email);
        createTestUser(name1, email1);
        User user = userRepository.findByEmail(email).get();
        User user1 = userRepository.findByEmail(email1).get();

        mvc.perform(patch("/user/"+user.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(user1)))
        .andDo(print())
        .andExpect(status().isNoContent());
    }

    @Test
    public void patchUser_thenStatus204_withnulls() throws Exception {
        String name = "Jonas";
        String email = "jonas@mail.com";
        String name1 = "Petras";
        String email1 = "Petras@mail.com";
        String jonasToken = createTestUser(name , email);
        createTestUser(name1, email1);
        User user = userRepository.findByEmail(email).get();
        User user1 = userRepository.findByEmail(email1).get();

        user1.setId(0);
        user1.setUsername("");
        user1.setPassword("");
        user1.setRegistration(null);

        mvc.perform(patch("/user/"+user.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(user1)))
        .andDo(print())
        .andExpect(status().isNoContent());
    }

    @Test(expected = Exception.class)
    public void patchUser_thenUserNotFound() throws Exception {
        String name = "Jonas";
        String email = "jonas@mail.com";

        String jonasToken = createTestUser(name , email);
        User user = userRepository.findByEmail(email).get();

        mvc.perform(patch("/user/789946112133168")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(user)))
        .andDo(print())
        .andExpect(status().isNoContent());
    }

    private String createTestUser(String username, String email){
        User user = new User(new Long(123), username, email, "12345", new Date(), "", "123", UserRole.ROLE_ADMIN);
        user.setProvider(AuthProvider.local);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.saveAndFlush(user);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    "12345"
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.createToken(authentication);

        return accessToken;
    }
}