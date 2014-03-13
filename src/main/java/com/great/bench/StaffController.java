package com.great.bench;

import com.mongodb.WriteResult;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.Plan;
import com.stripe.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class StaffController {

    @Autowired
    SpringMailSender mail;

    @Autowired
    private MongoTemplate mango;

    @RequestMapping(value = "/api/register", method = RequestMethod.POST)
    public ModelAndView register(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String card = req.getParameter("card");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("email", email);

        boolean isEmailUnique = !(mango.exists(new Query(Criteria.where("email").is(email)), "user"));

        if (isEmailUnique) {

            customerParams.put("card", card);
            customerParams.put("description", "Customer for the basic plan.");

            Customer newMember = Customer.create(customerParams);

            if (newMember.getEmail().equalsIgnoreCase(email)) {

                Map<String, Object> subscriptionParams = new HashMap<String, Object>();
                subscriptionParams.put("plan", "basic");

                newMember.createSubscription(subscriptionParams);

                mango.insert(new Member(email, password, newMember.getId(), "stripe", null, null));

            }

            customerParams.put("status", "success");

        } else {

            customerParams.put("status", "error");

        }

        return new ModelAndView("hello", customerParams);

    }

    @RequestMapping(value = "/api/deactivate", method = RequestMethod.POST)
    public ModelAndView deactivate(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String email = req.getParameter("email");

        Member member = mango.findOne(new Query(Criteria.where("email").is(email)), Member.class);

        Subscription subscription = Customer.retrieve(member.stripeId).cancelSubscription();

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", subscription.getStatus() == "canceled" ? "success" : "error");

        return new ModelAndView("hello", response);

    }

    @RequestMapping(value = "/api/reactivate", method = RequestMethod.POST)
    public ModelAndView reactivate(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String email = req.getParameter("email");

        Member member = mango.findOne(new Query(Criteria.where("email").is(email)), Member.class);

        Map<String, Object> subscriptionParams = new HashMap<String, Object>();
        subscriptionParams.put("plan", "basic");

        Subscription subscription = Customer.retrieve(member.stripeId).createSubscription(subscriptionParams);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", subscription.getStatus() == "active" ? "success" : "error");

        return new ModelAndView("hello", response);

    }

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        String sessionToken = Member.randomSessionToken();
        WriteResult mangoResult = mango.updateFirst(new Query(Criteria.where("email").is(email).and("password").is(password)),
                Update.update("sessionToken", sessionToken), Member.class);

        Map<String, Object> response = new HashMap<String, Object>();

        if (mangoResult.getN() > 0) {

            response.put("sessionToken", sessionToken);
            response.put("status", "success");

        } else {

            response.put("status", "error");

        }

        return new ModelAndView("hello", response);

    }

    @RequestMapping(value = "/api/logout", method = RequestMethod.POST)
     public ModelAndView logout(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String email = req.getParameter("email");

        WriteResult mangoResult = mango.updateFirst(new Query(Criteria.where("email").is(email)),
                Update.update("sessionToken", null), Member.class);

        Map<String, Object> response = new HashMap<String, Object>();

        if (mangoResult.getN() > 0) {

            response.put("status", "success");

        } else {

            response.put("status", "error");

        }

        return new ModelAndView("hello", response);

    }

    @RequestMapping(value = "/api/forgot", method = RequestMethod.POST)
    public ModelAndView sendForgotPasswordEmail(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String email = req.getParameter("email");

        WriteResult mangoResult = mango.updateFirst(new Query(Criteria.where("email").is(email)),
                Update.update("resetCode", Member.randomResetCode()), Member.class);

        mail.sendMail(new String[]{email},
                "Welcome to the Banch",
                "Dear %s,<br><br>Confirm your membership. <a href=\"http://www.neopets.com/?email=kanvus@gmail.com&code=TREre53\">LOOK!</a><br><br>Thanks,<br/>%s",
                new String[]{"dahling", "Polite Stare"});

        Map<String, Object> response = new HashMap<String, Object>();

        response.put("status", "success");

        return new ModelAndView("hello", response);

    }

    @RequestMapping(value = "/api/change", method = RequestMethod.POST)
    public ModelAndView changePassword(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        Map<String, Object> response = new HashMap<String, Object>();

        String email = req.getParameter("email");
        String code = req.getParameter("code");
        String password = req.getParameter("newPassword");

        Member member = mango.findOne(new Query(Criteria.where("email").is(email).and("resetCode").is(code)), Member.class);

        try {

            member.password = password;
            member.sessionToken = Member.randomSessionToken();
            member.resetCode = null;

            mango.save(member);

            response.put("sessionToken", member.sessionToken);
            response.put("status", "success");

        } catch (Exception e) {

            response.put("status", "error");
            e.printStackTrace();

        }

        return new ModelAndView("hello", response);

    }

    static {

        Stripe.apiKey = "sk_test_cAefVlVMmXfcSKMZOKLhielX";

        /* Making sure a 'basic' plan exists on Stripe if not already. */
        try {

            Plan.retrieve("basic");

        } catch (Exception e) {

            Map<String, Object> planParams = new HashMap<String, Object>();
            planParams.put("amount", 200);
            planParams.put("interval", "month");
            planParams.put("name", "Basic");
            planParams.put("currency", "usd");
            planParams.put("id", "basic");

            try {

                Plan.create(planParams);

            } catch (Exception ignored) {

                ignored.printStackTrace();

            }

        }
    }

}