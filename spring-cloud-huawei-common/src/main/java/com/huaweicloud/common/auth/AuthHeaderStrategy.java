package com.huaweicloud.common.auth;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2020/1/10
 **/
public abstract class AuthHeaderStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthHeaderStrategy.class);

  protected static final String DEFAULT_SECRET_AUTH_PATH = "/opt/CSE/etc/auth";

  protected static final String DEFAULT_SECRET_AUTH_NAME = ".dockerconfigjson";

  private static final int EXPECTED_ARR_LENGTH = 2;

  private boolean runOverHWC = !StringUtils.isEmpty(System.getenv("KUBERNETES_SERVICE_HOST"));

  private Map<String, String> defaultAuthHeaders = Collections.emptyMap();

  private boolean loaded = false;

  protected Map<String, String> getHeaders() {
    if (!runOverHWC) {
      return defaultAuthHeaders;
    }
    if (!loaded) {
      synchronized (this) {
        if (!loaded) {
          createAuthHeaders();
        }
      }
    }
    return defaultAuthHeaders;
  }

  public abstract void createAuthHeaders();

  protected void decode(JsonNode authNode) throws IOException {
    if (authNode == null) {
      return;
    }
    Map<String, String> authHeaders = new HashMap<String, String>();
    String authStr = authNode.asText();
    String authBase64Decode = new String(Base64.decodeBase64(authStr), "UTF-8");
    String[] auths = authBase64Decode.split("@");
    String[] akAndShaAkSk = auths[1].split(":");
    if (auths.length != EXPECTED_ARR_LENGTH || akAndShaAkSk.length != EXPECTED_ARR_LENGTH) {
      LOGGER.error("get docker config failed. The data is not valid cause of unexpected format");
      return;
    }
    String project = auths[0];
    String ak = akAndShaAkSk[0];
    String shaAkSk = akAndShaAkSk[1];
    authHeaders.put("X-Service-AK", ak);
    authHeaders.put("X-Service-ShaAKSK", shaAkSk);
    authHeaders.put("X-Service-Project", project);
    defaultAuthHeaders = authHeaders;
    loaded = true;
  }
}
