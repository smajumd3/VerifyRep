package com.ibm.workday.automation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

	@GetMapping("hello")
	public ModelAndView helloWorld(@RequestParam("name") String myName) {
		ModelAndView mv = new ModelAndView("index");
		mv.addObject("name", myName);
		return mv;
	}
}