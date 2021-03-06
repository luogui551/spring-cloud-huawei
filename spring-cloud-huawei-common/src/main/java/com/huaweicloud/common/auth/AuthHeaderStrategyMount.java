package com.huaweicloud.common.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.huaweicloud.common.util.JsonUtils;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author GuoYl123
 * @Date 2020/1/10
 **/
public class AuthHeaderStrategyMount extends AuthHeaderStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthHeaderStrategyMount.class);

  private ExecutorService executor = Executors.newFixedThreadPool(1);

  private WatchService watchService;

  public AuthHeaderStrategyMount() {
    try {
      watchService = FileSystems.getDefault().newWatchService();
      Path p = Paths.get(DEFAULT_SECRET_AUTH_PATH);
      p.register(watchService,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_CREATE);
      executor.execute(new FileUpdateCheckThread(watchService));
    } catch (Exception e) {
      LOGGER.warn("get watch service failed.", e);
    }
  }

  @Override
  public void createAuthHeaders() {
    try {
      String content = new String(
          Files.readAllBytes(Paths.get(DEFAULT_SECRET_AUTH_PATH, DEFAULT_SECRET_AUTH_NAME)),
          "UTF-8");
      JsonNode data = JsonUtils.OBJ_MAPPER.readTree(content);
      JsonNode authNode = data.findValue("auth");
      decode(authNode);
    } catch (Exception e) {
      LOGGER.warn("read auth info from dockerconfigjson failed.", e);
    }
  }


  final class FileUpdateCheckThread implements Runnable {

    private WatchService service;

    private FileUpdateCheckThread(WatchService service) {
      this.service = service;
    }

    public void run() {
      while (true) {
        try {
          WatchKey watchKey = service.take();
          // 清理掉已发生的事件，否则会导致事件遗留，进入死循环
          watchKey.pollEvents();
          synchronized (this) {
            createAuthHeaders();
          }
          watchKey.reset();
        } catch (InterruptedException e) {
          LOGGER.error("error occured. detail : {}", e.getMessage());
        }
      }
    }
  }
}
