package pl.clinic.project.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import pl.clinic.project.model.Doctor;
import pl.clinic.project.service.DoctorService;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/doctors")
public class MvcDoctorController {

    private final DoctorService doctorService;

    public MvcDoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/addDoctor")
    @PreAuthorize("hasRole('ADMIN')")
    ModelAndView addNewDoctorPage() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin/addDoctor.html");
        return mav;
    }


    @PostMapping("/addDoctor")
    @PreAuthorize("hasRole('ADMIN')")
    String addNewDoctor(@Valid @ModelAttribute("doctor") Doctor doctor, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "error.html";
        }
        doctorService.createDoctor(doctor);
        return "redirect:/users/add";
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER_PATIENT', 'ADMIN')")
    ModelAndView showAllDoctors() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("doctors.html");
        List<Doctor> doctors = doctorService.getAll();
        mav.addObject("doctors", doctors);
        return mav;
    }


}
