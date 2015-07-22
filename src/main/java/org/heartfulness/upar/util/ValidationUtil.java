package org.heartfulness.upar.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.heartfulness.upar.input.UparInput;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ValidationUtil {
    
    public String validateAndReturnToken(String regId) {
        // TODO Auto-generated method stub
        return regId;
    }
    public String validateAndReturnObject(UparInput input) {
        String msg = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mapper.writeValue(bos, input);
            msg = bos.toString().trim();
        } catch (JsonGenerationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return msg;
    }
}
