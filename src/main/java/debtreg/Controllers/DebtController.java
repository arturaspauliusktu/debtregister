package debtreg.Controllers;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import debtreg.Entities.Debt;
import debtreg.Repositories.DebtRepository;

@RestController
public class DebtController {
     private final AtomicLong counter = new AtomicLong();

     @Autowired
     private DebtRepository repo;

     @GetMapping("/debts")
     public List<Debt> getDebts(){
          return (List<Debt>) repo.findAll();
     }

     @PostMapping("/debt")
     public void addDebt(@RequestParam Debt requestdebt){
          
     }
}