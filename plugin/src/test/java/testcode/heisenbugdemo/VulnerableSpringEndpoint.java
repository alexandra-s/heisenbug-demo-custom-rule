package testcode.heisenbugdemo;

import org.springframework.web.bind.annotation.GetMapping;

public class VulnerableSpringEndpoint {

    @GetMapping("/idor")
    public String idor() {
        return "idor";
    }

    @GetMapping("/ok")
    public String ok() {
        if (canAccess()) {
            return "ok";
        } else {
            return "error";
        }
    }

    public String noAnnotation() {
        return "ok";
    }


    private boolean canAccess() {
        return true;
    }


}
