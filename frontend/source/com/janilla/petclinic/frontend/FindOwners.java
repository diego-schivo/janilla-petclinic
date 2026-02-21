package com.janilla.petclinic.frontend;

import java.util.List;
import java.util.Map;

import com.janilla.web.Render;

@Render(template = "findOwners.html")
public record FindOwners(String lastName, Map<String, List<String>> errors) {
}