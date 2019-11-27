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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpHeaders;
import org.assertj.core.api.EnumerableAssert;
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
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.MessageRepository;
import debtreg.Repositories.UserRepository;
import debtreg.Security.TokenProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc
@ContextConfiguration
@WebAppConfiguration
public class MessageControllerTest {

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

    @Test
    public void givenMessages_thenGetMessages() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());
        Message message2 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/messages/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION,
                "Bearer " + jonasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].id", is((int) message1.getId())))
                .andExpect(jsonPath("$[1].id", is((int) message2.getId()))).andExpect(status().isOk());
    }

    @Test
    public void givenMessages_thenGetMessage() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/message/" + message1.getId()).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is((int) message1.getId()))).andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void givenMessages_whenGetMessage_thenMessageNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/message/" + 241241).contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION,
                "Bearer " + jonasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is((int) message1.getId()))).andExpect(status().isOk());
    }

    @Test
    public void givenMessages_thenGetMessageByGiverId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());
        Message message2 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/giver/" + user2.getId() + "/messages").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.content[0].id", is((int) message1.getId())))
                .andExpect(jsonPath("$.content[1].id", is((int) message2.getId()))).andExpect(status().isOk());
    }

    @Test
    public void givenMessages_thenGetMessageByCurrentGiverId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());
        Message message2 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/giver/me/messages").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION,
                "Bearer " + petrasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.content[0].id", is((int) message1.getId())))
                .andExpect(jsonPath("$.content[1].id", is((int) message2.getId()))).andExpect(status().isOk());
    }

    @Test
    public void givenMessages_thenGetMessageByCurrentGiverIdAndMessageId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());
        // Message message2 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/giver/me/messages/" + message1.getId()).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.id", is((int) message1.getId())))
                // .andExpect(jsonPath("$.content[1].id", is((int)message2.getId())))
                .andExpect(status().isOk());
    }

    @Test
    public void givenMessages_thenGetMessageByGetterId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());
        Message message2 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/getter/" + user1.getId() + "/messages").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.content[0].id", is((int) message1.getId())))
                .andExpect(jsonPath("$.content[1].id", is((int) message2.getId()))).andExpect(status().isOk());
    }

    @Test
    public void givenMessages_thenGetMessageByCurrentGetterId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());
        Message message2 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/getter/me/messages").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION,
                "Bearer " + jonasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.content[0].id", is((int) message1.getId())))
                .andExpect(jsonPath("$.content[1].id", is((int) message2.getId()))).andExpect(status().isOk());
    }

    @Test
    public void givenMessages_thenGetMessageByCurrentGetterIdAndMessageId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());
        // Message message2 = createTestMessage(user1, user2, user1.getId());

        mvc.perform(get("/getter/me/messages/" + message1.getId()).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)).andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.id", is((int) message1.getId())))
                // .andExpect(jsonPath("$.content[1].id", is((int)message2.getId())))
                .andExpect(status().isOk());
    }

    @Test
    public void whenValid_thenAddMessageToGiver() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = new Message(0, "text1", new Date(), user1, user2, user1.getId());

        MvcResult a = mvc
                .perform(post("/giver/" + user1.getId() + "/message/").contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken).content(JsonUtil.toJson(message)))
                .andDo(print()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Message responcemessage = objectMapper.readValue(a.getResponse().getContentAsString(), Message.class);

        List<Message> messages = messageRepository.findAll();
        assertThat(messages).extracting("id").contains(responcemessage.getId());
    }

    @Test
    public void whenValid_thenAddMessageToGiver_thenUserNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = new Message(0, "text1", new Date(), user1, user2, user1.getId());

        MvcResult result = mvc
                .perform(post("/giver/" + 151616 + "/message/").contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken).content(JsonUtil.toJson(message)))
                .andDo(print()).andReturn();

        Optional<ResourceNotFoundException> e = Optional
                .ofNullable((ResourceNotFoundException) result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void whenValid_thenAddMessageToCurrentGiver()
    throws Exception{
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = new Message(0, "text1", new Date(), user1, user2, user1.getId());

        MvcResult a = mvc.perform(post("/giver/me/message/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Message responcemessage = objectMapper.readValue(a.getResponse().getContentAsString(), Message.class);
        
        List<Message> messages = messageRepository.findAll();
        assertThat(messages).extracting("id").contains(responcemessage.getId());
    }

    @Test
    public void whenValid_thenAddMessageToGetter()
    throws Exception{
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = new Message(0, "text1", new Date(), user1, user2, user1.getId());

        MvcResult a = mvc.perform(post("/getter/"+user1.getId()+"/message/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Message responcemessage = objectMapper.readValue(a.getResponse().getContentAsString(), Message.class);
        
        List<Message> messages = messageRepository.findAll();
        assertThat(messages).extracting("id").contains(responcemessage.getId());
    }

    @Test
    public void whenValid_thenAddMessageToGetter_UserNotFound()
    throws Exception{
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = new Message(0, "text1", new Date(), user1, user2, user1.getId());

        MvcResult result = mvc.perform(post("/getter/"+15125+"/message/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void whenValid_thenAddMessageToCurrentGetter()
    throws Exception{
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = new Message(0, "text1", new Date(), user1, user2, user1.getId());

        MvcResult a = mvc.perform(post("/getter/me/message/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Message responcemessage = objectMapper.readValue(a.getResponse().getContentAsString(), Message.class);
        
        List<Message> messages = messageRepository.findAll();
        assertThat(messages).extracting("id").contains(responcemessage.getId());
    }

    
    @Test
    public void givenMessage_deleteMessage() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());

        
        MvcResult result = mvc.perform(delete("/message/"+message1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andReturn();
        
        Optional<Message> message = messageRepository.findById(message1.getId());
        ((List<Message>) assertThat(message)).isEmpty();
    }

    @Test
    public void givenMessage_deleteMessage_thenCantDelete() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user2, user2, user2.getId());

        
        MvcResult result = mvc.perform(delete("/message/"+1651515)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andReturn();
        
        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenMessage_deleteMessageByUserAndMessageId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());

        
        MvcResult result = 
        mvc.perform(delete("/user/"+user1.getId()+"/message/"+message1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andReturn();
        
        Optional<Message> message = messageRepository.findById(message1.getId());
        ((List<Message>) assertThat(message)).isEmpty();
    }

    @Test
    public void givenMessage_deleteMessageByUserAndMessageId_thenNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());

        
        MvcResult result = 
        mvc.perform(delete("/user/"+115661+"/message/"+message1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andReturn();
        
        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);

    }

    @Test
    public void givenMessage_deleteMessageByCurrentUserAndMessageId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());

        
        MvcResult result = 
        mvc.perform(delete("/user/me/message/"+message1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andReturn();
        
        Optional<Message> message = messageRepository.findById(message1.getId());
        ((List<Message>) assertThat(message)).isEmpty();
    }

    @Test
    public void givenMessage_deleteMessageByCurrentUserAndMessageId_thenNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message1 = createTestMessage(user1, user2, user1.getId());

        
        MvcResult result = 
        mvc.perform(delete("/user/me/message/"+1241241)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andReturn();
        
        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenMessage_updateMessage() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        message.setText("updated message");
        
        mvc.perform(put("/message/"+message.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(status().isNoContent());
        
        List<Message> Messages =
        messageRepository.findAll();
        assertThat(Messages).extracting("text")
        .containsOnly("updated message");
    }

    @Test
    public void givenMessage_updateMessage_nulls() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        Long savedId = message.getId();
        message.setId(0);
        message.setText("");
        message.setGetter(null);
        message.setGiver(null);
        message.setDate(null);
        
        mvc.perform(put("/message/"+savedId)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(status().isNoContent());
    }

    @Test
    public void givenMessage_updateMessage_thenNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        message.setText("updated message");
        
        MvcResult result = mvc.perform(put("/message/"+121241)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andReturn();
        
        Optional<ResourceNotFoundException> e 
        = Optional.ofNullable((ResourceNotFoundException) 
        result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get())
        .isInstanceOf(ResourceNotFoundException.class);

    }

    
    @Test
    public void givenMessage_updateMessageByUserIdAndMessageId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        message.setText("updated message");
        
        mvc.perform(put("/user/"+user1.getId()+"/message/"+message.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(status().isOk());
        
        List<Message> messages =
        messageRepository.findAll();
        assertThat(messages).extracting("text")
        .containsOnly("updated message");
    }

    @Test
    public void givenMessage_updateMessageByUserIdAndMessageId_nulls() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        Long savedId = message.getId();
        message.setId(0);
        message.setText("");
        message.setGetter(null);
        message.setGiver(null);
        message.setDate(null);
        
        mvc.perform(put("/user/"+user1.getId()+"/message/"+savedId)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(status().isOk());
    
    }

    @Test
    public void givenMessage_updateMessageByUserIdAndMessageId_thenNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        message.setText("updated message");
        
        MvcResult result = mvc.perform(put("/user/"+124124+"/message/"+message.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print()).andReturn();
        
        Optional<ResourceNotFoundException> e 
        = Optional.ofNullable((ResourceNotFoundException) 
        result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get())
        .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenMessage_updateMessageByUserIdAndMessageId_thenMessageNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        message.setText("updated message");
        
        MvcResult result = mvc.perform(put("/user/"+user1.getId()+"/message/"+124124)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print()).andReturn();
        
        Optional<ResourceNotFoundException> e 
        = Optional.ofNullable((ResourceNotFoundException) 
        result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get())
        .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenMessage_updateMessageByCurrentUserIdAndMessageId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        message.setText("updated message");
        
        mvc.perform(put("/user/me/message/"+message.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(status().isOk());
        
        List<Message> Messages =
        messageRepository.findAll();
        assertThat(Messages).extracting("text")
        .containsOnly("updated message");
    }

    @Test
    public void givenMessage_updateMessageByCurrentUserIdAndMessageId_nulls() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        Long messageId = message.getId();
        message.setId(0);
        message.setText("");
        message.setDebt_getter(null);
        message.setDebt_giver(null);
        message.setDate(null);
        
        mvc.perform(put("/user/me/message/"+messageId)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andExpect(status().isOk());
    }


    @Test
    public void givenMessage_updateMessageByCurrentUserIdAndMessageId_thenNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Message message = createTestMessage(user1, user2, user1.getId());
        message.setText("updated message");
        
        MvcResult result = mvc.perform(put("/user/me/message/"+123141)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(message)))
        .andDo(print())
        .andReturn();
        
        Optional<ResourceNotFoundException> e 
        = Optional.ofNullable((ResourceNotFoundException) 
        result.getResolvedException());
        ((EnumerableAssert<?, Message>) assertThat(e)).isNotEmpty();
        assertThat(e.get())
        .isInstanceOf(ResourceNotFoundException.class);
    }

    //Not unit test _____________________________________________________
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

    protected Message createTestMessage(User getter, User giver, Long owner){
        Message message = new Message(0, "message1", new Date(), getter, giver, owner);
        message = messageRepository.saveAndFlush(message);
        return message;
    }
}