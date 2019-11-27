package debtreg.ControllerTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import debtreg.App;
import debtreg.Entities.AuthProvider;
import debtreg.Entities.Message;
import debtreg.Entities.User;
import debtreg.Entities.UserRole;
import debtreg.Exceptions.BadRequestException;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Payloads.ApiResponse;
import debtreg.Payloads.AuthResponse;
import debtreg.Payloads.LoginRequest;
import debtreg.Payloads.SignUpRequest;
import debtreg.Repositories.MessageRepository;
import debtreg.Repositories.UserRepository;
import debtreg.Security.TokenProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc
@ContextConfiguration
@WebAppConfiguration
public class AuthControllerTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected TokenProvider tokenProvider;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MessageRepository messageRepository;

    @After
    public void resetDb() {
        messageRepository.deleteAll();
        userRepository.deleteAll();
    }
    
    /**
     * Testing user login
     * @throws IOException
     * @throws Exception
     */
    @Test
    public void loginTest() throws IOException, Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String jonasToken = createTestUser(name1, email1);

        LoginRequest login = new LoginRequest();
        login.setEmail(email1);
        login.setPassword("12345");
        
        MvcResult result =
        mvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtil.toJson(login)))
        .andDo(print())
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        AuthResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        assertThat(response).extracting("accessToken").contains(jonasToken);
    }

    /**
     * Testing user singup
     * @throws IOException
     * @throws Exception
     */
    @Test
    public void signupTest() throws IOException, Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";

        SignUpRequest singup = new SignUpRequest();
        singup.setEmail(email1);
        singup.setPassword("12345");
        singup.setName(name1);
        
        MvcResult result =
        mvc.perform(post("/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtil.toJson(singup)))
        .andDo(print())
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ApiResponse.class);
        assertThat(response).extracting("success").contains(true);
        assertThat(response).extracting("message").contains("User registered successfully@");
    }

    @Test
    public void signupTest_inUse() throws IOException, Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";

        String jonasToke = createTestUser(name1, email1);

        SignUpRequest singup = new SignUpRequest();
        singup.setEmail(email1);
        singup.setPassword("12345");
        singup.setName(name1);
        
        MvcResult result =
        mvc.perform(post("/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtil.toJson(singup)))
        .andDo(print())
        .andReturn();

        Optional<BadRequestException> e 
        = Optional.ofNullable((BadRequestException) 
        result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get())
        .isInstanceOf(BadRequestException.class);
    }

    protected String createTestUser(String username, String email){
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