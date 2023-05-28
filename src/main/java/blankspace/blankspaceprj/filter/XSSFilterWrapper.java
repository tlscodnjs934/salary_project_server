package blankspace.blankspaceprj.filter;

import io.jsonwebtoken.io.IOException;
import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;

public class XSSFilterWrapper extends HttpServletRequestWrapper {

    private byte[] rawData;

    public XSSFilterWrapper(HttpServletRequest request) {
        super(request);
        try {
            InputStream inputStream = request.getInputStream();
            this.rawData = replaceXSS(IOUtils.toByteArray(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // XSS replace
    private byte[] replaceXSS(byte[] data) {
        String strData = new String(data);
        strData = strData.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;").replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");

        return strData.getBytes();
    }

    private String replaceXSS(String value) {
        if(value != null) {
            value = value.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;").replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
        }
        return value;
    }

    //새로운 인풋스트림을 리턴하지 않으면 에러가 남
    @Override
    public ServletInputStream getInputStream() throws IOException, java.io.IOException {
        if(this.rawData == null) {
            return super.getInputStream();
        }

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.rawData);

        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                // TODO Auto-generated method stub
                return byteArrayInputStream.read();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean isReady() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isFinished() {
                // TODO Auto-generated method stub
                return false;
            }
        };
    }

    @Override
    public String getQueryString() {
        return replaceXSS(super.getQueryString());
    }


    @Override
    public String getParameter(String name) {
        return replaceXSS(super.getParameter(name));
    }


    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> params = super.getParameterMap();
        if(params != null) {
            params.forEach((key, value) -> {
                for(int i=0; i<value.length; i++) {
                    value[i] = replaceXSS(value[i]);
                }
            });
        }
        return params;
    }


    @Override
    public String[] getParameterValues(String name) {
        String[] params = super.getParameterValues(name);
        if(params != null) {
            for(int i=0; i<params.length; i++) {
                params[i] = replaceXSS(params[i]);
            }
        }
        return params;
    }


    @Override
    public BufferedReader getReader() throws IOException, java.io.IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), "UTF-8"));
    }


    private static Pattern[] patterns = new Pattern[] {
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    private String stripXSS(String value) {
        //System.out.println("stripXSS: value = "+value);
        if (value != null) {

            // NOTE: It's highly recommended to use the ESAPI library and
            // uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);
            // Avoid null characters

            value = value.replaceAll("\0", "");

            // Remove all sections that match a pattern
            for(Pattern scriptPattern : patterns){
                if(scriptPattern.matcher(value).matches()){
                    value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                }
            }
            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'","&apos;");
        }
        //System.out.println("result = "+value);
        return value;
    }
}