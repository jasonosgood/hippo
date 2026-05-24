package normalhttp;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    public int status;
    public String statusText = "";
    public Map<String, String> headers = new LinkedHashMap<>();
    public String body = "";
}
