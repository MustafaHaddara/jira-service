package com.ashkan.jira.service;

import com.ashkan.jira.JiraClient;
import com.ashkan.jira.auth.OAuthClient;
import com.ashkan.jira.model.JiraQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JiraService {
	private static final String SEARCH_URI = "/search";
	private static final String ISSUE_URI = "/issue/";

	private OAuthClient oAuthClient;
	private JiraClient jiraClient;

	@Value("${jira.base}")
	private String jiraBaseUrl;

	@Autowired
	public JiraService(OAuthClient oAuthClient, JiraClient jiraClient) {
		this.oAuthClient = oAuthClient;
		this.jiraClient = jiraClient;
	}

	// TODO: reconsider return types
	public void searchWithJql() {
		Optional<Exception> authResult = oAuthClient.authenticate();
		if (!authResult.isPresent()) {
			String requestUrl = jiraBaseUrl + SEARCH_URI;
			// TODO: can we have JQL as user input?!
			// TODO: JQL can be a separate object and configurable
			JiraQuery jiraQuery = new JiraQuery();
			jiraQuery.setJql("project = Office365Refresh AND status = \"In Progress\"");
			jiraQuery.setFields(new ArrayList<>(Arrays.asList("summary")));
			jiraQuery.setStartAt(0);
			jiraQuery.setMaxResults(10);
			ObjectMapper objectMapper = new ObjectMapper();
			String body = null;
			try {
				body = objectMapper.writeValueAsString(jiraQuery);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			HttpContent httpContent = ByteArrayContent.fromString(Json.MEDIA_TYPE, body);
			jiraClient.sendPostRequest(oAuthClient.getAccessToken(), oAuthClient.getVerificationCode(), requestUrl, httpContent);
		}
	}

	public void getDetailsForTicket() {
		Optional<Exception> authResult = oAuthClient.authenticate();
		if (!authResult.isPresent()) {
			String issueName = jiraClient.getIssueNameFromUser();
			String requestUrl = jiraBaseUrl + ISSUE_URI + issueName;
			jiraClient.sendGetRequest(oAuthClient.getAccessToken(), oAuthClient.getVerificationCode(), requestUrl);
		}
	}
}
