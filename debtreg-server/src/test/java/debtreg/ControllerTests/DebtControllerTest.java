package debtreg.ControllerTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import debtreg.App;
import debtreg.Entities.AuthProvider;
import debtreg.Entities.Debt;
import debtreg.Entities.Deposite;
import debtreg.Entities.User;
import debtreg.Entities.UserRole;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.DebtRepository;
import debtreg.Repositories.UserRepository;
import debtreg.Security.TokenProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc 
@ContextConfiguration
@WebAppConfiguration
public class DebtControllerTest {
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

    @Autowired
    private DebtRepository debtRepository;

    @After
    public void resetDb(){
        debtRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void givenUserDebt_whenGetDebts_thenStatus200() throws Exception {
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


        mvc.perform(get("/debts").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
        .andExpect(jsonPath("$[0].id", is((int)debt1.getId())))
        .andExpect(jsonPath("$[1].id", is((int)debt2.getId())))
        .andExpect(status().isOk());
    }

    @Test
    public void givenUserDebt_whenGetDebt_thenStatus200() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        mvc.perform(get("/debt/"+(int)debt1.getId()).contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)debt1.getId())))
        .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void givenUserDebt_whenGetDebt_thenDebtNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);

        mvc.perform(get("/debt/48941656151").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is((int)debt1.getId())))
        .andExpect(status().isOk());
    }

    @Test
    public void givenUserDebtbyUserID_whenGetDebt_thenStatus200() throws Exception {
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

        mvc.perform(get("/user/"+(int)user1.getId()+"/debts").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
        .andExpect(jsonPath("$.content.[0].id", is((int)debt1.getId())))
        .andExpect(jsonPath("$.content.[1].id", is((int)debt2.getId())))
        .andExpect(status().isOk());
    }

    @Test
    public void givenUserDebtbyUserMe_whenGetDebt_thenStatus200() throws Exception {
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

        mvc.perform(get("/user/me/debts").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
        .andExpect(jsonPath("$.content.[0].id", is((int)debt1.getId())))
        .andExpect(jsonPath("$.content.[1].id", is((int)debt2.getId())))
        .andExpect(status().isOk());
    }

    @Test
    public void givenUserAssetsbyUserID_whenGetAssets_thenStatus200() throws Exception {
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

        mvc.perform(get("/user/"+(int)user2.getId()+"/assets").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
        .andExpect(jsonPath("$.content.[0].id", is((int)debt1.getId())))
        .andExpect(jsonPath("$.content.[1].id", is((int)debt2.getId())))
        .andExpect(status().isOk());
    }

    @Test
    public void givenUserAssetsbyUserMe_whenGetAssets_thenStatus200() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        createTestUser(name1, email1);
        String petrasToken = createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = createTestDebt(user1, user2);

        mvc.perform(get("/user/me/assets").contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + petrasToken))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
        .andExpect(jsonPath("$.content.[0].id", is((int)debt1.getId())))
        .andExpect(jsonPath("$.content.[1].id", is((int)debt2.getId())))
        .andExpect(status().isOk());
    }

    @Test
    public void whenValid_thenCreateAssetByUserId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = new Debt(1, 100, user2, user1, null);

        MvcResult a = mvc.perform(post("/giver/"+user1.getId()+"/debt")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Debt responcedebt = objectMapper.readValue(a.getResponse().getContentAsString(), Debt.class);
        
        List<Debt> debts = debtRepository.findAll();
        assertThat(debts).extracting("id").contains(responcedebt.getId());
    }

    @Test
    public void whenValid_whenCreateAssetByUserId_thenResourceNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = new Debt(1, 100, user2, user1, null);

        MvcResult a = mvc.perform(post("/giver/"+156156614+"/debt")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt)))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) a.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void whenValid_thenCreateDebtByUserId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = new Debt(1, 100, user1, user2, null);

        MvcResult a = mvc.perform(post("/getter/"+user1.getId()+"/debt")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Debt responcedebt = objectMapper.readValue(a.getResponse().getContentAsString(), Debt.class);
        
        List<Debt> debts = debtRepository.findAll();
        assertThat(debts).extracting("id").contains(responcedebt.getId());
    }

    @Test
    public void whenValid_whenCreateDebtByUserId_thenResourceNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = new Debt(1, 100, user1, user2, null);

        MvcResult a = mvc.perform(post("/getter/"+1516115+"/debt")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt)))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) a.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void whenValid_thenCreateAssetByCurrentUser() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = new Debt(1, 100, user2, user1, null);

        MvcResult a = mvc.perform(post("/giver/me/debt")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Debt responcedebt = objectMapper.readValue(a.getResponse().getContentAsString(), Debt.class);
        
        List<Debt> debts = debtRepository.findAll();
        assertThat(debts).extracting("id").contains(responcedebt.getId());
    }

    @Test
    public void whenValid_thenCreateDebtByCurrentUser() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = new Debt(1, 100, user1, user2, null);

        MvcResult a = mvc.perform(post("/getter/me/debt")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Debt responcedebt = objectMapper.readValue(a.getResponse().getContentAsString(), Debt.class);
        
        List<Debt> debts = debtRepository.findAll();
        assertThat(debts).extracting("id").contains(responcedebt.getId());
    }


    @Test
    public void createDebt_thenDeleteDebtByUserIdandDebtId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        mvc.perform(delete("/user/"+user1.getId()+"/debt/"+debt.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(status().isOk());
    }

    @Test
    public void createDebt_whenDeleteDebtByUserIdandDebtId_thenThrowResourceNotfound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        MvcResult result = mvc.perform(delete("/user/"+4941516+"/debt/"+debt.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print()).andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void createDebt_thenDeleteDebtByCurrentUserandDebtId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        mvc.perform(delete("/user/me/debt/"+debt.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andExpect(status().isOk());
    }

    @Test
    public void createDebt_whenDeleteDebtByCurrentUserandDebtId_thenResourceNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt = createTestDebt(user1, user2);

        MvcResult result = mvc.perform(delete("/user/me/debt/"+416164)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    
    @Test
    public void createDebt_thenPutDebtByUserIdandDebtId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = new Debt(debt1.getId(), 
        debt1.getMoneysum(),
        debt1.getGetter(),
        debt1.getGiver(),
        debt1.getDeposite());
        debt2.setMoneysum(500);

        MvcResult result = mvc.perform(put("/user/"+user1.getId()+"/debt/"+debt1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt2)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Debt debt = objectMapper.readValue(result.
        getResponse().getContentAsString(), Debt.class);

        List<Debt> found = debtRepository.findAll();

        assertThat(found).extracting("id").containsOnly(debt.getId());
        assertThat(found).extracting("moneysum").containsOnly(debt.getMoneysum());
    }

    @Test
    public void createDebt_whenPutDebtByUserIdandDebtId_thenUserNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = new Debt(debt1.getId(), 
        debt1.getMoneysum(),
        debt1.getGetter(),
        debt1.getGiver(),
        debt1.getDeposite());
        debt2.setMoneysum(500);

        MvcResult result = mvc.perform(put("/user/"+561165156+"/debt/"+debt1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt2)))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void createDebt_whenPutDebtByUserIdandDebtId_thenDebtNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = new Debt(debt1.getId(), 
        debt1.getMoneysum(),
        debt1.getGetter(),
        debt1.getGiver(),
        debt1.getDeposite());
        debt2.setMoneysum(500);

        MvcResult result = mvc.perform(put("/user/"+user1.getId()+"/debt/"+16161561)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt2)))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void createDebt_thenPutNullDebtByUserIdandDebtId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = new Debt(0, 
        0,
        null,
        null,
        debt1.getDeposite());

        MvcResult result = mvc.perform(put("/user/"+user1.getId()+"/debt/"+debt1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt2)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Debt debt = objectMapper.readValue(result.
        getResponse().getContentAsString(), Debt.class);

        List<Debt> found = debtRepository.findAll();

        assertThat(found).extracting("id").containsOnly(debt.getId());
        assertThat(found).extracting("moneysum").containsOnly(debt.getMoneysum());
    }
    

    @Test
    public void createDebt_thenPutDebtByCurrentUserandDebtId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = new Debt(debt1.getId(), 
        debt1.getMoneysum(),
        debt1.getGetter(),
        debt1.getGiver(),
        debt1.getDeposite());
        debt2.setMoneysum(500);

        MvcResult result = mvc.perform(put("/user/me/debt/"+debt1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt2)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Debt debt = objectMapper.readValue(result.
        getResponse().getContentAsString(), Debt.class);

        List<Debt> found = debtRepository.findAll();

        assertThat(found).extracting("id").containsOnly(debt.getId());
        assertThat(found).extracting("moneysum").containsOnly(debt.getMoneysum());
    }

    @Test
    public void createDebt_whenPutDebtByCurrentUserandDebtId_thenDebtNotFound() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = new Debt(debt1.getId(), 
        debt1.getMoneysum(),
        debt1.getGetter(),
        debt1.getGiver(),
        debt1.getDeposite());
        debt2.setMoneysum(500);

        MvcResult result = mvc.perform(put("/user/me/debt/"+6515616)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt2)))
        .andDo(print())
        .andReturn();

        Optional<ResourceNotFoundException> e = Optional.ofNullable((ResourceNotFoundException) result.getResolvedException());
        assertThat(e).isNotEmpty();
        assertThat(e.get()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void createDebt_thenPutnullDebtByCurrentUserandDebtId() throws Exception {
        String name1 = "Jonas";
        String email1 = "jonas@mail.com";
        String name2 = "Petras";
        String email2 = "petras@mail.com";
        String jonasToken = createTestUser(name1, email1);
        createTestUser(name2, email2);

        User user1 = userRepository.findByEmail(email1).get();
        User user2 = userRepository.findByEmail(email2).get();

        Debt debt1 = createTestDebt(user1, user2);
        Debt debt2 = new Debt(0, 
        0,
        null,
        null,
        debt1.getDeposite());

        MvcResult result = mvc.perform(put("/user/me/debt/"+debt1.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jonasToken)
        .content(JsonUtil.toJson(debt2)))
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Debt debt = objectMapper.readValue(result.
        getResponse().getContentAsString(), Debt.class);

        List<Debt> found = debtRepository.findAll();

        assertThat(found).extracting("id").containsOnly(debt.getId());
        assertThat(found).extracting("moneysum").containsOnly(debt.getMoneysum());
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

    private Debt createTestDebt(User getter, User giver){
        Debt debt = new Debt(0, 100, getter, giver, null);
        debtRepository.saveAndFlush(debt);
        return debt;
    }
}