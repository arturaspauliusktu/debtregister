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
import debtreg.Entities.Debt;
import debtreg.Entities.Deposite;
import debtreg.Entities.User;
import debtreg.Entities.UserRole;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.DebtRepository;
import debtreg.Repositories.DepositeRepository;
import debtreg.Repositories.UserRepository;
import debtreg.Security.TokenProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc 
@ContextConfiguration
@WebAppConfiguration
public class DepositeControllerTest {

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
    protected DebtRepository debtRepository;

    @Autowired
    protected DepositeRepository depositeRepository;

    @After
    public void resetDb(){
        debtRepository.deleteAll();
        depositeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void givenDeposites_whenGetDeposites_thenStatus200() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt1);
        Deposite deposite2 = createTestDeposite(debt2);
        
        mvc.perform(get("/deposites/").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
        .andExpect(jsonPath("$[0].id", is((int)deposite1.getId())))
        .andExpect(jsonPath("$[1].id", is((int)deposite2.getId())))
        .andExpect(status().isOk());
    }

    @Test
    public void givenDeposite_whenGetDeposite_thenStatus200() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt1);
        
        mvc.perform(get("/deposite/"+deposite1.getId()).contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)deposite1.getId())))
        .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void givenDeposite_whenGetDeposite_thenDepositeNotFound()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        createTestDeposite(debt1);
        
        MvcResult result = mvc.perform(
            get("/deposite/"+12314155)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content()
        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        Optional<ResourceNotFoundException> e = 
        Optional.ofNullable((ResourceNotFoundException) 
        result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get())
        .isInstanceOf(Exception.class);
    }




    
    @Test
    public void givenDeposite_whenGetDepositeByDebtId_thenStatus200() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt1);
        
        mvc.perform(get("/debt/"+debt1.getId()+"/deposite").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)deposite1.getId())))
        .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void givenDeposite_whenGetDepositeByDebtId_thenDepositeNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt1);
        
        mvc.perform(get("/debt/"+161651+"/deposite").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)deposite1.getId())));
    }

    @Test
    public void givenDeposite_whenGetDepositeByDebtIdandCurrentUser_thenStatus200() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt1);
        
        mvc.perform(get("/user/me/debt/"+debt1.getId()+"/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)deposite1.getId())))
        .andExpect(status().isOk());
    }


    @Test(expected = Exception.class)
    public void givenDeposite_whenGetDepositeByDebtIdandCurrentUser_thenDepositeNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt1);
        
        mvc.perform(get("/user/me/debt/"+1561611+"/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)deposite1.getId())))
        .andExpect(status().isOk());
    }

    @Test
    public void givenDeposite_whenGetDepositeByAssetIdandCurrentUser_thenStatus200() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt1);
        
        mvc.perform(get("/user/me/asset/"+debt1.getId()+"/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)deposite1.getId())))
        .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void givenDeposite_whenGetDepositeByAssetIdandCurrentUser_thenDepositeNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt1);
        
        mvc.perform(get("/user/me/asset/"+468461515+"/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)deposite1.getId())));
    }

    @Test
    public void whenValid_createDepositeByDebtId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = new Deposite("deposite1", "description1");

        
        MvcResult result = mvc.perform(post("/debt/"+debt1.getId()+"/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Deposite responcedDeposite = objectMapper.readValue(result.getResponse().getContentAsString(), Deposite.class);
        
        List<Deposite> deposites = depositeRepository.findAll();
        assertThat(deposites).extracting("id").contains(responcedDeposite.getId());
    }


    @Test
    public void whenValid_createDepositeByDebtId_thenResourceNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = new Deposite("deposite1", "description1");

        
        MvcResult result = mvc.perform(post("/debt/"+141451+"/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void whenValid_createDepositeByDebtIdofCurrentUser() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = new Deposite("deposite1", "description1");

        
        MvcResult result = mvc.perform(post("/user/me/debt/"+debt1.getId()+"/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Deposite responcedDeposite = objectMapper.readValue(result.getResponse().getContentAsString(), Deposite.class);
        
        List<Deposite> deposites = depositeRepository.findAll();
        assertThat(deposites).extracting("id").contains(responcedDeposite.getId());
    }

    @Test
    public void whenValid_createDepositeByDebtIdofCurrentUser_thenResourceNotFound()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        Deposite deposite1 = new Deposite("deposite1", "description1");

        
        MvcResult result = mvc.perform(post("/user/me/debt/"+1412541+"/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void whenValid_createDeposite() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String jonasToken = createTestUser(name1, email1);

        Deposite deposite1 = new Deposite("deposite1", "description1");

        
        MvcResult result = mvc.perform(post("/deposite")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Deposite responcedDeposite = objectMapper.readValue(result.getResponse().getContentAsString(), Deposite.class);
        
        List<Deposite> deposites = depositeRepository.findAll();
        assertThat(deposites).extracting("id").contains(responcedDeposite.getId());
    }

    @Test
    public void givenDeposite_deleteDeposite() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt);

        
        MvcResult result = mvc.perform(delete("/deposite/"+deposite1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        Optional<Deposite> deposites = depositeRepository.findById(deposite1.getId());
        assertThat(deposites).isEmpty();
    }

    @Test(expected = Exception.class)
    public void givenDeposite_deleteDeposite_thenDepositeNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt);

        
        MvcResult result = mvc.perform(delete("/deposite/"+11651)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andReturn();
    }

    @Test
    public void givenDeposite_deleteDepositeNotInDebt() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Deposite deposite1 = createTestDeposite();

        
        MvcResult result = mvc.perform(delete("/deposite/"+deposite1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        Optional<Deposite> deposites = depositeRepository.findById(deposite1.getId());
        assertThat(deposites).isEmpty();
    }

    @Test
    public void givenDeposite_deleteDepositebyDebtId()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt);

        
        MvcResult result = 
        mvc.perform(delete("/debt/"+debt.getId()+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        Optional<Deposite> deposites = 
        depositeRepository.findById(deposite1.getId());
        assertThat(deposites).isEmpty();
    }

    @Test(expected = Exception.class)
    public void givenDeposite_deleteDepositebyDebtId_thenDebtNotFound()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt);

        
        MvcResult result = 
        mvc.perform(delete("/debt/"+161651+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        Optional<Deposite> deposites = 
        depositeRepository.findById(deposite1.getId());
        assertThat(deposites).isEmpty();
    }

    @Test(expected = Exception.class)
    public void givenDeposite_deleteDepositebyDebtId_thenDontHaveDeposite()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite();

        
        MvcResult result = 
        mvc.perform(delete("/debt/"+debt.getId()+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        Optional<Deposite> deposites = 
        depositeRepository.findById(deposite1.getId());
        assertThat(deposites).isEmpty();
    }

    

    @Test
    public void givenDeposite_deleteDepositebyCurrentUserDebtId()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt);

        
        mvc.perform(delete("/user/me/debt/"+debt.getId()+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        Optional<Deposite> deposites = 
        depositeRepository.findById(deposite1.getId());
        assertThat(deposites).isEmpty();
    }

    @Test
    public void givenDeposite_deleteDepositebyCurrentUserDebtIdgetter()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user2, user1);

        Deposite deposite1 = createTestDeposite(debt);

        
        mvc.perform(delete("/user/me/debt/"+debt.getId()+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        
        Optional<Deposite> deposites = 
        depositeRepository.findById(deposite1.getId());
        assertThat(deposites).isEmpty();
    }

    @Test
    public void givenDeposite_deleteDepositebyCurrentUserDebtId_thenDebtNotFound()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite(debt);

        
        MvcResult result = mvc.perform(delete("/user/me/debt/"+1231241+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andReturn();
        
        Optional<ResourceNotFoundException> e = 
        Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenDeposite_deleteDepositebyCurrentUserDebtId_thenDebtNotFoundGetter()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user2, user1);

        Deposite deposite1 = createTestDeposite(debt);

        
        MvcResult result = mvc.perform(delete("/user/me/debt/"+1231241+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andReturn();
        
        Optional<ResourceNotFoundException> e = 
        Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenDeposite_deleteDepositebyCurrentUserDebtId_thenDontHaveDeposite()
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        Deposite deposite1 = createTestDeposite();

        
        MvcResult result = mvc.perform(delete("/user/me/debt/"+debt.getId()+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print())
        .andReturn();
        
        Optional<ResourceNotFoundException> e = 
        Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }



   @Test
    public void givenDeposite_updateDeposite() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);
        
        Deposite deposite1 = createTestDeposite(debt);
        deposite1.setDescription("updated description");
        
        mvc.perform(put("/deposite/"+deposite1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print())
        .andExpect(status().isNoContent());
        
        List<Deposite> deposites =
        depositeRepository.findAll();
        assertThat(deposites).extracting("description")
        .containsOnly("updated description");
    }

    @Test
    public void givenDeposite_updateDeposite_whenDepositeNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);
        
        Deposite deposite1 = createTestDeposite(debt);
        deposite1.setDescription("updated description");
        
        MvcResult result = mvc.perform(put("/deposite/"+514512)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print()).andReturn();
        
        Optional<ResourceNotFoundException> e = 
        Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenDeposite_updateCurrentUserDepositeByDebtId() 
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);
        
        Deposite deposite1 = createTestDeposite(debt);
        deposite1.setDescription("updated description");
        
        mvc.perform(put("/user/me/debt/"+debt.getId()+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print())
        .andExpect(status().isNoContent());
        
        List<Deposite> deposites =
        depositeRepository.findAll();
        assertThat(deposites).extracting("description")
        .containsOnly("updated description");
    }

    @Test
    public void givenDeposite_updateCurrentUserDepositeByDebtId_thenDontHaveDeposite() 
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);
        
        Deposite deposite1 = createTestDeposite();
        deposite1.setDescription("updated description");
        
        MvcResult result = mvc.perform(put("/user/me/debt/"+debt.getId()+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print()).andReturn();
        
        Optional<ResourceNotFoundException> e = 
        Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenDeposite_updateCurrentUserDepositeByDebtId_thenDebtNotFound() 
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);
        
        Deposite deposite1 = createTestDeposite(debt);
        deposite1.setDescription("updated description");
        
        MvcResult result = mvc.perform(put("/user/me/debt/"+124141+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print()).andReturn();
        
        Optional<ResourceNotFoundException> e = 
        Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void givenDeposite_updateCurrentUserDepositeByDebtId_thenDebtNotFoundgetter() 
    throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user2, user1);
        
        Deposite deposite1 = createTestDeposite(debt);
        deposite1.setDescription("updated description");
        
        MvcResult result = mvc.perform(put("/user/me/debt/"+124141+"/deposite/")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken)
        .content(JsonUtil.toJson(deposite1)))
        .andDo(print()).andReturn();
        
        Optional<ResourceNotFoundException> e = 
        Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
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

    protected Debt createTestDebt(User getter, User giver){
        Debt debt = new Debt(0, 100, getter, giver, null);
        debt = debtRepository.saveAndFlush(debt);
        return debt;
    }

    protected Deposite createTestDeposite(Debt debt){
        Deposite deposite = new Deposite("deposite1", "description");
        deposite = depositeRepository.saveAndFlush(deposite);
        Debt debtresume = debtRepository.findById(debt.getId()).get();
        debtresume.setDeposite(deposite);
        debtRepository.saveAndFlush(debtresume);
        return deposite;
    }

    protected Deposite createTestDeposite(){
        Deposite deposite = new Deposite("deposite1", "description");
        deposite = depositeRepository.saveAndFlush(deposite);
        return deposite;
    }
}