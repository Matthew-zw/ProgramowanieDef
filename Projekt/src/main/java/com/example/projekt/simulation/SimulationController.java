package com.example.projekt.simulation;

import com.example.projekt.dto.ProjectDTO;
import com.example.projekt.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final ProjectService projectService;
    private final SimulationService simulationService;

    @GetMapping("/{projectId}")
    public String showSimulationForm(@PathVariable Long projectId, Model model) {
        ProjectDTO project = projectService.getProjectById(projectId);
        SimulationRequest request = new SimulationRequest();
        request.setProjectId(projectId);
        request.setParitySymbols(8);
        request.setCorruptionLevel(2);

        model.addAttribute("project", project);
        model.addAttribute("simulationRequest", request);
        return "simulation/simulation-form";
    }

    @PostMapping("/run")
    public String runSimulation(@ModelAttribute SimulationRequest request, Model model) {
        SimulationResult result = simulationService.runSimulation(request);
        model.addAttribute("result", result);
        return "simulation/simulation-result";
    }
}