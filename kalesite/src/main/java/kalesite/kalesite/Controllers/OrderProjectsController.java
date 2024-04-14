package kalesite.kalesite.Controllers;

import kalesite.kalesite.Services.ProjectsOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class OrderProjectsController {

    private final ProjectsOrderService projectsOrderService;

    @PostMapping("/order")
    public ResponseEntity<?> orderProject(@RequestParam String fullName, @RequestParam String phone,
                                          @RequestParam(required = false) String comment) {

        return ResponseEntity.ok(projectsOrderService.saveProject(fullName, phone, comment));
    }
}