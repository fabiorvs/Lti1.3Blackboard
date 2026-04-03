package br.dev.fabiorvs.lti.model;

import lombok.Data;

import java.util.List;

@Data
public class LtiUserResponse {
    private String name;
    private String email;
    private List<String> roles;
}