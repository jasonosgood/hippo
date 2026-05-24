package normalhttp;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequest {
    public String method;
    public String path;
    public String version = "HTTP/1.1";
    public Map<String, String> headers = new LinkedHashMap<>();
    public String body = "";
}
