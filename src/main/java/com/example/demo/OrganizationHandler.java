package com.example.demo;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.demo.factory.ProxyResponseFactory;
import com.example.demo.lambda.ProxyRequest;
import com.example.demo.lambda.ProxyResponse;
import com.example.demo.model.Organization;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import com.amazonaws.services.lambda.runtime.Context;

public class OrganizationHandler implements RequestHandler<ProxyRequest, ProxyResponse> {

	static final Logger log = Logger.getLogger(OrganizationHandler.class);

	private ProxyResponseFactory factory;


	public OrganizationHandler() {
		factory = new ProxyResponseFactory();
	}

	@Override
	public ProxyResponse handleRequest(ProxyRequest input, Context context) {


		Organization organization = new Organization();
		organization.setName("DEMO NAME");

		log.info("Returning response " + input);
		return factory.createResponse(organization, HttpStatus.SC_OK, null);
	}
}
