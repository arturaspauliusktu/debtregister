package debtreg.ControllerTests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import debtreg.App;
import debtreg.Entities.Message;
import debtreg.Entities.User;
import debtreg.Entities.UserRole;
import debtreg.Exceptions.BadRequestException;
import debtreg.Exceptions.OAuth2AuthenticationProcessingException;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.MessageRepository;
import debtreg.Repositories.UserRepository;
import debtreg.Security.TokenProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc
@ContextConfiguration
@WebAppConfiguration
public class MiscellaneousTest {

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

    @Test
    public void entitiesTest() {
        Message message = new Message();
        message.getDebt_getter();
        message.getDebt_giver();
        User user = new User(UserRole.ROLE_ADMIN);
        new ResourceNotFoundException("", "", user.getId());
        new ResourceNotFoundException();
        new BadRequestException("");
        new BadRequestException("message", new Throwable());
        new OAuth2AuthenticationProcessingException("msg");
        new OAuth2AuthenticationProcessingException("msg", new Throwable());
    }

    
}