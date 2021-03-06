package br.com.caelum.restfulie.http.apache;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import br.com.caelum.restfulie.Response;
import br.com.caelum.restfulie.RestClient;
import br.com.caelum.restfulie.RestfulieException;
import br.com.caelum.restfulie.http.Headers;
import br.com.caelum.restfulie.http.Request;

public class ApacheResponse implements Response {

	private final HttpResponse response;
	private final RestClient client;
	private HttpEntity entity;
	private final Request details;

	public ApacheResponse(HttpResponse response, RestClient client, Request details) {
		this.response = response;
		this.client = client;
		this.details = details;
		this.entity = response.getEntity();
	}

	public int getCode() {
		return response.getStatusLine().getStatusCode();
	}

	public String getContent() {
		if (entity == null) {
			return "";
		}
		try {
			long len = entity.getContentLength();
			if (len < 10 * 1024 * 1024) {
				return EntityUtils.toString(entity);
			} else {
				return "";
			}
		} catch (IOException ex) {
			throw new RestfulieException("Unable to parse response content", ex);
		}
	}

	public List<String> getHeader(String key) {
		return getHeaders().get(key);
	}

	public <T> T getResource() {
		String contentType = getType();
		String content = getContent();
		return (T) client.getMediaTypes().forContentType(contentType)
				.unmarshal(content, client);
	}

	public String getType() {
		return getHeaders().getMain("Content-Type");
	}

	public Headers getHeaders() {
		return new ApacheHeaders(response,client);
	}

	public void discard() throws IOException {
		response.getEntity().consumeContent();
	}

	public URI getLocation() {
		try {
			String location = getHeaders().getFirst("Location");
			if(location == null || location.equals(""))
				return getRequest().getURI();
			else
				return new URI(location);
		} catch (URISyntaxException e) {
			throw new RestfulieException("Invalid URI received as a response", e);
		}
	}

	public Request getRequest() {
		return details;
	}

	public String getStatusLine() {
		return response.getStatusLine().getReasonPhrase();
	}

}