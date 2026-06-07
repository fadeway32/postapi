package com.fadeway32.postapi.strategy.body;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.BodyType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

public class FormUrlEncodedBodyWriterStrategy implements BodyWriterStrategy {
    @Override
    public boolean supports(BodyType bodyType) {
        return BodyType.FORM_URLENCODED == bodyType;
    }

    @Override
    public void write(ClassicHttpRequest request, ApiRequest apiRequest) {
        List<NameValuePair> pairs = new ArrayList<>();
        apiRequest.formFields().forEach((key, value) -> pairs.add(new BasicNameValuePair(key, value)));
        request.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
    }
}
